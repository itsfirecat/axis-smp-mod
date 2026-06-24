package net.itsfirecat.arcbound.client;

import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.*;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import java.io.IOException;

public class ArcImpactRenderType {

    @Nullable private static ShaderProgram entityWhiteProgram  = null;
    @Nullable private static ShaderProgram entityBlackProgram  = null;
    @Nullable private static ShaderProgram entityInvertProgram = null;

    public static void reload(ResourceManager manager) {
        System.out.println("[ARCIMPACT] reload() called");

        // Debug: check if resource manager sees the file at all
        var resource = manager.getResource(
                net.minecraft.util.Identifier.of("arcbound", "shaders/core/arc_entity_white.json")
        );
        System.out.println("[ARCIMPACT] resource present: " + resource.isPresent());

        closeAll();
        entityWhiteProgram  = load(manager, "arc_entity_white");
        entityBlackProgram  = load(manager, "arc_entity_black");
        entityInvertProgram = load(manager, "arc_entity_invert");
        System.out.println("[ARCIMPACT] white=" + entityWhiteProgram + " black=" + entityBlackProgram + " invert=" + entityInvertProgram);
    }

    private static ShaderProgram load(ResourceManager manager, String name) {
        try {
            return new ShaderProgram(manager, name,
                    VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL);
        } catch (IOException e) {
            System.err.println("[ARCIMPACT] " + name + " shader failed: " + e.getMessage());
            return null;
        }
    }

    private static void closeAll() {
        if (entityWhiteProgram  != null) { entityWhiteProgram.close();  entityWhiteProgram  = null; }
        if (entityBlackProgram  != null) { entityBlackProgram.close();  entityBlackProgram  = null; }
        if (entityInvertProgram != null) { entityInvertProgram.close(); entityInvertProgram = null; }
    }

    public static boolean isReady() {
        return entityWhiteProgram != null && entityBlackProgram != null;
    }

    @Nullable public static ShaderProgram getWhiteProgram()  { return entityWhiteProgram; }
    @Nullable public static ShaderProgram getBlackProgram()  { return entityBlackProgram; }
    @Nullable public static ShaderProgram getInvertProgram() {
        return entityInvertProgram != null ? entityInvertProgram : entityWhiteProgram;
    }

    public static RenderLayer getWhiteLayer(Identifier texture) {
        return buildLayer("arc_entity_white", texture, ArcImpactRenderType::getWhiteProgram);
    }

    public static RenderLayer getBlackLayer(Identifier texture) {
        return buildLayer("arc_entity_black", texture, ArcImpactRenderType::getBlackProgram);
    }

    public static RenderLayer getInvertLayer(Identifier texture) {
        return buildLayer("arc_entity_invert", texture, ArcImpactRenderType::getInvertProgram);
    }

    private static RenderLayer buildLayer(String name, Identifier texture,
                                          java.util.function.Supplier<ShaderProgram> program) {
        if (program.get() == null) return RenderLayer.getEntityCutout(texture);
        return RenderLayer.of(
                name,
                VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
                VertexFormat.DrawMode.QUADS,
                1536, false, false,
                RenderLayer.MultiPhaseParameters.builder()
                        .program(new RenderPhase.ShaderProgram(program))
                        .texture(new RenderPhase.Texture(texture, false, false))
                        .transparency(RenderPhase.NO_TRANSPARENCY)
                        .lightmap(RenderPhase.ENABLE_LIGHTMAP)
                        .overlay(RenderPhase.ENABLE_OVERLAY_COLOR)
                        .build(false)
        );
    }

    public static net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener createReloadListener() {
        return new net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener() {
            @Override public Identifier getFabricId() {
                return Identifier.of("arcbound", "impact_shaders");
            }
            @Override public void reload(ResourceManager manager) {
                ArcImpactRenderType.reload(manager);
            }
        };
    }
}