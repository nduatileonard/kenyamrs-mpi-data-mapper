/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.emr.utilities;

import com.almworks.sqlite4java.SQLite;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteStatement;
import com.emr.schemas.CustomTableModel;
import java.io.File;
import java.util.Vector;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.table.TableModel;

/**
 * A {@link SwingWorker} class to get saved processes from the SQLite database
 * @author LEONARD NDUATI
 */
public class SQLiteGetProcesses extends SwingWorker<CustomTableModel, TableModel> {
    String error_msg="";
    
    @Override
    protected CustomTableModel doInBackground() throws Exception {
        SQLite.setLibraryPath("lib");
        SQLiteConnection db=null;
        SQLiteStatement st=null;
        SQLiteStatement st2=null;
        SQLiteStatement st3=null;
        Vector data = new Vector();
        Vector columns = new Vector();
        columns.add("Name");
        columns.add("Description");
        columns.add("SelectQry");
        columns.add("DestinationTable");
        columns.add("TruncateFirst");
        columns.add("DestinationColumns");
        columns.add("ColumnsToBeMapped");
        columns.add("DB Name");
        columns.add("Delete?");
        
        try{
            File file=new File("sqlite/db");
            if(!file.exists()){
                file.createNewFile();
            }
            db=new SQLiteConnection(file);
            db.open(true);
            //due to addition of new column in procedures table, we'll use a temporary table to check if we should drop the table first
            db.exec("create table if not exists proc_check(hasColumn char(1))");
            st2=db.prepare("select hasColumn from proc_check");
            boolean empty_procs=true;
            while(st2.step()){
                //has column is set
                empty_procs=false;
                
            }
            if(empty_procs==true){
                st3=db.prepare("insert into proc_check(hasColumn) values(?)");
                st3.bind(1, "1");
                st3.step();
                db.exec("drop table procedures");
                
            }
            
            db.exec("create table if not exists procedures(name varchar(100),description text,selectQry text,destinationTable varchar(100),truncateFirst varchar(5),destinationColumns text,columnsToBeMapped text,dbName varchar(100))");
            st = db.prepare("SELECT name,description,selectQry,destinationTable,truncateFirst,destinationColumns,columnsToBeMapped,dbName FROM procedures");
            Vector row;
            while (st.step()) {
                row = new Vector(8);
                row.add(st.columnString(0));
                row.add(st.columnString(1));
                row.add(st.columnString(2));
                row.add(st.columnString(3));
                row.add(st.columnString(4));
                row.add(st.columnString(5));
                row.add(st.columnString(6));
                row.add(st.columnString(7));
                row.add("");
                data.add(row);
            }
        }catch(Exception e){
            System.err.println("Data Mover Error: " + e.getMessage());
            String stacktrace=org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(e);
             
            error_msg=stacktrace;
        }finally{
            if(st!=null)
                st.dispose();
            if(db!=null)
                db.dispose();
        }
        CustomTableModel tableModel = new CustomTableModel(data, columns);
        
        return tableModel;
    }
    @Override
    protected void done(){
        if(!"".equals(error_msg)){
            JOptionPane.showMessageDialog(null, "Could not fetch saved procedures. Error details: " + error_msg, "Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}
