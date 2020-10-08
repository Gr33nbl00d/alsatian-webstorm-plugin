package de.greenlood.alsatian.webstorm.plugin;

import java.io.File;

public class CoverageTempDataPaths {
    private final File nycTempDirectory;
    private final File reportTempDirectory;

    public CoverageTempDataPaths(File nycTempDirectory, File reportTempDirectory) {
        this.nycTempDirectory = nycTempDirectory;
        this.reportTempDirectory = reportTempDirectory;
    }

    public File getNycTempDirectory() {
        return nycTempDirectory;
    }

    public File getReportTempDirectory() {
        return reportTempDirectory;
    }
}
