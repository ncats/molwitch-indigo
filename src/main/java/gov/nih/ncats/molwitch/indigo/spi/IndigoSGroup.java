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
