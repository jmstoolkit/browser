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

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Properties;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

/**
 *
 * @author Scott Douglass
 */
public class MessageTableRecord implements TextMessage, ObjectMessage {

  private String jmsMessageID;
  private long jmsTimestamp;
  private byte[] jmsCorrelationIDAsBytes;
  private String jmsCorrelationID;
  private Destination jmsReplyTo;
  private Destination jmsDestination;
  private int jmsPriority;
  private boolean jmsRedelivered;
  private int jmsDeliveryMode;
  private String jmsType;
  private long jmsExpiration;
  private String text;
  private Serializable object;
  private Properties properties = new Properties();

  /**
   *
   * @return
   * @throws JMSException
   */
  @Override
  public String getJMSMessageID() throws JMSException {
    return this.jmsMessageID;
  }

  /**
   *
   * @param arg0
   * @throws JMSException
   */
  @Override
  public void setJMSMessageID(String arg0) throws JMSException {
    this.jmsMessageID = arg0;
  }

  /**
   *
   * @return
   * @throws JMSException
   */
  @Override
  public long getJMSTimestamp() throws JMSException {
    return this.jmsTimestamp;
  }

  /**
   *
   * @param arg0
   * @throws JMSException
   */
  @Override
  public void setJMSTimestamp(long arg0) throws JMSException {
    this.jmsTimestamp = arg0;
  }

  /**
   *
   * @return
   * @throws JMSException
   */
  @Override
  public byte[] getJMSCorrelationIDAsBytes() throws JMSException {
    return this.jmsCorrelationIDAsBytes;
  }

  /**
   *
   * @param arg0
   * @throws JMSException
   */
  @Override
  public void setJMSCorrelationIDAsBytes(byte[] arg0) throws JMSException {
    this.jmsCorrelationIDAsBytes = arg0;
  }

  /**
   *
   * @param arg0
   * @throws JMSException
   */
  @Override
  public void setJMSCorrelationID(String arg0) throws JMSException {
    this.jmsCorrelationID = arg0;
  }

  /**
   *
   * @return
   * @throws JMSException
   */
  @Override
  public String getJMSCorrelationID() throws JMSException {
    return this.jmsCorrelationID;
  }

  /**
   *
   * @return
   * @throws JMSException
   */
  @Override
  public Destination getJMSReplyTo() throws JMSException {
    return this.jmsReplyTo;
  }

  /**
   *
   * @param arg0
   * @throws JMSException
   */
  @Override
  public void setJMSReplyTo(Destination arg0) throws JMSException {
    this.jmsReplyTo = arg0;
  }

  /**
   *
   * @return
   * @throws JMSException
   */
  @Override
  public Destination getJMSDestination() throws JMSException {
    return this.jmsDestination;
  }

  /**
   *
   * @param arg0
   * @throws JMSException
   */
  @Override
  public void setJMSDestination(Destination arg0) throws JMSException {
    this.jmsDestination = arg0;
  }

  /**
   *
   * @return
   * @throws JMSException
   */
  @Override
  public int getJMSDeliveryMode() throws JMSException {
    return this.jmsDeliveryMode;
  }

  /**
   *
   * @param arg0
   * @throws JMSException
   */
  @Override
  public void setJMSDeliveryMode(int arg0) throws JMSException {
    this.jmsDeliveryMode = arg0;
  }

  /**
   *
   * @return
   * @throws JMSException
   */
  @Override
  public boolean getJMSRedelivered() throws JMSException {
    return this.jmsRedelivered;
  }

  /**
   *
   * @param arg0
   * @throws JMSException
   */
  @Override
  public void setJMSRedelivered(boolean arg0) throws JMSException {
    this.jmsRedelivered = arg0;
  }

  /**
   *
   * @return
   * @throws JMSException
   */
  @Override
  public String getJMSType() throws JMSException {
    return this.jmsType;
  }

  /**
   *
   * @param arg0
   * @throws JMSException
   */
  @Override
  public void setJMSType(String arg0) throws JMSException {
    this.jmsType = arg0;
  }

  /**
   *
   * @return
   * @throws JMSException
   */
  @Override
  public long getJMSExpiration() throws JMSException {
    return this.jmsExpiration;
  }

  /**
   *
   * @param arg0
   * @throws JMSException
   */
  @Override
  public void setJMSExpiration(long arg0) throws JMSException {
    this.jmsExpiration = arg0;
  }

  /**
   *
   * @return
   * @throws JMSException
   */
  @Override
  public int getJMSPriority() throws JMSException {
    return this.jmsPriority;
  }

  /**
   *
   * @param arg0
   * @throws JMSException
   */
  @Override
  public void setJMSPriority(int arg0) throws JMSException {
    this.jmsPriority = arg0;
  }

  /**
   *
   * @throws JMSException
   */
  @Override
  public void clearProperties() throws JMSException {
    this.getProperties().clear();
  }

  /**
   *
   * @param arg0
   * @return
   * @throws JMSException
   */
  @Override
  public boolean propertyExists(String arg0) throws JMSException {
    boolean result = false;
    if (this.getProperties().containsKey(arg0)) {
      result = true;
    }
    return result;
  }

  /**
   *
   * @param arg0
   * @return
   * @throws JMSException
   */
  @Override
  public boolean getBooleanProperty(String arg0) throws JMSException {
    return Boolean.getBoolean(this.getProperties().getProperty(arg0));
  }

  /**
   *
   * @param arg0
   * @return
   * @throws JMSException
   */
  @Override
  public byte getByteProperty(String arg0) throws JMSException {
    return new Byte(this.getProperties().getProperty(arg0));
  }

  /**
   *
   * @param arg0
   * @return
   * @throws JMSException
   */
  @Override
  public short getShortProperty(String arg0) throws JMSException {
    return new Short(this.getProperties().getProperty(arg0));
  }

  /**
   *
   * @param arg0
   * @return
   * @throws JMSException
   */
  @Override
  public int getIntProperty(String arg0) throws JMSException {
    return new Integer(this.getProperties().getProperty(arg0));
  }

  /**
   *
   * @param arg0
   * @return
   * @throws JMSException
   */
  @Override
  public long getLongProperty(String arg0) throws JMSException {
    return new Long(this.getProperties().getProperty(arg0));
  }

  /**
   *
   * @param arg0
   * @return
   * @throws JMSException
   */
  @Override
  public float getFloatProperty(String arg0) throws JMSException {
    return new Float(this.getProperties().getProperty(arg0));
  }

  /**
   *
   * @param arg0
   * @return
   * @throws JMSException
   */
  @Override
  public double getDoubleProperty(String arg0) throws JMSException {
    return new Double(this.getProperties().getProperty(arg0));
  }

  /**
   *
   * @param arg0
   * @return
   * @throws JMSException
   */
  @Override
  public String getStringProperty(String arg0) throws JMSException {
    return this.getProperties().getProperty(arg0);
  }

  /**
   *
   * @param arg0
   * @return
   * @throws JMSException
   */
  @Override
  public Object getObjectProperty(String arg0) throws JMSException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  /**
   *
   * @return
   * @throws JMSException
   */
  @Override
  public Enumeration getPropertyNames() throws JMSException {
    return this.getProperties().propertyNames();
  }

  /**
   *
   * @param arg0
   * @param arg1
   * @throws JMSException
   */
  @Override
  public void setBooleanProperty(String arg0, boolean arg1) throws JMSException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  /**
   *
   * @param arg0
   * @param arg1
   * @throws JMSException
   */
  @Override
  public void setByteProperty(String arg0, byte arg1) throws JMSException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  /**
   *
   * @param arg0
   * @param arg1
   * @throws JMSException
   */
  @Override
  public void setShortProperty(String arg0, short arg1) throws JMSException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  /**
   *
   * @param arg0
   * @param arg1
   * @throws JMSException
   */
  @Override
  public void setIntProperty(String arg0, int arg1) throws JMSException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  /**
   *
   * @param arg0
   * @param arg1
   * @throws JMSException
   */
  @Override
  public void setLongProperty(String arg0, long arg1) throws JMSException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  /**
   *
   * @param arg0
   * @param arg1
   * @throws JMSException
   */
  @Override
  public void setFloatProperty(String arg0, float arg1) throws JMSException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  /**
   *
   * @param arg0
   * @param arg1
   * @throws JMSException
   */
  @Override
  public void setDoubleProperty(String arg0, double arg1) throws JMSException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  /**
   *
   * @param arg0
   * @param arg1
   * @throws JMSException
   */
  @Override
  public void setStringProperty(String arg0, String arg1) throws JMSException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  /**
   *
   * @param arg0
   * @param arg1
   * @throws JMSException
   */
  @Override
  public void setObjectProperty(String arg0, Object arg1) throws JMSException {
    getProperties().setProperty(arg0, arg1.toString());//FIXME: this is bogus!!!
  }

  /**
   *
   * @throws JMSException
   */
  @Override
  public void acknowledge() throws JMSException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  /**
   *
   * @throws JMSException
   */
  @Override
  public void clearBody() throws JMSException {
    this.object = null;
    this.text = null;
  }

  /**
   *
   * @param arg0
   * @throws JMSException
   */
  @Override
  public void setText(String arg0) throws JMSException {
    this.text = arg0;
  }

  /**
   *
   * @return
   * @throws JMSException
   */
  @Override
  public String getText() throws JMSException {
    return this.text;
  }

  /**
   *
   * @param arg0
   * @throws JMSException
   */
  @Override
  public void setObject(Serializable arg0) throws JMSException {
    this.object = arg0;
  }

  /**
   *
   * @return
   * @throws JMSException
   */
  @Override
  public Serializable getObject() throws JMSException {
    return this.object;
  }

  /**
   * @return the properties
   */
  public Properties getProperties() {
    return properties;
  }

  /**
   * @param properties the properties to set
   */
  public void setProperties(Properties properties) {
    this.properties = properties;
  }

  @Override
  public long getJMSDeliveryTime() throws JMSException {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void setJMSDeliveryTime(long l) throws JMSException {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public <T> T getBody(Class<T> type) throws JMSException {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public boolean isBodyAssignableTo(Class type) throws JMSException {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
}
