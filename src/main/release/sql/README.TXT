USE ORACLE

1.) change the db.mode property in start-index.sh to 'oracle'

2.) adapt your oracle access parameters under

  plugins/admin-urlmaintenance/conf/META-INF/persistence.xml


MIGRATION FROM MYSQL TO ORACLE
  
If you migrate an existing DB from an installation before version 3.2.1.1 from MYSQL 
using the SQLDeveloper Migration, make sure the table fields do not start with '_' 
and run update_oracle_ingrid-2155.sql script. 


MIGRATING FROM MYSQL TO MYSQL

Since Version 3.2.1.1 the data model was changed due to Oracle compatibility issues.
Please run update_mysql_ingrid-2155.sql to adapt the mysql DB schema.   