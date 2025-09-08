import java.util.*;

public class LeviathanDistance {

    public static int levenshteinDistance(String a, String b) {
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

    public static Map<String, Object> fixMisspellings(String text, String targetWord, int maxDistance) {
        String[] words = text.split("\\s+");
        boolean[] skip = new boolean[words.length]; // Mark words to skip if joined

        // Step 1: Correct misspellings in place
        for (int i = 0; i < words.length; i++) {
            if (skip[i]) continue;

            String cleaned = words[i].replaceAll("\\p{Punct}", "").toLowerCase();
            if (levenshteinDistance(cleaned, targetWord) <= maxDistance) {
                words[i] = targetWord;
                continue;
            }

            // Try joining with next word
            if (i < words.length - 1) {
                String joined = cleaned + words[i + 1].replaceAll("\\p{Punct}", "").toLowerCase();
                if (levenshteinDistance(joined, targetWord) <= maxDistance) {
                    words[i] = targetWord;
                    words[i + 1] = ""; // remove second part
                    skip[i + 1] = true;
                }
            }
        }

        // Step 2: Rebuild final text and compute fixed indices in the new string
        StringBuilder result = new StringBuilder();
        List<Integer> fixedIndices = new ArrayList<>();
        int currentIndex = 0;

        for (String word : words) {
            if (!word.isEmpty()) {
                if (word.equals(targetWord)) {
                    fixedIndices.add(currentIndex);
                }
                result.append(word).append(" ");
                currentIndex += word.length() + 1; // word + space
            }
        }

        // Step 3: Return results
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("fixedText", result.toString().trim());
        resultMap.put("fixedIndices", fixedIndices);

        return resultMap;
    }

    public static void main(String[] args) {
        // String text = "I love aple and applle and ap ple. APPLE is great! Even appel.";
        String text = "So much l el. Lel X2, he sux. Le ll. lell. lells. lellz. lellzz. lellzzz.";
        Map<String, Object> result = fixMisspellings(text, "lel", 2);
        String fixedText = (String) result.get("fixedText");
        List<Integer> fixedIndices = (List<Integer>) result.get("fixedIndices");

        System.out.println("Fixed text:\n" + fixedText);
        System.out.println("Starting indices of fixed words: " + fixedIndices);
    }
}