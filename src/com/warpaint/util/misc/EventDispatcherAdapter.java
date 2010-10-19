/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.warpaint.util.misc;

/**
 *
 * @author telamon
 */
public abstract class EventDispatcherAdapter<T> {
    private final java.util.List<T> listeners = new java.util.ArrayList<T>();
    private T[] listenersCache;
    public void addListener(T l) {
       synchronized(listeners) {
            listeners.add(l);
            listenersCache=null;
        }
    }
    public void removeListener(T l) {
        synchronized(listeners) {
            listeners.remove(l);
            listenersCache=null;
        }
    }
    protected abstract Class getListenerClass();
    protected T[] getListenersArray(){
        synchronized(listeners){
            if(listenersCache==null)
                 listenersCache= listeners.toArray((T[])java.lang.reflect.Array.newInstance(getListenerClass(), listeners.size()));
                //listenersCache=listeners.toArray();
            return listenersCache;
        }
    }
    /**
     * A maintenence hatch for convenience, only to be used the classes that define this dispatcher internally.
     * Will return null by default.
     * @return
     */
    public T[] exportListenersArray(){
        return null;
    }



}
