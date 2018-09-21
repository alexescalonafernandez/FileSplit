package io.github.alexescalonafernandez.filesplit.spi;

import io.github.alexescalonafernandez.filesplit.api.OperationMode;
import io.github.alexescalonafernandez.filesplit.api.factory.SplitTaskFactory;
import io.github.alexescalonafernandez.filesplit.api.factory.SplitTaskFactoryProvider;

import java.util.Iterator;
import java.util.Optional;
import java.util.ServiceLoader;

/**
 * Created by alexander.escalona on 17/09/2018.
 */
public class SplitTaskFactoryLookup {
    public static SplitTaskFactory lookup(final OperationMode mode) {
        Iterator<SplitTaskFactoryProvider> it = ServiceLoader.load(SplitTaskFactoryProvider.class).iterator();
        SplitTaskFactory result = null;
        while (it.hasNext() && result == null) {
            result = Optional.ofNullable(it.next())
                    .filter(splitTaskFactoryProvider -> splitTaskFactoryProvider.accept(mode))
                    .map(splitTaskFactoryProvider -> splitTaskFactoryProvider.provide())
                    .orElse(result);
        }
        return result;
    }
}
