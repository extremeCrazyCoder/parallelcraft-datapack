package com.parallelcraft.datapack;

import com.parallelcraft.datapack.types.BlockState;
import com.parallelcraft.datapack.types.Blocks;
import com.parallelcraft.datapack.types.Dimensions;
import com.parallelcraft.datapack.types.Items;
import com.parallelcraft.datapack.types.LootTables;
import com.parallelcraft.datapack.types.ParticleTypes;
import com.parallelcraft.datapack.types.WorldgenBiomes;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Main class that will generate a vanilla like datapack from vanilla jar
 * 
 * Needs to be mapped using vanilla mappings -> decompilation not needed (jar will be invoked via reflection)
 * e.g. using DecompilerMC:
 * python3 main.py --mcversion 1.17 --side server --nauto --clean --download_mapping --remap_mapping --download_jar
 *                 --remap_jar --delete_dep f --decompile f
 * 
 * @author extremeCrazyCoder
 */
public class Main {
    public static final String SOURCE_PATH = "../DecompilerMC/src/1.17-server-temp.jar";
    public static final String DESTINATION_PATH = "../target/";
    
    public static final String ROOT_REGISTRY_PATH = "net.minecraft.core.Registry";
    public static final String BOOTSTRAP_PATH = "net.minecraft.server.Bootstrap";
    public static final String CRASH_REPORT_PATH = "net.minecraft.CrashReport";
    public static final String CONSTANTS_PATH = "net.minecraft.SharedConstants";
    
    public static Class registryClass;
    public static Class bootsrapClass;
    public static Class crashReportClass;
    
    public static URLClassLoader clsLoader;
    
   /**
     * Find:
     * Registry.register(Registry.
     * Registry.registerMapping(Registry.
     */
    public static void main(String args[]) throws Exception {
        recursiveDelete(new File(DESTINATION_PATH));
        
        //see https://stackoverflow.com/a/60775
        clsLoader = new URLClassLoader(
                new URL[]{new File(SOURCE_PATH).toURI().toURL()},
                new Main().getClass().getClassLoader()
        );
        
        //fake boot the server in order to extract the data afterwards
        crashReportClass = Class.forName(CONSTANTS_PATH, true, clsLoader);
        crashReportClass.getDeclaredMethod("tryDetectVersion").invoke(null);
        
        crashReportClass = Class.forName(CRASH_REPORT_PATH, true, clsLoader);
        crashReportClass.getDeclaredMethod("preload").invoke(null);
        
        bootsrapClass = Class.forName(BOOTSTRAP_PATH, true, clsLoader);
        bootsrapClass.getDeclaredMethod("bootStrap").invoke(null);
        bootsrapClass.getDeclaredMethod("validate").invoke(null);
        
        registryClass = Class.forName(ROOT_REGISTRY_PATH, true, clsLoader);
        
        Blocks.generateDatapackPart(registryClass);
        Items.generateDatapackPart(registryClass);
        Dimensions.generateDatapackPart(registryClass);
        WorldgenBiomes.generateDatapackPart(registryClass);
        ParticleTypes.generateDatapackPart(registryClass);
        BlockState.generateDatapackPart(registryClass);
        LootTables.generateDatapackPart(registryClass);
    }
    
    public static Object readReflective(Object src, String field) throws Exception {
        return readReflective(src.getClass(), src, field);
    }
    
    public static Object readReflective(Class cls, Object src, String field) throws Exception {
        Field f = cls.getDeclaredField(field);
        f.setAccessible(true);
        return f.get(src);
    }
    
    public static Object invokeReflective(Class cls, Object src, String methode) throws Exception {
        Method f = cls.getDeclaredMethod(methode);
        f.setAccessible(true);
        return f.invoke(src);
    }
    
    public static Object invokeUnknownReflective(Object src, String methode, Object... params) throws Exception {
        return invokeUnknownReflective(src.getClass(), src, methode, params);
    }
    
    public static Object invokeUnknownReflective(Class cls, Object src, String methode, Object... params) throws Exception {
        Method m[] = cls.getDeclaredMethods();
        for(Method me : m) {
            if(me.getName().equals(methode) && me.getParameterTypes().length == params.length) {
                me.setAccessible(true);
                return me.invoke(src, params);
            }
        }
        return invokeUnknownReflective(cls.getSuperclass(), src, methode, params);
    }
    
    public static int getRegistryID(Object data, String registryField) throws Exception {
        Field f = registryClass.getField(registryField);
        Object reg = f.get(null);
        return (int) getID(data, reg);
    }
    
    public static int getID(Object data, Object reg) throws Exception {
        Object result = reg.getClass().getMethod("getId", Object.class).invoke(reg, data);
        return (int) result;
    }
    
    public static int readAndGetID(Class cls, Object src, String field, String registryField) throws Exception {
        Object dat = readReflective(cls, src, field);
        return getRegistryID(dat, registryField);
    }
    
    public static String findName(Object toFind, String findIn) throws Exception {
        for(Field f: fetchClass(findIn).getDeclaredFields()) {
            if(! Modifier.isStatic(f.getModifiers())) continue;
            
            f.setAccessible(true);
            if(f.get(null) == toFind) {
                return f.getName();
            }
        }
        return null;
    }
    
    public static Class<?> fetchClass(String name) throws Exception {
        return Class.forName(name, true, clsLoader);
    }
    
    public static Class<?> fetchSubClass(String par, String name) throws Exception {
        Class parClsBeh = fetchClass(par);
        for(Class i : parClsBeh.getDeclaredClasses()) {
            if(i.getSimpleName().equals(name)) return i;
        }
        throw new ClassNotFoundException();
    }
    
    public static void writePart(String globalPath, JSONArray data) throws Exception {
        if(!globalPath.endsWith(".json")) {
            globalPath+= ".json";
        }
        File target = new File(DESTINATION_PATH + File.separator + globalPath);
        target.getParentFile().mkdirs();
        try (FileWriter writer = new FileWriter(target)) {
            data.write(writer, 4, 0);
        }
    }
    
    public static void writePart(String globalPath, JSONObject data) throws Exception {
        if(!globalPath.endsWith(".json")) {
            globalPath+= ".json";
        }
        File target = new File(DESTINATION_PATH + File.separator + globalPath);
        target.getParentFile().mkdirs();
        try (FileWriter writer = new FileWriter(target)) {
            data.write(writer, 4, 0);
        }
    }
    
    public static void recursiveDelete(File toDelete) {
        if(!toDelete.exists()) return;
        
        for(File f : toDelete.listFiles()) {
            if(f.isDirectory()) {
                recursiveDelete(f);
            } else if(f.isFile()) {
                f.delete();
            }
        }
        toDelete.delete();
    }
    
    //TODO delete only for debugging / developement
    private static JSONObject dumpClass(Class toDo, Object instance) throws Exception {
        JSONObject result = new JSONObject();
        for(Field f : toDo.getDeclaredFields()) {
            if(Modifier.isStatic(f.getModifiers())) continue;
            f.setAccessible(true);
            
            result.put(f.getName(), f.get(instance));
        }
        return result;
    }
    
    /**
     * List directory contents for a resource folder. Not recursive.
     * This is basically a brute-force implementation.
     * Works for regular files and also JARs.
     * copied from http://www.uofr.net/~greg/java/get-resource-listing.html
     * 
     * @author Greg Briggs
     * @param clazz Any java class that lives in the same place as the resources you want.
     * @param path Should end with "/", but not start with one.
     * @return Just the name of each member item, not the full paths.
     * @throws URISyntaxException 
     * @throws IOException 
     */
    public static String[] getResourceListing(Class clazz, String path, boolean all) throws URISyntaxException, IOException {
        URL dirURL = clazz.getClassLoader().getResource(path);
        if (dirURL != null && dirURL.getProtocol().equals("file")) {
            /* A file path: easy enough */
            return new File(dirURL.toURI()).list();
        } 

        if (dirURL == null) {
            /* 
             * In case of a jar file, we can't actually find a directory.
             * Have to assume the same jar as clazz.
             */
            String me = clazz.getName().replace(".", "/")+".class";
            dirURL = clazz.getClassLoader().getResource(me);
        }

        if (dirURL.getProtocol().equals("jar")) {
            /* A JAR path */
            String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!")); //strip out only the JAR file
            JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
            Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
            Set<String> result = new HashSet<>(); //avoid duplicates in case it is a subdirectory
            while(entries.hasMoreElements()) {
                String name = entries.nextElement().getName();
                if (name.startsWith(path)) { //filter according to the path
                    String entry = name.substring(path.length());
                    if(entry.length() == 0) continue;
                    if(entry.charAt(0) == '/') {
                        entry = entry.substring(1);
                    }
                    if(!all) {
                        int checkSubdir = entry.indexOf("/");
                        if (checkSubdir >= 0) {
                            // if it is a subdirectory, we just return the directory name
                            entry = entry.substring(0, checkSubdir);
                        }
                    }
                    if(entry.length() > 0) {
                        result.add(entry);
                    }
                }
            }
            return result.toArray(new String[result.size()]);
        }

        throw new UnsupportedOperationException("Cannot list files for URL "+dirURL);
    }
}
