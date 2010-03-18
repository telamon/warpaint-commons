/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.warpaint.util;

import java.io.IOException;
import java.net.URI;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 *
 * @author telamon
 */
public class CakeManager extends java.net.CookieHandler {

    private static CakeManager me;

    public CakeManager() {
        super();
        me = this;
    }

    public static void printCookies() {
        System.out.println("The cookiejar cointains:");
        if (me == null) {
            System.out.println("No CakeManager initialized!");
            return;
        }
        for (Object cookie : me.cookieJar) {
            System.out.println("Cookie:" + ((Cookie) cookie));
        }

    }
    private List<Cookie> cookieJar = new LinkedList<Cookie>();

    public void put(URI uri, Map<String, List<String>> responseHeaders) throws IOException {
        List<String> setCookieList = responseHeaders.get("Set-Cookie");
        if (setCookieList != null) {
            for (String item : setCookieList) {
                Cookie cookie = new Cookie(uri, item);
                //System.out.println("Storing cookie: "+ cookie.getURI().getHost() + " = "+cookie.getName());
                for (Cookie existingCookie : cookieJar) {
                    //System.out.println("    candidates: "+ existingCookie.getURI().getHost() + " = "+existingCookie.getName());
                    if ((cookie.getURI().getHost().equals(existingCookie.getURI().getHost())) && (cookie.getName().equals(existingCookie.getName()))) {
                        cookieJar.remove(existingCookie);
                        break;
                    }
                }
                cookieJar.add(cookie);
            }
        }
    }

    public Map<String, List<String>> get(URI uri, Map<String, List<String>> requestHeaders) throws IOException {
        //printCookies();
        StringBuilder cookies = new StringBuilder();
        for (Cookie cookie : cookieJar) {
            // Remove cookies that have expired
            if (cookie.hasExpired()) {
                cookieJar.remove(cookie);
            } else if (cookie.matches(uri)) {
                if (cookies.length() > 0) {
                    cookies.append(", ");
                }
                cookies.append(cookie.toString());
            }
        }

        Map<String, List<String>> cookieMap = new HashMap<String, List<String>>(requestHeaders);

        if (cookies.length() > 0) {
            List<String> list = Collections.singletonList(cookies.toString());
            cookieMap.put("Cookie", list);
        }
        //System.out.println("CookieMap: " + cookieMap);
        return Collections.unmodifiableMap(cookieMap);
    }
    private static DateFormat expiresFormat1 = new SimpleDateFormat("E, dd MMM yyyy k:m:s 'GMT'",
            Locale.US);
    private static DateFormat expiresFormat2 = new SimpleDateFormat("E, dd-MMM-yyyy k:m:s 'GMT'",
            Locale.US);

    class Cookie {

        String name;
        String value;
        URI uri;
        String domain;
        Date expires;
        String path;

        public Cookie(URI uri, String header) {
            String attributes[] = header.split(";");
            String nameValue = attributes[0].trim();
            this.uri = uri;
            this.name = nameValue.substring(0, nameValue.indexOf('='));
            this.value = nameValue.substring(nameValue.indexOf('=') + 1);
            this.path = "/";
            this.domain = uri.getHost();

            for (int i = 1; i < attributes.length; i++) {
                nameValue = attributes[i].trim();
                int equals = nameValue.indexOf('=');
                if (equals == -1) {
                    continue;
                }
                String namer = nameValue.substring(0, equals);
                String valuer = nameValue.substring(equals + 1);
                if (namer.equalsIgnoreCase("domain")) {
                    String uriDomain = uri.getHost();
                    if (uriDomain.equals(valuer)) {
                        this.domain = valuer;
                    } else {
                        if (!valuer.startsWith(".")) {
                            valuer = "." + valuer;
                        }
                        uriDomain = uriDomain.substring(uriDomain.indexOf('.'));
                        if (!uriDomain.equals(valuer)) {
                            throw new IllegalArgumentException("Trying to set foreign cookie");
                        }
                        this.domain = valuer;
                    }
                } else if (namer.equalsIgnoreCase("path")) {
                    this.path = valuer;
                } else if (namer.equalsIgnoreCase("expires")) {
                    try {
                        this.expires = expiresFormat1.parse(valuer);
                    } catch (ParseException e) {
                        try {
                            this.expires = expiresFormat2.parse(valuer);
                        } catch (ParseException e2) {
                            throw new IllegalArgumentException("Bad date format in header: " + valuer);
                        }
                    }
                }
            }
        }

        public boolean hasExpired() {
            if (expires == null) {
                return false;
            }
            Date now = new Date();
            return now.after(expires);
        }

        public String getName() {
            return name;
        }

        public URI getURI() {
            return uri;
        }

        public boolean matches(URI uri) {

            if (hasExpired()) {
                return false;
            }
            String pathe = uri.getPath();
            if (pathe == null) {
                pathe = "/";
            }

            return pathe.startsWith(this.path);
        }

        @Override
        public String toString() {
            StringBuilder result = new StringBuilder(name);
            result.append("=");
            result.append(value);
            return result.toString();
        }
    }
}
