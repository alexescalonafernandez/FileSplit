package io.github.alexescalonafernandez.filesplit;

import io.github.alexescalonafernandez.filesplit.api.OperationMode;
import io.github.alexescalonafernandez.filesplit.api.SplitTaskConfiguration;
import io.github.alexescalonafernandez.filesplit.behaviour.BaseInteractiveMode;
import io.github.alexescalonafernandez.filesplit.behaviour.TextIoTerminalMode;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

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
            SplitTaskConfiguration baseTaskConfiguration = buildTaskConfigurationFromArgs(args);
            BaseInteractiveMode terminal = null;
            if(BaseInteractiveMode.canRunWithoutUserInteraction(baseTaskConfiguration)) {
                terminal = new TextIoTerminalMode(baseTaskConfiguration);
            } else {
                terminal = new TextIoTerminalMode(baseTaskConfiguration);
            }

            terminal.run();
        }

    }

    private static HashMap<String, String> getArguments(String[] args) {
        HashMap<String, String> arguments = new HashMap<>();
        for(int i = 0; i < args.length - 1; i+=2)
            arguments.put(args[i], args[i + 1]);
        return arguments;
    }

    private static SplitTaskConfiguration buildTaskConfigurationFromArgs(String[] args) {
        final HashMap<String, String> arguments = getArguments(args);
        return new SplitTaskConfiguration() {
            @Override
            public String getFilePath() {
                return Optional.ofNullable(arguments.get("--filePath"))
                        .filter(filePath -> {
                            File file = new File(filePath);
                            return file.exists() && file.isFile();
                        })
                        .orElse(null);
            }

            @Override
            public Long getChunkSize() {
                long oneMB = 1024 * 1024;
                return Optional.ofNullable(
                        Optional.ofNullable(arguments.get("--chunkSize")).orElse(String.valueOf(oneMB)))
                        .filter(s -> {
                            try {
                                long value = Long.valueOf(s);
                                long oneGB = (long)Math.pow(1024, 3);
                                return value > 0 && value < oneGB;
                            } catch (NumberFormatException e) {
                                return false;
                            }
                        }).map(Long::valueOf)
                        .orElse(null);
            }

            @Override
            public Boolean appendFirstLine() {
                return Boolean.valueOf(arguments.get("--appendFirstLine"));
            }

            @Override
            public String getFolderPath() {
                return Optional.ofNullable(arguments.get("--folderPath"))
                        .filter(filePath -> {
                            File file = new File(filePath);
                            if(file.exists() && file.isDirectory()){
                                return true;
                            } else {
                                try {
                                    FileUtils.forceMkdir(file);
                                    return true;
                                } catch (IOException e) {
                                    return false;
                                }
                            }
                        })
                        .orElse(null);
            }

            @Override
            public Integer getThreadNumber() {
                return Optional.ofNullable(
                        Optional.ofNullable(arguments.get("--threadNumber")).orElse("8"))
                        .filter(s -> {
                            try {
                                int value = Integer.valueOf(s);
                                return value > 0;
                            } catch (NumberFormatException e) {
                                return false;
                            }
                        })
                        .map(Integer::valueOf)
                        .orElse(null);
            }

            @Override
            public Integer getMaxThreadNumber(int limit) {
                return getThreadNumber();
            }

            @Override
            public OperationMode getOperationMode() {
                return Optional.ofNullable(arguments.get("--mode"))
                        .map(value -> {
                            try {
                                return OperationMode.valueOf(value.toUpperCase());
                            } catch (IllegalArgumentException e) {
                                return null;
                            }
                        })
                        .filter(mode -> !OperationMode.ANY.equals(mode))
                        .orElse(null);
            }

            @Override
            public Integer getMaxLines() {
                return Optional.ofNullable(
                        Optional.ofNullable(arguments.get("--maxLines")).orElse("1000000"))
                        .filter(s -> {
                            try {
                                int value = Integer.valueOf(s);
                                return value > 0;
                            } catch (NumberFormatException e) {
                                return false;
                            }
                        })
                        .map(Integer::valueOf)
                        .orElse(null);
            }

            @Override
            public String getPrefix() {
                return Optional.ofNullable(arguments.get("--prefix"))
                        .filter(prefix -> prefix.trim().length() > 0)
                        .orElse(null);
            }

            @Override
            public String getRegex() {
                return Optional.ofNullable(arguments.get("--regex"))
                        .filter(regex -> {
                            try {
                                Pattern.compile(regex);
                                return true;
                            } catch (PatternSyntaxException ex) {
                                return false;
                            }
                        })
                        .orElse(null);
            }

            @Override
            public Integer getRegexGroup() {
                return Optional.ofNullable(arguments.get("--regexGroup"))
                        .filter(s -> {
                            try {
                                int value = Integer.valueOf(s);
                                return value > 0 && value < 10;
                            } catch (NumberFormatException e) {
                                return false;
                            }
                        })
                        .map(Integer::valueOf)
                        .orElse(null);
            }
        };
    }
}
