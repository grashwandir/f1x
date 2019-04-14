package org.f1x.api.message.id;

public interface IdentifierGenerator
{
    long next();
    
    void reset() throws UnsupportedOperationException;
}
