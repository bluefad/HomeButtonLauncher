package com.dynamicg.homebuttonlauncher.tools.drive;

/*
 * see http://hi-android.info/src/com/android/providers/contacts/Hex.java.html
 */
public class Hex {

    private static final char[] HEX_DIGITS = new char[]{
            '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };

    private static final char[] FIRST_CHAR = new char[256];
    private static final char[] SECOND_CHAR = new char[256];
    private static final byte[] DIGITS = new byte['f' + 1];

    static {
        for (int i = 0; i < 256; i++) {
            FIRST_CHAR[i] = HEX_DIGITS[(i >> 4) & 0xF];
            SECOND_CHAR[i] = HEX_DIGITS[i & 0xF];
        }
    }

    static {
        for (int i = 0; i <= 'F'; i++) {
            DIGITS[i] = -1;
        }
        for (byte i = 0; i < 10; i++) {
            DIGITS['0' + i] = i;
        }
        for (byte i = 0; i < 6; i++) {
            DIGITS['A' + i] = (byte) (10 + i);
            DIGITS['a' + i] = (byte) (10 + i);
        }
    }

    /**
     * Quickly converts a byte array to a hexadecimal string representation.
     *
     * @param array byte array, possibly zero-terminated.
     */
    public static String encodeHex(byte[] array, boolean zeroTerminated) {
        char[] cArray = new char[array.length * 2];

        int j = 0;
        for (int i = 0; i < array.length; i++) {
            int index = array[i] & 0xFF;
            if (index == 0 && zeroTerminated) {
                break;
            }

            cArray[j++] = FIRST_CHAR[index];
            cArray[j++] = SECOND_CHAR[index];
        }

        return new String(cArray, 0, j);
    }

    /**
     * Quickly converts a hexadecimal string to a byte array.
     */
    public static byte[] decodeHex(String hexString) {
        int length = hexString.length();

        if ((length & 0x01) != 0) {
            throw new IllegalArgumentException("Odd number of characters.");
        }

        boolean badHex = false;
        byte[] out = new byte[length >> 1];
        for (int i = 0, j = 0; j < length; i++) {
            int c1 = hexString.charAt(j++);
            if (c1 > 'f') {
                badHex = true;
                break;
            }

            final byte d1 = DIGITS[c1];
            if (d1 == -1) {
                badHex = true;
                break;
            }

            int c2 = hexString.charAt(j++);
            if (c2 > 'f') {
                badHex = true;
                break;
            }

            final byte d2 = DIGITS[c2];
            if (d2 == -1) {
                badHex = true;
                break;
            }

            out[i] = (byte) (d1 << 4 | d2);
        }

        if (badHex) {
            throw new IllegalArgumentException("Invalid hexadecimal digit: " + hexString);
        }

        return out;
    }
}

