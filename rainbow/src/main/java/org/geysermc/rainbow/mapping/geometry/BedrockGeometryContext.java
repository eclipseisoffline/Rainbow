package org.geysermc.rainbow.mapping.geometry;

import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.cuboid.UnbakedCuboidGeometry;
import net.minecraft.resources.Identifier;
import org.geysermc.rainbow.Rainbow;
import org.geysermc.rainbow.mapping.PackContext;
import org.geysermc.rainbow.mapping.PackSerializer;
import org.geysermc.rainbow.mapping.PackSerializingContext;
import org.geysermc.rainbow.mapping.animation.AnimationMapper;
import org.geysermc.rainbow.mapping.animation.BedrockAnimationContext;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public record BedrockGeometryContext(Optional<MappedGeometry> geometry,
                                     Optional<BedrockAnimationContext> animation, boolean handheld) implements PackSerializer.Serializable {
    private static final List<Identifier> HANDHELD_MODELS = Stream.of("item/handheld", "item/handheld_rod", "item/handheld_mace")
            .map(Identifier::withDefaultNamespace)
            .toList();

    @Override
    public CompletableFuture<?> save(PackSerializingContext context) {
        return PackSerializer.Serializable.wrapOptional(geometry)
                .with(animation) // TODO maybe move this out of this
                .save(context);
    }

    public static BedrockGeometryContext create(Identifier bedrockIdentifier, ModelContext modelContext, PackContext packContext) {
        boolean isFlatBuiltin = isFlatBuiltin(modelContext.model());
        if (isFlatBuiltin) {
            modelContext = modelContext.withModel(new FlatBuiltinItemModel(packContext.assetResolver(), modelContext.model()));
        }

        ResolvedModel parentModel = modelContext.model().parent();
        boolean handheld = parentModel != null && HANDHELD_MODELS.contains(Rainbow.getModelIdentifier(modelContext.model()));

        Optional<MappedGeometry> geometry = Optional.empty();
        Optional<BedrockAnimationContext> animation = Optional.empty();

        if (modelContext.textures().requiresAttachable() || !isFlatBuiltin) {
            // Not flat built-in model, or textures require an attachable (e.g. texture is animated), so map geometry and animation

            geometry = packContext.geometryCache().mapGeometry(bedrockIdentifier, modelContext);
            animation = Optional.of(AnimationMapper.mapAnimation(Rainbow.bedrockSafeIdentifier(bedrockIdentifier), "bone", modelContext.model().getTopTransforms()));
        }

        return new BedrockGeometryContext(geometry, animation, handheld);
    }

    public static boolean isFlatBuiltin(ResolvedModel model) {
        return !(model.getTopGeometry() instanceof UnbakedCuboidGeometry);
    }
}
