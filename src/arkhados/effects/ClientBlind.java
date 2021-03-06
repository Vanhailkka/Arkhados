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
package arkhados.effects;

import arkhados.ClientFog;
import arkhados.Globals;
import arkhados.UserCommandManager;
import arkhados.spell.buffs.info.BuffInfoParameters;
import arkhados.util.UserData;
import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.math.ColorRGBA;
import com.jme3.post.FilterPostProcessor;
import com.jme3.scene.Spatial;
import com.shaderblow.filter.colorscale.ColorScaleFilter;
import java.util.ArrayList;
import java.util.List;

public class ClientBlind extends AbstractAppState {

    private final List<BlindEffect> blinds = new ArrayList<>();
    private AppStateManager stateManager;
    private final FilterPostProcessor fpp
            = new FilterPostProcessor(Globals.assets);
    private ColorScaleFilter filter;
    private Application app;

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        this.app = app;
        this.stateManager = stateManager;
        fpp.setNumSamples(1);
    }

    void addBlindIfSelf(BlindEffect blind, BuffInfoParameters params) {
        int myCharacterId = stateManager.getState(UserCommandManager.class)
                .getCharacterId();
        Spatial spatial = params.buffControl.getSpatial();
        int entityId = spatial.getUserData(UserData.ENTITY_ID);

        if (entityId == myCharacterId) {
            addEffect();
            blinds.add(blind);
        }
    }

    private void addEffect() {
        if (!blinds.isEmpty()) {
            return;
        }

        stateManager.getState(ClientFog.class).addPreventer();
        
        // TODO: Add more impressive blind effect
        filter = new ColorScaleFilter(ColorRGBA.White.clone(), 0.4f);
        fpp.addFilter(filter);
        app.getViewPort().addProcessor(fpp);
    }

    public void removeBuffIfSelf(BlindEffect blind) {
        blinds.remove(blind);
        
        stateManager.getState(ClientFog.class).removePreventer();

        if (blinds.isEmpty()) {
            clean();
        }
    }

    private void clean() {
        if (fpp != null) {
            fpp.removeAllFilters();
            app.getViewPort().removeProcessor(fpp);
            filter = null;
        }
    }

    @Override
    public void cleanup() {
        super.cleanup();
        clean();
    }
}
