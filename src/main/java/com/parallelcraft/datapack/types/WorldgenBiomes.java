package com.parallelcraft.datapack.types;

import com.parallelcraft.datapack.Main;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author extremeCrazyCoder
 */
public class WorldgenBiomes {
    public static final String OUTPUT_PATH = "worldgen/biome";
    
    public static final String BUILTIN_REGISTRIES_PATH = "net.minecraft.data.BuiltinRegistries";
    public static final String BIOME_DATA_PATH = "net.minecraft.world.level.biome.Biome";
    
    public static final String REGISTRY_NAME = "BIOME";

    public static void generateDatapackPart(Class registryClass) throws Exception {
        System.out.println("Generating WORLDGEN_BIOMES part");
        Class regAcc = Main.fetchClass(BUILTIN_REGISTRIES_PATH);
        Object registry = Main.readReflective(regAcc, null, REGISTRY_NAME);
        
        Class clsBeh = Main.fetchClass(BIOME_DATA_PATH);
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
            settings.put("depth", (float) Main.invokeReflective(clsBeh, val, "getDepth"));
            settings.put("downfall", (float) Main.invokeReflective(clsBeh, val, "getDownfall"));
            settings.put("scale", (float) Main.invokeReflective(clsBeh, val, "getScale"));
            settings.put("temperature", (float) Main.invokeReflective(clsBeh, val, "getBaseTemperature"));
            
            Object precipitation = Main.invokeReflective(clsBeh, val, "getPrecipitation");
            settings.put("precipitation", (String) Main.invokeReflective(precipitation.getClass(), precipitation, "getSerializedName"));
            
            Object category = Main.invokeReflective(clsBeh, val, "getBiomeCategory");
            settings.put("category", (String) Main.invokeReflective(category.getClass(), category, "getSerializedName"));
            
            Object effObj = Main.invokeReflective(clsBeh, val, "getSpecialEffects");
            JSONObject effects = new JSONObject();
            effects.put("sky_color", (int) Main.invokeReflective(effObj.getClass(), effObj, "getSkyColor"));
           
            Optional<Integer> grassOverride = (Optional<Integer>) Main.invokeReflective(effObj.getClass(), effObj, "getGrassColorOverride");
            if(! grassOverride.isEmpty()) {
                effects.put("grass_color", grassOverride.get());
            }
            
            Object grassColorModifier = Main.invokeReflective(effObj.getClass(), effObj, "getGrassColorModifier");
            effects.put("grass_color_modifier", grassColorModifier.toString());
            
            Optional<Integer> foliageOverride = (Optional<Integer>) Main.invokeReflective(effObj.getClass(), effObj, "getFoliageColorOverride");
            if(! foliageOverride.isEmpty()) {
                effects.put("foliage_color", foliageOverride.get());
            }
            effects.put("water_fog_color", (int) Main.invokeReflective(effObj.getClass(), effObj, "getWaterFogColor"));
            effects.put("fog_color", (int) Main.invokeReflective(effObj.getClass(), effObj, "getFogColor"));
            effects.put("water_color", (int) Main.invokeReflective(effObj.getClass(), effObj, "getWaterColor"));
            
            Optional<?> particleOpt = (Optional<?>) Main.invokeReflective(effObj.getClass(), effObj, "getAmbientParticleSettings");
            if(! particleOpt.isEmpty()) {
                JSONObject particle = new JSONObject();
                Object data = particleOpt.get();
                particle.put("probability", Main.readReflective(data.getClass(), data, "probability"));
                Object partOpt = Main.readReflective(data.getClass(), data, "options");
                int partID = Main.getRegistryID(Main.invokeUnknownReflective(partOpt, "getType"), ParticleTypes.REGISTRY_NAME);
                particle.put("id", partID);
                effects.put("particle", particle);
            }
            
            Optional<?> caveSoundEffectSettingsOpt = (Optional<?>) Main.invokeReflective(effObj.getClass(), effObj, "getAmbientMoodSettings");
            if(! caveSoundEffectSettingsOpt.isEmpty()) {
                JSONObject caveSoundEffectSettings = new JSONObject();
                Object data = caveSoundEffectSettingsOpt.get();
                
                caveSoundEffectSettings.put("tick_delay", (int) Main.readReflective(data.getClass(), data, "tickDelay"));
                caveSoundEffectSettings.put("offset", (double) Main.readReflective(data.getClass(), data, "soundPositionOffset"));
                caveSoundEffectSettings.put("block_search_extent", (int) Main.readReflective(data.getClass(), data, "blockSearchExtent"));
                
                Object soundEvent = Main.readReflective(data.getClass(), data, "soundEvent");
                caveSoundEffectSettings.put("sound", Main.invokeReflective(soundEvent.getClass(), soundEvent, "getLocation").toString());
                effects.put("mood_sound", caveSoundEffectSettings);
            }
            
            Optional<?> caveSoundEffectOpt = (Optional<?>) Main.invokeReflective(effObj.getClass(), effObj, "getAmbientAdditionsSettings");
            if(! caveSoundEffectOpt.isEmpty()) {
                JSONObject caveSoundEffect = new JSONObject();
                Object data = caveSoundEffectOpt.get();
                
                caveSoundEffect.put("tick_chance", (double) Main.readReflective(data.getClass(), data, "tickChance"));
                
                Object soundEvent = Main.readReflective(data.getClass(), data, "soundEvent");
                caveSoundEffect.put("sound", Main.invokeReflective(soundEvent.getClass(), soundEvent, "getLocation").toString());
                effects.put("additions_sound", caveSoundEffect);
            }

            Optional<?> musicOpt = (Optional<?>) Main.invokeReflective(effObj.getClass(), effObj, "getBackgroundMusic");
            if(! musicOpt.isEmpty()) {
                JSONObject music = new JSONObject();
                Object data = musicOpt.get();
                
                Object soundEvent = Main.readReflective(data.getClass(), data, "event");
                music.put("sound", Main.invokeReflective(soundEvent.getClass(), soundEvent, "getLocation").toString());
                music.put("max_delay", (int) Main.readReflective(data.getClass(), data, "minDelay"));
                music.put("min_delay", (int) Main.readReflective(data.getClass(), data, "maxDelay"));
                music.put("replace_current_music", (boolean) Main.readReflective(data.getClass(), data, "replaceCurrentMusic"));
                effects.put("music", music);
            }
            
            Optional<?> soundOpt = (Optional<?>) Main.invokeReflective(effObj.getClass(), effObj, "getAmbientLoopSoundEvent");
            if(! soundOpt.isEmpty()) {
                Object data = soundOpt.get();
                effects.put("ambient_sound", Main.invokeReflective(data.getClass(), data, "getLocation").toString());
            }
            
            settings.put("effects", effects);
            result.put("element", settings);
            resultAll.put(result);
        }
        
        Main.writePart(OUTPUT_PATH, resultAll);
    }
}
