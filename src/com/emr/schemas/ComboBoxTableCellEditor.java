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
import javax.swing.table.TableCellEditor;

/**
 * {@link TableCellEditor} that uses {@link JComboBox} to render a list as the editor for a {@link JTable}
 * @author LEONARD NDUATI
 */
public class ComboBoxTableCellEditor extends AbstractCellEditor implements TableCellEditor {
        private JComboBox editor;
        private List<String> masterValues;
        
        /**
         * 
         * @param masterValues {@link List} A list of values to populate the {@link JComboBox}
         * @param editor {@link JComboBox} The ComboBox to be used as the editor
         */
        public ComboBoxTableCellEditor(List<String> masterValues,JComboBox editor) {
            this.editor = editor;
            this.masterValues = masterValues;
        }
        /**
         * Method to get a cell's value
         * @return {@link Object} The cell's value
         */
        @Override
        public Object getCellEditorValue() {
            return editor.getSelectedItem();
        }
        /**
         * Overriden method to construct the cell editor component
         * @param table {@link JTable}
         * @param value {@link Object}
         * @param isSelected {@link boolean}
         * @param row {@link int}
         * @param column {@link int}
         * @return {@link Component}
         */
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {

            DefaultComboBoxModel model = new DefaultComboBoxModel(masterValues.toArray(new String[masterValues.size()]));
            for (int index = 0; index < table.getRowCount(); index++) {
                if (index != row) {
                    String cellValue = (String) table.getValueAt(index, 0);
                    model.removeElement(cellValue);
                }
            }

            editor.setModel(model);
            editor.setSelectedItem(value);

            return editor;

        }
}
