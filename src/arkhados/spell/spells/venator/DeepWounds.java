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
package arkhados.spell.spells.venator;

import arkhados.CharacterInteraction;
import arkhados.actions.ACharge;
import arkhados.actions.EntityAction;
import arkhados.characters.Venator;
import arkhados.controls.CActionQueue;
import arkhados.controls.CCharacterPhysics;
import arkhados.controls.CInfluenceInterface;
import arkhados.spell.Spell;
import arkhados.spell.buffs.AbstractBuff;
import arkhados.spell.buffs.AbstractBuffBuilder;
import arkhados.util.BuffTypeIds;
import arkhados.util.UserData;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;


public class DeepWounds extends Spell {

    {
        iconName = "deep_wounds.png";
        setMoveTowardsTarget(true);
    }

    public DeepWounds(String name, float cooldown, float range,
            float castTime) {
        super(name, cooldown, range, castTime);
    }

    public static DeepWounds create() {
        final float cooldown = 8f;
        final float range = 50f;
        final float castTime = 0.3f;

        final DeepWounds spell = new DeepWounds("Deep Wounds", cooldown, range,
                castTime);

        spell.castSpellActionBuilder = (Node caster, Vector3f vec)
                -> new ACastDeepWounds(spell);

        return spell;
    }
}

class ACastDeepWounds extends EntityAction {

    private final DeepWounds spell;

    public ACastDeepWounds(DeepWounds spell) {
        this.spell = spell;
        setTypeId(Venator.ANIM_SWIPE_UP);
    }

    @Override
    public boolean update(float tpf) {
        ACharge charge = new ACharge(spell.getRange());
        charge.setHitDamage(100f);
        charge.setChargeSpeed(255f);
        spatial.getControl(CActionQueue.class).enqueueAction(charge);

        float damageFactor = spatial.getUserData(UserData.DAMAGE_FACTOR);

        AbstractBuffBuilder bleedBuff = new BleedBuff.MyBuilder(4.2f)
                .damagePerUnit(2 * damageFactor);

        bleedBuff.setOwnerInterface(spatial
                .getControl(CInfluenceInterface.class));

        charge.addBuff(bleedBuff);

        // TODO: MAKE SURE it's okay to disable this
        // spatial.getControl(UserInputControl.class).restoreWalking();
        return false;
    }
}

class BleedBuff extends AbstractBuff {

    private CCharacterPhysics physics = null;
    private Spatial spatial = null;
    private float dmgPerUnit = 2f;

    {
        name = "Deep Wounds";
    }

    private BleedBuff(float duration) {
        super(duration);
    }

    @Override
    public void attachToCharacter(CInfluenceInterface targetInterface) {
        super.attachToCharacter(targetInterface);
        spatial = targetInterface.getSpatial();
        physics = spatial.getControl(CCharacterPhysics.class);
    }

    @Override
    public void update(float time) {
        super.update(time);
        if (physics.getWalkDirection().equals(Vector3f.ZERO)) {
            return;
        }

        float speed = spatial.getUserData(UserData.SPEED_MOVEMENT);
        float dmg = speed * time * dmgPerUnit;
        CharacterInteraction.harm(getOwnerInterface(), targetInterface, dmg,
                null, false);
    }

    public void setDamagePerUnit(float dmgPerUnit) {
        this.dmgPerUnit = dmgPerUnit;
    }

    static class MyBuilder extends AbstractBuffBuilder {

        private float damagePerUnit;

        public MyBuilder(float duration) {
            super(duration);
            setTypeId(BuffTypeIds.DEEP_WOUNDS);
        }

        public MyBuilder damagePerUnit(float damagePerUnit) {
            this.damagePerUnit = damagePerUnit;
            return this;
        }

        @Override
        public AbstractBuff build() {
            BleedBuff bleedBuff = new BleedBuff(duration);
            bleedBuff.setDamagePerUnit(damagePerUnit);
            return set(bleedBuff);
        }
    }
}
