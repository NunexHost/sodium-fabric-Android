package me.jellysquid.mods.sodium.model.vertex.formats.generic;

import me.jellysquid.mods.sodium.interop.vanilla.matrix.Matrix4fUtil;
import me.jellysquid.mods.sodium.model.vertex.VanillaVertexTypes;
import me.jellysquid.mods.sodium.model.vertex.VertexSink;
import me.jellysquid.mods.sodium.model.vertex.buffer.VertexBufferView;
import me.jellysquid.mods.sodium.model.vertex.buffer.VertexBufferWriterNio;
import me.jellysquid.mods.sodium.model.vertex.buffer.VertexBufferWriterUnsafe;
import me.jellysquid.mods.sodium.model.vertex.fallback.VertexWriterFallback;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.math.Matrix4f;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public interface PositionTextureSink extends VertexSink {
    VertexFormat VERTEX_FORMAT = VertexFormats.POSITION_TEXTURE;

    void writeVertex(float x, float y, float z, float u, float v);

    default void writeVertex(Matrix4f matrix, float x, float y, float z, float u, float v) {
        float x2 = Matrix4fUtil.transformVectorX(matrix, x, y, z);
        float y2 = Matrix4fUtil.transformVectorY(matrix, x, y, z);
        float z2 = Matrix4fUtil.transformVectorZ(matrix, x, y, z);

        this.writeVertex(x2, y2, z2, u, v);
    }

    class WriterNio extends VertexBufferWriterNio implements PositionTextureSink {
        public WriterNio(VertexBufferView backingBuffer) {
            super(backingBuffer, VanillaVertexTypes.POSITION_TEXTURE);
        }

        @Override
        public void writeVertex(float x, float y, float z, float u, float v) {
            int i = this.writeOffset;

            ByteBuffer buf = this.byteBuffer;
            buf.putFloat(i, x);
            buf.putFloat(i + 4, y);
            buf.putFloat(i + 8, z);
            buf.putFloat(i + 12, u);
            buf.putFloat(i + 16, v);

            this.advance();
        }
    }

    class WriterUnsafe extends VertexBufferWriterUnsafe implements PositionTextureSink {
        public WriterUnsafe(VertexBufferView backingBuffer) {
            super(backingBuffer, VanillaVertexTypes.POSITION_TEXTURE);
        }

        @Override
        public void writeVertex(float x, float y, float z, float u, float v) {
            long i = this.writePointer;

            MemoryUtil.memPutFloat(i, x);
            MemoryUtil.memPutFloat(i + 4, y);
            MemoryUtil.memPutFloat(i + 8, z);

            MemoryUtil.memPutFloat(i + 12, u);
            MemoryUtil.memPutFloat(i + 16, v);

            this.advance();
        }
    }

    class WriterFallback extends VertexWriterFallback implements PositionTextureSink {
        public WriterFallback(VertexConsumer consumer) {
            super(consumer);
        }

        @Override
        public void writeVertex(float x, float y, float z, float u, float v) {
            this.consumer.vertex(x, y, z);
            this.consumer.texture(u, v);
        }
    }
}