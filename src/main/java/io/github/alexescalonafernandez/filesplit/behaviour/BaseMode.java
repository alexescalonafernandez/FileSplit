package io.github.alexescalonafernandez.filesplit.behaviour;

import io.github.alexescalonafernandez.filesplit.api.OperationMode;
import io.github.alexescalonafernandez.filesplit.api.Required;
import io.github.alexescalonafernandez.filesplit.api.SplitTaskConfiguration;
import io.github.alexescalonafernandez.filesplit.api.SplitTaskNotification;
import io.github.alexescalonafernandez.filesplit.task.SplitTaskExecutor;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by alexander.escalona on 17/09/2018.
 */
public abstract class BaseMode implements Runnable, SplitTaskConfiguration, SplitTaskNotification {
    protected final SplitTaskConfiguration baseSplitTaskConfiguration;
    private final HashMap<Method, Object> properties;
    public BaseMode(SplitTaskConfiguration baseSplitTaskConfiguration) {

        this.baseSplitTaskConfiguration = baseSplitTaskConfiguration;
        this.properties = new HashMap<>();
    }

    @Override
    public void run() {
        try {
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
            SplitTaskConfiguration proxyInstance = (SplitTaskConfiguration) Proxy.newProxyInstance(SplitTaskConfiguration.class.getClassLoader(),
                    new Class[]{SplitTaskConfiguration.class}, handler
            );

            SplitTaskExecutor splitTaskExecutor = new SplitTaskExecutor(proxyInstance, this);
            splitTaskExecutor.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public static boolean canRunWithoutUserInteraction(final SplitTaskConfiguration configuration) {
        if(configuration.getOperationMode() == null) {
            return false;
        }
        return Arrays.asList(SplitTaskConfiguration.class.getDeclaredMethods())
                .stream()
                .filter(method -> method.getAnnotation(Required.class) != null)
                .collect(Collectors.groupingBy(method -> method.getAnnotation(Required.class)))
                .entrySet()
                .stream()
                .filter(requiredListEntry ->
                        OperationMode.ANY.equals(requiredListEntry.getKey().value()) ||
                                requiredListEntry.getKey().value().equals(configuration.getOperationMode()))
                .map(requiredListEntry -> requiredListEntry.getValue())
                .flatMap(Collection::stream)
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
