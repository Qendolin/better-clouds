package com.qendolin.betterclouds;

import org.apache.logging.log4j.Logger;

@SuppressWarnings("unused")
public class NamedLogger {
    private final Logger delegate;
    private final String prefix;

    public NamedLogger(Logger delegate, boolean prefix) {
        this.delegate = delegate;
        this.prefix = prefix ? "[" + delegate.getName() + "] " : "";
    }

    public void debug(CharSequence message) {
        delegate.debug(prefix + message);
    }

    public void debug(CharSequence message, Throwable throwable) {
        delegate.debug(prefix + message, throwable);
    }

    public void debug(Object message) {
        delegate.debug(prefix + message);
    }

    public void debug(Object message, Throwable throwable) {
        delegate.debug(prefix + message, throwable);
    }

    public void debug(String message) {
        delegate.debug(prefix + message);
    }

    public void debug(String message, Object... params) {
        delegate.debug(prefix + message, params);
    }

    public void debug(String message, Throwable throwable) {
        delegate.debug(prefix + message, throwable);
    }

    public void error(CharSequence message) {
        delegate.error(prefix + message);
    }

    public void error(CharSequence message, Throwable throwable) {
        delegate.error(prefix + message, throwable);
    }

    public void error(Object message) {
        delegate.error(prefix + message);
    }

    public void error(Object message, Throwable throwable) {
        delegate.error(prefix + message, throwable);
    }

    public void error(String message) {
        delegate.error(prefix + message);
    }

    public void error(String message, Object... params) {
        delegate.error(prefix + message, params);
    }

    public void error(String message, Throwable throwable) {
        delegate.error(prefix + message, throwable);
    }

    public String getName() {
        return delegate.getName();
    }

    public void info(CharSequence message) {
        delegate.info(prefix + message);
    }

    public void info(CharSequence message, Throwable throwable) {
        delegate.info(prefix + message, throwable);
    }

    public void info(Object message) {
        delegate.info(prefix + message);
    }

    public void info(Object message, Throwable throwable) {
        delegate.info(prefix + message, throwable);
    }

    public void info(String message) {
        delegate.info(prefix + message);
    }

    public void info(String message, Object... params) {
        delegate.info(prefix + message, params);
    }

    public void info(String message, Throwable throwable) {
        delegate.info(prefix + message, throwable);
    }

    public void warn(CharSequence message) {
        delegate.warn(prefix + message);
    }

    public void warn(CharSequence message, Throwable throwable) {
        delegate.warn(prefix + message, throwable);
    }

    public void warn(Object message) {
        delegate.warn(prefix + message);
    }

    public void warn(Object message, Throwable throwable) {
        delegate.warn(prefix + message, throwable);
    }

    public void warn(String message) {
        delegate.warn(prefix + message);
    }

    public void warn(String message, Object... params) {
        delegate.warn(prefix + message, params);
    }

    public void warn(String message, Throwable throwable) {
        delegate.warn(prefix + message, throwable);
    }
}
