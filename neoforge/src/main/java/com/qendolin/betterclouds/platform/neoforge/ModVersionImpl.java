package com.qendolin.betterclouds.platform.neoforge;

import com.qendolin.betterclouds.platform.ModVersion;
import org.apache.maven.artifact.versioning.ArtifactVersion;

public class ModVersionImpl extends ModVersion {

    private final ArtifactVersion delegate;

    public ModVersionImpl(ArtifactVersion delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean isPresent() {
        return delegate != null;
    }

    @Override
    public String getFriendlyString() {
        return delegate.toString();
    }
}
