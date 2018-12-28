package com.melodigm.post.util;

import java.util.Random;

public class RandomUtil {
    public static int getRandom(int minValue, int maxValue) {
        Random random = new Random();
        int genValue = random.nextInt(maxValue - minValue);
        genValue += minValue;

        return genValue;
    }
}
