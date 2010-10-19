/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.warpaint.util.ui;


import com.warpaint.util.misc.EventDispatcherAdapter;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URI;
import java.util.Vector;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DropMode;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.TransferHandler;

/**
 *
 * @author telamon
 */
public class SimpleToolbar extends JList implements Toolbar, ListCellRenderer, MouseListener {
    Vector<ToolComponent> tools = new Vector<ToolComponent>();
    private int direction = 0;
    private final static int cellSize=40;
    private static class DispatchHolder{
        //Class intercommunication.
        public static final EventDispatcherAdapter<ToolbarListener> DISPATCHER = new EventDispatcherAdapter<ToolbarListener>(){
            @Override
            protected Class getListenerClass() {
                return ToolbarListener.class;
            }

            @Override
            public ToolbarListener[] exportListenersArray() {
                return getListenersArray();
            }

        };
    }

    public SimpleToolbar() {
        setFixedCellHeight(cellSize);
        setFixedCellWidth(cellSize);
        setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        setCellRenderer(this);
        setLayoutOrientation(javax.swing.JList.HORIZONTAL_WRAP);
        setVisibleRowCount(0);
        setMinimumSize(new Dimension(cellSize,cellSize));        
        this.setBackground(new Color(0x0,true));
        this.setOpaque(false);
        addMouseListener(this);
        this.setName("SimpleToolbar");
        TransferHandler th = new ToolTransferHandler();
        setTransferHandler(th);
        setDragEnabled(true);
        setDropMode(DropMode.ON_OR_INSERT);

    }
    public static void addListener(ToolbarListener l){
        DispatchHolder.DISPATCHER.addListener(l);
    }
    public static void removeListener(ToolbarListener l){
        DispatchHolder.DISPATCHER.removeListener(l);
    }
    public SimpleToolbar(Tool[] tools){
        this();
        for(Tool t: tools){
            addTool(t);
        }
    }
    public void addTool(Tool tool){
        addTool(tools.size(),tool);
    }
    public void setDirection(int d){
        direction=d;
    }

    public void addTool(int index,Tool tool) {
        ToolComponent tc = new ToolComponent(tool);        
        removeTool(tc.tool);
        index = index > tools.size() ? tools.size() : index;
        index = index < 0 ? 0 : index;
        tools.add(index,tc);
        if(direction==0){
            setPreferredSize(new Dimension(cellSize*tools.size() , cellSize));
        }else if(direction==1){
            setPreferredSize(new Dimension(cellSize , cellSize*tools.size()));
        }
        this.setListData(tools.toArray());        
        for(ToolbarListener tl: DispatchHolder.DISPATCHER.exportListenersArray()){
            tl.toolAppended(this, tool);
        }
    }
    public int indexOfURI(URI uri){
        for(ToolComponent tc : tools){
            if(tc.tool.getURI().equals(uri)){
                return tools.indexOf(tc);
            }
        }
        return -1;
    }
    public boolean containsURI(URI uri){
        return indexOfURI(uri) == -1 ? false : true;
    }
    public Tool removeTool(Tool tool) {
        for(ToolComponent tc : tools){
            if(tc.tool.getURI().equals(tool.getURI())){
                tools.remove(tc);
                this.setListData(tools);
                for(ToolbarListener tl: DispatchHolder.DISPATCHER.exportListenersArray()){
                    tl.toolRemoved(this, tool);
                }
                return tc.tool;
            }
        }
        return null;
    }

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        ToolComponent tc = (ToolComponent)value;
        tc.setBorder(null);
        return tc;
    }

    public void mouseClicked(MouseEvent ev) {
        int i = locationToIndex(ev.getPoint());
        if(i == -1){
            return;
        }
        Object o = this.getModel().getElementAt(i);
        if(o instanceof MouseListener){
            if(ev.isShiftDown()){
                removeTool(((ToolComponent)o).tool);
            }else{
                for(ToolbarListener tl: DispatchHolder.DISPATCHER.exportListenersArray()){
                    tl.toolClicked(this,((ToolComponent)o).tool);
                }
                ((MouseListener)o).mouseClicked(ev);
            }
        }
        this.repaint();
    }

    public void mousePressed(MouseEvent ev) {
        int i = locationToIndex(ev.getPoint());
        if(i == -1){
            return;
        }
        Object o = this.getModel().getElementAt(i);
        if(o instanceof MouseListener){
            ((MouseListener)o).mousePressed(ev);
        }
        this.repaint();
    }

    public void mouseReleased(MouseEvent ev) {
        int i = locationToIndex(ev.getPoint());
        if(i == -1){
            return;
        }
        Object o = this.getModel().getElementAt(i);
        if(o instanceof MouseListener){
            ((MouseListener)o).mouseReleased(ev);
        }
        this.repaint();
    }

    public void mouseEntered(MouseEvent ev) {
        int i = locationToIndex(ev.getPoint());
        if(i == -1){
            return;
        }
        Object o = this.getModel().getElementAt(i);
        if(o instanceof MouseListener){
            ((MouseListener)o).mouseEntered(ev);
        }
        this.repaint();
    }

    public void mouseExited(MouseEvent ev) {
        int i = locationToIndex(ev.getPoint());
        if(i == -1){
            return;
        }
        Object o = this.getModel().getElementAt(i);
        if(o instanceof MouseListener){
            ((MouseListener)o).mouseExited(ev);
        }
        this.repaint();
    }

    class ToolTransferHandler extends TransferHandler {

        @Override
        public int getSourceActions(javax.swing.JComponent c) {
            return TransferHandler.COPY_OR_MOVE;
        }

        @Override
        public Transferable createTransferable(javax.swing.JComponent c) {
            System.out.println("Transferable created");
            return (ToolComponent) getSelectedValue();
        }


        @Override
        protected void exportDone(JComponent source, Transferable data, int action) {
            super.exportDone(source, data, action);
            System.out.println("done");
        }


        @Override
        public boolean canImport(TransferSupport ts) {
            System.out.println("Can import?");
            if (!ts.isDataFlavorSupported(ToolComponent.flavours[0])) {
                return false;
            }
            return true;
        }

        @Override
        public boolean importData(TransferSupport ts) {
            System.out.println("Import");
            if (!canImport(ts)) {
                return false;
            }

            JList.DropLocation dl = (JList.DropLocation) ts.getDropLocation();
            int i = dl.getIndex() == -1 ? tools.size() : dl.getIndex();

            try {
                addTool(i, (Tool) ts.getTransferable().getTransferData(ToolComponent.flavours[0]));
            } catch (UnsupportedFlavorException ex) {
                Logger.getLogger(SimpleToolbar.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(SimpleToolbar.class.getName()).log(Level.SEVERE, null, ex);
            }
            return true;
        }
    };

}
