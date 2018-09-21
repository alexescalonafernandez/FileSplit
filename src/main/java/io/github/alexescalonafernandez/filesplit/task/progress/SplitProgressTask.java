package io.github.alexescalonafernandez.filesplit.task.progress;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Created by alexander.escalona on 11/09/2018.
 */
public class SplitProgressTask implements Runnable{
    private final long fileLength;
    private final CountDownLatch countDownLatch;
    private final Supplier<BlockingQueue<Integer>> blockingQueueSupplier;
    private final Consumer<Double> progressViewerNotifier;
    private long byteReads;

    public SplitProgressTask(long fileLength, CountDownLatch countDownLatch,
                             Supplier<BlockingQueue<Integer>> blockingQueueSupplier, Consumer<Double> progressViewerNotifier) {
        this.fileLength = fileLength;
        this.countDownLatch = countDownLatch;
        this.blockingQueueSupplier = blockingQueueSupplier;
        this.progressViewerNotifier = progressViewerNotifier;
    }

    @Override
    public void run() {
        if(blockingQueueSupplier.get().size() > 0) {
            List<Integer> chunkRead = new ArrayList<>();
            blockingQueueSupplier.get().drainTo(chunkRead);
            chunkRead.stream().forEach(integer -> byteReads += integer);
            double percent = byteReads * 100.00 / fileLength;
            progressViewerNotifier.accept(percent);
            if(byteReads == fileLength) {
                this.countDownLatch.countDown();
            }
        }
    }
}
