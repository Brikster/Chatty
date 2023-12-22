package ru.brikster.chatty.util;

import lombok.Value;
import lombok.experimental.UtilityClass;
import ru.brikster.chatty.Constants;
import ru.brikster.chatty.config.file.ReplacementsConfig;

import java.util.*;

@UtilityClass
public class GraphUtil {

    @Value
    public static class CycleAnalysisResult {
        Set<String> keysWithCycles;
        List<List<String>> cycles;
    }

    public CycleAnalysisResult analyseReplacementsForCycles(ReplacementsConfig replacementsConfig) {
        Set<String> keysWithCycles = new HashSet<>();
        List<List<String>> cycles = new ArrayList<>();

        var replacements = replacementsConfig.getReplacements();

        Set<String> checked = new HashSet<>();
        for (var entry : replacements.entrySet()) {
            if (checked.contains(entry.getKey())) continue;

            Deque<String> queue = new LinkedList<>();
            queue.add(entry.getKey());

            Set<String> parents = new HashSet<>();
            Deque<String> cycle = new LinkedList<>();

            while (!queue.isEmpty()) {
                String currentKey = queue.pollLast();

                // Remove level
                if (currentKey == null) {
                    String leafKey = cycle.removeLast();
                    parents.remove(leafKey);
                }

                String currentValue = replacements.get(currentKey);
                if (currentValue == null) continue;

                cycle.add(currentKey);

                if (parents.contains(currentKey)) {
                    keysWithCycles.addAll(cycle);
                    cycles.add(new ArrayList<>(cycle));
                    continue;
                }

                parents.add(currentKey);
                checked.add(currentKey);

                // Add labels for levels
                queue.addLast(null);

                var matcher = Constants.REPLACEMENTS_PATTERN.matcher(currentValue);
                while (matcher.find()) {
                    String key = matcher.group().substring(3, matcher.group().length() - 1);
                    queue.addLast(key);
                }
            }
        }

        return new CycleAnalysisResult(keysWithCycles, cycles);
    }

}
