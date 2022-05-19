package com.parallelcraft.datapack;

import com.parallelcraft.datapack.reflection.MRes;
import com.parallelcraft.datapack.reflection.ReflectionHelper;
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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Array;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Main class that will generate a vanilla like datapack from vanilla jar
 * 
 * For development:
 * python3 main.py --mcversion 1.18.2 --side server --nauto --clean --download_mapping --remap_mapping --download_jar --remap_jar --delete_dep f --decompile --decompiler f
 * 
 * @author extremeCrazyCoder
 */
public class Main {
    public static final String COMPILE_FOR_VERSION = "1.18.2";
    
    public static final String SOURCE_URL = "https://launchermeta.mojang.com/mc/game/version_manifest.json";
    public static final String TMP_PATH = "target";
    public static final String DESTINATION_TMP_PATH = TMP_PATH + "/pack";
    public static final String DESTINATION_PATCHING_TMP_PATH = TMP_PATH + "/pack_patched";
    public static final String DESTINATION_PATH = "../../parallelcraft/src/main/resources/datapack";

    private static MappingsHelper map;
    public static URLClassLoader clsLoader;
    
    public static void main(String args[]) throws Exception {
        try {
            Downloader.ensureDataDownloaded(COMPILE_FOR_VERSION);
            map = new MappingsHelper(COMPILE_FOR_VERSION);

            recursiveDelete(new File(DESTINATION_TMP_PATH));
            recursiveDelete(new File(DESTINATION_PATCHING_TMP_PATH));
            recursiveDelete(new File(DESTINATION_PATH));
            
            //see https://stackoverflow.com/a/60775
            clsLoader = new URLClassLoader(
                    Downloader.getLibraryURLArray(),
                    new Main().getClass().getClassLoader()
            );
            ReflectionHelper.init(clsLoader, map);
            
            fakeBootServer();

            //also only export sorted data
            // sort by name  for everything without an id
            //export data
            WorldgenBiomes.generateDatapackPart();
            Dimensions.generateDatapackPart();
            ParticleTypes.generateDatapackPart();
            Blocks.generateDatapackPart();
            BlockMaterials.generateDatapackPart();
            BlockSoundTypes.generateDatapackPart();
            BlockState.generateDatapackPart();
            Items.generateDatapackPart();
            Enchantments.generateDatapackPart();
            Effects.generateDatapackPart();
            Sounds.generateDatapackPart();
            LootTables.generateDatapackPart();
            ItemTags.generateDatapackPart();
            
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
    
    private static final String CONSTANTS_PATH = "net.minecraft.SharedConstants";
    private static final String CRASH_REPORT_PATH = "net.minecraft.CrashReport";
    private static final String BOOTSTRAP_PATH = "net.minecraft.server.Bootstrap";
    private static final String DEDICATED_SERVER_SETTINGS_PATH = "net.minecraft.server.dedicated.DedicatedServerSettings";
    private static final String LEVEL_STORAGE_SOURCE_PATH = "net.minecraft.world.level.storage.LevelStorageSource";
    private static final String REPOSITORY_SOURCE_PATH = "net.minecraft.server.packs.repository.RepositorySource";
    private static final String SERVER_PACKS_SOURCE_PATH = "net.minecraft.server.packs.repository.ServerPacksSource";
    private static final String LEVEL_RESOURCE_PATH = "net.minecraft.world.level.storage.LevelResource";
    private static final String PACK_SOURCE_PATH = "net.minecraft.server.packs.repository.PackSource";
    private static final String FOLDER_REPOSITORY_SOURCE_PATH = "net.minecraft.server.packs.repository.FolderRepositorySource";
    private static final String PACK_REPOSITORY_PATH = "net.minecraft.server.packs.repository.PackRepository";
    private static final String PACK_TYPE_PATH = "net.minecraft.server.packs.PackType";
    private static final String RES_PACK_MULTI_MGR_PATH = "net.minecraft.server.packs.resources.MultiPackResourceManager";
    private static final String RES_PACK_PACK_TYPE_PATH = "net.minecraft.server.packs.PackType";
    private static final String REGISTRY_ACCESS_PATH = "net.minecraft.core.RegistryAccess";
    private static final String RELOAD_SERVER_RES_PATH = "net.minecraft.server.ReloadableServerResources";
    private static final String COMMANDS_PATH = "net.minecraft.commands.Commands$CommandSelection";
    private static final String UTIL_PATH = "net.minecraft.Util";
    
    public static void fakeBootServer() throws Exception {
        PrintStream errOrig = System.err;
        PrintStream outOrig = System.out;
        
        ReflectionHelper.c(CONSTANTS_PATH).i("tryDetectVersion");
        ReflectionHelper.c(CRASH_REPORT_PATH).i("preload");
        ReflectionHelper.c(BOOTSTRAP_PATH).i("bootStrap");
        System.setErr(errOrig);
        System.setOut(outOrig);
        ReflectionHelper.c(BOOTSTRAP_PATH).i("validate");
        
        MRes serSettings = ReflectionHelper.c(DEDICATED_SERVER_SETTINGS_PATH).create(Paths.get("server.properties"));
        MRes levelStorageSource = ReflectionHelper.c(LEVEL_STORAGE_SOURCE_PATH).i("createDefault", new File(".").toPath());
        MRes levelStorageAccess = levelStorageSource.i("createAccess", "world");
        
        Object[] repositorySources = (Object[]) Array.newInstance(ReflectionHelper.c(REPOSITORY_SOURCE_PATH).inst(), 2);
        repositorySources[0] = ReflectionHelper.c(SERVER_PACKS_SOURCE_PATH).create().get();
        MRes datapackDir = ReflectionHelper.c(LEVEL_RESOURCE_PATH).f("DATAPACK_DIR");
        MRes packWorld = ReflectionHelper.c(PACK_SOURCE_PATH).f("WORLD");
        repositorySources[1] = ReflectionHelper.c(FOLDER_REPOSITORY_SOURCE_PATH).create(levelStorageAccess.i("getLevelPath", datapackDir).as(Path.class).toFile(), packWorld).get();
        MRes packRepository = ReflectionHelper.c(PACK_REPOSITORY_PATH).create(ReflectionHelper.c(PACK_TYPE_PATH).f("SERVER_DATA"), repositorySources);
        
        try {
            MRes allOpenList = packRepository.i("openAllSelected");
            MRes multiPackMgr = ReflectionHelper.c(RES_PACK_MULTI_MGR_PATH).create(
                    ReflectionHelper.c(RES_PACK_PACK_TYPE_PATH).f("SERVER_DATA"), allOpenList);
            
            MRes regAccess = ReflectionHelper.c(REGISTRY_ACCESS_PATH).i("builtinCopy").i("freeze");
            MRes result = ReflectionHelper.c(RELOAD_SERVER_RES_PATH).i("loadResources", multiPackMgr, regAccess,
                    ReflectionHelper.c(COMMANDS_PATH).f("DEDICATED"), serSettings.i("getProperties").f("functionPermissionLevel"),
                    ReflectionHelper.c(UTIL_PATH).i("backgroundExecutor"), (Executor) Runnable::run)
                .i("whenComplete", (BiConsumer) (var1x, var2x)-> {
                    if (var2x != null) {
                        try {
                            multiPackMgr.i("close");
                        } catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            
            MRes datapackRes = result.i("get");
            datapackRes.i("updateRegistryTags", regAccess);
            ReflectionHelper.setMainRepository(regAccess);
        } finally {
            packRepository.i("close");
        }
    }

    public static JSONArray generateIDs(JSONArray resultAll) {
        List<String> names = new ArrayList<>();
        Map<String, JSONObject> nameMap = new HashMap<>();
        for(int i = 0; i < resultAll.length(); i++) {
            JSONObject o = resultAll.getJSONObject(i);
            String n = o.getString("name");
            names.add(n);
            nameMap.put(n, o);
        }
        names = names.stream().sorted().toList();
        
        JSONArray sortedResult = new JSONArray();
        for(int i = 0; i < names.size(); i++) {
            JSONObject o = nameMap.get(names.get(i));
            o.put("id", i);
            sortedResult.put(o);
        }
        return sortedResult;
    }
    
    public static void writePart(String globalPath, JSONArray data) throws Exception {
        if(!globalPath.endsWith(".json")) {
            globalPath+= ".json";
        }
        JSONArray sorted = new JSONArray();
        for(int i = 0; i < data.length(); i++) {
            for(int j = 0; j < data.length(); j++) {
                if(data.getJSONObject(j).getInt("id") == i) {
                    sorted.put(data.getJSONObject(j));
                    break;
                }
            }
        }
        
        File target = new File(DESTINATION_TMP_PATH + File.separator + globalPath);
        target.getParentFile().mkdirs();
        try (FileWriter writer = new FileWriter(target)) {
            sorted.write(writer, 4, 0);
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
