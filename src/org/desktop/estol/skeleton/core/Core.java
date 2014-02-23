package org.desktop.estol.skeleton.core;

import org.desktop.estol.skeleton.system.windowloader.LoadWindow;
import org.desktop.estol.skeleton.windows.MainWindow;

/**
 * Entry point, loads the main window
 * 
 * @author estol
 */
public class Core
{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        LoadWindow.initSystem();
        LoadWindow.LoadWindow.Load(new MainWindow());
    }
}
