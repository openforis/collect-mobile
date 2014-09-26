package org.openforis.collect.android.util

import spock.lang.Specification

class UnzipperTest extends Specification {
    def zipFile = new File(getClass().getResource("/test-survey.collect-mobile").file)
    def outputFolder = File.createTempDir();
    def unZipper = new Unzipper(zipFile, outputFolder)

    def cleanup() {
        outputFolder.deleteDir()
    }

    def 'Can unzip a file'() {
        when: unZipper.unzip('collect.db')
        then: outputFolder.list().contains('collect.db')
    }

    def 'Can unzip many files'() {
        when: unZipper.unzip('collect.db', 'info.properties')
        then: outputFolder.list() as Set == ['collect.db', 'info.properties'] as Set
    }

    def 'Throws FileNotFoundException when file not in archive'() {
        when: unZipper.unzip('none-existing')
        then: thrown FileNotFoundException
    }
}
