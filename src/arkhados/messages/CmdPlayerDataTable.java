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

package arkhados.messages;

import com.jme3.network.serializing.Serializable;
import java.util.List;
import arkhados.PlayerData;
import arkhados.net.Command;
/**
 *
 * @author william
 */
@Serializable
public class CmdPlayerDataTable implements Command {    
    private List<PlayerData> playerDataList;

    public CmdPlayerDataTable() {

    }
    private CmdPlayerDataTable(List<PlayerData> playerDataList) {
        this.playerDataList = playerDataList;
    }

    public List<PlayerData> getPlayerData() {
        return this.playerDataList;        
    }

    public static CmdPlayerDataTable makeFromPlayerDataList() {        
        return new CmdPlayerDataTable(PlayerData.getPlayers());
    }

    @Override
    public boolean isGuaranteed() {
        return true;
    }
}