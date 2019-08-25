package theredspy15.ltecleanerfoss;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.widget.TextView;

import com.fxn.stash.Stash;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static theredspy15.ltecleanerfoss.WhitelistActivity.getWhiteList;

public class FileScanner {
    private File path;
    private MainActivity gui;
    private int filesRemoved = 0;
    private int kilobytesTotal = 0;
    private boolean delete = false;
    private boolean emptyDir = false;
    private boolean autoWhite = true;
    static ArrayList<String> filters = new ArrayList<>();
    private static String[] protectedFileList = {
            "BACKUP", "backup", "Backup", "backups",
            "Backups", "BACKUPS", "copy", "Copy", "copies", "Copies", "IMPORTANT",
            "important", "important", "do_not_edit"};

    public FileScanner(File path) {
        this.path = path;
    }

    private List<File> getListFiles() {
        return getListFiles(path);
    }

    /**
     * Used to generate a list of all files on device
     * @param parentDirectory where to start searching from
     * @return List of all files on device (besides whitelisted ones)
     */
    private synchronized List<File> getListFiles(File parentDirectory) {

        ArrayList<File> inFiles = new ArrayList<>();
        File[] files = parentDirectory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (!isWhiteListed(file)) { // won't touch if whitelisted
                    if (file.isDirectory()) { // folder

                        if (autoWhite) {
                            if (!autoWhiteList(file))
                                inFiles.add(file);
                        }
                        else inFiles.add(file); // add folder itself

                        inFiles.addAll(getListFiles(file)); // add contents to returned list

                    } else inFiles.add(file); // add file
                }
            }
        }

        return inFiles;
    }

    /**
     * Runs a for each loop through the white list, and compares the path of the file
     * to each path in the list
     * @param file file to check if in the whitelist
     * @return true if is the file is in the white list, false if not
     */
    private synchronized boolean isWhiteListed(File file) {

        for (String path : getWhiteList()) if (path.equals(file.getAbsolutePath()) || path.equals(file.getName())) return true;

        return false;
    }

    /**
     * Runs before anything is filtered/cleaned. Automatically adds folders to the whitelist
     * based on the name of the folder itself
     * @param file file to check whether it should be added to the whitelist
     */
    private synchronized boolean autoWhiteList(File file) {

        for (String protectedFile : protectedFileList) {
            if (file.getName().contains(protectedFile) && !getWhiteList().contains(file.getAbsolutePath())) {
                getWhiteList().add(file.getAbsolutePath());
                Stash.put("whiteList", getWhiteList());
                return true;
            }
        }

        return false;
    }


    /**
     * Runs as for each loop through the extension filter, and checks if
     * the file name contains the extension
     * @param file file to check
     * @return true if the file's extension is in the filter, false otherwise
     */
    public synchronized boolean filter(File file) {
        if (file.isDirectory() && isDirectoryEmpty(file)
                && emptyDir) return true; // empty folder

        for (String extension : filters)
            if (file.getAbsolutePath().contains(extension)) return true; // file

        return false; // not empty folder or file in filter
    }

    /**
     * lists the contents of the file to an array, if the array length is 0, then return true,
     * else false
     * @param directory directory to test
     * @return true if empty, false if containing a file(s)
     */
    private synchronized boolean isDirectoryEmpty(File directory) {

        return Objects.requireNonNull(directory.listFiles()).length == 0;
    }


    /**
     * Adds paths to the white list that are not to be cleaned. As well as adds
     * extensions to filter. 'generic', 'aggressive', and 'apk' should be assigned
     * by calling preferences.getBoolean()
     */
    @SuppressLint("ResourceType")
    public synchronized void setUpFilters(List<String> filterList, boolean apk) {

        // filters
        filters.clear();
        filters.addAll(filterList);

        // apk
        if (apk) filters.add(".apk");
    }

    public int startScan() {
        byte cycles = 0;
        byte maxCycles = 10;
        List<File> foundFiles;
        if (!delete) maxCycles = 1; // when nothing is being deleted. Stops duplicates from being found

        // removes the need to 'clean' multiple times to get everything
        while (cycles < maxCycles) {

            // find files
            foundFiles = getListFiles();
            if (gui != null) gui.scanPBar.setMax(gui.scanPBar.getMax() + foundFiles.size());

            // scan & delete
            for (File file : foundFiles) {
                if (filter(file)) { // filter
                    TextView tv = null;
                    if (gui != null)
                        tv = gui.displayPath(file);

                    if (delete)
                        if (file.delete()) { // deletion
                            kilobytesTotal += Integer.parseInt(String.valueOf(file.length() / 1024));
                            ++filesRemoved;
                        } else if (tv != null) tv.setTextColor(Color.RED); // error effect

                }

                if (gui != null) {
                    // progress
                    gui.runOnUiThread(() -> gui.scanPBar.setProgress(gui.scanPBar.getProgress() + 1));
                    double scanPercent = gui.scanPBar.getProgress() * 100.0 / gui.scanPBar.getMax();
                    gui.runOnUiThread(() -> gui.progressText.setText(String.format(Locale.US, "%.0f", scanPercent) + "%"));
                }
            }

            if (filesRemoved == 0) break; // nothing found this run, no need to run again

            filesRemoved = 0; // reset for next cycle
            ++cycles;
        }

        return kilobytesTotal;
    }

    public void setEmptyDir(boolean emptyDir) {
        this.emptyDir = emptyDir;
    }

    public void setGUI(MainActivity gui) {
        this.gui = gui;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }

    public void setAutoWhite(boolean autoWhite) {
        this.autoWhite = autoWhite;
    }
}
