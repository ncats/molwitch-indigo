/*
 * NCATS-MOLWITCH-INDIGO
 *
 * Copyright 2020 NIH/NCATS
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package gov.nih.ncats.molwitch.indigo.spi;

import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoObject;
import gov.nih.ncats.molwitch.Atom;
import gov.nih.ncats.molwitch.Bond;

import java.util.Objects;

public class IndigoBond implements Bond {
    private final IndigoObject bond;
    private final IndigoChemicalImpl mol;
    public IndigoBond(IndigoObject bond, IndigoChemicalImpl mol) {
        this.bond = bond;
        this.mol = mol;
    }

    @Override
    public Atom getOtherAtom(Atom a) {
        IndigoObject obj= ((IndigoAtom)a).getIndigoObject();
        if(bond.source().index() == obj.index()){
            return getAtom2();
        }
        return getAtom1();
    }

    @Override
    public Atom getAtom1() {
        IndigoObject atomObj= bond.source();
        return mol.getAtom(atomObj.index());
    }

    @Override
    public Atom getAtom2() {
        IndigoObject atomObj= bond.destination();
        return mol.getAtom(atomObj.index());
    }

    @Override
    public BondType getBondType() {
        int type = bond.bondOrder();
        if(type ==0){
            //query bond
            return null;
        }
        return BondType.ofOrder(type);
    }

    @Override
    public Stereo getStereo() {
        int bondStereo = bond.bondStereo();
        switch(bondStereo){
            case Indigo.UP: return Stereo.UP;
            case Indigo.DOWN: return Stereo.DOWN;
            case Indigo.EITHER: return Stereo.UP_OR_DOWN;
            default : return Stereo.NONE;
        }
    }

    @Override
    public DoubleBondStereo getDoubleBondStereo() {
        int bondStereo = bond.bondStereo();
        switch(bondStereo){
            case Indigo.CIS : return DoubleBondStereo.Z_CIS;
            case Indigo.TRANS : return DoubleBondStereo.E_TRANS;
            case Indigo.EITHER: return DoubleBondStereo.E_OR_Z;
            default:
                return DoubleBondStereo.NONE;
        }
    }

    @Override
    public Bond switchParity() {
       //TODO this doesn't seem to be used?
        return null;

    }

    @Override
    public void setStereo(Stereo stereo) {
        //TODO
    }

    @Override
    public void setBondType(BondType type) {
        //TODO
    }

    @Override
    public boolean isQueryBond() {
        return bond.bondOrder()==0;
    }

    @Override
    public boolean isInRing() {

        return bond.topology() == Indigo.RING;
    }

    @Override
    public boolean isAromatic() {
        return bond.bondOrder()==4;
    }

    IndigoObject getIndigoObject(){
        return bond;
    }
    public static IndigoObject getIndigoObject(Bond a){
        return ((IndigoBond)a).getIndigoObject();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IndigoBond)) return false;
        IndigoBond that = (IndigoBond) o;
        return bond.index()==that.bond.index() && mol.getIndioObject().self == that.mol.getIndioObject().self;
    }

    @Override
    public int hashCode() {
        return Objects.hash(bond.index(),mol.getIndioObject().self);
    }
}
