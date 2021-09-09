package com.parallelcraft.datapack.types;

import com.parallelcraft.datapack.Main;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author extremeCrazyCoder
 */
public class BlockState {
    public static final String OUTPUT_PATH = "world/block_state";
    
    public static final String BLOCK_PATH = "net.minecraft.world.level.block.Block";
    public static final String STATE_PATH = "net.minecraft.world.level.block.state.StateHolder";

    public static void generateDatapackPart(Class registryClass) throws Exception {
        Class clsReg = Main.fetchClass(BLOCK_PATH);
        Field f = clsReg.getField("BLOCK_STATE_REGISTRY");
        Iterable<?> obj = (Iterable<?>) f.get(null);
        
        Class stHol = Main.fetchClass(STATE_PATH);
        Class stBase = Main.fetchSubClass(Blocks.BLOCK_SETTINGS_PATH, "BlockStateBase");
        Method idMethod = obj.getClass().getMethod("getId", Object.class);
        
        Object blockRegistry = registryClass.getField("BLOCK").get(null);
        Method blRegResKey = blockRegistry.getClass().getMethod("getResourceKey", Object.class);
        
        JSONArray resultAll = new JSONArray();
        
        for(Object entry : obj) {
            int id = (int) idMethod.invoke(obj, entry);
            Optional<?> blNamePathOpt = (Optional<?>) blRegResKey.invoke(blockRegistry, Main.readReflective(stHol, entry, "owner"));
            Object blNamePath = blNamePathOpt.get();
            Object blLoc = blNamePath.getClass().getMethod("location").invoke(blNamePath);
            String blName = (String) (blLoc.getClass().getMethod("getPath").invoke(blLoc));
            String name = blName + "_" + id;
            
            JSONObject result = new JSONObject();
            result.put("name", name);
            result.put("id", id);
            
            JSONObject element = new JSONObject();
            element.put("blockId", Main.getRegistryID(Main.readReflective(stHol, entry, "owner"), Blocks.REGISTRY_NAME));
            element.put("canOcclude", (boolean) Main.readReflective(stBase, entry, "canOcclude"));
            element.put("useShapeForLightOcclusion", (boolean) Main.readReflective(stBase, entry, "useShapeForLightOcclusion"));
            element.put("isAir", (boolean) Main.readReflective(stBase, entry, "isAir"));
            element.put("lightEmission", (int) Main.readReflective(stBase, entry, "lightEmission"));
            element.put("destroySpeed", (float) Main.readReflective(stBase, entry, "destroySpeed"));
            
            Object values = Main.readReflective(stHol, entry, "values"); //RegularImmutableBiMap
            JSONObject jValues = new JSONObject();
            for(Map.Entry<?, ?> vEnt : (Set<Map.Entry<?, ?>>) Main.invokeUnknownReflective(values, "entrySet")) {
                String key = Main.invokeUnknownReflective(vEnt.getKey(), "getName").toString();
                Object val = vEnt.getValue();
                jValues.put(key, val.toString());
            }
            element.put("values", jValues);
            
            Object neighbours = Main.readReflective(stHol, entry, "neighbours"); //HashBasedTable
            JSONObject jNeighbours = new JSONObject();
            Map<?, Map<?, ?>> rMap = (Map<?, Map<?, ?>>) Main.invokeUnknownReflective(neighbours, "rowMap");
            for(Map.Entry<?, Map<?, ?>> nEnt : rMap.entrySet()) {
                String key = Main.invokeUnknownReflective(nEnt.getKey(), "getName").toString();
                JSONObject contents = new JSONObject();
                for(Map.Entry<?, ?> cEnt : nEnt.getValue().entrySet()) {
                    int cEntId = (int) idMethod.invoke(obj, cEnt.getValue());
                    if(cEntId >= 0) {
                        contents.put(cEnt.getKey().toString(), cEntId);
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
