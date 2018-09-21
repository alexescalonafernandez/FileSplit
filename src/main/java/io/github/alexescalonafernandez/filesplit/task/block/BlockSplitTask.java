package io.github.alexescalonafernandez.filesplit.task.block;

import io.github.alexescalonafernandez.filesplit.task.SplitTask;
import io.github.alexescalonafernandez.filesplit.task.data.Line;
import io.github.alexescalonafernandez.filesplit.task.data.SplitContext;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Created by alexander.escalona on 11/09/2018.
 */
public class BlockSplitTask extends SplitTask {
    private int lines, inc;

    public BlockSplitTask(SplitContext splitContext, CountDownLatch countDownLatch,
                          Consumer<Line> writeNotifier, Consumer<Integer> progressNotifier, AtomicBoolean stopPopulate) {
        super(splitContext, countDownLatch, writeNotifier, progressNotifier, stopPopulate);
        this.lines = 0;
        this.inc = 1;
    }

    @Override
    protected void processLine(long beginLineOffset, long endLineOffset, String line) {
        lines++;
        if(lines == 1 && splitContext.isAppendFirstLine() && splitContext.getFileHeader() != null) {
            if(beginLineOffset != 0) {
                writeNotifier.accept(new Line(buildSplitFilePath(), splitContext.getFileHeader()));
            }
        } else if(splitContext.getMaxLines() != null && lines > splitContext.getMaxLines()) {
            lines = 0;
            inc++;
        }
        writeNotifier.accept(new Line(buildSplitFilePath(), line));
    }

    private String buildSplitFilePath() {
        File fileToSplit = new File(splitContext.getFilePath());
        return new File(splitContext.getFolderPath(),
                String.format("%s-%d-%d.%s",
                        FilenameUtils.getBaseName(fileToSplit.getAbsolutePath()),
                        splitContext.getTimestamp(),
                        inc,
                        FilenameUtils.getExtension(fileToSplit.getAbsolutePath())
                )
        ).getAbsolutePath();
    }
}
