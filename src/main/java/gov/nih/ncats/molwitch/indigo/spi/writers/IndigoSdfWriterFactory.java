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
import gov.nih.ncats.common.functions.ThrowableFunction;
import gov.nih.ncats.common.util.CachedSupplier;
import gov.nih.ncats.common.util.Unchecked;
import gov.nih.ncats.molwitch.indigo.spi.IndigoUtil;
import gov.nih.ncats.molwitch.io.ChemFormat;
import gov.nih.ncats.molwitch.spi.ChemicalImpl;
import gov.nih.ncats.molwitch.spi.ChemicalWriterImpl;
import gov.nih.ncats.molwitch.spi.ChemicalWriterImplFactory;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.function.BiFunction;
import java.util.function.Function;

public class IndigoSdfWriterFactory  implements ChemicalWriterImplFactory {




    @Override
    public boolean supports(ChemFormat.ChemFormatWriterSpecification spec) {
        return spec instanceof ChemFormat.SdfFormatSpecification;
    }

    @Override
    public String writeAsString(ChemicalImpl chemicalImpl, ChemFormat.ChemFormatWriterSpecification spec) throws IOException {
        BiFunction<Indigo, IndigoObject, IndigoObject> adapter =CtabWriterUtil.createAdapterFrom(((ChemFormat.SdfFormatSpecification)spec).getMolSpec());
        Indigo indigo = IndigoUtil.getIndigoInstance();
        IndigoObject adapted = adapter.apply(indigo, (IndigoObject) chemicalImpl.getWrappedObject());
        IndigoObject writer =null;
        String result=null;
        try{
            writer = indigo.writeBuffer();
            System.out.println("adapted self = " + adapted.self);
            writer.sdfAppend(adapted);
            result= writer.toString();
        }finally{
            if(writer !=null){
                writer.close();
            }
        }
        return result;

    }


    @Override
    public ChemicalWriterImpl newInstance(OutputStream out, ChemFormat.ChemFormatWriterSpecification spec) throws IOException {
        return new SdfIndigoWriterImpl(new PrintWriter(out), c-> writeAsString(c, spec));
    }


    private static class SdfIndigoWriterImpl implements ChemicalWriterImpl{
        private final PrintWriter writer;
        private final ThrowableFunction<ChemicalImpl, String, IOException> stringFunction;
        private volatile boolean hasWritten=false;
        public SdfIndigoWriterImpl(PrintWriter writer, ThrowableFunction<ChemicalImpl, String, IOException> stringFunction) {
            this.writer = writer;
            this.stringFunction= stringFunction;
        }

        @Override
        public void write(ChemicalImpl chemicalImpl) throws IOException {
            writer.write(stringFunction.apply(chemicalImpl));
            hasWritten=true;
        }

        @Override
        public void close() throws IOException {
            writer.close();
        }
    }


}
