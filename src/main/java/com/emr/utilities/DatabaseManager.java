/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.emr.utilities;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

/**
 *  Class to manage opening/closing of Database connections
 * @author LEONARD NDUATI
 */
public class DatabaseManager {
    Connection conn = null; //The connection object
    String url; //Url to the server
    String servername; //Server name
    String port;  //Port to the database
    String dbName; //Database Name
    String driver; //MYSQL Java driver
    String userName; //Database username
    String password; //Database password
    
    Properties prop = new Properties(); //Properties file for storing the settings
    OutputStream output = null; //Required for writing to file
    /**
     * Constructor
     * @param servername {@link String} Server name
     * @param port {@link String} Server Mysql Port
     * @param dbName {@link String} Database name
     * @param username {@link String} Mysql Username
     * @param password {@link String} Password
     */
    public DatabaseManager(String servername,String port,String dbName,String username,String password){
        this.servername=servername;
        this.port=port;
        this.url="jdbc:mysql://" + servername + ":" + port + "/";
        this.dbName=dbName;
        this.userName=username;
        this.password=password;
        this.driver="com.mysql.jdbc.Driver";
        try{
            Class.forName(driver).newInstance();
            conn = DriverManager.getConnection(url+dbName,userName,password);
        }catch (SQLException ex) {
            // handle any errors
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
             JOptionPane.showMessageDialog(null, "SQLException: " + ex.getMessage(), "Exception!", JOptionPane.ERROR_MESSAGE);
        }catch(ClassNotFoundException cs){
             System.out.println("Class not found: " + cs.getMessage());
             JOptionPane.showMessageDialog(null, "Class not found: " + cs.getMessage(), "Exception!", JOptionPane.ERROR_MESSAGE);
        }catch(InstantiationException | IllegalAccessException i){
             System.out.println("Instantiation/Illegal State Error: " + i.getMessage());
             JOptionPane.showMessageDialog(null, "Instantiation/Illegal State Error: " + i.getMessage(), "Exception!", JOptionPane.ERROR_MESSAGE);
        }
    }
    /**
     * Get the connection
     * @return {@link Connection} The connection object
     */
    public Connection getConnection(){
        return conn;
    }
    /**
     * Close the connection
     */
    public void cleanUp(){
         try{
            if(conn!=null)
               conn.close();
         }catch(SQLException se){
             String stacktrace=org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(se);
             JOptionPane.showMessageDialog(null, "Error occured while executing file. Error Details: " + stacktrace, "File Error", JOptionPane.ERROR_MESSAGE);
		
         }
    }
    /**
     * Save database settings to a properties file
     * @param fileName {@link String} Properties file name
     * @param prefix {@link String} Prefix of the fields (emr|mpi)
     */
    public void saveSettings(String fileName,String prefix){
        try {
 
            output = new FileOutputStream("config/" + fileName);
 
            // set the properties value
            prop.setProperty(prefix + "_servername", servername);
            prop.setProperty(prefix + "_port", port);
            prop.setProperty(prefix + "_url", url);
            prop.setProperty(prefix + "_dbname", dbName);
            prop.setProperty(prefix + "_dbuser", userName);
            prop.setProperty(prefix + "_dbpassword", password);
 
            // save properties to project root folder
            prop.store(output, null);
            JOptionPane.showMessageDialog(null, "Connection Settings successfully saved.", "Save Connection Settings", JOptionPane.INFORMATION_MESSAGE);
	} catch (IOException io) {
            JOptionPane.showMessageDialog(null, "I/O Error: " + io.getMessage(), "Exception!", JOptionPane.ERROR_MESSAGE);
	} finally {
            if (output != null) {
                try {
                    output.close();
		} catch (IOException e) {
                    //e.printStackTrace();
		}
            }
 
	}
    }
}
