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
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        String lowerText = text.toLowerCase();
        for (String word : BAD_WORDS) {
            if (lowerText.contains(word)) {
                return true;
            }
        }
        return false;
    }
}
