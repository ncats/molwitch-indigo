package gov.nih.ncats.molwitch.indigo.spi;

import com.epam.indigo.IndigoInchi;
import com.epam.indigo.IndigoObject;
import gov.nih.ncats.molwitch.Chemical;
import gov.nih.ncats.molwitch.ChemicalBuilder;
import gov.nih.ncats.molwitch.ChemicalSource;
import gov.nih.ncats.molwitch.inchi.InChiResult;
import gov.nih.ncats.molwitch.internal.source.StringSource;
import gov.nih.ncats.molwitch.spi.InchiImplFactory;

import java.io.IOException;

public class IndigoInchiImplFactory implements InchiImplFactory {
    @Override
    public InChiResult asStdInchi(Chemical chemical, boolean trustCoordinates) throws IOException {
        IndigoInchi indigoInchi= IndigoUtil.getIndigoInchiInstance();
        String fullInchi = indigoInchi.getInchi((IndigoObject) chemical.getImpl().getWrappedObject());
        String warnings = indigoInchi.getWarning();
        String auxInfo = indigoInchi.getAuxInfo();
        String log = indigoInchi.getLog();
        String key = indigoInchi.getInchiKey(fullInchi);
        //TODO parse warning into Status ?
        return new InChiResult.Builder(InChiResult.Status.VALID)
                            .setInchi(fullInchi)
                            .setAuxInfo(auxInfo)
                            .setKey(key)
                            .setMessage(log)

                            .build();
    }

    @Override
    public Chemical parseInchi(String inchi) throws IOException {
        return ChemicalBuilder._fromImpl( new IndigoChemicalImpl(IndigoUtil.getIndigoInchiInstance().loadMolecule(inchi),
                new StringSource(inchi, ChemicalSource.Type.INCHI)))
                .build();
    }
}
