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
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.TransferHandler;

/**
 * SimpleToolbar.java
 * Acts as a container for Tools.
 * In reality it's just a JList with a custom renderer and a vector of ToolComponents.
 * All Mouse events/gestures are delegated to the ToolComponent below the mouse.
 * @author telamon
 */

public class SimpleToolbar extends JList implements Toolbar, ListCellRenderer, MouseListener {

    Vector<ToolComponent> tools = new Vector<ToolComponent>();
    private static Tool clipboard=null;
    private static SimpleToolbar clipSource=null;
    private static int clipIndex=0;

    private int direction = 0;
    private final static int cellSize=40;
    private int capacity = 0;
    private boolean readOnly= true;
    private static Transferable floating=null;
    
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

    public SimpleToolbar(int capacity) {
        setCapacity(capacity);

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
    public int indexOf(Tool t){
        for(ToolComponent tc: tools){
            if(tc!=null && tc.tool.getURI().equals(t.getURI())){
                return tools.indexOf(tc);
            }
        }
        return -1;
    }
    public void setCapacity(int cap){
        capacity=cap;
        tools.setSize(capacity);        
        updateSize();
    }
    public int getCapacity(){
        return capacity;
    }
    public SimpleToolbar(){
        this(8);
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
    private int firstFreeSlot(){
        for(int i=0;i<tools.size();i++){
            if(tools.get(i)==null){
                return i;
            }
        }
        return -1;
    }

    /**
     * ReadyOnly affects only drag'n'drop behaviour,
     * The addTool/removeTool are completely unaffected. 
     * @param dropEnabled 
     */
    public void setReadOnly(boolean dropEnabled) {
        this.readOnly = dropEnabled;
    }
    public boolean isReadOnly(){
        return readOnly;
    }
    public void addTool(Tool tool){
        if(tool==null){
            return;
        }
        addTool(firstFreeSlot(),tool);
    }
    public void setDirection(int d){
        direction=d;
        updateSize();
    }

    public void addTool(int index,Tool tool) {
        if(tool==null){
            return;
        }

        ToolComponent tc = new ToolComponent(tool);        
        removeTool(tc.tool);
        index = index > capacity ? capacity : index;
        index = index < 0 ? 0 : index;

        tools.set(index, tc);
        updateSize();
        this.setListData(tools.toArray());        
        for(ToolbarListener tl: DispatchHolder.DISPATCHER.exportListenersArray()){
            tl.toolAppended(this, tool);
        }
    }
    private void updateSize(){
        if(direction==0){
            setPreferredSize(new Dimension(cellSize*capacity , cellSize));
        }else if(direction==1){
            setPreferredSize(new Dimension(cellSize , cellSize*capacity));
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
        if(tool==null){
            return null;
        }
        for(ToolComponent tc : tools){
            if(tc==null){
                continue;
            }
            if(tc.tool.getURI().equals(tool.getURI())){
                tools.set(tools.indexOf(tc),null);
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
        JComponent tc = (ToolComponent)value;
        if(tc==null){
            tc = new javax.swing.JLabel();
        }
        tc.setBorder(new javax.swing.border.LineBorder(Color.lightGray));
        return tc;
    }

    public void mouseClicked(MouseEvent ev) {
        int i = locationToIndex(ev.getPoint());
        if(i == -1){
            return;
        }
        Object o = this.getModel().getElementAt(i);
        if(o instanceof MouseListener){
            if(o!=null && ev.isShiftDown() && !readOnly){
                removeTool(((ToolComponent)o).tool);
           }else{
                for(ToolbarListener tl: DispatchHolder.DISPATCHER.exportListenersArray()){
                  if(o!=null){
                   tl.toolClicked(this,((ToolComponent)o).tool);
                  }
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
        System.out.println("Released"+ev);
        int i = locationToIndex(ev.getPoint());
        
        // Transfer without dnd...
        if(clipboard!=null && !readOnly){
            
            if(clipSource == this && tools.elementAt(i)!= null){ // Perform a swap.
                addTool(clipIndex,removeTool(tools.elementAt(i).tool));
                
            }
            addTool(i, clipboard);
            clipboard=null;
            clipSource=null;
            this.invalidate();            
            this.repaint();
            return;
        }
        
        // Delegate
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
    /** 
     *  A qugly (Quick & Ugly ) access to this within anonymous methods.
     * @return An instance of this toolbar.
     */
    private SimpleToolbar self(){
        return this;
    }
    
    
    /**
     *  A private transferhandler
     *  
     */
    class ToolTransferHandler extends TransferHandler {

        @Override
        public int getSourceActions(javax.swing.JComponent c) {
            return (((SimpleToolbar)c).readOnly) ? TransferHandler.COPY :TransferHandler.COPY_OR_MOVE;
        }

        @Override
        public Transferable createTransferable(javax.swing.JComponent c) {
            System.out.println("Transferable created");
            if(getSelectedValue()==null){
                return null;
            }
            clipIndex=getSelectedIndex();
            clipboard= ((ToolComponent) getSelectedValue()).tool;
            clipSource= self();
            floating= (ToolComponent) getSelectedValue();
            //removeTool(clipboard);
            return (ToolComponent) getSelectedValue();
        }


        @Override
        protected void exportDone(JComponent source, Transferable data, int action) {
            super.exportDone(source, data, action);
            // Source seems to be the source..
            if(action == TransferHandler.MOVE && source instanceof SimpleToolbar){
                ((SimpleToolbar)source).removeTool(((ToolComponent)data).tool);
            }
            System.out.println("done");
        }
        

        @Override
        public Icon getVisualRepresentation(Transferable t) {
            if(t instanceof ToolComponent){
                return ((ToolComponent)t).getIcon();
            }else if(t instanceof Tool){
                return ((Tool)t).getIcon();
            }
            return null;
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
