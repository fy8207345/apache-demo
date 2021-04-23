package org.example.commons.io;

import org.apache.commons.io.FileSystemUtils;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileSystemUtilsTests {

    public static void main(String[] args) throws IOException {
        long l = FileSystemUtils.freeSpace("c:/");
        FileStore fileStore = Files.getFileStore(Paths.get("c:/"));
        long usableSpace = fileStore.getUsableSpace();
        System.out.println(l);
        System.out.println(usableSpace);
    }
}
