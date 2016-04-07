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

import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.Properties;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;
import javax.swing.table.AbstractTableModel;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

/**
 *
 * @author Scott Douglass
 */
public class MessageTableModel extends AbstractTableModel implements MessageListener {
  private static final Logger LOGGER = Logger.getLogger(MessageTableModel.class.getName());
  private static final SimpleDateFormat DATE_TIME = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  /**
   *
   */
  public static final String PROP_DATA = "data";
  private List<MessageTableRecord> data = new ArrayList<MessageTableRecord>();
  private String[] columnName = new String[]{
    "Timestamp",
    "Message ID",
    "Correlation ID",
    "Reply To",
    "Priority",
    "Delivery Mode",
    "Expiration",
    "Type",
    "Text"
  };

  private Boolean running = false;
  private Boolean shutdown = false;
  private Integer messagesReceived = 0;
  private DefaultMessageListenerContainer listenerContainer;

  /**
   *
   */
  public MessageTableModel() {
  }

  /**
   *
   */
  public void start() {
    listenerContainer = new DefaultMessageListenerContainer();
    listenerContainer.setMessageListener(this);
    if (!shutdown) {
      LOGGER.info("Starting listener...");
      listenerContainer.initialize();
      running = true;
    }
  }

  /**
   *
   */
  public void stop() {
    shutdown = true;
    LOGGER.info("Stopping listener...");
    listenerContainer.stop(new Stop());
    listenerContainer.shutdown();
    running = false;
  }

  private class Stop implements Runnable {
    @Override
    public void run() {
      LOGGER.info("MessageListener shut down.");
      shutdown = false;
    }
  }

  /**
   *
   * @return
   */
  public Boolean isRunning() {
    return this.running;
  }

  /**
   *
   * @param destination
   */
  public void setDestination(Destination destination) {
    listenerContainer.setDestination(destination);
  }

  /**
   *
   * @param connectionFactory
   */
  public void setConnectionFactory(ConnectionFactory connectionFactory) {
    listenerContainer.setConnectionFactory(connectionFactory);
  }

  /**
   *
   * @return
   */
  public List getData() {
    return data;
  }

  /**
   *
   * @param value
   */
  public void setData(List<MessageTableRecord> value) {
    List oldData = data;
    data = value;
    this.fireTableDataChanged();
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
  public String getColumnName(int column) {
    return columnName[column];
  }

  /**
   * @return the columnName
   */
  public String[] getColumnName() {
    return columnName;
  }

  /**
   * @param aColumnName the columnName to set
   */
  public void setColumnName(String[] aColumnName) {
    columnName = aColumnName;
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    Object[] record = data.toArray();
    MessageTableRecord qRecord = (MessageTableRecord) record[rowIndex];
    String result = "";
    try {
      switch (columnIndex) {
        case 0:
          result = DATE_TIME.format(qRecord.getJMSTimestamp());
          break;
        case 1:
          result = qRecord.getJMSMessageID();
          break;
        case 2:
          result = qRecord.getJMSCorrelationID();
          break;
        case 3:
          result = "";
          break;
        case 4:
          result = Integer.valueOf(qRecord.getJMSPriority()).toString();
          break;
        case 5:
          result = Integer.valueOf(qRecord.getJMSDeliveryMode()).toString();
          break;
        case 6:
          result = Long.valueOf(qRecord.getJMSExpiration()).toString();
          break;
        case 7:
          result = qRecord.getJMSType();
          break;
        case 8:
          result = qRecord.getText();
          break;
      }
    } catch (JMSException e) {
      LOGGER.log(Level.WARNING, "JMS problem", e);
    }
    return result;
  }

  /**
   *
   * @param message
   */
  @Override
  public void onMessage(Message message) {
    LOGGER.log(Level.FINE, "Message Received");
    messagesReceived++;
    try {
      MessageTableRecord qRecord = new MessageTableRecord();
      qRecord.setJMSCorrelationID(message.getJMSCorrelationID());
      qRecord.setJMSDeliveryMode(message.getJMSDeliveryMode());
      qRecord.setJMSExpiration(message.getJMSExpiration());
      qRecord.setJMSMessageID(message.getJMSMessageID());
      qRecord.setJMSTimestamp(message.getJMSTimestamp());
      qRecord.setJMSType(message.getJMSType());
      qRecord.setJMSDestination(message.getJMSDestination());
      qRecord.setJMSCorrelationIDAsBytes(message.getJMSCorrelationIDAsBytes());
      qRecord.setJMSPriority(message.getJMSPriority());
      qRecord.setJMSType(message.getJMSType());
      qRecord.setJMSReplyTo(message.getJMSReplyTo());
      qRecord.setJMSRedelivered(message.getJMSRedelivered());
      Enumeration pEnumerator = message.getPropertyNames();
      Properties props = new Properties();
      while (pEnumerator.hasMoreElements()) {
        String pElement = (String) pEnumerator.nextElement();
        props.put(pElement, message.getStringProperty(pElement));
      }
      qRecord.setProperties(props);

      if (message instanceof TextMessage) {
        qRecord.setText(((TextMessage) message).getText());
      }
      if (message instanceof ObjectMessage) {
        qRecord.setObject(((ObjectMessage) message).getObject());
      }

      List newData = data;
      newData.add(qRecord);
      this.setData(newData);
    } catch (JMSException e) {
      LOGGER.log(Level.WARNING, "JMS problem", e);
    }
  }
}
