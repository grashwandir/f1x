package org.f1x.api.message.id;

import java.io.*;
import java.nio.channels.*;

public final class SharedFileHiLowIdentifierGenerator extends FileBasedHiLowIdentifierGenerator
{
    public SharedFileHiLowIdentifierGenerator(final File dir, final String key, final int blockSize) {
        this(dir, key, blockSize, 1L);
    }
    
    public SharedFileHiLowIdentifierGenerator(final File dir, final String key, final int blockSize, final long startId) {
        super(dir, key, blockSize, startId);
    }
    
    @Override
    protected long acquireNextBlock(final long resetNextBlock) {
        RandomAccessFile raf = null;
        FileChannel channel = null;
        FileLock lock = null;
        try {
            try {
                raf = new RandomAccessFile(this.file, "rw");
                channel = raf.getChannel();
                lock = channel.lock();
                long nextBlock;
                if (resetNextBlock != 0L) {
                    nextBlock = resetNextBlock;
                }
                else if (this.file.length() == 0L) {
                    nextBlock = this.startId;
                }
                else {
                    final String lastBlock = raf.readLine();
                    nextBlock = Long.parseLong(lastBlock) + this.blockSize;
                }
                raf.seek(0L);
                raf.write(Long.toString(nextBlock).getBytes());
                return nextBlock;
            }
            catch (IOException e) {
                throw new RuntimeException("Error accessing sequence storage file: " + e.getMessage(), e);
            }
            finally {
                if (lock != null) {
                    lock.release();
                }
                if (channel != null) {
                    channel.close();
                }
                if (raf != null) {
                    raf.close();
                }
            }
        }
        catch (IOException e) {
            throw new RuntimeException("Error accessing sequence storage file: " + e.getMessage(), e);
        }
    }
}
