package gov.nih.ncats.molwitch.indigo.spi;

import gov.nih.ncats.molwitch.spi.WitchModule;

public class IndigoWitchModule implements WitchModule {
    @Override
    public String getName() {
        return "Indigo "+ IndigoUtil.getIndigoInstance().version();
    }
}
