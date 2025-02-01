package com.colak.nettymanager;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class FixedRateTimerTest {

    private static NettyManager nettyManager;

    @BeforeAll
    static void setup() {
        nettyManager = new NettyManager();
    }

    @AfterAll
    static void tearDown() {
        nettyManager.shutdown();
    }

    @Test
    void testTimer() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);

        String timerId = "timer1";
        FixedRateTimerParameters parameters = new FixedRateTimerParameters(timerId, countDownLatch::countDown, 0, 1000);
        nettyManager.scheduleFixedRateTimer(parameters);

        boolean taskExecuted = countDownLatch.await(2, TimeUnit.SECONDS);
        assertTrue(taskExecuted, "Timer task was not executed within the expected time");

        nettyManager.stopTimer(timerId);
    }

}