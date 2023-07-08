package com.qendolin.betterclouds.compat;

import com.qendolin.betterclouds.Main;

import java.net.URL;
import java.util.concurrent.CompletableFuture;

public interface ITelemetry {
    ITelemetry INSTANCE = create();

    static ITelemetry create() {
        try {
            URL url = new URL(Telemetry.ENDPOINT);
            return new Telemetry(url);
        } catch (Throwable e) {
            Main.LOGGER.error("Failed to create telemetry service: ", e);
        }
        return new NoopTelemetry();
    }

    CompletableFuture<Boolean> sendSystemInfo();

    void sendShaderCompileError(String error);

    void sendUnhandledException(Exception e);

    class NoopTelemetry implements ITelemetry {
        @Override
        public CompletableFuture<Boolean> sendSystemInfo() {
            return CompletableFuture.completedFuture(false);
        }

        @Override
        public void sendShaderCompileError(String error) {
        }

        @Override
        public void sendUnhandledException(Exception e) {
        }
    }
}
