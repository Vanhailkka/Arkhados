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
package arkhados.spell.spells.rockgolem;

import arkhados.CharacterInteraction;
import arkhados.CollisionGroups;
import arkhados.PlayerData;
import arkhados.World;
import arkhados.actions.ATrance;
import arkhados.actions.EntityAction;
import arkhados.controls.CActionQueue;
import arkhados.controls.CInfluenceInterface;
import arkhados.util.RemovalReasons;
import arkhados.util.UserData;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

public class SpiritStoneCollisionListener implements PhysicsCollisionListener {

    private Node myStone;
    private World world;
    private static final float M1_COMBINATION_DAMAGE = 300f;

    public SpiritStoneCollisionListener(Node myStone, World world) {
        this.myStone = myStone;
        this.world = world;
    }

    @Override
    public void collision(PhysicsCollisionEvent event) {
        boolean isA = myStone == event.getNodeA();
        boolean isB = myStone == event.getNodeB();
        if (!isA && !isB) {
            return;
        }

        Spatial other = isA ? event.getNodeB() : event.getNodeA();

        if (other == null) {
            return;
        }

        int myCollisionGroup = isA ? event.getObjectA().getCollisionGroup()
                : event.getObjectB().getCollisionGroup();

        if (myCollisionGroup == CollisionGroups.NONE) {
            return;
        }

        PhysicsCollisionObject otherPhysics = isA ? event.getObjectB()
                : event.getObjectA();
        int otherCollisionGroup = otherPhysics.getCollisionGroup();

        CSpiritStonePhysics stonePhysics =
                myStone.getControl(CSpiritStonePhysics.class);

        int stoneId = myStone.getUserData(UserData.ENTITY_ID);

        Integer otherTeamId = other.getUserData(UserData.TEAM_ID);
        if (otherTeamId == null) {
            if (stonePhysics.isPunched()) {
                world.removeEntity(stoneId, RemovalReasons.COLLISION);
            } else {
            }
            return;
        }
        int myTeamId = myStone.getUserData(UserData.TEAM_ID);

        CInfluenceInterface influenceInterface =
                other.getControl(CInfluenceInterface.class);
        if (influenceInterface != null && stonePhysics.isPunched()
                && !otherTeamId.equals(myTeamId)) {

            CActionQueue cQueue = other.getControl(CActionQueue.class);
            EntityAction currentAction = cQueue.getCurrent();

            int ownerId = myStone.getUserData(UserData.PLAYER_ID);
            int playerEntityId = PlayerData
                    .getIntData(ownerId, PlayerData.ENTITY_ID);
            Spatial playerEntity = world.getEntity(playerEntityId);

            if (currentAction != null && currentAction instanceof ATrance) {
                ((ATrance) currentAction).activate(playerEntity);
                world.removeEntity(stoneId, RemovalReasons.COLLISION);
                return;
            }

            CInfluenceInterface playerInterface =
                    playerEntity.getControl(CInfluenceInterface.class);

            CharacterInteraction.harm(playerInterface, influenceInterface,
                    M1_COMBINATION_DAMAGE, null, true);
            world.removeEntity(stoneId, RemovalReasons.COLLISION);
        } else if (stonePhysics.isPunched()
                && otherCollisionGroup == CollisionGroups.WALLS) {
            world.removeEntity(stoneId, RemovalReasons.COLLISION);
        }
    }

    public void setWorld(World world) {
        this.world = world;
    }
}
