package gov.nih.ncats.molwitch.indigo.spi;

import gov.nih.ncats.molwitch.fingerprint.Fingerprinter;
import gov.nih.ncats.molwitch.fingerprint.Fingerprinters;
import gov.nih.ncats.molwitch.spi.FingerprinterImpl;

import java.util.Collections;
import java.util.Set;

public class IndigoFingerprintImpl implements FingerprinterImpl {
    @Override
    public boolean supports(Fingerprinters.FingerprintSpecification options) {
        return options instanceof Fingerprinters.PathBasedSpecification;
    }

    @Override
    public boolean isDefault() {
        return true;
    }

    @Override
    public Set<String> getSupportedAlgorithmNames() {
        return Collections.singleton(Fingerprinters.FingerprintSpecification.PATH_BASED.name());
    }

    @Override
    public Fingerprinter createFingerPrinterFor(Fingerprinters.FingerprintSpecification fingerPrinterOptions) {
        Fingerprinters.PathBasedSpecification spec = (Fingerprinters.PathBasedSpecification)fingerPrinterOptions;

        int length = spec.getLength();
        IndigoUtil.getIndigoInstance().setOption("fp-ord-qwords", length*4);

        return IndigoFingerprinter.INSTANCE;
    }

    @Override
    public Fingerprinter createDefaultFingerprinter() {
        return IndigoFingerprinter.INSTANCE;
    }
}
