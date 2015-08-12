package com.day.qa.toughday;


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
                .add(new OtherTest(), 15);
        suite.test();
    }
}
