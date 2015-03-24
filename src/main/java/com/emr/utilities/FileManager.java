/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.emr.utilities;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import javax.swing.JOptionPane;

/**
 * Class to manage opening/saving of files (properties)
 * @author LEONARD NDUATI
 */
public class FileManager {
    Properties prop = new Properties(); //Properties file for storing the settings
    OutputStream output = null; //Required for writing to file
    InputStream input = null; //Required for reading from file
    /**
     * Empty constructor
     */
    public FileManager(){
        
    }
    /**
     * Method to get saved connection settings
     * @param filename {@link String} The properties filename
     * @param prefix {@link String} The prefix used for the fields in the settings file
     * @return {@link String[]} The connection settings
     */
    public String[] getConnectionSettings(String filename,String prefix){
        String [] connectionProperties=null;
        try{
            input = new FileInputStream("config/" + filename);
            // load a properties file
            prop.load(input);
            connectionProperties=new String[prop.size()];
            if(prop.size()>1){
                connectionProperties[0]= prop.getProperty(prefix + "_servername");
                connectionProperties[1]= prop.getProperty(prefix + "_port");
                connectionProperties[2]= prop.getProperty(prefix + "_url");
                connectionProperties[3]= prop.getProperty(prefix + "_dbname");
                connectionProperties[4]= prop.getProperty(prefix + "_dbuser");
                connectionProperties[5]= prop.getProperty(prefix + "_dbpassword");
            }
        }catch (IOException io) {
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
        return connectionProperties;
    }
    /**
     * Save connection settings to a properties file
     * @param fileName {@link String} Properties filename
     * @param prefix {@link String} Prefix used on fields
     * @param servername {@link String} Mysql server name
     * @param port {@link String} Mysql Server port
     * @param url {@link String} Full connection url
     * @param dbName {@link String} Database Name
     * @param userName {@link String} Username 
     * @param password {@link String} Password
     */
    public void saveSettings(String fileName,String prefix,String servername,String port,String url,String dbName,String userName,String password){
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
