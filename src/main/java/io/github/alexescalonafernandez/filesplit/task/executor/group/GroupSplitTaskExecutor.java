package io.github.alexescalonafernandez.filesplit.task.executor.group;

import io.github.alexescalonafernandez.filesplit.api.SplitTaskConfiguration;
import io.github.alexescalonafernandez.filesplit.api.SplitTaskNotification;
import io.github.alexescalonafernandez.filesplit.task.data.Line;
import io.github.alexescalonafernandez.filesplit.task.executor.SplitTaskExecutor;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

/**
 * Created by alexander.escalona on 13/09/2018.
 */
public class GroupSplitTaskExecutor extends SplitTaskExecutor {
    public GroupSplitTaskExecutor(SplitTaskConfiguration taskConfiguration, SplitTaskNotification taskNotification) throws IOException {
        super(taskConfiguration, taskNotification);
    }

    @Override
    protected void executeSplitTasks(ExecutorService executorService, CountDownLatch splitTaskCountDownLatch, BlockingQueue<Line> lineBlockingQueue, BlockingQueue<Integer> integerBlockingQueue) {
        tasks.forEach(splitContext -> executorService.execute(
                new GroupSplitTask(splitContext, splitTaskCountDownLatch,
                        line -> lineBlockingQueue.add(line),
                        byteReads -> integerBlockingQueue.add(byteReads)
                )
        ));
    }
}
