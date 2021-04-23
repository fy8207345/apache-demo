package org.example.commons.io;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class FileUtilsTests {

    public static void main(String[] args) throws IOException {
        FileUtilsTests.read();
    }

    public static void read() throws IOException {
        File file = new File("c:/windows-version.txt");
        List<String> strings = FileUtils.readLines(file, Charset.forName("GBK"));
        System.out.println(strings);
    }
}
