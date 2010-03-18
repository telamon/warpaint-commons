/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.warpaint.util;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;


/**
 *
 * @author telamon
 */
public class VirtualCrosslinkLibrary extends AbstractPersistenceLibrary{
    ArrayList<AbstractPersistenceLibrary> libraries;
    File datafile;
    final VirtualCrosslinkLibrary me = this;


    private class Reference{

        public Reference(LibraryEntry l){
            resource = l.getName();
            library=l.getLibraryName();
        }
        public Reference(String r,String lib){
            resource=r;
            library=lib;
        }
        @XStreamAsAttribute
        String resource;
        @XStreamAsAttribute
        String library;
        public WrapperEntry getLink(){
            return new WrapperEntry(me,this);
        }
        public LibraryEntry getTarget(){
            return getLibrary(library).find(resource);
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof Reference){
                Reference r = (Reference)obj;
                return r.library.equals(library) && r.resource.equals(resource);
            }
            return false;
        }

    }
    private class WrapperEntry extends LibraryEntry{
        Reference ref;
        WrapperEntry(AbstractPersistenceLibrary l,Reference r){
            super(me,r,r);
            ref=r;
        }

        @Override
        public String getLibraryName() {
            return ref.getTarget().getLibraryName();
        }

        @Override
        public String getName() {
            return ref.getTarget().getName();
        }

        @Override
        public ImageIcon getIcon() {
            return ref.getTarget().getIcon();
        }
        @Override
        public Object get() {
            return ref.getTarget().get();
        }

        @Override
        public boolean equals(Object obj) {
            return ref.getTarget().equals(obj);
        }

        @Override
        public void release() {
            ref.getTarget().release();
        }
        @Override
        public void rename(String newName) {
            ref.getTarget().rename(newName);
        }
        @Override
        public boolean exists() {
            return ref.getTarget().exists();
        }

        @Override
        protected WrapperEntry clone() {
           return new WrapperEntry(me,ref);
        }

        @Override
        public void save() {
            ref.getTarget().save();
        }

    }

    public VirtualCrosslinkLibrary(ArrayList<AbstractPersistenceLibrary> libraries,File references){
        this.libraries=libraries;
        this.datafile=references;
    }
    public void setLibraries(ArrayList<AbstractPersistenceLibrary> libraries){
        this.libraries=libraries;
    }
    @Override
    public String getMyName() {
        return datafile.getName();
    }
    private AbstractPersistenceLibrary getLibrary(String name){
        for(AbstractPersistenceLibrary apl:libraries){
            if(apl.getMyName().equals(name)){
                return apl;
            }
        }
        return null;
    }
    private ArrayList<Reference> readRefs(){
        try {
            return  (ArrayList<Reference>) getXStream().fromXML(new FileInputStream(datafile));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(VirtualCrosslinkLibrary.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ArrayList<Reference>();
    }
    private void saveRefs(ArrayList<Reference> refs){
        try {
            getXStream().toXML(refs, new FileOutputStream(datafile));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(VirtualCrosslinkLibrary.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public ArrayList<LibraryEntry> getEntryList() {
        ArrayList<LibraryEntry> entries = new ArrayList<LibraryEntry>();

            ArrayList<Reference> refs = readRefs();
            for(Reference r:refs){
                entries.add(r.getLink());
            }

        return entries;
    }
    // WRAP
    @Override
    protected Object getObject(Object key) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    // WRAP
    @Override
    protected Object keyOf(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private boolean isEntry(Object o){
        return (o instanceof LibraryEntry);
    }
    private boolean isCrosslink(Object o){
        return o instanceof WrapperEntry;
    }
    // WRAP
    @Override
    public String getNameFor(Object obj) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setNameFor(Object obj, String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public LibraryEntry newEntry(Object object) {
        if(object instanceof LibraryEntry){
            return new Reference((LibraryEntry) object).getLink();
        }
        return null;
    }

    @Override
    protected boolean remove(LibraryEntry entry) {
        if(entry instanceof WrapperEntry){
            Reference tbr =null;
            ArrayList<Reference> refs = readRefs();
            for(Reference r:refs){
                if(r.equals(((WrapperEntry)entry).ref)){
                    tbr=r;
                    break;
                }
            }
            if(tbr!=null){
                refs.remove(tbr);
                saveRefs(refs);
                return true;
            }
        }
        return false;
    }

    @Override
    protected void save(LibraryEntry entry) {
        if(entry instanceof WrapperEntry){
            ArrayList<Reference> refs = readRefs();
            refs.add(((WrapperEntry)entry).ref);
            saveRefs(refs);
        }
    }
    // WRAP
    @Override
    protected void rename(LibraryEntry entry, String newname) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    // WRAP
    @Override
    protected boolean exists(LibraryEntry entry) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    // WRAP
    @Override
    public ImageIcon generateIcon(Object obj) {
        throw new UnsupportedOperationException("Not supported yet.");
    }


}
