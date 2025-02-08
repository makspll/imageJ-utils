package makspll;

import java.io.File;
import ij.IJ;

/**
 * Performs a repeated routine on a user-selected list of files
 */
public class FileProcessor {

    private String procedureName;

    public FileProcessor(String procedureName) {
        this.procedureName = procedureName;
    }

    public void processFiles(FileHandler handler) {
        String directory = IJ.getDirectory("Choose directory containing .lif images for: " + procedureName);
        if (directory == null) {
            return;
        }

        File dir = new File(directory);

        if (!dir.isDirectory()) {
            IJ.error("The selected directory is not a directory");
            return;
        }

        String[] files = dir.list();

        for (String file : files) {
            // ignore dirs
            String absolutePath = dir.getAbsolutePath() + File.separator + file;
            if (new File(absolutePath).isDirectory()) {
                continue;
            }

            IJ.log("Processing file: " + file);
            handler.handleFile(absolutePath);
        }
    }

    public interface FileHandler {
        public abstract void handleFile(String file);
    }
}
