-- MySQL dump 10.14  Distrib 5.5.53-MariaDB, for Linux (x86_64)
--
-- Host: localhost    Database: localhost
-- ------------------------------------------------------
-- Server version	5.5.53-MariaDB

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
-- Table structure for table `acl`
--

DROP TABLE IF EXISTS `acl`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `acl` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `key` char(64) NOT NULL,
  `description` text,
  `timestamp` datetime DEFAULT NULL,
  `member_num` int(11) DEFAULT NULL,
  `useyn` varchar(1) DEFAULT NULL,
  `sales_man_num` int(11) DEFAULT NULL,
  `server_ip` varchar(15) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `key` (`key`)
) ENGINE=InnoDB AUTO_INCREMENT=148 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `acl_ip`
--

DROP TABLE IF EXISTS `acl_ip`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `acl_ip` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `ip` int(10) unsigned NOT NULL,
  `netmask` tinyint(3) unsigned NOT NULL DEFAULT '32',
  `description` text,
  `timestamp` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_ip` (`ip`,`netmask`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `acl_list`
--

DROP TABLE IF EXISTS `acl_list`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `acl_list` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `phone` varchar(20) NOT NULL,
  `acl_id` bigint(20) NOT NULL,
  `regdate` datetime DEFAULT NULL,
  `useyn` varchar(1) DEFAULT NULL,
  `is_autoreply` tinyint(1) NOT NULL DEFAULT '0',
  `autoreply_charge_phone` varchar(20) DEFAULT NULL,
  `autoreply_msg` text,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4136 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `attachment`
--

DROP TABLE IF EXISTS `attachment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `attachment` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `mo_id` bigint(20) unsigned NOT NULL,
  `name` varchar(255) NOT NULL,
  `mime` varchar(255) NOT NULL,
  `size` int(10) unsigned NOT NULL,
  `path` varchar(2048) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `mo_id` (`mo_id`),
  CONSTRAINT `attachment_ibfk_1` FOREIGN KEY (`mo_id`) REFERENCES `mo` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=156116522 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `mo`
--

DROP TABLE IF EXISTS `mo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `mo` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `msg_id` varchar(255) NOT NULL,
  `type` enum('SMS','LMS','MMS') NOT NULL,
  `is_autoreply` tinyint(1) NOT NULL DEFAULT '0',
  `type_autoreply` enum('SMS','LMS','MMS') DEFAULT NULL,
  `from` varchar(255) NOT NULL,
  `from_telco` varchar(255) NOT NULL,
  `to` varchar(255) NOT NULL,
  `to_telco` varchar(255) NOT NULL,
  `subject` varchar(255) NOT NULL,
  `body` text NOT NULL,
  `body_autoreply` text,
  `attach_count` int(10) unsigned NOT NULL,
  `forward_count` int(10) unsigned NOT NULL,
  `cdr` enum('READY','PROCESSING','DONE') NOT NULL DEFAULT 'READY',
  `timestamp` datetime NOT NULL,
  `agentyn` varchar(1) DEFAULT NULL,
  `agentdate` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `type` (`type`),
  KEY `from` (`from`),
  KEY `to` (`to`),
  KEY `cdr` (`cdr`),
  KEY `timestamp` (`timestamp`)
) ENGINE=InnoDB AUTO_INCREMENT=204957588 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `mt`
--

DROP TABLE IF EXISTS `mt`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `mt` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `mo_id` bigint(20) unsigned NOT NULL,
  `serial` bigint(20) unsigned NOT NULL,
  `to` varchar(255) NOT NULL,
  `carrier` varchar(255) NOT NULL,
  `status` enum('SENDING','DONE','ERR_INVALID_RECIPIENT','ERR_POWER_OFF','ERR_WEAK_SIGNAL','ERR_OVER_CAPACITY','ERR_PORTED_OUT','ERR_FORWARD_LIMIT','ERR_SPAM','ERR_UNSUPPORTED_MEDIA','ERR_UNSUPPORTED_MMS','ERR_TIMEOUT','ERR_INVALID_CREDENTIAL','ERR_ETC') NOT NULL DEFAULT 'SENDING',
  `cdr` enum('READY','PROCESSING','DONE') NOT NULL DEFAULT 'READY',
  `submit_timestamp` datetime NOT NULL,
  `receipt_timestamp` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `mo_id` (`mo_id`),
  KEY `serial` (`serial`),
  KEY `cdr` (`cdr`,`status`),
  KEY `submit_timestamp` (`submit_timestamp`),
  CONSTRAINT `mt_ibfk_1` FOREIGN KEY (`mo_id`) REFERENCES `mo` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=51565013 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `push`
--

DROP TABLE IF EXISTS `push`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `push` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'PK',
  `acl_id` bigint(20) unsigned NOT NULL COMMENT 'acl, acl_list 테이블 FK',
  `protocol` enum('HTTP','HTTPS') NOT NULL COMMENT 'http 또는 https 프로토콜 선택',
  `host` varchar(512) NOT NULL COMMENT '고객사 서버 도메인 또는 IP',
  `port` int(10) unsigned NOT NULL COMMENT '고객사 서버 포트',
  `path_health` varchar(512) NOT NULL COMMENT '(고객사 서버 API 경로 ex /api/v1/health) 상태 체크용',
  `path_text_message` varchar(512) NOT NULL COMMENT '(고객사 서버 API 경로 ex /api/v1/health) SMS, LMS 수신용',
  `path_multi_media_message` varchar(512) NOT NULL COMMENT '(고객사 서버 API 경로 ex /api/v1/health) MMS 수신용',
  `active` tinyint(4) NOT NULL DEFAULT '0' COMMENT '실제 사용 여부 true, false',
  `fail_count` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT 'push 서버에서 해당 고객의 URL로 API 요청시 실패 개수',
  `created_date` datetime(6) DEFAULT NULL COMMENT '생성일',
  `last_modified_date` datetime(6) DEFAULT NULL COMMENT '수정일',
  `use_content_disposition_array` tinyint(4) NOT NULL DEFAULT '0' COMMENT 'multipart request 내 content-disposition name을 array로 사용할지 여부',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_acl_id` (`acl_id`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `stat_acllist`
--

DROP TABLE IF EXISTS `stat_acllist`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `stat_acllist` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `description` varchar(200) DEFAULT NULL,
  `type` varchar(10) DEFAULT NULL,
  `to_telco` varchar(10) DEFAULT NULL,
  `mo_cnt` bigint(20) DEFAULT NULL,
  `suc_cnt` bigint(20) DEFAULT NULL,
  `orderdate` varchar(6) DEFAULT NULL,
  `regdate` datetime DEFAULT NULL,
  `moddate` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `type` (`type`),
  KEY `regdate` (`regdate`),
  KEY `orderdate` (`orderdate`),
  KEY `description` (`description`)
) ENGINE=InnoDB AUTO_INCREMENT=16967 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `stat_cnt`
--

DROP TABLE IF EXISTS `stat_cnt`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `stat_cnt` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `mo_cnt` bigint(20) DEFAULT NULL,
  `mt_cnt` bigint(20) DEFAULT NULL,
  `orderdate` varchar(6) DEFAULT NULL,
  `regdate` datetime DEFAULT NULL,
  `moddate` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `regdate` (`regdate`),
  KEY `orderdate` (`orderdate`)
) ENGINE=InnoDB AUTO_INCREMENT=84 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `stat_etc`
--

DROP TABLE IF EXISTS `stat_etc`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `stat_etc` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `description` varchar(200) DEFAULT NULL,
  `type` varchar(20) DEFAULT NULL,
  `to_telco` varchar(20) DEFAULT NULL,
  `phone` varchar(80) DEFAULT NULL,
  `mo_cnt` bigint(20) DEFAULT NULL,
  `orderdate` varchar(6) DEFAULT NULL,
  `regdate` datetime DEFAULT NULL,
  `moddate` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `type` (`type`),
  KEY `regdate` (`regdate`),
  KEY `orderdate` (`orderdate`),
  KEY `description` (`description`)
) ENGINE=InnoDB AUTO_INCREMENT=34484 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `stat_mo`
--

DROP TABLE IF EXISTS `stat_mo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `stat_mo` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `msg_type` varchar(10) DEFAULT NULL,
  `type` varchar(10) DEFAULT NULL,
  `from` varchar(20) DEFAULT NULL,
  `from_telco` varchar(20) DEFAULT NULL,
  `to` varchar(20) DEFAULT NULL,
  `timestamp` datetime DEFAULT NULL,
  `orderdate` varchar(6) NOT NULL,
  `regdate` datetime DEFAULT NULL,
  `moddate` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `msg_type` (`msg_type`),
  KEY `type` (`type`),
  KEY `regdate` (`regdate`),
  KEY `orderdate` (`orderdate`),
  KEY `to` (`to`),
  KEY `from` (`from`)
) ENGINE=InnoDB AUTO_INCREMENT=48445658 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `stat_mt`
--

DROP TABLE IF EXISTS `stat_mt`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `stat_mt` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `msg_type` varchar(10) DEFAULT NULL,
  `type` varchar(10) DEFAULT NULL,
  `from` varchar(20) DEFAULT NULL,
  `from_telco` varchar(20) DEFAULT NULL,
  `to` varchar(20) DEFAULT NULL,
  `to_telco` varchar(20) DEFAULT NULL,
  `submit_timestamp` datetime DEFAULT NULL,
  `receipt_timestamp` datetime DEFAULT NULL,
  `ETC` varchar(20) DEFAULT NULL,
  `ETC2` varchar(20) DEFAULT NULL,
  `ETC3` varchar(20) DEFAULT NULL,
  `orderdate` varchar(6) NOT NULL,
  `regdate` datetime DEFAULT NULL,
  `moddate` datetime DEFAULT NULL,
  `mt_to` varchar(20) DEFAULT NULL,
  `timestamp` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `msg_type` (`msg_type`),
  KEY `type` (`type`),
  KEY `regdate` (`regdate`),
  KEY `orderdate` (`orderdate`),
  KEY `to` (`to`),
  KEY `from` (`from`)
) ENGINE=InnoDB AUTO_INCREMENT=48116351 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `username` varchar(255) NOT NULL,
  `password` char(64) NOT NULL,
  `description` text,
  `active` tinyint(1) NOT NULL,
  `join_date` datetime NOT NULL,
  `last_login_date` datetime DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `acl_id` bigint(20) DEFAULT NULL,
  `member_num` int(11) DEFAULT NULL,
  `sales_man_num` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  KEY `active` (`active`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-03-12 15:51:49
