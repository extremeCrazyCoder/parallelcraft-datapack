package com.parallelcraft.datapack.types;

import com.parallelcraft.datapack.Main;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author extremeCrazyCoder
 */
public class BlockMaterials {
    public static final String OUTPUT_PATH = "world/blockMaterials";
    
    public static final String MATERIAL_PATH = "net.minecraft.world.level.material.Material";
    
    public static void generateDatapackPart(Class registryClass) throws Exception {
        System.out.println("Generating BLOCK_MATERIALS part");
        
        Class clsBeh = Main.fetchClass(MATERIAL_PATH);
        JSONArray resultAll = new JSONArray();

        Field[] entries = clsBeh.getFields();
        int id = 0;
        for(Field entry : entries) {
            //skip non static non final fields
            if((entry.getModifiers() & Modifier.FINAL) == 0) continue;
            if((entry.getModifiers() & Modifier.STATIC) == 0) continue;
            
            JSONObject result = new JSONObject();
            result.put("name", entry.getName());
            result.put("id", id++);
            
            Object val = entry.get(null);
            JSONObject element = new JSONObject();
            
            element.put("liquid", (boolean) Main.readReflective(clsBeh, val, "liquid"));
            element.put("flammable", (boolean) Main.readReflective(clsBeh, val, "flammable"));
            element.put("blocksMotion", (boolean) Main.readReflective(clsBeh, val, "blocksMotion"));
            element.put("solidBlocking", (boolean) Main.readReflective(clsBeh, val, "solidBlocking"));
            element.put("solid", (boolean) Main.readReflective(clsBeh, val, "solid"));
            element.put("replaceable", (boolean) Main.readReflective(clsBeh, val, "replaceable"));
            
            result.put("element", element);
            resultAll.put(result);
        }
        
        Main.writePart(OUTPUT_PATH, resultAll);
    }
}
