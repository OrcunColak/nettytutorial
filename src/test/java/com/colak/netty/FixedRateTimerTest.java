package com.colak.netty;

import com.colak.netty.timerparams.FixedRateTimerParameters;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class FixedRateTimerTest {

    private static NettyManager nettyManager;

    @BeforeAll
    static void setup() {
        NettyManagerParameters parameters = new NettyManagerParameters();
        nettyManager = new NettyManager(parameters);
    }

    @AfterAll
    static void tearDown() {
        nettyManager.shutdown();
    }

    @Test
    void testTimer() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);

        String timerId = "timer1";
        FixedRateTimerParameters parameters = new FixedRateTimerParameters(timerId, () -> {

            // Run by netty-worker-3-1
            log.info("Fixed rate timer expired");
            countDownLatch.countDown();
        }, 0, 1000);
        nettyManager.scheduleFixedRateTimer(parameters);

        boolean taskExecuted = countDownLatch.await(2, TimeUnit.SECONDS);
        assertTrue(taskExecuted, "Timer task was not executed within the expected time");

        nettyManager.stopTimer(timerId);
    }

}