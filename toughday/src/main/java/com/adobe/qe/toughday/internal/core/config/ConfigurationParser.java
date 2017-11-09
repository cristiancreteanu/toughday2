package com.adobe.qe.toughday.internal.core.config;

/**
 * Common interface for all parsers.
 */
public interface ConfigurationParser {
    ConfigParams parse(String[] cmdLineArgs);
}
