package org.f1x.api.message.id;

import java.io.*;

public abstract class FileBasedHiLowIdentifierGenerator extends HiLowIdentifierGenerator
{
    protected final File file;
    
    protected FileBasedHiLowIdentifierGenerator(final File dir, final String key, final int blockSize, final long startId) {
        super(key, blockSize, startId);
        dir.getAbsoluteFile().mkdirs();
        this.file = new File(dir, "sequence-" + key + ".id");
    }
}
