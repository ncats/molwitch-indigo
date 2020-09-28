package gov.nih.ncats.molwitch.indigo.spi.writers;

import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoObject;
import gov.nih.ncats.common.functions.ThrowableFunction;
import gov.nih.ncats.common.util.CachedSupplier;
import gov.nih.ncats.molwitch.io.ChemFormat;
import gov.nih.ncats.molwitch.spi.ChemicalImpl;
import gov.nih.ncats.molwitch.spi.ChemicalWriterImpl;
import gov.nih.ncats.molwitch.spi.ChemicalWriterImplFactory;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.function.BiFunction;
import java.util.function.Function;

public class IndigoMolWriterFactory implements ChemicalWriterImplFactory  {




    @Override
    public String writeAsString(ChemicalImpl chemicalImpl, ChemFormat.ChemFormatWriterSpecification spec) throws IOException {
        BiFunction<Indigo, IndigoObject, IndigoObject> adapter = CtabWriterUtil.createAdapterFrom((ChemFormat.MolFormatSpecification)spec);
        Indigo indigo = new Indigo();
        IndigoObject adapted = adapter.apply(indigo, (IndigoObject) chemicalImpl.getWrappedObject());
        return adapted.molfile();
    }


    @Override
    public ChemicalWriterImpl newInstance(OutputStream out, ChemFormat.ChemFormatWriterSpecification spec) throws IOException {
        return new MolIndigoWriterImpl(new PrintWriter(out), c-> writeAsString(c, spec));
    }

    @Override
    public boolean supports(ChemFormat.ChemFormatWriterSpecification spec) {
        return spec instanceof ChemFormat.MolFormatSpecification;
    }

    private static class MolIndigoWriterImpl implements ChemicalWriterImpl{
        private final PrintWriter writer;
        private final ThrowableFunction<ChemicalImpl, String, IOException>  stringFunction;
        private volatile boolean hasWritten=false;
        public MolIndigoWriterImpl(PrintWriter writer, ThrowableFunction<ChemicalImpl, String, IOException> stringFunction) {
            this.writer = writer;
            this.stringFunction= stringFunction;
        }

        @Override
        public void write(ChemicalImpl chemicalImpl) throws IOException {
            if(hasWritten){
                throw new IOException("already wrote a mol");
            }
            writer.write(stringFunction.apply(chemicalImpl));
            writer.flush();
            hasWritten=true;
        }

        @Override
        public void close() throws IOException {
            writer.close();
        }
    }
}
