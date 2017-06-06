/*
 * Copyright (c) 2009-2014 Ivan Voras <ivoras@fer.hr>
 * Copyright (c) 2017-2017 github.com/mantun
 * Released under the 2-clause BSDL.
 */

package enotes.doc.loaders;

import enotes.doc.Doc;
import enotes.doc.DocMetadata;
import enotes.doc.DocPasswordException;
import enotes.doc.Util;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

public class V2Loader implements DocLoader {
    @Override
    public Doc load(InputStream bin, String pwd) throws IOException, DocPasswordException {
        byte[] pwdhash = new byte[2];
        bin.read(pwdhash);
        byte[] iv = new byte[16];
        bin.read(iv);

        DocMetadata newdocm = new DocMetadata();
        newdocm.key = Util.sha1hash(pwd);

        byte[] keyHash = Util.sha1hash(Util.concat(newdocm.key, iv));
        if (keyHash[0] != pwdhash[0] || keyHash[1] != pwdhash[1]) {
            throw new DocPasswordException("Invalid password!");
        }

        Cipher dcipher = Util.getCipher(iv, newdocm.key, Cipher.DECRYPT_MODE);
        try (CipherInputStream cin = new CipherInputStream(bin, dcipher); GZIPInputStream zin = new GZIPInputStream(cin); DataInputStream din = new DataInputStream(zin)) {
            newdocm.loadMetadata(din);

            int len = din.readInt();
            byte[] ddata = new byte[len];
            int total_read = 0;
            while (total_read < len) {
                int nread = din.read(ddata, total_read, len - total_read);
                total_read += nread;
            }
            System.out.println("Read " + total_read + " bytes");
            String newtext = new String(ddata, "UTF-8");

            return new Doc(newtext, newdocm);
        }
    }

}
