package dam.passwordPolicy;

import java.util.ArrayList;
import java.util.function.Predicate;

public class PasswordPolicy {

    private static final double SIMILARITY_THRESHOLD = 0.75;

    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 20;

    private static final String SPECIAL_CHARACTERS = "&+_*";

    private static final Predicate<String> FT_NN = (s) -> s != null;
    private static final Predicate<String> FT_MIN_L = (s) -> s.length() >= MIN_LENGTH;
    private static final Predicate<String> FT_MAX_L = (s) -> s.length() <= MAX_LENGTH;

    private final ArrayList<Predicate<String>> tests;
    private final ArrayList<String> distinctStrings;
    private final ArrayList<String> containsAtLeast;

    public PasswordPolicy() {
        tests = new ArrayList<Predicate<String>>();
        distinctStrings = new ArrayList<String>();
        containsAtLeast = new ArrayList<String>();

        addDefaultTests();
    }

    private void addDefaultTests() {
        tests.add(FT_NN);
        tests.add(FT_MIN_L);
        tests.add(FT_MAX_L);
        containsAtLeast.add(SPECIAL_CHARACTERS);
        containsAtLeast.add("1234567890");
        containsAtLeast.add("abcdefghijklmnopqrstuvwxyzáéíóúäëïöü");
        containsAtLeast.add("ABCDEFGHIJKLMNOPQRSTUVWXYZÁÉÍÓÚÄËÏÖÜ");
    }

    public boolean isValid(String password) {
        for (int i = 0; i < tests.size(); i++) {
            if (!tests.get(i).test(password)) {
                return false;
            }
        }

        for (int i = 0; i < distinctStrings.size(); i++) {
            if (stringsAlike(password, distinctStrings.get(i))) {
                return false;
            }
        }

        for (int i = 0; i < containsAtLeast.size(); i++) {
            if (!password.contains(containsAtLeast.get(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean stringsAlike(String s1, String s2) {
        return similarity(s1, s2) > SIMILARITY_THRESHOLD;
    }

    private static double similarity(String s1, String s2) {
        String longer = s1, shorter = s2;
        if (s1.length() < s2.length()) { // longer should always have greater length
            longer = s2;
            shorter = s1;
        }
        int longerLength = longer.length();
        if (longerLength == 0)
            return 1.0; // Equal legth
        return (longerLength - levenshteinDist(longer, shorter)) / (double) longerLength;
    }

    /**
     * http://rosettacode.org/wiki/Levenshtein_distance#Java
     */
    private static int levenshteinDist(String s1, String s2) {
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();

        int[] costs = new int[s2.length() + 1];
        for (int i = 0, j, lastValue, newValue; i <= s1.length(); i++) {
            lastValue = i;
            for (j = 0; j <= s2.length(); j++) {
                if (i == 0)
                    costs[j] = j;
                else {
                    if (j > 0) {
                        newValue = costs[j - 1];
                        if (s1.charAt(i - 1) != s2.charAt(j - 1))
                            newValue = Math.min(Math.min(newValue, lastValue), costs[j]) + 1;
                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
            }
            if (i > 0)
                costs[s2.length()] = lastValue;
        }
        return costs[s2.length()];
    }
}