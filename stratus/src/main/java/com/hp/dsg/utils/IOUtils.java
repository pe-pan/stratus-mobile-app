package com.hp.dsg.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by panuska on 5.1.2015.
 */
public class IOUtils {
    //copied from commons-io
    public static void write(String data, OutputStream output) throws IOException {
        if (data != null) {
            output.write(data.getBytes());
        }
    }

    private static final int BUFFER_LEN = 1024;

    //copied from http://stackoverflow.com/questions/309424/read-convert-an-inputstream-to-a-string
    public static String toString(InputStream inputStream)
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        copy(inputStream, baos);
        return new String(baos.toByteArray());
    }

    public static void copy(InputStream inputStream, OutputStream outputStream)
            throws IOException {
        byte[] buffer = new byte[BUFFER_LEN];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, length);
        }
    }
}
