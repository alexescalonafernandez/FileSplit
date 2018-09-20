package io.github.alexescalonafernandez.filesplit.behaviour;

import io.github.alexescalonafernandez.filesplit.api.SplitTaskConfigurationFromArgs;

import java.io.File;
import java.util.function.Predicate;

/**
 * Created by alexander.escalona on 17/09/2018.
 */
public class TextIoTerminalMode extends AbstractTextIoTerminalMode {

    public TextIoTerminalMode(SplitTaskConfigurationFromArgs splitTaskConfigurationFromArgs) {
        super(splitTaskConfigurationFromArgs);
    }

    /**
     * Get the path of a file, which meets with {@code fileChecker} predicate
     *
     * @param questionText  the prompt text from the inquirer
     * @param failText  the text showed if {@code fileChecker} predicate fails
     * @param fileChecker   test if the file path is valid according to the predicate
     * @return  a valid file path according to the {@code fileChecker} predicate
     */
    private String getFilePath(String questionText, String failText, Predicate<File> fileChecker) {
        String filePath = textIO.newStringInputReader().read(questionText);
        if(fileChecker.test(new File(filePath))) {
            return filePath;
        } else {
            terminal.println(failText);
            return getFilePath(questionText, failText, fileChecker);
        }
    }

    @Override
    public String getFilePath() {
        return getFilePath(
                "Write the path of file to split:",
                "The text is not a file path, try again",
                file -> file.exists() && file.isFile());
    }

    @Override
    public String getFolderPath() {
        return getFilePath(
                "Write the path of the folder for storing the split operation:",
                "The text is not a folder path, try again",
                file -> file.exists() && file.isDirectory());
    }
}
