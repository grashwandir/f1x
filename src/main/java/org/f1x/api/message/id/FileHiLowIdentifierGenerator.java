package org.f1x.api.message.id;

import java.nio.channels.*;
import java.io.*;

public final class FileHiLowIdentifierGenerator extends FileBasedHiLowIdentifierGenerator implements Closeable
{
    private final RandomAccessFile raf;
    private final FileChannel channel;
    private final FileLock lock;
    private final boolean storeLastUsedOnClose;
    
    public FileHiLowIdentifierGenerator(final File dir, final String key, final int blockSize, final boolean writeLastUsedOnClose) throws IOException {
        this(dir, key, blockSize, 1L, writeLastUsedOnClose, "rwd");
    }
    
    public FileHiLowIdentifierGenerator(final File dir, final String key, final int blockSize, final long startId, final boolean writeLastUsedOnClose, final String fileMode) throws IOException {
        super(dir, key, blockSize, startId);
        this.storeLastUsedOnClose = writeLastUsedOnClose;
        this.raf = new RandomAccessFile(this.file, fileMode);
        this.channel = this.raf.getChannel();
        this.lock = this.channel.tryLock();
        if (this.lock == null) {
            throw new RuntimeException("Another program holds lock for file " + this.file.getAbsolutePath());
        }
    }
    
    @Override
    protected long acquireNextBlock(final long resetNextBlock) {
        try {
            long currentBlock;
            if (resetNextBlock != 0L) {
                currentBlock = resetNextBlock;
            }
            else if (this.file.length() == 0L) {
                currentBlock = this.startId;
            }
            else {
                this.raf.seek(0L);
                final String lastBlock = this.raf.readLine();
                currentBlock = Long.parseLong(lastBlock);
            }
            this.raf.seek(0L);
            this.raf.write(Long.toString(currentBlock + this.blockSize).getBytes());
            return currentBlock;
        }
        catch (IOException e) {
            throw new RuntimeException("Error accessing sequence storage file: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void close() throws IOException {
        if (this.storeLastUsedOnClose) {
            this.storeLastUsed();
        }
        this.lock.release();
        this.channel.close();
        this.raf.close();
    }
    
    private void storeLastUsed() {
        this.store(this.next());
    }
    
    private void store(final long lastUsed) {
        if (this.channel != null && this.channel.isOpen() && this.raf != null) {
            try {
                this.raf.seek(0L);
                final String block = Long.toString(lastUsed);
                this.raf.write(block.getBytes());
                this.raf.setLength(block.length());
            }
            catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }
}
