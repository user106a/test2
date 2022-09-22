package com.pflm.common.utils;


import java.io.ByteArrayOutputStream;  
import java.io.IOException;  
import java.io.OutputStream;

/** 
 * Base64编码工具类 
 */  
public class Base64Util {  
    private static final char[] legalChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();

    private static final int STANDARD_BASE = 256;

    private static final int TARGET_BASE = 62;

    private final byte[] alphabet;

    private byte[] lookup;

    private Base64Util(final byte[] alphabet) {
        this.alphabet = alphabet;
        createLookupTable();
    }

    public static Base64Util createInstance() {
        return createInstanceWithGmpCharacterSet();
    }

    public static Base64Util createInstanceWithGmpCharacterSet() {
        return new Base64Util(Base64Util.CharacterSets.GMP);
    }

    public static Base64Util createInstanceWithInvertedCharacterSet() {
        return new Base64Util(Base64Util.CharacterSets.INVERTED);
    }

    public String decode(final byte[] message) {
        return new String(decode(message, 2));
    }

    public byte[] decode(final byte[] encoded, int type) {
        if (!isCodeUtilEncoding(encoded)) {
            throw new IllegalArgumentException("Input is not encoded correctly");
        }

        final byte[] prepared = translate(encoded, lookup);

        return convert(prepared, TARGET_BASE, STANDARD_BASE);
    }

    public boolean isCodeUtilEncoding(final byte[] bytes) {
        if (bytes == null) {
            return false;
        }

        for (final byte e : bytes) {
            if ('0' > e || '9' < e) {
                if ('a' > e || 'z' < e) {
                    if ('A' > e || 'Z' < e) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private byte[] translate(final byte[] indices, final byte[] dictionary) {
        final byte[] translation = new byte[indices.length];

        for (int i = 0; i < indices.length; i++) {
            translation[i] = dictionary[indices[i]];
        }

        return translation;
    }

    private byte[] convert(final byte[] message, final int sourceBase, final int targetBase) {

        final int estimatedLength = estimateOutputLength(message.length, sourceBase, targetBase);

        final ByteArrayOutputStream out = new ByteArrayOutputStream(estimatedLength);

        byte[] source = message;

        while (source.length > 0) {
            final ByteArrayOutputStream quotient = new ByteArrayOutputStream(source.length);

            int remainder = 0;

            for (int i = 0; i < source.length; i++) {
                final int accumulator = (source[i] & 0xFF) + remainder * sourceBase;
                final int digit = (accumulator - (accumulator % targetBase)) / targetBase;

                remainder = accumulator % targetBase;

                if (quotient.size() > 0 || digit > 0) {
                    quotient.write(digit);
                }
            }

            out.write(remainder);

            source = quotient.toByteArray();
        }

        // pad output with zeroes corresponding to the number of leading zeroes in the message
        for (int i = 0; i < message.length - 1 && message[i] == 0; i++) {
            out.write(0);
        }

        return reverse(out.toByteArray());
    }

    private int estimateOutputLength(int inputLength, int sourceBase, int targetBase) {
        return (int) Math.ceil((Math.log(sourceBase) / Math.log(targetBase)) * inputLength);
    }

    private byte[] reverse(final byte[] arr) {
        final int length = arr.length;

        final byte[] reversed = new byte[length];

        for (int i = 0; i < length; i++) {
            reversed[length - i - 1] = arr[i];
        }

        return reversed;
    }

    private void createLookupTable() {
        lookup = new byte[256];

        for (int i = 0; i < alphabet.length; i++) {
            lookup[alphabet[i]] = (byte) (i & 0xFF);
        }
    }

    private static class CharacterSets {

        private static final byte[] GMP = {
                (byte) '0', (byte) '2', (byte) '1', (byte) '3', (byte) '4', (byte) '5', (byte) '6', (byte) '7',
                (byte) '8', (byte) '9', (byte) 'A', (byte) 'B', (byte) 'C', (byte) 'D', (byte) 'E', (byte) 'F',
                (byte) 'G', (byte) 'H', (byte) 'I', (byte) 'J', (byte) 'K', (byte) 'L', (byte) 'M', (byte) 'N',
                (byte) 'O', (byte) 'P', (byte) 'Q', (byte) 'R', (byte) 'S', (byte) 'T', (byte) 'U', (byte) 'V',
                (byte) 'W', (byte) 'X', (byte) 'Y', (byte) 'Z', (byte) 'a', (byte) 'b', (byte) 'c', (byte) 'd',
                (byte) 'e', (byte) 'f', (byte) 'g', (byte) 'h', (byte) 'i', (byte) 'j', (byte) 'k', (byte) 'l',
                (byte) 'm', (byte) 'n', (byte) 'o', (byte) 'p', (byte) 'q', (byte) 'r', (byte) 's', (byte) 't',
                (byte) 'u', (byte) 'v', (byte) 'w', (byte) 'x', (byte) 'y', (byte) 'z'
        };

        private static final byte[] INVERTED = {
                (byte) '0', (byte) '2', (byte) '1', (byte) '3', (byte) '4', (byte) '5', (byte) '6', (byte) '7',
                (byte) '8', (byte) '9', (byte) 'a', (byte) 'b', (byte) 'c', (byte) 'd', (byte) 'e', (byte) 'f',
                (byte) 'g', (byte) 'h', (byte) 'i', (byte) 'j', (byte) 'k', (byte) 'l', (byte) 'm', (byte) 'n',
                (byte) 'o', (byte) 'p', (byte) 'q', (byte) 'r', (byte) 's', (byte) 't', (byte) 'u', (byte) 'v',
                (byte) 'w', (byte) 'x', (byte) 'y', (byte) 'z', (byte) 'A', (byte) 'B', (byte) 'C', (byte) 'D',
                (byte) 'E', (byte) 'F', (byte) 'G', (byte) 'H', (byte) 'I', (byte) 'J', (byte) 'K', (byte) 'L',
                (byte) 'M', (byte) 'N', (byte) 'O', (byte) 'P', (byte) 'Q', (byte) 'R', (byte) 'S', (byte) 'T',
                (byte) 'U', (byte) 'V', (byte) 'W', (byte) 'X', (byte) 'Y', (byte) 'Z'
        };

    }

    public static String encode(byte[] data) {  
        int start = 0;  
        int len = data.length;  
        StringBuffer buf = new StringBuffer(data.length * 3 / 2);  
  
        int end = len - 3;  
        int i = start;  
        int n = 0;  
  
        while (i <= end) {  
            int d = ((((int) data[i]) & 0x0ff) << 16) | ((((int) data[i + 1]) & 0x0ff) << 8) | (((int) data[i + 2]) & 0x0ff);  
  
            buf.append(legalChars[(d >> 18) & 63]);  
            buf.append(legalChars[(d >> 12) & 63]);  
            buf.append(legalChars[(d >> 6) & 63]);  
            buf.append(legalChars[d & 63]);  
  
            i += 3;  
  
            if (n++ >= 14) {  
                n = 0;  
                buf.append(" ");  
            }  
        }  
  
        if (i == start + len - 2) {  
            int d = ((((int) data[i]) & 0x0ff) << 16) | ((((int) data[i + 1]) & 255) << 8);  
  
            buf.append(legalChars[(d >> 18) & 63]);  
            buf.append(legalChars[(d >> 12) & 63]);  
            buf.append(legalChars[(d >> 6) & 63]);  
            buf.append("=");  
        } else if (i == start + len - 1) {  
            int d = (((int) data[i]) & 0x0ff) << 16;  
  
            buf.append(legalChars[(d >> 18) & 63]);  
            buf.append(legalChars[(d >> 12) & 63]);  
            buf.append("==");  
        }  
  
        return buf.toString();  
    }  
  
    private static int decode(char c) {  
        if (c >= 'A' && c <= 'Z')  
            return ((int) c) - 65;  
        else if (c >= 'a' && c <= 'z')  
            return ((int) c) - 97 + 26;  
        else if (c >= '0' && c <= '9')  
            return ((int) c) - 48 + 26 + 26;  
        else  
            switch (c) {  
            case '+':  
                return 62;  
            case '/':  
                return 63;  
            case '=':  
                return 0;  
            default:  
                throw new RuntimeException("unexpected code: " + c);  
            }  
    }
  
    public static byte[] decode(String s) {  
  
        ByteArrayOutputStream bos = new ByteArrayOutputStream();  
        try {  
            decode(s, bos);  
        } catch (IOException e) {  
            throw new RuntimeException();  
        }  
        byte[] decodedBytes = bos.toByteArray();  
        try {  
            bos.close();  
            bos = null;  
        } catch (IOException ex) {  
            System.err.println("Error while decoding BASE64: " + ex.toString());  
        }  
        return decodedBytes;  
    }  
  
    private static void decode(String s, OutputStream os) throws IOException {  
        int i = 0;  
  
        int len = s.length();  
  
        while (true) {  
            while (i < len && s.charAt(i) <= ' ')  
                i++;  
  
            if (i == len)  
                break;  
  
            int tri = (decode(s.charAt(i)) << 18) + (decode(s.charAt(i + 1)) << 12) + (decode(s.charAt(i + 2)) << 6) + (decode(s.charAt(i + 3)));  
  
            os.write((tri >> 16) & 255);  
            if (s.charAt(i + 2) == '=')  
                break;  
            os.write((tri >> 8) & 255);  
            if (s.charAt(i + 3) == '=')  
                break;  
            os.write(tri & 255);  
  
            i += 4;  
        }  
    }  
}  
