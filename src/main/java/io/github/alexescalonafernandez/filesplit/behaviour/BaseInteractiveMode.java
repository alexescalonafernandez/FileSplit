package io.github.alexescalonafernandez.filesplit.behaviour;

import io.github.alexescalonafernandez.filesplit.api.Required;
import io.github.alexescalonafernandez.filesplit.api.SplitTaskConfiguration;
import io.github.alexescalonafernandez.filesplit.api.SplitTaskConfigurationFromArgs;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.Supplier;

import static io.github.alexescalonafernandez.filesplit.api.IntrospectionUtil.getMethodValue;
import static io.github.alexescalonafernandez.filesplit.api.SplitTaskConfigurationUtils.canRunWithoutUserInteraction;
import static io.github.alexescalonafernandez.filesplit.api.SplitTaskConfigurationUtils.getRequiredMethods;

/**
 * Created by alexander.escalona on 17/09/2018.
 */
public abstract class BaseInteractiveMode extends BaseMode implements SplitTaskConfiguration {
    protected final SplitTaskConfigurationFromArgs splitTaskConfigurationFromArgs;
    private final HashMap<Method, Object> properties;
    public BaseInteractiveMode(SplitTaskConfigurationFromArgs splitTaskConfigurationFromArgs) {
        super(splitTaskConfigurationFromArgs);
        this.splitTaskConfigurationFromArgs = splitTaskConfigurationFromArgs;
        this.properties = new HashMap<>();
    }

    @Override
    protected SplitTaskConfiguration getSplitTaskConfiguration() {
        // creating proxy
        final SplitTaskConfiguration thisImplementation = this;
        InvocationHandler handler = (proxy, method, args) -> {
            if(!properties.containsKey(method)) {
                Object value = Optional.ofNullable(method)
                        .filter(m -> {
                            if(m.getAnnotation(Required.class) != null) {
                                return canRunWithoutUserInteraction(
                                        splitTaskConfigurationFromArgs,
                                        m.getAnnotation(Required.class).priority()
                                ) || !splitTaskConfigurationFromArgs.isArgumentDataDefault(m);
                            } else {
                                return !splitTaskConfigurationFromArgs.isArgumentDataDefault(m);
                            }
                        })
                        .map(m -> getMethodValue(splitTaskConfigurationFromArgs, m, args))
                        .map(methodResult -> (Supplier<Object>) () -> methodResult)
                        .orElse(() -> getMethodValue(thisImplementation, method, args))
                        .get();
                properties.put(method, value);
            }
            return properties.get(method);
        };
        final SplitTaskConfiguration proxyInstance = (SplitTaskConfiguration) Proxy.newProxyInstance(
                SplitTaskConfiguration.class.getClassLoader(),
                new Class[]{SplitTaskConfiguration.class},
                handler
        );

        //populate properties
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
}
