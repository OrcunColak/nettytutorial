package com.colak.netty.streamingudprpc;

public abstract class StreamHandler<T> {

    private final Class<T> messageType;
    private volatile boolean completed;
    private volatile boolean timedOut;

    protected StreamHandler(Class<T> messageType) {
        this.messageType = messageType;
    }

    /**
     * Called internally by the transport layer.
     * Performs type check and dispatches to user-defined handler.
     */
    @SuppressWarnings("unchecked")
    public final void internalHandleMessage(Object message) {
        if (completed || timedOut) {
            return;
        }

        if (!messageType.isInstance(message)) {
            throw new IllegalArgumentException(
                    "Invalid message type. Expected: "
                    + messageType.getName()
                    + ", but received: "
                    + message.getClass().getName()
            );
        }

        T casted = (T) message;
        onHandleMessage(casted);
    }

    /**
     * Framework calls when stream completes normally.
     */
    public final void complete() {
        if (!completed && !timedOut) {
            completed = true;
            onComplete();
        }
    }

    /**
     * Framework calls when timeout occurs.
     */
    public final void timeout() {
        if (!completed && !timedOut) {
            timedOut = true;
            onTimeout();
        }
    }

    /**
     * User implements this to process messages.
     */
    protected abstract void onHandleMessage(T message);

    /**
     * User implements this for successful completion.
     */
    protected abstract void onComplete();

    /**
     * User implements this for timeout handling.
     */
    protected abstract void onTimeout();

    public boolean isCompleted() {
        return completed;
    }

    public boolean isTimedOut() {
        return timedOut;
    }
}

