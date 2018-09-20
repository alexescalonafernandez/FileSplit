package io.github.alexescalonafernandez.filesplit.api;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Created by alexander.escalona on 20/09/2018.
 */
public class IntrospectionUtil {
    public static boolean hasReturnTypeOf(Class returnType, Method method){
        return returnType.equals(method.getReturnType());
    }

    public static boolean hasSameNameAndParameters(Method methodA, Method methodB, boolean checkParameterTypes){
        if(methodA == null || methodB == null)
            return false;
        boolean sameName = methodA.getName().equals(methodB.getName()),
                sameParametersLength = methodA.getGenericParameterTypes().length == methodB.getGenericParameterTypes().length;

        if(sameName && sameParametersLength){
            if(checkParameterTypes){
                Class[] methodATypes = methodA.getParameterTypes();
                Class[] methodBTypes = methodB.getParameterTypes();
                for(int i = 0, length = methodATypes.length; i < length; i++)
                    if(!methodATypes[i].equals(methodBTypes[i]))
                        return false;
            }
            return true;
        }
        return false;
    }

    public static Object getMethodValue(Object instance, Method method, Object[] args){
        return Optional.ofNullable(method)
                .map(methodInstance -> {
                    methodInstance.setAccessible(true);
                    Object result = null;
                    try {
                        result = methodInstance.invoke(instance, args);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                    finally {
                        return result;
                    }
                }).orElse(null);
    }

    public static Method getCurrentEnclosingMethodFrom(Object runtimeObject) {
        return runtimeObject.getClass().getEnclosingMethod();
    }
}
