package gov.nih.ncats.molwitch.indigo.spi;

import com.epam.indigo.IndigoObject;
import gov.nih.ncats.molwitch.Chemical;
import gov.nih.ncats.molwitch.fingerprint.Fingerprint;
import gov.nih.ncats.molwitch.fingerprint.Fingerprinter;

public enum IndigoFingerprinter implements Fingerprinter {
    INSTANCE;
    @Override
    public Fingerprint computeFingerprint(Chemical chemical) {
        //TODO default is similarity not substructure do we want substructure?
        IndigoObject fp = ((IndigoObject)chemical.getImpl().getWrappedObject()).fingerprint();
        return new Fingerprint(fp.toBuffer());
    }
}
