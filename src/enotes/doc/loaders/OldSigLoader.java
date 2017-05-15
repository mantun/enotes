/*
 * Copyright (c) 2009-2014 Ivan Voras <ivoras@fer.hr>
 * Copyright (c) 2017-2017 github.com/mantun
 * Released under the 2-clause BSDL.
 */

package enotes.doc.loaders;

import enotes.doc.Doc;
import enotes.doc.DocException;
import enotes.doc.DocMetadata;
import enotes.doc.DocPasswordException;
import enotes.doc.Util;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

public class OldSigLoader implements DocLoader {
    public static final byte[] SIGNATURE = { 0x00, (byte)0xff, (byte)0xed, (byte)0xed };
    private static final byte VERSION_FORMAT = 1;
    private static final byte VERSION_MINOR = 2;

    /**
     * Expects stream to contain version format still
     */
    @Override
    public Doc load(InputStream bin, String pwd) throws IOException, DocException {
        byte ver_format = (byte) bin.read();
        if (ver_format > VERSION_FORMAT)
            throw new DocException("File is a Encrypted Notepad file but cannot be opened by this version of the program");
        byte ver_minor = (byte) bin.read();
        if (ver_minor > VERSION_MINOR)
            throw new DocException("File format version is newer than this app version supports");
        byte[] pwdhash = new byte[2];
        bin.read(pwdhash);
        byte[] iv = new byte[16];
        bin.read(iv);

        DocMetadata newdocm = new DocMetadata();
        newdocm.key = Util.sha1hash(pwd);

        boolean equal;
        if (ver_minor == 0) {
            equal = true;
            for (int i = 0; i < pwdhash.length; i++)
                if (pwdhash[i] != newdocm.key[newdocm.key.length-3+i]) {
                    equal = false;
                    break;
                }
        } else if (ver_minor == 1 || ver_minor == 2) {
            byte[] keyHash = Util.sha1hash(Util.concat(newdocm.key, iv));
            equal = keyHash[0] == pwdhash[0] && keyHash[1] == pwdhash[1];
        } else
            throw new DocException("Cannot read document with ver_minor="+ver_minor);

        if (!equal)
            throw new DocPasswordException("Invalid password!");

        Cipher dcipher = Util.getCipher(iv, newdocm.key, Cipher.DECRYPT_MODE);
        try (CipherInputStream cin = new CipherInputStream(bin, dcipher); GZIPInputStream zin = new GZIPInputStream(cin); DataInputStream din = new DataInputStream(zin)) {
            newdocm.caretPosition = din.readInt();
            din.readUTF(); // discard filename
            int nSave = din.readInt(); // number of save history entries
            for (int i = 0; i < nSave; i++) {
                din.readLong(); // discard time 
                din.readUTF();  // discard user
            }

            String newtext;
            if (ver_minor < 2)
                newtext = din.readUTF();
            else {
                int len = din.readInt();
                byte[] ddata = new byte[len];
                int total_read = 0;
                while (total_read < len) {
                    int nread = din.read(ddata, total_read, len-total_read);
                    total_read += nread;
                }
                System.out.println("Read "+ total_read + " bytes");
                newtext = new String(ddata, "UTF-8");
            }
            return new Doc(newtext, newdocm);
        }
    }
}
