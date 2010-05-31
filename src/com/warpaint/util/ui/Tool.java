/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.warpaint.util.ui;

import java.awt.Event;
import javax.swing.ImageIcon;

/**
 *
 * @author telamon
 */
public interface Tool {
    public String getUrl();
    public String getName();
    public ImageIcon getIcon();
    public void doAction(Event e);
    
}
