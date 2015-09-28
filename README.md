# toughday2
Toughday 2.0

usage: java -jar <toughday-jar> <global arguments>|<actions>
global arguments:
	--Host=val				
	--User=val				
	--Password=val				
	--Concurrency=val				number of concurrent users
	--Duration=val				how long will toughday run
	--WaitTime=val				wait time between two consecutive test runs for a user in milliseconds
	--Timeout=val				how long can a test run before it is interrupted and marked as failed
	--Port=val				
	--SuiteSetup=val				setup step for the test suite. where "val" can be:  DummySuiteSetup
	--Suite=val				where "val" can be one, or more predefined suite. (use comas to separate them)

available actions:
	--add TestClass/PublisherClass property1=val property2=val				 add a test to the suite or a publisher
	--config TestName property1=val property2=val				 override parameters for a test from a predefined suite
	--exclude TestName				 exclude a test from a predefined suite
	
	For more details run java -jar <toughday-jar> --help
