package io.github.alexescalonafernandez.filesplit.api.factory;

import io.github.alexescalonafernandez.filesplit.api.SplitTaskConfiguration;
import io.github.alexescalonafernandez.filesplit.task.data.SplitContext;

import java.util.List;

/**
 * Created by alexander.escalona on 21/09/2018.
 */
public interface SplitContextTasksFactory {
    List<SplitContext> createSplitTasksContexts(SplitTaskConfiguration taskConfiguration);
}
