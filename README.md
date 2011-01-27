Grisu template
=============

A grisu swing client which uses templates to render app-specific job creation panels.

Prerequisites
--------------------

In order to build the backend from the git sources, you need: 

- Sun Java Development Kit (version ≥ 6)
- [git](http://git-scm.com) 
- [Apache Maven](http://maven.apache.org) (version >=2)


Checking out sourcecode
-------------------------------------

`git clone git://github.com/grisu/grisu-template.git`

Building Grisu template using Maven
------------------------------------------

To build one of the above modules, cd into the module root directory of the module to build and execute: 

    cd grisu-template
    mvn clean install

This will build a war file that can be deployed into a container and also a deb file that can be installed on a Debian based machine.

Running the client
---------------------------

TODO

