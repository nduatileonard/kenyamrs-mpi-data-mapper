/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.emr.schemas;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.AbstractCellEditor;
import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

/**
 * The CheckboxColumn class provides a renderer and an editor that looks like a
 *  JCheckbox. The renderer and editor will then be used for a specified column
 *  in the table. The TableModel will contain the String to be displayed on
 *  the checkbox.
 *
 * @author LEONARD NDUATI
 */
public class CheckboxColumn extends AbstractCellEditor
	implements TableCellRenderer, TableCellEditor, ActionListener, MouseListener {
    private final JTable table;
    private JCheckBox chkbox;
    /**
	 *  Create the ButtonColumn to be used as a renderer and editor. The
	 *  renderer and editor will automatically be installed on the TableColumn
	 *  of the specified column.
	 *
	 *  @param table the table containing the button renderer/editor
	 *  @param column the column to which the button renderer/editor is added
	 */
    public CheckboxColumn(JTable table, int column){
        this.table = table;
        chkbox=new JCheckBox("", false);
        TableColumnModel columnModel = table.getColumnModel();
        columnModel.getColumn(column).setCellRenderer( this );
        columnModel.getColumn(column).setCellEditor( this );
    }
    @Override
    public Object getCellEditorValue() {
        return chkbox.isSelected();
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        chkbox.setSelected(value==null ? false : (boolean)value); 
        return chkbox;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        chkbox.setSelected(value==null ? false : (boolean)value); 
        return chkbox;
    }
    /*
	 *	In editing mode. Checkbox has been checked.
	 */
    @Override
    public void actionPerformed(ActionEvent e) {
        int row = table.convertRowIndexToModel( table.getEditingRow() );
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        
    }

    @Override
    public void mousePressed(MouseEvent e) {
        
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        
    }

    @Override
    public void mouseExited(MouseEvent e) {
        
    }
    
}
