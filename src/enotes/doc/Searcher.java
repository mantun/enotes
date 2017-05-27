/*
 * Copyright (c) 2009-2014 Ivan Voras <ivoras@fer.hr>
 * Copyright (c) 2017-2017 github.com/mantun
 * Released under the 2-clause BSDL.
 */

package enotes.doc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Searcher {
    private String word;
    private int currentMatch;
    private final List<Integer> matches;
    private final int caretPosition;
    private Highlighter highlighter;

    public String getWord() {
        return word;
    }

    public Searcher(String word, String content, int caretPosition, Highlighter highlighter) {
        if (word == null || word.isEmpty()) {
            throw new IllegalArgumentException(word);
        }
        this.word = word;

        matches = new ArrayList<>();
        int lastIndex = 0;
        while ((lastIndex = content.indexOf(word, lastIndex)) != -1) {
            matches.add(lastIndex);
            lastIndex = lastIndex + word.length();
        }
        currentMatch = -1;

        this.caretPosition = caretPosition;
        this.highlighter = highlighter;
    }

    public int findNext() {
        if (currentMatch < 0) {
            currentMatch = Collections.binarySearch(matches, caretPosition);
            if (currentMatch < 0) {
                currentMatch = -currentMatch - 1;
            }
        } else if (currentMatch < matches.size()) {
            currentMatch++;
        } else if (!matches.isEmpty()) {
            currentMatch = 0;
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
        highlighter.clearHighlights();
        for (int i = 0; i < matches.size(); i++) {
            Integer pos = matches.get(i);
            highlighter.addHighlight(pos, pos + word.length(), i == currentMatch);
        }
    }

    public interface Highlighter {
        void clearHighlights();
        void addHighlight(int start, int end, boolean isCurrent);
    }
}
