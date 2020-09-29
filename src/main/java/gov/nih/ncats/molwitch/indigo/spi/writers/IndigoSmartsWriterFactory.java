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
import gov.nih.ncats.molwitch.io.ChemFormat;
import gov.nih.ncats.molwitch.spi.ChemicalImpl;
import gov.nih.ncats.molwitch.spi.ChemicalWriterImpl;
import gov.nih.ncats.molwitch.spi.ChemicalWriterImplFactory;

import java.io.*;
import java.util.function.BiFunction;
import java.util.function.Function;

public class IndigoSmartsWriterFactory<T extends ChemFormat.ChemFormatWriterSpecification> implements ChemicalWriterImplFactory {


    @Override
    public String writeAsString(ChemicalImpl chemicalImpl, ChemFormat.ChemFormatWriterSpecification spec) throws IOException {
        return getSmarts(chemicalImpl);
    }



    @Override
    public ChemicalWriterImpl newInstance(OutputStream out, ChemFormat.ChemFormatWriterSpecification spec) throws IOException {

        return new MultiChemicalWriter(out);
    }

    @Override
    public ChemicalWriterImpl newInstance(File outputFile, ChemFormat.ChemFormatWriterSpecification spec) throws IOException {
        return newInstance(new BufferedOutputStream(new FileOutputStream(outputFile)), spec);
    }

    @Override
    public boolean supports(ChemFormat.ChemFormatWriterSpecification spec) {
        return spec instanceof ChemFormat.SmartsFormatSpecification;
    }

    private static class MultiChemicalWriter implements ChemicalWriterImpl{
        private PrintWriter writer;

        MultiChemicalWriter(OutputStream out){
            writer = new PrintWriter(out);
        }
        @Override
        public void write(ChemicalImpl chemicalImpl) {
            writer.println(getSmarts(chemicalImpl));
        }

        @Override
        public void close() throws IOException{
            writer.close();
        }
    }

    private static String getSmarts(ChemicalImpl chemicalImpl) {
        return ((IndigoObject)chemicalImpl.getWrappedObject()).smarts();
    }

}
