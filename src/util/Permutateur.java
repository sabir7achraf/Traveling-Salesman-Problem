package util;

import java.util.*;

public class Permutateur {

    public static List<List<String>> permutations(List<String> elements) {
        List<List<String>> result = new ArrayList<>();
        permute(new ArrayList<>(), elements, result);
        return result;
    }

    private static void permute(List<String> prefix, List<String> rest, List<List<String>> result) {
        if (rest.isEmpty()) {
            result.add(new ArrayList<>(prefix));
            return;
        }

        for (int i = 0; i < rest.size(); i++) {
            List<String> newPrefix = new ArrayList<>(prefix);
            newPrefix.add(rest.get(i));

            List<String> newRest = new ArrayList<>(rest);
            newRest.remove(i);

            permute(newPrefix, newRest, result);
        }
    }
}
