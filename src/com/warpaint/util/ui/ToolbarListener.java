/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.warpaint.util.ui;

/**
 *
 * @author telamon
 */
public interface ToolbarListener {
    public void toolAppended(SimpleToolbar target,Tool tool);
    public void toolRemoved(SimpleToolbar aThis, Tool tool);
    public void toolClicked(SimpleToolbar aThis, Tool tool);
    
}
