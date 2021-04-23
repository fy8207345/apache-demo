package org.example.commons.io;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class IOUtilsTests {

    public static void main(String[] args) {
//        IOUtilsTests.tostring();
        IOUtilsTests.buffer();
    }

    public static void buffer(){
        try {
            BufferedInputStream bufferedInputStream = IOUtils.buffer(inputStream(), IOUtils.DEFAULT_BUFFER_SIZE);
            InputStreamReader inputStreamReader = new InputStreamReader(bufferedInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            while ((line = bufferedReader.readLine()) != null){
                System.out.println(line);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void tostring(){
        try {
            System.out.println(IOUtils.toString(inputStream(), StandardCharsets.UTF_8));;
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static InputStream inputStream() throws IOException {
        return new URL("https://www.baidu.com").openStream();
    }
}
