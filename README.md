Requirements
============

You must first install [Maven](http://maven.apache.org/)

Installation
============

In the root directory run <code>mvn install</code>. This will unpack the native dependencies into
<code>target/natives</code>

If you are an Eclipse user you will need to follow the instructions [here](http://maven.apache.org/plugins/maven-eclipse-plugin/)
on how to work with Maven projects.

If you are an Intellij user you need do nothing else, as Intellij is vastly superior and has Maven integration as standard.

Running
=======

You need to set <code>-Djava.library.path=target/natives</code> before running <code>net.winsauce.noiseTest.Window</code>