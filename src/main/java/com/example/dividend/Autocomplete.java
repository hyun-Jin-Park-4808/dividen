package com.example.dividend;

import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.trie.PatriciaTrie;

public class Autocomplete {

    private Trie trie = new PatriciaTrie();

    public Autocomplete(Trie trie) {
        this.trie = trie;
    }

    public void add(String s) {
        this.trie.put(s, "world");
    }

    public Object get(String s) {
        return this.trie.get(s);
    }
}
