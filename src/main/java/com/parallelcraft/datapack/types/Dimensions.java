package com.parallelcraft.datapack.types;

import com.parallelcraft.datapack.Main;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author extremeCrazyCoder
 */
public class Dimensions {
    public static final String OUTPUT_PATH = "dimension_type";
    
    public static final String REGISTRY_ACCESS_PATH = "net.minecraft.core.RegistryAccess";
    public static final String DIMENSION_DATA_PATH = "net.minecraft.world.level.dimension.DimensionType";
    
    public static final String REGISTRY_NAME = "DIMENSION_TYPE_REGISTRY";

    public static void generateDatapackPart(Class registryClass) throws Exception {
        Class regAcc = Main.fetchClass(REGISTRY_ACCESS_PATH);
        Object obj = Main.invokeReflective(regAcc, null, "builtin");
        Object registry = Main.invokeUnknownReflective(obj.getClass(), obj, "registryOrThrow", Main.readReflective(registryClass, null, REGISTRY_NAME));
        
        Class clsBeh = Main.fetchClass(DIMENSION_DATA_PATH);
        JSONArray resultAll = new JSONArray();
        
        Set<Map.Entry<?, ?>> entries = (Set<Map.Entry<?, ?>>) registry.getClass().getMethod("entrySet").invoke(registry);
        for(Map.Entry<?, ?> entry : entries) {
            Object loc = entry.getKey().getClass().getMethod("location").invoke(entry.getKey());
            String name = (String) (loc.getClass().getMethod("getPath").invoke(loc));
            
            JSONObject result = new JSONObject();
            result.put("name", name);
            
            Object val = entry.getValue();
            result.put("id", Main.getID(val, registry));
            
            JSONObject settings = new JSONObject();
            settings.put("piglin_safe", (boolean) Main.invokeReflective(clsBeh, val, "piglinSafe"));
            settings.put("bed_works", (boolean) Main.invokeReflective(clsBeh, val, "bedWorks"));
            settings.put("has_skylight", (boolean) Main.invokeReflective(clsBeh, val, "hasSkyLight"));
            settings.put("respawn_anchor_works", (boolean) Main.invokeReflective(clsBeh, val, "respawnAnchorWorks"));
            settings.put("has_ceiling", (boolean) Main.invokeReflective(clsBeh, val, "hasCeiling"));
            settings.put("ultrawarm", (boolean) Main.invokeReflective(clsBeh, val, "ultraWarm"));
            settings.put("natural", (boolean) Main.invokeReflective(clsBeh, val, "natural"));
            settings.put("has_raids", (boolean) Main.invokeReflective(clsBeh, val, "hasRaids"));
            
            settings.put("coordinate_scale", (double) Main.invokeReflective(clsBeh, val, "coordinateScale"));
            settings.put("min_y", (int) Main.invokeReflective(clsBeh, val, "minY"));
            settings.put("height", (int) Main.invokeReflective(clsBeh, val, "height"));
            settings.put("logical_height", (int) Main.invokeReflective(clsBeh, val, "logicalHeight"));
            settings.put("ambient_light", (float) Main.readReflective(clsBeh, val, "ambientLight"));
            
            settings.put("infiniburn", Main.readReflective(clsBeh, val, "infiniburn").toString());
            settings.put("effects", Main.readReflective(clsBeh, val, "effectsLocation").toString());
            
            result.put("element", settings);
            resultAll.put(result);
        }
        
        Main.writePart(OUTPUT_PATH, resultAll);
    }
}
