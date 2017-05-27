/*
 * Copyright (c) 2009-2014 Ivan Voras <ivoras@fer.hr>
 * Copyright (c) 2017-2017 github.com/mantun
 * Released under the 2-clause BSDL.
 */

package enotes;

import enotes.doc.Searcher;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import java.awt.*;

public class WordSearcher {

    public WordSearcher(JTextComponent comp, String word, int caretPosition) {
        this.comp = comp;
        this.allMatchesPainter = new UnderlineHighlightPainter(Color.orange);
        this.currentMatchPainter = new UnderlineHighlightPainter(Color.red);

        // Look for the word we are given - insensitive search
        Document d = comp.getDocument();
        String content;
        try {
            content = d.getText(0, d.getLength()).toLowerCase();
        } catch (BadLocationException e) {
            throw new IllegalStateException(e);
        }
        Highlighter highlighter = comp.getHighlighter();
        searcher = new Searcher(word.toLowerCase(), content.toLowerCase(), caretPosition, new Searcher.Highlighter() {
            @Override
            public void clearHighlights() {
                highlighter.removeAllHighlights();
            }

            @Override
            public void addHighlight(int start, int end, boolean isCurrent) {
                try {
                    if (isCurrent) {
                        highlighter.addHighlight(start, end, currentMatchPainter);
                    } else {
                        highlighter.addHighlight(start, end, allMatchesPainter);
                    }
                } catch (BadLocationException ignored) {
                    // ignored
                }
            }
        });

    }

    public void removeHighlights() {
        comp.getHighlighter().removeAllHighlights();
    }

    public Searcher getSearcher() {
        return searcher;
    }

    private JTextComponent comp;
    private Searcher searcher;

    private Highlighter.HighlightPainter allMatchesPainter;
    private Highlighter.HighlightPainter currentMatchPainter;
}

