package io.github.alexescalonafernandez.filesplit.api;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by alexander.escalona on 19/09/2018.
 */
public class SplitTaskConfigurationUtils {

    public static Stream<Method> getRequiredMethods() {
        return Arrays.asList(SplitTaskConfiguration.class.getMethods())
                .stream()
                .filter(method -> method.getAnnotation(Required.class) != null)
                .collect(Collectors.toList())
                .stream();
    }

    public static Stream<Method> getRequiredMethods(final SplitTaskConfiguration configuration) {
        return getRequiredMethods()
                .collect(Collectors.groupingBy(method -> method.getAnnotation(Required.class).value()))
                .entrySet()
                .stream()
                .filter(requiredListEntry ->
                        OperationMode.ANY.equals(requiredListEntry.getKey()) ||
                                requiredListEntry.getKey().equals(configuration.getOperationMode()))
                .map(requiredListEntry -> requiredListEntry.getValue())
                .flatMap(Collection::stream);
    }

    public static boolean canRunWithoutUserInteraction(final SplitTaskConfiguration configuration, int priority) {
        if(configuration.getOperationMode() == null) {
            return false;
        }
        return getRequiredMethods(configuration)
                .filter(method -> method.getAnnotation(Required.class).priority() > priority)
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

    public static boolean canRunWithoutUserInteraction(final SplitTaskConfiguration configuration) {
        return canRunWithoutUserInteraction(configuration, -1);
    }
}
