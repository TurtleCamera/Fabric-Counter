package com.Counter.utils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhraseUtils {
    // Replaces all instances of the phrase with the shortcut if the characters
    // surrounding the phrase are punctuation or spaces.
    public static String replaceShortcut(String text, String phrase, String shortcut) {
        if (text == null || phrase == null || shortcut == null) return text;

        // Escape shortcut for regex
        String escapedShortcut = Pattern.quote(shortcut);

        // Regex: match beginning/end, space, or punctuation
        String regex = "(?<=^|\\s|\\p{Punct})" + escapedShortcut + "(?=$|\\s|\\p{Punct})";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);

        return matcher.replaceAll(Matcher.quoteReplacement(phrase));
    }

    // Finds the starting indices of all instances of a phrase (this is used if autocorrect is turned off)
    public static List<Integer> findPhraseIndices(String text, String phrase) {
        List<Integer> indices = new ArrayList<>();
        if (text == null || phrase == null || phrase.isEmpty()) return indices;

        String lowerText = text.toLowerCase();
        String lowerPhrase = phrase.toLowerCase();
        int index = lowerText.indexOf(lowerPhrase);

        while (index >= 0) {
            indices.add(index);
            index = lowerText.indexOf(lowerPhrase, index + 1);
        }

        return indices;
    }

    // Gets the starting index of trailing punctuation
    public static int trailingPunctuationStart(String content) {
        if (content == null || content.isEmpty()) {
            return -1;
        }

        // Walk backwards over punctuation
        for (int i = content.length () - 1; i >= 0; i--) {
            String suffix = content.substring(i);
            if (isAllPunctuation(suffix)) {
                return i;
            }
        }

        // No trailing punctuation, so return the end of the string
        return -1;
    }

    // Finds the index after the last instance of the phrase in String content. If this
    // phrase doesn't exist in the content, then returns -1 instead.
    private static int indexAfterLast(String content, String phrase) {
        if (content == null || phrase == null || phrase.isEmpty()) {
            return -1;
        }

        // Does this phrase even exist in content?
        int lastIndex = content.lastIndexOf(phrase);
        if (lastIndex == -1) {
            return -1;
        }

        // Return the index after the phrase
        return lastIndex + phrase.length();
    }

    // Checks if the string contains only punctuation
    public static boolean isAllPunctuation(String text) {
        if (text == null) {
            return true;
        }

        if (text.isEmpty()) {
            return true;
        }

        if (text.matches("\\p{Punct}+")) {
            return true;
        }

        return false;
    }
}
