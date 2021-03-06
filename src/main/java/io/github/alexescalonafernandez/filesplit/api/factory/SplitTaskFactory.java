package io.github.alexescalonafernandez.filesplit.api.factory;

import io.github.alexescalonafernandez.filesplit.task.BaseTask;
import io.github.alexescalonafernandez.filesplit.task.data.Line;
import io.github.alexescalonafernandez.filesplit.task.data.SplitContext;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Created by alexander.escalona on 17/09/2018.
 */
public interface SplitTaskFactory {
    BaseTask create(SplitContext splitContext, CountDownLatch countDownLatch,
                    Consumer<Line> writeNotifier, Consumer<Integer> progressNotifier,
                    AtomicBoolean stopPopulate);
}
