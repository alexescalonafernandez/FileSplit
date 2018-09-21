package io.github.alexescalonafernandez.filesplit.task.write;

import io.github.alexescalonafernandez.filesplit.task.data.Line;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * Created by alexander.escalona on 11/09/2018.
 */
public class SplitWriteTask implements Runnable{
    private final CountDownLatch countDownLatch;
    private final HashMap<String, FileOutputStream> outputStreamHashMap;
    private final Supplier<BlockingQueue<Line>> lineBlockingQueueSupplier;
    private final AtomicBoolean stopPopulate;
    private final Set<String> generatedFiles;

    public SplitWriteTask(CountDownLatch countDownLatch, Supplier<BlockingQueue<Line>> lineBlockingQueueSupplier,
                          AtomicBoolean stopPopulate) {
        this.countDownLatch = countDownLatch;
        this.outputStreamHashMap = new HashMap<>();
        this.lineBlockingQueueSupplier = lineBlockingQueueSupplier;
        this.stopPopulate = stopPopulate;
        this.generatedFiles = new HashSet<>();
    }

    @Override
    public void run() {
        if(lineBlockingQueueSupplier.get().size() > 0) {
            stopPopulate.set(true);
            List<Line> lines = new ArrayList<>();
            lineBlockingQueueSupplier.get().drainTo(lines);
            lines.stream().forEach(line -> {
                try {
                    if(!outputStreamHashMap.containsKey(line.getFilePath())) {
                        if(outputStreamHashMap.size() == 10) {
                            closeAllOutputStreams();
                        }
                        outputStreamHashMap.put(
                                line.getFilePath(),
                                new FileOutputStream(line.getFilePath(), !generatedFiles.add(line.getFilePath()))
                        );
                    }
                    outputStreamHashMap.get(line.getFilePath()).write(line.getText().getBytes());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            closeAllOutputStreams();
            stopPopulate.set(false);
        } else {
            if(this.countDownLatch.getCount() == 1) {
                this.countDownLatch.countDown();
            }
        }

    }

    private void closeAllOutputStreams() {
        outputStreamHashMap.values().forEach(fileOutputStream -> closeOutputStream(fileOutputStream));
        outputStreamHashMap.clear();
    }

    /**
     * Release the resources and close the file writer
     * @param os the writer to close
     */
    private void closeOutputStream(OutputStream os) {
        try {
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
