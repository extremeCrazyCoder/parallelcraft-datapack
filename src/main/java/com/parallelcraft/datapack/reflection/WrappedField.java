package com.parallelcraft.datapack.reflection;

import com.parallelcraft.datapack.ClassMapping;
import java.lang.reflect.Field;

/**
 * A wrapper for a Class. Uses the provided mappings for function calls
 * 
 * @author extremeCrazyCoder
 */
public class WrappedField {
    private ClassMapping mapping;
    private Field field;
    
    private WrappedField(Field toWrap, ClassMapping context) {
        mapping = context;
        field = toWrap;
    }

    static WrappedField wrap(Field toWrap, ClassMapping context) {
        return new WrappedField(toWrap, context);
    }
    
    public String getName() {
        String name = null;
        if(mapping != null) {
            name = mapping.getOrigFieldName(field.getName());
        }
        if(name == null) {
            name = field.getName();
        }
        return name;
    }
    
    public MRes getValue() throws Exception {
        return MRes.wrap(field.get(null));
    }
}
