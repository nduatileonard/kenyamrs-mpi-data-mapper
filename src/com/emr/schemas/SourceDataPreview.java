/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.emr.schemas;

import com.emr.utilities.DatabaseManager;
import com.emr.utilities.FileManager;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.Connection;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker.StateValue;
import javax.swing.table.DefaultTableModel;

/**
 * A {@link JInternalFrame} form for previewing data before its copied to the destination
 * @author LEONARD NDUATI
 */
public class SourceDataPreview extends javax.swing.JInternalFrame {
    FileManager fileManager;
    DatabaseManager dbManager;
    Connection emrConn;
    String sql="";
    
    DefaultTableModel model=new DefaultTableModel();
    /**
     * Constructor
     * @param stmnt {@link String} The source query
     * @param selected_columns {@link List} List of selected columns
     */
    public SourceDataPreview(String stmnt,List selected_columns) {
        fileManager=null;
        dbManager=null;
        emrConn=null;
        this.sql=stmnt;
        
        //Create KenyaEMR DB connection
        fileManager=new FileManager();
        String[] settings=fileManager.getConnectionSettings("emr_database.properties","emr");
        if(settings==null){
            //Connection settings not found
            JOptionPane.showMessageDialog(null, "Database Settings not found. Please set the connection settings for the database first.", "KenyaEMR Database settings", JOptionPane.ERROR_MESSAGE);
            //Open KenyaEMRConnectionForm form
        }else{
            if(settings.length<1){
                JOptionPane.showMessageDialog(null, "Database Settings not found. Please set the connection settings for the database first.", "KenyaEMR Database settings", JOptionPane.ERROR_MESSAGE);
                //Open KenyaEMRConnectionForm form
            }else{
                //Connection settings are ok
                //We establish a connection
                dbManager=new DatabaseManager(settings[0], settings[1], settings[3], settings[4], settings[5]);
                emrConn=dbManager.getConnection();
                if(emrConn==null){
                    JOptionPane.showMessageDialog(this, "Test Connection Failed", "Connection Test", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        //get selected columns, and add them as Table headers
        /*Object[] tableColumns=new Object[selected_columns.size()];
        ArrayList<Object> temp = new ArrayList<Object>(Arrays.asList(tableColumns));
        
        for(Object column:selected_columns){
            System.out.println("column added");
             temp.add(column);
        }
        Object[][] data=new Object[1][selected_columns.size()];
        model=new DefaultTableModel(data, temp.toArray());*/
        
        DataLoadWorker dl=new DataLoadWorker(sourceDataPreviewTable, sql, emrConn);
        dl.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent event) {
                switch (event.getPropertyName()) {
                    case "progress":
                        System.out.println("Fetching data from db");
                        break;
                    case "state":
                        switch ((StateValue) event.getNewValue()) {
                            case DONE:
                                try {
                                    model=dl.get();
                                    sourceDataPreviewTable.setModel(model);
                                }catch (final CancellationException ex) {
                                    Logger.getLogger(SourceDataPreview.class.getName()).log(Level.SEVERE, null, ex);
                                } catch (InterruptedException ex) {
                                    Logger.getLogger(SourceDataPreview.class.getName()).log(Level.SEVERE, null, ex);
                                } catch (ExecutionException ex) {
                                    Logger.getLogger(SourceDataPreview.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                
                            break;
                        }
                    break;
                }
            }
            
        });
        dl.execute();
        
        initComponents();
        this.setClosable(true);
        sourceDataPreviewTable.setPreferredScrollableViewportSize(sourceDataPreviewTable.getPreferredSize());
        sourceDataPreviewTable.setFillsViewportHeight(true);
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        sourceDataPreviewTable = new javax.swing.JTable();

        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle("Source Data Preview");
        setPreferredSize(new java.awt.Dimension(500, 500));

        sourceDataPreviewTable.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED), javax.swing.BorderFactory.createEtchedBorder()));
        sourceDataPreviewTable.setModel(model);
        jScrollPane1.setViewportView(sourceDataPreviewTable);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 484, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 470, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable sourceDataPreviewTable;
    // End of variables declaration//GEN-END:variables
}
