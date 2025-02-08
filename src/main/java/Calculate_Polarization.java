
import ij.IJ;
import ij.plugin.frame.PlugInFrame;
import makspll.FileProcessor;
import makspll.ImageHandler.PluginDependency;
import makspll.polarization.CalculatePolarization;
import makspll.polarization.PolarizationWizard;

/**
 * @author Maksymilian Mozolewski
 */
public class Calculate_Polarization extends PlugInFrame {

	/** Creates a new instance of ImageJ_Utils */
	public Calculate_Polarization() {
		super("Calculate Polarization");
	}

	public void run(String arg) {
		PluginDependency.VerifyInstallation(new CalculatePolarization().Dependencies());

		// create wizard
		PolarizationWizard wizard = new PolarizationWizard();

		if (wizard.batchMode) {
			FileProcessor processor = new FileProcessor("Calculating Polarization");
			CalculatePolarization plugin = new CalculatePolarization();
			processor.processFiles(plugin);
		} else {
			CalculatePolarization plugin = new CalculatePolarization();
			plugin.HandleImage(IJ.getImage());
		}

	}

}