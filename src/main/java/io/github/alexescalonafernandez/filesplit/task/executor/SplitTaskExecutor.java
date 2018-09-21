package io.github.alexescalonafernandez.filesplit.task.executor;

import io.github.alexescalonafernandez.filesplit.api.SplitTaskConfiguration;
import io.github.alexescalonafernandez.filesplit.api.SplitTaskNotification;
import io.github.alexescalonafernandez.filesplit.api.factory.SplitContextTasksFactory;
import io.github.alexescalonafernandez.filesplit.api.factory.SplitContextTasksFactoryProvider;
import io.github.alexescalonafernandez.filesplit.api.factory.SplitTaskFactory;
import io.github.alexescalonafernandez.filesplit.api.factory.SplitTaskFactoryProvider;
import io.github.alexescalonafernandez.filesplit.spi.FactoryLookup;
import io.github.alexescalonafernandez.filesplit.task.data.Line;
import io.github.alexescalonafernandez.filesplit.task.data.SplitContext;
import io.github.alexescalonafernandez.filesplit.task.progress.SplitProgressTask;
import io.github.alexescalonafernandez.filesplit.task.write.SplitWriteTask;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by alexander.escalona on 12/09/2018.
 */
public class SplitTaskExecutor {
    protected final SplitTaskConfiguration taskConfiguration;
    protected final SplitTaskNotification taskNotification;
    protected long fileLength;

    public SplitTaskExecutor(SplitTaskConfiguration taskConfiguration, SplitTaskNotification taskNotification) throws IOException {
        this.taskConfiguration = taskConfiguration;
        this.taskNotification = taskNotification;
        RandomAccessFile raf = new RandomAccessFile(taskConfiguration.getFilePath(), "r");
        fileLength = raf.length();
        raf.close();
    }

    public boolean execute() {
        taskNotification.getMessageNotifier().accept("Calculating tasks, wait a few seconds...");
        SplitContextTasksFactory splitContextTasksFactory = FactoryLookup.lookup(
                SplitContextTasksFactoryProvider.class,
                taskConfiguration.getOperationMode()
        );
        final List<SplitContext> tasks = splitContextTasksFactory.createSplitTasksContexts(taskConfiguration);
        taskNotification.getMessageNotifier().accept(
                String.format("Calculating tasks success...%s", System.lineSeparator())
        );
        final int taskCount = tasks.size();

        final ExecutorService executorService = Executors.newFixedThreadPool(
                taskConfiguration.getMaxThreadNumber(taskCount)
        );
        final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
        try {
            // Configure the tasks concurrency
            final BlockingQueue<Integer> integerBlockingQueue = new LinkedBlockingDeque<>();
            final BlockingQueue<Line> lineBlockingQueue = new LinkedBlockingDeque<>();
            CountDownLatch splitTaskCountDownLatch = new CountDownLatch(taskCount);
            CountDownLatch scheduleCountDownLatch = new CountDownLatch(2);

            taskNotification.initProgressViewer();
            final AtomicBoolean stopPopulate = new AtomicBoolean(false);
            SplitProgressTask splitProgressTask = new SplitProgressTask(fileLength, scheduleCountDownLatch,
                    () -> integerBlockingQueue, taskNotification.getProgressViewerNotifier());
            SplitWriteTask splitWriteTask = new SplitWriteTask(scheduleCountDownLatch, () -> lineBlockingQueue,
                    stopPopulate);

            scheduler.scheduleAtFixedRate(splitProgressTask, 100, 30, TimeUnit.MILLISECONDS);
            scheduler.scheduleAtFixedRate(splitWriteTask, 100, 30, TimeUnit.MILLISECONDS);

            SplitTaskFactory splitTaskFactory = FactoryLookup.lookup(SplitTaskFactoryProvider.class,
                    taskConfiguration.getOperationMode()
            );
            tasks.forEach(splitContext -> executorService.execute(
                    splitTaskFactory.create(splitContext, splitTaskCountDownLatch,
                            line -> lineBlockingQueue.add(line),
                            byteReads -> integerBlockingQueue.add(byteReads),
                            stopPopulate
                    )
            ));

            splitTaskCountDownLatch.await();
            scheduleCountDownLatch.await();

            return true;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        } finally {
            executorService.shutdown();
            scheduler.shutdownNow();
        }
    }
}
