package io.github.alexescalonafernandez.filesplit.behaviour;

import io.github.alexescalonafernandez.filesplit.api.OperationMode;
import io.github.alexescalonafernandez.filesplit.api.SplitTaskConfiguration;
import org.beryx.textio.TextIO;
import org.beryx.textio.TextIoFactory;
import org.beryx.textio.TextTerminal;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

/**
 * Created by alexander.escalona on 17/09/2018.
 */
public abstract class AbstractTextIoTerminalMode extends BaseMode {
    protected final TextIO textIO ;
    protected final TextTerminal terminal;
    private final AtomicInteger store;
    public AbstractTextIoTerminalMode(SplitTaskConfiguration baseSplitTaskConfiguration) {
        super(baseSplitTaskConfiguration);
        textIO = TextIoFactory.getTextIO();
        terminal = textIO.getTextTerminal();
        terminal.getProperties().put("pane.title", "File Split");
        this.store = new AtomicInteger(-1);
    }

    @Override
    public void run() {
        super.run();

        terminal.println();
        textIO.newCharInputReader().read("Press any key and press enter to exit");
        textIO.dispose();
    }

    @Override
    public Long getChunkSize() {
        List<String> sizeUnits = Arrays.asList("Bytes", "Kilo Bytes", "Mega Bytes");
        String selectedSize = textIO.newStringInputReader()
                .withNumberedPossibleValues(sizeUnits)
                .read("Select the size unit");
        long size = (long) Math.pow(1024, sizeUnits.indexOf(selectedSize));
        terminal.println();
        return size * textIO.newIntInputReader()
                .withMinVal(1)
                .withMaxVal(1024)
                .read(String.format("Select the estimated max size of chunks in %s (1 - 1024)", selectedSize));
    }

    @Override
    public Boolean appendFirstLine() {
        List<String> options = Arrays.asList("Yes", "No");
        String selectedOption = textIO.newStringInputReader()
                .withNumberedPossibleValues(options)
                .read("Select if each file will contains the first line as header");
        return "Yes".equals(selectedOption);
    }

    @Override
    public Integer getThreadNumber() {
        return baseSplitTaskConfiguration.getThreadNumber();
    }

    @Override
    public Integer getMaxThreadNumber(int limit) {
        Integer defaultValue = getThreadNumber();
        if(defaultValue != null) {
            if(BaseMode.canRunWithoutUserInteraction(baseSplitTaskConfiguration)) {
                return defaultValue;
            }
        }

        int maxThreadNumber = textIO.newIntInputReader()
                .withMinVal(1)
                .withMaxVal(limit)
                .read(String.format("Select the max of thread workers (1 - %d)", limit));
        return maxThreadNumber;
    }

    @Override
    public OperationMode getOperationMode() {
        List<String> options = Arrays.asList(OperationMode.values())
                .stream()
                .filter(operationMode -> !OperationMode.ANY.equals(operationMode))
                .map(operationMode -> operationMode.name())
                .collect(Collectors.toList());
        String selectedOption = textIO.newStringInputReader()
                .withNumberedPossibleValues(options)
                .read("Select the split operation mode");
        return OperationMode.valueOf(OperationMode.class, selectedOption);
    }

    @Override
    public Integer getMaxLines() {
        return textIO.newIntInputReader()
                .withMinVal(1)
                .withMaxVal(1000000)
                .read("Select the max lines of chunks (1 - 1000000)");
    }

    @Override
    public String getPrefix() {
        return textIO.newStringInputReader()
                .read("Insert the prefix of the name of the generated files");
    }

    @Override
    public String getRegex() {
        String regex = textIO.newStringInputReader()
                .read("Insert the regex to match the group in a line");
        try {
            Pattern.compile(regex);
            return regex;
        } catch (PatternSyntaxException ex) {
            terminal.println("Invalid regex");
            return getRegex();
        }
    }

    @Override
    public Integer getRegexGroup() {
        return textIO.newIntInputReader()
                .withMinVal(1)
                .withMaxVal(9)
                .read(String.format("Select the regex matched group, which will be used to group lines"));
    }

    @Override
    public void initProgressViewer() {
        store.set(-1);
        terminal.println();
        terminal.println();
        terminal.setBookmark("progressBar");
    }

    @Override
    public Consumer<Double> getProgressViewerNotifier() {
        return percent -> {
            int value = (int)Math.floor(percent);
            if(value > store.getAndSet(value)) {
                int progressCharsToShow = value / 2;
                terminal.resetToBookmark("progressBar");
                terminal.rawPrint(
                        String.format("Progress: |%s%s| %d%s",
                                repeat('\u2588', progressCharsToShow),
                                repeat('-', 50 - progressCharsToShow),
                                value, "%"
                        )
                );
            }
        };
    }

    protected String repeat(char c, int times) {
        StringBuilder sb = new StringBuilder();
        while (times-- > 0) {
            sb.append(c);
        }
        return sb.toString();
    }

    @Override
    public Consumer<String> getMessageNotifier() {
        return message -> terminal.println(message);
    }
}
