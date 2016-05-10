| Xtended-XML | [![Build Status](https://travis-ci.org/elbywan/xtended-xml.svg?branch=master)](https://travis-ci.org/elbywan/xtended-xml) | [![Coverage Status](https://coveralls.io/repos/elbywan/xtended-xml/badge.svg)](https://coveralls.io/r/elbywan/xtended-xml)
===============

##Xtended-xml ?

**Xtended-xml** is an xml pull parser built on top of the official scala-xml library.

##Installation

Add the following lines to your sbt build file :

```
resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies += "com.github.elbywan" %% "xtended-xml" % "0.1-SNAPSHOT"
```

To play around with the library, type `sbt console`.

##Usage

>todo : detailed usage

In the meantime, you can check the tests folder for some code samples.

###Imports

```
import org.xml.xtended.core.XtendedXmlParser
import org.xml.xtended.core.XtendedXmlParser._
```

###Initializing the parser

```
//Where xml is a Source object
val parser = new XtendedXmlParser(xml)
```

###Actions


###Pulled events list


###Content retrieval
