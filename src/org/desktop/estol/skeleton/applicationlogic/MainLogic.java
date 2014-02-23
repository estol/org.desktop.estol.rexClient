package org.desktop.estol.skeleton.applicationlogic;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import javax.swing.JOptionPane;
import org.clientserver.estol.commobject.CommunicationInterface;
import org.clientserver.estol.commobject.CommunicationObject;
import org.desktop.estol.skeleton.debug.DebugUtilities;
import org.desktop.estol.skeleton.system.exceptions.InternalErrorException;
import org.desktop.estol.skeleton.system.windowloader.LoadWindow;

/**
 *
 * @author estol
 */
public enum MainLogic
{
    MainLogic;
    
    private ThreadGroup setupThreads = new ThreadGroup("SetupThreads");
    
    private Thread scannerThread;
    
    private Socket socket = null;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    
    public void initialize()
    {
        try
        {
            getUDPPackets();
        }
        catch (InterruptedException ex)
        {
            DebugUtilities.addDebugMessage(ex.getMessage());
        }
    }
    
    private boolean getUDPPackets() throws InterruptedException
    {
        try
        {
            BroadcastScanner scanner = new BroadcastScanner();
            scannerThread = new Thread(setupThreads, scanner);
            DebugUtilities.addDebugMessage(setupThreads.activeCount() + "");
            if (setupThreads.activeCount() == 0)
            {
                scannerThread.start();
                DebugUtilities.addDebugMessage(setupThreads.activeCount() + "");
            }
        }
        catch (IOException | NoSuchAlgorithmException ex)
        {
            DebugUtilities.addDebugMessage(ex.getMessage());
        }
        return true;
    }
    
    public synchronized void setupTCPConnection(String serverInfo)
    {
        try {
            String[] parsedServerInfo = serverInfo.split(":");
            socket = new Socket(InetAddress.getByName(parsedServerInfo[0]), Integer.parseInt(parsedServerInfo[1]));
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());
            JOptionPane.showMessageDialog(null, "Successfully connected to " + parsedServerInfo[0] + " on port " + parsedServerInfo[1] + "!");
            try {
                LoadWindow.getFrame("Connect").dispose();
            } catch (InternalErrorException ex) {
                DebugUtilities.addDebugMessage("Error disposing connect window" + ex.getMessage());
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Couldn't connect to server\nThe following error occured:\n" + ex.getMessage());
            DebugUtilities.addDebugMessage(ex.getMessage());
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
            DebugUtilities.addDebugMessage("sendCommand method failed: "
                    + ex.getMessage());
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
    
    public void disconnectTCPConnection()
    {
        try
        {
            oos.close();
            ois.close();
            socket.close();
            JOptionPane.showMessageDialog(null, "Succesfully disconnected!");
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
}
