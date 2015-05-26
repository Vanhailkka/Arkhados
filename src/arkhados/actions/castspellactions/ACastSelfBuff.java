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
package arkhados.actions.castspellactions;

import arkhados.actions.EntityAction;
import arkhados.controls.CInfluenceInterface;
import arkhados.spell.buffs.AbstractBuff;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author william
 */
public class ACastSelfBuff extends EntityAction {

    private List<AbstractBuff> buffs = new ArrayList<>();

    @Override
    public boolean update(float tpf) {
        CInfluenceInterface target =
                spatial.getControl(CInfluenceInterface.class);
        for (AbstractBuff buff : buffs) {
            buff.attachToCharacter(target);
        }
        return false;
    }

    public void addBuff(AbstractBuff buff) {
        buffs.add(buff);
    }
}