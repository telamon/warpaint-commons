    /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.warpaint.util.persistence;

import com.thoughtworks.xstream.XStream;
import com.warpaint.util.ui.Tool;
import com.warpaint.util.ui.ToolResolver;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

/**
 *
 * @author telamon
 */
public abstract class AbstractPersistenceLibrary<T> {
    private final static Vector<AbstractPersistenceLibrary> initializedLibraries = new Vector<AbstractPersistenceLibrary>();
    private final static ToolResolver aplResolver = new ToolResolver("apl") {
        @Override
        protected Tool resolveTool(URI uri) {
            AbstractPersistenceLibrary apl = AbstractPersistenceLibrary.getLibrary(uri.getSchemeSpecificPart());
            return apl.find(uri.getFragment());
        }
    };
    public static AbstractPersistenceLibrary getLibrary(String identifier){
        for(AbstractPersistenceLibrary apl: initializedLibraries){
            if(apl.getMyName().equals(identifier)){
                return apl;
            }
        }
        return null;
    }
    protected AbstractPersistenceLibrary(){       
        initializedLibraries.add(this);
        ToolResolver.addResolver(aplResolver);
    }
    // Abstract Library implementations.
    public abstract String getMyName();
    public abstract ArrayList<LibraryEntry<T>> getEntryList();
    protected abstract T getObject(Object key);
    protected abstract Object keyOf(String name);
    public abstract LibraryEntry<T> newEntry(T object);
    protected abstract boolean remove(LibraryEntry<T> entry);
    protected abstract void save(LibraryEntry<T> entry);
    protected abstract void rename(LibraryEntry<T> entry,String newname);
    protected abstract boolean exists(LibraryEntry<T> entry);

    // On instance implementations.
    public abstract String getNameFor(T obj); //Implemented on instance creation
    public abstract void setNameFor(T obj,String name); //Implemented on instance creation
    public abstract javax.swing.ImageIcon generateIcon(T obj); //Implemented on instance creation
    
    /**
     *  Creates a new LibraryEntry and persists it.
     *  Renames this object if an object with the same name already exists.
     * @param object
     * @return the new LibraryEntry object.
     */
    public LibraryEntry<T> create(T object){
        LibraryEntry<T> e = newEntry(object);
        int i=2;
        String org= getNameFor(object);
        while(e.exists()){
            setNameFor(object,org+i);
            e=newEntry(object);
            i++;
        }
        e.save();
        return e;
    }
    /**
     * Tries to fetch the first object in the library.
     * Returns null if library is empty.
     * @return
     */
    public LibraryEntry<T> getFirst(){
        ArrayList<LibraryEntry<T>> entries = getEntryList();
        if(entries.size()<1){
            return null;
        }
        return entries.get(0);
    }
    /**
     * Loads all objects, generates icons and then frees up
     * all auto-loaded objects.
     * @param entries
     */
    public void generateIcons(LibraryEntry<T>[] entries) {
        for(LibraryEntry<T> e:entries){
            e.getIcon();
        }
    }
    private static XStream xstream = new XStream();
    public static void setXStream(XStream xs){
        xstream=xs;
    };
    protected static XStream getXStream(){
       return xstream;
    }

    public LibraryEntry<T> find(String name){
        LibraryEntry<T> tmp = new LibraryEntry<T>(this,keyOf(name));
        if(tmp.exists()){
            return tmp;
        }
        return null;
    }
    public void CopyAllTo(AbstractPersistenceLibrary<T> target){
        for( LibraryEntry<T> e:getEntryList()){
            boolean wasAllocated = !e.isAllocated();
            target.create(e.get());
            if(!wasAllocated){
                e.release();
            }
        }
    }
}
