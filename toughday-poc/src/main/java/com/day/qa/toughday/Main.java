package com.day.qa.toughday;


import com.day.qa.toughday.core.Engine;
import com.day.qa.toughday.core.TestSuite;
import com.day.qa.toughday.core.config.ConfigurationManager;

/**
 * Hello world!
 *
 */
public class Main
{
    public static void main( String[] args )
            throws Exception {
        ConfigurationManager configurationManager = new ConfigurationManager(args);
        if(args.length == 0 || (args.length == 1 && args[0] == "--help")) {
            configurationManager.printHelp();
        }
        else {
            TestSuite suite = configurationManager.getTestSuite();
            Engine engine = new Engine(suite);
            engine.runTests();
        }
    }
}
