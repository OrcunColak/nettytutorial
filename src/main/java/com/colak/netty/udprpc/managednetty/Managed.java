package com.colak.netty.udprpc.managednetty;

public final class Managed<T> implements AutoCloseable {
    private final T value;
    private final Runnable onClose;

    private Managed(T value, Runnable onClose) {
        this.value = value;
        this.onClose = onClose;
    }

    public static <T> Managed<T> owned(T value, Runnable onClose) {
        return new Managed<>(value, onClose);
    }

    public static <T> Managed<T> shared(T value) {
        return new Managed<>(value, () -> {});
    }

    public T get() {
        return value;
    }

    @Override
    public void close() {
        onClose.run();
    }
}

