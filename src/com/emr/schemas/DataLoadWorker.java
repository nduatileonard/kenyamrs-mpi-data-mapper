/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.emr.schemas;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Vector;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

/**
 * A {@link SwingWorker} for populating a {@link DefaultTableModel} to be used in a {@link JTable}
 * @author LEONARD NDUATI
 */
public class DataLoadWorker extends SwingWorker<DefaultTableModel, TableModel> {

    private final JTable table;
    private final String stmt;
    private final Connection con;
    /**
     * Constructor
     * @param table {@link JTable}
     * @param stmnt {@link String} SQL statement to be executed for the model's data
     * @param con {@link Connection} Connection to be used to execute the SQL statement
     */
    public DataLoadWorker(JTable table,String stmnt,Connection con) {
        this.table = table;
        this.stmt=stmnt;
        this.con=con;
    }
    /**
     * Actual execution of thread
     * @return {@link DefaultTableModel} A populated model.
     * @throws Exception Any exceptions that might be thrown
     */
    @Override
    protected DefaultTableModel doInBackground() throws Exception {
        Vector data = new Vector();
        Vector columns = new Vector();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = con.prepareStatement(stmt);
            rs = ps.executeQuery();
            ResultSetMetaData md = rs.getMetaData();
            int columnCount = md.getColumnCount();
            //store column names  
            for (int i = 1; i <= columnCount; i++) {
                columns.add(md.getColumnName(i));
            }

            columns.ensureCapacity(columnCount);

            Vector row;
            while (rs.next()) {

                row = new Vector(columnCount);
                for (int i = 1; i <= columnCount; i++) {
                    row.add(rs.getString(i));
                }
                data.add(row);

                //Debugging                
            }

            // List.setModel(tableModel);

        } finally {
            try {
                ps.close();
            } catch (Exception e) {
            }
            try {
                rs.close();
            } catch (Exception e) {
                
            }
        }

        DefaultTableModel tableModel = new DefaultTableModel(data, columns);
        
        return tableModel;
    }

}
