package io.github.alexescalonafernandez.filesplit.task.data;

/**
 * Created by alexander.escalona on 21/09/2018.
 */
public class SegmentMetadata {
    private final String filePath;
    private long lineCount;

    public SegmentMetadata(String filePath) {
        this.filePath = filePath;
        this.lineCount = 0;
    }

    public String getFilePath() {
        return filePath;
    }

    public long getLineCount() {
        return lineCount;
    }

    public void incrementLineCount() {
        this.lineCount++;
    }
}
