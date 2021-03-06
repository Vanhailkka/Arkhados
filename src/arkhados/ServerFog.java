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
package arkhados;

import arkhados.controls.CEntityVariable;
import arkhados.controls.CInfluenceInterface;
import arkhados.controls.PlayerEntityAwareness;
import arkhados.messages.sync.CmdAddEntity;
import arkhados.messages.sync.CmdBuff;
import arkhados.messages.sync.CmdRemoveEntity;
import arkhados.net.Command;
import arkhados.net.ServerSender;
import arkhados.spell.buffs.AbstractBuff;
import arkhados.util.ConnectionHelper;
import arkhados.util.RemovalReasons;
import arkhados.settings.server.Settings;
import arkhados.util.UserData;
import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.network.HostedConnection;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages things so that players are not aware of other entities behind walls
 * or too far away TODO: ServerFog is too complex and error prone.
 * Refactor it
 */
public class ServerFog extends AbstractAppState {

    private static final Logger logger =
            Logger.getLogger(ServerFog.class.getName());
    private Application app;
    private final Map<PlayerEntityAwareness, HostedConnection> awarenessConnectionMap =
            new LinkedHashMap<>();
    private Node walls;
    private float checkTimer = 0;
    private World world;

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        this.app = app;
        world = stateManager.getState(World.class);
    }

    @Override
    public void update(float tpf) {
        checkTimer -= tpf;
        if (checkTimer > 0) {
            return;
        }

        checkTimer = Settings.get().General().getDefaultSyncFrequency() / 2f;

        for (PlayerEntityAwareness playerEntityAwareness
                : awarenessConnectionMap.keySet()) {
            playerEntityAwareness.update(tpf);
        }
    }

    public void addCommand(Spatial spatial, Command command) {
        ServerSender sender =
                app.getStateManager().getState(ServerSender.class);

        for (Map.Entry<PlayerEntityAwareness, HostedConnection> entry
                : awarenessConnectionMap.entrySet()) {
            PlayerEntityAwareness awareness = entry.getKey();
            if (awareness.isAwareOf(spatial)) {
                sender.addCommandForSingle(command, entry.getValue());
            }
        }
    }

    public void createNewEntity(Spatial spatial, Command command) {
        ServerSender sender =
                app.getStateManager().getState(ServerSender.class);

//        PlayerEntityAwareness myAwareness = searchForAwareness(spatial);

        for (Map.Entry<PlayerEntityAwareness, HostedConnection> entry
                : awarenessConnectionMap.entrySet()) {
            PlayerEntityAwareness awareness = entry.getKey();
            awareness.addEntity(spatial);
            if (awareness.testVisibility(spatial)) {
                sender.addCommandForSingle(command, entry.getValue());
            }

            // This is at least temporarily disabled because it seems to cause problems and it's
            // not clear what its benefits are
//            if (awareness != myAwareness && myAwareness != null) {
//                if (myAwareness.testVisibility(awareness.getOwnSpatial()) &&
//                        !myAwareness.isAwareOf(awareness.getOwnSpatial())) {
////                    visibilityChanged(myAwareness, awareness.getOwnSpatial(), true);
//                }
//            }
        }
    }

    public void removeEntity(Spatial spatial, Command command) {
        ServerSender sender =
                app.getStateManager().getState(ServerSender.class);

        for (Map.Entry<PlayerEntityAwareness, HostedConnection> entry
                : awarenessConnectionMap.entrySet()) {
            PlayerEntityAwareness awareness = entry.getKey();
            if (awareness.removeEntity(spatial)) {
                sender.addCommandForSingle(command, entry.getValue());
            }

            if (awareness.getOwnSpatial() == spatial) {
                int entityId = spatial.getUserData(UserData.ENTITY_ID);
                logger.log(Level.INFO,
                        "Character with id {0} belonged for player with id {1}."
                        + " Nulling",
                        new Object[]{entityId, awareness.getPlayerId()});
                awareness.setOwnSpatial(null);
            }
        }
    }

    public void visibilityChanged(PlayerEntityAwareness awareness,
            Spatial target, boolean sees) {
        int entityId = target.getUserData(UserData.ENTITY_ID);

//        logger.log(Level.INFO, "Visibility of target {0} changed for awareness {1}. Sees: {2}",
//                new Object[]{entityId, awareness.getPlayerId(), sees});

        ServerSender sender =
                app.getStateManager().getState(ServerSender.class);

        if (sees) {
            int nodeBuilderId =
                    target.getUserData(UserData.NODE_BUILDER_ID);
            int playerId = target.getUserData(UserData.PLAYER_ID);
            float birthTime = target.getUserData(UserData.BIRTHTIME);
            float age = world.getWorldTime() - birthTime;
            Object healthMaybe =
                    target.getUserData(UserData.HEALTH_CURRENT);
            if (healthMaybe != null) {
                float health = (float) healthMaybe;
                if (health <= 0f) {
                    age = -1f;
                }
            }

            Vector3f location;
            Quaternion rotation;

            RigidBodyControl body = target.getControl(RigidBodyControl.class);
            if (body != null) {
                location = body.getPhysicsLocation();
                rotation = body.getPhysicsRotation();
            } else {
                location = target.getLocalTranslation();
                rotation = target.getLocalRotation();
            }
            Command command = new CmdAddEntity(entityId, nodeBuilderId,
                    location, rotation, playerId, age);
            sender.addCommandForSingle(command,
                    awarenessConnectionMap.get(awareness));

            CInfluenceInterface influenceInterface =
                    target.getControl(CInfluenceInterface.class);
            if (influenceInterface != null) {
                informAboutBuffs(sender, awareness,
                        influenceInterface.getBuffs());
            }
        } else {
            Command command = new CmdRemoveEntity(entityId,
                    RemovalReasons.DISAPPEARED);
            sender.addCommandForSingle(command,
                    awarenessConnectionMap.get(awareness));
        }
    }

    private <T extends AbstractBuff> void informAboutBuffs(ServerSender sender,
            PlayerEntityAwareness awareness, List<T> buffs) {
        for (AbstractBuff abstractBuff : buffs) {
            CmdBuff command = abstractBuff.generateBuffCommand(true);
            if (command != null) {
                command.setJustCreated(false);
                sender.addCommandForSingle(command,
                        awarenessConnectionMap.get(awareness));
            }
        }
    }

    public void addPlayerListToPlayers() {
        for (PlayerEntityAwareness awareness
                : awarenessConnectionMap.keySet()) {
            for (PlayerEntityAwareness awareness2
                    : awarenessConnectionMap.keySet()) {
                awareness.addEntity(awareness2.getOwnSpatial());
            }
        }
    }

    public PlayerEntityAwareness createAwarenessForPlayer(int playerId) {
        PlayerEntityAwareness playerAwareness =
                new PlayerEntityAwareness(playerId, walls, this);

        HostedConnection connection = ConnectionHelper.getSource(playerId);
        if (connection == null) {
            NullPointerException npe = new NullPointerException(
                    "Connection for playerId " + playerId + "is null!");
            logger.log(Level.WARNING, "", npe);
            throw npe;
        }

        awarenessConnectionMap.put(playerAwareness, connection);
        return playerAwareness;
    }

    public void teachAboutPrecedingEntities(PlayerEntityAwareness awareness) {
        // TODO IMPORTANT: This is not enough. There might be something near player at spawn time
        for (PlayerEntityAwareness otherAwareness
                : awarenessConnectionMap.keySet()) {
            if (otherAwareness == awareness) {
                break;
            }

            awareness.addEntity(otherAwareness.getOwnSpatial());
        }
    }

    public void registerCharacterForPlayer(int playerId, Spatial character) {
        int entityId = character.getUserData(UserData.ENTITY_ID);
        logger.log(Level.INFO, "Registering character with id {0}"
                + " for player with id {1}",
                new Object[]{entityId, playerId});
        for (PlayerEntityAwareness playerEntityAwareness
                : awarenessConnectionMap.keySet()) {
            if (playerEntityAwareness.getPlayerId() == playerId) {
                playerEntityAwareness.setOwnSpatial(character);
                character.getControl(CEntityVariable.class)
                        .setAwareness(playerEntityAwareness);
                break;
            }
        }
    }

    public void setWalls(Node walls) {
        this.walls = walls;
    }

    public void clearAwarenesses() {
        for (PlayerEntityAwareness playerEntityAwareness
                : awarenessConnectionMap.keySet()) {
            Spatial spatial = playerEntityAwareness.getOwnSpatial();
            if (spatial != null) {
                spatial.getControl(CEntityVariable.class)
                        .setAwareness(null);
            }
        }

        awarenessConnectionMap.clear();
    }

    public void removeConnection(HostedConnection connection) {
        awarenessConnectionMap.values().remove(connection);
    }
}
