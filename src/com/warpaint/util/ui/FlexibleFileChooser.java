/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.warpaint.util.ui;

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
    private int action;
    private java.awt.Component modal;
    private HashMap categories = new HashMap();
    /**
     * 
     * @param filefilters "Images:jpg,gif,png;Text Files:txt,.rtf"
     * @param imagePreview
     * @param acceptAll
     */
    public FlexibleFileChooser(java.awt.Component modal,String filefilters,int action,boolean imagePreview,boolean acceptAll){
        super();
        this.action = action;
        this.modal = modal;
        if(imagePreview){
            PreviewComponent pc = new PreviewComponent(this);
            setAccessory(pc);  
        }
        String[] filters = filefilters.split(";");
        String allSupported ="";

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
        
    }
    public void pop(){
boolean complete = false;
        while(!complete){
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
                //ensure extension on save addition
                String ext;
                if(action==SAVE && lastDot==-1){
                    String newName = ((FlexibleFileFilter)this.getFileFilter()).extensions.get(0);
                    file=new File( file.getAbsoluteFile() + (newName.startsWith(".") ? newName : "." + newName));
                    ext = ((FlexibleFileFilter)this.getFileFilter()).getDescription();
                }else{
                    ext=lastDot!=-1?((String)categories.get(file.getName().substring(lastDot).toLowerCase())):"";
                }
                if(action == SAVE && file.exists()){
                    int ores = javax.swing.JOptionPane.showConfirmDialog(modal, "The file chosen already exists, do you want to overwrite?","Overwrite?",javax.swing.JOptionPane.YES_NO_CANCEL_OPTION);
                    if(ores == javax.swing.JOptionPane.NO_OPTION){
                        continue;
                    }else if(ores == javax.swing.JOptionPane.CANCEL_OPTION){
                        complete=true;
                        break;
                    }
                }
                try {
                    fileChosen(ext, file);
                } catch (IOException ex) {
                    Logger.getLogger(FlexibleFileChooser.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
            complete = true;
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
