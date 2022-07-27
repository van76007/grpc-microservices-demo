package com.jeeconf.grpcdemo.providers;

import com.jeeconf.grpcdemo.Speed;
import com.jeeconf.grpcdemo.Wind;

import java.util.Random;
import java.util.function.Supplier;
import java.util.concurrent.TimeUnit;

/**
 * Randomly generates {@link Wind}.
 */
public class RandomWindProvider implements Supplier<Wind> {
    private static final long DELAY_MILLIS = 3;
    private final Random random = new Random();

    @Override
    public Wind get() {
        float direction = random.nextInt(359) + random.nextFloat();
        float speedValue = random.nextInt(70) + random.nextFloat();
        Speed speed = Speed.newBuilder().setValue(speedValue).setUnits(Speed.Units.KMH).build();
        simulateWork(DELAY_MILLIS);

        return Wind.newBuilder()
                .setDirection(direction)
                .setSpeed(speed)
                .build();
    }

    private void simulateWork(long millis) {
        try {
            TimeUnit.MILLISECONDS.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
