/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.warpaint.util.persistence;

import com.warpaint.util.ui.Tool;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;


/**
 *
 * @author telamon
 */
public class LibraryEntry<T> implements Tool{
    protected javax.swing.Icon icon;
    protected Object key;
    protected T cache;
    protected String name;
    private AbstractPersistenceLibrary parent;
    protected LibraryEntry(AbstractPersistenceLibrary p,Object key){
        this.parent=p;
        this.key=key;
    }
    protected LibraryEntry(AbstractPersistenceLibrary p,Object key,T obj){
        this(p,key);
        cache = obj;
    }

    @Override
    public String toString(){
        return getName();
    }
    
    private LibraryEntry() {

    }
    /**
     * Returns cached name if exists.
     * Tries to fecth new name if object is not allocated.
     * Returns null if all else fails.
     * @return
     */
    public String getName(){
        if(name!=null){
            return name;
        }
        if(!isAllocated()){
            get();
            release();
        }else{
            name=parent.getNameFor(cache);
        }
        return name;
    }
    public Class getObjectClass(){
        boolean released = cache == null;
        Class c = get().getClass();
        if(released){
            release();
        }
        return c;
    }
    public T get(){
        if(cache==null){
            cache = (T) parent.getObject(key);
            String tmp = parent.getNameFor(cache);
            if(tmp!=null){
                name=tmp;
            }
        }
        return cache;
    }
    public void save(){
        parent.save(this);
    }
    public void delete(){
        parent.remove(this);
    }
    /**
     * Releases this entry's object pointer to let the garbage collector
     * free up memory without destroying the entry. A getObject() call to this
     * entry will reload the object again into memory.
     * WARNING: Any changes to the object that were not saved throught save() , will be lost!
     */
    public void release(){
        cache=null;
    }
    public boolean isAllocated(){
        return cache!=null;
    }
    /**
     * Get a representional icon, will return a cached icon or a new if no
     * cached exists.
     * @return
     */
    public Icon getIcon(){
        if(icon!=null){
            return icon;
        }
        return regenerateIcon();
    }
    /**
     * Throw away the cached icon and generate a new one.
     * @return
     */
    public Icon regenerateIcon(){
        boolean wasAllocated=isAllocated();
        icon = parent.generateIcon(get());
        if(!wasAllocated){
            release();
        }
        return icon;
    }
    public String getLibraryName(){
        return parent.getMyName();
    }


    public AbstractPersistenceLibrary getLibrary() {
        return parent;
    }

    public void rename(String newName){
        parent.rename(this, newName);
    }
    /**
     *  Renames and moves the entry to moveTarget library.
     *  The move is only performed if moveTarget is another library.
     * @param newName
     * @param moveTarget
     */
    public void rename(String newName,AbstractPersistenceLibrary<T> moveTarget){
        rename(newName);
        if(moveTarget != parent){
            moveTo(moveTarget);
        }

    }
    public void copyTo(AbstractPersistenceLibrary<T> target){
        target.create(get());
    }
    public void moveTo(AbstractPersistenceLibrary<T> target){
        copyTo(target);
        parent.remove(this);
    }
    @Override
    protected LibraryEntry<T> clone(){
        LibraryEntry<T> clone = new LibraryEntry<T>();
        clone.name = ""+name;
        clone.cache = cache;
        clone.key = key;
        clone.parent = parent;
        return clone;
    }
    public boolean exists(){
        return parent.exists(this);
    }

    public URI getURI() {
        try {
            return new URI("apl",parent.getMyName(), getName());
        } catch (URISyntaxException ex) {
            Logger.getLogger(LibraryEntry.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public void doAction(ActionEvent e) {
        boolean doRelease = cache == null;
        T o = get();
        if(o instanceof ActionListener){
            e.setSource(this);
            ((ActionListener)o).actionPerformed(e);
        }else if(doRelease){
            release();
        }
    }
}
