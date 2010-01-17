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

    public ArrayList<SaveMetadata> saveHistory = new ArrayList<SaveMetadata>();
    public boolean modified = false;
    public String filename;
    public int caretPosition;
    public byte[] key;
    
}
