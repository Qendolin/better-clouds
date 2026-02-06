package com.qendolin.betterclouds.telemetry;

import com.qendolin.betterclouds.BetterCloudsStatic;
import gs.mclo.api.MclogsClient;
import gs.mclo.api.response.UploadLogResponse;

import java.util.concurrent.CompletableFuture;

public class McLogsUploader implements ILogUploader {

    private final MclogsClient client;

    public McLogsUploader() {
        String version = "unknown";
        if(BetterCloudsStatic.getVersion() != null) {
            version = BetterCloudsStatic.getVersion().getFriendlyString();
        }
        client = new MclogsClient(BetterCloudsStatic.MODID, version);
    }

    public CompletableFuture<String> upload(String text) {
        return client.uploadLog(text).thenApply(UploadLogResponse::getUrl);
    }
}
