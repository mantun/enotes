/*
 * Copyright (c) 2009-2014 Ivan Voras <ivoras@fer.hr>
 * Copyright (c) 2017-2017 github.com/mantun
 * Released under the 2-clause BSDL.
 */

package enotes.doc;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Util {

    /** Crypto mode to use while writing the file */
    public static final String CRYPTO_MODE = "AES/CBC/PKCS5Padding";

    /** The short name of the crypt algorithm used on the files */
    public static final String CRYPTO_ALG = "AES";

    /**
     * Returns a binary MD5 hash of the given string.
     */
    public static byte[] md5hash(String s) {
        return hash(s, "MD5");
    }


    /**
     * Returns a binary MD5 hash of the given binary buffer.
     */
    public static byte[] md5hash(byte[] buf) {
        return hash(buf, "MD5");
    }


    /**
     * Returns a binary SHA1 hash of the given string.
     */
    public static byte[] sha1hash(String s) {
        return hash(s, "SHA1");
    }


    /**
     * Returns a binary SHA1 hash of the given buffer.
     */
    public static byte[] sha1hash(byte[] buf) {
        return hash(buf, "SHA1");
    }
    

    /**
     * Returns a binary hash calculated with the specified algorithm of the
     * given string.
     */
    public static byte[] hash(String s, String hashAlg) {
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
     */
    public static byte[] hash(byte[] buf, String hashAlg) {
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
     */
    public static byte[] concat(byte[] b1, byte[] b2) {
        byte[] r = new byte[b1.length + b2.length];
        System.arraycopy(b1, 0, r, 0, b1.length);
        System.arraycopy(b2, 0, r, b1.length, b2.length);
        return r;
    }

    public static Cipher getCipher(byte[] iv, byte[] key, int mode) {
        AlgorithmParameterSpec paramSpec = new IvParameterSpec(iv);
        Cipher dcipher = null;
        try {
            dcipher = Cipher.getInstance(CRYPTO_MODE);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }
        try {
            dcipher.init(mode, new SecretKeySpec(key, 0, 16, CRYPTO_ALG), paramSpec);
        } catch (InvalidKeyException | InvalidAlgorithmParameterException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }
        return dcipher;
    }
}
