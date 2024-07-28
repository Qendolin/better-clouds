package com.qendolin.betterclouds.platform.fabric;

import com.qendolin.betterclouds.platform.ModVersion;

//? if fabric {
/*import net.fabricmc.loader.api.Version;
public class ModVersionImpl extends ModVersion {

    private final Version delegate;

    public ModVersionImpl(Version delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean isPresent() {
        return true;
    }

    @Override
    public String getFriendlyString() {
        return delegate.getFriendlyString();
    }
}*/
//?} else {
public abstract class ModVersionImpl extends ModVersion {
}
//?}