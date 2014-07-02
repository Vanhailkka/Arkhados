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

import arkhados.actions.CastingSpellAction;
import arkhados.actions.EntityAction;
import arkhados.actions.castspellactions.MeleeAttackAction;
import arkhados.characters.Venator;
import arkhados.controls.ActionQueueControl;
import arkhados.spell.CastSpellActionBuilder;
import arkhados.spell.Spell;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

/**
 *
 * @author william
 */
public class Rend extends Spell {

    {
        super.iconName = "rend.png";
        super.multipart = true;
    }
    public Rend(String name, float cooldown, float range, float castTime) {
        super(name, cooldown, range, castTime);
    }

    public static Spell create() {
        final float cooldown = 0.5f;
        final float range = 15f;
        final float castTime = 0.17f;

        final Rend spell = new Rend("Rend", cooldown, range, castTime);
        spell.setCanMoveWhileCasting(true);

        spell.castSpellActionBuilder = new CastSpellActionBuilder() {
            public EntityAction newAction(Node caster, Vector3f vec) {
//                return new MeleeAttackAction(150f, range);
                return new TripleMeleeAttackAction(spell);
            }
        };

        spell.nodeBuilder = null;
        return spell;
    }
}

class TripleMeleeAttackAction extends EntityAction {
    private final Spell spell;

    public TripleMeleeAttackAction(Spell spell) {
        this.spell = spell;
    }

    @Override
    public boolean update(float tpf) {
        // TODO: Make an attack start with different animation than previous one
        final float range = this.spell.getRange();
        final ActionQueueControl queue = super.spatial.getControl(ActionQueueControl.class);
        final MeleeAttackAction action1 = new MeleeAttackAction(50f, range);
        final CastingSpellAction action2Anim = new CastingSpellAction(this.spell, true);
        final MeleeAttackAction action2 = new MeleeAttackAction(60f, range);
        
        // action1 already has the default spell casting animation
        action2Anim.setTypeId(Venator.ANIM_SWIPE_RIGHT);
        queue.enqueueAction(action1);
        queue.enqueueAction(action2Anim);
        queue.enqueueAction(action2);

        return false;
    }
}