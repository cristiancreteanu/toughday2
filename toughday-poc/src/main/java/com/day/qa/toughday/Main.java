package com.day.qa.toughday;


import com.day.qa.toughday.cli.Cli;

import java.lang.reflect.InvocationTargetException;

/**
 * Hello world!
 *
 */
public class Main
{
    public static void main( String[] args ) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        Cli commandLineParser = new Cli();
        if(args.length == 0 || (args.length == 1 && args[0] == "--help")) {
            commandLineParser.printHelp();
        }
        else {
            TestSuite suite = commandLineParser.createTestSuite(args);
            suite.runTests();
        }
    }
}
