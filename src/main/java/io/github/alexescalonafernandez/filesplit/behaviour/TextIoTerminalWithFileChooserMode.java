package io.github.alexescalonafernandez.filesplit.behaviour;

import io.github.alexescalonafernandez.filesplit.api.SplitTaskConfigurationFromArgs;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.util.function.Predicate;

/**
 * Created by alexander.escalona on 17/09/2018.
 */
public class TextIoTerminalWithFileChooserMode extends AbstractTextIoTerminalMode {

    public TextIoTerminalWithFileChooserMode(SplitTaskConfigurationFromArgs splitTaskConfigurationFromArgs) {
        super(splitTaskConfigurationFromArgs);
    }

    String getFilePath(String questionText, int selectionMode, Predicate<File> fileChecker) {
        terminal.rawPrint(questionText);
        JFileChooser fileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        fileChooser.setDialogTitle(questionText);
        fileChooser.setFileSelectionMode(selectionMode);
        boolean flag = false;
        do {
            if(fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                if(flag = fileChecker.test(fileChooser.getSelectedFile())){
                    terminal.println(String.format(": %s", fileChooser.getSelectedFile().getAbsolutePath()));
                    return fileChooser.getSelectedFile().getAbsolutePath();
                }
            }
        } while (!flag);
        return null;
    }

    @Override
    public String getFilePath() {
        return getFilePath(
                "Choose the file to split",
                JFileChooser.FILES_ONLY,
                file -> file.exists() && file.isFile());
    }

    @Override
    public String getFolderPath() {
        return getFilePath(
                "Choose the folder for storing the split operation",
                JFileChooser.DIRECTORIES_ONLY,
                file -> file.exists() && file.isDirectory()
        );
    }
}
