/*
 * Copyright 2011, Scott Douglass <scott@swdouglass.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * on the World Wide Web for more details:
 * http://www.fsf.org/licensing/licenses/gpl.txt
 */
package com.jmstoolkit.beans;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Properties;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Scott Douglass
 */
public class PropertyTableModel extends AbstractTableModel {

  /**
   *
   */
  public static final String PROP_DATA = "data";
  private Properties data = new Properties();
  private PropertyChangeSupport propertySupport;
  private String[] columnName = new String[]{
    "Property",
    "Value"
  };

  @Override
  public String getColumnName(int column) {
    return columnName[column];
  }

  /**
   *
   */
  public PropertyTableModel() {
    propertySupport = new PropertyChangeSupport(this);
  }

  /**
   *
   * @return
   */
  public Properties getData() {
    return data;
  }

  /**
   *
   * @param value
   */
  public void setData(Properties value) {
    Properties oldValue = data;
    data =  value;
    propertySupport.firePropertyChange(PROP_DATA, oldValue, data);
  }

  /**
   *
   * @param listener
   */
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    propertySupport.addPropertyChangeListener(listener);
  }

  /**
   *
   * @param listener
   */
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    propertySupport.removePropertyChangeListener(listener);
  }

  @Override
  public int getRowCount() {
    return this.data.size();
  }

  @Override
  public int getColumnCount() {
    return columnName.length;
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    String value = "";
    Object[] keys = this.data.keySet().toArray();
    if (columnIndex == 1) { // return the value
      value = (String) this.data.get((String) keys[rowIndex]);
    } else if (columnIndex == 0) { // return the key
      value = (String) keys[rowIndex];
    }
    return value;
  }
}
