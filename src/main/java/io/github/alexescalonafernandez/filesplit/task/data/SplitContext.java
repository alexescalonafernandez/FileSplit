package io.github.alexescalonafernandez.filesplit.task.data;

import io.github.alexescalonafernandez.filesplit.api.SplitTaskConfiguration;

/**
 * Created by alexander.escalona on 11/09/2018.
 */
public class SplitContext {
    private long beginFilePointer, endFilePointer;
    private SplitTaskConfiguration configuration;
    private long timestamp;
    private String fileHeader;

    private SplitContext(long beginFilePointer, long endFilePointer, SplitTaskConfiguration configuration) {
        this.beginFilePointer = beginFilePointer;
        this.endFilePointer = endFilePointer;
        this.configuration = configuration;
    }

    public long getBeginFilePointer() {
        return beginFilePointer;
    }

    public long getEndFilePointer() {
        return endFilePointer;
    }

    public Long getTimestamp() {
        return timestamp;
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

    public static class Builder {
        private long beginFilePointer, endFilePointer;
        private SplitTaskConfiguration configuration;
        private long timestamp;
        private String fileHeader;

        public Builder(long beginFilePointer, long endFilePointer, SplitTaskConfiguration configuration) {
            this.beginFilePointer = beginFilePointer;
            this.endFilePointer = endFilePointer;
            this.configuration = configuration;
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

        public Builder setConfiguration(SplitTaskConfiguration configuration) {
            this.configuration = configuration;
            return this;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public Builder setTimestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public String getFileHeader() {
            return fileHeader;
        }

        public Builder fileHeader(String fileHeader) {
            this.fileHeader = fileHeader;
            return this;
        }

        public SplitContext build() {
            SplitContext context = new SplitContext(beginFilePointer, endFilePointer, configuration);
            context.timestamp = this.timestamp;
            context.fileHeader = this.fileHeader;
            return context;
        }
    }
}
