/*
 * Copyright (c) 2009-2014 Ivan Voras <ivoras@fer.hr>
 * Copyright (c) 2017-2017 github.com/mantun
 * Released under the 2-clause BSDL.
 */

package enotes;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WordSearcher {

    public WordSearcher(JTextComponent comp, String word, int caretPosition) {
        this.comp = comp;
        this.allMatchesPainter = new UnderlineHighlightPainter(Color.orange);
        this.currentMatchPainter = new UnderlineHighlightPainter(Color.red);

        if (word == null || word.isEmpty()) {
            throw new IllegalArgumentException(word);
        }
        this.word = word.toLowerCase();

        // Look for the word we are given - insensitive search
        Document d = comp.getDocument();
        String content;
        try {
            content = d.getText(0, d.getLength()).toLowerCase();
        } catch (BadLocationException e) {
            throw new IllegalStateException(e);
        }

        matches = new ArrayList<>();
        int lastIndex = 0;
        while ((lastIndex = content.indexOf(word, lastIndex)) != -1) {
            matches.add(lastIndex);
            lastIndex = lastIndex + word.length();
        }
        currentMatch = -1;
        this.caretPosition = caretPosition;
    }

    public int findNext() {
        if (currentMatch < 0) {
            currentMatch = Collections.binarySearch(matches, caretPosition);
            if (currentMatch < 0) {
                currentMatch = -currentMatch - 1;
            }
        } else if (currentMatch < matches.size()) {
            currentMatch++;
        }
        highlight();
        return currentMatch >= matches.size() ? -1 : matches.get(currentMatch);
    }

    public int findPrev() {
        if (currentMatch < 0) {
            currentMatch = Collections.binarySearch(matches, caretPosition);
            if (currentMatch < 0) {
                currentMatch = -currentMatch - 2;
            }
        } else if (currentMatch >= 0) {
            currentMatch--;
        }
        highlight();
        return currentMatch < 0 ? -1 : matches.get(currentMatch);
    }

    private void highlight() {
        Highlighter highlighter = comp.getHighlighter();
        highlighter.removeAllHighlights();
        for (int i = 0; i < matches.size(); i++) {
            Integer pos = matches.get(i);
            try {
                if (i == currentMatch) {
                    highlighter.addHighlight(pos, pos + word.length(), currentMatchPainter);
                } else {
                    highlighter.addHighlight(pos, pos + word.length(), allMatchesPainter);
                }
            } catch (BadLocationException ignored) {
                // ignored
            }
        }
    }

    public void removeHighlights() {
        comp.getHighlighter().removeAllHighlights();
    }

    public String getWord() {
        return word;
    }

    protected JTextComponent comp;
    private String word;
    private int currentMatch;
    private final List<Integer> matches;
    private final int caretPosition;

    protected Highlighter.HighlightPainter allMatchesPainter;
    protected Highlighter.HighlightPainter currentMatchPainter;
}

