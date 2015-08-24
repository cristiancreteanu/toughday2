package com.day.qa.toughday;


import com.day.qa.toughday.publishers.ConsolePublisher;
import com.day.qa.toughday.tests.CreatePageTest;
import com.day.qa.toughday.tests.CreateUserTest;
import com.day.qa.toughday.tests.OtherTest;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        TestSuite suite = new TestSuite(10, 10, 4)
                .add(new CreatePageTest(), 50)
                .add(new CreateUserTest(), 35)
                .add(new OtherTest(), 15)
                .addPublisher(new ConsolePublisher());
        suite.runTests();
    }
}
