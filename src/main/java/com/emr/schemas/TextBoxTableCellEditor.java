/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.emr.schemas;

import java.awt.Component;
import java.util.List;
import javax.swing.AbstractCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;

/**
 * A {@link AbstractCellEditor} that renders a {@link JTextField} as the editor of Table cells.
 * @author LEONARD NDUATI
 */
public class TextBoxTableCellEditor extends AbstractCellEditor implements TableCellEditor {
    private JTextField editor;
    public TextBoxTableCellEditor(JTextField editor){
        this.editor = editor;
    }
    @Override
    public Object getCellEditorValue() {
        return editor.getText();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        
            return editor;
    }
    
}
