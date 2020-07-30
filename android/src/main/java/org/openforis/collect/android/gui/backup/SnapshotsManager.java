package org.openforis.collect.android.gui.backup;

import org.apache.commons.io.FileUtils;
import org.openforis.collect.android.gui.util.Dates;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SnapshotsManager {

    private File surveysDir;
    private Pattern snapshotPattern;

    public SnapshotsManager(File surveysDir) {
        this.surveysDir = surveysDir;
        snapshotPattern = Pattern.compile(surveysDir.getName() + "-(\\d+)");
    }

    public File newSnapshotDir() {
        return new File(surveysDir.getParentFile(), surveysDir.getName() + "-" + System.currentTimeMillis());
    }

    public boolean existsSnapshot() {
        File[] snapshotDirs = getSnapshotDirs();
        return snapshotDirs.length > 0;
    }

    public void deleteOldestSnapshot() throws IOException {
        File oldestSnapshotDir = getOldestSnapshotDir();
        if (oldestSnapshotDir != null) {
            FileUtils.deleteDirectory(getOldestSnapshotDir());
        }
    }

    public File getOldestSnapshotDir() {
        File[] snapshotDirs = getSnapshotDirs();
        if (snapshotDirs.length > 0) {
            String[] snapshotDirNames = new String[snapshotDirs.length];
            for (int i = 0; i < snapshotDirs.length; i++) {
                snapshotDirNames[i] = snapshotDirs[i].getName();
            }
            Arrays.sort(snapshotDirNames);

            String oldestSnapshotDirName = snapshotDirNames[0];
            return new File(surveysDir.getParentFile(), oldestSnapshotDirName);
        }
        return null;
    }

    public String getOldestSnapshotDateFormatted() {
        File file = getOldestSnapshotDir();
        Matcher matcher = snapshotPattern.matcher(file.getName());
        if (matcher.find()) {
            String dateStr = matcher.group(1);
            Date date = new Date(Long.parseLong(dateStr));
            return Dates.formatFull(date);
        } else return null;
    }

    private File[] getSnapshotDirs() {
        return surveysDir.getParentFile().listFiles(new FileFilter() {
            public boolean accept(File file) {
                return file.isDirectory() && !file.getName().equals(surveysDir.getName()) &&
                        snapshotPattern.matcher(file.getName()).find();
            }
        });
    }
}
