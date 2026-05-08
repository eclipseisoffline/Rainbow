package org.geysermc.rainbow.mapping.rendercontroller;

import com.mojang.datafixers.util.Either;
import org.geysermc.rainbow.mapping.PackAssetCache;
import org.geysermc.rainbow.mapping.PackSerializer;
import org.geysermc.rainbow.mapping.PackSerializingContext;
import org.geysermc.rainbow.pack.PackPaths;
import org.geysermc.rainbow.pack.rendercontroller.BedrockRenderControllers;
import org.geysermc.rainbow.pack.rendercontroller.VanillaRenderControllers;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public record BedrockRenderControllerContext(Either<String, BedrockRenderControllers> renderControllers)
        implements PackAssetCache.Cacheable<BedrockRenderControllerContext>, PackSerializer.Serializable {
    public static final BedrockRenderControllerContext VANILLA_ARMOR = new BedrockRenderControllerContext(VanillaRenderControllers.ARMOR);
    public static final BedrockRenderControllerContext VANILLA_ITEM_DEFAULT = new BedrockRenderControllerContext(VanillaRenderControllers.ITEM_DEFAULT);

    public BedrockRenderControllerContext(String vanillaIdentifier) {
        this(Either.left(vanillaIdentifier));
    }

    public BedrockRenderControllerContext(BedrockRenderControllers renderControllers) {
        this(Either.right(renderControllers));
    }

    public String identifier() {
        return renderControllers.map(Function.identity(), controllers -> controllers.renderControllers().keySet().stream().findFirst().orElseThrow());
    }

    @Override
    public BedrockRenderControllerContext cachedCopy() {
        if (renderControllers.left().isPresent()) {
            return this;
        }
        return new BedrockRenderControllerContext(Either.left(identifier()));
    }

    @Override
    public CompletableFuture<?> save(PackSerializingContext context) {
        return PackSerializer.Serializable.wrapOptionalCodec(BedrockRenderControllers.CODEC, renderControllers.right(), PackPaths::renderControllers).save(context);
    }
}
