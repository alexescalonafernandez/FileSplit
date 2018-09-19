package io.github.alexescalonafernandez.filesplit.behaviour;

import io.github.alexescalonafernandez.filesplit.api.SplitTaskConfiguration;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Created by alexander.escalona on 19/09/2018.
 */
public class NativeTerminalMode extends BaseMode{

    public NativeTerminalMode(SplitTaskConfiguration baseSplitTaskConfiguration) {
        super(baseSplitTaskConfiguration);
    }

    @Override
    public void initProgressViewer() {
        store.set(-1);
        System.out.println();
    }

    @Override
    protected void printProgressBar(String progressBar) {
        System.out.printf("\r");
        System.out.println(progressBar);
    }

    @Override
    public Consumer<String> getMessageNotifier() {
        return message -> System.out.println(message);
    }
}
