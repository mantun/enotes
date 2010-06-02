/*
 * (c) 2009.-2010. Ivan Voras <ivoras@fer.hr>
 * Released under the 2-clause BSDL.
 */

package enotes;

import java.io.Serializable;

/**
 *
 * @author ivoras
 */
public class SaveMetadata implements Serializable {
    public long     timestamp;
    public String   username;

    public SaveMetadata(long timestamp, String username) {
        this.timestamp = timestamp;
        this.username = username;
    }
}
