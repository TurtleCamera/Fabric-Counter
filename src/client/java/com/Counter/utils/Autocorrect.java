package com.Counter.utils;

import com.Counter.CounterMod;

import java.util.*;
import java.util.regex.Pattern;

public class Autocorrect {
     // Replaces all occurrences of a given phrase in the text where its letters
     // may be separated by spaces, restoring it to the compact phrase.
     // Example: "h e l l o" → "hello"
    public static String closePhrase(String text, String phrase) {
        // Build regex like: h\s*e\s*l\s*l\s*o
        StringBuilder regexBuilder = new StringBuilder();
        for (int i = 0; i < phrase.length(); i++) {
            char c = phrase.charAt(i);
            regexBuilder.append(Pattern.quote(String.valueOf(c)));
            if (i < phrase.length() - 1) {
                regexBuilder.append("\\s*"); // allow any spaces between letters
            }
        }

        String regex = regexBuilder.toString();

        // Replace any spaced-out phrase with the compact version
        return text.replaceAll(regex, phrase);
    }

    // Calculates the Levenshtein distance (edit distance) between two strings
    private static int levenshteinDistance(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); i++) {
            for (int j = 0; j <= b.length(); j++) {
                if (i == 0) dp[i][j] = j;
                else if (j == 0) dp[i][j] = i;
                else {
                    int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                    dp[i][j] = Math.min(
                            Math.min(dp[i - 1][j] + 1,
                                    dp[i][j - 1] + 1),
                            dp[i - 1][j - 1] + cost);
                }
            }
        }
        return dp[a.length()][b.length()];
    }

    public static Map<String, Object> fixMisspellings(String text, String phrase) {
        // First, close any gaps that appear among the phrases
        text = closePhrase(text, phrase);

        String[] words = text.split("\\s+");

        // Cleaned version of target phrase for comparison
        String cleanedPhrase = phrase.replaceAll("\\p{Punct}", "").toLowerCase();

        // Max edit distance
        int maxDistance = CounterMod.configManager.getConfig().maxDistance;

        // Step 1: Correct individual words
        for (int i = 0; i < words.length; i++) {
            String cleanedWord = words[i].replaceAll("\\p{Punct}", "").toLowerCase();
            if (levenshteinDistance(cleanedWord, cleanedPhrase) <= maxDistance) {
                // Preserve original punctuation only if not already at the end
                String endPunct = "";
                if (!words[i].isEmpty() && words[i].matches(".*\\p{Punct}$")) {
                    endPunct = words[i].substring(words[i].length() - 1);
                    if (phrase.endsWith(endPunct)) {
                        endPunct = ""; // already included in the phrase
                    }
                }
                words[i] = phrase + endPunct;
            }
        }

        // Step 2: Remove consecutive duplicate words (ignoring punctuation)
        Pattern pattern = Pattern.compile("\\p{Punct}");
        List<String> cleanedWords = new ArrayList<>();
        String prevWordCleaned = null;
        for (String word : words) {
            if (!word.isEmpty()) {
                String cleanedWord = word.replaceAll("\\p{Punct}", "").toLowerCase();
                if (!cleanedWord.equals(prevWordCleaned)) {
                    cleanedWords.add(word);
                }
                else if(pattern.matcher(word).find() || pattern.matcher(prevWordCleaned).find()) {
                    cleanedWords.add(word);
                }
                prevWordCleaned = cleanedWord;
            }
        }

        // Step 3: Rebuild final text and compute fixed indices
        StringBuilder result = new StringBuilder();
        List<Integer> fixedIndices = new ArrayList<>();
        int currentIndex = 0;
        String prevWord = null;

        for (String word : cleanedWords) {
            String finalWord = word;

            // Capitalize if at sentence start
            boolean isSentenceStart = (prevWord == null) || prevWord.matches(".*[.!?]$");
            if (word.replaceAll("\\p{Punct}", "").equalsIgnoreCase(cleanedPhrase) && isSentenceStart) {
                finalWord = phrase.substring(0, 1).toUpperCase() + phrase.substring(1)
                        + word.substring(phrase.length());
            }

            // Record index if word was corrected
            if (word.replaceAll("\\p{Punct}", "").equalsIgnoreCase(cleanedPhrase)) {
                fixedIndices.add(currentIndex);
            }

            result.append(finalWord).append(" ");
            currentIndex += finalWord.length() + 1;
            prevWord = finalWord;
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("fixedText", result.toString().trim());
        resultMap.put("fixedIndices", fixedIndices);

        return resultMap;
    }


    // Finds the starting indices of all instances of a phrase (this is used if autocorrect is turned off)
    public static List<Integer> findPhraseIndices(String text, String phrase) {
        List<Integer> indices = new ArrayList<>();
        int index = text.indexOf(phrase);

        while (index >= 0) {
            indices.add(index);
            index = text.indexOf(phrase, index + 1);
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
