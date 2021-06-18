package cn.nihility.dowload.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleDownloadProgressPrinter implements DownloadProgressPrinter {

    private static final Logger log = LoggerFactory.getLogger(SimpleDownloadProgressPrinter.class);

    private long contentLength;
    private long alreadyDownloadLength;

    @Override
    public void print(String task, long contentLength, long alreadyDownloadLength, long speed) {
        this.contentLength = contentLength;
        this.alreadyDownloadLength = alreadyDownloadLength;
        log.info("[{}] 文件总大小 [{}] KB, 已下载 [{}] KB, 下载速度 [{}] KB",
            task, contentLength, alreadyDownloadLength / 1024, speed / 1000);
    }

    @Override
    public long getContentLength() {
        return this.contentLength;
    }

    @Override
    public long getAlreadyDownloadLength() {
        return this.alreadyDownloadLength;
    }
}
