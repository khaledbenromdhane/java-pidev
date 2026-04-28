package com.pidev.tools;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Liste partagée et vérification des mots interdits pour titres, descriptions et commentaires.
 */
public final class BadWordsFilter {

    private static final List<String> BAD_WORDS = Collections.unmodifiableList(Arrays.asList(
            "stupid", "idiot", "toxic", "insult",
            "merde", "putain", "salope", "connard"
    ));

    private BadWordsFilter() {
    }

    public static boolean containsBadWords(String text) {
        return countBadWords(text) > 0;
    }

    public static int countBadWords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        int count = 0;
        String lowerText = text.toLowerCase();
        for (String word : BAD_WORDS) {
            if (lowerText.contains(word)) {
                count++;
            }
        }
        return count;
    }
}
