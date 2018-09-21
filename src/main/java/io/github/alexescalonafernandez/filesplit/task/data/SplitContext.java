package io.github.alexescalonafernandez.filesplit.task.data;

import io.github.alexescalonafernandez.filesplit.api.SplitTaskConfiguration;

/**
 * Created by alexander.escalona on 11/09/2018.
 */
public class SplitContext {
    private long beginFilePointer, endFilePointer;
    private SplitTaskConfiguration configuration;
    private long taskCount, taskIndex;
    private String fileHeader;

    private SplitContext(long beginFilePointer, long endFilePointer, SplitTaskConfiguration configuration,
                         String fileHeader) {
        this.beginFilePointer = beginFilePointer;
        this.endFilePointer = endFilePointer;
        this.configuration = configuration;
        this.fileHeader = fileHeader;
    }

    public long getBeginFilePointer() {
        return beginFilePointer;
    }

    public long getEndFilePointer() {
        return endFilePointer;
    }

    public boolean isAppendFirstLine() {
        return configuration.appendFirstLine();
    }

    public Integer getMaxLines() {
        return configuration.getMaxLines();
    }

    public Integer getRegexGroup() {
        return configuration.getRegexGroup();
    }

    public String getFilePath() {
        return configuration.getFilePath();
    }

    public String getFolderPath() {
        return configuration.getFolderPath();
    }

    public String getPrefix() {
        return configuration.getPrefix();
    }

    public String getRegex() {
        return configuration.getRegex();
    }

    public String getFileHeader() {
        return fileHeader;
    }

    public long getTaskCount() {
        return taskCount;
    }

    public void setTaskCount(long taskCount) {
        this.taskCount = taskCount;
    }

    public long getTaskIndex() {
        return taskIndex;
    }

    public void setTaskIndex(long taskIndex) {
        this.taskIndex = taskIndex;
    }

    public static class Builder {
        private long beginFilePointer, endFilePointer;
        private SplitTaskConfiguration configuration;
        private long taskCount, taskIndex;
        private String fileHeader;

        public Builder(long beginFilePointer, long endFilePointer, SplitTaskConfiguration configuration,
                       String fileHeader) {
            this.beginFilePointer = beginFilePointer;
            this.endFilePointer = endFilePointer;
            this.configuration = configuration;
            this.fileHeader = fileHeader;
        }

        public long getBeginFilePointer() {
            return beginFilePointer;
        }

        public Builder beginFilePointer(long beginFilePointer) {
            this.beginFilePointer = beginFilePointer;
            return this;
        }

        public long getEndFilePointer() {
            return endFilePointer;
        }

        public Builder endFilePointer(long endFilePointer) {
            this.endFilePointer = endFilePointer;
            return this;
        }

        public SplitTaskConfiguration getConfiguration() {
            return configuration;
        }

        public Builder configuration(SplitTaskConfiguration configuration) {
            this.configuration = configuration;
            return this;
        }

        public String getFileHeader() {
            return fileHeader;
        }

        public Builder fileHeader(String fileHeader) {
            this.fileHeader = fileHeader;
            return this;
        }

        public long getTaskCount() {
            return taskCount;
        }

        public Builder taskCount(long taskCount) {
            this.taskCount = taskCount;
            return this;
        }

        public long getTaskIndex() {
            return taskIndex;
        }

        public Builder taskIndex(long taskIndex) {
            this.taskIndex = taskIndex;
            return this;
        }

        public SplitContext build() {
            SplitContext context = new SplitContext(beginFilePointer, endFilePointer,
                    configuration, fileHeader);
            context.taskCount = taskCount;
            context.taskIndex = taskIndex;
            return context;
        }
    }
}
