package com.day.qa.toughday;


import com.day.qa.toughday.core.Engine;
import com.day.qa.toughday.core.config.Configuration;

/**
 * Hello world!
 *
 */
public class Main
{
    public static void main( String[] args )
            throws Exception {
        Configuration configuration = new Configuration(args);
        if(args.length == 0 || (args.length == 1 && args[0] == "--help")) {
            configuration.printHelp();
        }
        else {
            Engine engine = new Engine(configuration);
            engine.runTests();
        }
    }
}
