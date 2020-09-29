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

package gov.nih.ncats.molwitch.indigo.spi.writers;

import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoObject;
import gov.nih.ncats.molwitch.indigo.spi.IndigoUtil;
import gov.nih.ncats.molwitch.io.ChemFormat;
import gov.nih.ncats.molwitch.spi.ChemicalImpl;
import gov.nih.ncats.molwitch.spi.ChemicalWriterImpl;
import gov.nih.ncats.molwitch.spi.ChemicalWriterImplFactory;

import java.io.*;
import java.util.function.BiFunction;
import java.util.function.Function;

public class IndigoSmilesWriterFactory<T extends ChemFormat.ChemFormatWriterSpecification> implements ChemicalWriterImplFactory {


    @Override
    public String writeAsString(ChemicalImpl chemicalImpl, ChemFormat.ChemFormatWriterSpecification spec) throws IOException {
        ChemFormat.SmilesFormatWriterSpecification smilesSpec = (ChemFormat.SmilesFormatWriterSpecification) spec;
        BiFunction<Indigo, IndigoObject, IndigoObject> adapter= CtabWriterUtil.createAdapterFrom(smilesSpec);
        return writeAsString(chemicalImpl, smilesSpec, adapter);
    }

    private String writeAsString(ChemicalImpl chemicalImpl, ChemFormat.SmilesFormatWriterSpecification smilesSpec, BiFunction<Indigo, IndigoObject, IndigoObject> adapter) {
        Indigo indigo = IndigoUtil.getIndigoInstance();
        IndigoObject adapted = adapter.apply(indigo, (IndigoObject) chemicalImpl.getWrappedObject());

        return writeAsString(smilesSpec, adapted);
    }

    protected String writeAsString(ChemFormat.SmilesFormatWriterSpecification smilesSpec, IndigoObject adapted) {
        switch(smilesSpec.getCanonization()){
            case CANONICAL: return adapted.canonicalSmiles();
            default: return adapted.smiles();
        }
    }

    @Override
    public ChemicalWriterImpl newInstance(OutputStream out, ChemFormat.ChemFormatWriterSpecification spec) throws IOException {
        ChemFormat.SmilesFormatWriterSpecification smilesSpec = (ChemFormat.SmilesFormatWriterSpecification) spec;
        BiFunction<Indigo, IndigoObject, IndigoObject> adapter= CtabWriterUtil.createAdapterFrom(smilesSpec);
        return new MultiChemicalWriter(out, c-> writeAsString(c, smilesSpec, adapter));
    }

    @Override
    public ChemicalWriterImpl newInstance(File outputFile, ChemFormat.ChemFormatWriterSpecification spec) throws IOException {
        return newInstance(new BufferedOutputStream(new FileOutputStream(outputFile)), spec);
    }

    @Override
    public boolean supports(ChemFormat.ChemFormatWriterSpecification spec) {
        return spec instanceof ChemFormat.SmilesFormatWriterSpecification;
    }

    private static class MultiChemicalWriter implements ChemicalWriterImpl{
        private PrintWriter writer;
        private Function<ChemicalImpl, String> function;

        MultiChemicalWriter(OutputStream out, Function<ChemicalImpl, String> function){
            writer = new PrintWriter(out);
            this.function = function;
        }
        @Override
        public void write(ChemicalImpl chemicalImpl) {
            writer.println(function.apply(chemicalImpl));
        }

        @Override
        public void close() throws IOException{
            writer.close();
        }
    }
    
}
