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

import com.epam.indigo.IndigoObject;
import gov.nih.ncats.common.iter.IteratorUtil;
import gov.nih.ncats.common.stream.StreamUtil;
import gov.nih.ncats.molwitch.Atom;
import gov.nih.ncats.molwitch.Bond;
import gov.nih.ncats.molwitch.SGroup;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Spliterators;
import java.util.stream.Stream;

public class IndigoSGroup implements SGroup {
    private final SGroupType type;
    private final IndigoObject sgroup;
    private final IndigoChemicalImpl mol;

    public IndigoSGroup(SGroupType type, IndigoObject sgroup, IndigoChemicalImpl mol) {
        this.type = type;
        this.sgroup = sgroup;
        this.mol = mol;
    }

    @Override
    public SGroupType getType() {
        return type;
    }

    @Override
    public Stream<Atom> getAtoms() {
        Iterator<IndigoObject> iter = sgroup.iterateAtoms().iterator();
        return StreamUtil.forIterator(IteratorUtil.map(iter,  mol::getAtom));
    }

    @Override
    public Stream<Bond> getBonds() {
        Iterator<IndigoObject> iter = sgroup.iterateBonds().iterator();
        return StreamUtil.forIterator(IteratorUtil.map(iter,  mol::getBond));
    }

    @Override
    public Stream<Atom> getOutsideNeighbors() {
        return null;
    }

    @Override
    public Stream<SGroup> getParentHierarchy() {
        return null;
    }

    @Override
    public SGroupConnectivity getConnectivity() {
        return null;
    }

    @Override
    public void addAtom(Atom a) {

    }

    @Override
    public void addBond(Bond b) {

    }

    @Override
    public void removeAtom(Atom a) {

    }

    @Override
    public void removeBond(Bond b) {

    }

    @Override
    public PolymerSubType getPolymerSubType() {
        return null;
    }

    @Override
    public void setPolymerSubType(PolymerSubType polymerSubtype) {

    }

    @Override
    public boolean hasBrackets() {
        //TODO there is a method to set bracket coords but not get?
        return false;
    }

    @Override
    public List<SGroupBracket> getBrackets() {
        //TODO there is a method to set bracket coords but not get?
        return null;
    }

    @Override
    public Optional<String> getSruLabel() {
        return Optional.empty();
    }

    @Override
    public boolean bracketsSupported() {
        //TODO there is a method to set bracket coords but not get?
        return false;
    }

    @Override
    public Optional<String> getSubscript() {
        return Optional.empty();
    }

    @Override
    public Optional<String> getSuperscript() {
        return Optional.empty();
    }

    @Override
    public Optional<String> getSuperatomLabel() {
        return Optional.empty();
    }

    @Override
    public boolean bracketsTrusted() {
        return false;
    }
}
