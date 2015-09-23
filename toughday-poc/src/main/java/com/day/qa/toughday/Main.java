package com.day.qa.toughday;


import com.day.qa.toughday.core.Engine;
import com.day.qa.toughday.core.config.CliParser;
import com.day.qa.toughday.core.config.Configuration;

/**
 * Hello world!
 *
 */
public class Main
{
    public static void main( String[] args )
            throws Exception {
        if(args.length == 0 || (args.length == 1 && args[0] == "--help")) {
            CliParser cliParser = new CliParser();
            cliParser.printHelp();
        }
        else {
            Configuration configuration = new Configuration(args);
            Engine engine = new Engine(configuration);
            engine.runTests();
        }
    }
}
