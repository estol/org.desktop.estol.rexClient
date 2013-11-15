/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.desktop.estol.skeleton.applicationlogic;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import org.desktop.estol.skeleton.commons.NumericUtilities;
import org.desktop.estol.skeleton.debug.DebugUtilities;

/**
 *
 * @author estol
 */
public class BroadcastScanner implements Runnable
{
    private final MulticastSocket socket;
    private final InetAddress group;
    private DatagramPacket broadcastMessage;
    private byte[] buffer = new byte[256];

    public BroadcastScanner() throws IOException
    {
        socket = new MulticastSocket(4041);
        group = InetAddress.getByName("230.0.0.1");
        socket.joinGroup(group);
        broadcastMessage = new DatagramPacket(buffer, buffer.length);
    }
    
    @Override
    public void run()
    {
        try
        {
            Thread.currentThread().setName("BroadcastScanner");
            for (int i = 0; i < 10; i++)
            {
                socket.receive(broadcastMessage);
                DebugUtilities.addDebugMessage(new String(broadcastMessage.getData(), 0, broadcastMessage.getLength()));
                Thread.sleep(NumericUtilities.ONE_SECOND * 3);
            }        
            socket.leaveGroup(group);
            socket.close();
        }
        catch (IOException | InterruptedException ex)
        {
            DebugUtilities.addDebugMessage(ex.getMessage());
        }
    }
}
