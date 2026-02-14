package com.colak.netty.streamingudprpc;

public abstract class StreamHandler<T> {

    private final Class<T> messageType;

    private Runnable closeRequest;

    protected StreamHandler(Class<T> messageType) {
        this.messageType = messageType;
    }

    /// This is called when stream is started
    void bindLifecycle(Runnable closeRequest) {
        this.closeRequest = closeRequest;
    }

    /// Called by user code to close the stream
    protected final void requestClose() {
        if (closeRequest != null) {
            closeRequest.run();
        }
    }

    @SuppressWarnings("unchecked")
    final boolean internalHandleMessage(Object message) {
        if (!messageType.isInstance(message)) {
            throw new IllegalArgumentException("Invalid message type: " + message.getClass().getName());
        }

        return onHandleMessage((T) message);
    }

    protected abstract boolean onHandleMessage(T message);

    protected abstract void onStreamClosed();

    protected abstract void onStreamTimeout();
}
