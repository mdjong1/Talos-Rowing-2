/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.example.talos_2.common;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 *
 * @author tshalif
 */
public abstract class ListenerList<ListenerT,EventDataT> {
    
    private final Set<ListenerT> listeners = new LinkedHashSet<ListenerT>();

    public synchronized void clear() {
        listeners.clear();
    }
    
    public synchronized void addListener(ListenerT listener) {
        listeners.add(listener);
    }
    
    public synchronized void removeListener(ListenerT listener) {
        listeners.remove(listener);
    }
    
    public synchronized void dispatch(EventDataT eventObject) {
        for (ListenerT l: listeners) {
            dispatch(l, eventObject);
        }
    }
    
    protected abstract void dispatch(ListenerT listener, EventDataT eventObject);
}
