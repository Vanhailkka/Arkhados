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

import arkhados.messages.roundprotocol.RoundFinishedMessage;
import arkhados.messages.roundprotocol.NewRoundMessage;
import com.jme3.network.serializing.Serializer;
import arkhados.messages.roundprotocol.ClientWorldCreatedMessage;
import arkhados.messages.roundprotocol.PlayerReadyForNewRoundMessage;
import arkhados.messages.roundprotocol.CreateWorldMessage;
import arkhados.messages.syncmessages.AddEntityMessage;
import arkhados.messages.syncmessages.RemoveEntityMessage;
import arkhados.messages.syncmessages.StartCastingSpellMessage;
import arkhados.messages.syncmessages.SyncCharacterMessage;
import arkhados.messages.syncmessages.SyncProjectileMessage;
import arkhados.messages.usercommands.UcCastSpellMessage;
import arkhados.messages.usercommands.UcMouseTargetMessage;
import arkhados.messages.usercommands.UcWalkDirection;

/**
 *
 * @author william
 */
public class MessageUtils {

    public static void registerMessages() {

        // <Lobby>
        Serializer.registerClass(ServerLoginMessage.class);
        Serializer.registerClass(ClientLoginMessage.class);
        Serializer.registerClass(PlayerDataTableMessage.class);
        Serializer.registerClass(ChatMessage.class);
        Serializer.registerClass(StartGameMessage.class);
        Serializer.registerClass(ClientSelectHeroMessage.class);
        // </Lobby>

        // <RoundProtocol>
        Serializer.registerClass(CreateWorldMessage.class);
        Serializer.registerClass(ClientWorldCreatedMessage.class);
        Serializer.registerClass(PlayerReadyForNewRoundMessage.class);
        Serializer.registerClass(NewRoundMessage.class);
        Serializer.registerClass(RoundFinishedMessage.class);
        // </RoundProtocol>

        Serializer.registerClass(SetPlayersCharacterMessage.class);

        // <Sync>
        Serializer.registerClass(AddEntityMessage.class);
        Serializer.registerClass(RemoveEntityMessage.class);
        Serializer.registerClass(SyncCharacterMessage.class);
        Serializer.registerClass(SyncProjectileMessage.class);
        Serializer.registerClass(StartCastingSpellMessage.class);
        // </Sync>

        // <UserCommands>
        Serializer.registerClass(UcCastSpellMessage.class);
        Serializer.registerClass(UcWalkDirection.class);
        Serializer.registerClass(UcMouseTargetMessage.class);
        // </UserCommands>
    }
}