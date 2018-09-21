package io.github.alexescalonafernandez.filesplit.spi.block;

import io.github.alexescalonafernandez.filesplit.api.factory.SplitTaskFactory;
import io.github.alexescalonafernandez.filesplit.task.SplitTask;
import io.github.alexescalonafernandez.filesplit.task.data.Line;
import io.github.alexescalonafernandez.filesplit.task.data.SplitContext;
import io.github.alexescalonafernandez.filesplit.task.split.BlockSplitTask;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Created by alexander.escalona on 17/09/2018.
 */
public class BlockSplitTaskFactory implements SplitTaskFactory{
    @Override
    public SplitTask create(SplitContext splitContext, CountDownLatch countDownLatch,
                            Consumer<Line> writeNotifier, Consumer<Integer> progressNotifier,
                            AtomicBoolean stopPopulate) {
        return new BlockSplitTask(splitContext, countDownLatch, writeNotifier, progressNotifier, stopPopulate);
    }
}
