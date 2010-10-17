/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.warpaint.util.ui;

import com.warpaint.util.ui.SimpleToolbar.ToolTransferHandler;
import java.awt.Color;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import javax.swing.JLabel;
import javax.swing.TransferHandler;

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
        this.setBorder(null);

        //this.setBorder(new javax.swing.border.LineBorder(Color.GRAY));
        
    }

    public void mouseClicked(MouseEvent arg0) {
        tool.doAction(new ActionEvent(this, ActionEvent.ACTION_PERFORMED,"MouseClicked", arg0.getModifiersEx()));
    }

    public void mousePressed(MouseEvent e) {
      
    }
    public void mouseReleased(MouseEvent arg0) {
        //tool.doAction(new ActionEvent(this, ActionEvent.ACTION_PERFORMED,"MouseReleased", arg0.getModifiersEx()));
    }

    public void mouseEntered(MouseEvent arg0) {
        //this.setBorder(new javax.swing.border.LineBorder(Color.BLUE));
    }

    public void mouseExited(MouseEvent arg0) {
        //this.setBorder(null);
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
