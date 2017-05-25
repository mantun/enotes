/*
 * Copyright (c) 2009-2014 Ivan Voras <ivoras@fer.hr>
 * Copyright (c) 2017-2017 github.com/mantun
 * Released under the 2-clause BSDL.
 */

package enotes.doc;

import enotes.doc.loaders.OldSigLoader;
import enotes.doc.loaders.V2Loader;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

public class Doc {

    private String text;
    private DocMetadata docm;

    public Doc(String text, DocMetadata docm) {
        this.text = text;
        this.docm = docm;
    }

    /**
     * Saves the currently edited document to the given file.
     */
    public boolean save(OutputStream out) throws IOException, DocPasswordException  {
        if (docm.key == null) {
            throw new DocPasswordException("Key not set in DocMetadata");
        }

        try (BufferedOutputStream bout = new BufferedOutputStream(out)) {

            bout.write(DocMetadata.SIGNATURE);
            bout.write(DocMetadata.VERSION_FORMAT);

            byte[] iv = new byte[16];
            try {
                SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
                random.nextBytes(iv);
            } catch (NoSuchAlgorithmException ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                System.exit(1);
            }

            byte[] keyHash = Util.sha1hash(Util.concat(docm.key, iv));

            bout.write(keyHash, 0, 2); /* Save password hash */
            bout.write(iv);

            Cipher ecipher = Util.getCipher(iv, docm.key, Cipher.ENCRYPT_MODE);
            try (CipherOutputStream cout = new CipherOutputStream(bout, ecipher); GZIPOutputStream zout = new GZIPOutputStream(cout); DataOutputStream dout = new DataOutputStream(zout)) {
                docm.saveMetadata(dout);
                byte[] ddata = text.getBytes("UTF-8");
                dout.writeInt(ddata.length);
                dout.write(ddata);
                System.out.println("Written " + ddata.length + " bytes");
            }
        }
        return true;
    }


    /**
     * Opens the specified file to be the currently edited document.
     */
    public static Doc open(InputStream in, String pwd) throws IOException, DocException {
        try (BufferedInputStream bin = new BufferedInputStream(in)) {
            byte[] sig = new byte[DocMetadata.SIGNATURE.length];
            bin.read(sig);
            if (!Arrays.equals(sig, DocMetadata.SIGNATURE)) {
                if (Arrays.equals(sig, OldSigLoader.SIGNATURE)) {
                    return new OldSigLoader().load(bin, pwd);
                }
                throw new DocException("File is not a valid Encrypted Notepad file");
            }
            byte ver_format = (byte) bin.read();
            if (ver_format > DocMetadata.VERSION_FORMAT) {
                throw new DocException("File format version is newer than this app version supports");
            }
            switch (ver_format) {
                case 2: return new V2Loader().load(bin, pwd);
                default: throw new DocException("Unknown file version: " + ver_format);
            }
        }
    }


    public String getText() {
        return text;
    }


    public DocMetadata getDocMetadata() {
        return docm;
    }
}
