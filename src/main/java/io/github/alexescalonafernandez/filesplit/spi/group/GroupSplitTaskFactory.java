package io.github.alexescalonafernandez.filesplit.spi.group;

import io.github.alexescalonafernandez.filesplit.api.SplitTaskFactory;
import io.github.alexescalonafernandez.filesplit.task.SplitTask;
import io.github.alexescalonafernandez.filesplit.task.data.Line;
import io.github.alexescalonafernandez.filesplit.task.data.SplitContext;
import io.github.alexescalonafernandez.filesplit.task.group.GroupSplitTask;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Created by alexander.escalona on 17/09/2018.
 */
public class GroupSplitTaskFactory implements SplitTaskFactory{
    @Override
    public SplitTask create(SplitContext splitContext, CountDownLatch countDownLatch,
                            Consumer<Line> writeNotifier, Consumer<Integer> progressNotifier, AtomicBoolean stopPopulate) {
        return new GroupSplitTask(splitContext, countDownLatch, writeNotifier, progressNotifier, stopPopulate);
    }
}
