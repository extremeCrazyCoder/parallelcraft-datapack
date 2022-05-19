package com.parallelcraft.datapack.types;

import com.parallelcraft.datapack.Main;
import com.parallelcraft.datapack.reflection.MRes;
import com.parallelcraft.datapack.reflection.ReflectionHelper;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author extremeCrazyCoder
 */
public class Dimensions {
    private static final String OUTPUT_PATH = "dimension_type";
    
    private static final String REGISTRY_NAME = "DIMENSION_TYPE_REGISTRY";

    public static void generateDatapackPart() throws Exception {
        System.out.println("Generating DIMENSIONS part");
        
        MRes registry = ReflectionHelper.getRegistry(REGISTRY_NAME);
        
        JSONArray resultAll = new JSONArray();
        MRes entries = registry.i("entrySet");
        for(MRes entry : entries) {
            MRes loc = entry.i("getKey").i("location");
            String name = loc.i("getPath").aStr();
            
            JSONObject result = new JSONObject();
            result.put("name", name);
            
            MRes val = entry.i("getValue");
            result.put("id", registry.i("getId", val).aI());
            
            JSONObject settings = new JSONObject();
            settings.put("piglin_safe", val.i("piglinSafe").aB());
            settings.put("bed_works", val.i("bedWorks").aB());
            settings.put("has_skylight", val.i("hasSkyLight").aB());
            settings.put("respawn_anchor_works", val.i("respawnAnchorWorks").aB());
            settings.put("has_ceiling", val.i("hasCeiling").aB());
            settings.put("ultrawarm", val.i("ultraWarm").aB());
            settings.put("natural", val.i("natural").aB());
            settings.put("has_raids",  val.i("hasRaids").aB());
            
            settings.put("coordinate_scale", val.i("coordinateScale").aD());
            settings.put("min_y", val.i("minY").aI());
            settings.put("height", val.i("height").aI());
            settings.put("logical_height", val.i("logicalHeight").aI());
            settings.put("ambient_light", val.f("ambientLight").aF());
            
            settings.put("infiniburn", val.f("infiniburn").i("location").get().toString());
            settings.put("effects", val.f("effectsLocation").get().toString());
            
            result.put("element", settings);
            resultAll.put(result);
        }
        
        Main.writePart(OUTPUT_PATH, resultAll);
    }
}
