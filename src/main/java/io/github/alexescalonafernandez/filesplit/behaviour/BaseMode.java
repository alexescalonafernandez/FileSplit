package io.github.alexescalonafernandez.filesplit.behaviour;

import io.github.alexescalonafernandez.filesplit.api.SplitTaskConfiguration;
import io.github.alexescalonafernandez.filesplit.api.SplitTaskNotification;
import io.github.alexescalonafernandez.filesplit.task.SplitTaskExecutor;

import java.io.IOException;

/**
 * Created by alexander.escalona on 19/09/2018.
 */
public abstract class BaseMode implements Runnable, SplitTaskNotification {
    protected final SplitTaskConfiguration baseSplitTaskConfiguration;

    public BaseMode(SplitTaskConfiguration baseSplitTaskConfiguration) {
        this.baseSplitTaskConfiguration = baseSplitTaskConfiguration;
    }

    protected SplitTaskConfiguration getSplitTaskNotification() {
        return baseSplitTaskConfiguration;
    }

    @Override
    public void run() {
        SplitTaskExecutor splitTaskExecutor = null;
        try {
            splitTaskExecutor = new SplitTaskExecutor(getSplitTaskNotification(), this);
            splitTaskExecutor.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
