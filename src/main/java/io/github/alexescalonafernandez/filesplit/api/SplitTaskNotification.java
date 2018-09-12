package io.github.alexescalonafernandez.filesplit.api;

import java.util.function.Consumer;

/**
 * Created by alexander.escalona on 11/09/2018.
 */
public interface SplitTaskNotification {
    void initProgressViewer();
    Consumer<Double> getProgressViewerNotifier();
    Consumer<String> getMessageNotifier();
}
