package com.garganttua.api.spec;

public class Singularizer {
    public static String toSingular(String word) {
        if (word == null || word.isEmpty()) {
            return word;
        }

        if (word.endsWith("ies")) {
            return word.substring(0, word.length() - 3) + "y";
        } else if (word.endsWith("es")) {
            String baseWord = word.substring(0, word.length() - 2);
            if (baseWord.endsWith("s") || baseWord.endsWith("sh") || baseWord.endsWith("ch") || baseWord.endsWith("x") || baseWord.endsWith("z")) {
                return baseWord;
            }
        } else if (word.endsWith("s")) {
            return word.substring(0, word.length() - 1);
        }

        return word;
    }
}
