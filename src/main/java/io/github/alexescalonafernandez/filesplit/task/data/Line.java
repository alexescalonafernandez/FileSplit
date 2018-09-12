package io.github.alexescalonafernandez.filesplit.task.data;

/**
 * Created by alexander.escalona on 11/09/2018.
 */
public class Line {
    private final String filePath, text;

    /**
     *
     * @param filePath  the file where the line will be appended
     * @param text  the text of the line
     */
    public Line(String filePath, String text) {
        this.filePath = filePath;
        this.text = text;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getText() {
        return text;
    }
}
