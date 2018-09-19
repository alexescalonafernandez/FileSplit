package io.github.alexescalonafernandez.filesplit.api;

import java.util.function.Consumer;

/**
 * Created by alexander.escalona on 11/09/2018.
 */
public interface SplitTaskNotification {
    default String repeat(char c, int times)  {
        StringBuilder sb = new StringBuilder();
        while (times-- > 0) {
            sb.append(c);
        }
        return sb.toString();
    }

    default String generateProgressBar(int percent) {
        int progressCharsToShow = percent / 2;
        return String.format("Progress: |%s%s| %d%s",
                repeat('\u2588', progressCharsToShow),
                repeat('-', 50 - progressCharsToShow),
                percent, "%"
        );
    }


    void initProgressViewer();
    Consumer<Double> getProgressViewerNotifier();
    Consumer<String> getMessageNotifier();
}
