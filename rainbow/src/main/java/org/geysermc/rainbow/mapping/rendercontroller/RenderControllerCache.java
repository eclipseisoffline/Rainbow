package org.geysermc.rainbow.mapping.rendercontroller;

import org.geysermc.rainbow.mapping.PackAssetCache;
import org.geysermc.rainbow.mapping.geometry.BedrockGeometryContext;
import org.geysermc.rainbow.mapping.texture.ModelTextures;

import java.util.List;

public final class RenderControllerCache extends PackAssetCache<RenderControllerCache.Key, BedrockRenderControllerContext> {

    public BedrockRenderControllerContext map(List<ModelTextures> textures, List<BedrockGeometryContext> geometries, List<Float> useDurations) {
        List<ModelTextures.RenderControllerConfiguration> textureConfigurations = textures.stream()
                .flatMap(texture -> texture.renderControllerConfiguration().stream())
                .toList();
        return getOrCompute(new Key(textureConfigurations, geometries, useDurations), () -> {
            if (textures.isEmpty()) {
                return BedrockRenderControllerContext.VANILLA_ITEM_DEFAULT;
            }
        });
    }

    public BedrockRenderControllerContext map(ModelTextures textures, BedrockGeometryContext geometry) {
        return map(List.of(textures), List.of(geometry), List.of());
    }

    public record Key(List<ModelTextures.RenderControllerConfiguration> textureConfigurations, List<BedrockGeometryContext> geometries, List<Float> useDurations) {}
}
