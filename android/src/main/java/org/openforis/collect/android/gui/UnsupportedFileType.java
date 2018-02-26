package org.openforis.collect.android.gui;

/**
 * @author Stefano Ricci
 */
public class UnsupportedFileType extends Exception {

    private String expectedExtention;
    private String foundExtension;

    public UnsupportedFileType(String expectedExtention, String foundExtension) {
        super(String.format("Unsupported file type: expected \"%s\" found \"%s\"", expectedExtention, foundExtension));
        this.expectedExtention = expectedExtention;
        this.foundExtension = foundExtension;
    }

    public String getExpectedExtention() {
        return expectedExtention;
    }

    public String getFoundExtension() {
        return foundExtension;
    }
}
