/*
 * (c) 2009.-2010. Ivan Voras <ivoras@fer.hr>
 * Released under the 2-clause BSDL.
 */

package enotes.doc;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ivoras
 */
public class Util {


    /**
     * Returns a binary MD5 hash of the given string.
     *
     * @param s
     * @return
     */
    public static byte[] md5hash(String s) {
        return hash(s, "MD5");
    }


    /**
     * Returns a binary MD5 hash of the given binary buffer.
     *
     * @param buf
     * @return
     */
    public static byte[] md5hash(byte[] buf) {
        return hash(buf, "MD5");
    }


    /**
     * Returns a binary SHA1 hash of the given string.
     *
     * @param s
     * @return
     */
    public static byte[] sha1hash(String s) {
        return hash(s, "SHA1");
    }


    /**
     * Returns a binary SHA1 hash of the given buffer.
     *
     * @param buf
     * @return
     */
    public static byte[] sha1hash(byte[] buf) {
        return hash(buf, "SHA1");
    }
    

    /**
     * Returns a binary hash calculated with the specified algorithm of the
     * given string.
     *
     * @param s
     * @param hashAlg
     * @return
     */
    public static final byte[] hash(String s, String hashAlg) {
        byte b[] = null;
        try {
            b = s.getBytes("UTF-8");
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }
        return hash(b, hashAlg);
    }


    /**
     * Converts a binary buffer to a string of lowercase hexadecimal characters.
     *
     * @param h
     * @return
     */
    public static String bytea2hex(byte[] h) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < h.length; i++)
            sb.append(String.format("%02x", h[i] & 0xff));
        return sb.toString();
    }


    /**
     * Returns a binary hash calculated with the specified algorithm of the
     * given input buffer.
     *
     * @param buf
     * @param hashAlg
     * @return
     */
    public static final byte[] hash(byte[] buf, String hashAlg) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance(hashAlg);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }
        return md.digest(buf);
    }


    /**
     * Concatenates two byte arrays and returns the result.
     * 
     * @param b1
     * @param b2
     * @return
     */
    public static final byte[] concat(byte[] b1, byte[] b2) {
        byte[] r = new byte[b1.length + b2.length];
        System.arraycopy(b1, 0, r, 0, b1.length);
        System.arraycopy(b2, 0, r, b1.length, b2.length);
        return r;
    }
}
