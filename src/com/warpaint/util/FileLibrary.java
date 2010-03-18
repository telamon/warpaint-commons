/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.warpaint.util;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author telamon
 */
public abstract class FileLibrary<T> extends AbstractPersistenceLibrary<T> {
    private File root;
    private static final String XML = ".xml";
    public FileLibrary(File root){
        super();
        this.root=root;
        if(!root.exists()||!root.isDirectory()){
            root.mkdirs();
        }
        
    }

    @Override
    public String getMyName() {
        return root.getName();
    }
    @Override
    public ArrayList<LibraryEntry<T>> getEntryList() {
        File[] files = root.listFiles(new FileFilter(){
            public boolean accept(File pathname) {
                return pathname.isFile() && pathname.getName().endsWith(XML);
            }
        });
        ArrayList<LibraryEntry<T>> entries = new ArrayList<LibraryEntry<T>>();
        for(File f:files){
            LibraryEntry<T> e = new LibraryEntry<T>(this,f);
            entries.add(e);
        }
        return entries;
    }

    @Override
    protected T getObject(Object key) {
        try {
            FileInputStream fis = new FileInputStream((File)key);
            Object o =getXStream().fromXML(fis);
            fis.close();
            return (T)o;
        } catch (IOException ex) {
            Logger.getLogger(FileLibrary.class.getName()).log(Level.SEVERE, null, ex);
        } 
        return null;
    }

    @Override
    protected void rename(LibraryEntry<T> entry, String newname) {
        if(entry.name.equals(newname)){
            return;
        }
        LibraryEntry<T> clone = entry.clone();
        setNameFor(entry.get(),newname);
        entry.name=newname;
        entry.key=newEntry(entry.get()).key;
        entry.save();
        clone.delete();
    }

    @Override
    protected boolean exists(LibraryEntry<T> entry) {
        return ((File)entry.key).exists() && ((File)entry.key).getName().endsWith(XML);
    }

    @Override
    public LibraryEntry<T> newEntry(T object) {
        return new LibraryEntry<T>(this,keyOf(getNameFor(object)),object);
    }

    @Override
    protected Object keyOf(String name) {
        return new File(root.getPath() + File.separator + name + XML);
    }
    @Override
    protected boolean remove(LibraryEntry<T> entry) {
        File f = (File)entry.key;
        if(f.exists()){
            return f.delete();
        }
        return false;
    }

    @Override
    protected void save(LibraryEntry<T> entry) {
        if(entry.cache == null){
            return;
        }
        try {
            File f = (File) entry.key;
            FileOutputStream fos = new FileOutputStream(f);
            getXStream().toXML(entry.cache, fos);
            fos.close();
        } catch (IOException ex) {
            Logger.getLogger(FileLibrary.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
