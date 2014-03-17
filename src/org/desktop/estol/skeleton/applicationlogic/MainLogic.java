package org.desktop.estol.skeleton.applicationlogic;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.tree.DefaultMutableTreeNode;
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
        try
        {
            String[] parsedServerInfo = serverInfo.split(":");
            socket = new Socket(InetAddress.getByName(parsedServerInfo[0]), Integer.parseInt(parsedServerInfo[1]));
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());
            //JOptionPane.showMessageDialog(null, "Successfully connected to " + parsedServerInfo[0] + " on port " + parsedServerInfo[1] + "!");
            sendCommand("auth");
            String response = (String) getResponse().getPayload();
            switch (response)
            {
                case "hash":
                {
                    JPanel loginDialog = new JPanel();
                    loginDialog.setLayout(new BoxLayout(loginDialog, BoxLayout.PAGE_AXIS));
                    JTextField username = new JTextField(32);
                    JPasswordField password = new JPasswordField();
                    loginDialog.add(new JLabel("Username: "));
                    loginDialog.add(username);
                    loginDialog.add(Box.createVerticalStrut(20));
                    loginDialog.add(new JLabel("Password: "));
                    loginDialog.add(password);
                    
                    int result = JOptionPane.showConfirmDialog(null, loginDialog, "Please enter your login credentials!", JOptionPane.OK_CANCEL_OPTION);
                    if (result == JOptionPane.OK_OPTION)
                    {
                        StringBuilder sb = new StringBuilder();
                        sb.append("authAs:");
                        sb.append(username.getText());
                        sb.append("-");
                        sb.append(password.getPassword()); // TODO: encode password
                        sendCommand(sb.toString());
                        if ("ok".equals(((String)getResponse().getPayload())))
                        {
                            JOptionPane.showMessageDialog(null, "Successfully authenticated!", "Login successful!", JOptionPane.ERROR_MESSAGE);
                        }
                        else
                        {
                            JOptionPane.showMessageDialog(null, "Error authenticating. Invalid username or password!", "Login failed!", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                    else
                    {
                        disconnectTCPConnection();
                    }
                    break;
                }
                /*
                case "totp":
                {
                    JOptionPane.showMessageDialog(null, "Time-based One Time password authentication not implemented!", "Error!", JOptionPane.INFORMATION_MESSAGE);
                }
                */
                default:
                {
                    JOptionPane.showMessageDialog(null, "Bad response.", "Error!", JOptionPane.INFORMATION_MESSAGE);
                }
            }
            try
            {
                LoadWindow.getFrame("Connect").dispose();
            }
            catch (InternalErrorException ex)
            {
                DebugUtilities.addDebugMessage("Error disposing connect window" + ex.getMessage());
            }
        } catch (IOException | ClassNotFoundException ex) {
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
    
    public synchronized ArrayList<DefaultMutableTreeNode> getNodes(Object path) throws IOException, ClassNotFoundException
    {
        if (path instanceof TreePath)
        {
            TreePath pth = (TreePath) path;
            Object[] bits = pth.getPath();
            StringBuilder sb = new StringBuilder();
            for (Object o : bits)
            {
                if (!((String)((DefaultMutableTreeNode) o).getUserObject()).startsWith("/"))
                {
                    sb.append("/");
                }
                sb.append(o);
            }
            //DebugUtilities.addDebugMessage(sb.toString());
            sendCommand("ls:" + sb.toString());
            CommunicationInterface response = getResponse();
            return (ArrayList<DefaultMutableTreeNode>) response.getPayload();
        }
        else if (path instanceof String)
        {
            String pth = (String) path;
            if ("ls:|root|".equals(pth))
            {
                sendCommand(pth);
                CommunicationInterface response = getResponse();
                return (ArrayList<DefaultMutableTreeNode>) response.getPayload();
            }
        }

        throw new ClassNotFoundException("Unknown object passed");        
    }
    
    public synchronized ArrayList<DefaultMutableTreeNode> getNodes() throws IOException, ClassNotFoundException
    {
        return getNodes("ls:|root|");
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
