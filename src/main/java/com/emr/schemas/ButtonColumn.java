/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.emr.schemas;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteStatement;
import com.emr.utilities.SQLiteGetProcesses;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

/**
 *  The ButtonColumn class provides a renderer and an editor that looks like a
 *  JButton. The renderer and editor will then be used for a specified column
 *  in the table. The TableModel will contain the String to be displayed on
 *  the button.
 *
 *  The button can be invoked by a mouse click or by pressing the space bar
 *  when the cell has focus. Optionally a mnemonic can be set to invoke the
 *  button. When the button is invoked the provided Action is invoked. The
 *  source of the Action will be the table. The action command will contain
 *  the model row number of the button that was clicked.
 *
 */
public class ButtonColumn extends AbstractCellEditor
	implements TableCellRenderer, TableCellEditor, ActionListener, MouseListener
{
	private JTable table;
	private Action action;
	private int mnemonic;
	private Border originalBorder;
	private Border focusBorder;

	private JButton renderButton;
	private JButton editButton;
	private Object editorValue;
	private boolean isButtonColumnEditor;
        private final String btnText;
        String name;
        String desc;

	/**
	 *  Create the ButtonColumn to be used as a renderer and editor. The
	 *  renderer and editor will automatically be installed on the TableColumn
	 *  of the specified column.
	 *
	 *  @param table the table containing the button renderer/editor
	 *  @param action the Action to be invoked when the button is invoked
	 *  @param column the column to which the button renderer/editor is added
	 */
	public ButtonColumn(JTable table, Action action, int column,String btnText)
	{
		this.table = table;
		this.action = action;
                this.btnText=btnText;

		renderButton = new JButton();
		editButton = new JButton();
		editButton.setFocusPainted( false );
		editButton.addActionListener( this );
		originalBorder = editButton.getBorder();
		setFocusBorder( new LineBorder(Color.BLUE) );

		TableColumnModel columnModel = table.getColumnModel();
		columnModel.getColumn(column).setCellRenderer( this );
		columnModel.getColumn(column).setCellEditor( this );
		table.addMouseListener( this );
	}


	/**
	 *  Get foreground color of the button when the cell has focus
	 *
	 *  @return the foreground color
	 */
	public Border getFocusBorder()
	{
		return focusBorder;
	}

	/**
	 *  The foreground color of the button when the cell has focus
	 *
	 *  @param focusBorder the foreground color
	 */
	public void setFocusBorder(Border focusBorder)
	{
		this.focusBorder = focusBorder;
		editButton.setBorder( focusBorder );
	}

	public int getMnemonic()
	{
		return mnemonic;
	}

	/**
	 *  The mnemonic to activate the button when the cell has focus
	 *
	 *  @param mnemonic the mnemonic
	 */
	public void setMnemonic(int mnemonic)
	{
		this.mnemonic = mnemonic;
		renderButton.setMnemonic(mnemonic);
		editButton.setMnemonic(mnemonic);
	}

	@Override
	public Component getTableCellEditorComponent(
		JTable table, Object value, boolean isSelected, int row, int column)
	{
		editButton.setText(btnText);

		this.editorValue = value;
		return editButton;
	}

	@Override
	public Object getCellEditorValue()
	{
		return editorValue;
	}

//
//  Implement TableCellRenderer interface
//
	public Component getTableCellRendererComponent(
		JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
	{
		if (isSelected)
		{
			renderButton.setForeground(table.getSelectionForeground());
	 		renderButton.setBackground(table.getSelectionBackground());
		}
		else
		{
			renderButton.setForeground(table.getForeground());
			renderButton.setBackground(UIManager.getColor("Button.background"));
		}

		if (hasFocus)
		{
			renderButton.setBorder( focusBorder );
		}
		else
		{
			renderButton.setBorder( originalBorder );
		}

//		renderButton.setText( (value == null) ? "" : value.toString() );
		renderButton.setText(btnText);

		return renderButton;
	}

//
//  Implement ActionListener interface
//
	/*
	 *	The button has been pressed. Stop editing and invoke the custom Action
	 */
	public void actionPerformed(ActionEvent e)
	{
                int row = table.convertRowIndexToModel( table.getEditingRow() );
		fireEditingStopped();

		//  Invoke the Action

		ActionEvent event = new ActionEvent(
			table,
			ActionEvent.ACTION_PERFORMED,
			"" + row);
		action.actionPerformed(event);
	}

//
//  Implement MouseListener interface
//
	/*
	 *  When the mouse is pressed the editor is invoked. If you then then drag
	 *  the mouse to another cell before releasing it, the editor is still
	 *  active. Make sure editing is stopped when the mouse is released.
	 */
    public void mousePressed(MouseEvent e)
    {
    	if (table.isEditing()
		&&  table.getCellEditor() == this)
			isButtonColumnEditor = true;
    }

    public void mouseReleased(MouseEvent e)
    {
    	if (isButtonColumnEditor
    	&&  table.isEditing())
    		table.getCellEditor().stopCellEditing();

		isButtonColumnEditor = false;
    }

    public void mouseClicked(MouseEvent e) {
        int row = table.getSelectedRow();
        int col = table.getSelectedColumn();
        
        if(col==7){
            //Ask user if they want to delete..
            int deleteProcess = JOptionPane.showConfirmDialog(null, 
                                      "Are you sure you want to delete this process?", 
                                      "Delete Process", 
                                      JOptionPane.YES_NO_OPTION); 
                if (deleteProcess == JOptionPane.YES_OPTION) {
                     name=(String)table.getModel().getValueAt(row, 0);
                     desc=(String)table.getModel().getValueAt(row, 1);
                     new DeleteProcess().execute();
                     
                }
        }
    }
	public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    private class DeleteProcess extends SwingWorker<Boolean, String>{
        String error_msg="";
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
                st=db.prepare("delete from procedures where name=? and description=?");
                st.bind(1, name);
                st.bind(2, desc);
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
               JOptionPane.showMessageDialog(null, "Successfully deleted process", "Success", JOptionPane.INFORMATION_MESSAGE);
               //repopulate the table
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
                                                table.setModel(sp.get());
                                                table.getColumnModel().getColumn(0).setMaxWidth(300);
                                                //hide irrelevant columns
                                                table.getColumnModel().getColumn(2).setMinWidth(0);
                                                table.getColumnModel().getColumn(2).setMaxWidth(0);
                                                table.getColumnModel().getColumn(3).setMinWidth(0);
                                                table.getColumnModel().getColumn(3).setMaxWidth(0);
                                                table.getColumnModel().getColumn(4).setMinWidth(0);
                                                table.getColumnModel().getColumn(4).setMaxWidth(0);
                                                table.getColumnModel().getColumn(5).setMinWidth(0);
                                                table.getColumnModel().getColumn(5).setMaxWidth(0);
                                                table.getColumnModel().getColumn(6).setMinWidth(0);
                                                table.getColumnModel().getColumn(6).setMaxWidth(0);

                                                ButtonColumn buttonColumn = new ButtonColumn(table, new AbstractAction() {

                                                    @Override
                                                    public void actionPerformed(ActionEvent e) {
                                                        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                                                    }
                                                }, 7,"Delete");
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
           }else{
               JOptionPane.showMessageDialog(null, "Could not save process. Error details: " + error_msg, "Failed", JOptionPane.ERROR_MESSAGE);
           }
        
    }
    }
    
}
