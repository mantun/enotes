/*
 * (c) 2009.-2010. Ivan Voras <ivoras@fer.hr>
 * Released under the 2-clause BSDL.
 */

package enotes.doc;

/**
 *
 * @author ivoras
 */
public class DocException extends Exception {

    /**
	 * 
	 */
	private static final long serialVersionUID = -8618217887516753754L;


	/**
     * Creates a new instance of <code>DocException</code> without detail message.
     */
    public DocException() {
    }


    /**
     * Constructs an instance of <code>DocException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public DocException(String msg) {
        super(msg);
    }
}
