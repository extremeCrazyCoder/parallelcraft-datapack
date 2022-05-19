package com.parallelcraft.datapack.types;

import com.parallelcraft.datapack.Main;
import com.parallelcraft.datapack.reflection.MRes;
import com.parallelcraft.datapack.reflection.ReflectionHelper;
import com.parallelcraft.datapack.reflection.WrappedField;
import static com.parallelcraft.datapack.types.Items.REGISTRY_NAME;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Stream;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * @author extremeCrazyCoder
 */
public class ItemTags {
    public static final String OUTPUT_PATH = "tags/item/";
    private static final String INPUT_PATH = "data/minecraft/tags/items/";
    

    public static void generateDatapackPart() throws Exception {
        System.out.println("Generating ITEM_TAGS part");
        
        Class ref = ReflectionHelper.getRegistryClass().inst();
        for(String fName : Main.getResourceListing(ref, INPUT_PATH, true)) {
            if(!fName.endsWith(".json")) {
                System.out.println("Ignoring: /" + INPUT_PATH + fName);
                continue;
            }
            InputStream contentStream = ref.getResourceAsStream("/" + INPUT_PATH + fName);
            JSONTokener contentTokener = new JSONTokener(contentStream);
            JSONObject contents = new JSONObject(contentTokener);
            
            Main.writePart(OUTPUT_PATH + fName, contents);
        }
        
    }
}
