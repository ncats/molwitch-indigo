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
