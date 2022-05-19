package com.parallelcraft.datapack;

import com.parallelcraft.datapack.reflection.RefCls;
import com.parallelcraft.datapack.reflection.ReflectionHelper;

/**
 * Helper class for decoding the Mappings and using the inside the ReflectionHelper
 *
 * @author extremeCrazyCoder
 */
public class MethodMapping {
    private final String origName;
    private final String rawName;
    private final String retVal;
    private final String[] args;
    private Class[] argsCls = null;

    public MethodMapping(String origName, String rawName, String retVal, String[] args) {
        this.origName = origName;
        this.rawName = rawName;
        this.retVal = retVal;
        this.args = args;
    }

    public String getOrigName() {
        return origName;
    }

    public String getRawName() {
        return rawName;
    }

    public Class[] getArgs() throws ClassNotFoundException {
        if(argsCls == null) {
            argsCls = new Class[args.length];
            for(int i = 0; i < args.length; i++) {
                argsCls[i] = ReflectionHelper.classOnly(args[i]);
            }
        }
        return argsCls;
    }

    boolean checkArgs(Class[] arguments) throws ClassNotFoundException {
        Class[] thisArgs = getArgs();
        if(arguments.length != thisArgs.length) return false;
        
        for(int i = 0; i < arguments.length; i++) {
            if(arguments[i] == null) continue;
            if(! RefCls.asObjectClass(thisArgs[i]).isAssignableFrom(arguments[i])) {
                return false;
            }
        }
        return true;
    }
}
