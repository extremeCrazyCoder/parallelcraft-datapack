package com.parallelcraft.datapack.types;

import com.parallelcraft.datapack.Main;
import com.parallelcraft.datapack.reflection.MRes;
import com.parallelcraft.datapack.reflection.ReflectionHelper;
import com.parallelcraft.datapack.reflection.WrappedField;
import java.lang.reflect.Field;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author extremeCrazyCoder
 */
public class BlockMaterials {
    private static final String OUTPUT_PATH = "world/blockMaterials";
    
    public static final String MATERIAL_PATH = "net.minecraft.world.level.material.Material";
    
    public static void generateDatapackPart() throws Exception {
        System.out.println("Generating BLOCK_MATERIALS part");
        
        JSONArray resultAll = new JSONArray();
        List<WrappedField> entries = ReflectionHelper.c(MATERIAL_PATH).getPublicFinalStaticFields();
        for(WrappedField entry : entries) {
            JSONObject result = new JSONObject();
            result.put("name", entry.getName());
            
            MRes val = entry.getValue();
            JSONObject element = new JSONObject();
            
            element.put("liquid", val.f("liquid").aB());
            element.put("flammable", val.f("flammable").aB());
            element.put("blocksMotion", val.f("blocksMotion").aB());
            element.put("solidBlocking", val.f("solidBlocking").aB());
            element.put("solid", val.f("solid").aB());
            element.put("replaceable", val.f("replaceable").aB());
            
            result.put("element", element);
            resultAll.put(result);
        }
        
        resultAll = Main.generateIDs(resultAll);
        Main.writePart(OUTPUT_PATH, resultAll);
    }
}
