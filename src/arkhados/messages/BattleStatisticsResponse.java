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

import arkhados.CharacterInteraction;
import arkhados.net.Command;
import arkhados.util.PlayerRoundStats;
import com.jme3.network.serializing.Serializable;
import java.util.ArrayList;

@Serializable
public class BattleStatisticsResponse implements Command {

    private boolean teamStats;
    private ArrayList<PlayerRoundStats> playerRoundStatsList;

    public BattleStatisticsResponse() {
    }

    public ArrayList<PlayerRoundStats> getPlayerRoundStatsList() {
        return this.playerRoundStatsList;
    }

    public static BattleStatisticsResponse buildPlayerStatisticsResponse() {
        BattleStatisticsResponse message = new BattleStatisticsResponse();
        message.playerRoundStatsList = CharacterInteraction
                .getCurrentPlayerRoundStats().cloneCurrentStatsList();
        message.teamStats = false;
        return message;
    }

    public static BattleStatisticsResponse buildTeamStatisticsResponse() {
        BattleStatisticsResponse message = new BattleStatisticsResponse();
        message.playerRoundStatsList = CharacterInteraction
                .getCurrentTeamRoundStats().cloneCurrentStatsList();
        message.teamStats = true;
        return message;
    }

    @Override
    public boolean isGuaranteed() {
        return true;
    }

    public boolean isTeamStats() {
        return teamStats;
    }
}
