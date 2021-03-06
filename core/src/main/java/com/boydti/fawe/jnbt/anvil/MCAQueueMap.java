package com.boydti.fawe.jnbt.anvil;

import com.boydti.fawe.example.IFaweQueueMap;
import com.boydti.fawe.example.MappedFaweQueue;
import com.boydti.fawe.example.NullFaweChunk;
import com.boydti.fawe.object.FaweChunk;
import com.boydti.fawe.object.FaweQueue;
import com.boydti.fawe.object.RunnableVal;
import com.boydti.fawe.object.exception.FaweException;
import com.boydti.fawe.util.MathMan;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MCAQueueMap implements IFaweQueueMap {


    private FaweQueue queue;

    private Map<Long, MCAFile> mcaFileMap = new ConcurrentHashMap<>(8, 0.9f, 1);
    private NullFaweChunk nullChunk;
    private boolean isHybridQueue;

    public void setParentQueue(FaweQueue queue) {
        this.queue = queue;
        this.nullChunk = new NullFaweChunk(queue, 0, 0);
        this.isHybridQueue = queue != null && !(queue instanceof MCAQueue) && (!(queue instanceof MappedFaweQueue) || ((MappedFaweQueue) queue).getFaweQueueMap() != this);
    }

    private MCAFile lastFile;
    private int lastFileX = Integer.MIN_VALUE;
    private int lastFileZ = Integer.MIN_VALUE;

    public MCAFile getMCAFile(int cx, int cz) {
        int mcaX = cx >> 5;
        int mcaZ = cz >> 5;
        if (mcaX == lastFileX && mcaZ == lastFileZ) {
            return lastFile;
        }
        long pair = MathMan.pairInt(lastFileX = mcaX, lastFileZ = mcaZ);
        lastFile = mcaFileMap.get(pair);
        if (lastFile == null) {
            try {
                lastFile = new MCAFile(queue, lastFileX, lastFileZ);
            } catch (FaweException.FaweChunkLoadException ignore) {
            } catch (Exception e) {
                e.printStackTrace();
                return lastFile = null;
            }
            mcaFileMap.put(pair, lastFile);
        }
        return lastFile;
    }

    @Override
    public Collection<FaweChunk> getFaweCunks() {
        final List<FaweChunk> chunks = new ArrayList<>();
        for (Map.Entry<Long, MCAFile> entry : mcaFileMap.entrySet()) {
            MCAFile file = entry.getValue();
            if (file != null) {
                chunks.addAll(file.getCachedChunks());
            }
        }
        return chunks;
    }

    @Override
    public void forEachChunk(RunnableVal<FaweChunk> onEach) {
        for (FaweChunk chunk : getFaweCunks()) {
            onEach.run(chunk);
        }
    }

    private FaweChunk lastChunk;
    private int lastX = Integer.MIN_VALUE;
    private int lastZ = Integer.MIN_VALUE;

    @Override
    public FaweChunk getFaweChunk(int cx, int cz) {
        if (cx == lastX && cz == lastZ) {
            if (nullChunk == lastChunk) {
                nullChunk.setLoc(queue, lastX, lastZ);
            }
            return lastChunk;
        }
        lastX = cx;
        lastZ = cz;
        if (isHybridQueue) {
            lastChunk = ((MappedFaweQueue) queue).getFaweQueueMap().getCachedFaweChunk(cx, cz);
            if (lastChunk != null) {
                return lastChunk;
            }
        }
        try {
            MCAFile mcaFile = getMCAFile(cx, cz);
            if (mcaFile != null) {
                mcaFile.init();
                lastChunk = mcaFile.getChunk(cx, cz);
                if (lastChunk != null) {
                    return lastChunk;
                }
            }
        } catch (Throwable ignore) {
            ignore.printStackTrace();
        }
        if (isHybridQueue) { // Use parent queue for in use chunks
            lastChunk = ((MappedFaweQueue)queue).getFaweQueueMap().getFaweChunk(cx, cz);
            return lastChunk;
        }
        nullChunk.setLoc(queue, lastX, lastZ);
        return lastChunk = nullChunk;
    }

    @Override
    public FaweChunk getCachedFaweChunk(int cx, int cz) {
        int mcaX = cx >> 5;
        int mcaZ = cz >> 5;
        long pair = MathMan.pairInt(mcaX, mcaZ);
        MCAFile file = mcaFileMap.get(pair);
        if (file != null) {
            return file.getCachedChunk(cx, cz);
        }
        return null;
    }

    @Override
    public void add(FaweChunk chunk) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void clear() {
        mcaFileMap.clear();
        if (isHybridQueue) {
            queue.clear();
        }
    }

    @Override
    public int size() {
        int size = mcaFileMap.size();
        if (isHybridQueue) {
            size += queue.size();
        }
        return size;
    }

    @Override
    public boolean next(int size, long time) {
        lastX = Integer.MIN_VALUE;
        lastZ = Integer.MIN_VALUE;
        lastFileX = Integer.MIN_VALUE;
        lastFileZ = Integer.MIN_VALUE;
        if (!mcaFileMap.isEmpty()) {
            Iterator<Map.Entry<Long, MCAFile>> iter = mcaFileMap.entrySet().iterator();
            boolean result;
            long start = System.currentTimeMillis();
            do {
                if (result = iter.hasNext()) {
                    MCAFile file = iter.next().getValue();
                    iter.remove();
                    file.flush();
                    file.close();
                } else {
                    break;
                }
            } while (System.currentTimeMillis() - start < time);
            return result;
        }
        if (isHybridQueue) {
            boolean value = queue.next();
            return value;
        }
        return false;
    }
}
