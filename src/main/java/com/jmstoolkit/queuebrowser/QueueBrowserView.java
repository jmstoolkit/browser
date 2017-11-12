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
package com.jmstoolkit.queuebrowser;

import com.jmstoolkit.beans.MessageTableRecord;
import com.jmstoolkit.Settings;
import com.jmstoolkit.JTKException;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.naming.NamingException;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.Task;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.Properties;
import java.util.List;
import java.util.ArrayList;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.connection.UserCredentialsConnectionFactoryAdapter;
import org.springframework.jms.core.BrowserCallback;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jndi.JndiTemplate;

/**
 * The application's main frame.
 */
public class QueueBrowserView extends FrameView {

  private static final String P_CONNECTION_FACTORIES = "connection_factories";
  private static final String P_DESTINATIONS = "destinations";
  private static final String D_JNDI_PROPERTIES = "jndi.properties";
  private JndiTemplate jndiTemplate;
  private JmsTemplate jmsTemplate;
  private Task browseTask;
  private List<String> connectionFactoryList = new ArrayList<>();
  private List<String> destinationList = new ArrayList<>();
  private Properties appProperties = new Properties();
  private CachingConnectionFactory connectionFactory;

  private void _init() {
    try {
      Settings.loadSystemSettings(
        System.getProperty(D_JNDI_PROPERTIES, D_JNDI_PROPERTIES));
      // load settings from default file: app.properties
      // which contains previously used connection
      // factories and destinations
      Settings.loadSettings(appProperties);
      connectionFactoryList = Settings.getSettings(appProperties, P_CONNECTION_FACTORIES);
      destinationList = Settings.getSettings(appProperties, P_DESTINATIONS);
    } catch (JTKException se) {
      // this happens BEFORE initComponents, so can't put the error in the
      // status area or any other part of the GUI
      System.out.println(se.toStringWithStackTrace());
    }
    // FIXME: Not using the applicationContext at all... ho hum
    this.jmsTemplate = new JmsTemplate();

    this.connectionFactory = new CachingConnectionFactory();
    this.connectionFactory.setCacheProducers(true);
    this.jmsTemplate.setConnectionFactory(connectionFactory);
    this.jndiTemplate = new JndiTemplate();
  }

  private ConnectionFactory wrapConnectionFactory(String inJNDIName)
    throws NamingException {
    UserCredentialsConnectionFactoryAdapter uccfa
      = new UserCredentialsConnectionFactoryAdapter();
    uccfa.setUsername(appProperties.getProperty("jmstoolkit.username"));
    uccfa.setPassword(appProperties.getProperty("jmstoolkit.password"));
    uccfa.setTargetConnectionFactory(
      (ConnectionFactory) jndiTemplate.lookup(inJNDIName));
    return uccfa;
  }

  /**
   *
   * @param app the SingleFrameApplication
   */
  public QueueBrowserView(SingleFrameApplication app) {
    super(app);

    _init();
    initComponents();

    // post components, finish inititalization based on initial values
    // of combo boxes
    try {
      this.jmsTemplate.setDefaultDestination(
        (Destination) this.jndiTemplate.lookup(
          destinationComboBox.getSelectedItem().toString()));
      connectionFactory.setTargetConnectionFactory(
        wrapConnectionFactory(connectionFactoryComboBox.getSelectedItem().toString()));
    } catch (NamingException ex) {
      messageTextArea.setText(
        JTKException.formatException(ex));
    } catch (NullPointerException e) {
      // if we have no previous properties, we'll get NullPointerException from
      // the .toString()s... but we don't care.
    }

    // status bar initialization - message timeout, idle icon and busy animation, etc
    ResourceMap resourceMap = getResourceMap();
    int messageTimeout = 10; //resourceMap.getInteger("StatusBar.messageTimeout");
    messageTimer = new Timer(messageTimeout, new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        statusMessageLabel.setText("");
      }
    });
    messageTimer.setRepeats(false);
    int busyAnimationRate = 10; //resourceMap.getInteger("StatusBar.busyAnimationRate");
    for (int i = 0; i < busyIcons.length; i++) {
      busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
    }
    busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
        statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
      }
    });
    idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
    statusAnimationLabel.setIcon(idleIcon);
    progressBar.setVisible(false);

    // connecting action tasks to status bar via TaskMonitor
    TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
    taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {

      @Override
      public void propertyChange(java.beans.PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName();
        if (null != propertyName) {
          switch (propertyName) {
            case "started":
              if (!busyIconTimer.isRunning()) {
                statusAnimationLabel.setIcon(busyIcons[0]);
                busyIconIndex = 0;
                busyIconTimer.start();
              }
              progressBar.setVisible(true);
              progressBar.setIndeterminate(true);
              break;
            case "done":
              busyIconTimer.stop();
              statusAnimationLabel.setIcon(idleIcon);
              progressBar.setVisible(false);
              progressBar.setValue(0);
              break;
            case "message":
              String text = (String) (evt.getNewValue());
              statusMessageLabel.setText((text == null) ? "" : text);
              messageTimer.restart();
              break;
            case "progress":
              int value = (Integer) (evt.getNewValue());
              progressBar.setVisible(true);
              progressBar.setIndeterminate(false);
              progressBar.setValue(value);
              break;
            default:
              break;
          }
        }
      }
    });
  }

  /**
   *
   */
  @Action
  public void showAboutBox() {
    if (aboutBox == null) {
      JFrame mainFrame = QueueBrowserApp.getApplication().getMainFrame();
      aboutBox = new QueueBrowserAboutBox(mainFrame);
      aboutBox.setLocationRelativeTo(mainFrame);
    }
    QueueBrowserApp.getApplication().show(aboutBox);
  }

  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    mainPanel = new javax.swing.JPanel();
    destinationLabel = new javax.swing.JLabel();
    destinationComboBox = new javax.swing.JComboBox();
    browseButton = new javax.swing.JButton();
    cancelButton = new javax.swing.JButton();
    connectionFactoryLabel = new javax.swing.JLabel();
    connectionFactoryComboBox = new javax.swing.JComboBox();
    messageSplitPane = new javax.swing.JSplitPane();
    messagePropertiesSplitPane = new javax.swing.JSplitPane();
    messageScrollPane = new javax.swing.JScrollPane();
    messageTextArea = new javax.swing.JTextArea();
    messagePropertiesScrollPane = new javax.swing.JScrollPane();
    messagePropertiesTable = new javax.swing.JTable();
    messageRecordTableScrollPane = new javax.swing.JScrollPane();
    messageRecordTable = new javax.swing.JTable();
    menuBar = new javax.swing.JMenuBar();
    javax.swing.JMenu fileMenu = new javax.swing.JMenu();
    drainQueueMenuItem = new javax.swing.JMenuItem();
    javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
    javax.swing.JMenu helpMenu = new javax.swing.JMenu();
    javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
    statusPanel = new javax.swing.JPanel();
    javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
    statusMessageLabel = new javax.swing.JLabel();
    statusAnimationLabel = new javax.swing.JLabel();
    progressBar = new javax.swing.JProgressBar();
    messageTableModel = new com.jmstoolkit.beans.MessageTableModel();
    messagePropertyTableModel = new com.jmstoolkit.beans.PropertyTableModel();
    queueDrainedDialog = new javax.swing.JDialog();
    queueDrainedDialogOKButton = new javax.swing.JButton();
    itemsDrainedLabel = new javax.swing.JLabel();
    itemsDrainedTextField = new javax.swing.JTextField();
    queueDrainedScrollPane = new javax.swing.JScrollPane();
    queueDrainedTextPane = new javax.swing.JTextPane();

    org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance().getContext().getResourceMap(QueueBrowserView.class);
    mainPanel.setBackground(resourceMap.getColor("mainPanel.background")); // NOI18N
    mainPanel.setForeground(resourceMap.getColor("mainPanel.foreground")); // NOI18N
    mainPanel.setName("mainPanel"); // NOI18N

    destinationLabel.setText(resourceMap.getString("destinationLabel.text")); // NOI18N
    destinationLabel.setName("destinationLabel"); // NOI18N

    destinationComboBox.setEditable(true);
    destinationComboBox.setModel(new javax.swing.DefaultComboBoxModel(destinationList.toArray()));
    destinationComboBox.setToolTipText(resourceMap.getString("destinationComboBox.toolTipText")); // NOI18N
    destinationComboBox.setName("destinationComboBox"); // NOI18N
    destinationComboBox.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        destinationComboBoxActionPerformed(evt);
      }
    });

    javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance().getContext().getActionMap(QueueBrowserView.class, this);
    browseButton.setAction(actionMap.get("browseQueue")); // NOI18N
    browseButton.setText(resourceMap.getString("browseButton.text")); // NOI18N
    browseButton.setToolTipText(resourceMap.getString("browseButton.toolTipText")); // NOI18N
    browseButton.setName("browseButton"); // NOI18N

    cancelButton.setFont(resourceMap.getFont("cancelButton.font")); // NOI18N
    cancelButton.setText(resourceMap.getString("cancelButton.text")); // NOI18N
    cancelButton.setToolTipText(resourceMap.getString("cancelButton.toolTipText")); // NOI18N
    cancelButton.setEnabled(false);
    cancelButton.setName("cancelButton"); // NOI18N
    cancelButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        cancelButtonActionPerformed(evt);
      }
    });

    connectionFactoryLabel.setText(resourceMap.getString("connectionFactoryLabel.text")); // NOI18N
    connectionFactoryLabel.setName("connectionFactoryLabel"); // NOI18N

    connectionFactoryComboBox.setEditable(true);
    connectionFactoryComboBox.setModel(new javax.swing.DefaultComboBoxModel(connectionFactoryList.toArray()));
    connectionFactoryComboBox.setName("connectionFactoryComboBox"); // NOI18N
    connectionFactoryComboBox.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        connectionFactoryComboBoxActionPerformed(evt);
      }
    });

    messageSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
    messageSplitPane.setName("messageSplitPane"); // NOI18N
    messageSplitPane.setPreferredSize(new java.awt.Dimension(456, 400));

    messagePropertiesSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
    messagePropertiesSplitPane.setName("messagePropertiesSplitPane"); // NOI18N
    messagePropertiesSplitPane.setPreferredSize(new java.awt.Dimension(454, 200));

    messageScrollPane.setBackground(resourceMap.getColor("messageScrollPane.background")); // NOI18N
    messageScrollPane.setForeground(resourceMap.getColor("messageScrollPane.foreground")); // NOI18N
    messageScrollPane.setName("messageScrollPane"); // NOI18N

    messageTextArea.setBackground(resourceMap.getColor("messageTextArea.background")); // NOI18N
    messageTextArea.setColumns(20);
    messageTextArea.setForeground(resourceMap.getColor("messageTextArea.foreground")); // NOI18N
    messageTextArea.setLineWrap(true);
    messageTextArea.setRows(5);
    messageTextArea.setTabSize(2);
    messageTextArea.setToolTipText(resourceMap.getString("messageTextArea.toolTipText")); // NOI18N
    messageTextArea.setWrapStyleWord(true);
    messageTextArea.setName("messageTextArea"); // NOI18N
    messageScrollPane.setViewportView(messageTextArea);

    messagePropertiesSplitPane.setTopComponent(messageScrollPane);

    messagePropertiesScrollPane.setName("messagePropertiesScrollPane"); // NOI18N
    messagePropertiesScrollPane.setPreferredSize(new java.awt.Dimension(452, 202));

    messagePropertiesTable.setModel(messagePropertyTableModel);
    messagePropertiesTable.setAutoCreateRowSorter(true);
    messagePropertiesTable.setCellSelectionEnabled(true);
    messagePropertiesTable.setDoubleBuffered(true);
    messagePropertiesTable.setName("messagePropertiesTable"); // NOI18N
    messagePropertiesScrollPane.setViewportView(messagePropertiesTable);

    messagePropertiesSplitPane.setRightComponent(messagePropertiesScrollPane);

    messageSplitPane.setRightComponent(messagePropertiesSplitPane);

    messageRecordTableScrollPane.setName("messageRecordTableScrollPane"); // NOI18N
    messageRecordTableScrollPane.setPreferredSize(new java.awt.Dimension(452, 202));

    messageRecordTable.setModel(messageTableModel);
    messageRecordTable.setCellSelectionEnabled(true);
    messageRecordTable.setDoubleBuffered(true);
    messageRecordTable.setName("messageRecordTable"); // NOI18N
    messageRecordTable.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        messageRecordTableMouseClicked(evt);
      }
    });
    messageRecordTableScrollPane.setViewportView(messageRecordTable);

    messageSplitPane.setLeftComponent(messageRecordTableScrollPane);

    javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
    mainPanel.setLayout(mainPanelLayout);
    mainPanelLayout.setHorizontalGroup(
      mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(mainPanelLayout.createSequentialGroup()
        .addContainerGap()
        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
          .addGroup(mainPanelLayout.createSequentialGroup()
            .addComponent(destinationLabel)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(destinationComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
          .addGroup(mainPanelLayout.createSequentialGroup()
            .addComponent(connectionFactoryLabel)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(connectionFactoryComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE)))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addComponent(browseButton)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(cancelButton)
        .addGap(19, 19, 19))
      .addComponent(messageSplitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 597, Short.MAX_VALUE)
    );
    mainPanelLayout.setVerticalGroup(
      mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(mainPanelLayout.createSequentialGroup()
        .addContainerGap()
        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(connectionFactoryLabel)
          .addComponent(connectionFactoryComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(destinationLabel)
          .addComponent(destinationComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(cancelButton)
          .addComponent(browseButton))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(messageSplitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 454, Short.MAX_VALUE))
    );

    menuBar.setName("menuBar"); // NOI18N

    fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
    fileMenu.setName("fileMenu"); // NOI18N

    drainQueueMenuItem.setAction(actionMap.get("drainQueue")); // NOI18N
    drainQueueMenuItem.setText(resourceMap.getString("drainQueueMenuItem.text")); // NOI18N
    drainQueueMenuItem.setName("drainQueueMenuItem"); // NOI18N
    fileMenu.add(drainQueueMenuItem);

    exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
    exitMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.CTRL_MASK));
    exitMenuItem.setText(resourceMap.getString("exitMenuItem.text")); // NOI18N
    exitMenuItem.setToolTipText(resourceMap.getString("exitMenuItem.toolTipText")); // NOI18N
    exitMenuItem.setName("exitMenuItem"); // NOI18N
    fileMenu.add(exitMenuItem);

    menuBar.add(fileMenu);

    helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
    helpMenu.setName("helpMenu"); // NOI18N

    aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
    aboutMenuItem.setName("aboutMenuItem"); // NOI18N
    helpMenu.add(aboutMenuItem);

    menuBar.add(helpMenu);

    statusPanel.setName("statusPanel"); // NOI18N
    statusPanel.setPreferredSize(new java.awt.Dimension(454, 30));

    statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

    statusMessageLabel.setAlignmentY(0.0F);
    statusMessageLabel.setName("statusMessageLabel"); // NOI18N

    statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

    progressBar.setName("progressBar"); // NOI18N

    javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
    statusPanel.setLayout(statusPanelLayout);
    statusPanelLayout.setHorizontalGroup(
      statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 597, Short.MAX_VALUE)
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, statusPanelLayout.createSequentialGroup()
        .addContainerGap()
        .addComponent(statusMessageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 235, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 166, Short.MAX_VALUE)
        .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(statusAnimationLabel)
        .addContainerGap())
    );
    statusPanelLayout.setVerticalGroup(
      statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(statusPanelLayout.createSequentialGroup()
        .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
          .addGroup(statusPanelLayout.createSequentialGroup()
            .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
              .addComponent(statusMessageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
              .addComponent(statusAnimationLabel))
            .addGap(3, 3, 3))
          .addGroup(statusPanelLayout.createSequentialGroup()
            .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap())))
    );

    messagePropertyTableModel.setData(messagePropertyTableModel.getData());

    queueDrainedDialog.setLocationByPlatform(true);
    queueDrainedDialog.setMinimumSize(new java.awt.Dimension(300, 180));
    queueDrainedDialog.setModal(true);
    queueDrainedDialog.setName("queueDrainedDialog"); // NOI18N
    queueDrainedDialog.setResizable(false);

    queueDrainedDialogOKButton.setText(resourceMap.getString("queueDrainedDialogOKButton.text")); // NOI18N
    queueDrainedDialogOKButton.setName("queueDrainedDialogOKButton"); // NOI18N
    queueDrainedDialogOKButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        queueDrainedDialogOKButtonActionPerformed(evt);
      }
    });

    itemsDrainedLabel.setText(resourceMap.getString("itemsDrainedLabel.text")); // NOI18N
    itemsDrainedLabel.setName("itemsDrainedLabel"); // NOI18N

    itemsDrainedTextField.setEditable(false);
    itemsDrainedTextField.setText(resourceMap.getString("itemsDrainedTextField.text")); // NOI18N
    itemsDrainedTextField.setName("itemsDrainedTextField"); // NOI18N

    queueDrainedScrollPane.setName("queueDrainedScrollPane"); // NOI18N

    queueDrainedTextPane.setEditable(false);
    queueDrainedTextPane.setText(resourceMap.getString("queueDrainedTextPane.text")); // NOI18N
    queueDrainedTextPane.setName("queueDrainedTextPane"); // NOI18N
    queueDrainedScrollPane.setViewportView(queueDrainedTextPane);

    javax.swing.GroupLayout queueDrainedDialogLayout = new javax.swing.GroupLayout(queueDrainedDialog.getContentPane());
    queueDrainedDialog.getContentPane().setLayout(queueDrainedDialogLayout);
    queueDrainedDialogLayout.setHorizontalGroup(
      queueDrainedDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(queueDrainedDialogLayout.createSequentialGroup()
        .addGroup(queueDrainedDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(queueDrainedDialogLayout.createSequentialGroup()
            .addContainerGap()
            .addComponent(itemsDrainedLabel)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(itemsDrainedTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
          .addGroup(queueDrainedDialogLayout.createSequentialGroup()
            .addContainerGap()
            .addComponent(queueDrainedScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE))
          .addGroup(queueDrainedDialogLayout.createSequentialGroup()
            .addGap(127, 127, 127)
            .addComponent(queueDrainedDialogOKButton)))
        .addContainerGap())
    );
    queueDrainedDialogLayout.setVerticalGroup(
      queueDrainedDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(queueDrainedDialogLayout.createSequentialGroup()
        .addContainerGap()
        .addGroup(queueDrainedDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(itemsDrainedLabel)
          .addComponent(itemsDrainedTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(queueDrainedScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addComponent(queueDrainedDialogOKButton)
        .addGap(32, 32, 32))
    );

    setComponent(mainPanel);
    setMenuBar(menuBar);
    setStatusBar(statusPanel);
  }// </editor-fold>//GEN-END:initComponents

  private void destinationComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_destinationComboBoxActionPerformed
    try {
      String selectedItem = destinationComboBox.getSelectedItem().toString().trim();
      this.jmsTemplate.setDefaultDestination(
        (Destination) this.jndiTemplate.lookup(selectedItem));
      if (evt.getActionCommand().equals("comboBoxEdited")
        && (!destinationList.contains(selectedItem))) {
        destinationList = Settings.addSetting(appProperties, P_DESTINATIONS, selectedItem);
        destinationComboBox.addItem(selectedItem);
      }
    } catch (NamingException ex) {
      messageTextArea.setText(JTKException.formatException(ex));
    }
  }//GEN-LAST:event_destinationComboBoxActionPerformed

  private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
    browseTask.cancel(true);
    cancelButton.setEnabled(false);
    browseButton.setEnabled(true);
  }//GEN-LAST:event_cancelButtonActionPerformed

  private void connectionFactoryComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connectionFactoryComboBoxActionPerformed
    try {
      String selectedItem
        = connectionFactoryComboBox.getSelectedItem().toString().trim();
      connectionFactory.setTargetConnectionFactory(
        wrapConnectionFactory(selectedItem));
      if (evt.getActionCommand().equals("comboBoxEdited")
        && (!connectionFactoryList.contains(selectedItem))) {
        connectionFactoryList
          = Settings.addSetting(appProperties, P_CONNECTION_FACTORIES, selectedItem);
        connectionFactoryComboBox.addItem(selectedItem);
      }
    } catch (NamingException ex) {
      messageTextArea.setText(JTKException.formatException(ex));
    }
  }//GEN-LAST:event_connectionFactoryComboBoxActionPerformed

  private void messageRecordTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_messageRecordTableMouseClicked
    Integer selectedColumn = messageRecordTable.getSelectedColumn();
    Integer selectedRow = messageRecordTable.getSelectedRow();
    this.messageTextArea.setText(
      (String) this.messageRecordTable.getValueAt(selectedRow, selectedColumn));
    MessageTableRecord mRecord = (MessageTableRecord) this.messageTableModel.getData().get(selectedRow);
    this.messagePropertyTableModel.setData(mRecord.getProperties());
    this.messagePropertyTableModel.fireTableDataChanged();
  }//GEN-LAST:event_messageRecordTableMouseClicked

  private void queueDrainedDialogOKButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_queueDrainedDialogOKButtonActionPerformed
    queueDrainedDialog.setVisible(false);
  }//GEN-LAST:event_queueDrainedDialogOKButtonActionPerformed
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton browseButton;
  private javax.swing.JButton cancelButton;
  private javax.swing.JComboBox connectionFactoryComboBox;
  private javax.swing.JLabel connectionFactoryLabel;
  private javax.swing.JComboBox destinationComboBox;
  private javax.swing.JLabel destinationLabel;
  private javax.swing.JMenuItem drainQueueMenuItem;
  private javax.swing.JLabel itemsDrainedLabel;
  private javax.swing.JTextField itemsDrainedTextField;
  private javax.swing.JPanel mainPanel;
  private javax.swing.JMenuBar menuBar;
  private javax.swing.JScrollPane messagePropertiesScrollPane;
  private javax.swing.JSplitPane messagePropertiesSplitPane;
  private javax.swing.JTable messagePropertiesTable;
  private com.jmstoolkit.beans.PropertyTableModel messagePropertyTableModel;
  private javax.swing.JTable messageRecordTable;
  private javax.swing.JScrollPane messageRecordTableScrollPane;
  private javax.swing.JScrollPane messageScrollPane;
  private javax.swing.JSplitPane messageSplitPane;
  private com.jmstoolkit.beans.MessageTableModel messageTableModel;
  private javax.swing.JTextArea messageTextArea;
  private javax.swing.JProgressBar progressBar;
  private javax.swing.JDialog queueDrainedDialog;
  private javax.swing.JButton queueDrainedDialogOKButton;
  private javax.swing.JScrollPane queueDrainedScrollPane;
  private javax.swing.JTextPane queueDrainedTextPane;
  private javax.swing.JLabel statusAnimationLabel;
  private javax.swing.JLabel statusMessageLabel;
  private javax.swing.JPanel statusPanel;
  // End of variables declaration//GEN-END:variables
  private final Timer messageTimer;
  private final Timer busyIconTimer;
  private final Icon idleIcon;
  private final Icon[] busyIcons = new Icon[15];
  private int busyIconIndex = 0;
  private JDialog aboutBox;

  /**
   *
   * @return a Task
   */
  @Action
  public Task browseQueue() {
    browseTask = new BrowseQueueTask(getApplication());
    return browseTask;
  }

  private class BrowseQueueTask extends org.jdesktop.application.Task<Object, Void> {

    private List<MessageTableRecord> messages = new ArrayList<>();

    BrowseQueueTask(org.jdesktop.application.Application app) {
      // Copy GUI state that
      // doInBackground() depends on from parameters
      // to ReceiveMessageTask fields, here.
      super(app);
      cancelButton.setEnabled(true);
      browseButton.setEnabled(false);
    }

    @Override
    protected Object doInBackground() {
      // Your Task's code here.  This method runs
      // on a background thread, so don't reference
      // the Swing GUI from here.
      messages = (List<MessageTableRecord>) jmsTemplate.browse(new QueueBrowserCallback());
      return messages;  // return your result
    }

    @Override
    protected void succeeded(Object result) {
      // Update the GUI based on
      // the result computed by doInBackground().
      messageTableModel.setData(messages);
      cancelButton.setEnabled(false);
      browseButton.setEnabled(true);
      statusMessageLabel.setText("Items in queue: " + messages.size());
    }
  }

  private static class QueueBrowserCallback implements BrowserCallback {

    @Override
    public Object doInJms(Session session, QueueBrowser browser) throws JMSException {
      Enumeration messageEnumerator = browser.getEnumeration();
      List<MessageTableRecord> messages = new ArrayList<>();
      while (messageEnumerator.hasMoreElements()) {
        MessageTableRecord qRecord = new MessageTableRecord();
        Message msg = (Message) messageEnumerator.nextElement();
        qRecord.setJMSCorrelationID(msg.getJMSCorrelationID());
        qRecord.setJMSCorrelationIDAsBytes(msg.getJMSCorrelationIDAsBytes());
        qRecord.setJMSDeliveryMode(msg.getJMSDeliveryMode());
        qRecord.setJMSDestination(msg.getJMSDestination());
        qRecord.setJMSRedelivered(msg.getJMSRedelivered());
        qRecord.setJMSExpiration(msg.getJMSExpiration());
        qRecord.setJMSPriority(msg.getJMSPriority());
        qRecord.setJMSTimestamp(msg.getJMSTimestamp());
        qRecord.setJMSType(msg.getJMSType());

        Enumeration propertyEnumerator = msg.getPropertyNames();
        Properties props = new Properties();
        while (propertyEnumerator.hasMoreElements()) {
          String pElement = (String) propertyEnumerator.nextElement();
          if (!(pElement == null || pElement.isEmpty())) {
            props.put(pElement, msg.getStringProperty(pElement));
          }
        }
        qRecord.setProperties(props);

        if (msg instanceof TextMessage) {
          qRecord.setText(((TextMessage) msg).getText());
        }
        if (msg instanceof ObjectMessage) {
          qRecord.setObject(((ObjectMessage) msg).getObject());
        }
        messages.add(qRecord);
      }
      return messages;
    }
  }

  /**
   *
   * @return a DrainQueueTask
   */
  @Action
  public Task drainQueue() {
    return new DrainQueueTask(getApplication());
  }

  private class DrainQueueTask extends org.jdesktop.application.Task<Object, Void> {

    private final Integer mCount;
    private final JmsTemplate dqJmsTemplate;

    DrainQueueTask(org.jdesktop.application.Application app) {
      // Copy GUI state that
      // doInBackground() depends on from parameters
      // to DrainQueueTask fields, here.
      super(app);
      mCount = messageTableModel.getRowCount();
      dqJmsTemplate = jmsTemplate;
      drainQueueMenuItem.setEnabled(false);
    }

    @Override
    protected Object doInBackground() {
      // Your Task's code here.  This method runs
      // on a background thread, so don't reference
      // the Swing GUI from here.
      Integer i = 0;
      while (i < mCount) {
        dqJmsTemplate.receive();
        i++;
      }
      return i;  // return your result
    }

    @Override
    protected void succeeded(Object result) {
      // Update the GUI based on
      // the result computed by doInBackground().
      drainQueueMenuItem.setEnabled(true);
      itemsDrainedTextField.setText(result.toString());
      queueDrainedDialog.setVisible(true);
    }
  }

  /**
   *
   */
  @Action
  public void quit() {
    int code = 0;
    try {
      Settings.saveSettings(appProperties, "Saved.");
    } catch (JTKException e) {
      System.out.println(e.toStringWithStackTrace());
      code = 1;
    } finally {
      System.exit(code);
    }
  }
}
