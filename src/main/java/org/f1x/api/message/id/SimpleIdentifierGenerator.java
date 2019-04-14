package org.f1x.api.message.id;

import java.util.concurrent.*;

public class SimpleIdentifierGenerator implements ResettableIdentifierGenerator
{
    private long nextID;
    
    public SimpleIdentifierGenerator() {
        this(System.currentTimeMillis() % TimeUnit.DAYS.toMillis(1L));
    }
    
    public SimpleIdentifierGenerator(final long base) {
        this.nextID = base;
    }
    
    @Override
    public synchronized long next() {
        return this.nextID++;
    }
    
    @Override
    public synchronized void setNext(final long nextId) {
        if (this.nextID < nextId) {
            this.nextID = nextId;
        }
    }
    
    @Override
    public void reset() {
        throw new UnsupportedOperationException();
    }
}
