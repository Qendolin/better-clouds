package com.qendolin.betterclouds.platform.fabric;

import com.qendolin.betterclouds.platform.ModVersion;

//? if fabric {
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;

import java.util.Optional;

public class ModVersionImpl extends ModVersion {

    private final Version delegate;

    public ModVersionImpl(Version delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean isPresent() {
        return delegate != null;
    }

    @Override
    public String getFriendlyString() {
        return delegate.getFriendlyString();
    }

    @Override
    public Optional<SemVer> asSemVer() {
        SemanticVersion semver;
        if (delegate instanceof SemanticVersion) {
            semver = (SemanticVersion) delegate;
        } else {
            try {
                semver = SemanticVersion.parse(delegate.getFriendlyString());
            } catch (VersionParsingException e) {
                return Optional.empty();
            }
        }

        int major = 0, minor = 0, patch = 0;
        int count = semver.getVersionComponentCount();
        if (count == 0) return Optional.empty();
        if (count >= 1) major = semver.getVersionComponent(0);
        if (count >= 2) minor = semver.getVersionComponent(1);
        if (count >= 3) patch = semver.getVersionComponent(2);
        return Optional.of(new SemVer(major, minor, patch, semver.getBuildKey().orElse(""), semver.getPrereleaseKey().orElse("")));
    }

}
//?} else {
/*public abstract class ModVersionImpl extends ModVersion {
}
*///?}