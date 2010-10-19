/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.warpaint.util.ui;

import java.net.URI;
import java.util.ArrayList;

/**
 *
 * @author telamon
 */
public abstract class ToolResolver {
    private String scheme;
    public ToolResolver(String name){
        scheme=name;
    }
    private static final ArrayList<ToolResolver> resolvers = new ArrayList<ToolResolver>();
    public static void addResolver(ToolResolver resolver){
        synchronized(resolvers){
            if(!resolvers.contains(resolver)){
                resolvers.add(resolver);
            }
        }
    }
    public static void removeResolver(ToolResolver resolver){
        synchronized(resolvers){
            resolvers.remove(resolver);
        }
    }
    protected abstract Tool resolveTool(URI uri);
    public static Tool resolve(URI uri){
        synchronized(resolvers){
            for(ToolResolver tr:resolvers){
                if(tr.scheme.equalsIgnoreCase(uri.getScheme())){
                    Tool t = tr.resolveTool(uri);
                    if(t!=null){
                        return t;
                    }
                }
            }
            return null;
        }
    }
    public static Tool[] resolve(URI[] uris){
        ArrayList<Tool> vt = new ArrayList<Tool>();
        for(URI u: uris){
            Tool t = resolve(u);
            if(t!=null){
                vt.add(t);
            }
        }
        return vt.toArray(new Tool[vt.size()]);
    }
}
