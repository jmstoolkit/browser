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
package com.jmstoolkit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author Scott Douglass
 */
public class Settings {
  //FIXME: use a regex that matches more possible separators (i.e.: "," " " "-")

  /**
   *
   */
  
  public static final String ELEMENT_SEPARATOR = ":";

  /**
   *
   */
  public static final String D_APP_PROPERTIES = "app.properties";

  /**
   *
   */
  public static final String D_JNDI_PROPERTIES = "jndi.properties";

  /**
   *
   */
  public static final String JNDI_PROPERTIES =
    System.getProperty(D_JNDI_PROPERTIES, D_JNDI_PROPERTIES);
  /** Allow -Dapp.properties= to override default file name for settings. */
  public static final String APP_PROPERTIES =
    System.getProperty(D_APP_PROPERTIES, D_APP_PROPERTIES);


  /**
   * Adds a value to a list property and returns a List with all the
   * values.
   *
   * @param inProperties: properties to use
   * @param inKey: key for list property
   * @param inValue: value for key in list property
   * @return the List of values for this key
   */
  public static List<String> addSetting(final Properties inProperties,
    final String inKey, final String inValue) {
    if (!(inValue == null || inValue.isEmpty())) {
      final StringBuilder listItem = new StringBuilder();

      listItem.append(inValue);

      if (inProperties.containsKey(inKey)) {
        listItem.append(ELEMENT_SEPARATOR);
        listItem.append(inProperties.getProperty(inKey));
      }
      inProperties.setProperty(inKey, listItem.toString());
    }

    return getSettings(inProperties, inKey);
  }

  /**
   *
   * @param inProperties
   * @param inKey
   * @return
   */
  public static List<String> getSettings(final Properties inProperties,
    final String inKey) {
    List<String> settings = new ArrayList<>();
    if (inProperties.containsKey(inKey)) {
      final String allElements = inProperties.getProperty(inKey);
      settings = Arrays.asList(allElements.split(ELEMENT_SEPARATOR));
    }
    return settings;
  }

  /**
   *
   * @param inFileName
   * @throws JTKException
   */
  public static void loadSystemSettings(final String inFileName)
    throws JTKException {
    loadSettings(System.getProperties(), inFileName);
  }

  /**
   *
   * @param inProperties
   * @throws JTKException
   */
  public static void loadSettings(final Properties inProperties)
    throws JTKException {
    loadSettings(inProperties, APP_PROPERTIES);
  }

  /**
   *
   * @param inProperties
   * @param inFileName
   * @throws JTKException
   */
  public static void loadSettings(final Properties inProperties,
    final String inFileName)
    throws JTKException {
    FileInputStream propStream = null;
    try {
      final File props = new File(inFileName);
      propStream = new FileInputStream(props);
      inProperties.load(propStream);
    } catch (FileNotFoundException fnfe) {
      throw new JTKException(
        "No properties file: " + inFileName
        + " in working directory: " + System.getProperty("user.dir"), fnfe);
    } catch (IOException ioe) {
      throw new JTKException("Problem loading properties file: "
        + APP_PROPERTIES, ioe);
    } finally {
      if (propStream != null) {
        try {
          propStream.close();
        } catch (IOException ioe) {
          //
        }
      }
    }
  }

  /**
   *
   * @param inProperties
   * @param inMessage
   * @throws JTKException
   */
  public static void saveSettings(final Properties inProperties,
    final String inMessage)
    throws JTKException {
    saveSettings(inProperties, APP_PROPERTIES, inMessage);
  }

  /**
   *
   * @param inProperties
   * @param inFileName
   * @param inMessage
   * @throws JTKException
   */
  public static void saveSettings(final Properties inProperties,
    final String inFileName, final String inMessage)
    throws JTKException {
    FileOutputStream propStream = null;
    try {
      final File props = new File(inFileName);
      propStream = new FileOutputStream(props);
      inProperties.store(propStream, System.getProperty("user.name")
        + ": " + inMessage);

    } catch (FileNotFoundException fnfe) {
      throw new JTKException("Couldn't find properties file: "
        + inFileName, fnfe);
    } catch (IOException ioe) {
      throw new JTKException(
        "Something went wrong saving properties file: "
        + inFileName, ioe);
    } finally {
      if (propStream != null) {
        try {
          propStream.close();
        } catch (IOException ioe) {
          //
        }
      }
    }
  }
}
