package com.day.qa.toughday;


import com.day.qa.toughday.core.Engine;
import com.day.qa.toughday.core.cli.Cli;
import com.day.qa.toughday.core.TestSuite;

/**
 * Hello world!
 *
 */
public class Main
{
    public static void main( String[] args )
            throws Exception {
        Cli commandLineParser = new Cli();
        if(args.length == 0 || (args.length == 1 && args[0] == "--help")) {
            commandLineParser.printHelp();
        }
        else {
            TestSuite suite = commandLineParser.createTestSuite(args);
            Engine engine = new Engine(suite);
            engine.runTests();
        }
    }
}
