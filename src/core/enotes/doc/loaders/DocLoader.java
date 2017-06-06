/*
 * Copyright (c) 2009-2014 Ivan Voras <ivoras@fer.hr>
 * Copyright (c) 2017-2017 github.com/mantun
 * Released under the 2-clause BSDL.
 */

package enotes.doc.loaders;

import enotes.doc.Doc;
import enotes.doc.DocException;

import java.io.IOException;
import java.io.InputStream;

public interface DocLoader {
    Doc load(InputStream bin, String pwd) throws IOException, DocException;
}
