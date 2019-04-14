package org.f1x.api.message.id;

public abstract class HiLowIdentifierGenerator implements IdentifierGenerator
{
    protected final int blockSize;
    protected final long startId;
    protected final String key;
    private long base;
    private long id;
    
    protected HiLowIdentifierGenerator(final String key, final int blockSize, final long startId) {
        this.key = key;
        this.blockSize = blockSize;
        this.startId = startId;
        this.base = 0L;
        this.id = blockSize;
    }
    
    @Override
    public synchronized long next() {
        ++this.id;
        if (this.id >= this.blockSize) {
            this.base = this.acquireNextBlock(0L);
            this.id = 0L;
        }
        return this.base + this.id;
    }
    
    protected abstract long acquireNextBlock(final long p0);
    
    @Override
    public void reset() throws UnsupportedOperationException {
    }
}
