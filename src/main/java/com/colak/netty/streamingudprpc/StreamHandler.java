package com.colak.netty.streamingudprpc;

public abstract class StreamHandler<T> {
    private final Class<T> messageType;
    private volatile boolean closed;
    private volatile boolean timedOut;

    private Runnable terminationCallback;
    private StreamInactivityTracker tracker;

    protected StreamHandler(Class<T> messageType) {
        this.messageType = messageType;
    }

    void setTerminationCallback(Runnable callback) {
        this.terminationCallback = callback;
    }

    public void setTracker(StreamInactivityTracker tracker) {
        this.tracker = tracker;
    }

    /**
     * Called internally by the transport layer.
     * Performs type check and dispatches to user-defined handler.
     */
    @SuppressWarnings("unchecked")
    public final void internalHandleMessage(Object message) {
        if (closed || timedOut) {
            return;
        }

        if (!messageType.isInstance(message)) {
            throw new IllegalArgumentException("Invalid message type. Expected: " + messageType.getName()
                                               + ", but received: " + message.getClass().getName());
        }

        T casted = (T) message;
        tracker.recordActivity();
        onHandleMessage(casted);
    }

    /**
     * Framework calls when stream completes normally.
     */
    public final void terminateStream() {
        if (!closed && !timedOut) {
            closed = true;
            if (terminationCallback != null) {
                terminationCallback.run();
            }
            onStreamClosed();
        }
    }

    /**
     * Framework calls when timeout occurs.
     */
    public final void timeout() {
        if (!closed && !timedOut) {
            timedOut = true;
            if (terminationCallback != null) {
                terminationCallback.run();
            }
            onStreamTimeout();
        }
    }

    /**
     * User implements this to process messages.
     */
    protected abstract void onHandleMessage(T message);

    /**
     * User implements this for successful completion.
     */
    protected abstract void onStreamClosed();

    /**
     * User implements this for timeout handling.
     */
    protected abstract void onStreamTimeout();

    public boolean isCompleted() {
        return closed;
    }

    public boolean isTimedOut() {
        return timedOut;
    }
}