package gov.nih.ncats.molwitch.indigo.spi;

import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoObject;
import gov.nih.ncats.common.util.CachedSupplier;
import gov.nih.ncats.common.util.CachedSupplierGroup;
import gov.nih.ncats.molwitch.Atom;
import gov.nih.ncats.molwitch.AtomCoordinates;
import gov.nih.ncats.molwitch.Bond;
import gov.nih.ncats.molwitch.Chirality;
import gov.nih.ncats.molwitch.isotopes.Elements;
import gov.nih.ncats.molwitch.isotopes.Isotope;
import gov.nih.ncats.molwitch.isotopes.IsotopeFactory;
import gov.nih.ncats.molwitch.isotopes.NISTIsotopeFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IndigoAtom implements Atom {


    private static Pattern RGROUP_PATTERN = Pattern.compile("R(\\d+)");
    private final IndigoObject atom;

    private final IndigoChemicalImpl parent;

    private CachedSupplierGroup dirtyGroup = new CachedSupplierGroup();

    public IndigoAtom(IndigoObject atom, IndigoChemicalImpl parent) {
        this.atom = atom;
        this.parent = parent;


    }
    IndigoChemicalImpl getMolecule(){
        return parent;
    }
    IndigoObject getIndigoObject(){
        return atom;
    }

    @Override
    public int getAtomicNumber() {
        return atom.atomicNumber();
    }

    @Override
    public void setAtomicNumber(int atomicNumber) {
        //TODO can this be done?
    }

    @Override
    public String getSymbol() {
        return atom.symbol();
    }

    @Override
    public List<? extends Bond> getBonds() {
        List<Bond> bonds = new ArrayList<>();
        for(IndigoObject neighbor : atom.iterateNeighbors()){
            bonds.add(parent.getBond(neighbor.bond().index()));
        }
        return bonds;
    }

    @Override
    public boolean hasAromaticBond() {
        //TODO
        return false;
    }

    @Override
    public int getCharge() {
       Integer charge= atom.charge();
       if(charge ==null){
           return 0;
       }
       return charge.intValue();
    }

    @Override
    public int getRadical() {
        Integer radical= atom.radical();
        if(radical==null){
            return 0;
        }
        return radical;
    }

    @Override
    public void setRadical(int radical) {
        atom.setRadical(radical);
    }

    @Override
    public AtomCoordinates getAtomCoordinates() {
        IndigoObject mol = getMolecule().getIndioObject();
        if(mol.hasCoord()){
            float[] xyz = atom.xyz();
            if(mol.hasZCoord()) {
                return AtomCoordinates.valueOf(xyz[0], xyz[1], xyz[2]);
            }
            return AtomCoordinates.valueOf(xyz[0], xyz[1]);
        }
        return null;

    }

    @Override
    public void setAtomCoordinates(AtomCoordinates atomCoordinates) {
        atom.setXYZ((float) atomCoordinates.getX(), (float) atomCoordinates.getY(), (float) atomCoordinates.getZ().orElse(0D));
    }

    @Override
    public Chirality getChirality() {
        if(atom.isChiral()){
            return IndigoUtil.getIndigoChiralityForAtom(atom);
        }
        return Chirality.Non_Chiral;
    }

    @Override
    public void setChirality(Chirality chirality) {
       //TODO

    }

    @Override
    public double getExactMass() {
        return atom.monoisotopicMass();
    }

    @Override
    public int getMassNumber() {
        int isotope = atom.isotope();
        if(isotope ==0){
            return 0;
        }
        Optional<Isotope> found=NISTIsotopeFactory.INSTANCE.getIsotopesFor(atom.atomicNumber()).stream()
                                    .filter(i-> i.getIsotopicComposition().meetsCriteria(isotope))
                                    .findFirst();
        if(found.isPresent()){
            return found.get().getMassNumber();
        }
        return 0;
    }

    @Override
    public void setCharge(int charge) {
        atom.setCharge(charge);
    }

    @Override
    public void setMassNumber(int mass) {
        atom.setIsotope(mass);
    }

    @Override
    public int getImplicitHCount() {
        return atom.countImplicitHydrogens();
    }

    @Override
    public boolean isInRing() {
        int index = atom.index();
        for(IndigoObject ring : this.parent.iterateRings()){
            for(IndigoObject a : ring.iterateAtoms()){
                if(index ==a.index()){
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public OptionalInt getValence() {
        //TODO what's the difference between explicitValence and valence?
        return IndigoUtil.valueOrEmpty(atom.explicitValence());
    }

    @Override
    public boolean hasValenceError() {
        //if there's a problem returns string with message
        //and empty string if correct
//        atom.checkValence();
        String valenceCheck = atom.checkBadValence();
//
//        System.out.println("valenceCheckBad " + valenceCheck);
//        System.out.println("calling actual checkValence " + atom.checkValence());
//        System.out.println("valenceCheckBad again " +  atom.checkBadValence());
       return !valenceCheck.isEmpty();
    }

    @Override
    public boolean isIsotope() {
        return false;
    }

    @Override
    public boolean isQueryAtom() {
        return  atom.isPseudoatom();
    }

    @Override
    public boolean isRGroupAtom() {
        return atom.isRSite();
    }

    @Override
    public OptionalInt getRGroupIndex() {
        if(atom.isRSite()){
            String symbol = atom.symbol();
            Matcher matcher = RGROUP_PATTERN.matcher(symbol);
            if(matcher.find()){
                return OptionalInt.of(Integer.parseInt(matcher.group(1)));
            }
        }
        return OptionalInt.empty();
    }

    @Override
    public void setRGroup(Integer rGroup) {
        if(rGroup==null){
            //clear?
            atom.setRSite(Elements.getSymbolByAtomicNumber(atom.atomicNumber()));
        }else {
            atom.setRSite("R" + rGroup);
        }
    }

    @Override
    public OptionalInt getAtomToAtomMap() {
        //TODO mapping number takes a reaction object?
        int value = atom.atomMappingNumber(atom);
        if(value>0){
            return OptionalInt.of(value);
        }
        return OptionalInt.empty();
    }

    @Override
    public void setAtomToAtomMap(int value) {
        atom.setAtomMappingNumber(atom, value);
    }

    @Override
    public int getSmallestRingSize() {
        return atom.countSSSR();
    }

    @Override
    public int getAtomIndexInParent() {
        return atom.index();
    }

    @Override
    public Optional<String> getAlias() {
        //symbol is either a periodic table symbol, an Rsite or an alias
        String symbol = atom.symbol();
        if(!atom.isRSite() && !Elements.isElementSymbol(symbol)){
            return Optional.of(symbol);
        }
        return Optional.empty();
    }

    @Override
    public void setAlias(String alias) {
        atom.resetAtom(alias);
    }

    @Override
    public void setImplicitHCount(Integer implicitH) {

        atom.setImplicitHCount(IndigoUtil.valueOrZero(implicitH));
    }

    @Override
    public boolean isValidAtomicSymbol() {
        //TODO
        return false;
    }

    @Override
    public boolean isPseudoAtom() {
        return atom.isPseudoatom();
    }

    public static IndigoObject getIndigoObject(Atom a){
        return ((IndigoAtom)a).getIndigoObject();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IndigoAtom)) return false;
        IndigoAtom that = (IndigoAtom) o;
        return atom.index()==that.atom.index() && parent.getIndioObject().self == that.parent.getIndioObject().self;
    }

    @Override
    public int hashCode() {
        return Objects.hash(atom.index(),parent.getIndioObject().self);
    }
}
