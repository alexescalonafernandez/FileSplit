package io.github.alexescalonafernandez.filesplit.task.executor.group;

import io.github.alexescalonafernandez.filesplit.task.SplitTask;
import io.github.alexescalonafernandez.filesplit.task.data.Line;
import io.github.alexescalonafernandez.filesplit.task.data.SplitContext;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;
import java.util.regex.Matcher;

/**
 * Created by alexander.escalona on 12/09/2018.
 */
public class GroupSplitTask extends SplitTask {
    private boolean firstLine;
    public GroupSplitTask(SplitContext splitContext, CountDownLatch countDownLatch,
                          Consumer<Line> writeNotifier, Consumer<Integer> progressNotifier) {
        super(splitContext, countDownLatch, writeNotifier, progressNotifier);
        this.firstLine = true;
    }

    @Override
    protected String getLineMatcherRegex() {
        return splitContext.getRegex();
    }

    @Override
    protected void notifyMatchedDataToWriteTask(Matcher matcher) {
        Optional.ofNullable(splitContext.getRegexGroup()).ifPresent(value -> {
            if(value <  matcher.groupCount()) {
                String name = matcher.group(splitContext.getRegexGroup());
                if(this.firstLine && splitContext.isAppendFirstLine() && splitContext.getFileHeader() != null) {
                    writeNotifier.accept(new Line(buildSplitFilePath(name), splitContext.getFileHeader()));
                }
                writeNotifier.accept(new Line(buildSplitFilePath(name), matcher.group(0)));
                this.firstLine = false;
            }
        });
    }

    @Override
    protected void notifyNotMatchedDataToWriteTask(StringBuilder buffer) {
        String name = "no-match";
        if(splitContext.isAppendFirstLine() && splitContext.getFileHeader() != null) {
            writeNotifier.accept(new Line(buildSplitFilePath(name), splitContext.getFileHeader()));
        }
        writeNotifier.accept(new Line(buildSplitFilePath(name), buffer.toString()));
        progressNotifier.accept(buffer.length());
    }

    private String buildSplitFilePath(String name) {
        File fileToSplit = new File(splitContext.getFilePath());
        return new File(splitContext.getFolderPath(),
                String.format("%s%s.%s",
                        Optional.ofNullable(splitContext.getPrefix()).orElse(""),
                        name,
                        FilenameUtils.getExtension(fileToSplit.getAbsolutePath())
                )
        ).getAbsolutePath();
    }
}
