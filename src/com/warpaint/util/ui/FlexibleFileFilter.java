/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.warpaint.util.ui;

import java.io.File;
import java.util.ArrayList;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author Administrator
 */
public class FlexibleFileFilter extends FileFilter {
    public ArrayList<String> extensions=new ArrayList<String>();
    public String description;
    public FlexibleFileFilter(String description, String[] extensions){
        this.description=description;
        for(String s:extensions){
            if(s.equals("")){
                continue; // Don't add empty strings;
            }
            this.extensions.add(s);
        }
    }

    FlexibleFileFilter(String description, String ext) {
        this(description,ext.split(","));
    }
    public boolean accept(File pathname) {
        if(pathname.isDirectory()){
            return true;
        }
        for(String ext :extensions){  
            String s = pathname.getName().toLowerCase();
            s = s.startsWith(".")?s:"."+s;
            if(s.endsWith("."+ext)){
                return true;
            }
        }            
        return false;
    }

    @Override
    public String getDescription() {
        String exts="";
        for(String s:extensions){
            exts+=", *"+(s.startsWith(".")?"":".")+s;
        }
        return description +"  ("+exts.substring(2)+")";
    }

}
