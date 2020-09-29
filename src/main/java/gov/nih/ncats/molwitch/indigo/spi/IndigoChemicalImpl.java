package gov.nih.ncats.molwitch.indigo.spi;

import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoObject;
import gov.nih.ncats.common.iter.IteratorUtil;
import gov.nih.ncats.common.util.CachedSupplier;
import gov.nih.ncats.molwitch.*;
import gov.nih.ncats.molwitch.isotopes.Elements;
import gov.nih.ncats.molwitch.isotopes.Isotope;
import gov.nih.ncats.molwitch.isotopes.IsotopeFactory;
import gov.nih.ncats.molwitch.spi.ChemicalImpl;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IndigoChemicalImpl implements ChemicalImpl<IndigoChemicalImpl> {

    private final ChemicalSource source;

    private final IndigoObject mol;
    private final Map<IndigoObject, IndigoAtom> atoms = new ConcurrentHashMap<>();
    private final Map<IndigoObject, IndigoBond> bonds = new ConcurrentHashMap<>();

    private final BondTable bondTable = new IndigoBondTable();
//    private CachedSupplier<IndigoObject> ringsSupplier;

    public IndigoChemicalImpl(IndigoObject mol){
        this(mol, null);
    }
    public IndigoChemicalImpl(IndigoObject mol, ChemicalSource source) {
        this.mol = mol;
        this.source = source;
//        ringsSupplier = CachedSupplier.of(()->mol.iterateRings(3, 12));
    }

    IndigoObject getIndioObject(){
        return mol;
    }

    IndigoObject iterateRings(){
       return mol.iterateRings(3, 12);
    }

    @Override
    public String getName() {
        String name= mol.name();
        if(name.isEmpty()){
            return null;
        }
        return name;
    }

    @Override
    public void setName(String name) {
        mol.setName(name);
    }

    @Override
    public double getMass() {
        return mol.monoisotopicMass();
    }

    @Override
    public int getAtomCount() {
        return mol.countAtoms();
    }

    @Override
    public int getBondCount() {
        return mol.countBonds();
    }

    @Override
    public Atom getAtom(int i) {
        IndigoObject atom = mol.getAtom(i);

        return getAtom(atom);
    }

    Atom getAtom(IndigoObject atom) {
        return atoms.computeIfAbsent(atom, a->new IndigoAtom(a, this));
    }

    @Override
    public void aromatize() {
        mol.aromatize();
    }

    @Override
    public void kekulize() {
        mol.dearomatize();
    }

    @Override
    public GraphInvariant getGraphInvariant() {
        //TODO
        return null;
    }

    @Override
    public Bond getBond(int i) {
        IndigoObject bond = mol.getBond(i);
        return getBond(bond);
    }

    Bond getBond(IndigoObject bond) {
        return bonds.computeIfAbsent(bond, b-> new IndigoBond(b,this));
    }

    @Override
    public BondTable getBondTable() {
        return bondTable;
    }

    @Override
    public int indexOf(Atom a) {
        return ((IndigoAtom)a).getIndigoObject().index();
    }

    @Override
    public int indexOf(Bond b) {
        return ((IndigoBond)b).getIndigoObject().index();
    }

    @Override
    public IndigoChemicalImpl shallowCopy() {
        //TODO like this?
        return new IndigoChemicalImpl(mol);
    }

    @Override
    public void makeHydrogensExplicit() {
        mol.unfoldHydrogens();
        setDirty();
    }

    private void setDirty(){
        atoms.clear();
        bonds.clear();
    }
    @Override
    public Atom addAtom(String symbol) {
        IndigoObject atom = mol.addAtom(symbol);
        IndigoAtom indigoAtom = new IndigoAtom(atom, this);
        atoms.put(atom, indigoAtom);
        return indigoAtom;
    }

    @Override
    public Atom addAtomByAtomicNum(int atomicNumber) {
        return addAtom(Elements.getSymbolByAtomicNumber(atomicNumber));
    }

    @Override
    public Bond addBond(Atom atom1, Atom atom2, Bond.BondType type) {

        IndigoObject b = IndigoAtom.getIndigoObject(atom1).addBond(IndigoAtom.getIndigoObject(atom2), type.getOrder());

        IndigoBond newBond = new IndigoBond(b, this);
        bonds.put(b, newBond);
        return newBond;
    }

    @Override
    public List<ExtendedTetrahedralChirality> getExtendedTetrahedrals() {
        List<ExtendedTetrahedralChirality> list = new ArrayList<>();
        for (IndigoObject atom : mol.iterateAlleneCenters()){

            list.add(new IndigoAllene(atom));
        }
        return list;
    }

    @Override
    public List<TetrahedralChirality> getTetrahedrals() {
        List<TetrahedralChirality> list = new ArrayList<>();
        for (IndigoObject atom : mol.iterateStereocenters()){
            list.add(new IndigoTetrahedralChirality(atom));
        }
        return list;
    }

    @Override
    public List<DoubleBondStereochemistry> getDoubleBondStereochemistry() {
        List<DoubleBondStereochemistry> list = new ArrayList<>();
        for (IndigoObject bond : mol.iterateBonds()){
            int bondStereo = bond.bondStereo();
            //CIS = 7 TRANS = 8
            if(bondStereo ==7 || bondStereo == 8){
                list.add(new IndigoDoubleBondStereo(bond));
            }

        }
        return list;
    }

    @Override
    public void prepareForBuild(PreparationOptions options) {
        if(options.aromatize){
            mol.aromatize();
        }
        if(options.computeStereo){
            mol.markStereobonds();

        }
        if(options.makeHydrogensExplicit){
            mol.unfoldHydrogens();
        }
        if(options.computeCoords){
            mol.layout();
        }
    }

    @Override
    public String getProperty(String key) {
        //indigo throws exception is property missing
        if(mol.hasProperty(key)){
            return mol.getProperty(key);
        }
        return null;
    }

    @Override
    public void setProperty(String key, String value) {
        mol.setProperty(key, value);
    }

    @Override
    public Iterator<Map.Entry<String, String>> properties() {
        Iterator<IndigoObject> iter = mol.iterateProperties().iterator();
        return IteratorUtil.map(iter, obj-> new AbstractMap.SimpleEntry(obj.name(), obj.rawData()));
    }

    @Override
    public ChemicalSource getSource() {
        return source;
    }

    @Override
    public void removeProperty(String name) {
        mol.removeProperty(name);
    }

    @Override
    public int getSGroupCount() {
        return mol.countDataSGroups()+ mol.countGenericSGroups()
                + mol.countMultipleGroups() + mol.countRepeatingUnits() + mol.countSuperatoms();
    }

    @Override
    public Object getWrappedObject() {
        return mol;
    }

    @Override
    public String getFormula() {
        return mol.grossFormula();
    }

    @Override
    public void makeHydrogensImplicit() {
        mol.foldHydrogens();
    }

    @Override
    public String getFormula(boolean includeImplicitHydrogen) {
        //TODO
        return null;
    }

    @Override
    public boolean hasImplicitHydrogens() {
        return mol.countImplicitHydrogens() >0;
    }

    @Override
    public IndigoChemicalImpl deepCopy() {

        return new IndigoChemicalImpl(mol.clone(), source);
    }

    @Override
    public Iterator<IndigoChemicalImpl> connectedComponents() {
        return IteratorUtil.map(mol.iterateComponents().iterator(),
                                    c-> new IndigoChemicalImpl(c.clone()));

    }

    @Override
    public boolean hasCoordinates() {
        return mol.hasCoord();
    }

    @Override
    public boolean has2DCoordinates() {

        return mol.hasCoord() && !mol.hasZCoord();
    }

    @Override
    public boolean has3DCoordinates() {
        return mol.hasZCoord();
    }

    @Override
    public int getSmallestRingSize() {
        return mol.countSSSR();
    }

    @Override
    public Atom removeAtom(int i) {
        IndigoObject a =mol.getAtom(i);

        Atom atomObj = atoms.remove(a);
        a.remove();
        if(atomObj ==null){
            return new IndigoAtom(a, this);
        }
        return atomObj;
    }


    @Override
    public Atom removeAtom(Atom a) {
        if(this ==((IndigoAtom)a).getMolecule()){
            return removeAtom(a.getAtomIndexInParent());
        }
        return a;
    }

    @Override
    public Bond removeBond(int i) {
        IndigoObject bond = mol.getBond(i);
        bond.remove();
        return new IndigoBond(bond, this);
    }

    private Bond removeBondObj(IndigoObject bond) {
        Bond b = bonds.remove(bond);

        bond.remove();
        return b;
    }

    @Override
    public Bond removeBond(Bond b) {
        IndigoObject bondObj = IndigoBond.getIndigoObject(b);
        if(bonds.remove(bondObj, b)){
            bondObj.remove();

        }
        return b;
    }

    @Override
    public Bond removeBond(Atom a, Atom b) {
        IndigoObject a1 = IndigoAtom.getIndigoObject(a);
        IndigoObject a2 = IndigoAtom.getIndigoObject(b);
        int indexOfA2 = a2.index();
        for(IndigoObject neighborAtom : a1.iterateNeighbors()){
            if(neighborAtom.index() == indexOfA2){
                return removeBondObj(neighborAtom.bond());
            }
        }
        return null;
    }

    @Override
    public Atom addAtom(Atom a) {
        IndigoObject newAtom = mol.addAtom(a.getSymbol());
        IndigoAtom ret = new IndigoAtom(newAtom, this);
        atoms.put(newAtom, ret);
        return ret;
    }

    @Override
    public Bond addBond(Bond b) {
        System.out.println("bond self = " + ((IndigoBond)b).getIndigoObject().self);
        int order = b.getBondType().getOrder();
        IndigoObject newBond = ((IndigoAtom)b.getAtom1()).getIndigoObject().addBond(((IndigoAtom)b.getAtom2()).getIndigoObject(), order);
        IndigoBond ret = new IndigoBond(newBond, this);
        bonds.put(newBond,ret );
        return ret;
    }

    @Override
    public Atom addAtom(Isotope isotope) {
        IndigoObject obj = mol.addAtom(isotope.getSymbol());
        obj.setIsotope(isotope.getMassNumber());
        return getAtom(obj.index());
    }

    @Override
    public void addChemical(ChemicalImpl<IndigoChemicalImpl> other) {

    }

    @Override
    public void removeSGroup(SGroup sgroup) {

    }

    @Override
    public SGroup addSgroup(SGroup.SGroupType type) {
        return null;
    }

    @Override
    public List<SGroup> getSGroups() {
        List<SGroup> list = new ArrayList<>();
        for(IndigoObject obj : mol.iterateSGroups()){
            list.add(new IndigoSgroup(obj, obj.getSGroupType()));
        }
        return list;
    }

    @Override
    public boolean hasSGroups() {
        return getSGroupCount() >0;
    }

    @Override
    public void expandSGroups() {

    }

    @Override
    public void generateCoordinates() {
        mol.layout();
    }

    @Override
    public void flipChirality(Stereocenter s) {
       //TODO
    }

    private class IndigoSgroup implements SGroup{

        private final SGroupType type;
        private IndigoObject sgroup;
        IndigoSgroup(IndigoObject sgroup, int type){
            this.type = convertType(type);
            this.sgroup = sgroup;
        }
        private SGroupType convertType(int type){
            switch(type){
                case Indigo.SG_TYPE_GEN : return SGroupType.GENERIC;
                case Indigo.SG_TYPE_DAT : return SGroupType.DATA;
                case Indigo.SG_TYPE_SUP : return SGroupType.SUPERATOM_OR_ABBREVIATION;
                case Indigo.SG_TYPE_SRU : return SGroupType.SRU;
                case Indigo.SG_TYPE_MUL : return SGroupType.MULTIPLE;
                case Indigo.SG_TYPE_MON : return SGroupType.MONOMER;

                case Indigo.SG_TYPE_MER : return SGroupType.MER;
                case Indigo.SG_TYPE_COP : return SGroupType.COPOLOYMER;
                case Indigo.SG_TYPE_CRO : return SGroupType.CROSSLINK;
                case Indigo.SG_TYPE_MOD : return SGroupType.MODIFICATION;
                case Indigo.SG_TYPE_GRA : return SGroupType.GRAFT;
                case Indigo.SG_TYPE_COM: return SGroupType.COMPONENT;
                case Indigo.SG_TYPE_MIX : return SGroupType.MIXTURE;
                case Indigo.SG_TYPE_FOR : return SGroupType.FORMULATION;
                case Indigo.SG_TYPE_ANY : return SGroupType.ANYPOLYMER;
                default : return null;
            }
        }
        @Override
        public SGroupType getType() {
            return type;
        }

        @Override
        public Stream<Atom> getAtoms() {
            //does this work?
            List<Atom> list = new ArrayList<>();
            for(IndigoObject a : sgroup.iterateAtoms()){
                list.add(getAtom(a.index()));
            }
            return list.stream();
        }

        @Override
        public Stream<Bond> getBonds() {
            //does this work?
            List<Bond> list = new ArrayList<>();
            for(IndigoObject a : sgroup.iterateBonds()){
                list.add(getBond(a.index()));
            }
            return list.stream();
        }

        @Override
        public Stream<Atom> getOutsideNeighbors() {
            //TODO
            return Stream.empty();
        }

        @Override
        public Stream<SGroup> getParentHierarchy() {

            //TODO
            return Stream.empty();
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
            return false;
        }

        @Override
        public List<SGroupBracket> getBrackets() {
//            float[] coords = sgroup.setSGroupBrackets()
            //TODO
            return null;
        }

        @Override
        public Optional<String> getSruLabel() {
            return Optional.ofNullable(sgroup.getSGroupName());
        }

        @Override
        public boolean bracketsSupported() {
            //TODO can set brackets but not get them?
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
            return Optional.ofNullable(sgroup.getSGroupName());
        }

        @Override
        public boolean bracketsTrusted() {
            return false;
        }
    }
    private class IndigoDoubleBondStereochemistry implements DoubleBondStereochemistry{
        private final IndigoObject center;

        public IndigoDoubleBondStereochemistry(IndigoObject center){
            this.center = center;
        }
        @Override
        public DoubleBondStereo getStereo() {
            return null;
        }

        @Override
        public Bond getDoubleBond() {
            return null;
        }

        @Override
        public Atom getLigand(int i) {
            return null;
        }

        @Override
        public Bond getLigandBond(int i) {
            return null;
        }
    }

    private class IndigoAllene implements ExtendedTetrahedralChirality{
        private final IndigoObject center;
        private final int terminal1, terminal2;
        private List<Integer> peripherals = new ArrayList<>();
        boolean isDefined;
        private final Chirality chirality;
        private IndigoAllene(IndigoObject center) {
            this.center = center;
            chirality = IndigoUtil.getIndigoChiralityForAtom(center);
            int centerIndex = center.index();
            Iterator<IndigoObject> centerNeighbors = center.iterateNeighbors().iterator();
            terminal1 = centerNeighbors.next().index();
            terminal2 = centerNeighbors.next().index();

            int[] atomOrder = new int[2];
            if(chirality.isOdd()){
                //clockwise
                atomOrder[0] = terminal2;
                atomOrder[1] = terminal1;
            }else{
                atomOrder[0] = terminal1;
                atomOrder[1] = terminal2;
            }
            for(IndigoObject nei : mol.getAtom(atomOrder[0]).iterateNeighbors()){
                int index = nei.index();
                if(index != centerIndex){
                    peripherals.add(index);
                    if(!isDefined && nei.bond().bondStereo() !=0){
                        isDefined =true;
                    }
                }

            }
            if(peripherals.size() ==1){

                //has implicit H add terminal 1
                if(chirality.isOdd()){
                    peripherals.add(0, atomOrder[0]);
                }else {
                    peripherals.add(atomOrder[0]);
                }
            }
            for(IndigoObject nei : mol.getAtom(atomOrder[1]).iterateNeighbors()){
                int index = nei.index();
                if(index != centerIndex){
                    peripherals.add(index);
                    if(!isDefined && nei.bond().bondStereo() !=0){
                        isDefined =true;
                    }
                }

            }
            if(peripherals.size() ==3){
                if(chirality.isOdd()){
                    peripherals.add(2, atomOrder[1]);
                }else {
                    //has implicit H add terminal 2
                    peripherals.add( atomOrder[1]);
                }
            }
        }

        @Override
        public List<Atom> getTerminalAtoms() {
            return Arrays.asList(getAtom(terminal1), getAtom(terminal2));
        }

        @Override
        public List<Atom> getPeripheralAtoms() {
            return peripherals.stream().map(IndigoChemicalImpl.this::getAtom).collect(Collectors.toList());
        }

        @Override
        public boolean isDefined() {
            return isDefined;
        }

        @Override
        public Atom getCenterAtom() {
            return getAtom(center);
        }

        @Override
        public Chirality getChirality() {
            return chirality;
        }
    }


    private class IndigoTetrahedralChirality implements TetrahedralChirality{
        private IndigoObject center;

        private int[] ligands = new int[4];
        public IndigoTetrahedralChirality(IndigoObject center) {
            this.center = center;
            int i=0;
            for(IndigoObject neighbor :center.iterateNeighbors()){
                ligands[i++] = neighbor.index();
            }
        }

        @Override
        public Atom getLigand(int i) {
             return getAtom(ligands[i]);
        }

        @Override
        public Atom getCenterAtom() {
            return getAtom(center);
        }

        @Override
        public Chirality getChirality() {
            return Chirality.valueByParity(center.checkChirality());
        }

        @Override
        public List<Atom> getPeripheralAtoms() {
            return Arrays.asList(getLigand(0),
                    getLigand(1),
                    getLigand(2),
                    getLigand(3));
        }

        @Override
        public boolean isDefined() {
            return true;
        }
    }

    public class IndigoBondTable implements BondTable{

        @Override
        public boolean bondExists(int i, int j) {
            for(IndigoObject nei : mol.getAtom(i).iterateNeighbors()){
                if(j ==nei.index()){
                    return true;
                }
            }
            return false;
        }

        @Override
        public Bond getBond(int i, int j) {
            for(IndigoObject nei : mol.getAtom(i).iterateNeighbors()){
                if(j ==nei.index()){
                    return IndigoChemicalImpl.this.getBond(nei.bond().index());
                }
            }
            return null;
        }

        @Override
        public int getAtomCount() {
            return mol.countAtoms();
        }
    }

    private class IndigoDoubleBondStereo implements DoubleBondStereochemistry{

        private final IndigoObject stereoBond;
        private DoubleBondStereo stereo;
        public IndigoDoubleBondStereo(IndigoObject stereoBond) {
            this.stereoBond = stereoBond;
            int doubleBondIndex = stereoBond.index();
            int value = stereoBond.bondStereo();
            if(value ==7){
                stereo = DoubleBondStereo.Z_CIS;
            }else if(value ==8){
                stereo = DoubleBondStereo.E_TRANS;

            }else{
                stereo = DoubleBondStereo.E_OR_Z;
            }
            //TODO handle either?

            //TODO not sure how to get the ligand atoms without knowing how to get CIP values
//            int atom1Index = stereoBond.source().index();
//            int atom2Index = stereoBond.destination().index();
//
//            /*
//                A
//                 \
//                 C =
//                 /
//                 X
//             */
//            for(IndigoObject b : mol.getAtom(atom1Index).iterateBonds()){
//                int index = b.index();
//                if(index !=doubleBondIndex){
//                    IndigoObject sourceAtom = b.source();
//                    sourceAtom.
//                    int atomIndex = sourceAtom.index();
//
//                    if(atomIndex == atom1Index){
//                        b.destination()
//                    }
//                }
//            }
        }

        @Override
        public DoubleBondStereo getStereo() {
            return stereo;
        }

        @Override
        public Bond getDoubleBond() {
            return getBond(stereoBond.index());
        }

        @Override
        public Atom getLigand(int i) {

            return null;
        }

        @Override
        public Bond getLigandBond(int i) {
            return null;
        }
    }
}
