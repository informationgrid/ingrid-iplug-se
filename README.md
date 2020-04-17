Search Engine iPlug
========

The SE-iPlug crawls websites within a defined URL space and connects the crawled resources to the InGrid data space.

Features
--------

- harvests websites within a defined URL at a certain schedule
- based on apache nutch and elastic search
- flexible crawling functionality
  - adaptive crawl reschedule algorithm
  - flexible metadata definition and propagation algorithm
  - supports multiple crawl instances
- provides search functionality on the crawled data
- GUI for easy crawl administration, url space definition


Requirements
-------------

- a running InGrid Software System

Installation
------------

Download from https://distributions.informationgrid.eu/ingrid-iplug-se/
 
or

build from source with `mvn clean package`.

Execute

```
java -jar ingrid-iplug-se-x.x.x-installer.jar
```

and follow the install instructions.

Obtain further information at http://www.ingrid-oss.eu/ (sorry only in German)


Contribute
----------

- Issue Tracker: https://github.com/informationgrid/ingrid-iplug-se/issues
- Source Code: https://github.com/informationgrid/ingrid-iplug-se
 
### Set up eclipse project

```
mvn eclipse:eclipse
```

and import project into eclipse.

### Tests

The tests do not work out of the box, since they are adapted
to run inside a docker container with jenkins.

To enable the tests, start a elastic search node:

```docket-compose up -d```

Adapt the file `ingrid-iplug-se-iplug/src/test/resources/elasticsearch.properties`.


### Debug under eclipse

- execute `mvn install` to expand the base web application
- set up a java application Run Configuration with start class `de.ingrid.iplug.se.SEIPlug`
- add the VM argument `-Djetty.webapp=src/main/webapp` to the Run Configuration
- add src/test/resources to class path
- the admin gui starts per default on port 8082, change this with VM argument `-Djetty.port=8083`

### Debug nutch process

Debugging a nutch process can be tricky because the processes are spawned from the main process. Do
as follows:

- Add -DdebugNutchCall=<TRAILING NUTCH PROCESS>, i.e. BWUpdateDb or see IngridCrawlNutchProcess.java
- The process will halt there until the remote debugger connects to port 7000


Support
-------

If you are having issues, please let us know: info@informationgrid.eu

License
-------

The project is licensed under the EUPL license.
