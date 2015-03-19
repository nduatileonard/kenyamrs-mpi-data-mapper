/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.emr.schemas;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;

/**
 *
 * @author GIGOVIO
 */
public class ForeignDataMover extends javax.swing.JInternalFrame {
    Connection emrConn;
    Connection mpiConn;
    String sourceColumn;
    List sourceTables=new ArrayList();
    DefaultComboBoxModel sourceTablesModel=new DefaultComboBoxModel();
    List destTables=new ArrayList();
    DefaultComboBoxModel destTablesModel=new DefaultComboBoxModel();
    DefaultComboBoxModel sourcePrimaryFields=new DefaultComboBoxModel();
    DefaultComboBoxModel sourceUniqueFields=new DefaultComboBoxModel();
    DefaultComboBoxModel mpiPrimarycolumns=new DefaultComboBoxModel();
    DefaultComboBoxModel mpiUniquFields=new DefaultComboBoxModel();
    /**
     * Form for moving foreign data
     * @param emrConn Connection to the emr database
     * @param mpiConn Connection to the mpi database
     * @param sourceColumn The column that has the foreign key
     */
    public ForeignDataMover(Connection emrConn,Connection mpiConn,String sourceColumn) {
        this.emrConn=emrConn;
        this.mpiConn=mpiConn;
        this.sourceColumn=sourceColumn;
        sourceTables=getDatabaseTables(emrConn);
        sourceTablesModel = new DefaultComboBoxModel(sourceTables.toArray(new String[sourceTables.size()]));
        
        destTables=getDatabaseTables(mpiConn);
        destTablesModel = new DefaultComboBoxModel(destTables.toArray(new String[destTables.size()]));
        
        initComponents();
        txtSelectedCollumn.setText("<html><b color='green'>" + sourceColumn + "</b></html>");
        cmbForeignTables.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    String selectedTable = (String)e.getItem();
                    //populate primary table columns
                    List primaryColumns=getTableColumns(selectedTable,emrConn);
                    sourcePrimaryFields = new DefaultComboBoxModel(primaryColumns.toArray(new String[primaryColumns.size()]));
                    cmbPrimaryKey.setModel(sourcePrimaryFields);
                    sourceUniqueFields = new DefaultComboBoxModel(primaryColumns.toArray(new String[primaryColumns.size()]));
                    cmbUniquekey.setModel(sourceUniqueFields);
                }
            }
        });
        cmbMPITables.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    String selectedTable = (String)e.getItem();
                    //populate primary table columns
                    List columns=getTableColumns(selectedTable,mpiConn);
                    mpiPrimarycolumns = new DefaultComboBoxModel(columns.toArray(new String[columns.size()]));
                    cmbMPIPrimaryKey.setModel(mpiPrimarycolumns);
                    mpiUniquFields = new DefaultComboBoxModel(columns.toArray(new String[columns.size()]));
                    cmbMPIUniqueField.setModel(mpiUniquFields);
                }
            }
        });
    }
    private List getTableColumns(String tableName,Connection con){
        List columns=new ArrayList();
        try {
            DatabaseMetaData dbmd = con.getMetaData();
            ResultSet rs=dbmd.getColumns(null, null, tableName, "%");
            while (rs.next()) {
                String colName = rs.getString(4);
                columns.add(colName);
            }
        }catch (SQLException e) {
            String stacktrace=org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(e);
            JOptionPane.showMessageDialog(this, "Could not fetch Tables for the Database. Error Details: " + stacktrace, "Table Names Error", JOptionPane.ERROR_MESSAGE);
        }
        return columns;
    }
    private List getDatabaseTables(Connection con)
    {
        List tables=new ArrayList();
        try {

            DatabaseMetaData dbmd = con.getMetaData();
            String[] types = {"TABLE"};
            ResultSet rs = dbmd.getTables(null, null, "%", types);
            while (rs.next()) {
                //Add table name to Jlist
                tables.add(rs.getString("TABLE_NAME"));
            }
        }catch (SQLException e) {
            String stacktrace=org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(e);
            JOptionPane.showMessageDialog(this, "Could not fetch Tables for the KenyaEMR Database. Error Details: " + stacktrace, "Table Names Error", JOptionPane.ERROR_MESSAGE);
        }
        return tables;
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
        txtSelectedCollumn = new javax.swing.JLabel();
        cmbPrimaryKey = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        cmbForeignTables = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        cmbUniquekey = new javax.swing.JComboBox();
        jLabel5 = new javax.swing.JLabel();
        cmbMPITables = new javax.swing.JComboBox();
        jLabel6 = new javax.swing.JLabel();
        cmbMPIPrimaryKey = new javax.swing.JComboBox();
        jLabel7 = new javax.swing.JLabel();
        cmbMPIUniqueField = new javax.swing.JComboBox();
        jToolBar1 = new javax.swing.JToolBar();
        jButton1 = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        jButton2 = new javax.swing.JButton();

        setClosable(true);
        setIconifiable(true);
        setTitle("Foreign Data Mover");
        setToolTipText("");

        jLabel1.setText("Selected Column");

        cmbPrimaryKey.setModel(sourcePrimaryFields);

        jLabel2.setText("Foreign Table");

        jLabel3.setText("Foreign Table Primary Key");

        cmbForeignTables.setModel(sourceTablesModel);

        jLabel4.setText("Foreign Table Unique Field");

        cmbUniquekey.setModel(sourceUniqueFields);

        jLabel5.setText("MPI Foreign Table");

        cmbMPITables.setModel(destTablesModel);

        jLabel6.setText("MPI Foreign Table Primary Key");

        cmbMPIPrimaryKey.setModel(mpiPrimarycolumns);

        jLabel7.setText("MPI Foreign Table Unique field");

        cmbMPIUniqueField.setModel(mpiUniquFields);

        jToolBar1.setRollover(true);

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/emr/icons/save.png"))); // NOI18N
        jButton1.setText("Save");
        jButton1.setFocusable(false);
        jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jButton1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jButton1);
        jToolBar1.add(jSeparator1);

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/emr/icons/cancel.png"))); // NOI18N
        jButton2.setText("Cancel");
        jButton2.setFocusable(false);
        jButton2.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jButton2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(cmbPrimaryKey, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(cmbForeignTables, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(83, 83, 83))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addComponent(jLabel5)
                            .addComponent(jLabel6))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 9, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(cmbUniquekey, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(cmbMPITables, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(83, 83, 83))
                            .addGroup(layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(cmbMPIPrimaryKey, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addGap(18, 18, 18)
                                .addComponent(txtSelectedCollumn))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(cmbMPIUniqueField, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
            .addComponent(jToolBar1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(34, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtSelectedCollumn))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(cmbForeignTables, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(cmbPrimaryKey, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(cmbUniquekey, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(cmbMPITables, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cmbMPIPrimaryKey, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(cmbMPIUniqueField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(38, 38, 38)
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        this.dispose();
    }//GEN-LAST:event_jButton2ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox cmbForeignTables;
    private javax.swing.JComboBox cmbMPIPrimaryKey;
    private javax.swing.JComboBox cmbMPITables;
    private javax.swing.JComboBox cmbMPIUniqueField;
    private javax.swing.JComboBox cmbPrimaryKey;
    private javax.swing.JComboBox cmbUniquekey;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JLabel txtSelectedCollumn;
    // End of variables declaration//GEN-END:variables
}
