/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.emr.utilities;

import com.almworks.sqlite4java.SQLite;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteStatement;

import java.io.File;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

/**
 * A {@link SwingWorker} class to get saved mappings from the SQLite database
 * @author LEONARD NDUATI
 */
public class SQliteDataLoadWorker extends SwingWorker<DefaultTableModel, TableModel> {
    private final JTable table;
    private final String stmt;
    
    public SQliteDataLoadWorker(JTable table,String stmnt){
        SQLite.setLibraryPath("lib");
    	this.table = table;
        this.stmt=stmnt;
    }
    @Override
    protected DefaultTableModel doInBackground() throws Exception {
        SQLiteConnection db=null;
        SQLiteStatement st=null;
        Vector data = new Vector();
        Vector columns = new Vector();
        columns.add("Value");
        columns.add("Mapping");
        try{
            File file=new File("sqlite/db");
            if(!file.exists()){
                file.createNewFile();
            }
            db=new SQLiteConnection(file);
            db.open(true);
            db.exec("create table if not exists mappings(sourceValue text,dataMapping text)");
            st = db.prepare(stmt);
            Vector row;
            while (st.step()) {
                row = new Vector(2);
                row.add(st.columnString(0));
                row.add(st.columnString(1));
                data.add(row);
            }
            //Add a few rows for adding new mappings
            int i;
            for(i=0;i<5;i++){
                row = new Vector(2);
                row.add("");
                row.add("");
                data.add(row);
            }
        }catch(Exception e){
            System.err.println("Data Mover Error: " + e.getMessage());
            e.printStackTrace();
        }finally{
            if(st!=null)
                st.dispose();
            if(db!=null)
                db.dispose();
        }
        DefaultTableModel tableModel = new DefaultTableModel(data, columns);
        return tableModel;
    }
    
}
