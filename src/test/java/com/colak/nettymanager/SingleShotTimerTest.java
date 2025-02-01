package com.colak.nettymanager;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SingleShotTimerTest {

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

        SingleShotTimerParameters parameters = new SingleShotTimerParameters(countDownLatch::countDown, 0);
        nettyManager.scheduleSingleShotTimer(parameters);

        boolean taskExecuted = countDownLatch.await(2, TimeUnit.SECONDS);
        assertTrue(taskExecuted, "Timer task was not executed within the expected time");
    }

}