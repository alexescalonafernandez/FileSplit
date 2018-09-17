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
            InvocationHandler handler = new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    if(!properties.containsKey(method)) {
                        properties.put(
                                method ,
                                Optional.ofNullable(
                                        baseSplitTaskConfiguration.getClass().getDeclaredMethod(
                                                method.getName(), method.getParameterTypes()
                                        ).invoke(baseSplitTaskConfiguration, args)
                                )
                                        .filter(o -> canRunWithoutUserInteraction(baseSplitTaskConfiguration))
                                        .orElse(
                                                thisImplementation.getClass().getDeclaredMethod(
                                                        method.getName(),
                                                        method.getParameterTypes())
                                                        .invoke(thisImplementation, args)
                                        )
                        );
                    }
                    return properties.get(method);
                }
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
