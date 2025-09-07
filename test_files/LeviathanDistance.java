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

    public static Map<String, Object> fixAppleMisspellings(String text, String targetWord, int maxDistance) {
        String[] words = text.split("\\s+");
        boolean[] skip = new boolean[words.length]; // mark words to skip if joined
        List<Integer> fixedIndices = new ArrayList<>(); // to store the starting indices of fixed words

        // Precompute the starting indices of each word
        List<Integer> startIndices = new ArrayList<>();
        int index = 0;
        for (String word : words) {
            startIndices.add(index);
            index += word.length() + 1; // +1 to account for space after each word
        }

        for (int i = 0; i < words.length; i++) {
            if (skip[i]) continue;

            String cleaned = words[i].replaceAll("\\p{Punct}", "").toLowerCase();
            if (levenshteinDistance(cleaned, targetWord) <= maxDistance) {
                words[i] = "apple";
                fixedIndices.add(startIndices.get(i));
                continue;
            }
            
            // Try joining with next word
            if (i < words.length - 1) {
                String joined = cleaned + words[i + 1].replaceAll("\\p{Punct}", "").toLowerCase();
                if (levenshteinDistance(joined, targetWord) <= maxDistance) {
                    words[i] = "apple";
                    words[i + 1] = ""; // remove second part
                    fixedIndices.add(startIndices.get(i));
                    skip[i + 1] = true;
                }
            }
        }

        // Rebuild final text
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(word).append(" ");
            }
        }

        // Return the fixed text and the list of starting indices
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("fixedText", result.toString().trim());
        resultMap.put("fixedIndices", fixedIndices);

        return resultMap;
    }

    public static void main(String[] args) {
        String text = "I love aple and applle and ap ple. APPLE is great! Even appel.";
        Map<String, Object> result = fixAppleMisspellings(text, "apple", 2);
        String fixedText = (String) result.get("fixedText");
        List<Integer> fixedIndices = (List<Integer>) result.get("fixedIndices");

        System.out.println("Fixed text:\n" + fixedText);
        System.out.println("Starting indices of fixed words: " + fixedIndices);
    }
}