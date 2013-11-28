/*
 * Copyright (C) 2013 estol
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.desktop.estol.skeleton.windows;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import org.desktop.estol.skeleton.applicationlogic.MainLogic;
import org.desktop.estol.skeleton.debug.DebugUtilities;
import org.desktop.estol.skeleton.system.windowloader.LoadWindow;

/**
 *
 * @author estol
 */
public class CustomCommand extends javax.swing.JFrame
{

    /**
     * Creates new form CustomCommand
     */
    public CustomCommand()
    {
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        ta_ResponseArea = new javax.swing.JTextArea();
        tf_CommandField = new javax.swing.JTextField();
        bt_SendCommand = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Custom command");
        setMinimumSize(new java.awt.Dimension(282, 150));

        ta_ResponseArea.setEditable(false);
        ta_ResponseArea.setColumns(20);
        ta_ResponseArea.setRows(5);
        jScrollPane1.setViewportView(ta_ResponseArea);

        tf_CommandField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tf_CommandFieldKeyPressed(evt);
            }
        });

        bt_SendCommand.setText("Submit");
        bt_SendCommand.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bt_SendCommandActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 450, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(tf_CommandField)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(bt_SendCommand, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 197, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(9, 9, 9)
                        .addComponent(tf_CommandField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addComponent(bt_SendCommand)))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void tf_CommandFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tf_CommandFieldKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER)
        {
            sendMessage();
            commands.add(tf_CommandField.getText());
        }
        if (evt.getKeyCode() == KeyEvent.VK_UP)
        {
            current = tf_CommandField.getText();
            Iterator<String> iterator = commands.iterator();
            if (iterator.hasNext())
            {
                tf_CommandField.setText(iterator.next());
            }
            else
            {
                tf_CommandField.setText(current);
            }    
        }
    }//GEN-LAST:event_tf_CommandFieldKeyPressed

    private void bt_SendCommandActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bt_SendCommandActionPerformed
        sendMessage();
    }//GEN-LAST:event_bt_SendCommandActionPerformed

    @Override
    public void dispose()
    {
        LoadWindow.LoadWindow.Destroyed(this);
        super.dispose();
    }

    
    private void sendMessage()
    {
        try
        {
            String command = tf_CommandField.getText();
            tf_CommandField.setText("");
            long sendTime = System.currentTimeMillis();
            MainLogic.MainLogic.sendCommand(command);
            tf_CommandField.setEnabled(false);
            bt_SendCommand.setEnabled(false);
            tf_CommandField.setText("Waiting for response...");
            String response = MainLogic.MainLogic.getResponse().getPayload().toString();
            long responseTime = System.currentTimeMillis();
            ta_ResponseArea.setText(ta_ResponseArea.getText() + "[COMMAND] " + command + "\n[RESPONSE] " + response + "\n[RESPONSE TIME] " + (responseTime - sendTime) + " MS\n");
            tf_CommandField.setText("");
            tf_CommandField.setEnabled(true);
            bt_SendCommand.setEnabled(true);
        }
        catch (IOException | ClassNotFoundException ex)
        {
            DebugUtilities.addDebugMessage(ex.getMessage());
        }
    }

    
    private ArrayList<String> commands = new ArrayList();
    private String current;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bt_SendCommand;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea ta_ResponseArea;
    private javax.swing.JTextField tf_CommandField;
    // End of variables declaration//GEN-END:variables
}