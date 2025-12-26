package com.colak.network.requestresponse;

import com.colak.network.udpsender.UdpParams;
import lombok.Getter;

import java.net.DatagramPacket;
import java.time.Duration;

@Getter
public class SendWithAckParams {
    private final UdpParams udpParams;
    private final int responseTimeoutMillis;
    private final ResponseValidator responseValidator;

    private SendWithAckParams(Builder builder) {
        this.udpParams = builder.udpParams;
        this.responseTimeoutMillis = builder.responseTimeoutMillis;
        this.responseValidator = builder.responseValidator;
    }

    public static Builder builder() {
        return new Builder();
    }

    public DatagramPacket toDatagramPacket() {
        return udpParams.toDatagramPacket();
    }

    public static class Builder {
        private UdpParams udpParams;
        private int responseTimeoutMillis = 5000; // Default 5 seconds
        private ResponseValidator responseValidator;

        public Builder udpParams(UdpParams udpParams) {
            this.udpParams = udpParams;
            return this;
        }

        public Builder timeout(Duration timeout) {
            try {
                this.responseTimeoutMillis = Math.toIntExact(timeout.toMillis());
            } catch (ArithmeticException e) {
                throw new IllegalArgumentException("Timeout value is too big: " + timeout, e);
            }
            return this;
        }

        public Builder responseValidator(ResponseValidator responseValidator) {
            this.responseValidator = responseValidator;
            return this;
        }

        public SendWithAckParams build() {
            if (udpParams == null || responseValidator == null) {
                throw new IllegalArgumentException("UdpParams and response validator must be provided");
            }
            if (responseTimeoutMillis <= 0) {
                throw new IllegalArgumentException("Timeout must be positive");
            }
            return new SendWithAckParams(this);
        }
    }
}