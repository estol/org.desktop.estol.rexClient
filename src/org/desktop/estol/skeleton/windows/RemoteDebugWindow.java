/*
 * Copyright (C) 2014 Péter Szabó
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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Iterator;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import org.clientserver.estol.commobject.CommunicationInterface;
import org.clientserver.estol.commobject.CommunicationObject;
import org.clientserver.estol.commobject.CommunicationObjectObjectPayload;
import org.desktop.estol.skeleton.applicationlogic.MainLogic;
import org.desktop.estol.skeleton.commons.NumericUtilities;
import org.desktop.estol.skeleton.commons.ThreadedUtility;
import org.desktop.estol.skeleton.debug.DebugUtilities;
import org.desktop.estol.skeleton.system.windowloader.LoadWindow;
import org.headless.estol.debugobject.DebugInformationObject;
import org.headless.estol.debugobject.DebugObject;
import org.headless.estol.debugobject.MemoryInformationObject;
import org.headless.estol.debugobject.ThreadInformationObject;

/**
 *
 * @author Péter Szabó
 */
public class RemoteDebugWindow extends javax.swing.JFrame
{
    private class PackageHandler implements Runnable, ThreadedUtility
    {
        ObjectInputStream ois;
        ObjectOutputStream oos;
        boolean runFlag = true;
        long sleepInterval = (long)(NumericUtilities.ONE_SECOND * 1.5);
        JLabel currentThread, peakThread; // labels for threads
        JLabel totalMemory, freeMemory, maxMemory, usedMemory, peakMemory, peakThreadNoreset; //  labels for the memory usage
        JTextArea log; // textarea for the log
        
        PackageHandler(ObjectInputStream ois, ObjectOutputStream oos)
        {
            this.ois = ois;
            this.oos = oos;
        }
        
        void setDisplayElements(JLabel currentThread, JLabel peakThread, JLabel totalMemory, JLabel freeMemory, JLabel maxMemory, JLabel usedMemory, JLabel peakMemory, JLabel peakThreadNoreset, JTextArea log)
        {
            this.currentThread     = currentThread;
            this.peakThread        = peakThread;
            this.peakThreadNoreset = peakThreadNoreset;

            this.totalMemory       = totalMemory;
            this.freeMemory        = freeMemory;
            this.maxMemory         = maxMemory;
            this.usedMemory        = usedMemory;
            this.peakMemory        = peakMemory;
            
            this.log               = log;
        }

        @Override
        public void run()
        {
            Thread.currentThread().setName("Remote debug info reader");
            while (runFlag)
            {
                try
                {
                    sendCommand("full"); // TODO read this from a gui element
                    CommunicationInterface object = getResponse();
                    if (object instanceof CommunicationObjectObjectPayload)
                    {
                        DebugObject dObject = (DebugObject) object.getPayload();
                        DebugInformationObject dio  = dObject.getDebugInformationObject();
                        MemoryInformationObject mio = dObject.getMemoryInformationObject();
                        ThreadInformationObject tio = dObject.getThreadInformationObject();
                        
                        currentThread.setText("Current: " + tio.getCurrentTC());
                        peakThread.setText("Peak (GC): " + tio.getPeakTC());
                        peakThreadNoreset.setText("Peak (RT): " + tio.getResetTC());
                        
                        
                        totalMemory.setText(mio.getTotal());
                        freeMemory.setText(mio.getFree());
                        maxMemory.setText(mio.getMax());
                        usedMemory.setText(mio.getUsed());
                        peakMemory.setText(mio.getPeak());
                        
                        // do this last, probably the slowest with the loop
                        // although does it matter? repaint will probably wait till all updates are done
                        // eff Swing, next application I'm writing will be javafx, or even better
                        // eff java too, and use C++ and have a proper user interface defined with Qt
                        Map messages = dio.getDebugMessages();
                        Iterator iterator = messages.entrySet().iterator();
                        while (iterator.hasNext())
                        {
                            Map.Entry<String, String> pairs = (Map.Entry) iterator.next();
                            log.setText(log.getText() + "\n" + pairs.getKey() + " - " + pairs.getValue());
                            iterator.remove();
                        }
                        Thread.sleep(sleepInterval);
                    }

                }
                catch (IOException | ClassNotFoundException | InterruptedException ex)
                {
                    DebugUtilities.addDebugMessage(ex.getMessage());
                }
            }
        }

        @Override
        public void display()
        {
            new Thread(this).start();
        }

        @Override
        public void shutdown()
        {
            runFlag = false;
            disconnectTCPConnection();
            //MainLogic.MainLogic.sendCommand("EndDebug");
        }

        @Override
        public boolean isRunning()
        {
            return runFlag;
        }
        
    }
    

    /**
     * Creates new form RemoteDebugWindow
     */
    public RemoteDebugWindow()
    {
        try {
            initComponents();
            MainLogic.MainLogic.sendCommand("BeginDebug");
            debugServer = (String) MainLogic.MainLogic.getResponse().getPayload();
            setupTCPConnection(debugServer);
            ph = new PackageHandler(ois, oos);
            ph.setDisplayElements(lbl_CurrentThreads, lbl_PeakThreads, lbl_TotalMemory, lbl_FreeMemory, lbl_MaxMemory, lbl_UsedMemory, lbl_PeakMemory, lbl_PeakThreadsNoreset, ta_LogOutput);
            ph.display();
        }
        catch (IOException | ClassNotFoundException ex)
        {
            DebugUtilities.addDebugMessage(ex.getMessage());
        }
    }
    
    private synchronized void setupTCPConnection(String serverInfo)
    {
        try
        {
            String[] parsedServerInfo = serverInfo.split(":");
            socket = new Socket(InetAddress.getByName(parsedServerInfo[0]), Integer.parseInt(parsedServerInfo[1]));
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream (socket.getInputStream());
            DebugUtilities.addDebugMessage("Successfully connected to debug server " + parsedServerInfo[0] + " on port " + parsedServerInfo[1] + "!");
        }
        catch (IOException ex)
        {
            JOptionPane.showMessageDialog(this, "Couldn't connect to server\nThe following error occured:\n" + ex.getMessage());
            DebugUtilities.addDebugMessage(ex.getMessage());
        }
    }
    
    public void disconnectTCPConnection()
    {
        try
        {
            oos.close();
            ois.close();
            socket.close();
            DebugUtilities.addDebugMessage("Disconnected from debug server!");
        }
        catch (IOException | NullPointerException ex)
        {
            DebugUtilities.addDebugMessage(ex.getMessage());
            if (ex instanceof NullPointerException)
            {
                JOptionPane.showMessageDialog(null,
                        "Can't disconnect. Not even connected");
            }
        }
    }
    
    public synchronized void sendCommand(String cmd)
    {
        try
        {
            if (socket.isClosed())
            {
                DebugUtilities.addDebugMessage("The socket is "
                        + "closed for some reason...");
            }
            CommunicationObject co = new CommunicationObject(cmd);
            oos.writeObject(co);
        }
        catch (IOException ex)
        {
            // TODO: notify the user that the sending failed.
            DebugUtilities.addDebugMessage("sendCommand method failed: " + ex.getMessage());
        }
    }
    
    public synchronized CommunicationInterface getResponse()
            throws IOException, ClassNotFoundException
    {
        CommunicationInterface response =
                (CommunicationInterface) ois.readObject();
        if (response.getPayload() instanceof String)
        {
            if ("The server is shutting down!".equals((String)response.getPayload()))
            {
                disconnectTCPConnection();
            }
        }
        return response;
    }
    // expected inputs on the other end: full ; memory ; thread ; log
    // ATM only full debug object is supporetd
    // TODO: support partial debug objects
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        lbl_TotalMemory = new javax.swing.JLabel();
        lbl_FreeMemory = new javax.swing.JLabel();
        lbl_MaxMemory = new javax.swing.JLabel();
        lbl_UsedMemory = new javax.swing.JLabel();
        lbl_PeakMemory = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        lbl_CurrentThreads = new javax.swing.JLabel();
        lbl_PeakThreads = new javax.swing.JLabel();
        lbl_PeakThreadsNoreset = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        ta_LogOutput = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Remote Debug Console");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Memory and Thread information"));

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Memory"));

        lbl_TotalMemory.setText("jLabel1");
        lbl_TotalMemory.setMaximumSize(new java.awt.Dimension(80, 14));
        lbl_TotalMemory.setMinimumSize(new java.awt.Dimension(80, 14));
        lbl_TotalMemory.setPreferredSize(new java.awt.Dimension(80, 14));

        lbl_FreeMemory.setText("jLabel2");
        lbl_FreeMemory.setMaximumSize(new java.awt.Dimension(80, 14));
        lbl_FreeMemory.setMinimumSize(new java.awt.Dimension(80, 14));
        lbl_FreeMemory.setPreferredSize(new java.awt.Dimension(80, 14));

        lbl_MaxMemory.setText("jLabel3");
        lbl_MaxMemory.setMaximumSize(new java.awt.Dimension(80, 14));
        lbl_MaxMemory.setMinimumSize(new java.awt.Dimension(80, 14));
        lbl_MaxMemory.setPreferredSize(new java.awt.Dimension(80, 14));

        lbl_UsedMemory.setText("jLabel4");
        lbl_UsedMemory.setMaximumSize(new java.awt.Dimension(80, 14));
        lbl_UsedMemory.setMinimumSize(new java.awt.Dimension(80, 14));
        lbl_UsedMemory.setPreferredSize(new java.awt.Dimension(80, 14));

        lbl_PeakMemory.setText("jLabel5");
        lbl_PeakMemory.setMaximumSize(new java.awt.Dimension(80, 14));
        lbl_PeakMemory.setMinimumSize(new java.awt.Dimension(80, 14));
        lbl_PeakMemory.setPreferredSize(new java.awt.Dimension(80, 14));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap(24, Short.MAX_VALUE)
                .addComponent(lbl_TotalMemory, javax.swing.GroupLayout.PREFERRED_SIZE, 79, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lbl_FreeMemory, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lbl_MaxMemory, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lbl_UsedMemory, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lbl_PeakMemory, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap(25, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbl_TotalMemory, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lbl_FreeMemory, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lbl_MaxMemory, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lbl_UsedMemory, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lbl_PeakMemory, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(27, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Threads"));

        lbl_CurrentThreads.setText("jLabel1");

        lbl_PeakThreads.setText("jLabel2");

        lbl_PeakThreadsNoreset.setText("jLabel1");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lbl_CurrentThreads, javax.swing.GroupLayout.DEFAULT_SIZE, 128, Short.MAX_VALUE)
                    .addComponent(lbl_PeakThreads, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lbl_PeakThreadsNoreset, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(lbl_CurrentThreads)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lbl_PeakThreads)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lbl_PeakThreadsNoreset))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Console Messages"));

        ta_LogOutput.setEditable(false);
        ta_LogOutput.setColumns(20);
        ta_LogOutput.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        ta_LogOutput.setLineWrap(true);
        ta_LogOutput.setRows(5);
        ta_LogOutput.setWrapStyleWord(true);
        jScrollPane1.setViewportView(ta_LogOutput);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 297, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    @Override
    public void dispose()
    {
        LoadWindow.LoadWindow.Destroyed(this);
        ph.shutdown();
        super.dispose();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lbl_CurrentThreads;
    private javax.swing.JLabel lbl_FreeMemory;
    private javax.swing.JLabel lbl_MaxMemory;
    private javax.swing.JLabel lbl_PeakMemory;
    private javax.swing.JLabel lbl_PeakThreads;
    private javax.swing.JLabel lbl_PeakThreadsNoreset;
    private javax.swing.JLabel lbl_TotalMemory;
    private javax.swing.JLabel lbl_UsedMemory;
    private javax.swing.JTextArea ta_LogOutput;
    // End of variables declaration//GEN-END:variables
    private String debugServer;
    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream  ois;
    private PackageHandler ph;
}
