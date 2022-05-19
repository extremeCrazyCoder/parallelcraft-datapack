package com.parallelcraft.datapack.reflection;

import com.parallelcraft.datapack.ClassMapping;
import com.parallelcraft.datapack.MethodMapping;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A wrapper for a Class. Uses the provided mappings for function calls
 * 
 * @author extremeCrazyCoder
 */
public class RefCls {
    private final Class inst;
    private final ClassMapping instMap;

    RefCls(Class c, ClassMapping map) {
        inst = c;
        instMap = map;
    }

    /**
     * Invoke a static method of that class
     * @param methodName The name of that method
     * @param args The arguments of that method
     */
    public MRes i(String methodName, Object... args) throws Exception {
        Class arguments[] = new Class[args.length];
        for(int i = 0; i < args.length; i++) {
            if(args[i] instanceof MRes methodResult) {
                args[i] = methodResult.get();
            }
            arguments[i] = (args[i] != null)?args[i].getClass():null;
        }

        MethodMapping mMapping = instMap.getRawMethodName(methodName, arguments);
        Method m = inst.getDeclaredMethod(mMapping.getRawName(), mMapping.getArgs());
        m.setAccessible(true);
        return MRes.wrap(m.invoke(null, args));
    }

    /**
     * Invoke a non static method of that class
     * @param methodName The name of that method
     * @param args The arguments of that method
     */
    public MRes iO(Object instance, String methodName, Object... args) throws Exception {
        Class arguments[] = new Class[args.length];
        for(int i = 0; i < args.length; i++) {
            if(args[i] instanceof MRes methodResult) {
                args[i] = methodResult.get();
            }
            arguments[i] = (args[i] != null)?args[i].getClass():null;
        }

        MethodMapping mMapping;
        if(instMap != null) {
            try {
                mMapping = instMap.getRawMethodName(methodName, arguments);
            } catch(NoSuchMethodException e) {
                List<Class> clsToTry = new ArrayList<>();
                if(inst.getSuperclass() != null && !inst.getSuperclass().equals(Object.class)) {
                    clsToTry.add(inst.getSuperclass());
                }
                clsToTry.addAll(Arrays.asList(inst.getInterfaces()));
                
                for(Class c : clsToTry) {
                    try {
                        RefCls superCls = ReflectionHelper.cRaw(c);
                        return superCls.iO(instance, methodName, args);
                    } catch(NoSuchMethodException ex) {}
                }
                throw e;
            }

            Method m = inst.getDeclaredMethod(mMapping.getRawName(), mMapping.getArgs());
            m.setAccessible(true);
            return MRes.wrap(m.invoke(instance, args));
            
        } else {
            for(Method m : inst.getDeclaredMethods()) {
                if(! m.getName().equals(methodName)) continue;
                if(! RefCls.checkArguments(m.getParameterTypes(), arguments)) continue;
                
                m.setAccessible(true);
                return MRes.wrap(m.invoke(instance, args));
            }
            
            if(inst.getSuperclass() != null && !inst.getSuperclass().equals(Object.class)) {
                RefCls superCls = ReflectionHelper.cRaw(inst.getSuperclass());
                return superCls.iO(instance, methodName, args);
            }
            
            throw new NoSuchMethodException(methodName);
        }
    }

    /**
     * Returns a static field of that class
     * @param name The name of that field
     */
    public MRes f(String name) throws Exception {
        String rawName = instMap.getRawFieldName(name);
        if(rawName == null) throw new NoSuchFieldException(name);
        Field f = inst.getDeclaredField(rawName);
        f.setAccessible(true);
        return MRes.wrap(f.get(null));
    }

    /**
     * Returns a static field of that class
     * @param name The name of that field
     */
    public MRes fO(Object instance, String name) throws Exception {
        String rawName = instMap.getRawFieldName(name);
        if(rawName == null) {
            RefCls superCls = ReflectionHelper.cRaw(inst.getSuperclass());
            return superCls.fO(instance, name);
        }

        Field f = inst.getDeclaredField(rawName);
        f.setAccessible(true);
        return MRes.wrap(f.get(instance));
    }
    
    public List<WrappedField> getPublicFinalStaticFields() throws Exception {
        List<WrappedField> result = new ArrayList<>();
        for(Field entry : inst.getFields()) {
            //skip non static non final fields
            if((entry.getModifiers() & Modifier.FINAL) == 0) continue;
            if((entry.getModifiers() & Modifier.STATIC) == 0) continue;
            
            result.add(WrappedField.wrap(entry, instMap));
        }
        
        return result;
    }
    
    public String findName(Object toFind) throws Exception {
        if(toFind instanceof MRes) {
            toFind = ((MRes) toFind).get();
        }
        for(Field f: inst.getDeclaredFields()) {
            if(! Modifier.isStatic(f.getModifiers())) continue;
            
            f.setAccessible(true);
            if(f.get(null) == toFind) {
                return instMap.getOrigFieldName(f.getName());
            }
        }
        return null;
    }

    public MRes create(Object... args) throws Exception {
        Class arguments[] = new Class[args.length];
        for(int i = 0; i < args.length; i++) {
            if(args[i] instanceof MRes methodResult) {
                args[i] = methodResult.get();
            }
            arguments[i] = args[i].getClass();
        }

        for(Constructor c : inst.getConstructors()) {
            if(! RefCls.checkArguments(c.getParameterTypes(), arguments)) continue;
            return MRes.wrap(c.newInstance(args));
        }

        throw new NoSuchMethodException("<init>");
    }

    RefCls fetchClassInternal(String sub, Class def) throws ClassNotFoundException {
        for(Class i : inst.getDeclaredClasses()) {
            if(i.getSimpleName().equals(sub)) {
                if(instMap == null) {
                    return new RefCls(i, null);
                }
                return new RefCls(i, ReflectionHelper.getForRawName(instMap.getRawName() + "$" + sub));
            }
        }
        
        if(instMap == null) {
            return new RefCls(def, null);
        }
        return new RefCls(def, ReflectionHelper.getForRawName(instMap.getRawName() + "$" + sub));
    }

    public Class inst() {
        return inst;
    }
    
    public static boolean checkArguments(Class[] target, Class[] args) {
        if(target.length != args.length) return false;
        for(int i = 0; i < args.length; i++) {
            if(! asObjectClass(target[i]).isAssignableFrom(args[i])) {
                return false;
            }
        }
        return true;
    }
    
    public final static Map<Class<?>, Class<?>> map = new HashMap<Class<?>, Class<?>>();
    static {
        map.put(boolean.class, Boolean.class);
        map.put(byte.class, Byte.class);
        map.put(short.class, Short.class);
        map.put(char.class, Character.class);
        map.put(int.class, Integer.class);
        map.put(long.class, Long.class);
        map.put(float.class, Float.class);
        map.put(double.class, Double.class);
    }
    
    public static Class<?> asObjectClass(Class<?> in) {
        if(map.containsKey(in)) {
            return map.get(in);
        }
        return in;
    }
}
