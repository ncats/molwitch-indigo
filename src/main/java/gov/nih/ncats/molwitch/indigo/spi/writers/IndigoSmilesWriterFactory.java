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
