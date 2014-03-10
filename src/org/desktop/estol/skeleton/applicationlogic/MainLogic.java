package org.desktop.estol.skeleton.applicationlogic;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import javax.swing.JOptionPane;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
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
            DebugUtilities.addDebugMessage("Command was sent: " + cmd);
            oos.writeObject(co);
        }
        catch (IOException ex)
        {
            JOptionPane.showMessageDialog(null,
                    "The following error occured while trying to send command \"" + cmd + "\"\n" + ex.getMessage(),
                    "Error sending command!", JOptionPane.ERROR_MESSAGE);
            DebugUtilities.addDebugMessage("sendCommand method failed: " + ex.getMessage());
        }
    }
    
    public synchronized TreeModel getTree(Object path) throws IOException, ClassNotFoundException
    {
        if (path instanceof TreePath)
        {
            TreePath pth = (TreePath) path;
            Object[] bits = pth.getPath();
            StringBuilder sb = new StringBuilder();
            for (Object o : bits)
            {
                sb.append("/");
                sb.append(o);
            }
            //DebugUtilities.addDebugMessage(sb.toString());
            sendCommand("ls:" + sb.toString());
            CommunicationInterface response = getResponse();
            return (TreeModel) response.getPayload();
        }
        else if (path instanceof String)
        {
            String pth = (String) path;
            if ("ls:|root|".equals(pth))
            {
                sendCommand(pth);
                CommunicationInterface response = getResponse();
                return (TreeModel) response.getPayload();
            }
        }

        throw new ClassNotFoundException("Unknown object passed");        
    }
    
    public synchronized TreeModel getTree() throws IOException, ClassNotFoundException
    {
        return getTree("ls:|root|");
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
    
    public boolean isConnected()
    {
        if (socket != null)
        {
            return socket.isConnected();
        }
        return false;
    }
}
