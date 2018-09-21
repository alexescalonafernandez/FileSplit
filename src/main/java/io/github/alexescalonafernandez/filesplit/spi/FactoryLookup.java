package io.github.alexescalonafernandez.filesplit.spi;

import io.github.alexescalonafernandez.filesplit.api.factory.FactoryProvider;

import java.util.Iterator;
import java.util.Optional;
import java.util.ServiceLoader;

/**
 * Created by alexander.escalona on 17/09/2018.
 */
public class FactoryLookup {

    public static <F, A, P extends FactoryProvider<F, A>> F lookup(Class<P> providerClass, A check) {
        Iterator<P> it = ServiceLoader.load(providerClass).iterator();
        F factory = null;
        while (it.hasNext() && factory == null) {
            factory = Optional.ofNullable(it.next())
                    .filter(provider -> provider.accept(check))
                    .map(provider -> provider.provide())
                    .orElse(factory);
        }
        return factory;
    }
}
