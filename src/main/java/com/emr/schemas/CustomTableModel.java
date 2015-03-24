/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.emr.schemas;

import java.util.Vector;
import javax.swing.table.DefaultTableModel;

/**
 * A {@link DefaultTableModel} class that prevents editing of cells
 * @author LEONARD NDUATI
 */
public class CustomTableModel extends DefaultTableModel {
    /**
     * Constructor with no parameters.
     */
    public CustomTableModel(){
        super();
    }
    /**
     * Constructor with two parameters.
     * @param data {@link Vector} The data to be applied to the model.
     * @param columns {@link Vector} The columns for the model.
     */
    public CustomTableModel(Vector data, Vector columns) {
        super(data,columns);
    }
    /**
     * Prohibits editing of Table cells.
     * @param row {@link int} Rowindex
     * @param column {@link int} column index
     * @return {@link boolean} Whether the cells can be edited or not.
     */
    public boolean isCellEditable(int row, int column){  
        return false;  
    }
}
