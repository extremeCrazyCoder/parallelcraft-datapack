package com.parallelcraft.datapack;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper class for decoding the Mappings and using the inside the ReflectionHelper
 * 
 * @author extremeCrazyCoder
 */
public class MappingsHelper {
    private Map<String, ClassMapping> mappedClsMappings;
    private Map<String, ClassMapping> rawClsMappings;
    private Map<String, String> clsNameMappings;
    private Map<String, String> clsNameMappingsInv;
    
    public MappingsHelper(String version) throws IOException {
        BufferedReader r = new BufferedReader(new FileReader(Downloader.getMappingPath(version)));
        
        mappedClsMappings = new HashMap<>();
        rawClsMappings = new HashMap<>();
        clsNameMappings = new HashMap<>();
        clsNameMappingsInv = new HashMap<>();
        
        String line;
        ClassMapping curCls = null;
        while((line = r.readLine()) != null) {
            if(line.startsWith("#")) continue;
            if(line.startsWith("    ")) {
                //mapping for a class
                curCls.updateMappings(line.trim());
            } else {
                int sep = line.indexOf(" -> ");
                String origName = line.substring(0, sep);
                String rawName = line.substring(sep + 4, line.length() - 1);
                curCls = new ClassMapping(origName, rawName);
                mappedClsMappings.put(origName, curCls);
                rawClsMappings.put(rawName, curCls);
                clsNameMappings.put(origName, rawName);
                clsNameMappingsInv.put(rawName, origName);
            }
        }
    }

    public ClassMapping getForClassName(String name) {
        return mappedClsMappings.get(name);
    }

    public ClassMapping getForRawName(String name) {
        return rawClsMappings.get(name);
    }

    public String getRawNameForClassName(String name) {
        return clsNameMappings.get(name);
    }
}
