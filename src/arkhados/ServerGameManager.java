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

import arkhados.gamemode.GameMode;
import arkhados.messages.CmdTopicOnly;
import arkhados.net.Sender;
import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;

/**
 *
 * @author william
 */
public class ServerGameManager extends AbstractAppState {

    private WorldManager worldManager;
    private ServerFogManager fogManager;
    private boolean running = false;
    private Application app;
    private GameMode gameMode;

    public ServerGameManager(GameMode gameMode) {
        this.gameMode = gameMode;
        CharacterInteraction.gameMode = gameMode;
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        gameMode.initialize(app);
        worldManager = app.getStateManager().getState(WorldManager.class);
        fogManager = new ServerFogManager();

        stateManager.attach(fogManager);

        this.app = app;

        ServerMain serverApp = (ServerMain) app;
        serverApp.startGame();
    }

    public synchronized boolean startGame() {
        if (running) {
            return false;
        }

        Sender sender = app.getStateManager().getState(Sender.class);

        Preloader.loadServer(Globals.assetManager);
        app.getStateManager().getState(SyncManager.class)
                .addObject(-1, worldManager);

        running = true;
        sender.addCommand(new CmdTopicOnly(Topic.START_GAME));

        gameMode.startGame();

        return true;
    }

    @Override
    public void update(float tpf) {
        if (!running) {
            return;
        }

        gameMode.update(tpf);
    }

    @Override
    public void cleanup() {
        super.cleanup();
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public void playerJoined(int playerId) {
        gameMode.playerJoined(playerId);
    }
}