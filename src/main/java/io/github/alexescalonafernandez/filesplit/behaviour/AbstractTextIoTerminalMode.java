package io.github.alexescalonafernandez.filesplit.behaviour;

import io.github.alexescalonafernandez.filesplit.api.OperationMode;
import io.github.alexescalonafernandez.filesplit.api.SplitTaskConfigurationFromArgs;
import org.beryx.textio.TextIO;
import org.beryx.textio.TextIoFactory;
import org.beryx.textio.TextTerminal;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import static io.github.alexescalonafernandez.filesplit.api.utils.IntrospectionUtil.getCurrentEnclosingMethodFrom;

/**
 * Created by alexander.escalona on 17/09/2018.
 */
public abstract class AbstractTextIoTerminalMode extends BaseInteractiveMode {
    protected final TextIO textIO ;
    protected final TextTerminal terminal;
    public AbstractTextIoTerminalMode(SplitTaskConfigurationFromArgs splitTaskConfigurationFromArgs) {
        super(splitTaskConfigurationFromArgs);
        textIO = TextIoFactory.getTextIO();
        terminal = textIO.getTextTerminal();
        terminal.getProperties().put("pane.title", "File Split");
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
        return splitTaskConfigurationFromArgs.getThreadNumber();
    }

    @Override
    public Integer getMaxThreadNumber(int limit) {
        Method currentMethod = getCurrentEnclosingMethodFrom(new Object(){});
        if(!splitTaskConfigurationFromArgs.isArgumentDataDefault(currentMethod)) {
            return splitTaskConfigurationFromArgs.getMaxThreadNumber(limit);
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
    public Consumer<String> getMessageNotifier() {
        return message -> terminal.println(message);
    }

    @Override
    protected void printProgressBar(String progressBar) {
        terminal.resetToBookmark("progressBar");
        terminal.rawPrint(progressBar);
    }
}
