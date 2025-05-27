package com.colak.netty;

import com.colak.netty.timerparams.SingleShotTimerParameters;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
class SingleShotTimerTest {

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

        SingleShotTimerParameters parameters = new SingleShotTimerParameters(() -> {

            // Run by netty-worker-3-1
            log.info("Single shot timer expired");
            countDownLatch.countDown();
        }, 0);
        nettyManager.scheduleSingleShotTimer(parameters);

        boolean taskExecuted = countDownLatch.await(2, TimeUnit.SECONDS);
        assertTrue(taskExecuted, "Timer task was not executed within the expected time");
    }

}