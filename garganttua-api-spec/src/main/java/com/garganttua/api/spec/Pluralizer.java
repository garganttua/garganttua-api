package com.garganttua.api.spec;

public class Pluralizer {
	
    public static String toPlural(String word) {
        if (word == null || word.isEmpty()) {
            return word;
        }
        
        if (word.endsWith("y")) {
            if (word.length() > 1 && !isVowel(word.charAt(word.length() - 2))) {
                return word.substring(0, word.length() - 1) + "ies";
            }
        } else if (word.endsWith("s") || word.endsWith("sh") || word.endsWith("ch") || word.endsWith("x") || word.endsWith("z")) {
            return word + "es";
        } else {
            return word + "s";
        }

        return word + "s";
    }

    private static boolean isVowel(char c) {
        return "AEIOUaeiou".indexOf(c) != -1;
    }
}
