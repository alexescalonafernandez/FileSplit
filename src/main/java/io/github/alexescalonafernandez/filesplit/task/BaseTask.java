package io.github.alexescalonafernandez.filesplit.task;

import io.github.alexescalonafernandez.filesplit.task.data.Line;
import io.github.alexescalonafernandez.filesplit.task.data.SplitContext;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by alexander.escalona on 11/09/2018.
 */
public abstract class BaseTask implements Runnable {
    protected final SplitContext splitContext;
    private CountDownLatch countDownLatch;
    protected Consumer<Line> writeNotifier;
    protected Consumer<Integer> progressNotifier;
    private final AtomicBoolean stopPopulate;

    public BaseTask(SplitContext splitContext, CountDownLatch countDownLatch,
                    Consumer<Line> writeNotifier, Consumer<Integer> progressNotifier, AtomicBoolean stopPopulate) {
        this.splitContext = splitContext;
        this.countDownLatch = countDownLatch;
        this.writeNotifier = writeNotifier;
        this.progressNotifier = progressNotifier;
        this.stopPopulate = stopPopulate;
    }

    @Override
    public void run() {
        RandomAccessFile raf = null;
        try {
            // set the file pointer at the beginning of the chunk to process
            raf = new RandomAccessFile(new File(splitContext.getFilePath()), "r");
            raf.seek(splitContext.getBeginFilePointer());

            // for read 1Kb
            byte[] chunk = new byte[1024];
            int byteReads, end = 0, size;
            StringBuilder buffer = new StringBuilder();
            long chunkOffset = -1, beginLineOffset;
            Pattern pattern = Pattern.compile("[^\\n]*\\n", Pattern.MULTILINE);
            // process from @{code splitContext.beginFilePointer} till @{code splitContext.endFilePointer}
            while (raf.getFilePointer() <= splitContext.getEndFilePointer()) {
                // for reading the last portion, which can be less than 1Kb
                chunkOffset = raf.getFilePointer() - buffer.length();
                if(splitContext.getEndFilePointer() - raf.getFilePointer() + 1 < chunk.length)
                    byteReads = raf.read(chunk, 0, (int)(splitContext.getEndFilePointer() - raf.getFilePointer() + 1));
                else byteReads = raf.read(chunk);

                buffer.append(new String(chunk, 0, byteReads));
                end = 0;
                Matcher matcher = pattern.matcher(buffer.toString());
                while (matcher.find()) {
                    processLineWrapper(
                            chunkOffset + matcher.start(),
                            chunkOffset + matcher.end() - 1,
                            matcher.group(0));
                    end = matcher.end();
                }
                size = buffer.length();
                buffer.delete(0, end);
                progressNotifier.accept(size - buffer.length());
            }
            if(buffer.length() > 0) {
                String line = buffer.toString();
                processLineWrapper(
                        splitContext.getEndFilePointer() - buffer.length(),
                        splitContext.getEndFilePointer(),
                        line);
                progressNotifier.accept(buffer.length());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(raf != null) {
                try {
                    raf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            dispose();
        }
    }

    protected abstract void processLine(long beginLineOffset, long endLineOffset, String line);

    protected void dispose() {
        countDownLatch.countDown();
    }

    protected void processLineWrapper(long beginLineOffset, long endLineOffset, String line) {
        while (stopPopulate.get())
            Thread.yield();
        processLine(beginLineOffset, endLineOffset, line);
    }
}
