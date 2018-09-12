package io.github.alexescalonafernandez.filesplit.task.data;

import io.github.alexescalonafernandez.filesplit.api.SplitTaskConfiguration;

/**
 * Created by alexander.escalona on 11/09/2018.
 */
public class SplitContext {
    private long beginFilePointer, endFilePointer;
    private SplitTaskConfiguration configuration;
    private long timestamp;
    
    private SplitContext(long beginFilePointer, long endFilePointer, long timestamp, SplitTaskConfiguration configuration) {
        this.beginFilePointer = beginFilePointer;
        this.endFilePointer = endFilePointer;
        this.timestamp = timestamp;
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
}
