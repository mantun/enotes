/*
 * (c) 2009.-2010. Ivan Voras <ivoras@fer.hr>
 * Released under the 2-clause BSDL.
 */

package enotes.doc;

/**
 *
 * @author ivoras
 */
public class DocPasswordException extends DocException {

    /**
     * Creates a new instance of <code>DocPasswordException</code> without detail message.
     */
    public DocPasswordException() {
    }


    /**
     * Constructs an instance of <code>DocPasswordException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public DocPasswordException(String msg) {
        super(msg);
    }
}
