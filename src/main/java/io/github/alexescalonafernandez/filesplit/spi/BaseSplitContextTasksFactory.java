package io.github.alexescalonafernandez.filesplit.spi;

import io.github.alexescalonafernandez.filesplit.api.SplitTaskConfiguration;
import io.github.alexescalonafernandez.filesplit.api.factory.SplitContextTasksFactory;
import io.github.alexescalonafernandez.filesplit.task.data.SplitContext;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by alexander.escalona on 21/09/2018.
 */
public class BaseSplitContextTasksFactory implements SplitContextTasksFactory {
    @Override
    public List<SplitContext> createSplitTasksContexts(SplitTaskConfiguration taskConfiguration) {
        RandomAccessFile raf = null;
        List<SplitContext> tasks = new ArrayList<>();
        try {
            raf = new RandomAccessFile(new File(taskConfiguration.getFilePath()), "r");
            long begin, end = -1, chunkSize = taskConfiguration.getChunkSize();
            int byteReads;
            byte[] chunk = new byte[1024];
            Pattern pattern = Pattern.compile("[^\\n]*\\n", Pattern.MULTILINE);
            boolean flag = true, find;
            String fileHeader = raf.readLine() + System.lineSeparator();
            while(flag) {
                begin = end + 1;
                end = begin + chunkSize;
                if(end >= raf.length())
                    end = raf.length() - 1;
                raf.seek(end);
                find = false;
                do {
                    byteReads = raf.read(chunk);
                    Matcher matcher = pattern.matcher(new String(chunk, 0, byteReads));
                    if(matcher.find()) {
                        find = true;
                        end += matcher.end() - 1;
                    } else {
                        end += byteReads - 1;
                    }
                } while((raf.getFilePointer() < raf.length()) && !find);

                flag = raf.getFilePointer() < raf.length();
                tasks.add(new SplitContext.Builder(begin, end, taskConfiguration, fileHeader).build());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                raf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return tasks;
    }
}
