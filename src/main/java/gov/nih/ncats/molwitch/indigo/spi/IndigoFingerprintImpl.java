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
