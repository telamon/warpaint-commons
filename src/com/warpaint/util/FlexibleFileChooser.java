/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.warpaint.util;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Administrator
 */
public abstract class  FlexibleFileChooser extends javax.swing.JFileChooser{
    public static final int OPEN = 0;
    public static final int SAVE = 1;
    public static String lastUsedPath ="";
    /**
     * 
     * @param filefilters "Images:jpg,gif,png;Text Files:txt,.rtf"
     * @param imagePreview
     * @param acceptAll
     */
    public FlexibleFileChooser(java.awt.Component modal,String filefilters,int action,boolean imagePreview,boolean acceptAll){
        super();
        if(imagePreview){
            PreviewComponent pc = new PreviewComponent(this);
            setAccessory(pc);  
        }
        String[] filters = filefilters.split(";");
        String allSupported ="";
        HashMap categories=new HashMap();
        for(String filter : filters){            
            String[] filt  = filter.split(":");
            String[] extensions = filt[1].split(",");
            addChoosableFileFilter(new FlexibleFileFilter(filt[0],extensions)); 
            for(String ext:extensions){                
                allSupported +=","+ext;
                categories.put((ext.startsWith(".")?ext:"."+ext).toLowerCase(),filt[0]);
            }
        }
        FlexibleFileFilter all = new FlexibleFileFilter("All supported extensions",allSupported.substring(1));
        addChoosableFileFilter(all);
        setFileFilter(all);
        setAcceptAllFileFilterUsed(acceptAll);
        File cwd = new File(lastUsedPath);
        if(cwd.exists() && cwd.isDirectory()){
            setCurrentDirectory(cwd);
        }
        
        int result ;
        if(action == OPEN){
            result = showOpenDialog(modal);
        }else if(action == SAVE){
            result = showSaveDialog(modal);
        }else{
            result = showDialog(modal, "Undefined action");
        }
        
        if(result==javax.swing.JFileChooser.APPROVE_OPTION) {
            java.io.File file = getSelectedFile();
            lastUsedPath = file.getParent();
            int lastDot = file.getName().lastIndexOf(".");
            String ext = lastDot!=-1?((String)categories.get(file.getName().substring(lastDot).toLowerCase())):"";
            try {
                fileChosen(ext, file);
            } catch (IOException ex) {
                Logger.getLogger(FlexibleFileChooser.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
    }
    public FlexibleFileChooser(java.awt.Component modal,String filefilters,int action){
        this(modal,filefilters,action,true,false);
    }
    public abstract void fileChosen(String group,java.io.File file) throws IOException;
    
    public static void popDemonstration(){
        new FlexibleFileChooser(null,"Bitmap:bmp;Lossy:jpg,jpeg;Animooted:gif,.png",OPEN){
            @Override
            public void fileChosen(String group, File file) {
                System.out.println("g:"+group+" f:"+file.getName());
            }

        };
    }
}
