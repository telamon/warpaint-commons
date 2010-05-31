/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.warpaint.util.ui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author telamon
 */
public class SimpleToolbar extends JPanel implements Toolbar {
    ArrayList<ToolComp> tools = new ArrayList<ToolComp>();
    public SimpleToolbar(){
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    }

    public void addTool(Tool tool) {
        ToolComp tc = new ToolComp(tool);
        tools.add(tc);
        this.add(tc);
    }

    public Tool removeTool(Tool tool) {
        for(ToolComp tc : tools){
            if(tc.tool == tool){
                this.remove(tc);
                return tc.tool;
            }
        }
        return null;
    }
    private class ToolComp extends JLabel implements MouseListener{
        Tool tool;
        public ToolComp(Tool t){
            tool = t;
            this.setIcon(t.getIcon());
            this.setName(t.getName());
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
    }

}
