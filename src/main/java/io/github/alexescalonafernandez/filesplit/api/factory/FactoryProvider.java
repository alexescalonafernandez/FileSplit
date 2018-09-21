package io.github.alexescalonafernandez.filesplit.api.factory;

/**
 * Created by alexander.escalona on 21/09/2018.
 */
public interface FactoryProvider<F, P> {
    boolean accept(P check);
    F provide();
}
