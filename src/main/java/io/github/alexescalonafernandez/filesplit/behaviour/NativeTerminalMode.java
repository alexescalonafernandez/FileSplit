package io.github.alexescalonafernandez.filesplit.behaviour;

import io.github.alexescalonafernandez.filesplit.api.SplitTaskConfigurationFromArgs;

import java.util.function.Consumer;

/**
 * Created by alexander.escalona on 19/09/2018.
 */
public class NativeTerminalMode extends BaseMode{

    public NativeTerminalMode(SplitTaskConfigurationFromArgs splitTaskConfigurationFromArgs) {
        super(splitTaskConfigurationFromArgs);
    }

    @Override
    public void initProgressViewer() {
        store.set(-1);
        System.out.println();
    }

    @Override
    protected void printProgressBar(String progressBar) {
        System.out.printf("\r");
        System.out.print(progressBar);
    }

    @Override
    public Consumer<String> getMessageNotifier() {
        return message -> System.out.println(message);
    }
}
