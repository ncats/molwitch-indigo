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
import gov.nih.ncats.common.functions.ThrowableFunction;
import gov.nih.ncats.common.io.IOUtil;
import gov.nih.ncats.common.io.InputStreamSupplier;
import gov.nih.ncats.common.io.TextLineParser;
import gov.nih.ncats.common.sneak.Sneak;
import gov.nih.ncats.molwitch.ChemicalSource;
import gov.nih.ncats.molwitch.SmartsSource;
import gov.nih.ncats.molwitch.SmilesSource;
import gov.nih.ncats.molwitch.internal.source.MolStringSource;
import gov.nih.ncats.molwitch.internal.source.StringSource;
import gov.nih.ncats.molwitch.io.ChemFormat;
import gov.nih.ncats.molwitch.spi.ChemicalImpl;
import gov.nih.ncats.molwitch.spi.ChemicalImplFactory;
import gov.nih.ncats.molwitch.spi.ChemicalImplReader;
import org.apache.commons.io.Charsets;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Function;

public class IndigoChemicalImplFactory implements ChemicalImplFactory {
    @Override
    public ChemicalImpl createFromSmiles(String smiles) throws IOException {
        IndigoObject mol = IndigoUtil.getIndigoInstance().loadMolecule(smiles);
        return new IndigoChemicalImpl(mol, new SmilesSource(smiles));
    }

    @Override
    public ChemicalImpl createFromString(String format, String input) throws IOException {
        if(ChemFormat.MolFormatSpecification.NAME.equalsIgnoreCase(format)) {
            IndigoObject mol = IndigoUtil.getIndigoInstance().loadMolecule(input);
            return new IndigoChemicalImpl(mol, new MolStringSource(input));
        }
        if(ChemFormat.SmilesFormatWriterSpecification.NAME.equalsIgnoreCase(format)) {
            return createFromSmiles(input);
        }
        if(ChemFormat.SdfFormatSpecification.NAME.equalsIgnoreCase(format)) {
            IndigoObject mol = IndigoUtil.getIndigoInstance().loadMolecule(input);
            return new IndigoChemicalImpl(mol, new MolStringSource(input, ChemicalSource.Type.SDF));
        }
        if(ChemFormat.SmartsFormatSpecification.NAME.equalsIgnoreCase(format)) {
            return createFromSmarts(input);
        }
        throw new IOException("unknown format " + format);
    }

    @Override
    public ChemicalImplReader create(byte[] bytes, int start, int length) throws IOException {
        return create(new ByteArrayInputStream(bytes, start, length));
    }

    @Override
    public ChemicalImplReader create(String format, byte[] bytes, int start, int length) throws IOException {
        if(start==0 && bytes.length==length){
            //whole array
            IteratorResult iter = getIteratorForFormat(format,bytes);
            return new IndigoChemicalReader(iter);
        }
        IteratorResult iter = getIteratorForFormat(format, Arrays.copyOfRange(bytes,start, start+length-1));
        return new IndigoChemicalReader(iter);
    }

    @Override
    public ChemicalImplReader create(String format, String input) throws IOException {
        //there doesn't seem to be an "iterate" method that takes strings so we'll do it ourselves
        if(ChemFormat.MolFormatSpecification.NAME.equalsIgnoreCase(format)) {
            IndigoObject mol = IndigoUtil.getIndigoInstance().loadMolecule(input);
            ChemicalImpl singleRecord = new IndigoChemicalImpl(mol, new MolStringSource(input));
            return new SingleChemicalImplReader(singleRecord);
        }
        if(ChemFormat.SmilesFormatWriterSpecification.NAME.equalsIgnoreCase(format)) {
            ThrowableFunction<TextLineParser, Optional<String>, IOException> sectionParser = p-> {
                while(true) {
                    String line = p.nextLine();

                    if (line == null) {
                        return Optional.empty();
                    }
                    line = line.trim();
                    if (!line.isEmpty()) {
                        return Optional.of(line);
                    }
                }
            };
            Function<String, ChemicalImpl> reader = s-> {
                    IndigoObject mol = IndigoUtil.getIndigoInstance().loadMolecule(s);
                    return new IndigoChemicalImpl(mol, new SmilesSource(input));
            };
            return new IterativeStringChemicalImplReader(input, sectionParser, reader);
        }
        if(ChemFormat.SdfFormatSpecification.NAME.equalsIgnoreCase(format)) {
            ThrowableFunction<TextLineParser, Optional<String>, IOException> sectionParser = p-> {
                StringBuilder builder = new StringBuilder(2000);
                String line;
                do{
                    line=p.nextLine();
                    if(line !=null){
                        builder.append(line);
                    }
                }while(line !=null && !line.startsWith("$$$$"));
                if(builder.length()==0){
                    return Optional.empty();
                }
                String sdRecord = builder.toString().trim();
                if(sdRecord.isEmpty()){
                    return Optional.empty();
                }
                return Optional.of(sdRecord);
            };

            Function<String, ChemicalImpl> reader = s-> {
                IndigoObject mol = IndigoUtil.getIndigoInstance().loadMolecule(s);
                return new IndigoChemicalImpl(mol, new SmilesSource(input));
            };
            return new IterativeStringChemicalImplReader(input, sectionParser, reader);
        }
        if(ChemFormat.SmartsFormatSpecification.NAME.equalsIgnoreCase(format)) {
            ThrowableFunction<TextLineParser, Optional<String>, IOException> sectionParser = p-> {
                while(true) {
                    String line = p.nextLine();

                    if (line == null) {
                        return Optional.empty();
                    }
                    line = line.trim();
                    if (!line.isEmpty()) {
                        return Optional.of(line);
                    }
                }
            };
            Function<String, ChemicalImpl> reader = s-> {
                IndigoObject mol = IndigoUtil.getIndigoInstance().loadSmarts(s);
                return new IndigoChemicalImpl(mol, new SmartsSource(input));
            };
            return new IterativeStringChemicalImplReader(input, sectionParser, reader);
        }
        throw new IOException("unknown format "+ format);
    }

    @Override
    public ChemicalImplReader create(File file) throws IOException {
        String guessedFormat="smiles";
        String firstLine =null;
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(InputStreamSupplier.forFile(file).get()))){
            firstLine = reader.readLine();
            reader.readLine();
            reader.readLine();
            String defline = reader.readLine();
            if(defline !=null && (defline.endsWith("V2000")|| defline.endsWith("V3000"))){
                guessedFormat = "sdf";
            }else if(hasSmartsChars(firstLine)){
                guessedFormat = "smarts";

            }
        }
        System.out.println("guessedFormat = " + guessedFormat);
        return create(guessedFormat, file);
    }

    @Override
    public ChemicalImplReader create(String format, InputStream in) throws IOException {
        File tmp = toTempFile(format, in);
        return create(format, tmp);
    }

    @Override
    public ChemicalImplReader create(String format, File file) throws IOException {
        if("sdf".equalsIgnoreCase(format)){
                ThrowableFunction<TextLineParser, Optional<String>, IOException> sectionParser = p-> {
                    StringBuilder builder = new StringBuilder(2000);
                    String line;
                    do{
                        line=p.nextLine();
                        if(line !=null){
                            builder.append(line);
                        }
                    }while(line !=null && !line.startsWith("$$$$"));
                    if(builder.length()==0){
                        return Optional.empty();
                    }
                    String sdRecord = builder.toString();
                    if(sdRecord.isEmpty()){
                        return Optional.empty();
                    }
                    return Optional.of(sdRecord);
                };

                Function<String, ChemicalImpl> reader = s-> {
                    IndigoObject mol = IndigoUtil.getIndigoInstance().loadMolecule(s);
                    return new IndigoChemicalImpl(mol, new MolStringSource(s, ChemicalSource.Type.SDF));
                };
                return new IterativeStringChemicalImplReader(file, sectionParser, reader);

        }
        if("mol".equalsIgnoreCase(format)){
            ThrowableFunction<TextLineParser, Optional<String>, IOException> sectionParser = p-> {
                StringBuilder builder = new StringBuilder(2000);
                String line;
                do{
                    line=p.nextLine();
                    if(line !=null){
                        builder.append(line);
                    }
                }while(line !=null && !line.startsWith("M  END"));
                if(builder.length()==0){
                    return Optional.empty();
                }
                String sdRecord = builder.toString();
                if(sdRecord.isEmpty()){
                    return Optional.empty();
                }
                return Optional.of(sdRecord);
            };

            Function<String, ChemicalImpl> reader = s-> {
                IndigoObject mol = IndigoUtil.getIndigoInstance().loadMolecule(s);
                return new IndigoChemicalImpl(mol, new MolStringSource(s, ChemicalSource.Type.MOL));
            };
            return new IterativeStringChemicalImplReader(file, sectionParser, reader);

        }
        ThrowableFunction<TextLineParser, Optional<String>, IOException> sectionParser = p-> {
            //one per line
            //TODO handle ids?
            String line;
            do{
                line=p.nextLine();
            } while(line !=null && line.trim().isEmpty());

            return Optional.ofNullable(line);
        };
        if("smiles".equalsIgnoreCase(format)){
            Function<String, ChemicalImpl> reader = s-> {
                IndigoObject mol = IndigoUtil.getIndigoInstance().loadMolecule(s);
                return new IndigoChemicalImpl(mol, new SmilesSource(s));
            };
            return new IterativeStringChemicalImplReader(file, sectionParser, reader);

        }
        Function<String, ChemicalImpl> reader = s-> {
            IndigoObject mol = IndigoUtil.getIndigoInstance().loadSmarts(s);
            return new IndigoChemicalImpl(mol, new SmartsSource(s));
        };
        return new IterativeStringChemicalImplReader(file, sectionParser, reader);
    }

    private static class IteratorResult{
        final Iterator<IndigoObject> iterator;
        final ChemicalSource source;

        public IteratorResult(Iterator<IndigoObject> iterator, ChemicalSource source) {
            this.iterator = iterator;
            this.source = source;
        }
    }
    private IteratorResult getIteratorForFormat(String format, File file) throws IOException{
        String absPath = file.getAbsolutePath();
        if("smiles".equalsIgnoreCase(format)){
            return new IteratorResult(IndigoUtil.getIndigoInstance().iterateSmilesFile(absPath).iterator(),
                    new SmilesSource(""));
        }
        if("mol".equalsIgnoreCase(format)){
            IndigoObject obj = IndigoUtil.getIndigoInstance().loadMolecule(TextLineParser.parseIntoString(InputStreamSupplier.forFile(file).get()));
            return new IteratorResult(Collections.singleton(obj).iterator(),
                    new MolStringSource(""));
        }

        if("smarts".equalsIgnoreCase(format)){
            return new IteratorResult( IndigoUtil.getIndigoInstance().loadSmartsFromFile(absPath).iterator(),
                    new SmartsSource(""));
        }
        throw new IOException("unsupport format " + format);
    }

    private IteratorResult getIteratorForFormat(String format, byte[] buffer) throws IOException{

        if("smiles".equalsIgnoreCase(format)){

            return new IteratorResult(IndigoUtil.getIndigoInstance().loadStructure(buffer).iterator(),
                    new SmilesSource(""));
        }
        if("mol".equalsIgnoreCase(format) || "sdf".equalsIgnoreCase(format)){

            return new IteratorResult(IndigoUtil.getIndigoInstance().loadStructure(buffer).iterator(),
                    new MolStringSource("", ChemicalSource.Type.parseType(format)));
        }
        if("smarts".equalsIgnoreCase(format)){
            return new IteratorResult(IndigoUtil.getIndigoInstance().loadSmarts(buffer).iterator(),
                    new SmartsSource(""));
        }
        throw new IOException("unsupport format " + format);
    }


    @Override
    public ChemicalImplReader create(String format, InputStreamSupplier in) throws IOException {
        Optional<File> file = in.getFile();
        if(file.isPresent()){
            try {
                return create(format, file.get());
            } catch (IOException e) {
                return Sneak.sneakyThrow(e);
            }
        }
        return create(format, toTempFile(format, in.get()));
    }

    @Override
    public ChemicalImplReader create(InputStreamSupplier in) throws IOException {
        Optional<File> file = in.getFile();
        if(file.isPresent()){
            try {
                return create(file.get());
            } catch (IOException e) {
                return Sneak.sneakyThrow(e);
            }
        }
        return create(toTempFile(in.get()));

    }

    @Override
    public ChemicalImplReader create(InputStream in) throws IOException {
        return create(toTempFile(in));
    }

    @Override
    public ChemicalImpl createNewEmptyChemical() {
        return new IndigoChemicalImpl(IndigoUtil.getIndigoInstance().createMolecule());
    }

    @Override
    public boolean supports(String format) {
        return true;
    }

    @Override
    public ChemicalImpl createFromSmarts(String smarts) throws IOException {
        IndigoObject mol = IndigoUtil.getIndigoInstance().loadSmarts(smarts);
        return new IndigoChemicalImpl(mol, new SmartsSource(smarts));
    }

    @Override
    public boolean isDefault() {
        return true;
    }

    @Override
    public ChemicalImpl create(String unknownFormattedInput) throws IOException {
        if (unknownFormattedInput.indexOf('\n') >= 0 || unknownFormattedInput.indexOf('\r') >= 0) {
            //multi line probably mol or sdf
            IndigoObject mol = IndigoUtil.getIndigoInstance().loadMolecule(unknownFormattedInput);
            return new IndigoChemicalImpl(mol, new StringSource(unknownFormattedInput, ChemicalSource.Type.UNKNOWN));

        }
        //smiles or smarts
        if (hasSmartsChars(unknownFormattedInput)) {
            //has wildcards
            IndigoObject mol = IndigoUtil.getIndigoInstance().loadSmarts(unknownFormattedInput);
            return new IndigoChemicalImpl(mol, new SmartsSource(unknownFormattedInput));

        }
        IndigoObject mol = IndigoUtil.getIndigoInstance().loadMolecule(unknownFormattedInput);
        return new IndigoChemicalImpl(mol, new SmilesSource(unknownFormattedInput));
    }

    private boolean hasSmartsChars(String unknownFormattedInput) {
        return unknownFormattedInput.indexOf(',') > -1 || unknownFormattedInput.indexOf('~') > -1 || unknownFormattedInput.indexOf('*') > -1
                || unknownFormattedInput.contains("[#") || unknownFormattedInput.indexOf('!') > -1 ;
    }

    @Override
    public boolean isFormatAgnostic() {
        return true;
    }

    private static class IndigoChemicalReader implements ChemicalImplReader{
        private final Iterator<IndigoObject> iter;
        private ChemicalSource source;
        public IndigoChemicalReader(IteratorResult iter) {
            this.iter = iter.iterator;
            this.source = iter.source;
        }

        @Override
        public ChemicalImpl read() throws IOException {
            try {
                if (iter.hasNext()) {
                    return new IndigoChemicalImpl(iter.next(),source);
                }
            }catch(Throwable t){
                throw new IOException("error reading next Indigo Object", t);
            }
            return null;
        }

        @Override
        public void close() throws IOException {
            //TODO there is a dispose should we call that?
        }
    }
    private static File toTempFile(InputStream in) throws IOException{
        return toTempFile("unknown", in);
    }
    private static File toTempFile(String format, InputStream in) throws IOException{
        File f = File.createTempFile("molwitch-indigo", "."+format);
        try(OutputStream out = new BufferedOutputStream(new FileOutputStream(f))){
            IOUtil.copy(in,out);
        }
        f.deleteOnExit();
        return f;

    }

    private static class SingleChemicalImplReader implements ChemicalImplReader{
        private ChemicalImpl impl;

        public SingleChemicalImplReader(ChemicalImpl impl) {
            this.impl = impl;
        }

        @Override
        public ChemicalImpl read() throws IOException {
            if(impl !=null){
                ChemicalImpl ret = impl;
                impl = null;
                return ret;
            }
            return impl;
        }

        @Override
        public void close()  {
            impl = null;
        }
    }
    private static class IterativeStringChemicalImplReader implements ChemicalImplReader{
        Function<String, ChemicalImpl> reader ;
        ThrowableFunction<TextLineParser, Optional<String>, IOException> sectionParser;

        TextLineParser parser;
        IterativeStringChemicalImplReader(String input, ThrowableFunction<TextLineParser, Optional<String>, IOException> sectionParser, Function<String, ChemicalImpl> reader) throws IOException{
            parser = new TextLineParser(new ByteArrayInputStream(input.getBytes(Charsets.UTF_8)));
            this.reader = reader;
            this.sectionParser = sectionParser;
        }

        IterativeStringChemicalImplReader(File f, ThrowableFunction<TextLineParser, Optional<String>, IOException> sectionParser, Function<String, ChemicalImpl> reader) throws IOException{
            parser = new TextLineParser(f);
            this.reader = reader;
            this.sectionParser = sectionParser;
        }

        @Override
        public ChemicalImpl read() throws IOException {
            Optional<String> result = sectionParser.apply(parser);
            if(result.isPresent()){
                return reader.apply(result.get());
            }
            return null;
        }

        @Override
        public void close() throws IOException {
            parser.close();
        }
    }
}
