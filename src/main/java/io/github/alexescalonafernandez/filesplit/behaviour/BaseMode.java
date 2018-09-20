package io.github.alexescalonafernandez.filesplit.behaviour;

import io.github.alexescalonafernandez.filesplit.api.SplitTaskConfiguration;
import io.github.alexescalonafernandez.filesplit.api.SplitTaskConfigurationFromArgs;
import io.github.alexescalonafernandez.filesplit.api.SplitTaskNotification;
import io.github.alexescalonafernandez.filesplit.task.SplitTaskExecutor;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Created by alexander.escalona on 19/09/2018.
 */
public abstract class BaseMode implements Runnable, SplitTaskNotification {
    protected final SplitTaskConfigurationFromArgs splitTaskConfigurationFromArgs;
    protected final AtomicInteger store;

    public BaseMode(SplitTaskConfigurationFromArgs splitTaskConfigurationFromArgs) {
        this.splitTaskConfigurationFromArgs = splitTaskConfigurationFromArgs;
        this.store = new AtomicInteger(-1);
    }

    protected SplitTaskConfiguration getSplitTaskConfiguration() {
        return splitTaskConfigurationFromArgs;
    }

    @Override
    public void run() {
        SplitTaskExecutor splitTaskExecutor;
        try {
            splitTaskExecutor = new SplitTaskExecutor(getSplitTaskConfiguration(), this);
            splitTaskExecutor.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Consumer<Double> getProgressViewerNotifier() {
        return percent -> {
            int value = (int)Math.floor(percent);
            if(value > store.getAndSet(value)) {
                printProgressBar(generateProgressBar(value));
            }
        };
    }

    protected abstract void printProgressBar(String progressBar);

    protected String repeat(char c, int times)  {
        StringBuilder sb = new StringBuilder();
        while (times-- > 0) {
            sb.append(c);
        }
        return sb.toString();
    }

    protected String generateProgressBar(int percent) {
        int progressCharsToShow = percent / 2;
        return String.format("Progress: |%s%s| %d%s",
                repeat('\u2588', progressCharsToShow),
                repeat('-', 50 - progressCharsToShow),
                percent, "%"
        );
    }
}
