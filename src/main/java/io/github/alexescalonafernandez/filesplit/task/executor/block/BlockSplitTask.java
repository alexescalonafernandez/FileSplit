package io.github.alexescalonafernandez.filesplit.task.executor.block;

import io.github.alexescalonafernandez.filesplit.task.SplitTask;
import io.github.alexescalonafernandez.filesplit.task.data.Line;
import io.github.alexescalonafernandez.filesplit.task.data.SplitContext;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;
import java.util.regex.Matcher;

/**
 * Created by alexander.escalona on 11/09/2018.
 */
public class BlockSplitTask extends SplitTask {
    private int lines, inc;
    private Consumer<Line> writeNotifier;
    private Consumer<Integer> progressNotifier;
    public BlockSplitTask(SplitContext splitContext, CountDownLatch countDownLatch,
                          Consumer<Line> writeNotifier, Consumer<Integer> progressNotifier) {
        super(splitContext, countDownLatch);
        this.lines = 0;
        this.inc = 1;
        this.writeNotifier = writeNotifier;
        this.progressNotifier = progressNotifier;
    }

    @Override
    protected String getLineMatcherRegex() {
        return "[^\\n]*\\n";
    }

    @Override
    protected void notifyToWriteTask(Matcher matcher) {
        lines++;
        if(lines == 1 && splitContext.isAppendFirstLine() && splitContext.getFileHeader() != null) {
            writeNotifier.accept(new Line(buildSplitFilePath(), splitContext.getFileHeader()));
        } else if(splitContext.getMaxLines() != null && lines > splitContext.getMaxLines()) {
            lines = 0;
            inc++;
        }
        writeNotifier.accept(new Line(buildSplitFilePath(), matcher.group(0)));
    }

    @Override
    protected void notifyToProgressTask(int byteReads, int bufferSizeBeforeMatch, int bufferSizeAfterMatch) {
        progressNotifier.accept(bufferSizeBeforeMatch - bufferSizeAfterMatch);
    }

    @Override
    protected void processNotMatchData(StringBuilder buffer) {
        inc++;
        writeNotifier.accept(new Line(buildSplitFilePath(), buffer.toString()));
        progressNotifier.accept(buffer.length());
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
