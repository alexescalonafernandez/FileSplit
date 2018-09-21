package io.github.alexescalonafernandez.filesplit.task.split;

import io.github.alexescalonafernandez.filesplit.task.BaseTask;
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
public class BlockSplitTask extends BaseTask {
    private boolean isFirstLine;
    private final String outputFilePath;
    public BlockSplitTask(SplitContext splitContext, CountDownLatch countDownLatch,
                          Consumer<Line> writeNotifier, Consumer<Integer> progressNotifier,
                          AtomicBoolean stopPopulate) {
        super(splitContext, countDownLatch, writeNotifier, progressNotifier, stopPopulate);
        this.isFirstLine = true;

        String index = String.valueOf(splitContext.getTaskIndex());
        int maxCharacters = String.valueOf(splitContext.getTaskCount()).length();
        while (index.length() < maxCharacters) {
            index = "0" + index;
        }

        File fileToSplit = new File(splitContext.getFilePath());
        outputFilePath = new File(splitContext.getFolderPath(),
                String.format("%s-%s.%s",
                        FilenameUtils.getBaseName(fileToSplit.getAbsolutePath()),
                        index,
                        FilenameUtils.getExtension(fileToSplit.getAbsolutePath())
                )
        ).getAbsolutePath();
    }

    @Override
    protected void processLine(long beginLineOffset, long endLineOffset, String line) {
        if(isFirstLine && splitContext.isAppendFirstLine() && splitContext.getFileHeader() != null) {
            if(beginLineOffset != 0) {
                writeNotifier.accept(new Line(outputFilePath, splitContext.getFileHeader()));
            }
        }
        writeNotifier.accept(new Line(outputFilePath, line));
        isFirstLine = false;
    }
}
