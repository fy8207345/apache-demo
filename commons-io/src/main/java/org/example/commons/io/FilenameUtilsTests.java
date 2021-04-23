package org.example.commons.io;

import org.apache.commons.io.FilenameUtils;

public class FilenameUtilsTests {

    public static void main(String[] args) {
        String filename = "c:/intel/../windows-version.txt";
        String normalized = FilenameUtils.normalize(filename);
        System.out.println(normalized);
    }
}
