package gov.nih.ncats.molwitch.indigo.spi.writers;

import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoObject;
import gov.nih.ncats.common.util.CachedSupplier;
import gov.nih.ncats.molwitch.indigo.spi.IndigoUtil;
import gov.nih.ncats.molwitch.io.ChemFormat;

import java.util.function.BiFunction;
import java.util.function.Function;

final class CtabWriterUtil {
    private CtabWriterUtil(){
        //can not instantiate
    }

    public static class AdapterResult{
        Indigo indigo;
        IndigoObject obj;

    }
    public static BiFunction<Indigo, IndigoObject, IndigoObject> createAdapterFrom(ChemFormat.AromaticAwareChemFormatWriterSpecification molSpec){


        return (indigo, obj)-> {
            CachedSupplier<IndigoObject> cloneObj = CachedSupplier.runOnce(obj::clone);
            Indigo currentIndigo = indigo;
            if(molSpec instanceof ChemFormat.MolFormatSpecification){
                switch (((ChemFormat.MolFormatSpecification)molSpec).getVersion()) {
                    case V2000:
                        if(!"2000".equals(currentIndigo.getOption("molfile-saving-mode"))) {


                        indigo.setOption("molfile-saving-mode", "2000");
                    }
                        break;

                    case V3000:
                        indigo.setOption("molfile-saving-mode", "3000");


                }
            }
            switch (molSpec.getHydrogenEncoding()) {
                case MAKE_EXPLICIT: {
                    IndigoObject clone = cloneObj.get();
                    clone.unfoldHydrogens();

                    break;
                }
                case MAKE_IMPLICIT: {
                    IndigoObject clone = cloneObj.get();
                    clone.foldHydrogens();

                    break;
                }
            }
            switch (molSpec.getKekulization()) {
                case FORCE_AROMATIC:{
                    IndigoObject clone = cloneObj.get();
                    clone.aromatize();
                    break;
                }
                case KEKULE:{
                    IndigoObject clone = cloneObj.get();
                    clone.dearomatize();

                    break;
                }
            }

            if(molSpec instanceof ChemFormat.SmilesFormatWriterSpecification){
                switch( ((ChemFormat.SmilesFormatWriterSpecification)molSpec).getEncodeStereo()){
                    case EXCLUDE_STEREO:{

                        IndigoObject indigoObject = cloneObj.get();
                        indigoObject.clearStereocenters();
                        indigoObject.clearCisTrans();
                        break;
                    }
                    case INCLUDE_STEREO:{
                        if(obj.countStereocenters() ==0) {
                            cloneObj.get().markStereobonds();
                        }
                    }
                    default: break;
                }
            }
            if(cloneObj.hasRun()){
                return cloneObj.get();
            }
            return obj;

        };
    }
}
