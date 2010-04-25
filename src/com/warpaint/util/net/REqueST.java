/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.warpaint.util.net;


import com.thoughtworks.xstream.XStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.TransformerException;

/**
 *
 * @author Tony Ivanov <telamon@warpaint.se>
 */
public class REqueST {
    private static boolean DEBUG;
    private URL url;
    private static String baseURL;
    private static String contentType="application/xml";
    private String method;
    private Object postData;
    private static HashMap<String,String> headers;
    private HttpURLConnection conn;
    private static java.net.CookieHandler cm;
    public static void showDebug(boolean b){
        DEBUG=b;
    }
    public static void setContentType(String ctype){
        contentType=ctype;
    }
    protected REqueST(URL url){
        initCookieManager();
        this.url = url;
        initHeaders();
    }
    public static void setBaseURL(String url){
        baseURL=url;
    }
    public static void setCookieHandler(java.net.CookieHandler ch){
        cm = ch;
        java.net.CookieHandler.setDefault(cm);
    }
    private static void initCookieManager(){
        if(cm==null){
            cm = new CakeManager();
            java.net.CookieHandler.setDefault(cm);
        }
    }
    private static void initHeaders(){
        if(headers==null){
            headers = new HashMap<String,String>();
            headers.put("User-agent", "WarPaint.RESTLib/1.0");
            headers.put("Accept",contentType);
            headers.put("Content-type", contentType);
        }
    }
    private static void setHeaderField(String key, String value){
        initHeaders();
        headers.put(key, value);
    }
    protected REqueST(URL url,String method,Object postData) throws IOException{
        this(url);
        this.method = method;
        this.postData = postData;
    }
    public void setRawData(String data){
        postData = data;
    }
    public void setData(Object data){
        if(data!=null){
            this.postData = getXStream().toXML(data);
        }
    }
    public String getResponseMessage() throws IOException{
        return getConnection().getResponseMessage();
    }
    public int getResponseCode() throws IOException{
        return getConnection().getResponseCode();
    }
    public InputStream getInputStream() throws IOException{
        return getConnection().getInputStream();
    }
    public OutputStream getOutputStream() throws IOException{
        return getConnection().getOutputStream();
    }
    private HttpURLConnection getConnection() throws IOException{
        if(conn==null){
            conn = configureConnection();
        }
        return conn;
    }

    private HttpURLConnection configureConnection() throws IOException{
        conn = (HttpURLConnection) url.openConnection();
        conn.setDoInput(true);
        

        conn.setRequestMethod(method);
        if(DEBUG){
            System.out.println(method+" "+url.toString());
        }
        
        Iterator<Entry<String,String>> i =headers.entrySet().iterator();
        while(i.hasNext()){
            Entry<String,String> property = i.next();
            conn.setRequestProperty(property.getKey(),property.getValue());
        }
        return conn;
    }
    private void doOutput() throws IOException{
        if(postData!=null && (method.equals("POST") || method.equals("PUT"))){
            getConnection().setDoOutput(true);
            OutputStream os = getOutputStream();
            if(postData instanceof String){
                os.write(((String)postData).getBytes("UTF-8"));
            }else if(postData instanceof com.jamesmurty.utils.XMLBuilder){
                try {
                    ((com.jamesmurty.utils.XMLBuilder) postData).toWriter(new java.io.OutputStreamWriter(os), null);
                } catch (TransformerException ex) {
                    Logger.getLogger(REqueST.class.getName()).log(Level.SEVERE, null, ex);
                }
            }else{
                getXStream().toXML(postData,os);
            }
            os.close();
        }
    }

    private static XStream xstream;
    public static XStream getXStream(){
        if(xstream==null){
            xstream = new XStream();
        }
        return xstream;
    }
    public static void parseAnnotationsFor(Class c){
        getXStream().processAnnotations(c);
    }
    public static void parseAnnotationsFor(Class[] c){
        getXStream().processAnnotations(c);
    }
    public static void parseAnnotationsFor(Object o){
        getXStream().processAnnotations(o.getClass());
    }

    public static REqueST GET(URL url) throws IOException{
        return new REqueST(url,"GET",null);
    }
    public static REqueST PUT(URL url,Object data) throws IOException{
        return new REqueST(url,"PUT",data);
    }
    public static REqueST POST(URL url,Object data) throws IOException{
        return new REqueST(url,"POST",data);
    }
    public static REqueST DELETE(URL url) throws IOException{
        return new REqueST(url,"DELETE",null);
    }
    public static REqueST GET(String resource) throws IOException{
        return new REqueST(new URL(baseURL+resource),"GET",null);
    }
    public static REqueST PUT(String resource,Object data) throws IOException{
        return new REqueST(new URL(baseURL+resource),"PUT",data);
    }
    public static REqueST POST(String resource,Object data) throws IOException{
        return new REqueST(new URL(baseURL+resource),"POST",data);
    }
    public static REqueST DELETE(String resource) throws IOException{
        return new REqueST(new URL(baseURL+resource),"DELETE",null);
    }
    public static REqueST CUSTOM(URL url,String method,Object data) throws IOException{
        return new REqueST(url,method,data);
    }
    public static REqueST CUSTOM(String resource,String method,Object data) throws IOException{
        return new REqueST(new URL(baseURL+resource),method,data);
    }
    public Object fetch() throws IOException{
        return fetch(Object.class);
    }
    public Object fetch(Class t) throws IOException{
        Object o;
        try{
            o = getXStream().fromXML(fetchRaw());
        }catch(com.thoughtworks.xstream.mapper.CannotResolveClassException ex){
            //ex.printStackTrace();
            throw new IOException("Could not process response");
        }
        if(DEBUG){
            System.out.println("Response "+o.getClass()+": "+o.toString());
        }
        if(o instanceof ResponseFallback || (!t.equals(Object.class) && !o.getClass().equals(t))){
            if(o instanceof ResponseFallback){
                ((ResponseFallback)o).fallback();
                return null;
                //throw new IOException("Fallback received: "+o.getClass());
            }else{
                throw new IOException("Unexpected responsetype "+o.getClass()+". Was expecting "+t);
            }
        }
        return o;
    }
    public String fetchRaw() throws IOException{
        doOutput();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream is = getInputStream();
        int c;
        while((c=is.read())!=-1){
            baos.write(c);
        }
        String s=baos.toString("UTF-8");
        if(DEBUG){
            System.out.println(s);
        }
        return s;
    }

    /**
     *  Just a static method for convinience. Creates an XMLBuilder.
     * @param name
     * @return
     * @throws IOException
     */
    public static com.jamesmurty.utils.XMLBuilder XB(String name) throws IOException{
        try {
            return com.jamesmurty.utils.XMLBuilder.create(name);
        } catch (javax.xml.parsers.ParserConfigurationException ex) {
//            Logger.getLogger(RailsCore.class.getName()).log(Level.SEVERE, null, ex);
        } catch (javax.xml.parsers.FactoryConfigurationError ex) {
//            Logger.getLogger(RailsCore.class.getName()).log(Level.SEVERE, null, ex);
        }
        throw new IOException("Internal error");
    }

}
