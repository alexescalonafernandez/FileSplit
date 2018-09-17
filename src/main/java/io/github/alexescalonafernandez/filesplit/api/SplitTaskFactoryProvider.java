package io.github.alexescalonafernandez.filesplit.api;

/**
 * Created by alexander.escalona on 17/09/2018.
 */
public interface SplitTaskFactoryProvider {
    boolean accept(OperationMode mode);
    SplitTaskFactory provide();
}
