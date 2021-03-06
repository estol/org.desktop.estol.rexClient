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

package org.desktop.estol.skeleton.applicationlogic;

import java.awt.TrayIcon;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import org.desktop.estol.skeleton.commons.NotificationIcon;
import org.desktop.estol.skeleton.debug.DebugUtilities;
import org.desktop.estol.skeleton.system.exceptions.InternalErrorException;
import org.desktop.estol.skeleton.system.windowloader.LoadWindow;
import org.desktop.estol.skeleton.windows.Connect;

/**
 *
 * @author Péter Szabó
 */
public class BroadcastScanner implements Runnable
{
    private final MulticastSocket socket;
    private final InetAddress group;
    private final DatagramPacket broadcastMessage;
    private final byte[] buffer = new byte[256];
    
    public static ScannerStates state = ScannerStates.INIT;
    
    private HashMap<String, String> packets = new HashMap(); 
    
    private MessageDigest md;

    public BroadcastScanner() throws IOException, NoSuchAlgorithmException
    {
        socket = new MulticastSocket(4041);
        //NetworkInterface nif = NetworkInterface.getByInetAddress(InetAddress.);
        group = InetAddress.getByName("230.0.0.1");
        socket.joinGroup(group);
        broadcastMessage = new DatagramPacket(buffer, buffer.length);
        md = MessageDigest.getInstance("SHA-256");
    }
    
    public ScannerStates getState()
    {
        return state;
    }
    
    private void setState(ScannerStates state)
    {
        try {
            this.state = state;
            Connect connect = (Connect)LoadWindow.getFrame("Connect");
            
            if (state == ScannerStates.SCAN)
            {
                connect.getProgressBar().setIndeterminate(true);
            }
            
            if (state == ScannerStates.VALID || state == ScannerStates.INVALID || state == ScannerStates.FAIL)
            {
                connect.getProgressBar().setIndeterminate(false);
            }
        } catch (InternalErrorException ex) {
            DebugUtilities.addDebugMessage(ex.getMessage());
        }
    }
    
    private String getServerInfo()
    {
        Collection<String> values = packets.values();
        Iterator iterator = values.iterator();
        boolean flag = true;
        String prevHash = null;
        while (iterator.hasNext())
        {
            if (prevHash != null)
            {
                if (prevHash.equals((String)iterator.next()))
                {
                    flag = true;
                }
                else
                {
                    flag = false;
                }
            }
            else
            {
                prevHash = (String)iterator.next();
            }
        }
        if (flag)
        {
            setState(ScannerStates.VALID);
            return packets.keySet().iterator().next().substring(3);
        }
        else
        {
            setState(ScannerStates.INVALID);
            return null;
        }
    }
    
    @Override
    public void run()
    {
        try
        {
            setState(ScannerStates.SCAN);
            Thread.currentThread().setName("BroadcastScanner"); 
            socket.setSoTimeout(25000);
            int recieved = 0;
            while (packets.size() <= 2)
            {
                socket.receive(broadcastMessage);
                md.update(broadcastMessage.getData());
                byte[] hash = md.digest();
                StringBuilder sb = new StringBuilder();
                for(byte hashByte : hash)
                {
                    sb.append(Integer.toString((hashByte & 0xff) + 0x100, 16).substring(1));
                }
                packets.put(recieved + ". " + new String(broadcastMessage.getData(), 0, broadcastMessage.getLength()), sb.toString());
                DebugUtilities.addDebugMessage(new String(broadcastMessage.getData(), 0, broadcastMessage.getLength()));
                recieved++;
            }
            DebugUtilities.addDebugMessage("loop ended in broadcastscanner");
            //LoadWindow.disposeFrame("Connect");
            socket.leaveGroup(group);
            socket.close();
            MainLogic.MainLogic.setupTCPConnection(getServerInfo());
            
        }
        catch (IOException ex)
        {
            setState(ScannerStates.FAIL);
            NotificationIcon.displayMessage("This is odd", "More than 40 seconds passed, and no server was detected so far.\n"
                    + "Are you sure the server is running?", TrayIcon.MessageType.INFO);
            DebugUtilities.addDebugMessage("Automagic setup experienced a problem: " + ex.getMessage());
        }
    }
}
