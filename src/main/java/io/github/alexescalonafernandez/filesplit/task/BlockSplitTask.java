package io.github.alexescalonafernandez.filesplit.task;

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

    public BlockSplitTask(SplitContext splitContext, CountDownLatch countDownLatch,
                          Consumer<Line> writeNotifier, Consumer<Integer> progressNotifier) {
        super(splitContext, countDownLatch, writeNotifier, progressNotifier);
        this.lines = 0;
        this.inc = 1;
    }

    @Override
    protected String getLineMatcherRegex() {
        return "[^\\n]*\\n";
    }

    @Override
    protected void notifyMatchedDataToWriteTask(Matcher matcher) {
        notifyToWriter(matcher.group(0));
    }

    @Override
    protected void notifyNotMatchedDataToWriteTask(StringBuilder buffer) {
        notifyToWriter(buffer.toString());
    }

    private void notifyToWriter(String data) {
        lines++;
        if(lines == 1 && splitContext.isAppendFirstLine() && splitContext.getFileHeader() != null) {
            writeNotifier.accept(new Line(buildSplitFilePath(), splitContext.getFileHeader()));
        } else if(splitContext.getMaxLines() != null && lines > splitContext.getMaxLines()) {
            lines = 0;
            inc++;
        }
        writeNotifier.accept(new Line(buildSplitFilePath(), data));
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
