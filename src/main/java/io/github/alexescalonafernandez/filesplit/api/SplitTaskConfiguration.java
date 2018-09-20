package io.github.alexescalonafernandez.filesplit.api;

/**
 * Created by alexander.escalona on 11/09/2018.
 */
public interface SplitTaskConfiguration {

    /**
     *
     * @return the split operation mode
     */
    @Required(priority = 1, argumentName = "--mode")
    OperationMode getOperationMode();

    /**
     *
     * @return the path of the file to split
     */
    @Required(priority = 2, argumentName = "--filePath")
    String getFilePath();

    /**
     *
     * @return the chunk size that will read as average each split task of the file to split
     */
    @Required(priority = 3, argumentName = "--chunkSize")
    Long getChunkSize();

    /**
     * If returns {@code True} the first line of the file to split will be inserted as the first line
     * of each new generated file. This is useful when a csv is splitted for example.
     *
     * @return
     */
    @Required(priority = 4, argumentName = "--appendFirstLine")
    Boolean appendFirstLine();

    /**
     *
     * @return the folder where will be stored the generated files
     */
    @Required(priority = 5, argumentName = "--folderPath")
    String getFolderPath();

    /**
     *
     * @return the desired thread workers that will split the file
     */
    @Required(priority = 6, argumentName = "--threadNumber")
    Integer getThreadNumber();
    Integer getMaxThreadNumber(int limit);

    /**
     * Used in {@code OperationMode.BLOCK}
     * @return the max of lines that will have at most each generated file
     */
    @Required(value = OperationMode.BLOCK, priority = 7, argumentName = "--maxLines")
    Integer getMaxLines();

    /**
     * Used in {@code OperationMode.GROUP}
     * @return the prefix that will have each generated file in its name
     */
    @Required(value = OperationMode.GROUP, priority = 7, argumentName = "--prefix")
    String getPrefix();

    /**
     * Used in {@code OperationMode.GROUP}
     * @return the regex that will be used to match a line in a multiline mode regex.
     */
    @Required(value = OperationMode.GROUP, priority = 8, argumentName = "--regex")
    String getRegex();

    /**
     * Used in {@code OperationMode.GROUP}
     * @return the regex group matched by {@code getRegex} that will be used create a new file
     * with the group match as part of the file name, and the matched line will be appended to
     * this file.
     */
    @Required(value = OperationMode.GROUP, priority = 9, argumentName = "--regexGroup")
    Integer getRegexGroup();
}

