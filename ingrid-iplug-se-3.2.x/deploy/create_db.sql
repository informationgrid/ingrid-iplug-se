-- SQL-script to create a new database 'myles_iplugse' on local machine.
CREATE DATABASE IF NOT EXISTS myles_iplugse DEFAULT CHARACTER SET utf8;
GRANT ALL PRIVILEGES ON myles_iplugse.* TO 'myles'@'localhost' IDENTIFIED BY 'eki7y6' WITH GRANT OPTION;