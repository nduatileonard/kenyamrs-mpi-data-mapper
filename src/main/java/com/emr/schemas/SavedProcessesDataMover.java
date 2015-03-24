/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.emr.schemas;

import com.emr.utilities.CSVLoader;
import com.emr.utilities.DatabaseManager;
import com.emr.utilities.FileManager;
import com.emr.utilities.SQLiteGetProcesses;
import com.opencsv.CSVWriter;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileWriter;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDesktopPane;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import org.apache.commons.lang3.StringUtils;

/**
 * A JInternalFrame form for fetching the saved processes from the SQLite db and displaying the same
 * in a table.
 * @author LEONARD NDUATI
 */
public class SavedProcessesDataMover extends javax.swing.JInternalFrame {
    
    CustomTableModel model=new CustomTableModel();
    /**
     * Constructor
     */
    public SavedProcessesDataMover() {
        initComponents();
        this.setClosable(true);
        final Action processDelete = new AbstractAction(){
                                        @Override
                                        public void actionPerformed(ActionEvent e)
                                        {
                                            JTable table = (JTable)e.getSource();
                                            int modelRow = Integer.valueOf( e.getActionCommand() );
                                            String foreignKeysTable=(String)tblProcesses.getModel().getValueAt(modelRow, 0);
                                            if(foreignKeysTable==null || "".equals(foreignKeysTable)){
                                                JOptionPane.showMessageDialog(null, "Table is Empty", "Empty Table", JOptionPane.ERROR_MESSAGE);
                                            }else{
                                                
                                            }
                                        }

                                    };
        final SQLiteGetProcesses sp=new SQLiteGetProcesses();
        sp.addPropertyChangeListener(new PropertyChangeListener() {

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
                                    model=sp.get();
                                    tblProcesses.setModel(model);
                                    tblProcesses.getColumnModel().getColumn(0).setMaxWidth(300);
                                    //hide irrelevant columns
                                    tblProcesses.getColumnModel().getColumn(2).setMinWidth(0);
                                    tblProcesses.getColumnModel().getColumn(2).setMaxWidth(0);
                                    tblProcesses.getColumnModel().getColumn(3).setMinWidth(0);
                                    tblProcesses.getColumnModel().getColumn(3).setMaxWidth(0);
                                    tblProcesses.getColumnModel().getColumn(4).setMinWidth(0);
                                    tblProcesses.getColumnModel().getColumn(4).setMaxWidth(0);
                                    tblProcesses.getColumnModel().getColumn(5).setMinWidth(0);
                                    tblProcesses.getColumnModel().getColumn(5).setMaxWidth(0);
                                    tblProcesses.getColumnModel().getColumn(6).setMinWidth(0);
                                    tblProcesses.getColumnModel().getColumn(6).setMaxWidth(0);
                                    
                                    ButtonColumn buttonColumn = new ButtonColumn(tblProcesses, processDelete, 7,"Delete");
                                }catch (final CancellationException ex) {
                                    Logger.getLogger(SavedProcessesDataMover.class.getName()).log(Level.SEVERE, null, ex);
                                } catch (InterruptedException ex) {
                                    Logger.getLogger(SavedProcessesDataMover.class.getName()).log(Level.SEVERE, null, ex);
                                } catch (ExecutionException ex) {
                                    Logger.getLogger(SavedProcessesDataMover.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                
                            break;
                        }
                    break;
                }
            }
            
        });
        sp.execute();
        
        tblProcesses.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    JTable target = (JTable)e.getSource();
                    int rowIndex = target.getSelectedRow();
                    String selectQry=(String)target.getModel().getValueAt(rowIndex, 2);
                    String destinationTable=(String)target.getModel().getValueAt(rowIndex, 3);
                    String truncateFirst=(String)target.getModel().getValueAt(rowIndex, 4);
                    String destinationColumns=(String)target.getModel().getValueAt(rowIndex, 5);
                    String columnsToBeMapped=(String)target.getModel().getValueAt(rowIndex, 6);
                    lblUpdateText.setText("<html><b color='red'>Moving Data</b></html>");
                    dbProgressBar.setIndeterminate(true);
                    new DBUpdater(selectQry, destinationTable,truncateFirst,destinationColumns,columnsToBeMapped).execute();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void mouseExited(MouseEvent e) {
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        });
        
    }
    /**
     * Method for getting a saved connection object
     * @param propertiesFilename
     * @param prefix
     * @return 
     */
    private Connection getConnection(String propertiesFilename,String prefix){
        FileManager fileManager;
        DatabaseManager dbManager;
        Connection con = null;
        fileManager=new FileManager();
        String[] settings=fileManager.getConnectionSettings(propertiesFilename + ".properties",prefix);
        
        if(settings==null){
            //Connection settings not found
            JOptionPane.showMessageDialog(null, "Database Settings not found. Please set the connection settings for the " + prefix + " database first.", "KenyaEMR Database settings", JOptionPane.ERROR_MESSAGE);
            
        }else{
            if(settings.length<1){
                
                JOptionPane.showMessageDialog(null, "Database Settings not found. Please set the connection settings for the " + prefix + " database.", "KenyaEMR Database settings", JOptionPane.ERROR_MESSAGE);
                
            }else{
                //Connection settings are ok
                //We establish a connection
                dbManager=new DatabaseManager(settings[0], settings[1], settings[3], settings[4], settings[5]);
                con=dbManager.getConnection();
                if(con==null){
                    JOptionPane.showMessageDialog(null, "A connection to the " + prefix + " Database could not be established. Check your connection settings. Also, make sure the MySQL service is running.", "Connection Failed", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        return con;
    }
    /**
     * {@link SwingWorker} Class for performing the selected process. (Moving the data from the source database to the destination)
     */
    private class DBUpdater extends SwingWorker<Boolean, String>{
        private final String destinationTable;
        private String selectQuery;
        private final String truncateFirst;
        private final String destinationColumns;
        private final String columnsToBeMapped;
        Connection mpiConn;
        Connection emrConn;
        String error_msg="";
        public DBUpdater(String selectQuery,String destinationTable,String truncateFirst,String destinationColumns,String columnsToBeMapped) {
            this.destinationTable=destinationTable;
            this.selectQuery=selectQuery;
            this.truncateFirst=truncateFirst;
            this.destinationColumns=destinationColumns;
            this.columnsToBeMapped=columnsToBeMapped;
            mpiConn=getConnection("mpi_database", "mpi");
            emrConn=getConnection("emr_database", "emr");
        }
        
        @Override
        protected Boolean doInBackground() throws Exception {
            publish("Data Migration From Saved Process Started...");
            //run the query passed in
            PreparedStatement ps=null;
            ResultSet rs = null;
            Random randomGenerator = new Random();
            int randomInt = randomGenerator.nextInt(100);
            String filename="temp" + randomInt + ".csv";
            String csv = "temp/" + filename;
            publish("Creating temporary CSV file...");
            CSVWriter writer = new CSVWriter(new FileWriter(csv));
            Path path = FileSystems.getDefault().getPath("temp", filename);
            
            try {
                //get source data and dump it to a csv file
                
                ps = emrConn.prepareStatement(selectQuery);
                rs = ps.executeQuery();
                publish("Writing data from Source to the CSV file...");
                writer.writeAll(rs, true);
                writer.close();
                
                Boolean truncate;
                truncate = "true".equals(truncateFirst) || "TRUE".equals(truncateFirst) || "1".equals(truncateFirst);
                String[] columns=StringUtils.split(destinationColumns, "|");
                //read csv file and load it to destination table
                System.out.println("columnsToBeMapped: " + columnsToBeMapped);
                List<String> mapColumns = Arrays.asList(columnsToBeMapped.split("\\s*,\\s*"));
                publish("Moving data from CSV to destination database (might take a while)...");
                CSVLoader loader = new CSVLoader(mpiConn);
                loader.loadCSV(csv, destinationTable, truncate,columns,mapColumns); //automatically moves data to the supplied table
                publish("A little housekeeping...");
                Files.delete(path); //delete csv after everything
            } catch (SQLException e) {
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
        @Override
        protected void done() {
            Boolean success=null;
            try {
                success=get();
            } catch (InterruptedException | ExecutionException ex) {
                Logger.getLogger(EditMappingsForm.class.getName()).log(Level.SEVERE, null, ex);
                success=false;
            }
            lblUpdateText.setText("<html><b color='green'>Done!</b></html>");
            dbProgressBar.setIndeterminate(false);
            if(success==true){
                JOptionPane.showMessageDialog(null, "Successfully executed the process.", "Success", JOptionPane.INFORMATION_MESSAGE);
                //now show the report window
                JDesktopPane desktopPane = getDesktopPane();
                
                MovedDataReport frm=new MovedDataReport(emrConn,selectQuery);
                desktopPane.add(frm);
                frm.setVisible(true);
                frm.setSize(500, 350);
                frm.setLocation(120,60);
                frm.moveToFront();
            }else{
                JOptionPane.showMessageDialog(null, "An error stopped the move operation. Error details: " + error_msg, "Data could not be moved.", JOptionPane.ERROR_MESSAGE);
            }
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

        jScrollPane1 = new javax.swing.JScrollPane();
        tblProcesses = new javax.swing.JTable();
        lblUpdateText = new javax.swing.JLabel();
        dbProgressBar = new javax.swing.JProgressBar();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtProgress = new javax.swing.JTextArea();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle("Select Process");

        tblProcesses.setModel(model);
        jScrollPane1.setViewportView(tblProcesses);

        lblUpdateText.setText("<html><b color='green'>Double Click to run process</b></html>");

        txtProgress.setColumns(20);
        txtProgress.setRows(5);
        jScrollPane2.setViewportView(txtProgress);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblUpdateText, javax.swing.GroupLayout.DEFAULT_SIZE, 235, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dbProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 238, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addComponent(jScrollPane1)
            .addComponent(jScrollPane2)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(3, 3, 3)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblUpdateText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dbProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 261, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JProgressBar dbProgressBar;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblUpdateText;
    private javax.swing.JTable tblProcesses;
    private javax.swing.JTextArea txtProgress;
    // End of variables declaration//GEN-END:variables
}
