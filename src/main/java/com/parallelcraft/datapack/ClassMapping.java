package com.parallelcraft.datapack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for decoding the Mappings and using the inside the ReflectionHelper
 *
 * @author extremeCrazyCoder
 */
public class ClassMapping {
    private final String origName;
    private final String rawName;
    
    private final Map<String, String> variableMapping;
    private final Map<String, String> variableMappingInv;
    private final List<MethodMapping> methodMappings;

    public ClassMapping(String origName, String rawName) {
        variableMapping = new HashMap<>();
        variableMappingInv = new HashMap<>();
        methodMappings = new ArrayList<>();
        
        this.origName = origName;
        this.rawName = rawName;
    }
    
    void updateMappings(String line) {
        if(!line.contains("(") && ! line.contains(")")) {
            //variable
            int sep1 = line.indexOf(" ");
            int sep2 = line.indexOf(" -> ", sep1 + 1);
            
            String type = line.substring(0, sep1);
            String mOrigName = line.substring(sep1 + 1, sep2);
            String mRawName = line.substring(sep2 + 4);
            variableMapping.put(mOrigName, mRawName);
            variableMappingInv.put(mRawName, mOrigName);
        } else {
            //method
            if(line.contains("<init>(")) return; //constructor not needed
            int sep1 = line.indexOf(":"); //will return -1 if not found, might happen for anonymus functions
            int sep2 = line.indexOf(":", sep1 + 1); //will return -1 if not found
            int sep3 = line.indexOf(" ", sep2 + 1);
            int sep4 = line.indexOf("(", sep3 + 1);
            int sep5 = line.indexOf(") -> ", sep3 + 1);
            
            String retType = line.substring(sep2 + 1, sep3);
            String mOrigName = line.substring(sep3 + 1, sep4);
            String argAll = line.substring(sep4 + 1, sep5);
            String mRawName = line.substring(sep5 + 5);
            
            String mArgs[] = argAll.split(",");
            if(mArgs.length == 1 && mArgs[0].equals("")) {
                mArgs = new String[0];
            }
            
            MethodMapping m = new MethodMapping(mOrigName, mRawName, retType, mArgs);
            methodMappings.add(m);
        }
    }

    public String getOrigName() {
        return origName;
    }

    public String getRawName() {
        return rawName;
    }

    public MethodMapping getRawMethodName(String methodName, Class[] arguments) throws NoSuchMethodException, ClassNotFoundException {
        for(MethodMapping mMap : methodMappings) {
            if(! mMap.getOrigName().equals(methodName)) continue;
            if(! mMap.checkArgs(arguments)) continue;
            
            return mMap;
        }
        throw new NoSuchMethodException(methodName);
    }

    public String getRawFieldName(String name) {
        return variableMapping.get(name);
    }

    public String getOrigFieldName(String rawName) {
        return variableMappingInv.get(rawName);
    }
}
