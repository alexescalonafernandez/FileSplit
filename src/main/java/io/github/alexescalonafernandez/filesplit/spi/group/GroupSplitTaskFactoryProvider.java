package io.github.alexescalonafernandez.filesplit.spi.group;

import io.github.alexescalonafernandez.filesplit.api.OperationMode;
import io.github.alexescalonafernandez.filesplit.api.factory.SplitTaskFactory;
import io.github.alexescalonafernandez.filesplit.api.factory.SplitTaskFactoryProvider;

/**
 * Created by alexander.escalona on 17/09/2018.
 */
public class GroupSplitTaskFactoryProvider implements SplitTaskFactoryProvider {
    @Override
    public boolean accept(OperationMode mode) {
        return OperationMode.GROUP.equals(mode);
    }

    @Override
    public SplitTaskFactory provide() {
        return new GroupSplitTaskFactory();
    }
}
