package me.jellysquid.mods.sodium.client.render.chunk.lists;

import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSectionFlags;
import me.jellysquid.mods.sodium.client.util.iterator.ByteIterator;
import me.jellysquid.mods.sodium.client.util.iterator.ReversibleByteArrayIterator;
import me.jellysquid.mods.sodium.client.util.iterator.ByteArrayIterator;
import me.jellysquid.mods.sodium.client.render.chunk.region.RenderRegion;
import org.jetbrains.annotations.Nullable;

import java.util.BitSet;

public class ChunkRenderList {
    private final RenderRegion region;

    private final BitSet sectionsWithGeometry = new BitSet(RenderRegion.REGION_SIZE);
    private int sectionsWithGeometryCount;

    private final BitSet sectionsWithSprites = new BitSet(RenderRegion.REGION_SIZE);
    private int sectionsWithSpritesCount;

    private final BitSet sectionsWithEntities = new BitSet(RenderRegion.REGION_SIZE);
    private int sectionsWithEntitiesCount;

    private int size;

    private int lastVisibleFrame;

    public ChunkRenderList(RenderRegion region) {
        this.region = region;
    }

    public void reset(int frame) {
        sectionsWithGeometry.clear();
        sectionsWithSprites.clear();
        sectionsWithEntities.clear();

        this.size = 0;
        this.lastVisibleFrame = frame;
    }

    public void add(RenderSection render) {
        if (size >= RenderRegion.REGION_SIZE) {
            throw new ArrayIndexOutOfBoundsException("Render list is full");
        }

        size++;

        int index = render.getSectionIndex();
        int flags = render.getFlags();

        sectionsWithGeometry.set(index, (flags >>> RenderSectionFlags.HAS_BLOCK_GEOMETRY) & 1 != 0);
        sectionsWithSprites.set(index, (flags >>> RenderSectionFlags.HAS_ANIMATED_SPRITES) & 1 != 0);
        sectionsWithEntities.set(index, (flags >>> RenderSectionFlags.HAS_BLOCK_ENTITIES) & 1 != 0);

        sectionsWithGeometryCount = sectionsWithGeometry.cardinality();
        sectionsWithSpritesCount = sectionsWithSprites.cardinality();
        sectionsWithEntitiesCount = sectionsWithEntities.cardinality();
    }

    public @Nullable ByteIterator sectionsWithGeometryIterator(boolean reverse) {
        if (sectionsWithGeometryCount == 0) {
            return null;
        }

        return new ReversibleByteArrayIterator(sectionsWithGeometry.toByteArray(), sectionsWithGeometryCount, reverse);
    }

    public @Nullable ByteIterator sectionsWithSpritesIterator() {
        if (sectionsWithSpritesCount == 0) {
            return null;
        }

        return new ByteArrayIterator(sectionsWithSprites.toByteArray(), sectionsWithSpritesCount);
    }

    public @Nullable ByteIterator sectionsWithEntitiesIterator() {
        if (sectionsWithEntitiesCount == 0) {
            return null;
        }

        return new ByteArrayIterator(sectionsWithEntities.toByteArray(), sectionsWithEntitiesCount);
    }

    public int getSectionsWithGeometryCount() {
        return sectionsWithGeometryCount;
    }

    public int getSectionsWithSpritesCount() {
        return sectionsWithSpritesCount;
    }

    public int getSectionsWithEntitiesCount() {
        return sectionsWithEntitiesCount;
    }

    public int getLastVisibleFrame() {
        return lastVisibleFrame;
    }

    public RenderRegion getRegion() {
        return region;
    }

    public int size() {
        return size;
    }
}
