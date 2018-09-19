package io.github.alexescalonafernandez.filesplit.behaviour;

import io.github.alexescalonafernandez.filesplit.api.OperationMode;
import io.github.alexescalonafernandez.filesplit.api.Required;
import io.github.alexescalonafernandez.filesplit.api.SplitTaskConfiguration;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by alexander.escalona on 17/09/2018.
 */
public abstract class BaseInteractiveMode extends BaseMode implements SplitTaskConfiguration {
    protected final SplitTaskConfiguration baseSplitTaskConfiguration;
    private final HashMap<Method, Object> properties;
    public BaseInteractiveMode(SplitTaskConfiguration baseSplitTaskConfiguration) {
        super(baseSplitTaskConfiguration);
        this.baseSplitTaskConfiguration = baseSplitTaskConfiguration;
        this.properties = new HashMap<>();
    }

    @Override
    protected SplitTaskConfiguration getSplitTaskNotification() {
        // creating proxy
        final SplitTaskConfiguration thisImplementation = this;
        InvocationHandler handler = (proxy, method, args) -> {
            if(!properties.containsKey(method)) {
                properties.put(method ,
                        Optional.ofNullable(getMethodValue(baseSplitTaskConfiguration, method, args))
                                .filter(o -> canRunWithoutUserInteraction(baseSplitTaskConfiguration))
                                .orElse(getMethodValue(thisImplementation, method, args))
                );
            }
            return properties.get(method);
        };
        final SplitTaskConfiguration proxyInstance = (SplitTaskConfiguration) Proxy.newProxyInstance(
                SplitTaskConfiguration.class.getClassLoader(),
                new Class[]{SplitTaskConfiguration.class},
                handler
        );

        //get values from terminal if necessary
        getRequiredMethods(proxyInstance)
                .sorted(Comparator.comparing(o -> Integer.valueOf(o.getAnnotation(Required.class).priority())))
                .forEach(method -> {
                    try {
                        method.invoke(proxyInstance);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                });
        return proxyInstance;
    }

    private Object getMethodValue(Object instance, Method method, Object[] args) throws Throwable{
        return  Optional.ofNullable(
                instance.getClass().getMethod(
                        method.getName(), method.getParameterTypes()
                )
        ).map(methodInstance -> {
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

    private static Stream<Method> getRequiredMethods(final SplitTaskConfiguration configuration) {
        return Arrays.asList(SplitTaskConfiguration.class.getMethods())
                .stream()
                .filter(method -> method.getAnnotation(Required.class) != null)
                .collect(Collectors.groupingBy(method -> method.getAnnotation(Required.class).value()))
                .entrySet()
                .stream()
                .filter(requiredListEntry ->
                        OperationMode.ANY.equals(requiredListEntry.getKey()) ||
                                requiredListEntry.getKey().equals(configuration.getOperationMode()))
                .map(requiredListEntry -> requiredListEntry.getValue())
                .flatMap(Collection::stream);
    }

    public static boolean canRunWithoutUserInteraction(final SplitTaskConfiguration configuration) {
        if(configuration.getOperationMode() == null) {
            return false;
        }
        return getRequiredMethods(configuration)
                .map(method -> {
                    boolean result = false;
                    try {
                        result = method.invoke(configuration) != null;
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    } finally {
                        return result;
                    }
                })
                .reduce(true, (acc, current) -> acc && current);
    }
}
