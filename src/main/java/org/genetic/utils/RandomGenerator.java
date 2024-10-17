package org.genetic.utils;


import org.genetic.utils.entities.Interval;

import java.util.List;
import java.util.Random;

public class RandomGenerator {
    private static final Random rand = new Random();

    public static int getRandomInt(int min, int max, List<Integer> exclusion) {
        int randInt;
        do {
            randInt = rand.nextInt(max - min) + min;
        } while (exclusion.contains(randInt));
        return randInt;
    }

    public static int getRandomInt(int max, List<Integer> exclusion) {
        return getRandomInt(0, max, exclusion);
    }

    public static int getRandomInt(int max) {
        return rand.nextInt(max);
    }

    public static Interval<Integer> getRandomInterval(int max) {
        int firstIndex;
        int secondIndex;
        do {
            firstIndex = RandomGenerator.getRandomInt(max);
            secondIndex = RandomGenerator.getRandomInt(max, List.of(firstIndex));
        } while (firstIndex >= secondIndex);

        return new Interval<>(firstIndex, secondIndex);
    }

    public static double randomDouble() {
        return rand.nextDouble();
    }
}
