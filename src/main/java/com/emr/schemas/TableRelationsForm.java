/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.emr.schemas;

import com.emr.utilities.DatabaseManager;
import com.emr.utilities.FileManager;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JComboBox;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.table.DefaultTableModel;

/**
 * A {@link JInternalFrame} form for defining the relationships between the source tables, as well as the columns to be moved.
 * @author LEONARD NDUATI
 */
public class TableRelationsForm extends javax.swing.JInternalFrame {
    FileManager fileManager;
    DatabaseManager dbManager;
    Connection emrConn;
    
    DefaultTableModel model;
    DefaultTableModel columnsModel;
    List foreignTables=new ArrayList(); //tables
    List primaryColumns=new ArrayList(); //Columns of the selected primary table
    List foreignColumns=new ArrayList(); //Columns of the selected foreign table
    List all_columns =new ArrayList(); //All the columns of the tables selected in the previous step
    List selected_columns=new ArrayList(); //The selected columns
    List tables=new ArrayList();
    DefaultListModel<String> listModel=new DefaultListModel<String>();;
    
    JComboBox combo1;
    JComboBox combo2;
    JComboBox combo3;
    JComboBox combo4;
    
    String sql="";
    String where_clause="";
    
    SchemerMapper parent;
    
    /**
     * Constructor
     * @param tables {@link List} List of source tables
     */
    public TableRelationsForm(List tables,SchemerMapper parent) {
        fileManager=null;
        dbManager=null;
        emrConn=null;
        this.parent=parent;
        this.tables=tables; //source tables
        
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
                    JOptionPane.showMessageDialog(null, "Test Connection Failed", "Connection Test", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        
        //get all columns and add them to the columns list
        for(Object table:tables){
             String tablename=(String)table;
             populateTableColumnsToList(tablename);
        }
        
        model=new DefaultTableModel(new Object[]{"Primary Table","Column","Reference Table","Foreign Column"}, 10);
        
        
        initComponents();
        this.setClosable(true);
        foreignTables=tables;
        combo1=new JComboBox();//Combobox for the primary tables
        combo2=new JComboBox();
        combo3=new JComboBox();
        combo4=new JComboBox();
        combo1.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent event) {
                if (event.getStateChange() == ItemEvent.SELECTED) {
                    String selectedTable = (String)event.getItem();
                    //populate primary table columns
                    primaryColumns=getTableColumns(selectedTable);
                    DefaultComboBoxModel combo2_model = new DefaultComboBoxModel(primaryColumns.toArray(new String[primaryColumns.size()]));
                    JComboBox comboBox = new JComboBox();
                    comboBox.setModel(combo2_model);
                    relationsTable.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(comboBox));
                    
                }
            }
        });
        
        
        combo3.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent event) {
                if (event.getStateChange() == ItemEvent.SELECTED) {
                    String selectedTable = (String)event.getItem();
                    //populate foreign table columns
                    foreignColumns=getTableColumns(selectedTable);
                    DefaultComboBoxModel combo3_model = new DefaultComboBoxModel(foreignColumns.toArray(new String[foreignColumns.size()]));
                    JComboBox comboBox = new JComboBox();
                    comboBox.setModel(combo3_model);
                    relationsTable.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(comboBox));
                }
            }
        });
        ComboBoxTableCellEditor primaryTableEditor = new ComboBoxTableCellEditor(tables,combo1);
        ComboBoxTableCellEditor primaryTableColumns = new ComboBoxTableCellEditor(primaryColumns,combo2);
        
        ComboBoxTableCellEditor foreignTableEditor = new ComboBoxTableCellEditor(foreignTables,combo3);//TODO: remove selected primary table from list
        
        ComboBoxTableCellEditor foreignTableColumns = new ComboBoxTableCellEditor(foreignColumns,combo4);
        relationsTable.getColumnModel().getColumn(0).setCellEditor(primaryTableEditor);
        relationsTable.getColumnModel().getColumn(1).setCellEditor(primaryTableColumns);
        relationsTable.getColumnModel().getColumn(2).setCellEditor(foreignTableEditor);
        relationsTable.getColumnModel().getColumn(3).setCellEditor(foreignTableColumns);
        
        columnsList.setCellRenderer(new CheckboxListCellRenderer());
        SourceTablesListener listSelectionListener=new SourceTablesListener(new JTextArea(),selected_columns);
        columnsList.addListSelectionListener(listSelectionListener);
        columnsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        columnsList.setSelectionModel(new DefaultListSelectionModel() {
            @Override
            public void setSelectionInterval(int index0, int index1) {
                if (isSelectedIndex(index0))
                    super.removeSelectionInterval(index0, index1);
                else
                    super.addSelectionInterval(index0, index1);
            }
        });
    }
    
    /**
     * Method to get all the columns in a table
     * @param tableName {@link String} Table name
     * @return {@link List} List of the table's columns
     */
    private List getTableColumns(String tableName){
        List columns=new ArrayList();
        try {
            DatabaseMetaData dbmd = emrConn.getMetaData();
            ResultSet rs=dbmd.getColumns(null, null, tableName, "%");
            while (rs.next()) {
                String colName = rs.getString(4);
                columns.add(colName);
            }
        }catch (SQLException e) {
            String stacktrace=org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(e);
            JOptionPane.showMessageDialog(this, "Could not fetch Tables for the KenyaEMR Database. Error Details: " + stacktrace, "Table Names Error", JOptionPane.ERROR_MESSAGE);
        }
        return columns;
    }
    public void closeForm(){
        this.dispose();
        parent.closeForm();
    }
    /**
     * Method to populate a table's columns to a {@link ListModel}
     * @param tableName {@link String} Table name
     */
    private void populateTableColumnsToList(String tableName){
        try {
            DatabaseMetaData dbmd = emrConn.getMetaData();
            ResultSet rs=dbmd.getColumns(null, null, tableName, "%");
            while (rs.next()) {
                String colName = rs.getString(4);
                listModel.addElement(tableName + "." + colName);
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

        jLabel3 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        relationsTable = new javax.swing.JTable();
        jLabel2 = new javax.swing.JLabel();
        btnPreview = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        btnNext = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();
        btnClearSelection = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        columnsList = new javax.swing.JList();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle("Table Relations");

        jLabel3.setText("<html><b color='blue'>Define Table Relations Here.</b> <b color='red'>Incomplete relations will not be inlcuded in the results.</b></html>");

        relationsTable.setModel(model);
        jScrollPane1.setViewportView(relationsTable);

        jLabel2.setText("<html><b color='blue'>Columns to be moved.</b></html>");

        btnPreview.setIcon(new javax.swing.ImageIcon(getClass().getResource("/preview.png"), ""));
        btnPreview.setText("Preview");
        btnPreview.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPreviewActionPerformed(evt);
            }
        });

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

        columnsList.setModel(listModel);
        jScrollPane4.setViewportView(columnsList);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 559, Short.MAX_VALUE)
                    .addComponent(jScrollPane4)
                    .addComponent(jLabel3)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnPreview)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btnNext)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnClearSelection)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(31, 31, 31)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 176, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(4, 4, 4)
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnPreview, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(btnNext)
                                .addComponent(btnClearSelection))
                            .addComponent(jSeparator2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(15, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    /**
     * Method to construct an SQL query from the selected source tables, columns and their relationships
     * @return {@link String} the source SQL query.
     */
    private String getSourceQueryString(){
        String str;
        //get source columns and construct source query
        int length=selected_columns.size();
        
        str ="select ";
        int counter=1;
        for(Object column:selected_columns){
             String columnName=(String)column;
             if(counter==length){
                 str +=columnName;
             }else{
                str +=columnName + ","; 
             }
             counter++;
        }
        //add source tables to the query
        str +=" from ";
        int cnt=1;
        int tablescount=tables.size();
        for(Object table:tables){
             String tablename=(String)table;
             if(cnt==tablescount){
                 str +=tablename;
             }else{
                str +=tablename + ","; 
             }
             cnt++;
        }
        //get the relations
        where_clause =" where 1=1";
        String dbName=getDatabaseName(emrConn);
        Object[][] relations=getTableData(relationsTable);
        
        int relationssize=0;
        for(Object[] row: relations){
            if(row[0]!=null && row[1]!=null && row[2]!=null && row[3]!=null){
                where_clause += " and " + dbName + "." + row[0] + "." + row[1] + "=" + dbName + "." + row[2] + "." + row[3];
                relationssize++;
            }
        }
        
        if(tables.size()>1){
            //user specified 2 tables, relations shd be the size - 1
            if(relationssize<(tables.size() - 1)){
                return "";
            }
        }
        
        
        
        str +=where_clause;
        return str;
    }
    /**
     * Get database name from a connection
     * @param con {@link Connection} the Connection object
     * @return {@link String} the database name
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
    /**
     * Handles preview button (Data preview)
     * @param evt {@link ActionEvent}
     */
    private void btnPreviewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPreviewActionPerformed
        int length=selected_columns.size();
        if(length<1){
             JOptionPane.showMessageDialog(this, "Please select at least one column from the source columns list.", "No column(s) selected.", JOptionPane.ERROR_MESSAGE);
             
        }else{
            sql=getSourceQueryString();
            if("".equals(sql)){
                JOptionPane.showMessageDialog(this, "You selected " + tables.size() + " source tables without matching relationships. Please define relations between all the tables, or go back and refine/change your tables selection.", "Table Relations missing", JOptionPane.ERROR_MESSAGE);
            }else{
                JDesktopPane desktopPane = getDesktopPane();
                SourceDataPreview frm=new SourceDataPreview(sql, selected_columns);
                desktopPane.add(frm);
                frm.setVisible(true);
                frm.setSize(500, 500);
                frm.setLocation(120,60);
                frm.moveToFront();
            }
            
        }
        
    }//GEN-LAST:event_btnPreviewActionPerformed
    /**
     * Handles click event for the next button
     * Opens {@link DestinationTables} form for the user to select the destination table
     * @param evt {@link ActionEvent}
     */
    private void btnNextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextActionPerformed
        int length=selected_columns.size();
        if(length<1){
             JOptionPane.showMessageDialog(this, "Please select at least one column from the source columns list.", "No column(s) selected.", JOptionPane.ERROR_MESSAGE);
             
        }else{
            
            sql=getSourceQueryString();
            if("".equals(sql)){
                JOptionPane.showMessageDialog(this, "You selected " + tables.size() + " source tables without matching relationships. Please define relations between all the tables, or go back and refine/change your tables selection.", "Table Relations missing", JOptionPane.ERROR_MESSAGE);
            }else{
                btnNext.setEnabled(false);
                btnClearSelection.setEnabled(false);
                //open form for choosing the destination table
                JDesktopPane desktopPane = getDesktopPane();
                DestinationTables frm=new DestinationTables(sql,selected_columns,tables,where_clause,emrConn,this);
                desktopPane.add(frm);
                frm.setVisible(true);

                frm.setSize(900, 500);
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
                        btnNext.setEnabled(true);
                        btnClearSelection.setEnabled(true);
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
    }//GEN-LAST:event_btnNextActionPerformed
    /**
     * Clear selection
     * @param evt {@link ActionEvent}
     */
    private void btnClearSelectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearSelectionActionPerformed
        relationsTable.clearSelection();
        columnsList.clearSelection();
    }//GEN-LAST:event_btnClearSelectionActionPerformed
    /**
     * Method to get a table's contents
     * @param table {@link JTable} The table
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

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClearSelection;
    private javax.swing.JButton btnNext;
    private javax.swing.JButton btnPreview;
    private javax.swing.JList columnsList;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTable relationsTable;
    // End of variables declaration//GEN-END:variables
}
