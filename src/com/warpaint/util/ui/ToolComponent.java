/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.warpaint.util.ui;

import java.awt.Color;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import javax.swing.JLabel;

/**
 *
 * @author telamon
 */
public class ToolComponent extends JLabel implements MouseListener, Transferable{
    protected Tool tool;
    public ToolComponent(Tool t){
        tool = t;
        this.setIcon(t.getIcon());
        this.setToolTipText(t.getName());
        this.addMouseListener(this);
    }

    public void mouseClicked(MouseEvent arg0) {
        tool.doAction(new ActionEvent(this, ActionEvent.ACTION_PERFORMED,"MouseClicked", arg0.getModifiersEx()));
    }

    public void mousePressed(MouseEvent arg0) {

    }

    public void mouseReleased(MouseEvent arg0) {
        tool.doAction(new ActionEvent(this, ActionEvent.ACTION_PERFORMED,"MouseReleased", arg0.getModifiersEx()));
    }

    public void mouseEntered(MouseEvent arg0) {
        this.setBorder(new javax.swing.border.LineBorder(Color.BLUE));
    }

    public void mouseExited(MouseEvent arg0) {
        this.setBorder(null);
    }
    public static final DataFlavor[] flavours = new DataFlavor[]{new DataFlavor(Tool.class,DataFlavor.javaJVMLocalObjectMimeType)};
    public DataFlavor[] getTransferDataFlavors() {
        return flavours;
    }

    public boolean isDataFlavorSupported(DataFlavor arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object getTransferData(DataFlavor arg0) throws UnsupportedFlavorException, IOException {
        return tool;
    }

}
