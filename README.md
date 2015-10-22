Tough Day 2.0
=============

Usage
=====
java -jar toughday2.jar [--help | --print_tests] [\<global arguments\> | \<actions\>]

Use --print_tests to show full help of all the available tests, suites and actions

Global arguments
----------------
| Parameter                     |   Description
| ----------------------------- | --------------------------------------------------------------------------------
| --port=val                    |
|	--waittime=val                |   Wait time between two consecutive test runs for a user in milliseconds
|	--duration=val                |   How long to run toughday
|	--host=val                    | 
|	--concurrency=val             |   Number of concurrent users
|	--password=val                |
|	--user=val                    |
|	--timeout=val                 |   How long a test runs before it is interrupted and marked as failed
|	--SuiteSetup=val              |   Setup step for the test suite, where "val" can be:
|	--suite=val                   |   Where "val" can be one, or more predefined suite. (use comas to separate them)

Available actions
-----------------
| Paramerer                                                       |  Description
| --------------------------------------------------------------- | -------------------------------------------------
| --add TestClass/PublisherClass property1=val property2=val      |  add a test to the suite or a publisher
| --config TestName property1=val property2=val                   |  override parameters for a test from a predefined suite
| --exclude TestName                                              |  exclude a test from a predefined suite

Building
========

$ mvn clean package

