/*
 * (c) 2009.-2010. Ivan Voras <ivoras@fer.hr>
 * Released under the 2-clause BSDL.
 */

package enotes;

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

    public static byte[] md5hash(String s) {
        return hash(s, "MD5");
    }

    public static byte[] sha1hash(String s) {
        return hash(s, "SHA1");
    }

    public static byte[] hash(String s, String hash) {
        byte b[] = null;
        try {
            b = s.getBytes("UTF-8");
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance(hash);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }
        return md.digest(b);
    }

    public static String bytea2hex(byte[] h) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < h.length; i++)
            sb.append(String.format("%02x", h[i] & 0xff));
        return sb.toString();
    }

}
