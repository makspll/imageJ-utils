package makspll;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import ij.IJ;
import ij.ImagePlus;

public interface ImageHandler {
    /**
     * Carry out an operation across an image.
     */
    public void HandleImage(ImagePlus image);

    /**
     * Return a list of dependencies that are required to run this plugin together
     * with helpful instructions on how to install them.
     */
    public PluginDependency[] Dependencies();

    public class PluginDependency {
        public String ImageProcessVerb;
        public String PluginName;
        public String CommandName;
        public String InstallationInstructions;

        public PluginDependency(String imageProcessVerb, String pluginName,
                String commandName, String installationInstructions) {
            ImageProcessVerb = imageProcessVerb;
            PluginName = pluginName;
            CommandName = commandName;
            InstallationInstructions = installationInstructions;
        }

        @Override
        public String toString() {
            return "Cannot " + ImageProcessVerb + ". plugin '" + PluginName
                    + "' is required to run command '" + CommandName + "'. " + InstallationInstructions;
        }

        public static void VerifyInstallation(PluginDependency[] dependencies) {
            Hashtable<String, String> commandsHash = new Hashtable<String, String>();
            Hashtable<String, String> realCommandsHash = (Hashtable<String, String>) (ij.Menus.getCommands().clone());
            Set<String> realCommandSet = realCommandsHash.keySet();
            for (Iterator<String> i = realCommandSet.iterator(); i.hasNext();) {
                String command = (String) i.next();
                // Some of these are whitespace only or separators - ignore them:
                String trimmedCommand = command.trim();
                if (trimmedCommand.length() > 0 && !trimmedCommand.equals("-")) {
                    commandsHash.put(command,
                            realCommandsHash.get(command));
                }
            }

            for (PluginDependency dependency : dependencies) {
                if (!commandsHash.containsKey(dependency.CommandName)) {
                    IJ.error(dependency.toString());
                }
            }
        }
    }
}