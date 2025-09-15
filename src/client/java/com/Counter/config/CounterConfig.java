package com.Counter.config;

import java.util.ArrayList;
import java.util.HashMap;

public class CounterConfig {
    // Maps server IP to another hashmap that maps the emote string to the current count
    public HashMap<String, HashMap<String, Integer>> counters = new HashMap<>();

    // Keeps track of the phrases that we want to keep a counter for
    public ArrayList<String> phrases = new ArrayList<>();

    // If this variable is not null, then this emote will be appended to the end of the sentence
    public String appendEmote = null;

    // Enables or disables all the features of this mod
    public boolean enable = false;
}