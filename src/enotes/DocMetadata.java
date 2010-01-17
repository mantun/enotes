/*
 * (c) 2009. Ivan Voras <ivoras@fer.hr>
 */

package enotes;

import java.util.ArrayList;

/**
 *
 * @author ivoras
 */
public class DocMetadata {

    static final byte[] SIGNATURE = { 0x00, (byte)0xff, (byte)0xed, (byte)0xed };
    static final byte VERSION_FORMAT = 1;
    static final byte VERSION_MINOR = 0;
    static final byte[] DEFAULT_IV = new byte[] {
        (byte)0xfE, 0x14, 0x39, (byte)0xaC, 0x0e, 0x72, (byte)0x9F, 0x5b,
        (byte)0xef, 0x01, 0x42, (byte)0xd1, 0x40, 0x12, (byte)0x8F, 0x17
    };

    public ArrayList<SaveMetadata> saveHistory = new ArrayList<SaveMetadata>();
    public boolean modified = false;
    public String filename;
    public int caretPosition;
    public byte[] key;
    
}
