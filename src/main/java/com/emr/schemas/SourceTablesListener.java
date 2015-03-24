/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.emr.schemas;

import java.text.MessageFormat;
import java.util.List;
import javax.swing.JList;
import javax.swing.JTextArea;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author GIGOVIO
 */
public class SourceTablesListener implements ListSelectionListener {
        private final MessageFormat elementFormat;
	private final String delim;
        private JTextArea txtArea;
        List listOfTables;
        public SourceTablesListener(JTextArea txtArea,List listOfTables ){
            elementFormat=new MessageFormat("{0}");
            delim="\n";
            this.txtArea=txtArea;
            this.listOfTables=listOfTables;
        }
       @Override
	public void valueChanged(ListSelectionEvent e) {
		JList<?> list = (JList<?>) e.getSource();
		ListModel<?> model = list.getModel();

		ListSelectionModel listSelectionModel = list.getSelectionModel();

		int minSelectionIndex = listSelectionModel.getMinSelectionIndex();
		int maxSelectionIndex = listSelectionModel.getMaxSelectionIndex();

		StringBuilder textBuilder = new StringBuilder();
                listOfTables.clear();
		for (int i = minSelectionIndex; i <= maxSelectionIndex; i++) {
			if (listSelectionModel.isSelectedIndex(i)) {
				Object elementAt = model.getElementAt(i);
                                if(!listOfTables.contains(elementAt))
                                listOfTables.add(elementAt);
				formatElement(elementAt, textBuilder, i);
			}
		}
                
                txtArea.setText(textBuilder.toString());
	}
        private void formatElement(Object element, StringBuilder textBuilder, int i) {
		String formatted = elementFormat.format(new Object[] { element });
		textBuilder.append(formatted);
		textBuilder.append(delim);
	}
}
