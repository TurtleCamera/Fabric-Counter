package com.Counter.config;

import com.Counter.CounterMod;
import com.Counter.utils.UUIDHandler;

import java.util.ArrayList;
import java.util.HashMap;

public class CounterConfig {
    // Maps server IP to another hashmap that maps the emote string to the current count
    public HashMap<String, HashMap<String, Integer>> counters = new HashMap<>();

    // Keeps track of the phrases that we want to keep a counter for
    public ArrayList<String> phrases = new ArrayList<>();

    // If this variable is not null, then this emote will be appended to the end of the sentence
    public String appendPhrase = null;

    // Enables or disables Levenshtein distance autocorrect
    public boolean enableAutocorrect = false;

    // The distance used for Levenshtein distance
    public int maxDistance = 2;    // Default is 2

    // Given a phrase, update the counters in the config and perform error checks
    public void performCountersErrorChecks(String phrase, boolean putPhrase) {
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