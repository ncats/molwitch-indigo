package gov.nih.ncats.molwitch.indigo.spi.writers;

import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoObject;
import gov.nih.ncats.common.functions.ThrowableConsumer;
import gov.nih.ncats.common.util.Unchecked;
import gov.nih.ncats.molwitch.io.ChemFormat;
import gov.nih.ncats.molwitch.spi.ChemicalImpl;
import gov.nih.ncats.molwitch.spi.ChemicalWriterImpl;
import gov.nih.ncats.molwitch.spi.ChemicalWriterImplFactory;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public abstract class AbstractIndigoWriterFactory implements ChemicalWriterImplFactory {
    private final String format;
    protected AbstractIndigoWriterFactory(String format){
        this.format = Objects.requireNonNull(format);
    }

    protected abstract BiFunction<Indigo, IndigoObject, IndigoObject>  createAdapterFrom(ChemFormat.ChemFormatWriterSpecification spec);

    @Override
    public ChemicalWriterImpl newInstance(OutputStream out, ChemFormat.ChemFormatWriterSpecification spec) throws IOException {
//        BiFunction<Indigo, IndigoObject, IndigoObject> adapter = createAdapterFrom(spec);
//
//        File tmp = File.createTempFile("indigo", "."+format);
//
//        return new MolSaver(tmp, adapter, format,
//                () -> {
//                    try {
//                        Files.copy(tmp.toPath(), out);
//                    } finally {
//                        out.close();
//                    }
//                });
        BiFunction<Indigo, IndigoObject, IndigoObject> adapter = createAdapterFrom(spec);
        return new MolSaver(out, adapter, format);
    }

    @Override
    public ChemicalWriterImpl newInstance(File outputFile, ChemFormat.ChemFormatWriterSpecification spec) throws IOException {
        BiFunction<Indigo, IndigoObject, IndigoObject> adapter = createAdapterFrom(spec);
        return new MolSaver(outputFile, adapter, format);
    }

    @Override
    public String writeAsString(ChemicalImpl chemicalImpl, ChemFormat.ChemFormatWriterSpecification spec) throws IOException {
        BiFunction<Indigo, IndigoObject, IndigoObject> adapter = createAdapterFrom(spec);
        String[] capture = new String[1];
        try(MolSaver saver = new MolSaver(adapter, format, s->capture[0]=s)) {
            saver.write(chemicalImpl);
        }
        return capture[0];
    }

    private static class MolSaver implements ChemicalWriterImpl{

        private final IndigoObject writer;
        private final Indigo indigo;
        private final BiFunction<Indigo, IndigoObject, IndigoObject> adapter;
        private final Unchecked.ThrowingRunnable<IOException> onClose;

        public MolSaver(BiFunction<Indigo, IndigoObject, IndigoObject> adapter, String format, ThrowableConsumer<String, IOException> resultConsumer) {
            this.indigo = new Indigo();
            IndigoObject tmpBuffer = indigo.writeBuffer();
            writer = indigo.createSaver(tmpBuffer, format);
            this.adapter = adapter;
            this.onClose= ()->  resultConsumer.accept(tmpBuffer.toString());
        }
        public MolSaver(OutputStream out, BiFunction<Indigo, IndigoObject, IndigoObject> adapter, String format) {
            this(adapter, format, s->{
                PrintWriter writer = new PrintWriter(out);
                writer.write(s);
                writer.flush();
            });
        }

        public MolSaver(File out, BiFunction<Indigo, IndigoObject, IndigoObject> adapter, String format) {
            this(out, adapter, format, null);
        }
        public MolSaver(File out, BiFunction<Indigo, IndigoObject, IndigoObject> adapter, String format, Unchecked.ThrowingRunnable<IOException> onClose) {
            this.indigo = new Indigo();
            this.writer = indigo.createFileSaver(out.getAbsolutePath(), format);
            this.adapter = adapter;
            this.onClose= onClose;
        }

        @Override
        public void write(ChemicalImpl chemicalImpl) throws IOException {
            IndigoObject mol = (IndigoObject)chemicalImpl.getWrappedObject();
            IndigoObject adapted = adapter.apply(indigo, mol);
            writer.append(adapted);
        }

        @Override
        public void close() throws IOException {
            writer.close();
            if(onClose !=null){
                onClose.run();
            }
        }
    }
}
