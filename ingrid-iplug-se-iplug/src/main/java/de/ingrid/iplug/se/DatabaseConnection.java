/*
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: /cvs/asp-search/src/java/com/ms/aspsearch/PermissionDeniedException.java,v $
 */

package de.ingrid.iplug.se;

import de.ingrid.utils.IDataSourceConnection;

/**
 * Class for creating a database connection.
 */
public class DatabaseConnection implements IDataSourceConnection {

    private static final long serialVersionUID = DatabaseConnection.class
            .getName().hashCode();

    public static final String DRIVER_MYSQL = "com.mysql.jdbc.Driver";
    public static final String DRIVER_ORACLE = "oracle.jdbc.driver.OracleDriver";
    public static final String DRIVER_MS_2000 = "com.microsoft.jdbc.sqlserver.SQLServerDriver";
    public static final String DRIVER_MS_2005 = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    public static final String DRIVER_POSTGRE = "org.postgresql.Driver";
    public static final String DRIVER_ACCESS = "sun.jdbc.odbc.JdbcOdbcDriver";

    private String fDriver;

    private String fConnectionurl;

    private String fUser;

    private String fPassword;

    private String fSchema;

    public DatabaseConnection() {
        super();
    }

    /**
     * @param driver
     * @param connectionurl
     * @param user
     * @param password
     * @param schema
     */
    public DatabaseConnection(String driver, String connectionurl, String user,
            String password, String schema) {
        this.fDriver = driver;
        this.fConnectionurl = connectionurl;
        this.fUser = user;
        this.fPassword = password;
        this.fSchema = schema;
    }

    /**
     * Returns the database driver.
     * 
     * @return The driver.
     */
    public String getDataBaseDriver() {
        return this.fDriver;
    }

    public void setDataBaseDriver(String fDriver) {
        this.fDriver = fDriver;
    }

    /**
     * Returns the database connection url.
     * 
     * @return The connection URL.
     */
    public String getConnectionURL() {
        return this.fConnectionurl;
    }

    public void setConnectionURL(String fConnectionurl) {
        this.fConnectionurl = fConnectionurl;
    }

    /**
     * Returns the database user.
     * 
     * @return The user.
     */
    public String getUser() {
        return this.fUser;
    }

    public void setUser(String fUser) {
        this.fUser = fUser;
    }

    /**
     * Returns the database password for the given user.
     * 
     * @return The password.
     */
    public String getPassword() {
        return this.fPassword;
    }

    public void setPassword(String fPassword) {
        this.fPassword = fPassword;
    }

    /**
     * Returns the database schema.
     * 
     * @return The schema.
     */
    public String getSchema() {
        return this.fSchema;
    }

    public void setSchema(String fSchema) {
        this.fSchema = fSchema;
    }

}
