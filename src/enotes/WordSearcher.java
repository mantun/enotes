/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package enotes;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import javax.swing.text.*;

/**
 *
 * @author ivoras
 */
class WordSearcher {

    public WordSearcher(JTextComponent comp) {
        this.comp = comp;
        this.painter = new UnderlineHighlighter.UnderlineHighlightPainter(Color.red);
    }

    // Search for a word and return the offset of the
    // first occurrence. Highlights are added for all
    // occurrences found.
    public int search(String word, int caretPosition) {

        removeHighlights();

        if (word == null || word.equals("")) {
            return -1;
        }
        
        // Look for the word we are given - insensitive search
        String content;
        try {
            Document d = comp.getDocument();
            content = d.getText(0, d.getLength()).toLowerCase();
        } catch (BadLocationException e) {
            // Cannot happen
            return -1;
        }

        word = word.toLowerCase();
        highlight(word, caretPosition, content);

        int pos = content.indexOf(word, caretPosition);
        if (pos == caretPosition) {
            pos = content.indexOf(word, caretPosition + 1);
        }
        return pos;
    }

    private void highlight(String word, int caretPosition, String content) {
        int lastIndex = caretPosition;
        int wordSize = word.length();

        Highlighter highlighter = comp.getHighlighter();
        while ((lastIndex = content.indexOf(word, lastIndex)) != -1) {
            int endIndex = lastIndex + wordSize;
            try {
                highlighter.addHighlight(lastIndex, endIndex, painter);
            } catch (BadLocationException ignored) {
                // Nothing to do
            }
            lastIndex = endIndex;
        }
    }

    public void removeHighlights() {
        // Remove any existing highlights for last word
        Highlighter highlighter = comp.getHighlighter();
        Highlighter.Highlight[] highlights = highlighter.getHighlights();
        for (int i = 0; i < highlights.length; i++) {
            Highlighter.Highlight h = highlights[i];
            if (h.getPainter() instanceof UnderlineHighlighter.UnderlineHighlightPainter) {
                highlighter.removeHighlight(h);
            }
        }
    }

    protected JTextComponent comp;
    protected Highlighter.HighlightPainter painter;
}

