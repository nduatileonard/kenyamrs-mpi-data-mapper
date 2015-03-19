/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.emr.schemas;

import com.emr.utilities.CSVLoader;
import com.emr.utilities.SqliteAddProcess;
import com.opencsv.CSVWriter;
import java.awt.event.ActionEvent;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JDesktopPane;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import java.util.Random;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JToggleButton;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

/**
 * A JInternalFrame form for mapping source and destination columns
 * @author LEONARD NDUATI
 */
public class EditMappingsForm extends javax.swing.JInternalFrame {
    Connection mpiConn;
    Connection emrConn;
    
    DefaultTableModel model;
    List selected_columns;
    String destinationTable;
    String sourceQuery;
    String relations;
    List sourceTables;
    ButtonGroup grp;
    String query;
    String insertQuery;
    String selectQuery;
    List columnsToBeMapped;
    DestinationTables parent;
    
    String [] destinationColumns=null;
    /**
     * Constructor
     * @param mpiConn {@link Connection} Connection object for the MPI Database
     * @param emrConn {@link Connection} Connection object for the EMR Database
     * @param selected_columns {@link List} List of the selected source columns
     * @param destinationTable {@link String} The destination table
     * @param sourceQuery {@link String} The query for getting the data to be moved
     * @param sourceTables {@link List} List of source tables
     * @param relations {@link String} Relationships between the source tables
     */
    public EditMappingsForm(Connection mpiConn,Connection emrConn,List selected_columns,String destinationTable,String sourceQuery,List sourceTables,String relations,DestinationTables parent) {
        this.mpiConn=mpiConn;
        this.emrConn=emrConn;
        this.selected_columns=selected_columns;
        this.destinationTable=destinationTable;
        this.sourceQuery=sourceQuery;
        this.sourceTables=sourceTables;
        this.relations=relations;
        this.parent=parent;
        columnsToBeMapped=new ArrayList();
        query="";
        insertQuery="";
        selectQuery="";
        model=new DefaultTableModel(new Object[]{"Source","Destination","Apply Mappings?"}, selected_columns.size());
        
        
        initComponents();
        this.setClosable(true);
        
        ComboBoxTableCellEditor sourceColumns = new ComboBoxTableCellEditor(selected_columns,new JComboBox());
        ComboBoxTableCellEditor destinationColumnsEditor = new ComboBoxTableCellEditor(getTableColumns(destinationTable),new JComboBox());
        mappingsTable.getColumnModel().getColumn(0).setCellEditor(sourceColumns);
        mappingsTable.getColumnModel().getColumn(1).setCellEditor(destinationColumnsEditor);
        /*Action foreignKeysMap = new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent e)
            {
                JTable table = (JTable)e.getSource();
                int modelRow = Integer.valueOf( e.getActionCommand() );
                String foreignKeysTable=(String)mappingsTable.getModel().getValueAt(modelRow, 0);
                if(foreignKeysTable==null || "".equals(foreignKeysTable)){
                    JOptionPane.showMessageDialog(null, "Column Mappings not defined", "Empty Column Mappings", JOptionPane.ERROR_MESSAGE);
                }else{
                    String tablename=foreignKeysTable.substring(0, foreignKeysTable.lastIndexOf("."));
                    String column=foreignKeysTable.substring(foreignKeysTable.lastIndexOf(".") + 1,foreignKeysTable.length());
                    System.out.println("Table: " + tablename + ", Column: " + column);
                    //open form for mapping the source foreign table to the destination foreign table
                    JDesktopPane desktopPane = getDesktopPane();
                    ForeignDataMover frm=new ForeignDataMover(emrConn,mpiConn,foreignKeysTable);
                    desktopPane.add(frm);
                    frm.setVisible(true);
                    frm.setSize(400, 400);
                    frm.setLocation(120,60);
                    frm.moveToFront();
                    btnMoveData.setEnabled(false);
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
                            btnMoveData.setEnabled(true);
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
        };
        ButtonColumn buttonColumn = new ButtonColumn(mappingsTable, foreignKeysMap, 2);*/
        CheckboxColumn checkboxColumn=new CheckboxColumn(mappingsTable, 2);
        grp=new ButtonGroup();
        grp.add(radioAppendRows);
        grp.add(radioDeleteRows);
        
    }
    /**
     * Method for getting a tables' columns
     * @param tableName {@link String} The table name
     * @return {@link List} List of the table's columns
     */
    private List getTableColumns(String tableName){
        List tableColumns=new ArrayList();
        try {
            DatabaseMetaData dbmd = mpiConn.getMetaData();
            ResultSet rs=dbmd.getColumns(null, null, tableName, "%");
            while (rs.next()) {
                String colName = rs.getString(4);
                tableColumns.add(colName);
            }
        }catch (SQLException e) {
            
            String stacktrace=org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(e);
            JOptionPane.showMessageDialog(this, "Could not fetch Tables for the KenyaEMR Database. Error Details: " + stacktrace, "Table Names Error", JOptionPane.ERROR_MESSAGE);
        }
        return tableColumns;
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
        mappingsTable = new javax.swing.JTable();
        radioDeleteRows = new javax.swing.JRadioButton();
        radioAppendRows = new javax.swing.JRadioButton();
        jSeparator1 = new javax.swing.JSeparator();
        dbProgressBar = new javax.swing.JProgressBar();
        lblUpdateText = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtProgress = new javax.swing.JTextArea();
        jToolBar1 = new javax.swing.JToolBar();
        btnMoveData = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        btnCancel = new javax.swing.JButton();

        setTitle("Edit Mappings");

        mappingsTable.setModel(model);
        jScrollPane1.setViewportView(mappingsTable);

        radioDeleteRows.setText("Delete Rows in Destination Table");

        radioAppendRows.setSelected(true);
        radioAppendRows.setText("Append Rows to the destination Table");

        txtProgress.setColumns(20);
        txtProgress.setRows(5);
        jScrollPane2.setViewportView(txtProgress);

        jToolBar1.setRollover(true);

        btnMoveData.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/emr/icons/OK.png"))); // NOI18N
        btnMoveData.setText("Move Data");
        btnMoveData.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMoveDataActionPerformed(evt);
            }
        });
        jToolBar1.add(btnMoveData);
        jToolBar1.add(jSeparator2);

        btnCancel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/emr/icons/cancel.png"))); // NOI18N
        btnCancel.setText("Cancel");
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });
        jToolBar1.add(btnCancel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(radioAppendRows)
                    .addComponent(radioDeleteRows))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblUpdateText, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(dbProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING)
            .addComponent(jScrollPane2)
            .addComponent(jToolBar1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(radioDeleteRows)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(radioAppendRows)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblUpdateText))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dbProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    /**
     * Handles click event for the Move Data button
     * <br />
     * Uses the {@link SwingWorker} object {@link DBUpdater} to move the data
     * @param evt {@link ActionEvent}
     */
    private void btnMoveDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMoveDataActionPerformed
        String emrDbName=getDatabaseName(emrConn);
        String mpidbName=getDatabaseName(mpiConn);
        
        //Construct insert query
        insertQuery="insert ignore into " + mpidbName + "." + destinationTable + "(";
        selectQuery="select ";
        //get source & destination columns
        mappingsTable.editingStopped(null);//force editing to stop
        Object[][] columns=getTableData(mappingsTable);
        int length=0; //hack: get length of non empty columns
        for(Object[] row: columns){
            if(row[0]!=null && row[1]!=null){
                length++;
            }
        }
        int counter=1;
        String sourceCol="";
        String destinationCol="";
        boolean applyMapping=false;
        destinationColumns=new String[length];
        for(Object[] row: columns){
            if(row[0]!=null && row[1]!=null){
                sourceCol=(String)row[0];
                destinationCol=(String)row[1];
                if(row[2]!=null){
                    applyMapping=(boolean)row[2];
                    if(applyMapping){
                        columnsToBeMapped.add(sourceCol);
                    }
                }
                if(counter==length){//no comma at the end
                    destinationColumns[counter-1]=destinationCol;
                    selectQuery +=row[0]; //source column
                    insertQuery +=row[1]; //destination column
                }else{
                    destinationColumns[counter-1]=destinationCol;
                    selectQuery +=row[0] + ","; //source column
                    insertQuery +=row[1] + ","; //destination column
                }
            }
            counter++;
        }
        insertQuery += ")";
        //add source tables to the select query
        selectQuery +=" from ";
        int cnt=1;
        int tablescount=sourceTables.size();
        for(Object table:sourceTables){
             String tablename=(String)table;
             if(cnt==tablescount){
                 selectQuery +=emrDbName + "." + tablename;
             }else{
                 selectQuery +=emrDbName + "." + tablename + ","; 
             }
             cnt++;
        }
        //add where clause to select query
        selectQuery +=relations;
        query="";
        //check if delete rows is checked
        if(radioDeleteRows.isSelected()){
            query="delete from " + destinationTable + ";";
        }
        query +=insertQuery + selectQuery;
        //System.out.println(query);
        btnMoveData.setEnabled(false);
        btnCancel.setEnabled(false);
        lblUpdateText.setText("<html><b color='red'>Moving Data</b></html>");
        dbProgressBar.setIndeterminate(true);
        //execute the query
        new DBUpdater().execute();
    }//GEN-LAST:event_btnMoveDataActionPerformed
    /**
     * Handles click event for the cancel button
     * @param evt {@link ActionEvent}
     */
    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        this.dispose();
        parent.closeForm();
    }//GEN-LAST:event_btnCancelActionPerformed
    /**
     * Method for scrapping a table's data into a multidimensional array
     * @param table {@link JTable} The table whose data is to be fetched
     * @return {@link Object[][]} Multidimensional array containing the table's data
     */
    public Object[][] getTableData (JTable table) {
        DefaultTableModel dtm = (DefaultTableModel) table.getModel();
        int nRow = dtm.getRowCount(), nCol = dtm.getColumnCount();
        Object[][] tableData = new Object[nRow][nCol];
        for (int i = 0 ; i < nRow ; i++)
            for (int j = 0 ; j < nCol ; j++){
                
                if(dtm.getValueAt(i, j)!=null){
                    tableData[i][j] = dtm.getValueAt(i,j);
                }
            }
        return tableData;
    }
    /**
     * Private {@link SwingWorker} class to move data in a separate thread
     * <br />
     * Querries the data from the source and copies it to a temporary CSV file while applying any defined data mappings
     * <br />
     * Then reads the CSV file into the destination
     * <br />
     * Optionally, saves the process to an SQLite db, for future use.
     * <br />
     * Uses the library OpenCSV to read/write csv files.
     */
    private class DBUpdater extends SwingWorker<Boolean, String>{
        String error_msg="";
        @Override
        protected Boolean doInBackground() throws Exception {
            publish("Data Migration Started...");
            setProgress(1);
            PreparedStatement ps=null;
            ResultSet rs = null;
            Random randomGenerator = new Random();
            int randomInt = randomGenerator.nextInt(100);
            String filename="temp" + randomInt + ".csv";
            String csv = "temp/" + filename;
            publish("Creating temporary CSV file...");
            setProgress(10);
            CSVWriter writer = new CSVWriter(new FileWriter(csv));
            Path path = FileSystems.getDefault().getPath("temp", filename);
            try {
                //get source data and dump it to a csv file
                ps = emrConn.prepareStatement(selectQuery);
                rs = ps.executeQuery();
                publish("Writing data from Source to the CSV file...");
                setProgress(30);
                writer.writeAll(rs, true);
                writer.close();
                //read csv file and load it to destination table
                CSVLoader loader = new CSVLoader(mpiConn);
                publish("Moving data from CSV to destination database (might take a while)...");
                setProgress(90);
                loader.loadCSV(csv, destinationTable, radioDeleteRows.isSelected(),destinationColumns,columnsToBeMapped); //automatically moves data to the supplied table
                publish("A little housekeeping...");
                setProgress(100);
                Files.delete(path); //delete csv after everything is complete
            } catch (Exception e) {
                error_msg=org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(e);
                publish(error_msg);
                return Boolean.FALSE;
            }finally {
                try{
                    if(ps!=null)
                        ps.close();
                    if(rs!=null)
                        rs.close();
                }catch(SQLException s){
                    s.printStackTrace();
                }
            }
            return Boolean.TRUE;
        }
        @Override
        protected void process(List< String> chunks) {
          for (final String string : chunks) {
            txtProgress.append(string);
            txtProgress.append("\n");
          }
        }
        protected void done() {
            Boolean success=null;
            try {
                success=get();
            } catch (InterruptedException ex) {
                Logger.getLogger(EditMappingsForm.class.getName()).log(Level.SEVERE, null, ex);
                //error_msg=org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(ex);
                JOptionPane.showMessageDialog(null, "An error stopped the move operation. Error detail(s): " + ex.getMessage(), "Data could not be moved.", JOptionPane.ERROR_MESSAGE);
                success=false;
            } catch (ExecutionException ex) {
                Logger.getLogger(EditMappingsForm.class.getName()).log(Level.SEVERE, null, ex);

                //error_msg=org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(ex);
                JOptionPane.showMessageDialog(null, "An error stopped the move operation. Error detail(s): " + ex.getMessage(), "Data could not be moved.", JOptionPane.ERROR_MESSAGE);
                success=false;
            }
            btnCancel.setEnabled(true);
            lblUpdateText.setText("<html><b color='green'>Done!</b></html>");
            dbProgressBar.setIndeterminate(false);
            if(success==true){
                //show Window with moved records, but first ask user if they want to save the process.
                int saveProcedure = JOptionPane.showConfirmDialog(null, 
                                      "Successfully moved data from KenyaEMR to the MPI database. Save process for future reference?", 
                                      "Success", 
                                      JOptionPane.YES_NO_OPTION); 
                if (saveProcedure == JOptionPane.YES_OPTION) {
                    //save the query, selectQuery, insertQuery
                    //prompt user for the name/description of the process
                    String processName=JOptionPane.showInputDialog(null, "Enter a name for the Data move process", "Name");
                    String description=JOptionPane.showInputDialog(null, "Enter a short description", "Description");
                    
                    //save columns to be mapped
                    StringBuilder buffer = new StringBuilder();
                    boolean processedFirst = false;
                    String columnsToBeMappedString = null;

                    try{
                        for(Object record: columnsToBeMapped){
                            String column=(String)record;
                            if(processedFirst)
                                buffer.append(",");
                            buffer.append(column);
                            processedFirst = true;
                        }
                        columnsToBeMappedString = buffer.toString();
                    }finally{
                        buffer = null;
                    }
                    
                    dbProgressBar.setIndeterminate(true);
                    lblUpdateText.setText("Saving Process for future reference");
                    new SqliteAddProcess(processName, description, selectQuery, destinationTable,radioDeleteRows.isSelected(),destinationColumns,columnsToBeMappedString, dbProgressBar, lblUpdateText).execute();
                }
                //now show the report window
                JDesktopPane desktopPane = getDesktopPane();
                
                MovedDataReport frm=new MovedDataReport(emrConn,selectQuery);
                desktopPane.add(frm);
                frm.setVisible(true);
                frm.setSize(500, 350);
                frm.setLocation(120,60);
                frm.moveToFront();
            }else{
                JOptionPane.showMessageDialog(null, "An error stopped the move operation. " + error_msg, "Data could not be moved.", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    /**
     * Returns the name of a database from a Connection object
     * @param con {@link Connection} The connection object
     * @return {@link String} The database name
     */
    private String getDatabaseName(Connection con){
        String dbName="";
        try{
            String url=con.getMetaData().getURL();
            dbName=url.substring(url.lastIndexOf("/") + 1, url.length());
        } catch (SQLException ex) {
            Logger.getLogger(EditMappingsForm.class.getName()).log(Level.SEVERE, null, ex);
        }
        return dbName;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnMoveData;
    private javax.swing.JProgressBar dbProgressBar;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JLabel lblUpdateText;
    private javax.swing.JTable mappingsTable;
    private javax.swing.JRadioButton radioAppendRows;
    private javax.swing.JRadioButton radioDeleteRows;
    private javax.swing.JTextArea txtProgress;
    // End of variables declaration//GEN-END:variables
}
