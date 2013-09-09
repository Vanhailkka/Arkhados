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
package arkhados.messages.usercommands;

import com.jme3.math.Vector3f;
import com.jme3.network.serializing.Serializable;
import com.jme3.scene.Node;
import arkhados.controls.ActionQueueControl;
import arkhados.controls.CharacterPhysicsControl;
import arkhados.controls.InfluenceInterfaceControl;
import arkhados.messages.syncmessages.AbstractSyncMessage;
import arkhados.util.UserDataStrings;

/**
 *
 * @author william
 */
@Serializable
public class UcWalkDirection extends AbstractSyncMessage {

    private int down;
    private int right;

    public UcWalkDirection() {
    }

    public UcWalkDirection(int down, int right) {
        this.down = down;
        this.right = right;
    }

    @Override
    public void applyData(Object target) {
        Node character = (Node) target;
        CharacterPhysicsControl characterControl = character.getControl(CharacterPhysicsControl.class);
        if (characterControl == null) {
            return;
        }
        if (character.getControl(InfluenceInterfaceControl.class).canMove()) {
            // FIXME: canMove is too coarse. If character is snared, it can still turn around
            Vector3f walkDirection = new Vector3f(this.right, 0f, this.down);
            Float speedMovement = character.getUserData(UserDataStrings.SPEED_MOVEMENT);
            walkDirection.normalizeLocal().multLocal(speedMovement);
            characterControl.setWalkDirection(walkDirection);
            if (down != 0 || right != 0) {
                character.getControl(ActionQueueControl.class).clear();
                characterControl.setViewDirection(walkDirection);
            }
            character.getControl(ActionQueueControl.class).clear();
        }

    }
}
