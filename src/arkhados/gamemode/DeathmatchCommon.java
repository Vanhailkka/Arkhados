/*    This file is part of Arkhados.

 Arkhados is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 Arkhados is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Arkhados.  If not, see <http://www.gnu.org/licenses/>. */
package arkhados.gamemode;

import arkhados.CharacterInteraction;
import arkhados.ClientMain;
import arkhados.Globals;
import arkhados.PlayerData;
import arkhados.ServerFog;
import arkhados.Sync;
import arkhados.Topic;
import arkhados.UserCommandManager;
import arkhados.World;
import arkhados.arena.AbstractArena;
import arkhados.controls.PlayerEntityAwareness;
import arkhados.effects.Death;
import arkhados.messages.CmdPlayerKill;
import arkhados.messages.CmdSetPlayersCharacter;
import arkhados.messages.CmdTopicOnly;
import arkhados.net.ClientSender;
import arkhados.net.Sender;
import arkhados.net.ServerSender;
import arkhados.ui.hud.ClientHud;
import arkhados.ui.hud.DeathMatchHeroSelectionLayerBuilder;
import arkhados.ui.hud.DeathMatchHeroSelectionLayerController;
import arkhados.util.AudioQueue;
import arkhados.util.NodeBuilderIdHeroNameMatcher;
import arkhados.util.RemovalReasons;
import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.audio.AudioNode;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.network.HostedConnection;
import com.jme3.util.IntMap;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.screen.Screen;
import java.util.concurrent.Callable;

public class DeathmatchCommon {

    private static final IntMap<String> spreeAnnouncements = new IntMap<>();
    private static final IntMap<String> comboAnnouncements = new IntMap<>();
    private static final String FIRST_BLOOD_PATH
            = "Interface/Sound/Announcer/FirstBlood.wav";

    static {
        spreeAnnouncements.put(3, "Interface/Sound/Announcer/KillingSpree.wav");
        spreeAnnouncements.put(4, "Interface/Sound/Announcer/MegaKill.wav");
        spreeAnnouncements.put(5, "Interface/Sound/Announcer/Dominating.wav");
        spreeAnnouncements.put(6, "Interface/Sound/Announcer/Ownage.wav");
        spreeAnnouncements.put(7, "Interface/Sound/Announcer/Mayhem.wav");
        spreeAnnouncements.put(8, "Interface/Sound/Announcer/Carnage.wav");
        spreeAnnouncements.put(9, "Interface/Sound/Announcer/Godlike.wav");

        comboAnnouncements.put(2, "Interface/Sound/Announcer/DoubleKill.wav");
        comboAnnouncements.put(3, "Interface/Sound/Announcer/TripleKill.wav");
        comboAnnouncements.put(4, "Interface/Sound/Announcer/Rampage.wav");
        comboAnnouncements.put(5, "Interface/Sound/Announcer/Massacre.wav");
    }
    private Application app;
    private World world;
    private AppStateManager states;
    private Sync sync;
    private final AudioQueue audioQueue = new AudioQueue();
    private boolean firstBloodHappened;
    private float respawnTime;
    private final IntMap<DeathMatchPlayerTracker> trackers = new IntMap<>();
    private int killLimit;
    private final IntMap<Boolean> canPickHeroMap = new IntMap<>();
    private Element heroSelectionLayer;
    private Nifty nifty;

    void initialize(Application app) {
        this.app = app;
        states = app.getStateManager();
        world = states.getState(World.class);
        sync = states.getState(Sync.class);

        CharacterInteraction.startNewRound();

        sync.addObject(-1, world);

        firstBloodHappened = false;

        if (states.getState(Sender.class).isServer()) {
            sync.setEnabled(true);
            sync.startListening();
            Globals.worldRunning = true;
        } else {
            states.getState(UserCommandManager.class).setEnabled(true);
            states.getState(ClientHud.class).clearMessages();

            preloadAnnouncer();
        }
    }

    void setNifty(final Nifty nifty) {
        this.nifty = nifty;

        if (Globals.replayMode) {
            return;
        }

        DeathMatchHeroSelectionLayerBuilder layerBuilder
                = new DeathMatchHeroSelectionLayerBuilder();

        Screen screen = nifty.getScreen("default_hud");

        heroSelectionLayer = layerBuilder
                .build(nifty, screen, screen.getRootElement());
        heroSelectionLayer.hideWithoutEffect();

        DeathMatchHeroSelectionLayerController control = heroSelectionLayer
                .getControl(DeathMatchHeroSelectionLayerController.class);
        control.setStateManager(states);
    }

    void update(float tpf) {
        audioQueue.update();

        for (IntMap.Entry<DeathMatchPlayerTracker> entry : trackers) {                    
            entry.getValue().update(tpf);
        }
    }

    void clientHandleTopicOnlyCommand(CmdTopicOnly command) {
        switch (command.getTopicId()) {
            case Topic.GAME_ENDED:
                gameEnded();
                break;
            case Topic.FIRST_BLOOD_HAPPENED:
                firstBloodHappened = true;
                break;
        }
    }

    void serverHandleTopicOnlyCommand(HostedConnection source,
            CmdTopicOnly command) {
        switch (command.getTopicId()) {
            case Topic.CLIENT_WORLD_CREATED:
                if (firstBloodHappened) {
                    ServerSender sender = states.getState(ServerSender.class);
                    sender.addCommandForSingle(
                            new CmdTopicOnly(Topic.FIRST_BLOOD_HAPPENED),
                            source);
                }
                break;
        }
    }

    void playerDied(int playerId, int killersPlayerId) {
        boolean deathByEnvironment = killersPlayerId < 0;

        DeathMatchPlayerTracker dead = trackers.get(playerId);
        int endedSpree = dead.getKillingSpree();

        dead.death(respawnTime, deathByEnvironment);

        canPickHeroMap.put(playerId, Boolean.TRUE);

        Sender sender = states.getState(ServerSender.class);

        int killingSpree = 0;
        int combo = 0;
        if (!deathByEnvironment) {
            DeathMatchPlayerTracker killer = trackers.get(killersPlayerId);
            killer.addKill();

            killingSpree = killer.getKillingSpree();
            combo = killer.getCombo();
        }

        sender.addCommand(new CmdPlayerKill(playerId, killersPlayerId,
                killingSpree, combo, endedSpree));
    }

    void clientPlayerDied(int playerId, int killersId,
            int killingSpree, int combo, int endedSpree) {
        int myPlayer = states.getState(UserCommandManager.class).getPlayerId();

        String playerName = getPlayerName(playerId);
        String killerName = getPlayerName(killersId);

        killedMessage(playerName, killerName, endedSpree);

        firstBloodMessage(killersId);

        comboMessage(killerName, combo);

        killingSpreeMessage(killerName, killingSpree);
        if (playerId == myPlayer) {
            handleOwnDeath();
        }
    }

    void preparePlayer(final int playerId) {
        app.enqueue(() -> {
            DeathMatchPlayerTracker tracker = new DeathMatchPlayerTracker(0.5f);
            getTrackers().put(playerId, tracker);

            ServerFog fog = states.getState(ServerFog.class);
            if (fog != null) { // Same as asking for if this is server
                PlayerEntityAwareness awareness
                        = fog.createAwarenessForPlayer(playerId);
                fog.teachAboutPrecedingEntities(awareness);

                getCanPickHeroMap().put(playerId, Boolean.TRUE);
                CharacterInteraction.addPlayer(playerId);
            }

            return null;
        });
    }

    void playerChoseHero(final int playerId, final String heroName) {
        Boolean canPickHero = canPickHeroMap.get(playerId);
        if (canPickHero == null || Boolean.FALSE.equals(canPickHero)) {
            return;
        }
        canPickHeroMap.put(playerId, Boolean.FALSE);

        long delay = (long) trackers.get(playerId).getSpawnTimeLeft() * 1000;
        if (delay < 0) {
            delay = 100;
        }

        final Callable<Void> callable = () -> {
            int oldEntityId = PlayerData
                    .getIntData(playerId, PlayerData.ENTITY_ID);
            world.removeEntity(oldEntityId, RemovalReasons.DEATH);

            int teamId = PlayerData.getIntData(playerId, PlayerData.TEAM_ID);

            Vector3f startingLocation = getNewSpawnLocation(teamId);
            PlayerData playerData = PlayerData.getPlayerId(playerId);

            int nodeBuilderId = NodeBuilderIdHeroNameMatcher
                    .get().getId(heroName);
            int entityId = world.addNewEntity(nodeBuilderId,
                    startingLocation, new Quaternion(), playerId);
            playerData.setData(PlayerData.ENTITY_ID, entityId);

            CmdSetPlayersCharacter cmdPlayersCharacter
                    = new CmdSetPlayersCharacter(entityId, playerId);

            states.getState(ServerSender.class).addCommand(cmdPlayersCharacter);

            return null;
        };

        new java.util.Timer().schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                getApp().enqueue(callable);
            }
        }, delay);
    }

    void gameEnded() {
        final Sender sender = states.getState(Sender.class);

        if (sender.isClient()) {

            final ClientHud hud = states.getState(ClientHud.class);
            getApp().enqueue(() -> {
                hud.clear();
                hud.showStatistics();
                nifty.removeElement(nifty.getScreen("default_hud"),
                        getHeroSelectionLayer());
                return null;
            });

            getApp().enqueue(() -> {
                states.getState(Sync.class).clear();
                // TODO: Find out why following line causes statistics to not appear
                //  stateManager.getState(UserCommandManager.class).nullifyCharacter();
                states.getState(ClientHud.class).disableCCharacterHud();
                return null;
            });

            final Callable<Void> callable = () -> {
                if (sender instanceof ClientSender) {
                    ((ClientSender) sender).getClient().close();
                }

                PlayerData.destroyAllData();
                hud.endGame();
                states.getState(World.class).clear();
                states.getState(UserCommandManager.class).nullifyCharacter();
                ((ClientMain) getApp()).gameEnded();
                getTrackers().clear();
                return null;
            };

            new java.util.Timer().schedule(new java.util.TimerTask() {
                @Override
                public void run() {
                    getApp().enqueue(callable);
                }
            }, 15000);
        }
    }

    private Vector3f getNewSpawnLocation(int teamId) {
        AbstractArena arena = states.getState(World.class).getArena();
        return arena.getSpawnPoint(teamId).setY(1f);
    }

    private void handleOwnDeath() {
        getApp().enqueue(() -> {
            UserCommandManager userCommandManager
                    = states.getState(UserCommandManager.class);
            int characterId = userCommandManager.getCharacterId();

            world.removeEntity(characterId, RemovalReasons.DEATH); // TODO: Get rid of this

            userCommandManager.nullifyCharacter();
            ClientHud hud = states.getState(ClientHud.class);

            hud.clearAllButCharactersInfo();

            hud.showStatistics();

            states.getState(Death.class).death();
            if (!Globals.replayMode) {
                getHeroSelectionLayer().showWithoutEffects();
            }

            return null;
        });
    }

    private void killedMessage(String playerName, String killerName,
            int endedSpree) {
        String message = DeathMatchMessageMaker
                .killed(playerName, killerName, endedSpree);
        states.getState(ClientHud.class).addMessage(message);
    }

    private void firstBloodMessage(int killersId) {
        if (firstBloodHappened || killersId < 0) {
            return;
        }

        firstBloodHappened = true;

        String name = getPlayerName(killersId);

        String message = String.format("%s just drew First Blood!", name);
        states.getState(ClientHud.class).addMessage(message);

        playAnnouncerSound(FIRST_BLOOD_PATH);
    }

    private void killingSpreeMessage(String playerName, int spree) {
        if (spree < 3) {
            return;
        } else if (spree > 9) {
            spree = 9;
        }

        String message = DeathMatchMessageMaker.spree(playerName, spree);
        states.getState(ClientHud.class).addMessage(message);

        String audioPath = spreeAnnouncements.get(spree);

        playAnnouncerSound(audioPath);
    }

    private void comboMessage(String playerName, int combo) {
        if (combo < 2) {
            return;
        } else if (combo > 5) {
            combo = 5;
        }

        String message = DeathMatchMessageMaker.combo(playerName, combo);
        states.getState(ClientHud.class).addMessage(message);

        String audioPath = comboAnnouncements.get(combo);

        playAnnouncerSound(audioPath);
    }

    private String getPlayerName(int id) {
        return id < 0
                ? "Environment"
                : PlayerData.getStringData(id, PlayerData.NAME);
    }

    private void preloadAnnouncer() {
        Globals.assets.loadAudio(FIRST_BLOOD_PATH);

        for (IntMap.Entry<String> entry : spreeAnnouncements) {                    
            Globals.assets.loadAudio(entry.getValue());
        }
    }

    private void playAnnouncerSound(final String path) {
        AudioNode audio = new AudioNode(Globals.assets, path);
        audio.setVolume(1.2f);
        audio.setPositional(false);

        audioQueue.enqueueAudio(audio);
    }

    int getKillLimit() {
        return killLimit;
    }

    Element getHeroSelectionLayer() {
        return heroSelectionLayer;
    }

    IntMap<Boolean> getCanPickHeroMap() {
        return canPickHeroMap;
    }

    private Application getApp() {
        return app;
    }

    IntMap<DeathMatchPlayerTracker> getTrackers() {
        return trackers;
    }

    public void setKillLimit(int killLimit) {
        this.killLimit = killLimit;
    }

    public void setRespawnTime(float respawnTime) {
        this.respawnTime = respawnTime;
    }
}
