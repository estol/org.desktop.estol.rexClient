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


package org.desktop.estol.skeleton.commons;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 *
 * @author Péter Szabó
 */
public class NumericUtilities
{
    /**
     * round a number to two decimal places.
     */
    public static final DecimalFormat TWO_DECIMAL   =
            new DecimalFormat("#0.##");
    /**
     * round a number to three decimal places.
     */
    public static final DecimalFormat THREE_DECIMAL =
            new DecimalFormat("#0.###");
    /**
     * round a number to four decimal places
     */
    public static final DecimalFormat FOUR_DECIMAL  =
            new DecimalFormat("#0.####");
    /**
     * round a number to five decimal places
     */
    public static final DecimalFormat FIVE_DECIMAL  =
            new DecimalFormat("#0.#####");
    /**
     * one minute in milliseconds
     */
    public static final long ONE_MINUTE = 60000;
    /**
     * one second in milliseconds
     */
    public static final long ONE_SECOND = 1000;

    /**
     * Rounds the passed floating point number, to 3 decimal places.
     * @param f
     * @return 
     */
    public static float roundFloat(float f)
    {
        BigDecimal bd = new BigDecimal(Float.toString(f));
        bd = bd.setScale(3, RoundingMode.HALF_UP);
        return bd.floatValue();
    }

    /**
     * Rounds the passed floating point number to the passed Scale decimal
     * places
     * @param f
     * @param Scale
     * @return 
     */
    public static float roundFloat(float f, int Scale)
    {
        BigDecimal bd = new BigDecimal(Float.toString(f));
        bd = bd.setScale(Scale, RoundingMode.HALF_UP);
        return bd.floatValue();
    }
}
