package org.geysermc.rainbow.mapping.geometry;

import com.mojang.math.Transformation;
import net.minecraft.client.resources.model.geometry.UnbakedGeometry;
import net.minecraft.resources.Identifier;
import org.geysermc.rainbow.Rainbow;
import org.geysermc.rainbow.mapping.PackAssetCache;
import org.geysermc.rainbow.pack.geometry.BedrockGeometry;

import java.util.Optional;

public class MappedGeometryCache extends PackAssetCache<MappedGeometryCache.Key, MappedGeometry> {

    public Optional<MappedGeometry> mapGeometry(Identifier bedrockIdentifier, ModelContext context) {
        return getOrComputeOptional(new Key(context), () -> {
            String safeIdentifier = Rainbow.bedrockSafeIdentifier(bedrockIdentifier);
            return GeometryMapper.mapGeometry(safeIdentifier, "bone", context)
                    .map(BedrockGeometry::of)
                    .map(MappedGeometryInstance::new);
        });
    }

    public record Key(UnbakedGeometry geometry, Transformation transformation) {

        public Key(ModelContext context) {
            this(context.model().getTopGeometry(), context.transformation());
        }
    }
}
