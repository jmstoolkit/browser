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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 *
 * @author Scott Douglass
 */
public class JTKException extends Exception {

  /** The date and time the exeption was created. */
  private final String timeStamp;
  /** System property used to set the time stamp format. */
  public static final String P_TIMESTAMP_FORMAT = "app.timestamp.format";
  /** The default time stamp format. */
  public static final String D_TIMESTAMP_FORMAT = "yyyy-MM-dd hh:mm:ss.SSS z";
  /** A SimpleDateFormat based on the value of the P_TIMESTAMP_FORMAT
    system property. */
  public static final SimpleDateFormat TIMESTAMP_FORMAT =
    new SimpleDateFormat(
      System.getProperty(P_TIMESTAMP_FORMAT, D_TIMESTAMP_FORMAT), Locale.US);

  /**
   *
   * @param inMessage The exception message
   */
  public JTKException(final String inMessage) {
    super(inMessage);
    timeStamp = TIMESTAMP_FORMAT.format(new Date());
  }

  /**
   *
   * @param inMessage the message text
   * @param inException the exception class
   */
  public JTKException(final String inMessage, final Throwable inException) {
    super(inMessage, inException);
    timeStamp = TIMESTAMP_FORMAT.format(new Date());
  }

  /**
   * Method for printing the exception in a complete way.
   * @return nicely formatted stack trace
   */
  public final String toStringWithStackTrace() {
    return formatException(this);
  }

  /**
   * Add a time stamp to the exception message.
   * @param inThrowable The exception to time stamp
   * @return the exception as a String
   */
  public static String formatException(final Throwable inThrowable) {
    final StringBuilder outException = new StringBuilder();
    if (inThrowable instanceof JTKException) {
      outException.append(((JTKException) inThrowable).getTimeStamp());
      outException.append(" ");
    }
    outException.append(inThrowable.getMessage());
    outException.append("\n");
    outException.append(formatStackTrace(inThrowable));
    return outException.toString();
  }

  /**
   * Format the stack trace output to make it similar to pargs on SunOS.
   * @param inThrowable the exception to format
   * @return The formated stack trace
   */
  public static String formatStackTrace(final Throwable inThrowable) {
    final StringBuilder outStackTrace = new StringBuilder();
    final StackTraceElement[] stackTraceElement = inThrowable.getStackTrace();
    outStackTrace.append(inThrowable.toString());
    outStackTrace.append("\n");
    for (int count = 0; count < stackTraceElement.length; count++) {
      outStackTrace.append(count);
      outStackTrace.append(". ");
      outStackTrace.append(stackTraceElement[count]);
      outStackTrace.append("\n");
    }
    if (inThrowable.getCause() != null) {
      outStackTrace.append("Caused by: ");
      outStackTrace.append(inThrowable.getCause().toString());
      outStackTrace.append("\n");
      formatException(inThrowable.getCause());
    }
    return outStackTrace.toString();
  }

  /**
   * @return the timeStamp
   */
  public final String getTimeStamp() {
    return timeStamp;
  }
}
