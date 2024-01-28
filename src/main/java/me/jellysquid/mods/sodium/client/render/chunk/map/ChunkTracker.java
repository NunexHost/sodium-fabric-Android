package me.jellysquid.mods.sodium.client.render.chunk.map;

import it.unimi.dsi.fastutil.longs.*;
import net.minecraft.util.math.ChunkPos;

public class ChunkTracker implements ClientChunkEventListener {
    private final Long2IntOpenHashMap chunkStatus = new Long2IntOpenHashMap();
    private final LongOpenHashSet chunkReady = new LongOpenHashSet();

    private final LongSet unloadQueue = new LongOpenHashSet();
    private final LongSet loadQueue = new LongOpenHashSet();

    public ChunkTracker() {

    }

    @Override
    public void updateMapCenter(int chunkX, int chunkZ) {

    }

    @Override
    public void updateLoadDistance(int loadDistance) {

    }

    @Override
    public void onChunkStatusAdded(int x, int z, int flags) {
        long key = ChunkPos.toLong(x, z);

        int prev = this.chunkStatus.get(key);
        int cur = prev | flags;

        if (prev == cur) {
            return;
        }

        this.chunkStatus.put(key, cur);

        this.updateNeighbors(x, z);
    }

    @Override
    public void onChunkStatusRemoved(int x, int z, int flags) {
        long key = ChunkPos.toLong(x, z);

        int prev = this.chunkStatus.get(key);
        int cur = prev & ~flags;

        if (prev == cur) {
            return;
        }

        if (cur == this.chunkStatus.defaultReturnValue()) {
            this.chunkStatus.remove(key);
        } else {
            this.chunkStatus.put(key, cur);
        }

        this.updateNeighbors(x, z);
    }

    private void updateNeighbors(int x, int z) {
        int[] neighbors = new int[8];

        for (int i = 0, idx = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                neighbors[idx++] = ChunkPos.toLong(x + i - 1, z + j - 1);
            }
        }

        this.updateMerged(neighbors);
    }

    private void updateMerged(long[] neighbors) {
        for (long key : neighbors) {
            int flags = this.chunkStatus.get(key);

            for (long neighbor : neighbors) {
                flags &= this.chunkStatus.get(neighbor);
            }

            if (flags == ChunkStatus.FLAG_ALL) {
                if (this.chunkReady.add(key) && !this.unloadQueue.remove(key)) {
                    this.loadQueue.add(key);
                }
            } else {
                if (this.chunkReady.remove(key) && !this.loadQueue.remove(key)) {
                    this.unloadQueue.add(key);
                }
            }
        }
    }

    public LongCollection getReadyChunks() {
        return LongSets.unmodifiable(this.chunkReady);
    }

    public void forEachEvent(ChunkEventHandler loadEventHandler, ChunkEventHandler unloadEventHandler) {
        forEachChunk(this.unloadQueue, unloadEventHandler);
        this.unloadQueue.clear();

        forEachChunk(this.loadQueue, loadEventHandler);
        this.loadQueue.clear();
    }

    public static void forEachChunk(LongCollection queue, ChunkEventHandler handler) {
        var iterator = queue.iterator();

        while (iterator.hasNext()) {
            var pos = iterator.nextLong();

            var x = ChunkPos.getPackedX(pos);
            var z = ChunkPos.getPackedZ(pos);

            handler.apply(x, z);
        }
    }

    public interface ChunkEventHandler {
        void apply(int x, int z);
    }
                    }
