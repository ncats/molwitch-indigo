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
