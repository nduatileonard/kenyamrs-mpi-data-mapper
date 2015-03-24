/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.emr.schemas;

import java.awt.Component;
import java.awt.Container;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

/**
 * A {@link JInternalFrame} for for selecting the source tables
 * @author LEONARD NDUATI
 */
public class SchemerMapper extends javax.swing.JInternalFrame {
    Connection emrConn;
    //JList sourceTablesList;
    DefaultListModel<String> listModel;
    List listOfTables = new ArrayList();
    String emrDbName;
    /**
     * Constructor
     * @param emrConn {@link Connection} Connection to the EMR database
     * @param emrDbName {@link String} Database name
     */
    public SchemerMapper(Connection emrConn,String emrDbName) {
        this.emrConn=emrConn;
        this.emrDbName=emrDbName;
        listModel = new DefaultListModel<String>();
        initComponents();
        this.setClosable(true);
        
        SourceTablesListener listSelectionListener=new SourceTablesListener(txtSelectedTables,listOfTables);
        
        sourceTablesList.setCellRenderer(new CheckboxListCellRenderer());
        sourceTablesList.addListSelectionListener(listSelectionListener);
        sourceTablesList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        sourceTablesList.setSelectionModel(new DefaultListSelectionModel() {
            @Override
            public void setSelectionInterval(int index0, int index1) {
                if (isSelectedIndex(index0))
                    super.removeSelectionInterval(index0, index1);
                else
                    super.addSelectionInterval(index0, index1);
            }
        });
        
        //getDatabaseMetaData();
        final ListUpdater lu=new ListUpdater();
        lu.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent event) {
                switch (event.getPropertyName()) {
                    case "progress":
                        System.out.println("Fetching data from db");
                        break;
                    case "state":
                        switch ((SwingWorker.StateValue) event.getNewValue()) {
                            case DONE:
                                try {
                                    listModel=lu.get();
                                    sourceTablesList.setModel(listModel);
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
        lu.execute();
    }
    private class ListUpdater extends SwingWorker<DefaultListModel, Object>{

        @Override
        protected DefaultListModel doInBackground() throws Exception {
            DefaultListModel<String> lm=new DefaultListModel<String>();
            try {

                DatabaseMetaData dbmd = emrConn.getMetaData();
                String[] types = {"TABLE"};
                ResultSet rs = dbmd.getTables(null, null, "%", types);
                while (rs.next()) {
                    //Add table name to Jlist
                    lm.addElement(rs.getString("TABLE_NAME"));
                }
            }catch (SQLException e) {
                String stacktrace=org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(e);
                JOptionPane.showMessageDialog(null, "Could not fetch Tables for the KenyaEMR Database. Error Details: " + stacktrace, "Table Names Error", JOptionPane.ERROR_MESSAGE);
            }
            return lm;
        }
        
    }
    /**
     * Method for getting the database's tables and populating the List with the same.
     */
    public final void getDatabaseMetaData()
    {
        try {

            DatabaseMetaData dbmd = emrConn.getMetaData();
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
    public void closeForm(){
        this.dispose();
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
        sourceTablesList = new javax.swing.JList<String>();
        sourceTablesList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        sourceTablesList.setVisibleRowCount(4);
        jScrollPane2 = new javax.swing.JScrollPane();
        txtSelectedTables = new javax.swing.JTextArea();
        jLabel2 = new javax.swing.JLabel();
        btnClearSel = new javax.swing.JButton();
        btnNextScreen = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();

        setClosable(true);
        setIconifiable(true);
        setResizable(true);
        setTitle("Map Schemas");

        jLabel1.setText("<html><b color='blue'>Select Table(s) to copy data from (<i color='green'>Ctrl-Click to select multiple tables</i>)</b></html>");

        sourceTablesList.setModel(listModel);
        jScrollPane1.setViewportView(sourceTablesList);

        txtSelectedTables.setColumns(20);
        txtSelectedTables.setRows(5);
        jScrollPane2.setViewportView(txtSelectedTables);

        jLabel2.setText("<html><b color='blue'>Selected Tables</b></html>");

        btnClearSel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/clear.png"), ""));
        btnClearSel.setText("Clear Selection");
        btnClearSel.setFocusable(false);
        btnClearSel.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        btnClearSel.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnClearSel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearSelActionPerformed(evt);
            }
        });

        btnNextScreen.setIcon(new javax.swing.ImageIcon(getClass().getResource("/next.png"), ""));
        btnNextScreen.setText("Next");
        btnNextScreen.setFocusable(false);
        btnNextScreen.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        btnNextScreen.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnNextScreen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNextScreenActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 29, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnNextScreen)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnClearSel)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(btnClearSel)
                        .addComponent(btnNextScreen))
                    .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(31, 31, 31))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    /**
     * Handles click event for the Next button
     * Opens {@link TableRelationsForm} for the user to define the relationships between the selected tables.
     * @param evt {@link ActionEvent}
     */
    private void btnNextScreenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextScreenActionPerformed
        if(listOfTables.isEmpty()){
            JOptionPane.showMessageDialog(this, "Please select source tables from thee list", "No selection was made", JOptionPane.ERROR_MESSAGE);
        }else{
            Container container = SwingUtilities.getAncestorOfClass(JDesktopPane.class, (Component)evt.getSource());
               
            if (container != null)
                {
                    btnClearSel.setEnabled(false);
                    btnNextScreen.setEnabled(false);
                    JDesktopPane desktopPane = getDesktopPane();
                    TableRelationsForm frm=new TableRelationsForm(listOfTables,this);
                    desktopPane.add(frm);
                    frm.setVisible(true);
                    frm.setSize(800, 500);
                    frm.setLocation(120,60);
                    frm.moveToFront();
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
                            btnClearSel.setEnabled(true);
                            btnNextScreen.setEnabled(true);
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
        }
        
    }//GEN-LAST:event_btnNextScreenActionPerformed

    private void btnClearSelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearSelActionPerformed
        listOfTables.clear();
        txtSelectedTables.setText("");
        sourceTablesList.clearSelection();
    }//GEN-LAST:event_btnClearSelActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClearSel;
    private javax.swing.JButton btnNextScreen;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JList<String> sourceTablesList;
    private javax.swing.JTextArea txtSelectedTables;
    // End of variables declaration//GEN-END:variables
}
