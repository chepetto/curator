#!/bin/sh
mvn install:install-file -Dfile=org.eclipse.mylyn.wikitext.core_1.8.0.I20130206-1602.jar -DgroupId=org.eclipse.mylyn.wikitext -DartifactId=wikitext -Dversion=1.8.0 -Dpackaging=jar
mvn install:install-file -Dfile=org.eclipse.mylyn.wikitext.mediawiki.core_1.8.0.I20130206-1602.jar -DgroupId=org.eclipse.mylyn.wikitext -DartifactId=wikitext.mediawiki -Dversion=1.8.0 -Dpackaging=jar
