Tough Day 2.0
=============

Usage
=====

https://wiki.corp.adobe.com/display/cq5/ToughDay+2+-+Customer+Doc

Building
========

$  __mvn clean package__ (using the reactor pom) 

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
