/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.warpaint.util;

/**
 *
 * @author Administrator
 */
import javax.swing.*;
import java.beans.*;
import java.awt.*;
import java.io.File;

/* ImagePreview.java by FileChooserDemo2.java. */
public class PreviewComponent extends JComponent
                          implements PropertyChangeListener {
    ImageIcon thumbnail = null;
    File file = null;

    public PreviewComponent(JFileChooser fc) {
        setPreferredSize(new Dimension(180, 50));
        fc.addPropertyChangeListener(this);
    }

    public void loadImage() {
        if (file == null) {
            thumbnail = null;
            return;
        }

        //Don't use createImageIcon (which is a wrapper for getResource)
        //because the image we're trying to load is probably not one
        //of this program's own resources.
        ImageIcon tmpIcon = new ImageIcon(file.getPath());
        if (tmpIcon != null) {
            if (tmpIcon.getIconWidth() > 170) {
                thumbnail = new ImageIcon(tmpIcon.getImage().
                                          getScaledInstance(170, -1,
                                                      Image.SCALE_DEFAULT));
            } else { //no need to miniaturize
                thumbnail = tmpIcon;
            }
        }
    }

    public void propertyChange(PropertyChangeEvent e) {
        boolean update = false;
        String prop = e.getPropertyName();

        //If the directory changed, don't show an image.
        if (JFileChooser.DIRECTORY_CHANGED_PROPERTY.equals(prop)) {
            file = null;
            update = true;

        //If a file became selected, find out which one.
        } else if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(prop)) {
            file = (File) e.getNewValue();
            update = true;
        }

        //Update the preview accordingly.
        if (update) {
            thumbnail = null;
            if (isShowing()) {
                loadImage();
                repaint();
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (thumbnail == null) {
            loadImage();
        }
        if (thumbnail != null) {
            int x = getWidth()/2 - thumbnail.getIconWidth()/2;
            int y = getHeight()/2 - thumbnail.getIconHeight()/2;

            if (y < 0) {
                y = 0;
            }

            if (x < 5) {
                x = 5;
            }
            thumbnail.paintIcon(this, g, x, y);
        }
    }
}
