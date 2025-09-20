import java.util.*;

public class LeviathanDistance {
    // Calculates the Levenshtein distance (edit distance) between two strings.
    // This measures how many single-character edits (insert, delete, substitute)
    // are needed to turn string 'a' into string 'b'.
    public static int levenshteinDistance(String a, String b) {
        // Create a DP (dynamic programming) table with dimensions (a.length+1) x (b.length+1)
        int[][] dp = new int[a.length() + 1][b.length() + 1];

        // Fill the table
        for (int i = 0; i <= a.length(); i++) {
            for (int j = 0; j <= b.length(); j++) {
                // Base case: if one string is empty, distance = length of the other string
                if (i == 0) {
                    dp[i][j] = j;
                }
                else if (j == 0) {
                    dp[i][j] = i;
                }
                else {
                    // If characters match, cost = 0; otherwise substitution cost = 1
                    int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;

                    // Take the minimum of:
                    // 1. Deletion: dp[i-1][j] + 1
                    // 2. Insertion: dp[i][j-1] + 1
                    // 3. Substitution: dp[i-1][j-1] + cost
                    dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1,
                                 dp[i][j - 1] + 1),
                                 dp[i - 1][j - 1] + cost);
                }
            }
        }

        // The bottom-right cell contains the edit distance between full strings
        return dp[a.length()][b.length()];
    }

    // Corrects misspellings of a target word in a given text,
    // based on a maximum allowed Levenshtein distance.
    // Returns both the corrected text and the indices where corrections were applied.
    public static Map<String, Object> fixMisspellings(String text, String targetWord, int maxDistance) {
        // Split text into words (by whitespace)
        String[] words = text.split("\\s+");
        boolean[] skip = new boolean[words.length]; // Marks words that should be skipped (if merged)

        // Correct misspellings in place
        for (int i = 0; i < words.length; i++) {
            // Skip words already handled in a merge
            if (skip[i]) {
                continue;
            }

            // Remove punctuation and lowercase for comparison
            String cleaned = words[i].replaceAll("\\p{Punct}", "").toLowerCase();

            // If the word is within maxDistance of targetWord, replace it
            if (levenshteinDistance(cleaned, targetWord) <= maxDistance) {
                words[i] = targetWord;
                continue;
            }

            // If not corrected, try joining with the next word
            if (i < words.length - 1) {
                String joined = cleaned + words[i + 1].replaceAll("\\p{Punct}", "").toLowerCase();
                if (levenshteinDistance(joined, targetWord) <= maxDistance) {
                    words[i] = targetWord;     // Replace with correct word
                    words[i + 1] = "";         // Remove second part
                    skip[i + 1] = true;        // Mark as skipped
                }
            }
        }

        // Rebuild final text and compute fixed indices in the new string
        StringBuilder result = new StringBuilder();
        List<Integer> fixedIndices = new ArrayList<>(); // Stores start indices of corrections
        int currentIndex = 0;   // Tracks character position in rebuilt string
        String prevWord = null;

        for (String word : words) {
            if (!word.isEmpty()) {
                String finalWord = word;

                // Capitalize if at sentence start
                if (word.equals(targetWord)) {
                    // Capitalize if it’s the first word in the sentence
                    boolean isSentenceStart = (prevWord == null) ||
                            prevWord.matches(".*[.!?]$");
                    if (isSentenceStart) {
                        finalWord = targetWord.substring(0, 1).toUpperCase() + targetWord.substring(1);
                    }
                    fixedIndices.add(currentIndex); // Record index of correction
                }

                result.append(finalWord).append(" ");
                currentIndex += finalWord.length() + 1; // word + space
                prevWord = finalWord;
            }
        }

        // Package results into a map
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("fixedText", result.toString().trim());   // Final cleaned text
        resultMap.put("fixedIndices", fixedIndices);            // List of correction indices

        return resultMap;
    }

    public static void main(String[] args) {
        String text = "I love aple and applle and ap ple. APPLE is great! Even appel.";
        // String text = "l el. So much l el. Lel X2, he sux. Le ll. lell. lells. lellz. lellzz. lellzzz.";
        Map<String, Object> result = fixMisspellings(text, "apple", 2);
        String fixedText = (String) result.get("fixedText");
        List<Integer> fixedIndices = (List<Integer>) result.get("fixedIndices");

        System.out.println("Fixed text:\n" + fixedText);
        System.out.println("Starting indices of fixed words: " + fixedIndices);
    }
}