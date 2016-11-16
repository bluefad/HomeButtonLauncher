package com.dynamicg.common;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class FileUtil {

    private static final int BUFFERSIZE = 2048;

    public static ByteArrayOutputStream getContent(File file) throws Exception {
        FileInputStream in = new FileInputStream(file);
        byte[] buffer = new byte[BUFFERSIZE];
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int len;
        while ((len = in.read(buffer)) > 0) {
            out.write(buffer, 0, len);
        }
        in.close();
        return out;
    }

    public static void writeToFile(File file, byte[] decodeHex) throws Exception {
        FileOutputStream os = new FileOutputStream(file);
        os.write(decodeHex);
        os.close();
    }

}
