/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.emr.utilities;

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
        SQLiteConnection db=null;
        SQLiteStatement st=null;
        Vector data = new Vector();
        Vector columns = new Vector();
        columns.add("Name");
        columns.add("Description");
        columns.add("SelectQry");
        columns.add("DestinationTable");
        columns.add("TruncateFirst");
        columns.add("DestinationColumns");
        columns.add("ColumnsToBeMapped");
        columns.add("Delete?");
        
        try{
            File file=new File("sqlite/db");
            if(!file.exists()){
                file.createNewFile();
            }
            db=new SQLiteConnection(file);
            db.open(true);
            st = db.prepare("SELECT name,description,selectQry,destinationTable,truncateFirst,destinationColumns,columnsToBeMapped FROM procedures");
            Vector row;
            while (st.step()) {
                row = new Vector(5);
                row.add(st.columnString(0));
                row.add(st.columnString(1));
                row.add(st.columnString(2));
                row.add(st.columnString(3));
                row.add(st.columnString(4));
                row.add(st.columnString(5));
                row.add(st.columnString(6));
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
