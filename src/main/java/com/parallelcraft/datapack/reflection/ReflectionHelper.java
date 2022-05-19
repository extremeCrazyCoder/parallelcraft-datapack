package com.parallelcraft.datapack.reflection;

import com.parallelcraft.datapack.ClassMapping;
import com.parallelcraft.datapack.MappingsHelper;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

/**
 * This thing hanldes the start of all the reflection calls (getting the correct class)
 *
 * @author extremeCrazyCoder
 */
public class ReflectionHelper {
    public static final String ROOT_REGISTRY_PATH = "net.minecraft.core.Registry";
    
    private static URLClassLoader clsLoader = null;
    private static MappingsHelper map = null;
    private static RefCls registryClass = null;
    private static MRes registryHolder = null;
    
    private static Map<String, RefCls> reflectionCache;
    private static Map<String, Class> reflectionPureCache;
    private static Map<Class, RefCls> reflectionRawCache;
    
    private static Map<String, MRes> registryCache;
    
    public static void init(URLClassLoader pClsLoader, MappingsHelper pMap) {
        clsLoader = pClsLoader;
        map = pMap;
        reflectionCache = new HashMap<>();
        reflectionRawCache = new HashMap<>();
        
        reflectionPureCache = new HashMap<>();
        reflectionPureCache.put("float", float.class);
        reflectionPureCache.put("int", int.class);
        
        registryCache = new HashMap<>();
    }
    
    public static void setMainRepository(MRes holder) {
        registryHolder = holder;
    }
    
    /**
     * Get a class object
     * 
     * Use a $ inside the name to show where the subclass starts
     * 
     * @param name The name of the class to retrieve
     */
    public static RefCls c(String name) throws ClassNotFoundException {
        if(reflectionCache.containsKey(name)) {
            return reflectionCache.get(name);
        }
        
        String raw = map.getRawNameForClassName(name);
        if(name.startsWith("java") || name.startsWith("com") || name.startsWith("org")) {
            raw = name;
        }
        if(raw == null) {
            throw new ClassNotFoundException(name);
        }
        
        RefCls retval;
        if(raw.contains("$")) {
            String par = raw.substring(0, raw.indexOf("$"));
            String sub = raw.substring(raw.indexOf("$") + 1);
            RefCls parCls = ReflectionHelper.fetchClassInternal(par);
            retval = parCls.fetchClassInternal(sub, null);
        } else {
            retval = ReflectionHelper.fetchClassInternal(raw);
        }
        reflectionCache.put(name, retval);
        return retval;
    }
    
    public static RefCls getRegistryClass() throws Exception {
        if(registryClass == null) {
            registryClass = ReflectionHelper.c(ROOT_REGISTRY_PATH);
        }
        return registryClass;
    }
    
    public static MRes getRegistryHolder() throws Exception {
        return registryHolder;
    }
    
    public static MRes getRegistry(String registryField) throws Exception {
        if(registryCache.containsKey(registryField)) {
            return registryCache.get(registryField);
        }
        MRes retval = registryHolder.i("registryOrThrow", getRegistryClass().f(registryField));
        registryCache.put(registryField, retval);
        return retval;
    }
    
    public static int getRegistryID(Object data, String registryField) throws Exception {
        MRes reg = getRegistry(registryField);
        return reg.i("getId", data).aI();
    }
    
    static RefCls cRaw(Class cls) throws ClassNotFoundException {
        if(reflectionRawCache.containsKey(cls)) {
            return reflectionRawCache.get(cls);
        }
        
        String raw = cls.getName();
        RefCls retval;
        if(raw.startsWith("java.") || raw.startsWith("com.") || raw.startsWith("org.")) {
            //non obfuscated stuff
            retval = new RefCls(cls, null);
        } else if(raw.contains("$")) {
            String par = raw.substring(0, raw.indexOf("$"));
            String sub = raw.substring(raw.indexOf("$") + 1);
            
            RefCls parCls = ReflectionHelper.fetchClassInternal(par);
            retval = parCls.fetchClassInternal(sub, cls);
        } else {
            retval = ReflectionHelper.fetchClassInternal(raw);
        }
        reflectionRawCache.put(cls, retval);
        return retval;
    }
    
    /**
     * Get only the class Object itself
     * 
     * Use a $ inside the name to show where the subclass starts
     * 
     * @param name The name of the class to retrieve
     */
    public static Class classOnly(String name) throws ClassNotFoundException {
        if(reflectionPureCache.containsKey(name)) {
            return reflectionPureCache.get(name);
        }
        Class retval = ReflectionHelper.c(name).inst();
        reflectionPureCache.put(name, retval);
        return retval;
    }
    
    private static RefCls fetchClassInternal(String rawName) throws ClassNotFoundException {
        Class c = Class.forName(rawName, true, clsLoader);
        return new RefCls(c, map.getForRawName(rawName));
    }

    static ClassMapping getForRawName(String name) {
        return map.getForRawName(name);
    }
}
