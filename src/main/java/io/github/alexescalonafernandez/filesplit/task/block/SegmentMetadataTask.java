package io.github.alexescalonafernandez.filesplit.task.block;

import io.github.alexescalonafernandez.filesplit.task.BaseTask;
import io.github.alexescalonafernandez.filesplit.task.data.Line;
import io.github.alexescalonafernandez.filesplit.task.data.SegmentMetadata;
import io.github.alexescalonafernandez.filesplit.task.data.SplitContext;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Created by alexander.escalona on 21/09/2018.
 */
public class SegmentMetadataTask extends BaseTask {
    private final SegmentMetadata metadata;
    private final Queue<LineMetadata> store;

    public SegmentMetadataTask(SplitContext splitContext, CountDownLatch countDownLatch,
                               Consumer<Line> writeNotifier, Consumer<Integer> progressNotifier,
                               AtomicBoolean stopPopulate, SegmentMetadata metadata) {
        super(splitContext, countDownLatch, writeNotifier, progressNotifier, stopPopulate);
        this.metadata = metadata;
        this.store = new LinkedList<>();
    }

    @Override
    protected void processLineWrapper(long beginLineOffset, long endLineOffset, String line) {
        this.processLine(beginLineOffset, endLineOffset, line);
    }

    @Override
    protected void processLine(long beginLineOffset, long endLineOffset, String line) {
        store.add(new LineMetadata(beginLineOffset, endLineOffset));
        if(store.size() == 100) {
            flush();
        }
    }

    @Override
    protected void dispose() {
        if(!store.isEmpty()) {
            flush();
        }
        super.dispose();
    }

    private void flush() {
        DataOutputStream outputStream = null;
        try {
            outputStream = new DataOutputStream(new FileOutputStream(metadata.getFilePath(), true));
            while (!store.isEmpty()) {
                LineMetadata lineMetadata = store.poll();
                outputStream.writeLong(lineMetadata.endOffset);
                metadata.incrementLineCount();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class LineMetadata {
        private final long beginOffset, endOffset;

        private LineMetadata(long beginOffset, long endOffset) {
            this.beginOffset = beginOffset;
            this.endOffset = endOffset;
        }
    }
}
