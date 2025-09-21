package com.Counter.config;

import com.Counter.CounterMod;
import com.Counter.utils.Tuple;
import com.Counter.utils.UUIDHandler;

import java.util.*;

public class CounterConfig {
    // Maps server IP to another hashmap that maps the emote string to the current count
    public HashMap<String, HashMap<String, Integer>> counters = new HashMap<>();

    // Keeps track of the phrases that we want to keep a counter for
    public ArrayList<String> phrases = new ArrayList<>();

    // Keeps track of shortcuts
    public ArrayList<Tuple<String, String>> shortcuts = new ArrayList<>();

    // If this variable is not null, then this emote will be appended to the end of the sentence
    public String appendPhrase = null;

    // Enables or disables Levenshtein distance autocorrect
    public boolean enableAutocorrect = false;

    // The distance used for Levenshtein distance
    public int maxDistance = 1;    // Default is 1

    // Used when initializing the mod. Will check for any invalid settings.
    public boolean checkErrors() {
        // Variable to track if we found an error
        boolean hasErrors = false;

        // Is the Levenshtein distance a valid value?
        if (maxDistance <= 0) {
            // Set it back to the default value
            System.err.println("Error: Max Levenshtein distance was a negative value. Setting it back to the default value of 2.");
            maxDistance = 2;
            hasErrors = true;
        }

        // Is the append phrase untracked?
        if (appendPhrase != null && !phrases.contains(appendPhrase)) {
            // Set the append phrase to null
            System.err.println("Error: The append phrase was untracked, so it has been removed.");
            appendPhrase = null;
            hasErrors = true;
        }

        // Shortcuts shouldn't have any null elements
        boolean hasInvalid = false;
        for (int i = shortcuts.size() - 1; i >= 0; i--) {
            Tuple<String, String> tuple = shortcuts.get(i);

            // Cases that should be thrown out
            if (tuple == null) {
                // Is this tuple null?
                hasInvalid = true;
                hasErrors = true;
                shortcuts.remove(i);
            }
            else if (tuple.first() == null || tuple.second() == null) {
                // Is either entry null?
                hasInvalid = true;
                hasErrors = true;
                shortcuts.remove(i);
            }
            else if (tuple.first().isEmpty() || tuple.second().isEmpty()) {
                // Is either empty?
                hasInvalid = true;
                hasErrors = true;
                shortcuts.remove(i);
            }
        }
        if (hasInvalid) {
            System.err.println("Error: There were invalid shortcut entries. They have been removed.");
        }

        // Shortcuts list also should have any duplicate shortcuts (duplicate phrases are fine)
        Set<String> seen = new HashSet<>();
        hasInvalid = false;
        Iterator<Tuple<String, String>> iterator = shortcuts.iterator();
        while(iterator.hasNext()) {
            // Get the new tuple
            Tuple<String, String> tuple = iterator.next();

            // Are we already tracking this shortcut?
            if (!seen.add(tuple.second())) {
                // Remove this tuple
                iterator.remove();
                hasInvalid = true;
                hasErrors = true;
            }
        }
        if (hasInvalid) {
            System.err.println("Error: There were duplicate shortcuts. Only the first shortcut in the list was kept.");
        }

        // There should be no duplicate phrases tracked
        Set<String> set = new LinkedHashSet<>(phrases);
        int oldPhraseCount = phrases.size();
        phrases.clear();
        phrases.addAll(set);
        int newPhraseCount = phrases.size();
        if (oldPhraseCount != newPhraseCount) {
            // Remove all duplicates
            System.err.println("Error: There were duplicate tracked phrases. They have been removed.");
            hasErrors = true;
        }

        // There shouldn't be any null or empty strings in phrases
        if (phrases.remove(null)) {
            // Remove all null and empty strings
            System.err.println("Error: There was a null string in the tracked phrases. It has been removed.");
            hasErrors = true;
        }
        if (phrases.remove("")) {
            // Remove all null and empty strings
            System.err.println("Error: There was an empty string in the tracked phrases. It has been removed.");
            hasErrors = true;
        }

        // Are all the counters valid?
        hasInvalid = false;
        // Loop through all servers (and the single player uuid if it exists)
        for (String uuid : counters.keySet()) {
            // Loop through all the counters for this server or single player
            for (String phrase : counters.get(uuid).keySet()) {
                // Is this an invalid counter?
                if (counters.get(uuid).get(phrase) < 0) {
                    // Note the invalid integer and fix it
                    hasInvalid = true;
                    hasErrors = true;
                    counters.get(uuid).put(phrase, 0);
                }
            }
        }
        if (hasInvalid) {
            System.err.println("Error: There were invalid counters stored. They have been set to 0.");
        }

        return hasErrors;
    }

    // Given a phrase, update the counters in the config and perform error checks
    public void performCountersChecks(String phrase, boolean putPhrase) {
        // Get the UUID
        String uuid = UUIDHandler.getUUID();

        // This should not happen, but check if there even is a hashmap of servers
        if (CounterMod.configManager.getConfig().counters == null) {
            CounterMod.configManager.getConfig().counters = new HashMap<>();
        }

        // Are we already tracking this server (or single player)?
        if (!CounterMod.configManager.getConfig().counters.containsKey(uuid)) {
            // If not, create a new hashmap to track counters
            CounterMod.configManager.getConfig().counters.put(uuid, new HashMap<>());
        }

        // Don't execute the rest of the code if we don't need to put this phrase in the counters
        if (!putPhrase) {
            return;
        }

        // Are we already tracking counters for this phrase on this server?
        if (!CounterMod.configManager.getConfig().counters.get(uuid).containsKey(phrase)) {
            CounterMod.configManager.getConfig().counters.get(uuid).put(phrase, 0);
        }

        // Should not happen, but if the counter is negative, set it to 0
        if(CounterMod.configManager.getConfig().counters.get(uuid).get(phrase) < 0) {
            CounterMod.configManager.getConfig().counters.get(uuid).put(phrase, 0);
        }
    }
}