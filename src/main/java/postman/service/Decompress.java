package postman.service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.DataFormatException;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import org.brotli.dec.BrotliInputStream;
import postman.exception.DecompressException;

public class Decompress {

    public static String decompressGzip(byte[] compressedData, String charset) throws DecompressException {
        GZIPInputStream gzipInputStream = null;
        StringBuilder result = new StringBuilder();
        try {
            ByteArrayInputStream byteInputStream = new ByteArrayInputStream(compressedData);
            gzipInputStream = new GZIPInputStream(byteInputStream);
            InputStreamReader inputStreamReader = new InputStreamReader(gzipInputStream, charset);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            // Đọc và in ra phản hồi đã giải mã
            String decompressedLine;
            while ((decompressedLine = bufferedReader.readLine()) != null) {
                result.append(decompressedLine).append("\r\n");
            }
        } catch (IOException ex) {
            Logger.getLogger(HttpClient.class.getName()).log(Level.SEVERE, null, ex);
            throw new DecompressException("Error while descompress body");
        } finally {
            try {
                gzipInputStream.close();
            } catch (IOException ex) {
                Logger.getLogger(HttpClient.class.getName()).log(Level.SEVERE, null, ex);
                throw new DecompressException("Error while descompress body");

            }
        }
        return result.toString();
    }

    public static String decompressDeflate(byte[] compressedData, String charset) throws DecompressException, DataFormatException {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(compressedData);
            Inflater inflater = new Inflater();
            inflater.setInput(compressedData);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            byte[] buffer = new byte[1024];
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                outputStream.write(buffer, 0, count);
            }

            outputStream.close();
            return new String(outputStream.toByteArray(), charset);
        } catch (IOException ex) {
            Logger.getLogger(HttpClient.class.getName()).log(Level.SEVERE, null, ex);
            throw new DecompressException("Error while descompress body");
        }
    }

    public static String decompressBrotli(byte[] compressedData, String charset) throws DecompressException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(compressedData);
        BrotliInputStream brotliInputStream;
        try {
            brotliInputStream = new BrotliInputStream(inputStream);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = brotliInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }

            brotliInputStream.close();
            outputStream.close();
            return new String(outputStream.toByteArray(), charset);
        } catch (IOException ex) {
            Logger.getLogger(Decompress.class.getName()).log(Level.SEVERE, null, ex);
            throw new DecompressException("Error while descompress body");
        }
    }

}
