package com.parallelcraft.datapack;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.jar.JarFile;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Does all the downloading
 * 
 * 
 * @author extremeCrazyCoder
 */
public class Downloader {
    public static void ensureDataDownloaded(String version) throws IOException {
        boolean serExists = getServerPathInternal(version).exists();
        boolean mapExists = getMappingPath(version).exists();
        if(!serExists || !mapExists) {
            JSONObject lData = fetchJsonUrl(getLauncherUrlForVersion(version));

            JSONObject downData = lData.getJSONObject("downloads");
            String serverUrl = downData.getJSONObject("server").getString("url");
            String serverMapUrl = downData.getJSONObject("server_mappings").getString("url");

            if(! serExists) {
                downloadFileAndSave(serverUrl, getServerPathInternal(version));
            }
            if(! mapExists) {
                downloadFileAndSave(serverMapUrl, getMappingPath(version));
            }
        }
        
        extractServerVersion(version);
    }
    
    private static JSONObject fetchJsonUrl(String requestURL) throws IOException {
        System.out.println("Fetching " + requestURL);
        try (Scanner scanner = new Scanner(new URL(requestURL).openStream(), StandardCharsets.UTF_8.toString()))
        {
            scanner.useDelimiter("\\A");
            return new JSONObject(scanner.hasNext() ? scanner.next() : "");
        }
    }
    
    private static String getLauncherUrlForVersion(String version) throws IOException {
        JSONObject allVersions = fetchJsonUrl(Main.SOURCE_URL);
        
        JSONArray versions = allVersions.getJSONArray("versions");
        for(Object o : versions) {
            if(o instanceof JSONObject vOb) {
                if(vOb.getString("id").equals(version)) {
                    return vOb.getString("url");
                }
            }
        }
        
        throw new IllegalArgumentException("Version with id " + version + " not found");
    }
    
    private static void downloadFileAndSave(String url, File saveTo) throws IOException {
        System.out.println("Downloading " + url);
        URL website = new URL(url);
        URLConnection connection = website.openConnection();
        try (InputStream in = new BufferedInputStream(connection.getInputStream()); FileOutputStream w = new FileOutputStream(saveTo)) {
            byte buffer[] = new byte[1024*1024*1024];
            int read;
            
            while((read = in.read(buffer)) > 0) {
                w.write(buffer, 0, read);
            }
        }
    }
    
    private static List<String> libraries;

    private static void extractServerVersion(String version) throws IOException {
        Main.recursiveDelete(new File(Main.TMP_PATH + "/server_extr"));
        (new File(Main.TMP_PATH + "/server_extr")).mkdirs();
        JarFile jar = new JarFile(getServerPathInternal(version));
        libraries = multiExtract(jar, "libraries");
        libraries.addAll(multiExtract(jar, "versions"));
    }
    
    private static List<String> multiExtract(JarFile jar, String type) throws IOException {
        List<String> extracted = new ArrayList<>();
        try (BufferedReader idxData = new BufferedReader(new InputStreamReader(jar.getInputStream(jar.getEntry("META-INF/" + type + ".list"))))) {
            String line;
            byte buffer[] = new byte[1024*1024*1024];
            int read;
            
            while((line = idxData.readLine()) != null) {
                String splitted[] = line.split("\t");
                String name = splitted[2].substring(splitted[2].lastIndexOf("/") + 1);
                System.out.println("Extracting " + name);
                
                try (InputStream in = jar.getInputStream(jar.getEntry("META-INF/" + type + "/" + splitted[2]));
                        FileOutputStream w = new FileOutputStream(Main.TMP_PATH + "/server_extr/" + name)) {
                    while((read = in.read(buffer)) > 0) {
                        w.write(buffer, 0, read);
                    }
                }
                extracted.add(name);
            }
        }
        return extracted;
    }
    
    private static File getServerPathInternal(String version) {
        return new File(Main.TMP_PATH + "/server_" + version + ".jar");
    }
    
    public static File getMappingPath(String version) {
        return new File(Main.TMP_PATH + "/server_mappings_" + version + ".txt");
    }
    
    public static URL[] getLibraryURLArray() throws MalformedURLException {
        URL retval[] = new URL[libraries.size()];
        
        for(int i = 0; i < retval.length; i++) {
            retval[i] = new File(Main.TMP_PATH + "/server_extr/" + libraries.get(i)).toURI().toURL();
        }
        
        return retval;
    }
}
