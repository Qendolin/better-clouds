package com.qendolin.betterclouds.platform.forge;

import com.qendolin.betterclouds.platform.ModVersion;

//? if forge {
/*import org.apache.maven.artifact.versioning.ArtifactVersion;

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
*///?} else {
public abstract class ModVersionImpl extends ModVersion {
}
//?}