package io.github.alexescalonafernandez.filesplit;

import io.github.alexescalonafernandez.filesplit.api.SplitTaskConfigurationFromArgs;
import io.github.alexescalonafernandez.filesplit.api.SplitTaskConfigurationUtils;
import io.github.alexescalonafernandez.filesplit.behaviour.BaseMode;
import io.github.alexescalonafernandez.filesplit.behaviour.NativeTerminalMode;
import io.github.alexescalonafernandez.filesplit.behaviour.TextIoTerminalMode;
import io.github.alexescalonafernandez.filesplit.behaviour.TextIoTerminalWithFileChooserMode;
import org.beryx.textio.swing.SwingTextTerminal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;

/**
 * Created by alexander.escalona on 11/09/2018.
 */
public class Main {

    /**
     * Split a file into several files, where each of them has a maximum number of lines.
     * To do this, the path of the file to be splitted is requested, also the restrictions that
     * the new files must meet in size and in maximum number of lines. It also requests the path
     * of the folder where the new files will be stored, as well as the number of parallel execution threads
     * that you want to run to accelerate the process.
     *
     * @param args
     */
    public static void main(String[] args) {
        if(args.length == 1 && "--help".equals(args[0])) {
            try {
                InputStream resource = Main.class.getClassLoader().getResourceAsStream("help.txt");
                BufferedReader br = new BufferedReader(new InputStreamReader(resource));
                String line;
                while ((line = br.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            SplitTaskConfigurationFromArgs splitTaskConfigurationFromArgs = new SplitTaskConfigurationFromArgs(args);
            BaseMode terminal = null;
            if(SplitTaskConfigurationUtils.canRunWithoutUserInteraction(splitTaskConfigurationFromArgs)) {
                terminal = new NativeTerminalMode(splitTaskConfigurationFromArgs);
            } else {
                System.setProperty("org.beryx.textio.TextTerminal", SwingTextTerminal.class.getName());
                int mode = -1;
                System.out.println("For running without user interaction run with --help argument for more details");
                System.out.println();
                System.out.println();
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                do {
                    System.out.println();
                    System.out.println("Select the interaction mode");
                    System.out.println("1. Single Terminal Mode");
                    System.out.println("2. Terminal with File Chooser Mode");
                    System.out.println("3. Exit program");
                    try {
                        mode = Optional.ofNullable(br.readLine())
                                .filter(s -> s.matches("^1|2|3$"))
                                .map(s -> Integer.valueOf(s))
                                .orElse(-1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } while (mode == -1);
                if(mode == 1) {
                    terminal = new TextIoTerminalMode(splitTaskConfigurationFromArgs);
                } else if (mode == 2){
                    terminal = new TextIoTerminalWithFileChooserMode(splitTaskConfigurationFromArgs);
                }
            }
            Optional.ofNullable(terminal).ifPresent(baseMode -> baseMode.run());
        }

    }
}
