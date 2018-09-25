# File Splitter

File Splitter is a Java application that can be used to split a file in two modes:
* The **BLOCK** mode allow split a file in files of `M` bytes where the maximum quantity of lines is `N`. Both, `M` and `N` are specified by the user. The maximum quantity of lines has more priority than the size in bytes. Therefore the files will have `M` bytes, if the quantity of read lines in `M` is less or equal than `N`. 
* The **GROUP** mode allow use a **regular expression** for search a **matched group** in each line of the file. Each **matched group** represents a file, and each **matched line** in a group will be stored in that **group**. If a line does not **match** in the **regular expression** will be stored in the '**no-match**' file. Both the **regular expression** and the **group to match** are specified by the user.

In both modes the user can allow to append the **first line** of the file in each generated file, which can be useful if the file is a CSV for example.
 
## File Splitter Goals
* Splitting a file in files of `M` bytes if in `M` bytes there are not over `N` lines.
* Splitting a file through a regular expression, which try to match a line in a group, and each matched group represents the file where the matched lines will be stored.
* Storing the generated files in a folder specified by the user.
* Appending the first line of the file in each generated file, at the request of the user.
* Overwriting the generated files if necessary.
* Reading the file with multiple working threads for speed up the split process. Each worker will try read the M bytes specified by the user. The maximum quantity of workers running concurrently is also specified by the user.
* Minimize the memory consumption among the read workers and the write worker through synchronization.
* Show a progress bar with the percentage of completion of the task.
* Allow to split a file without user interaction through arguments where some arguments have default values.

## Usage
The application can be used with or without user interaction. If the application is runned without arguments all necessary information to split a file will be asked to the user in the console.

For interactive mode run:

`java -jar FileSplitter.jar`

For non interactive mode you can use the arguments, for more information about arguments you can run the application in the help mode:

`java -jar FileSplitter.jar --help`

### Arguments
| ARGUMENT | TYPE | MODE | DEFAULT | DESCRIPTION |
| --- | --- | --- | ---- | ---- |
| `--mode` | **BLOCK**\|**GROUP** | **ANY** | | The splitting mode that the application uses. |
| `--filePath` | string | **ANY** | | The path of the file which will be splitted. |
| `--chunkSize` | integer | **ANY** | 1 **Mb** |The estimated size in bytes that each working thread will processing of the file to split. |
| `--appendFirstLine` | *true*\|*false* | **ANY** | *false* |If its value is true, the first line of the file will be inserted as the first line of all generated files, which can be useful when processing CSV files. |
| `--folderPath` | string | **ANY** | | The path of the folder where the generated files will be stored. |
| `--threadNumber` | integer | **ANY** | 8 |The maximum number of working threads which split the file concurrently. |
| `--maxLines` | integer | **BLOCK** | 100000 |The maximum number of lines that the generated files can have. |
| `--prefix` | string | **GROUP** | | The prefix that will have the name of each generated file. |
| `--regex` | regex | **GROUP** | | A string representing a regular expression, which will be applied to each line processed by a working thread. It is expected that the regular expression has a group that represents the value by which the lines are grouped, which will be used as part of the name of the generated file. |
| `--regexGroup` | integer | **GROUP** | | A number between 1 and 9 that represents the group specified in the --regex argument, which will be used to extract a segment of the line representing the value to group the lines. |

#### Example
Example of arguments where a CSV file is splitted using the BLOCK mode:

`java -jar FileSplitter.jar --mode BLOCK --filePath D:\file.csv --chunkSize 1048576 --appendFirstLine true --folderPath D:\split 
           --threadNumber 8 --maxLines 10000`

