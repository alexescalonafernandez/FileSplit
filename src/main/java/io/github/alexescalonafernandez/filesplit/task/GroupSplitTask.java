package io.github.alexescalonafernandez.filesplit.task;

import io.github.alexescalonafernandez.filesplit.task.data.Line;
import io.github.alexescalonafernandez.filesplit.task.data.SplitContext;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * Created by alexander.escalona on 12/09/2018.
 */
public class GroupSplitTask extends SplitTask {
    private HashSet<String> groups;
    public GroupSplitTask(SplitContext splitContext, CountDownLatch countDownLatch,
                          Consumer<Line> writeNotifier, Consumer<Integer> progressNotifier, AtomicBoolean stopPopulate) {
        super(splitContext, countDownLatch, writeNotifier, progressNotifier, stopPopulate);
        this.groups = new HashSet<>();
    }

    @Override
    protected void processLine(long lineOffset, String line) {
        boolean flag = splitContext.isAppendFirstLine() && splitContext.getFileHeader() != null;
        if(flag && lineOffset == 0)
            return;
        Pattern pattern = Pattern.compile(splitContext.getRegex());
        String name = Optional.of(pattern.matcher(line))
                .filter(matcher -> matcher.find())
                .filter(matcher -> Optional.ofNullable(splitContext.getRegexGroup()).isPresent())
                .filter(matcher -> splitContext.getRegexGroup() <= matcher.groupCount())
                .map(matcher -> matcher.group(splitContext.getRegexGroup()))
                .orElse(null);
        String path;
        if(name == null) {
            File file = new File(splitContext.getFolderPath(), "no-match");
            try {
                if(!file.exists()) {
                    tryCreateFolder(file);
                }
            } catch (IOException e) {
                e.printStackTrace();
                file = new File(splitContext.getFolderPath());
            } finally {
                File fileToSplit = new File(splitContext.getFilePath());
                path = new File(file,
                        String.format("no-match.%s",
                                FilenameUtils.getExtension(fileToSplit.getAbsolutePath())
                        )
                ).getAbsolutePath();
            }
        } else {
            path = buildSplitFilePath(name);
        }
        if(flag && groups.add(name)) {
            writeNotifier.accept(new Line(path, splitContext.getFileHeader()));
        }
        writeNotifier.accept(new Line(path, line));
    }

    private static synchronized void tryCreateFolder(File file) throws IOException {
        if(!file.exists()) {
            FileUtils.forceMkdir(file);
        }
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
