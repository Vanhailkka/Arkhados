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
package arkhados.controls;

import arkhados.spell.Spell;
import arkhados.util.AnimationData;
import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.Animation;
import com.jme3.animation.LoopMode;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import java.io.IOException;
import java.util.HashMap;

/**
 * Original animation control for Mage. Most likely each character needs its own
 * custom character animation control and this will be changed to abstract class
 *
 * @author william
 */
public class CharacterAnimationControl extends AbstractControl {

    private AnimControl animControl;
    private CharacterPhysicsControl characterControl;
    private AnimChannel channel;
    private float castTime = 0f;
    private HashMap<String, AnimationData> spellAnimationMap = new HashMap<String, AnimationData>(6);
    private HashMap<String, AnimationData> actionAnimationData = new HashMap<String, AnimationData>(8);
    // TODO: Allow mapping of animations to specific AnimChannels

    private AnimationData walkAnimation;
    private AnimationData deathAnimation;

    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
        this.animControl = super.spatial.getControl(AnimControl.class);
        this.characterControl = super.spatial.getControl(CharacterPhysicsControl.class);

        this.channel = this.animControl.createChannel();
        this.channel.setAnim(this.walkAnimation.getName());
        this.channel.setSpeed(this.walkAnimation.getSpeed());
    }

    @Override
    protected void controlUpdate(float tpf) {
        this.castTime -= tpf;
        if (this.castTime > 0f) {
            return;
        }
        if (!this.characterControl.getWalkDirection().equals(Vector3f.ZERO)) {
            if (!this.walkAnimation.getName().equals(this.channel.getAnimationName())) {
                this.channel.setAnim(this.walkAnimation.getName(), 0.5f);
            }
            this.channel.setSpeed(this.walkAnimation.getSpeed());
        } else {
            if (this.walkAnimation.getName().equals(this.channel.getAnimationName())) {
                this.channel.setSpeed(0.0f);
            }
        }
    }

    public void death() {
        this.channel.setAnim(this.deathAnimation.getName());
        this.channel.setSpeed(this.deathAnimation.getSpeed());
        this.channel.setLoopMode(LoopMode.DontLoop);
        super.setEnabled(false);
    }

    public void castSpell(final Spell spell) {
        final AnimationData animationData = this.spellAnimationMap.get(spell.getName());
        if (animationData == null) {
            return;
        }

        this.castTime = spell.getCastTime();

        this.channel.setAnim(animationData.getName());
        this.channel.setSpeed(animationData.getSpeed());
        this.channel.setLoopMode(animationData.getLoopMode());
    }

    public void animateAction(final String actionName) {
        final AnimationData data = this.actionAnimationData.get(actionName);
        this.channel.setAnim(data.getName());
        this.channel.setSpeed(data.getSpeed());
        this.channel.setLoopMode(data.getLoopMode());
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    public Control cloneForSpatial(Spatial spatial) {
        CharacterAnimationControl control = new CharacterAnimationControl();
        return control;
    }

    @Override
    public void read(JmeImporter im) throws IOException {
        super.read(im);
        InputCapsule in = im.getCapsule(this);
    }

    @Override
    public void write(JmeExporter ex) throws IOException {
        super.write(ex);
        OutputCapsule out = ex.getCapsule(this);
    }

    public void addSpellAnimation(String spellName, final AnimationData animData) {
        this.spellAnimationMap.put(spellName, animData);
    }

    public void addActionAnimation(final String actionName, final AnimationData data) {
        this.actionAnimationData.put(actionName, data);
    }

    public AnimationData getWalkAnimation() {
        return this.walkAnimation;
    }

    public void setWalkAnimation(AnimationData walkAnimation) {
        this.walkAnimation = walkAnimation;
    }

    public AnimationData getDeathAnimation() {
        return this.deathAnimation;
    }

    public void setDeathAnimation(AnimationData deathAnimation) {
        this.deathAnimation = deathAnimation;
    }

    public AnimControl getAnimControl() {
        return this.animControl;
    }
}