package io.github.alexescalonafernandez.filesplit.task;

import io.github.alexescalonafernandez.filesplit.task.data.Line;
import io.github.alexescalonafernandez.filesplit.task.data.SplitContext;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by alexander.escalona on 11/09/2018.
 */
public abstract class SplitTask implements Runnable {
    protected SplitContext splitContext;
    private CountDownLatch countDownLatch;
    protected Consumer<Line> writeNotifier;
    protected Consumer<Integer> progressNotifier;

    public SplitTask(SplitContext splitContext, CountDownLatch countDownLatch,
                     Consumer<Line> writeNotifier, Consumer<Integer> progressNotifier) {
        this.splitContext = splitContext;
        this.countDownLatch = countDownLatch;
        this.writeNotifier = writeNotifier;
        this.progressNotifier = progressNotifier;
    }

    @Override
    public void run() {
        try {
            // set the file pointer at the beginning of the chunk to process
            RandomAccessFile raf = new RandomAccessFile(new File(splitContext.getFilePath()), "r");
            raf.seek(splitContext.getBeginFilePointer());

            // for read 1Kb
            byte[] chunk = new byte[1024];
            int byteReads, end, size;
            StringBuilder buffer = new StringBuilder();
            Pattern pattern = Pattern.compile(getLineMatcherRegex(), Pattern.MULTILINE);
            // process from @{code splitContext.beginFilePointer} till @{code splitContext.endFilePointer}
            while (raf.getFilePointer() <= splitContext.getEndFilePointer()) {
                // for reading the last portion, which can be less than 1Kb
                if(splitContext.getEndFilePointer() - raf.getFilePointer() + 1 < chunk.length)
                    byteReads = raf.read(chunk, 0, (int)(splitContext.getEndFilePointer() - raf.getFilePointer() + 1));
                else byteReads = raf.read(chunk);

                buffer.append(new String(chunk, 0, byteReads));
                end = 0;
                Matcher matcher = pattern.matcher(buffer.toString());
                while (matcher.find()) {
                    notifyMatchedDataToWriteTask(matcher);
                    end = matcher.end();
                }
                size = buffer.length();
                buffer.delete(0, end);
                progressNotifier.accept(size - buffer.length());
            }
            if(buffer.length() > 0) {
                notifyNotMatchedDataToWriteTask(buffer);
                progressNotifier.accept(buffer.length());
            }
            countDownLatch.countDown();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected abstract String getLineMatcherRegex();
    protected abstract void notifyMatchedDataToWriteTask(Matcher matcher);
    protected abstract void notifyNotMatchedDataToWriteTask(StringBuilder buffer);
}
