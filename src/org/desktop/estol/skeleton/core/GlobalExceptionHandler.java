/*
 * Copyright (C) 2014 Péter Szabó - estol - pwyvern@gmail.com
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

package org.desktop.estol.skeleton.core;

import java.lang.Thread.UncaughtExceptionHandler;
import org.desktop.estol.skeleton.commons.ObjectStreamWriter;

/**
 *
 * @author estol
 */
public class GlobalExceptionHandler implements UncaughtExceptionHandler
{
    /**
     * TODO decide if the program is in working state after the exception, and try
     * to continue if it is.
     * 
     * @param t
     * @param e 
     */
    @Override
    public void uncaughtException(Thread t, Throwable e)
    {
        long epoch = System.currentTimeMillis() / 1000L;
        String dumpFileName = Long.toString(epoch) + ".dumpobject";
        new Thread(new ObjectStreamWriter(new dumpObject(t, e), dumpFileName)).start();
        System.exit(1);
    }
}
