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

    public ArrayList<SaveMetadata> saveHistory = new ArrayList<SaveMetadata>();
    public boolean modified = false;
    public String filename;
    public int caretPosition;
}
