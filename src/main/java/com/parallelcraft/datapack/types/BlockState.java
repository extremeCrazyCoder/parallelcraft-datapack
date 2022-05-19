package com.parallelcraft.datapack.types;

import com.parallelcraft.datapack.Main;
import com.parallelcraft.datapack.reflection.MRes;
import com.parallelcraft.datapack.reflection.ReflectionHelper;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author extremeCrazyCoder
 */
public class BlockState {
    private static final String OUTPUT_PATH = "world/block_state";
    
    private static final String BLOCK_PATH = "net.minecraft.world.level.block.Block";
    private static final String FIELD_NAME = "BLOCK_STATE_REGISTRY";

    public static void generateDatapackPart() throws Exception {
        System.out.println("Generating BLOCK_STATE part");
        
        MRes registry = ReflectionHelper.c(BLOCK_PATH).f(FIELD_NAME);
        
        JSONArray resultAll = new JSONArray();
        for(MRes entry : registry) {
            int id = registry.i("getId", entry).aI();
            MRes blNamePathOpt = ReflectionHelper.getRegistry(Blocks.REGISTRY_NAME).i("getResourceKey", entry.f("owner"));
            String blName = blNamePathOpt.i("get").i("location").i("getPath").get().toString();
            String name = blName + "_" + id;
            
            JSONObject result = new JSONObject();
            result.put("name", name);
            result.put("id", id);
            
            JSONObject element = new JSONObject();
            element.put("blockId", ReflectionHelper.getRegistryID(entry.f("owner"), Blocks.REGISTRY_NAME));
            element.put("canOcclude", entry.f("canOcclude").aB());
            element.put("useShapeForLightOcclusion", entry.f("useShapeForLightOcclusion").aB());
            element.put("isAir", entry.f("isAir").aB());
            element.put("lightEmission", entry.f("lightEmission").aI());
            element.put("destroySpeed", entry.f("destroySpeed").aF());
            
            MRes values = entry.f("values"); //RegularImmutableBiMap
            JSONObject jValues = new JSONObject();
            for(MRes vEnt : values.i("entrySet")) {
                String key = vEnt.i("getKey").i("getName").get().toString();
                Object val = vEnt.i("getValue").get().toString();
                jValues.put(key, val);
            }
            element.put("values", jValues);
            
            MRes neighbours = entry.f("neighbours"); //HashBasedTable
            JSONObject jNeighbours = new JSONObject();
            for(MRes nEnt : neighbours.i("rowMap").i("entrySet")) {
                String key = nEnt.i("getKey").i("getName").get().toString();
                JSONObject contents = new JSONObject();
                for(MRes cEnt : nEnt.i("getValue").i("entrySet")) {
                    int cEntId = registry.i("getId", cEnt.i("getValue")).aI();
                    if(cEntId >= 0) {
                        contents.put(cEnt.i("getKey").get().toString(), cEntId);
                    }
                }
                jNeighbours.put(key, contents);
            }
            element.put("neighbours", jNeighbours);
            
            result.put("element", element);
            resultAll.put(result);
        }
        
        Main.writePart(OUTPUT_PATH, resultAll);
    }
}
