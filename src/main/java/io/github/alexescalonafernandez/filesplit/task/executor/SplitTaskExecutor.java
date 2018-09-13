package io.github.alexescalonafernandez.filesplit.task.executor;

import io.github.alexescalonafernandez.filesplit.api.SplitTaskConfiguration;
import io.github.alexescalonafernandez.filesplit.api.SplitTaskNotification;
import io.github.alexescalonafernandez.filesplit.task.SplitProgressTask;
import io.github.alexescalonafernandez.filesplit.task.SplitWriteTask;
import io.github.alexescalonafernandez.filesplit.task.data.Line;
import io.github.alexescalonafernandez.filesplit.task.data.SplitContext;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by alexander.escalona on 12/09/2018.
 */
public abstract class SplitTaskExecutor {
    protected final SplitTaskConfiguration taskConfiguration;
    protected final SplitTaskNotification taskNotification;
    private final List<SplitContext> tasks;
    protected long fileLength;

    public SplitTaskExecutor(SplitTaskConfiguration taskConfiguration, SplitTaskNotification taskNotification) throws IOException {
        this.taskConfiguration = taskConfiguration;
        this.taskNotification = taskNotification;
        this.tasks = buildSplitTasksContexts();
    }

    protected abstract SplitContext createSplitContext(long beginFilePointer, long endFilePointer);
    protected abstract void executeSplitTasks(ExecutorService executorService);

    public int getTaskCount() {
        return tasks.size();
    }

    public boolean execute() {
        final ExecutorService executorService = Executors.newFixedThreadPool(taskConfiguration.getMaxThreadNumber(getTaskCount()));
        final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
        try {
            // Configure the tasks concurrency
            final BlockingQueue<Integer> integerBlockingQueue = new LinkedBlockingDeque<>();
            final BlockingQueue<Line> lineBlockingQueue = new LinkedBlockingDeque<>();
            CountDownLatch splitTaskCountDownLatch = new CountDownLatch(getTaskCount());
            CountDownLatch scheduleCountDownLatch = new CountDownLatch(2);

            taskNotification.initProgressViewer();
            SplitProgressTask splitProgressTask = new SplitProgressTask(fileLength, scheduleCountDownLatch,
                    () -> integerBlockingQueue, taskNotification.getProgressViewerNotifier());
            SplitWriteTask splitWriteTask = new SplitWriteTask(scheduleCountDownLatch, () -> lineBlockingQueue);

            scheduler.scheduleAtFixedRate(splitProgressTask, 100, 100, TimeUnit.MILLISECONDS);
            scheduler.scheduleAtFixedRate(splitWriteTask, 100, 100, TimeUnit.MILLISECONDS);
            executeSplitTasks(executorService);
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

    private List<SplitContext> buildSplitTasksContexts() throws IOException {
        RandomAccessFile raf = new RandomAccessFile(new File(taskConfiguration.getFilePath()), "r");
        List<SplitContext> tasks = new ArrayList<>();
        fileLength = raf.length();
        long begin, end = -1, chunkSize = taskConfiguration.getChunkSize();
        int byteReads;
        byte[] chunk = new byte[1024];
        Pattern pattern = Pattern.compile("[^\\n]*\\n", Pattern.MULTILINE);
        boolean flag = true, find;
        while(flag) {
            begin = end + 1;
            end = begin + chunkSize;
            if(end >= raf.length())
                end = raf.length() - 1;
            raf.seek(end);
            find = false;
            do {
                byteReads = raf.read(chunk);
                Matcher matcher = pattern.matcher(new String(chunk, 0, byteReads));
                if(matcher.find()) {
                    find = true;
                    end += matcher.end() - 1;
                } else {
                    end += byteReads - 1;
                }
            } while((raf.getFilePointer() < raf.length()) && !find);

            flag = raf.getFilePointer() < raf.length();
            tasks.add(createSplitContext(begin, end));
            if(tasks.size() == 1)
                taskNotification.getMessageNotifier().accept("Calculating tasks, wait a few seconds...");
        }
        taskNotification.getMessageNotifier().accept( String.format("Calculating tasks success...%s", System.lineSeparator()));
        return tasks;
    }
}
