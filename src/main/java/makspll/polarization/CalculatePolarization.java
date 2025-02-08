package makspll.polarization;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.Roi;
import ij.gui.Toolbar;
import ij.gui.WaitForUserDialog;
import makspll.FileProcessor;
import makspll.ImageHandler;

public class CalculatePolarization implements FileProcessor.FileHandler, ImageHandler {

    static String[] IgnoreExtensions = { ".Identifier" };

    @Override
    public PluginDependency[] Dependencies() {
        return new PluginDependency[] {
                new PluginDependency(
                        "Run the mexican hat filter to enhance vessel visibility",
                        "Mexican Hat Filter",
                        "Mexican Hat Filter",
                        "Follow installation instructions over at: "
                                + "https://imagej.net/ij/plugins/mexican-hat/index.html"),
                new PluginDependency(
                        "Run automatic tresholding to create a vessel mask",
                        "Auto Threshold",
                        "Auto Threshold",
                        "Follow installation instructions over at: " +
                                "https://imagej.net/plugins/auto-threshold")
        };
    }

    public void HandleImage(ImagePlus image) {

        IJ.log("Successfully opened Image: " + image.getTitle());

        // ask user to select the channel and slice then press okay when ready

        WaitForUserDialog dialog = new WaitForUserDialog("Select Channel and Slice then press okay when ready.",
                "I am ready to proceed");
        dialog.setSize(400, 100);
        dialog.show();

        // duplicate current stack and slice
        int slice = image.getCurrentSlice();
        int channel = image.getChannel();

        IJ.log("Using slice: " + slice + " channel: " + channel);

        String slice_range = slice + "," + slice;
        IJ.run("Duplicate...",
                "title=PolarizationMask duplicate slices=" + slice_range + " channels=" + channel);

        // use the duplicated image
        ImagePlus maskImage = IJ.getImage();

        // calculate the vessel mask
        Roi vesselMask = calculateVesselMask(maskImage);

        // close the mask image
        maskImage.close();

        // apply mask to the original image
        image.setRoi(vesselMask);

        // get the mean of the ROI
        double mean = image.getStatistics().mean;

        // deselect
        image.deleteRoi();

        // calculate how many pixels are above the mean
        image.getProcessor().threshold((int) mean);

        // get histogram of 0 and max values
        int[] histogram = image.getProcessor().getHistogram();

        int lowVal = histogram[0];
        int highVal = histogram[255];

        // calculate the polarization
        // i.e. 1 - (no of pixels above the mean / pixels)

        double polarization = 1 - ((double) highVal / (lowVal + highVal));
        // save polarization in .txt file corresponding to the image title

        String title = image.getTitle();

        // ask user for the output directory
        String directory = image.getOriginalFileInfo().directory;

        // write "Polarization: " + polarization to the file
        IJ.log("Polarization: " + polarization);
        FileWriter myWriter;
        try {
            String fileName = directory + File.separator + title + "polarization-slice-" + slice + "-channel-" + channel
                    + ".txt";
            myWriter = new FileWriter(fileName);
            myWriter.write("Polarization: " + polarization);
            myWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void handleFile(String file) {
        // check if the file should be ignored
        for (String extension : IgnoreExtensions) {
            if (file.endsWith(extension)) {
                IJ.log("Ignoring file: " + file);
                return;
            }
        }

        // open the image
        IJ.open(file);
        ImagePlus image;

        try {
            image = IJ.getImage();
        } catch (Exception e) {
            IJ.log("Could not open image: " + file + "skipping.");
            return;
        }

        HandleImage(image);
    }

    private Roi calculateVesselMask(ImagePlus image) {
        RunCommandWithOptionalWait("Enhance Contrast...", "saturated=0.4 equalize");

        RunCommandWithOptionalWait("Mexican Hat Filter", "radius=10");

        RunCommandWithOptionalWait("Auto Threshold", "method=Shanbhag");

        // let the user invert the mask if needed
        GenericDialog dialog = new GenericDialog("Invert Mask? (vessels should be bright/white)");
        dialog.addCheckbox("Invert Mask", false);
        dialog.setSize(400, 100);
        dialog.showDialog();
        if (dialog.wasCanceled()) {
            throw new RuntimeException("User cancelled the operation");
        }
        boolean invertMask = dialog.getNextBoolean();

        if (invertMask) {
            RunCommandWithOptionalWait("Invert", null);
        }

        RunCommandWithOptionalWait("Remove Outliers...", "radius=5 threshold=50 which=Dark");
        for (int i = 0; i < 2; i++) {
            IJ.run("Remove Outliers...", "radius=3 threshold=0 which=Bright");
        }
        for (int i = 0; i < 10; i++) {
            IJ.run("Despeckle");
        }

        // ask user to select the vessel mask
        Roi selected = WaitForUserWandSelections(image, "Select all the vessels using any selection tools");

        return selected;
    }

    private void RunCommandWithOptionalWait(String command, String options) {
        WaitForUserDialog dialog = new WaitForUserDialog("About to run: " + command + " " + options,
                "I am ready");

        // make size big enough to read the message
        dialog.setSize(400, 100);
        dialog.show();

        if (options == null || options.isEmpty()) {
            IJ.run(command);
        } else {
            IJ.run(command, options);
        }
    }

    private Roi WaitForUserWandSelections(ImagePlus img, String message) {
        IJ.setTool(Toolbar.WAND);
        WaitForUserDialog waitDialog = new WaitForUserDialog(message, "I am happy with my selections");
        waitDialog.setSize(400, 100);
        waitDialog.show();

        // get the selections
        Roi roi = img.getRoi();

        return roi;

    }

}
