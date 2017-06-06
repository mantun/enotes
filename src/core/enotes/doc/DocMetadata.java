/*
 * Copyright (c) 2009-2014 Ivan Voras <ivoras@fer.hr>
 * Copyright (c) 2017-2017 github.com/mantun
 * Released under the 2-clause BSDL.
 */

package enotes.doc;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;

public class DocMetadata implements Serializable {

    private static final long serialVersionUID = 1L;
	
    static final byte[] SIGNATURE = { 'E', 'T', 'X', 'T' };
    static final byte VERSION_FORMAT = 2;

    public boolean modified = false;
    public String filename;
    public String displayName;
    public int caretPosition;
    public byte[] key;

    void saveMetadata(DataOutputStream oout) throws IOException {
        oout.writeInt(caretPosition);
    }

    public void loadMetadata(DataInputStream ois) throws IOException {
        caretPosition = ois.readInt();
    }

    public void setKey(String pwd) {
        key = Util.sha1hash(pwd);
    }

}
