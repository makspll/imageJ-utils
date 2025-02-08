package makspll.polarization;

import ij.gui.GenericDialog;

public class PolarizationWizard {
    public boolean batchMode = false;

    public PolarizationWizard() {
        // build dialog and ask user for all the necessary information
        GenericDialog dialog = new GenericDialog("Polarization Wizard");
        dialog.addCheckbox("Batch Mode", false);
        dialog.showDialog();

        if (dialog.wasCanceled()) {
            throw new RuntimeException("User cancelled the operation");
        }

        this.batchMode = dialog.getNextBoolean();
    }
}
