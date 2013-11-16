/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.desktop.estol.skeleton.applicationlogic;

import java.awt.TrayIcon;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import org.desktop.estol.skeleton.commons.NotificationIcon;
import org.desktop.estol.skeleton.debug.DebugUtilities;

/**
 *
 * @author estol
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
        group = InetAddress.getByName("230.0.0.1");
        socket.joinGroup(group);
        broadcastMessage = new DatagramPacket(buffer, buffer.length);
        md = MessageDigest.getInstance("SHA-256");
    }
    
    public String getServerInfo()
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
            state = ScannerStates.VALID;
            return packets.keySet().iterator().next();
        }
        else
        {
            state = ScannerStates.INVALID;
            return null;
        }
    }
    
    @Override
    public void run()
    {
        try
        {
            state = ScannerStates.SCAN;
            Thread.currentThread().setName("BroadcastScanner"); 
            socket.setSoTimeout(40000);
            while (packets.size() < 5)
            {
                socket.receive(broadcastMessage);
                md.update(broadcastMessage.getData());
                byte[] hash = md.digest();
                StringBuilder sb = new StringBuilder();
                for(byte hashByte : hash)
                {
                    sb.append(Integer.toString((hashByte & 0xff) + 0x100, 16).substring(1));
                }
                packets.put(new String(broadcastMessage.getData(), 0, broadcastMessage.getLength()), sb.toString());
                DebugUtilities.addDebugMessage(new String(broadcastMessage.getData(), 0, broadcastMessage.getLength()));
            }
            socket.leaveGroup(group);
            socket.close();
            
        }
        catch (IOException ex)
        {
            state = ScannerStates.FAIL;
            NotificationIcon.displayMessage("This is odd", "More than 40 seconds passed, and no server was detected so far.\n"
                    + "Are you sure the server is running?", TrayIcon.MessageType.INFO);
            DebugUtilities.addDebugMessage("Automagic setup experienced a problem: " + ex.getMessage());
        }
    }
}
