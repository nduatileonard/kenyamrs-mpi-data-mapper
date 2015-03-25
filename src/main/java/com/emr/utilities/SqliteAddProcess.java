/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.emr.utilities;

import javax.swing.SwingWorker;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteStatement;
import com.emr.schemas.EditMappingsForm;
import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import org.apache.commons.lang3.StringUtils;
/**
 * A {@link SwingWorker} class to save processes to the SQLite database
 * @author LEONARD NDUATI
 */
public class SqliteAddProcess extends SwingWorker<Boolean, Object> {

    String name;
    String description;
    Boolean truncateFirst;
    String destinationTable;
    String dbName;
    String selectQuery;
    String [] destinationColumns;
    String destinationCols="";
    String columnsToBeMappedString;
    JProgressBar jp;
    JLabel lbl;
    String error_msg="";
    /**
     * Constructor
     * @param name Name of the process
     * @param description Description
     * @param selectQuery The select Query used to fetch data from the emr database
     * @param destinationTable The destination table in the mpi database
     * @param truncateFirst Whether to truncate the destination table first
     * @param destinationColumns List of columns to be copied into
     * @param jp The progress bar
     * @param lbl Label to be updated on completion
     */
    public SqliteAddProcess(String name,String description,String selectQuery,String destinationTable,Boolean truncateFirst,String [] destinationColumns,String columnsToBeMappedString,JProgressBar jp,JLabel lbl,String dbName){
        this.name=name;
        this.description=description;
        this.selectQuery=selectQuery;
        this.destinationTable=destinationTable;
        this.truncateFirst=truncateFirst;
        this.destinationColumns=destinationColumns;
        this.columnsToBeMappedString=columnsToBeMappedString;
        this.jp=jp;
        this.lbl=lbl;
        this.dbName=dbName;
        
        destinationCols=StringUtils.join(destinationColumns, "|");
    }
    @Override
    protected Boolean doInBackground() throws Exception {
        SQLiteConnection db=null;
        SQLiteStatement st=null;
        try{
            File file=new File("sqlite/db");
            if(!file.exists()){
                file.createNewFile();
            }
            db=new SQLiteConnection(file);
            db.open(true);
            
            //db.exec("drop table procedures");
            db.exec("create table if not exists procedures(name varchar(100),description text,selectQry text,destinationTable varchar(100),truncateFirst varchar(5),destinationColumns text,columnsToBeMapped text,dbName varchar(100))");
            st=db.prepare("insert into procedures(name,description,selectQry,destinationTable,truncateFirst,destinationColumns,columnsToBeMapped,dbName) values(?,?,?,?,?,?,?,?)");
            st.bind(1, name);
            st.bind(2, description);
            st.bind(3, selectQuery);
            st.bind(4, destinationTable);
            st.bind(5, truncateFirst.toString());
            st.bind(6, destinationCols);
            st.bind(7, columnsToBeMappedString);
            st.bind(8, dbName);
            st.step();
        }catch(Exception e){
            error_msg=org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(e);
            return Boolean.FALSE;
        }finally{
            if(st!=null)
                st.dispose();
            if(db!=null)
                db.dispose();
        }
        return Boolean.TRUE;
    }
    protected void done() {
        Boolean success=null;
            try {
                success=get();
            } catch (InterruptedException ex) {
                Logger.getLogger(EditMappingsForm.class.getName()).log(Level.SEVERE, null, ex);
                success=false;
            } catch (ExecutionException ex) {
                Logger.getLogger(EditMappingsForm.class.getName()).log(Level.SEVERE, null, ex);
                success=false;
            }
           if(success){
               JOptionPane.showMessageDialog(null, "Successfully saved process", "Success", JOptionPane.INFORMATION_MESSAGE);
           }else{
               JOptionPane.showMessageDialog(null, "Could not save process. Error details: " + error_msg, "Failed", JOptionPane.ERROR_MESSAGE);
           }
        jp.setIndeterminate(false);
        lbl.setText("<html><b color='green'>Done!</b></html>");
    }
}
