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
package arkhados.characters;

import arkhados.Globals;
import arkhados.components.CRest;
import arkhados.controls.ActionQueueControl;
import arkhados.controls.CharacterAnimationControl;
import arkhados.controls.CharacterBuffControl;
import arkhados.controls.CharacterHudControl;
import arkhados.controls.CharacterPhysicsControl;
import arkhados.controls.CharacterSoundControl;
import arkhados.controls.CharacterSyncControl;
import arkhados.controls.ComponentAccessor;
import arkhados.controls.InfluenceInterfaceControl;
import arkhados.controls.SpellCastControl;
import arkhados.controls.SyncInterpolationControl;
import arkhados.effects.EffectBox;
import arkhados.effects.SimpleSoundEffect;
import arkhados.spell.Spell;
import arkhados.systems.SRest;
import arkhados.ui.hud.ClientHudManager;
import arkhados.util.AnimationData;
import arkhados.util.InputMappingStrings;
import arkhados.util.AbstractNodeBuilder;
import arkhados.util.UserDataStrings;
import com.jme3.animation.AnimControl;
import com.jme3.animation.LoopMode;
import com.jme3.app.state.AppStateManager;
import com.jme3.scene.Node;

/**
 * Creates entity with EmberMage's features.
 *
 * @author william
 */
public class EmberMage extends AbstractNodeBuilder {

    public static final int ACTION_FIREWALK = 0;
    private ClientHudManager clientHudManager;

    public EmberMage(ClientHudManager clientHudManager) {
        this.clientHudManager = clientHudManager;
        setEffectBox(new EffectBox());
        getEffectBox().addActionEffect(ACTION_FIREWALK,
                new SimpleSoundEffect("Effects/Sound/Firewalk.wav"));
    }

    @Override
    public Node build(Object location) {
        Node entity = (Node) assetManager.loadModel("Models/Mage.j3o");
        float movementSpeed = 35.8f;
        entity.setUserData(UserDataStrings.SPEED_MOVEMENT, movementSpeed);
        entity.setUserData(UserDataStrings.SPEED_MOVEMENT_BASE, movementSpeed);
        entity.setUserData(UserDataStrings.SPEED_ROTATION, 0.0f);
        float radius = 5.0f;
        entity.setUserData(UserDataStrings.RADIUS, radius);
        float health = 1700f;
        entity.setUserData(UserDataStrings.HEALTH_MAX, health);
        entity.setUserData(UserDataStrings.HEALTH_CURRENT, health);
        entity.setUserData(UserDataStrings.DAMAGE_FACTOR, 1f);
        entity.setUserData(UserDataStrings.LIFE_STEAL, 0f);

        AppStateManager stateManager = Globals.app.getStateManager();

        ComponentAccessor componentAccessor = new ComponentAccessor();
        entity.addControl(componentAccessor);

        entity.addControl(new CharacterPhysicsControl(radius, 20.0f, 75.0f));

        /**
         * By setting physics damping to low value, we can effectively apply
         * impulses on it.
         */
        entity.getControl(CharacterPhysicsControl.class).setPhysicsDamping(0.2f);
        entity.addControl(new ActionQueueControl());

        /**
         * To add spells to entity, create SpellCastControl and call its
         * putSpell-method with name of the spell as argument.
         */
        SpellCastControl spellCastControl = new SpellCastControl();
        entity.addControl(spellCastControl);
        spellCastControl.putSpell(Spell.getSpell("Fireball"),
                InputMappingStrings.getId(InputMappingStrings.M1));
        spellCastControl.putSpell(Spell.getSpell("Magma Bash"),
                InputMappingStrings.getId(InputMappingStrings.M2));
        spellCastControl.putSpell(Spell.getSpell("Ember Circle"),
                InputMappingStrings.getId(InputMappingStrings.Q));
        spellCastControl.putSpell(Spell.getSpell("Meteor"),
                InputMappingStrings.getId(InputMappingStrings.E));
        spellCastControl.putSpell(Spell.getSpell("Purifying Flame"),
                InputMappingStrings.getId(InputMappingStrings.R));
        spellCastControl.putSpell(Spell.getSpell("Firewalk"),
                InputMappingStrings.getId(InputMappingStrings.SPACE));
        spellCastControl.putSpell(Spell.getSpell("Ignite"), null);

        /**
         * Map Spell names to casting animation's name. In this case all spells
         * use same animation.
         */
        AnimControl animControl = entity.getControl(AnimControl.class);

        CharacterAnimationControl characterAnimControl = new CharacterAnimationControl(animControl);
        AnimationData deathAnim = new AnimationData("Die", 1f, LoopMode.DontLoop);
        AnimationData walkAnim = new AnimationData("Walk", 1f, LoopMode.DontLoop);

        characterAnimControl.setDeathAnimation(deathAnim);
        characterAnimControl.setWalkAnimation(walkAnim);
        entity.addControl(characterAnimControl);

        AnimationData animationData = new AnimationData("Idle", 1f, LoopMode.Loop);

        characterAnimControl.addSpellAnimation("Fireball", animationData);
        characterAnimControl.addSpellAnimation("Magma Bash", animationData);
        characterAnimControl.addSpellAnimation("Ember Circle", animationData);
        characterAnimControl.addSpellAnimation("Meteor", animationData);
        characterAnimControl.addSpellAnimation("Purifying Flame", null);
        characterAnimControl.addSpellAnimation("Firewalk", animationData);

        entity.addControl(new InfluenceInterfaceControl());
        entity.addControl(new CharacterSyncControl());

        if (worldManager.isClient()) {
            CharacterSoundControl soundControl = new CharacterSoundControl();
            soundControl.setSufferSound("Effects/Sound/EmberMagePain.wav");
            soundControl.setDeathSound("Effects/Sound/EmberMageDeath.wav");
            entity.addControl(soundControl);
            entity.addControl(new CharacterBuffControl());
            entity.addControl(new CharacterHudControl());

            entity.addControl(new SyncInterpolationControl());
            entity.getControl(InfluenceInterfaceControl.class).setIsServer(false);
        } else {
            CRest cResting = new CRest();
            cResting.spatial = entity;
            componentAccessor.resting = cResting;
            componentAccessor.addComponent(cResting);
        }

        return entity;
    }
}