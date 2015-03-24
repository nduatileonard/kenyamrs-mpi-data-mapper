/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.emr.schemas;

import com.emr.utilities.DatabaseManager;
import com.emr.utilities.FileManager;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JDesktopPane;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

/**
 * A JInternalFrame form for the destination tables (MPI)
 * Only one destination table can be selected.
 * @author LEONARD NDUATI
 */
public class DestinationTables extends javax.swing.JInternalFrame {
    FileManager fileManager;
    DatabaseManager dbManager;
    Connection mpiConn;
    
    String sourceQuery;
    List selected_columns;
    List sourceTables;
    String relations;
    Connection emrConn;
    TableRelationsForm parent;
    
    List listOfTables = new ArrayList();
    DefaultListModel<String> listModel;
    /**
     * Creates a new DestinationTables form.
     * @param sourceQuery {@link String} SQL Query for getting the source data.
     * @param selected_columns {@link List} List containing the selected source columns
     * @param sourceTables {@link List} List containing the source tables
     * @param relations {@link String} Relationship between the source tables
     * @param emrConn {@link Connection} KenyaEMR Database Connection object
     */
    public DestinationTables(String sourceQuery,List selected_columns,List sourceTables,String relations,Connection emrConn,TableRelationsForm parent) {
        fileManager=null;
        dbManager=null;
        mpiConn=null;
        
        this.parent=parent;
        this.sourceQuery=sourceQuery;
        this.selected_columns=selected_columns;
        this.sourceTables=sourceTables;
        this.relations=relations;
        this.emrConn=emrConn;
        
        listModel = new DefaultListModel<String>();
        initComponents();
        this.setClosable(true);
        
        SourceTablesListener listSelectionListener=new SourceTablesListener(new JTextArea(),listOfTables);
        
        destinationTablesList.setCellRenderer(new CheckboxListCellRenderer());
        destinationTablesList.addListSelectionListener(listSelectionListener);
        destinationTablesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        //Create KenyaEMR DB connection
        fileManager=new FileManager();
        String[] settings=fileManager.getConnectionSettings("mpi_database.properties","mpi");
        if(settings==null){
            //Connection settings not found
            //Open MPIConnectionForm
            JOptionPane.showMessageDialog(null, "Database Settings not found. Please set the connection settings for the database first.", "MPI Database settings", JOptionPane.ERROR_MESSAGE);
            
            
        }else{
            if(settings.length<1){
                //Open MPIConnectionForm
                JOptionPane.showMessageDialog(null, "Database Settings not found. Please set the connection settings for the database first.", "MPI Database settings", JOptionPane.ERROR_MESSAGE);
            
            }else{
                //Connection settings are ok
                //We establish a connection
                dbManager=new DatabaseManager(settings[0], settings[1], settings[3], settings[4], settings[5]);
                mpiConn=dbManager.getConnection();
                if(mpiConn==null){
                    JOptionPane.showMessageDialog(null, "Test Connection Failed", "Connection Test", JOptionPane.ERROR_MESSAGE);
                }else{
                    //get emr schema
                    getDatabaseMetaData();
                }
            }
        }
    }
    public void closeForm(){
        this.dispose();
        parent.closeForm();
    }
    /**
     * Method for getting the MPI database's tables
     */
    public final void getDatabaseMetaData()
    {
        try {

            DatabaseMetaData dbmd = mpiConn.getMetaData();
            String[] types = {"TABLE"};
            ResultSet rs = dbmd.getTables(null, null, "%", types);
            while (rs.next()) {
                //Add table name to Jlist
                listModel.addElement(rs.getString("TABLE_NAME"));
            }
        }catch (SQLException e) {
            String stacktrace=org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(e);
            JOptionPane.showMessageDialog(this, "Could not fetch Tables for the KenyaEMR Database. Error Details: " + stacktrace, "Table Names Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        destinationTablesList = new javax.swing.JList();
        btnNext = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        btnClearSelection = new javax.swing.JButton();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle("Select Destination Table (MPI)");

        jLabel1.setText("<html><b color='blue'>Select Destination Table(<i color='green'>Where to copy data to</i>)</b></html>");

        destinationTablesList.setModel(listModel);
        jScrollPane1.setViewportView(destinationTablesList);

        btnNext.setIcon(new javax.swing.ImageIcon(getClass().getResource("/next.png"), ""));
        btnNext.setText("Next");
        btnNext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNextActionPerformed(evt);
            }
        });

        btnClearSelection.setIcon(new javax.swing.ImageIcon(getClass().getResource("/clear.png"), ""));
        btnClearSelection.setText("Clear Selection");
        btnClearSelection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearSelectionActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addComponent(btnNext)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnClearSelection)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 241, Short.MAX_VALUE))
                    .addComponent(jLabel1))
                .addContainerGap())
            .addComponent(jScrollPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 270, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(22, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnClearSelection)
                        .addComponent(btnNext))))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    /**
     * Handles click event for the Next button
     * <br />
     * Opens the EditMappings form for mapping of source and destination columns.
     * @param evt {@link ActionEvent}
     */
    private void btnNextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextActionPerformed
        //JOptionPane.showMessageDialog(this, "Under Construction", "Under Construction", JOptionPane.INFORMATION_MESSAGE);
        //get selected destination table
        if(destinationTablesList.getSelectedIndex()<0){
            JOptionPane.showMessageDialog(this, "Please select a destination table from the list.", "Destination table not selected", JOptionPane.ERROR_MESSAGE);
             
        }else{
            btnNext.setEnabled(false);
            btnClearSelection.setEnabled(false);
            String destinationTable=(String)destinationTablesList.getSelectedValue();
            JDesktopPane desktopPane = getDesktopPane();
            EditMappingsForm frm =new EditMappingsForm(mpiConn,emrConn, selected_columns, destinationTable, sourceQuery,sourceTables,relations,this);
            desktopPane.add(frm);
            frm.setVisible(true);
            frm.setSize(600, 480);
            frm.setLocation(300,140);
            frm.moveToFront();
            final DestinationTables currentForm=this;
            frm.addInternalFrameListener(new InternalFrameListener() {

                @Override
                public void internalFrameOpened(InternalFrameEvent e) {
                    //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }

                @Override
                public void internalFrameClosing(InternalFrameEvent e) {
                    //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }

                @Override
                public void internalFrameClosed(InternalFrameEvent e) {
                    btnNext.setEnabled(true);
                    btnClearSelection.setEnabled(true);
                    currentForm.closeForm();
                }

                @Override
                public void internalFrameIconified(InternalFrameEvent e) {
                    //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }

                @Override
                public void internalFrameDeiconified(InternalFrameEvent e) {
                    //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }

                @Override
                public void internalFrameActivated(InternalFrameEvent e) {
                    //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }

                @Override
                public void internalFrameDeactivated(InternalFrameEvent e) {
                    //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }
            });
        }
        
        
    }//GEN-LAST:event_btnNextActionPerformed
    /**
     * Handles click event for the Clear Selection Button
     * @param evt {@link ActionEvent}
     */
    private void btnClearSelectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearSelectionActionPerformed
        destinationTablesList.clearSelection();
    }//GEN-LAST:event_btnClearSelectionActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClearSelection;
    private javax.swing.JButton btnNext;
    private javax.swing.JList destinationTablesList;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    // End of variables declaration//GEN-END:variables
}
