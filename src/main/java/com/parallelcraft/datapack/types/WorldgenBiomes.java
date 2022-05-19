package com.parallelcraft.datapack.types;

import com.parallelcraft.datapack.Main;
import com.parallelcraft.datapack.reflection.MRes;
import com.parallelcraft.datapack.reflection.ReflectionHelper;
import java.util.Optional;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author extremeCrazyCoder
 */
public class WorldgenBiomes {
    private static final String OUTPUT_PATH = "worldgen/biome";
    
    private static final String REGISTRY_NAME = "BIOME_REGISTRY";

    public static void generateDatapackPart() throws Exception {
        System.out.println("Generating WORLDGEN_BIOMES part");
        
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
            settings.put("downfall", val.i("getDownfall").aF());
            settings.put("temperature", val.i("getBaseTemperature").aF());
            settings.put("precipitation", val.i("getPrecipitation").i("getSerializedName").aStr());
            settings.put("category", val.i("getBiomeCategory").i("getSerializedName").aStr());
            
            MRes effObj = val.i("getSpecialEffects");
            JSONObject effects = new JSONObject();
            effects.put("sky_color", effObj.i("getSkyColor").aI());
           
            Optional<Integer> grassOverride = effObj.i("getGrassColorOverride").aOI();
            if(! grassOverride.isEmpty()) {
                effects.put("grass_color", grassOverride.get());
            }
            
            effects.put("grass_color_modifier", effObj.i("getGrassColorModifier").get().toString());
            
            Optional<Integer> foliageOverride = effObj.i("getFoliageColorOverride").aOI();
            if(! foliageOverride.isEmpty()) {
                effects.put("foliage_color", foliageOverride.get());
            }
            effects.put("water_fog_color", effObj.i("getWaterFogColor").aI());
            effects.put("fog_color", effObj.i("getFogColor").aI());
            effects.put("water_color", effObj.i("getWaterColor").aI());
            
            Optional<?> particleOpt = (Optional<?>) effObj.i("getAmbientParticleSettings").get();
            if(! particleOpt.isEmpty()) {
                JSONObject particle = new JSONObject();
                MRes data = MRes.wrap(particleOpt.get());
                particle.put("probability", data.f("probability").aF());
                int partID = ReflectionHelper.getRegistryID(data.f("options").i("getType"), ParticleTypes.REGISTRY_NAME);
                particle.put("id", partID);
                effects.put("particle", particle);
            }
            
            Optional<?> caveSoundEffectSettingsOpt = (Optional<?>) effObj.i("getAmbientMoodSettings").get();
            if(! caveSoundEffectSettingsOpt.isEmpty()) {
                JSONObject caveSoundEffectSettings = new JSONObject();
                MRes data = MRes.wrap(caveSoundEffectSettingsOpt.get());
                
                caveSoundEffectSettings.put("tick_delay", data.f("tickDelay").aI());
                caveSoundEffectSettings.put("offset", data.f("soundPositionOffset").aD());
                caveSoundEffectSettings.put("block_search_extent", data.f("blockSearchExtent").aI());
                caveSoundEffectSettings.put("sound", data.f("soundEvent").i("getLocation").get().toString());
                effects.put("mood_sound", caveSoundEffectSettings);
            }
            
            Optional<?> caveSoundEffectOpt = (Optional<?>) effObj.i("getAmbientAdditionsSettings").get();
            if(! caveSoundEffectOpt.isEmpty()) {
                JSONObject caveSoundEffect = new JSONObject();
                MRes data = MRes.wrap(caveSoundEffectOpt.get());
                
                caveSoundEffect.put("tick_chance", data.f("tickChance").aD());
                caveSoundEffect.put("sound", data.f("soundEvent").i("getLocation").get().toString());
                effects.put("additions_sound", caveSoundEffect);
            }

            Optional<?> musicOpt = (Optional<?>) effObj.i("getBackgroundMusic").get();
            if(! musicOpt.isEmpty()) {
                JSONObject music = new JSONObject();
                MRes data = MRes.wrap(musicOpt.get());
                
                music.put("sound", data.f("event").i("getLocation").get().toString());
                music.put("max_delay", data.f("minDelay").aI());
                music.put("min_delay", data.f("maxDelay").aI());
                music.put("replace_current_music", data.f("replaceCurrentMusic").aB());
                effects.put("music", music);
            }
            
            Optional<?> soundOpt = (Optional<?>) effObj.i("getAmbientLoopSoundEvent").get();
            if(! soundOpt.isEmpty()) {
                MRes data = MRes.wrap(soundOpt.get());
                effects.put("ambient_sound", data.i("getLocation").get().toString());
            }
            
            settings.put("effects", effects);
            result.put("element", settings);
            resultAll.put(result);
        }
        
        Main.writePart(OUTPUT_PATH, resultAll);
    }
}
