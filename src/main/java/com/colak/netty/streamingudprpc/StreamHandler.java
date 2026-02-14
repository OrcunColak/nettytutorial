package com.colak.netty.streamingudprpc;

public abstract class StreamHandler<T> {

    private final Class<T> messageType;

    private Runnable closeRequest;
    private Runnable timeoutRequest;

    protected StreamHandler(Class<T> messageType) {
        this.messageType = messageType;
    }

    /// This is called when stream is started
    void bindLifecycle(Runnable closeRequest,
                       Runnable timeoutRequest) {
        this.closeRequest = closeRequest;
        this.timeoutRequest = timeoutRequest;
    }

    /// Called by user code to close the stream
    protected final void requestClose() {
        if (closeRequest != null) {
            closeRequest.run();
        }
    }

    @SuppressWarnings("unchecked")
    final void internalHandleMessage(Object message) {
        if (!messageType.isInstance(message)) {
            throw new IllegalArgumentException("Invalid message type: " + message.getClass().getName());
        }

        onHandleMessage((T) message);
    }

    protected abstract void onHandleMessage(T message);

    protected abstract void onStreamClosed();

    protected abstract void onStreamTimeout();
}
