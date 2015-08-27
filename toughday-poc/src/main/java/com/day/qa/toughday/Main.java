package com.day.qa.toughday;


import com.day.qa.toughday.cli.Cli;
import com.day.qa.toughday.publishers.ConsolePublisher;
import com.day.qa.toughday.tests.CreatePageTest;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

/**
 * Hello world!
 *
 */
public class Main
{
    public static void main( String[] args )
    {
        Options options = Cli.getOptions();

        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp(100, "toughday","", options, "");

        TestSuite suite = new TestSuite(300, 10, 100)
                .add(new CreatePageTest(), 100)
                //.add(new CreateUserTest(), 35)
                //.add(new OtherTest(), 15)
                .addPublisher(new ConsolePublisher());
        //suite.runTests();
    }
}
