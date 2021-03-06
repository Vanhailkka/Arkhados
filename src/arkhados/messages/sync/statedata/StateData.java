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

package arkhados.messages.sync.statedata;

import arkhados.net.Command;
import com.jme3.network.serializing.Serializable;

@Serializable
public abstract class StateData implements Command {
    private short syncId = -1;
    
    public StateData() {
    }

    public StateData(int syncId) {
        this.syncId = (short) syncId;
    }
            
    public abstract void applyData(Object target);

    public int getSyncId() {
        return syncId;
    }

    @Override
    public boolean isGuaranteed() {
        return true; // This is default that some classes need to override
    }
    
    public void setSyncId(int syncId) {
        this.syncId = (short) syncId;
    }
}
