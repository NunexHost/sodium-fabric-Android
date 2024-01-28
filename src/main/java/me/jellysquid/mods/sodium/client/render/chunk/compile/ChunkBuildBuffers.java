package me.jellysquid.mods.sodium.client.render.chunk.compile;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import me.jellysquid.mods.sodium.client.gl.util.VertexRange;
import me.jellysquid.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import me.jellysquid.mods.sodium.client.render.chunk.compile.buffers.BakedChunkModelBuilder;
import me.jellysquid.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import me.jellysquid.mods.sodium.client.render.chunk.data.BuiltSectionInfo;
import me.jellysquid.mods.sodium.client.render.chunk.data.BuiltSectionMeshParts;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.DefaultTerrainRenderPasses;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.Material;
import me.jellysquid.mods.sodium.client.render.chunk.vertex.builder.ChunkMeshBufferBuilder;
import me.jellysquid.mods.sodium.client.render.chunk.vertex.format.ChunkVertexType;
import me.jellysquid.mods.sodium.client.util.NativeBuffer;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Uma coleção de buffers temporários para cada thread de trabalho que serão usados para construir malhas de chunk para
 * renderizações passadas. Isso faz uma tentativa de melhor esforço para escolher um tamanho adequado para cada buffer de
 * rascunho, mas nunca tentará reduzir um buffer.
 */
public class ChunkBuildBuffers {
    private final Reference2ReferenceOpenHashMap<TerrainRenderPass, BakedChunkModelBuilder> builders = new Reference2ReferenceOpenHashMap<>();

    private final ChunkVertexType vertexType;
    private final int initialBufferSize;

    public ChunkBuildBuffers(ChunkVertexType vertexType) {
        this.vertexType = vertexType;
        this.initialBufferSize = 128 * 1024;

        for (TerrainRenderPass pass : DefaultTerrainRenderPasses.ALL) {
            var vertexBuffers = new ChunkMeshBufferBuilder[ModelQuadFacing.COUNT];

            for (int facing = 0; facing < ModelQuadFacing.COUNT; facing++) {
                vertexBuffers[facing] = new ChunkMeshBufferBuilder(this.vertexType, initialBufferSize);
            }

            this.builders.put(pass, new BakedChunkModelBuilder(vertexBuffers));
        }
    }

    public void init(BuiltSectionInfo.Builder renderData, int sectionIndex) {
        for (var builder : this.builders.values()) {
            builder.begin(renderData, sectionIndex);
        }
    }

    public ChunkModelBuilder get(Material material) {
        return this.builders.get(material.pass);
    }

    /**
     * Cria malhas de chunk assadas imutáveis a partir de todos os buffers de rascunho não vazios. Isso é usado após
     * todos os blocos terem sido renderizados para passar as malhas finalizadas para a placa gráfica. Esta função pode
     * ser chamada várias vezes para retornar várias cópias.
     */
    public BuiltSectionMeshParts createMesh(TerrainRenderPass pass) {
        var builder = this.builders.get(pass);

        List<ByteBuffer> vertexBuffers = new ArrayList<>();
        VertexRange[] vertexRanges = new VertexRange[ModelQuadFacing.COUNT];

        int vertexCount = 0;

        for (ModelQuadFacing facing : ModelQuadFacing.VALUES) {
            var buffer = builder.getVertexBuffer(facing);

            if (buffer.isEmpty()) {
                continue;
            }

            vertexBuffers.add(buffer.slice());
            vertexRanges[facing.ordinal()] = new VertexRange(vertexCount, buffer.count());

            vertexCount += buffer.count();
        }

        if (vertexCount == 0) {
            return null;
        }

        var mergedBuffer = new NativeBuffer(vertexCount * this.vertexType.getVertexFormat().getStride());
        var mergedBufferBuilder = mergedBuffer.getDirectBuffer();

        for (var buffer : vertexBuffers) {
            mergedBufferBuilder.put(buffer);
        }

        mergedBufferBuilder.flip();

        return new BuiltSectionMeshParts(mergedBuffer, vertexRanges);
    }

    public void destroy() {
        for (var builder : this.builders.values()) {
            builder.destroy();
        }
    }
}

            
