package io.github.alexescalonafernandez.filesplit.spi.block;

import io.github.alexescalonafernandez.filesplit.api.SplitTaskConfiguration;
import io.github.alexescalonafernandez.filesplit.spi.BaseSplitContextTasksFactory;
import io.github.alexescalonafernandez.filesplit.task.block.SegmentMetadataTask;
import io.github.alexescalonafernandez.filesplit.task.data.SegmentMetadata;
import io.github.alexescalonafernandez.filesplit.task.data.SplitContext;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Created by alexander.escalona on 21/09/2018.
 */
public class BlockSplitContextTasksFactory extends BaseSplitContextTasksFactory {

    @Override
    public List<SplitContext> createSplitTasksContexts(SplitTaskConfiguration taskConfiguration) {
        List<SplitContext> tasks = super.createSplitTasksContexts(taskConfiguration);
        if(tasks.size() == 0) {
            return tasks;
        }

        final ExecutorService executorService = Executors.newFixedThreadPool(tasks.size());
        final CountDownLatch countDownLatch = new CountDownLatch(tasks.size());
        AtomicBoolean stopPopulate = new AtomicBoolean(false);
        try{
            //calculate how many lines has each segment concurrently
            List<SegmentMetadata> metadataList = tasks.stream()
                    .map(splitContext -> {
                        File temporal = null;
                        do {
                            try {
                                temporal = File.createTempFile(UUID.randomUUID().toString(), ".tmp");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }while(temporal == null);
                        temporal.deleteOnExit();

                        SegmentMetadata segmentMetadata = new SegmentMetadata(temporal.getAbsolutePath());
                        executorService.execute(new SegmentMetadataTask(
                                splitContext, countDownLatch,
                                line -> {}, integer -> {}, stopPopulate,
                                segmentMetadata
                        ));
                        return segmentMetadata;
                    })
                    .collect(Collectors.toList());
            countDownLatch.await();

            // calculating offsets
            long maxLines = taskConfiguration.getMaxLines(), pendingLines = 0;
            long offset = 0;
            final List<Long[]> newOffsets = new ArrayList<>();
            Iterator<SegmentMetadata> it = metadataList.iterator();
            while (it.hasNext()) {
                SegmentMetadata segmentMetadata = it.next();
                RandomAccessFile raf = null;
                try {
                    raf = new RandomAccessFile(new File(segmentMetadata.getFilePath()), "r");
                    if(pendingLines + segmentMetadata.getLineCount() > maxLines) {
                        long endOffset;
                        if(pendingLines > 0) {
                            long linePointer = maxLines - pendingLines;
                            raf.seek((linePointer - 1) * 8);
                            endOffset = raf.readLong();
                            newOffsets.add(new Long[]{offset, endOffset});
                            offset = endOffset + 1;
                        }

                        while ((raf.length() - raf.getFilePointer()) / 8 > maxLines) {
                            raf.seek(raf.getFilePointer() +  maxLines * 8);
                            endOffset = raf.readLong();
                            newOffsets.add(new Long[]{offset, endOffset});
                            offset = endOffset + 1;
                        }
                        pendingLines = (raf.length() - raf.getFilePointer()) / 8;
                    } else {
                        raf.seek((segmentMetadata.getLineCount() - 1) * 8);
                        long endOffset = raf.readLong();
                        newOffsets.add(new Long[]{offset, endOffset});
                        offset = endOffset + 1;
                    }
                    if(pendingLines > 0 && !it.hasNext()) {
                        raf.seek(raf.length() - 8);
                        newOffsets.add(new Long[]{offset, raf.readLong()});
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
            }

            // generating tasks context
            final String fileHeader = tasks.get(0).getFileHeader();
            tasks.clear();
            final long taskCount = newOffsets.size();
            final AtomicInteger taskIndex = new AtomicInteger(1);
            tasks = newOffsets.stream().map(offsets -> {
                long beginOffset = offsets[0];
                long endOffset = offsets[1];
                return new SplitContext.Builder(beginOffset, endOffset, taskConfiguration, fileHeader)
                        .taskCount(taskCount)
                        .taskIndex(taskIndex.getAndIncrement())
                        .build();
            }).collect(Collectors.toList());

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            executorService.shutdown();
        }
        return tasks;
    }
}
