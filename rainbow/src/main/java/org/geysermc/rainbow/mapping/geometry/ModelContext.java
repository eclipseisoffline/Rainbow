package org.geysermc.rainbow.mapping.geometry;

import com.mojang.math.Transformation;
import net.minecraft.client.resources.model.ResolvedModel;
import org.geysermc.rainbow.mapping.texture.ModelTextures;

public record ModelContext(ResolvedModel model, ModelTextures textures, Transformation transformation) {

    public ModelContext withModel(ResolvedModel model) {
        return new ModelContext(model, textures, transformation);
    }
}
