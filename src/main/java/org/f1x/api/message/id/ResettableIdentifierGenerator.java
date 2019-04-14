package org.f1x.api.message.id;

public interface ResettableIdentifierGenerator extends IdentifierGenerator
{
    void setNext(final long p0);
}
