package postman.util;

import org.brotli.dec.BrotliInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import postman.exception.DecompressException;

import java.io.*;
import java.util.zip.DataFormatException;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;

public class Decompress {

    private static final Logger log = LoggerFactory.getLogger(Decompress.class);

    public static String decompressGzip(byte[] compressedData, String charset) throws DecompressException {
        try (
                ByteArrayInputStream byteInputStream = new ByteArrayInputStream(compressedData);
                GZIPInputStream gzipInputStream = new GZIPInputStream(byteInputStream);
                InputStreamReader inputStreamReader = new InputStreamReader(gzipInputStream, charset);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader)
        ) {
            String decompressedLine;
            StringBuilder result = new StringBuilder();
            while ((decompressedLine = bufferedReader.readLine()) != null) {
                result.append(decompressedLine).append("\r\n");
            }
            return result.toString();
        } catch (IOException ex) {
            log.error("Unable to decompress", ex);
            throw new DecompressException("Error while decompress body");
        }
    }

    public static String decompressDeflate(byte[] compressedData, String charset) throws DecompressException, DataFormatException {
        try {
            Inflater inflater = new Inflater();
            inflater.setInput(compressedData);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                outputStream.write(buffer, 0, count);
            }
            outputStream.close();
            return outputStream.toString(charset);
        } catch (IOException ex) {
            log.error("Unable to decompress", ex);
            throw new DecompressException("Error while decompress body");
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
            return outputStream.toString(charset);
        } catch (IOException ex) {
            log.error("Unable to decompress", ex);
            throw new DecompressException("Error while decompress body");
        }
    }

    private Decompress() {}
}
