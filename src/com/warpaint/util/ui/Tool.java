/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.warpaint.util.ui;

import java.awt.event.ActionEvent;
import java.net.URI;
import javax.swing.Icon;


/**
 *
 * @author telamon
 */
public interface Tool {
    public URI getURI();
    public String getName();
    public Icon getIcon();
    public void doAction(ActionEvent e);
    
}
