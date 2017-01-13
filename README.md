Tough Day 2.0
=============

Usage
=====
__java -jar toughday2.jar [--help | --print_tests] [\<global arguments\> | \<actions\>]__

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
| Parameter                                                       |  Description
| --------------------------------------------------------------- | -------------------------------------------------
| --add TestClass/PublisherClass property1=val property2=val      |  add a test to the suite or a publisher
| --config TestName property1=val property2=val                   |  override parameters for a test from a predefined suite
| --exclude TestName                                              |  exclude a test from a predefined suite

Building
========

$  __mvn clean package__

Examples
========

smoke_tests
-----------
 $ __java -jar toughday2.jar --suite=smoke_tests__

 $ __java -jar toughday2.jar --suite=smoke_tests --concurrency=30 --waittime=500__
 
 $ __java -jar toughday2.jar --suite=smoke_tests --add ConsolePublisher --add CSVPublisher filepath=myresults.csv__

Product name and versioning
===========================

Name of product
---------------
The product is called "Toughday 2" (artifact "toughday2")

Versioning
----------
* We follow [semantic versioning](http://semver.org/)
* We start with 0.1.0-SNAPSHOT - allowed to break API until first major release
* It's not stable until 1.0.0
* 1.0.0 should be an official proposed release candidate
* We might go into prod with something like toughday2 1.5.6

When is 1.0.0 released?
-----------------------
When we think we're ready for production. See the _Toughday_ component JIRA, under NPR.

What is alpha, what is beta
---------------------------
* Beta should be before the actual version release
* We can have 0.5.0-beta, which is a version before the actual 0.5.0, but we will probably not
* We should have 1.0.0-beta before cutting 1.0.0

JIRA versions
-------------
* Following the development versioning of the artifacts
* We currently have versions _1.0.0_ and _1.0.0-beta_
 
Branching
---------
* We will do NO proactive branching and will avoid having more than one long-lived branch
