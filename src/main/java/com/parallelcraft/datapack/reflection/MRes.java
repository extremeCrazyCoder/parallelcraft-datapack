package com.parallelcraft.datapack.reflection;

import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A wrapper for the result of a reflective method call or the content of a field
 * Allows easy acess to functions / field of that resulting object
 *
 * @author extremeCrazyCoder
 */
public class MRes implements Iterable<MRes> {
    Object result;

    private MRes(Object res) {
        result = res;
    }


    /**
     * Invoke a method of that class
     * @param methodName The name of that method
     * @param args The arguments of that method
     */
    public MRes i(String methodName, Object... args) throws Exception {
        RefCls refCls = ReflectionHelper.cRaw(result.getClass());
        return refCls.iO(result, methodName, args);
    }


    /**
     * Get a field of that class
     * @param fieldName The name of that field
     */
    public MRes f(String fieldName) throws Exception {
        RefCls refCls = ReflectionHelper.cRaw(result.getClass());
        return refCls.fO(result, fieldName);
    }

    @Override
    public Iterator<MRes> iterator() {
        if(result instanceof Stream) {
            result = ((Stream) result).toList();
        }
        if(result instanceof Iterable) {
            return new MResIterator((Iterable<?>) result);
        }
        throw new IllegalArgumentException("result is not Iterable");
    }

    private static class MResIterator implements Iterator<MRes> {
        Iterator<?> origResult;

        public MResIterator(Iterable<?> iterable) {
            origResult = iterable.iterator();
        }

        @Override
        public boolean hasNext() {
            return origResult.hasNext();
        }

        @Override
        public MRes next() {
            return new MRes(origResult.next());
        }
    }

    public Object get() {
        return result;
    }

    public <T> T as(Class<T> cls) {
        return cls.cast(result);
    }

    public String aStr() {
        return (String) result;
    }

    public Optional<Integer> aOI() {
        return (Optional<Integer>) result;
    }

    public float aF() {
        return (float) result;
    }

    public double aD() {
        return (double) result;
    }

    public int aI() {
        return (int) result;
    }

    public boolean aB() {
        return (boolean) result;
    }
    
    public static MRes wrap(Object data) {
        return new MRes(data);
    }

    public MRes setDefault(Object o) {
        if(result == null) {
            if(o instanceof MRes) {
                result = ((MRes) o).get();
            } else {
                result = o;
            }
        }
        
        return this;
    }
}
