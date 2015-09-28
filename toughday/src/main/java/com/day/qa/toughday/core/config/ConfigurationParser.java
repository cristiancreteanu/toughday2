package com.day.qa.toughday.core.config;

/**
 * Created by tuicu on 18/09/15.
 * Common interface for all parsers.
 */
public interface ConfigurationParser {
    ConfigParams parse(String[] cmdLineArgs);
}
