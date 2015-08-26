package com.day.qa.toughday;


import com.day.qa.toughday.publishers.ConsolePublisher;
import com.day.qa.toughday.tests.CreateUserTest;

/**
 * Hello world!
 *
 */
public class Main
{
    public static void main( String[] args )
    {
        System.out.println(Runtime.getRuntime().totalMemory() + " " + Runtime.getRuntime().maxMemory());
        TestSuite suite = new TestSuite(300, 10, 2000)
                //.add(new CreatePageTest(), 100)
                .add(new CreateUserTest(), 35)
                //.add(new OtherTest(), 15)
                .addPublisher(new ConsolePublisher());
        suite.runTests();
    }
}
