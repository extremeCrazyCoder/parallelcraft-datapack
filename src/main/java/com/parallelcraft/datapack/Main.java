package com.parallelcraft.datapack;

import com.parallelcraft.datapack.types.BlockMaterials;
import com.parallelcraft.datapack.types.BlockSoundTypes;
import com.parallelcraft.datapack.types.BlockState;
import com.parallelcraft.datapack.types.Blocks;
import com.parallelcraft.datapack.types.Dimensions;
import com.parallelcraft.datapack.types.Effects;
import com.parallelcraft.datapack.types.Enchantments;
import com.parallelcraft.datapack.types.ItemTags;
import com.parallelcraft.datapack.types.Items;
import com.parallelcraft.datapack.types.LootTables;
import com.parallelcraft.datapack.types.ParticleTypes;
import com.parallelcraft.datapack.types.Sounds;
import com.parallelcraft.datapack.types.WorldgenBiomes;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

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
    public static String SOURCE_PATH = "../DecompilerMC/src/1.17-server-temp.jar";
    public static String DESTINATION_TMP_PATH = "target/pack";
    public static String DESTINATION_PATCHING_TMP_PATH = "target/pack_patched";
    public static String DESTINATION_PATH = "../../parallelcraft/src/main/resources/datapack";
    public static final String ROOT_REGISTRY_PATH = "net.minecraft.core.Registry";
    
    public static Class registryClass;
    
    public static URLClassLoader clsLoader;
    
   /**
     * Find:
     * Registry.register(Registry.
     * Registry.registerMapping(Registry.
     */
    public static void main(String args[]) {
        try {
            recursiveDelete(new File(DESTINATION_TMP_PATH));
            recursiveDelete(new File(DESTINATION_PATCHING_TMP_PATH));
            recursiveDelete(new File(DESTINATION_PATH));

            //see https://stackoverflow.com/a/60775
            clsLoader = new URLClassLoader(
                    new URL[]{new File(SOURCE_PATH).toURI().toURL()},
                    new Main().getClass().getClassLoader()
            );

            fakeBootServer();
            registryClass = Main.fetchClass(ROOT_REGISTRY_PATH);

            //export data
            WorldgenBiomes.generateDatapackPart(registryClass);
            Dimensions.generateDatapackPart(registryClass);
            ParticleTypes.generateDatapackPart(registryClass);
            Blocks.generateDatapackPart(registryClass);
            BlockMaterials.generateDatapackPart(registryClass);
            BlockSoundTypes.generateDatapackPart(registryClass);
            BlockState.generateDatapackPart(registryClass);
            Items.generateDatapackPart(registryClass);
            ItemTags.generateDatapackPart(registryClass);
            Enchantments.generateDatapackPart(registryClass);
            Effects.generateDatapackPart(registryClass);
            Sounds.generateDatapackPart(registryClass);
            LootTables.generateDatapackPart(registryClass);
            
            //apply patches
            System.out.println("applying patches");
            for(File patch : new File("datapack_patches").listFiles()) {
                System.out.println("applying patch " + patch.getName());
                Process p = Runtime.getRuntime().exec("patch -p0");
                new StreamMapper(p.getInputStream(), System.out, false);
                new StreamMapper(p.getErrorStream(), System.err, false);
                new StreamMapper(new FileInputStream(patch), p.getOutputStream(), true);
                p.waitFor();
            }
            
            System.out.println("copying of the result");
            recursiveCopy(new File(DESTINATION_TMP_PATH), DESTINATION_PATCHING_TMP_PATH, false);
            System.out.println("minifying of the result");
            recursiveCopy(new File(DESTINATION_TMP_PATH), DESTINATION_PATH, true);
        } catch(Exception e) {
            e.printStackTrace();
        }
        //ensure that we are shutting down even in the case of an error
        System.exit(0);
    }
    
    public static final String CONSTANTS_PATH = "net.minecraft.SharedConstants";
    public static final String CRASH_REPORT_PATH = "net.minecraft.CrashReport";
    public static final String BOOTSTRAP_PATH = "net.minecraft.server.Bootstrap";
    public static final String STATIC_TAGS_PATH = "net.minecraft.tags.StaticTags";
    public static final String REGISTRY_ACCESS_PATH = "net.minecraft.core.RegistryAccess";
    public static final String LEVEL_STORAGE_SOURCE_PATH = "net.minecraft.world.level.storage.LevelStorageSource";
    public static final String REPOSITORY_SOURCE_PATH = "net.minecraft.server.packs.repository.RepositorySource";
    public static final String SERVER_PACKS_SOURCE_PATH = "net.minecraft.server.packs.repository.ServerPacksSource";
    public static final String FOLDER_REPOSITORY_SOURCE_PATH = "net.minecraft.server.packs.repository.FolderRepositorySource";
    public static final String LEVEL_RESOURCE_PATH = "net.minecraft.world.level.storage.LevelResource";
    public static final String PACK_SOURCE_PATH = "net.minecraft.server.packs.repository.PackSource";
    public static final String PACK_REPOSITORY_PATH = "net.minecraft.server.packs.repository.PackRepository";
    public static final String PACK_TYPE_PATH = "net.minecraft.server.packs.PackType";
    public static final String DATAPACK_CONFIG_PATH = "net.minecraft.world.level.DataPackConfig";
    public static final String MINECRAFT_SERVER_PATH = "net.minecraft.server.MinecraftServer";
    public static final String SERVER_RESOURCES_PATH = "net.minecraft.server.ServerResources";
    public static final String COMMANDS_PATH = "net.minecraft.commands.Commands";
    public static final String UTIL_PATH = "net.minecraft.Util";
    
//    public static final String TAG_MANAGER_PATH = "net.minecraft.tags.TagManager";
    
    public static void fakeBootServer() throws Exception {
        Class constantsClass = Main.fetchClass(CONSTANTS_PATH);
        constantsClass.getDeclaredMethod("tryDetectVersion").invoke(null);

        Class crashReportClass = Main.fetchClass(CRASH_REPORT_PATH);
        crashReportClass.getDeclaredMethod("preload").invoke(null);

        Class bootsrapClass = Main.fetchClass(BOOTSTRAP_PATH);
        bootsrapClass.getDeclaredMethod("bootStrap").invoke(null);
        bootsrapClass.getDeclaredMethod("validate").invoke(null);
        
        Class staticTags = Main.fetchClass(STATIC_TAGS_PATH);
        staticTags.getMethod("bootStrap").invoke(null);
        
        
        Object registryAccess = Main.fetchClass(REGISTRY_ACCESS_PATH).getMethod("builtin").invoke(null);
        Object levelStorageSource = Main.fetchClass(LEVEL_STORAGE_SOURCE_PATH).getMethod("createDefault", Path.class).invoke(null, new File(".").toPath());
        Object levelStorageAccess = Main.invokeUnknownReflective(levelStorageSource, "createAccess", "world");
        Object datapackConfig = Main.invokeUnknownReflective(levelStorageAccess, "getDataPacks");
        
        Object[] repositorySources = (Object[]) Array.newInstance(Main.fetchClass(REPOSITORY_SOURCE_PATH), 2);
        repositorySources[0] = Main.fetchClass(SERVER_PACKS_SOURCE_PATH).getConstructor().newInstance();
        Object datapackDir = Main.fetchClass(LEVEL_RESOURCE_PATH).getField("DATAPACK_DIR").get(null);
        Object packWorld = Main.fetchClass(PACK_SOURCE_PATH).getField("WORLD").get(null);
        repositorySources[1] = Main.invokeConstructor(Main.fetchClass(FOLDER_REPOSITORY_SOURCE_PATH), ((Path) Main.invokeUnknownReflective(levelStorageAccess, "getLevelPath", datapackDir)).toFile(), packWorld);
        Object packRepository = Main.invokeConstructor(Main.fetchClass(PACK_REPOSITORY_PATH), Main.fetchClass(PACK_TYPE_PATH).getField("SERVER_DATA").get(null), repositorySources);
        
        if(datapackConfig == null) {
            datapackConfig = Main.fetchClass(DATAPACK_CONFIG_PATH).getField("DEFAULT").get(null);
        }
        
        Object datapackConfigNew = Main.invokeUnknownReflective(Main.fetchClass(MINECRAFT_SERVER_PATH), (Object) null, "configurePackRepository", packRepository, datapackConfig, true);
        CompletableFuture feature = (CompletableFuture) Main.fetchClass(SERVER_RESOURCES_PATH)
                .getMethod("loadResources", List.class, Main.fetchClass(REGISTRY_ACCESS_PATH),
                        Main.fetchSubClass(COMMANDS_PATH, "CommandSelection"), int.class, Executor.class, Executor.class)
                .invoke(null,
                    Main.invokeUnknownReflective(packRepository, "openAllSelected"),
                    registryAccess,
                    Main.fetchSubClass(COMMANDS_PATH, "CommandSelection").getField("DEDICATED").get(null),
                    2, Main.invokeUnknownReflective(Main.fetchClass(UTIL_PATH), (Object) null, "backgroundExecutor"),
                    (Executor) Runnable::run);
        
        Object serverRes = feature.get();
        Main.invokeUnknownReflective(serverRes, "updateGlobals");
    }
    
    public static void realBootServer() throws Exception {
        //fake eula
        BufferedWriter w = new BufferedWriter(new FileWriter(new File("eula.txt")));
        w.write("eula=true");
        w.close();
        
        String[] argSub = new String[]{"--nogui"};
        Class mC = fetchClass("net.minecraft.server.Main");
        Method mM = mC.getMethod("main", argSub.getClass());
        mM.invoke(null, (Object) argSub);
        Thread.sleep(10000);
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
    
    public static Object invokeConstructor(Class cls, Object... params) throws Exception {
        Constructor c[] = cls.getDeclaredConstructors();
        for(Constructor co : c) {
            if(co.getParameterTypes().length != params.length) {
                continue;
            }
            boolean allCorrect = true;
            for(int i = 0; i < params.length; i++) {
                if(!co.getParameterTypes()[i].isAssignableFrom(params[i].getClass())) {
                    allCorrect = false;
                    break;
                }
            }
            if(!allCorrect) continue;
            
            co.setAccessible(true);
            return co.newInstance(params);
        }
        throw new NoSuchMethodException();
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
        File target = new File(DESTINATION_TMP_PATH + File.separator + globalPath);
        target.getParentFile().mkdirs();
        try (FileWriter writer = new FileWriter(target)) {
            data.write(writer, 4, 0);
        }
    }
    
    public static void writePart(String globalPath, JSONObject data) throws Exception {
        if(!globalPath.endsWith(".json")) {
            globalPath+= ".json";
        }
        File target = new File(DESTINATION_TMP_PATH + File.separator + globalPath);
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
    
    public static void recursiveCopy(File source, String target, boolean minify) throws IOException {
        if(source.isDirectory()) {
            (new File(target)).mkdirs();
            for(File f : source.listFiles()) {
                if(f.isDirectory()) {
                    recursiveCopy(f, target + File.separator + f.getName(), minify);
                } else if(f.isFile()) {
                    if(minify) {
                        minifyJSON(f, new File(target + File.separator + f.getName()));
                    } else {
                        Files.copy(f.toPath(), Path.of(target + File.separator + f.getName()));
                    }
                }
            }
        }
    }
    
    private static void minifyJSON(File source, File target) throws IOException {
        InputStream contentStream = new FileInputStream(source);
        JSONTokener contentTokener = new JSONTokener(contentStream);
        Object data = contentTokener.nextValue();
        
        try (FileWriter writer = new FileWriter(target)) {
            if(data instanceof JSONObject) {
                ((JSONObject) data).write(writer);
            } else if(data instanceof JSONArray) {
                ((JSONArray) data).write(writer);
            } else {
                throw new UnsupportedOperationException("invalid data type(" + data.getClass() + ") found at " + source);
            }
        }
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
