-- MySQL dump 10.13  Distrib 5.5.29, for debian-linux-gnu (x86_64)
--
-- Host: localhost    Database: curator
-- ------------------------------------------------------
-- Server version	5.5.29-0ubuntu0.12.04.1

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `Article`
--

DROP TABLE IF EXISTS `Article`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Article` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `author` varchar(128) DEFAULT NULL,
  `customText` varchar(255) DEFAULT NULL,
  `date` datetime NOT NULL,
  `locale` varchar(255) DEFAULT NULL,
  `mediaType` varchar(255) NOT NULL,
  `published` tinyint(1) NOT NULL,
  `publishedTime` datetime DEFAULT NULL,
  `quality` double DEFAULT NULL,
  `ratingsCount` int(11) NOT NULL,
  `ratingsSum` int(11) NOT NULL,
  `special_id` bigint(20) DEFAULT NULL,
  `text` longtext,
  `title` varchar(1024) NOT NULL,
  `url` varchar(255) NOT NULL,
  `views` int(11) DEFAULT NULL,
  `customtitle` varchar(256) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `url` (`url`),
  KEY `urlIdx` (`url`),
  KEY `FK379164D6F2623894` (`special_id`),
  CONSTRAINT `FK379164D6F2623894` FOREIGN KEY (`special_id`) REFERENCES `Special` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4526 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Feed`
--

DROP TABLE IF EXISTS `Feed`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Feed` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `active` tinyint(1) NOT NULL,
  `articlesCount` int(11) NOT NULL,
  `creationTime` datetime NOT NULL,
  `harvestRequired` tinyint(1) NOT NULL,
  `lastArticleTime` datetime DEFAULT NULL,
  `lastHarvestTime` datetime DEFAULT NULL,
  `reviewRequired` tinyint(1) NOT NULL,
  `url` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `url` (`url`),
  KEY `feedUrlIdx` (`url`)
) ENGINE=InnoDB AUTO_INCREMENT=175 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `MetricResult`
--

DROP TABLE IF EXISTS `MetricResult`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `MetricResult` (
  `articleId` bigint(20) NOT NULL,
  `metric` int(11) NOT NULL,
  `result` double NOT NULL,
  `id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`articleId`,`metric`),
  UNIQUE KEY `metric` (`metric`,`articleId`),
  KEY `FK6880FA0D79ED878B` (`id`),
  KEY `FK6880FA0D9B46EF01` (`articleId`),
  CONSTRAINT `FK6880FA0D79ED878B` FOREIGN KEY (`id`) REFERENCES `Article` (`id`),
  CONSTRAINT `FK6880FA0D9B46EF01` FOREIGN KEY (`articleId`) REFERENCES `Article` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Related`
--

DROP TABLE IF EXISTS `Related`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Related` (
  `b` varchar(255) NOT NULL,
  `a` varchar(255) NOT NULL,
  `frequency` int(11) NOT NULL,
  PRIMARY KEY (`b`,`a`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Request`
--

DROP TABLE IF EXISTS `Request`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Request` (
  `url` varchar(255) NOT NULL,
  `timestamp` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`url`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Special`
--

DROP TABLE IF EXISTS `Special`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Special` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `active` tinyint(1) NOT NULL,
  `date` datetime NOT NULL,
  `description` varchar(1024) NOT NULL,
  `title` varchar(512) NOT NULL,
  `url` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `url` (`url`),
  KEY `urlIdx` (`url`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Topic`
--

DROP TABLE IF EXISTS `Topic`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Topic` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `value` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `value` (`value`),
  KEY `valueIdx` (`value`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `User`
--

DROP TABLE IF EXISTS `User`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `User` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  KEY `usernameIdx` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `article_topic_mapping`
--

DROP TABLE IF EXISTS `article_topic_mapping`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `article_topic_mapping` (
  `topicId` bigint(20) NOT NULL,
  `articleId` bigint(20) NOT NULL,
  PRIMARY KEY (`articleId`,`topicId`),
  KEY `FK31AFA6358EA4DBB3` (`topicId`),
  KEY `FK31AFA6359B46EF01` (`articleId`),
  CONSTRAINT `FK31AFA6358EA4DBB3` FOREIGN KEY (`topicId`) REFERENCES `Topic` (`id`),
  CONSTRAINT `FK31AFA6359B46EF01` FOREIGN KEY (`articleId`) REFERENCES `Article` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `related_topics`
--

DROP TABLE IF EXISTS `related_topics`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `related_topics` (
  `topicId` bigint(20) NOT NULL,
  `relatedId` bigint(20) NOT NULL,
  PRIMARY KEY (`relatedId`,`topicId`),
  KEY `FK89FE43B88EA4DBB3` (`topicId`),
  KEY `FK89FE43B8D215DBAF` (`relatedId`),
  CONSTRAINT `FK89FE43B88EA4DBB3` FOREIGN KEY (`topicId`) REFERENCES `Topic` (`id`),
  CONSTRAINT `FK89FE43B8D215DBAF` FOREIGN KEY (`relatedId`) REFERENCES `Topic` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `special_id`
--

DROP TABLE IF EXISTS `special_id`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `special_id` (
  `Special_id` bigint(20) NOT NULL,
  `articles_id` bigint(20) NOT NULL,
  UNIQUE KEY `articles_id` (`articles_id`),
  KEY `FKC4844A61F2623894` (`Special_id`),
  KEY `FKC4844A61AAC094CD` (`articles_id`),
  CONSTRAINT `FKC4844A61AAC094CD` FOREIGN KEY (`articles_id`) REFERENCES `Article` (`id`),
  CONSTRAINT `FKC4844A61F2623894` FOREIGN KEY (`Special_id`) REFERENCES `Special` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_topic_mapping`
--

DROP TABLE IF EXISTS `user_topic_mapping`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_topic_mapping` (
  `userId` bigint(20) NOT NULL,
  `topicId` bigint(20) NOT NULL,
  PRIMARY KEY (`topicId`,`userId`),
  KEY `FKBB10AC4A8EA4DBB3` (`topicId`),
  KEY `FKBB10AC4A1706F2B7` (`userId`),
  CONSTRAINT `FKBB10AC4A1706F2B7` FOREIGN KEY (`userId`) REFERENCES `User` (`id`),
  CONSTRAINT `FKBB10AC4A8EA4DBB3` FOREIGN KEY (`topicId`) REFERENCES `Topic` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-02-05 23:32:10
