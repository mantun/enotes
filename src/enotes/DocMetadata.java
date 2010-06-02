/*
 * (c) 2009.-2010. Ivan Voras <ivoras@fer.hr>
 * Released under the 2-clause BSDL.
 */

package enotes;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author ivoras
 */
public class DocMetadata {

    static final byte[] SIGNATURE = { 0x00, (byte)0xff, (byte)0xed, (byte)0xed };
    static final byte VERSION_FORMAT = 1;
    static final byte VERSION_MINOR = 0;

    public ArrayList<SaveMetadata> saveHistory = new ArrayList<SaveMetadata>();
    public boolean modified = false;
    public String filename;
    public int caretPosition;
    public byte[] key;

    void saveMetadata(DataOutputStream oout) throws IOException {
        oout.writeInt(caretPosition);
        oout.writeUTF(filename);
        oout.writeInt(saveHistory.size());
        for (SaveMetadata sm: saveHistory) {
            oout.writeLong(sm.timestamp);
            oout.writeUTF(sm.username);
        }
    }

    void loadMetadata(DataInputStream ois) throws IOException {
        caretPosition = ois.readInt();
        filename = ois.readUTF();
        int nSave = ois.readInt();
        saveHistory = new ArrayList<SaveMetadata>();
        for (int i = 0; i < nSave; i++)
            saveHistory.add(new SaveMetadata(ois.readLong(), ois.readUTF()));
    }

}
