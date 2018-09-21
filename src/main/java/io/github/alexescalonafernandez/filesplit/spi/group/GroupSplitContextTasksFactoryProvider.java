package io.github.alexescalonafernandez.filesplit.spi.group;

import io.github.alexescalonafernandez.filesplit.api.OperationMode;
import io.github.alexescalonafernandez.filesplit.api.factory.SplitContextTasksFactory;
import io.github.alexescalonafernandez.filesplit.api.factory.SplitContextTasksFactoryProvider;

/**
 * Created by alexander.escalona on 21/09/2018.
 */
public class GroupSplitContextTasksFactoryProvider implements SplitContextTasksFactoryProvider {
    @Override
    public boolean accept(OperationMode mode) {
        return OperationMode.GROUP.equals(mode);
    }

    @Override
    public SplitContextTasksFactory provide() {
        return new GroupSplitContextTasksFactory();
    }
}
