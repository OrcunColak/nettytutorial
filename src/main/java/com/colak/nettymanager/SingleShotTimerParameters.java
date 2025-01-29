package com.colak.nettymanager;

public record SingleShotTimerParameters(Runnable runnable, long delay) {
}
