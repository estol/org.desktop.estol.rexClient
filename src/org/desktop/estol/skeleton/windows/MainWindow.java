package org.desktop.estol.skeleton.windows;


import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.desktop.estol.skeleton.commons.NotificationIcon;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import org.desktop.estol.skeleton.applicationlogic.MainLogic;
import org.desktop.estol.skeleton.debug.DebugUtilities;
import org.desktop.estol.skeleton.system.windowloader.LoadWindow;

/**
 *
 * @author estol
 */
public class MainWindow extends javax.swing.JFrame {
    
    /**
     * Creates new form MainWindow
     */
    public MainWindow()
    {
        initComponents();
        NotificationIcon.initSystrayIcon();
        tree_FileSystem.addTreeExpansionListener(new TreeExpansionListener()
        {
            @Override
            public void treeExpanded(TreeExpansionEvent event)
            {
                try
                {
                    /*
                    DefaultTreeModel tree = (DefaultTreeModel) MainLogic.MainLogic.getTree(event.getPath());
                    DefaultMutableTreeNode relativeRoot = (DefaultMutableTreeNode)event.getPath().getLastPathComponent();
                    relativeRoot.removeAllChildren();
                    DefaultTreeModel displayTree = (DefaultTreeModel) tree_FileSystem.getModel();
                    
                    Enumeration en = ((DefaultMutableTreeNode)tree.getRoot()).depthFirstEnumeration();
                    while(en.hasMoreElements())
                    {
                        DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) en.nextElement();
                        DebugUtilities.addDebugMessage("The current node("+ currentNode +") has " + currentNode.getChildCount() + " children.");
                        displayTree.insertNodeInto((DefaultMutableTreeNode) en.nextElement(), relativeRoot, relativeRoot.getChildCount());
                    }
                    */
                    DefaultTreeModel newTree = (DefaultTreeModel) MainLogic.MainLogic.getTree(event.getPath());
                    DefaultMutableTreeNode root = (DefaultMutableTreeNode) event.getPath().getLastPathComponent();
                    root.removeAllChildren();
                    DefaultTreeModel oldTree = (DefaultTreeModel) tree_FileSystem.getModel();
                    addChildNodes(oldTree, newTree, root);
                }
                catch (IOException | ClassNotFoundException ex)
                {
                    
                }
            }

            void addChildNodes(DefaultTreeModel oldTree, DefaultTreeModel newTree, DefaultMutableTreeNode parent)
            {
                Enumeration<DefaultMutableTreeNode> en = ((DefaultMutableTreeNode)newTree.getRoot()).depthFirstEnumeration();
                
                HashMap<DefaultMutableTreeNode, Boolean> nodes = new HashMap<>();
                
                while (en.hasMoreElements())
                {                    
                    if (en.nextElement().getChildCount() == 0)
                    {
                        nodes.put(en.nextElement(), false);
                    }
                    else
                    {
                        nodes.put(en.nextElement(), false);
                    }
                    en.nextElement().removeAllChildren();
                }
                
                Iterator iterator = nodes.entrySet().iterator();
                while (iterator.hasNext())
                {
                    Map.Entry<DefaultMutableTreeNode, Boolean> pair = (Map.Entry)iterator.next();
                    if (pair.getValue())
                    {
                        pair.getKey().add(new DefaultMutableTreeNode("Working... Please wait! (client)"));
                    }
                    oldTree.insertNodeInto(pair.getKey(), parent, parent.getChildCount());
                }
                
                /*
                while (en.hasMoreElements())
                {
                    if (en.nextElement().getChildCount() != 0)
                    {
                        en.nextElement().removeAllChildren();
                        en.nextElement().add(new DefaultMutableTreeNode("Workng... Please wait! (client)"));
                        oldTree.insertNodeInto(child, parent, parent.getChildCount());
                    }
                }*/
            }

            @Override
            public void treeCollapsed(TreeExpansionEvent event)
            {
                //tree_FileSystem.getModel().
            }
        
        });
        //throw new Exception();
        //hideTimePanel();
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane3 = new javax.swing.JScrollPane();
        tree_FileSystem = new javax.swing.JTree();
        jToolBar1 = new javax.swing.JToolBar();
        jButton1 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        MenuBar = new javax.swing.JMenuBar();
        FileMenu = new javax.swing.JMenu();
        m_Connect = new javax.swing.JMenuItem();
        m_Disconnect = new javax.swing.JMenuItem();
        m_About = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        m_ExitButton = new javax.swing.JMenuItem();
        CommandMenu = new javax.swing.JMenu();
        VideoCommandsMenu = new javax.swing.JMenu();
        m_BlankVideoCommand = new javax.swing.JMenuItem();
        AudioCommandsMenu = new javax.swing.JMenu();
        m_BlankAudioCommand = new javax.swing.JMenuItem();
        ArchiveCommandsMenu = new javax.swing.JMenu();
        m_BlankArchiveCommand = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        m_CustomCommand = new javax.swing.JMenuItem();
        SettingsMenu = new javax.swing.JMenu();
        Preferences = new javax.swing.JMenuItem();
        DebugMenu = new javax.swing.JMenu();
        FireDebugMethod = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        LocalDebugConsole = new javax.swing.JMenuItem();
        RemoteDebugConsole = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Remote Executor Client");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setMinimumSize(new java.awt.Dimension(423, 600));
        setName("JFrame"); // NOI18N

        tree_FileSystem.setModel(treeStructure());
        jScrollPane3.setViewportView(tree_FileSystem);

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/desktop/estol/skeleton/ImageAssets/Actions-network-disconnect-icon.png"))); // NOI18N
        jButton1.setFocusable(false);
        jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jButton1);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 900, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 36, Short.MAX_VALUE)
        );

        MenuBar.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N

        FileMenu.setText("File");
        FileMenu.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N

        m_Connect.setText("Connect");
        m_Connect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_ConnectActionPerformed(evt);
            }
        });
        FileMenu.add(m_Connect);

        m_Disconnect.setText("Disconnect");
        m_Disconnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_DisconnectActionPerformed(evt);
            }
        });
        FileMenu.add(m_Disconnect);

        m_About.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        m_About.setText("About");
        m_About.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_AboutActionPerformed(evt);
            }
        });
        FileMenu.add(m_About);
        FileMenu.add(jSeparator2);

        m_ExitButton.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        m_ExitButton.setText("Exit (terminate application)");
        m_ExitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_ExitButtonActionPerformed(evt);
            }
        });
        FileMenu.add(m_ExitButton);

        MenuBar.add(FileMenu);

        CommandMenu.setText("Command");

        VideoCommandsMenu.setText("Video Commands");

        m_BlankVideoCommand.setText("Not yet implemented!");
        m_BlankVideoCommand.setEnabled(false);
        VideoCommandsMenu.add(m_BlankVideoCommand);

        CommandMenu.add(VideoCommandsMenu);

        AudioCommandsMenu.setText("Audio Commands");

        m_BlankAudioCommand.setText("Not yet implemented!");
        m_BlankAudioCommand.setEnabled(false);
        AudioCommandsMenu.add(m_BlankAudioCommand);

        CommandMenu.add(AudioCommandsMenu);

        ArchiveCommandsMenu.setText("Archive Commands");

        m_BlankArchiveCommand.setText("Not yet implemented!");
        m_BlankArchiveCommand.setEnabled(false);
        ArchiveCommandsMenu.add(m_BlankArchiveCommand);

        CommandMenu.add(ArchiveCommandsMenu);
        CommandMenu.add(jSeparator3);

        m_CustomCommand.setText("Custom Command");
        m_CustomCommand.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_CustomCommandActionPerformed(evt);
            }
        });
        CommandMenu.add(m_CustomCommand);

        MenuBar.add(CommandMenu);

        SettingsMenu.setText("Settings");
        SettingsMenu.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N

        Preferences.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        Preferences.setText("Preferences");
        Preferences.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PreferencesActionPerformed(evt);
            }
        });
        SettingsMenu.add(Preferences);

        MenuBar.add(SettingsMenu);

        DebugMenu.setText("Debug");
        DebugMenu.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        MenuBar.add(Box.createHorizontalGlue());

        FireDebugMethod.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        FireDebugMethod.setText("THE TRIGGER");
        FireDebugMethod.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FireDebugMethodActionPerformed(evt);
            }
        });
        DebugMenu.add(FireDebugMethod);
        DebugMenu.add(jSeparator1);

        LocalDebugConsole.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        LocalDebugConsole.setText("Local Debug Console");
        LocalDebugConsole.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LocalDebugConsoleActionPerformed(evt);
            }
        });
        DebugMenu.add(LocalDebugConsole);

        RemoteDebugConsole.setText("Remote Debug Console");
        RemoteDebugConsole.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RemoteDebugConsoleActionPerformed(evt);
            }
        });
        DebugMenu.add(RemoteDebugConsole);

        MenuBar.add(DebugMenu);

        setJMenuBar(MenuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 900, Short.MAX_VALUE)
            .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 507, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void FireDebugMethodActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FireDebugMethodActionPerformed
        /*try
        {
            IniParser iniparser = new IniParser("testfile.ini");
            System.out.printf("%b%n",
                    iniparser.getBoolean("section1", "boolkey", false));
        }
        catch (IOException ex)
        {
            DebugUtilities.addDebugMessage("Ini parser error: " + ex.getMessage());
        }*/
        treeStructure();
    }//GEN-LAST:event_FireDebugMethodActionPerformed

    private void LocalDebugConsoleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LocalDebugConsoleActionPerformed
        LoadWindow.LoadWindow.Load(new DebugWindow());
    }//GEN-LAST:event_LocalDebugConsoleActionPerformed

    private void m_ExitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_ExitButtonActionPerformed
        LoadWindow.LoadWindow.Terminate();
    }//GEN-LAST:event_m_ExitButtonActionPerformed

    private void PreferencesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PreferencesActionPerformed
        LoadWindow.LoadWindow.Load(new Preferences());
    }//GEN-LAST:event_PreferencesActionPerformed

    private void m_AboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_AboutActionPerformed
        LoadWindow.LoadWindow.Load(new About());
    }//GEN-LAST:event_m_AboutActionPerformed

    private void m_DisconnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_DisconnectActionPerformed
        MainLogic.MainLogic.disconnectTCPConnection();
    }//GEN-LAST:event_m_DisconnectActionPerformed

    private void m_CustomCommandActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_CustomCommandActionPerformed
        LoadWindow.LoadWindow.Load(new CustomCommand());
    }//GEN-LAST:event_m_CustomCommandActionPerformed

    private void RemoteDebugConsoleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RemoteDebugConsoleActionPerformed
        LoadWindow.LoadWindow.Load(new RemoteDebugWindow());
    }//GEN-LAST:event_RemoteDebugConsoleActionPerformed

    private void m_ConnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_ConnectActionPerformed
        LoadWindow.LoadWindow.Load(new Connect());
    }//GEN-LAST:event_m_ConnectActionPerformed

    @Override
    public void dispose()
    {
        LoadWindow.LoadWindow.Destroyed(this);
        NotificationIcon.removeSystrayIcon();
        MainLogic.MainLogic.sendCommand("socketExit");
        MainLogic.MainLogic.disconnectTCPConnection();
        super.dispose();
    }
    
    private TreeModel treeStructure()
    {
        if (MainLogic.MainLogic.isConnected())
        {
            try
            {
                TreeModel tree = MainLogic.MainLogic.getTree();
                tree_FileSystem.setModel(tree);
                return tree;
            }
            catch (IOException | ClassNotFoundException ex)
            {
                JOptionPane.showMessageDialog(null, "Error occured while getting remote filesystem structure!", "Error!", JOptionPane.ERROR_MESSAGE);
                DebugUtilities.addDebugMessage("Error occured while getting tree structure: " + ex.getMessage());
                DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Error occured, please try reconnecting!");
                TreeModel tree = new DefaultTreeModel(rootNode);
                tree_FileSystem.setModel(tree);
                return tree;
            }
        }
        else
        {
                DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Client not connected, please connect first!");
                TreeModel tree = new DefaultTreeModel(rootNode);
                tree_FileSystem.setModel(tree);
                return tree;
        }
    }
    
    protected DefaultListModel currentEventListModel = new DefaultListModel();
    protected DefaultListModel pastEventListModel = new DefaultListModel();
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu ArchiveCommandsMenu;
    private javax.swing.JMenu AudioCommandsMenu;
    private javax.swing.JMenu CommandMenu;
    private javax.swing.JMenu DebugMenu;
    private javax.swing.JMenu FileMenu;
    private javax.swing.JMenuItem FireDebugMethod;
    private javax.swing.JMenuItem LocalDebugConsole;
    private javax.swing.JMenuBar MenuBar;
    private javax.swing.JMenuItem Preferences;
    private javax.swing.JMenuItem RemoteDebugConsole;
    private javax.swing.JMenu SettingsMenu;
    private javax.swing.JMenu VideoCommandsMenu;
    private javax.swing.JButton jButton1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JMenuItem m_About;
    private javax.swing.JMenuItem m_BlankArchiveCommand;
    private javax.swing.JMenuItem m_BlankAudioCommand;
    private javax.swing.JMenuItem m_BlankVideoCommand;
    private javax.swing.JMenuItem m_Connect;
    private javax.swing.JMenuItem m_CustomCommand;
    private javax.swing.JMenuItem m_Disconnect;
    private javax.swing.JMenuItem m_ExitButton;
    private javax.swing.JTree tree_FileSystem;
    // End of variables declaration//GEN-END:variables
}
