package cn.nihility.dowload;

import cn.nihility.dowload.ext.FileResponseExtractor;
import cn.nihility.dowload.support.MultiThreadDownloadProgressPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RequestCallback;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultiThreadFileDownloader extends AbstractDownloader {
    private static final Logger log = LoggerFactory.getLogger(MultiThreadFileDownloader.class);

    private int threadNum;

    public MultiThreadFileDownloader(int threadNum) {
        super(null);

        int threadSize = Runtime.getRuntime().availableProcessors();
        log.info("System available processors [{}]", threadSize);

        if (threadNum < threadSize) {
            this.threadNum = threadNum;
        } else {
            this.threadNum = threadSize;
        }

        // 多线程下载进度打印
        final MultiThreadDownloadProgressPrinter downloadProgressPrinter = new MultiThreadDownloadProgressPrinter(threadNum);
        CompletableFuture.runAsync(() -> {
            while (true) {
                long alreadyDownloadLength = downloadProgressPrinter.getAlreadyDownloadLength();
                long contentLength = downloadProgressPrinter.getContentLength();

                log.info("Content length [{}] ---> [{}]", contentLength, alreadyDownloadLength);

                if (alreadyDownloadLength != 0 && alreadyDownloadLength > contentLength) {
                    break;
                }
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        super.downloadProgressPrinter = downloadProgressPrinter;
    }

    @Override
    protected void doDownload(String fileURL, String dir, String fileName, HttpHeaders headers) throws IOException {
        ExecutorService executorService = Executors.newFixedThreadPool(threadNum);
        long contentLength = headers.getContentLength();
        log.info("Download header content length [{}]", contentLength);

        downloadProgressPrinter.setContentLength(contentLength);

        //均分文件的大小
        final long step = contentLength / threadNum;

        List<CompletableFuture<File>> futures = new ArrayList<>();

        for (int index = 0; index < threadNum; index++) {
            // 计算出每个线程的下载开始位置和结束位置
            String start = Long.toString(step * index);
            String end = (index == threadNum - 1 ? "" : Long.toString(step * (index + 1) - 1));

            String tempFilePath = dir + File.separator + "." + fileName + ".download." + index;
            final FileResponseExtractor extractor =
                new FileResponseExtractor(index, tempFilePath, downloadProgressPrinter);

            log.info("temp file index [{}], start [{}], end [{}]", index, start, end);

            CompletableFuture<File> future = CompletableFuture.supplyAsync(() -> {
                RequestCallback callback = request -> {
                    // 设置 HTTP 请求头 Range 信息，开始下载到临时文件
                    request.getHeaders().add(HttpHeaders.RANGE, "bytes=" + start + "-" + end);
                };
                return restTemplate.execute(fileURL, HttpMethod.GET, callback, extractor);
            }, executorService).exceptionally(e -> {
                e.printStackTrace();
                return null;
            });
            futures.add(future);
        }

        //创建最终文件
        String tmpFilePath = dir + File.separator + fileName;
        File file = new File(tmpFilePath);
        FileChannel outChannel = new FileOutputStream(file).getChannel();

        futures.forEach(future -> {
            try {
                File tmpFile = future.get();
                FileChannel tmpIn = new FileInputStream(tmpFile).getChannel();
                //合并每个临时文件
                outChannel.transferFrom(tmpIn, outChannel.size(), tmpIn.size());
                tmpIn.close();

                // 合并完成后删除临时文件
                tmpFile.delete();
            } catch (InterruptedException | ExecutionException | IOException e) {
                e.printStackTrace();
            }
        });
        outChannel.close();
        executorService.shutdown();
    }

}
