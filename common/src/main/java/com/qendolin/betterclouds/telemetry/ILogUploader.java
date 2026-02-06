package com.qendolin.betterclouds.telemetry;


import java.util.concurrent.CompletableFuture;

public interface ILogUploader {

    CompletableFuture<String> upload(String log);

}
