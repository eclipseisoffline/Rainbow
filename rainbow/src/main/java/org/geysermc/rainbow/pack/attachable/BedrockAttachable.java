package org.geysermc.rainbow.pack.attachable;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.EquipmentSlot;
import org.geysermc.rainbow.PackConstants;
import org.geysermc.rainbow.pack.BedrockTextures;
import org.geysermc.rainbow.pack.BedrockVersion;
import org.geysermc.rainbow.pack.animation.VanillaAnimations;
import org.geysermc.rainbow.pack.geometry.VanillaGeometries;
import org.geysermc.rainbow.pack.rendercontroller.VanillaRenderControllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public record BedrockAttachable(BedrockVersion formatVersion, AttachableInfo info) {
    public static final Codec<BedrockAttachable> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BedrockVersion.STRING_CODEC.fieldOf("format_version").forGetter(BedrockAttachable::formatVersion),
                    AttachableInfo.CODEC.fieldOf("description").fieldOf("minecraft:attachable").forGetter(BedrockAttachable::info)
            ).apply(instance, BedrockAttachable::new)
    );

    public static Builder builder(Identifier identifier) {
        return new Builder(identifier);
    }

    public static BedrockAttachable.Builder equipment(Identifier identifier, EquipmentSlot slot, String texture, boolean glider) {
        String script = switch (slot) {
            case HEAD -> "variable.helmet_layer_visible = 0.0;";
            case CHEST -> "variable.chest_layer_visible = 0.0;";
            case LEGS -> "variable.leg_layer_visible = 0.0;";
            case FEET -> "variable.boot_layer_visible = 0.0;";
            default -> "";
        };
        Builder builder = builder(identifier)
                .withMaterial(VanillaAttachableTargets.DEFAULT, glider ? VanillaMaterials.ELYTRA : VanillaMaterials.ARMOR)
                .withMaterial(VanillaAttachableTargets.ENCHANTED, glider ? VanillaMaterials.ELYTRA_GLINT : VanillaMaterials.ARMOR_ENCHANTED)
                .withTexture(VanillaAttachableTargets.DEFAULT, texture)
                .withTexture(VanillaAttachableTargets.ENCHANTED, VanillaTextures.ENCHANTED_ACTOR_GLINT)
                .withGeometry(VanillaAttachableTargets.DEFAULT, Objects.requireNonNull(VanillaGeometries.fromEquipmentSlot(slot, glider)))
                .withScript("parent_setup", script)
                .withRenderController(VanillaRenderControllers.ARMOR);

        if (glider) {
            builder.withAnimation("default_controller", VanillaAnimations.ELYTRA_CONTROLLER);
            builder.withAnimation("default", VanillaAnimations.ELYTRA_DEFAULT);
            builder.withAnimation("gliding", VanillaAnimations.ELYTRA_GLIDING);
            builder.withAnimation("sneaking", VanillaAnimations.ELYTRA_SNEAKING);
            builder.withAnimation("sleeping", VanillaAnimations.ELYTRA_SLEEPING);
            builder.withAnimation("swimming", VanillaAnimations.ELYTRA_SWIMMING);
            builder.withScript("animate", List.of("default_controller"));
        }
        return builder;
    }

    public static BedrockAttachable.Builder geometry(Identifier identifier, String geometry) {
        return builder(identifier)
                .withMaterial(VanillaAttachableTargets.DEFAULT, VanillaMaterials.ENTITY_ALPHATEST)
                .withMaterial(VanillaAttachableTargets.ENCHANTED, VanillaMaterials.ENTITY_ALPHATEST_GLINT)
                .withTexture(VanillaAttachableTargets.ENCHANTED, VanillaTextures.ENCHANTED_ITEM_GLINT)
                .withGeometry(VanillaAttachableTargets.DEFAULT, geometry);
    }

    public static class Builder {
        private final Identifier identifier;
        private final Map<String, String> materials = new Object2ObjectOpenHashMap<>();
        private final Map<String, String> textures = new Object2ObjectOpenHashMap<>();
        private final Map<String, String> geometries = new Object2ObjectOpenHashMap<>();
        private final Map<String, String> animations = new Object2ObjectOpenHashMap<>();
        private final Map<String, List<Script>> scripts = new Object2ObjectOpenHashMap<>();
        private final List<String> renderControllers = new ArrayList<>();

        public Builder(Identifier identifier) {
            this.identifier = identifier;
        }

        public Builder withMaterial(String slot, String material) {
            materials.put(slot, material);
            return this;
        }

        public Builder withTexture(String slot, String texture) {
            textures.put(slot, BedrockTextures.TEXTURES_FOLDER + texture);
            return this;
        }

        public Builder withGeometry(String target, String geometry) {
            geometries.put(target, geometry);
            return this;
        }

        public Builder withAnimation(String key, String animation) {
            animations.put(key, animation);
            return this;
        }

        public Builder withScript(String key, Script script) {
            scripts.merge(key, List.of(script), (scripts, newScript) -> Stream.concat(scripts.stream(), newScript.stream()).toList());
            return this;
        }

        public Builder withScript(String key, String script, String condition) {
            return withScript(key, new Script(Either.left(script), Optional.of(condition)));
        }

        public Builder withScript(String key, String script) {
            return withScript(key, new Script(Either.left(script), Optional.empty()));
        }

        public Builder withScript(String key, List<String> scripts) {
            return withScript(key, new Script(Either.right(List.copyOf(scripts)), Optional.empty()));
        }

        public Builder withRenderController(String controller) {
            renderControllers.add(controller);
            return this;
        }

        public BedrockAttachable build() {
            return new BedrockAttachable(PackConstants.ENGINE_VERSION,
                    new AttachableInfo(identifier, Map.copyOf(materials), Map.copyOf(textures), Map.copyOf(geometries), Map.copyOf(animations),
                            new Scripts(Map.copyOf(scripts)), List.copyOf(renderControllers)));
        }
    }

    public record AttachableInfo(Identifier identifier, Map<String, String> materials, Map<String, String> textures,
                                 Map<String, String> geometry, Map<String, String> animations, Scripts scripts,
                                 List<String> renderControllers) {
        private static final Codec<Map<String, String>> STRING_MAP_CODEC = Codec.unboundedMap(Codec.STRING, Codec.STRING);
        public static final Codec<AttachableInfo> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Identifier.CODEC.fieldOf("identifier").forGetter(AttachableInfo::identifier),
                        Codec.unboundedMap(Codec.STRING, Codec.STRING).fieldOf("materials").forGetter(AttachableInfo::materials),
                        Codec.unboundedMap(Codec.STRING, Codec.STRING).fieldOf("textures").forGetter(AttachableInfo::textures),
                        Codec.unboundedMap(Codec.STRING, Codec.STRING).fieldOf("geometry").forGetter(AttachableInfo::geometry),
                        STRING_MAP_CODEC.optionalFieldOf("animations", Map.of()).forGetter(AttachableInfo::animations),
                        Scripts.CODEC.optionalFieldOf("scripts", Scripts.EMPTY).forGetter(AttachableInfo::scripts),
                        Codec.STRING.listOf().optionalFieldOf("render_controllers", List.of()).forGetter(AttachableInfo::renderControllers)
                ).apply(instance, AttachableInfo::new)
        );
    }

    public record Scripts(Map<String, List<Script>> scripts) {
        public static final Codec<Scripts> CODEC = Codec.unboundedMap(Codec.STRING, ExtraCodecs.compactListCodec(Script.CODEC)).xmap(Scripts::new, Scripts::scripts);
        public static final Scripts EMPTY = new Scripts(Map.of());
    }

    // TODO clean this up
    public record Script(Either<String, List<String>> script, Optional<String> condition) {
        private static final Codec<Script> SCRIPT_WITH_CONDITION_CODEC = Codec.unboundedMap(Codec.STRING, Codec.STRING).flatXmap(
                scriptMap -> {
                    if (scriptMap.size() != 1) {
                        return DataResult.error(() -> "Script with condition must have exactly one key-value pair");
                    }
                    String script = scriptMap.keySet().iterator().next();
                    return DataResult.success(new Script(Either.left(script), Optional.of(scriptMap.get(script))));
                },
                script -> script.condition.map(condition -> DataResult.success(Map.of(script.script.left().orElseThrow(), condition)))
                        .orElse(DataResult.error(() -> "Script must have a condition"))
        );
        private static final Codec<Script> SCRIPT_LIST_CODEC = Codec.STRING.listOf().flatComapMap(scripts -> new Script(Either.right(scripts), Optional.empty()), script ->
                script.script.map(_ -> DataResult.error(() -> "Script must have a list of scripts"), DataResult::success));
        public static final Codec<Script> CODEC = SCRIPT_WITH_CONDITION_CODEC.mapResult(new Codec.ResultFunction<>() {
            @Override
            public <T> DataResult<Pair<Script, T>> apply(DynamicOps<T> ops, T input, DataResult<Pair<Script, T>> decoded) {
                if (decoded.isError()) {
                    decoded = SCRIPT_LIST_CODEC.decode(ops, input);
                    if (decoded.isError()) {
                        return Codec.STRING.map(script -> new Script(Either.left(script), Optional.empty())).decode(ops, input);
                    }
                }
                return decoded;
            }

            @Override
            public <T> DataResult<T> coApply(DynamicOps<T> ops, Script input, DataResult<T> encoded) {
                if (encoded.isError()) {
                    encoded = SCRIPT_LIST_CODEC.encodeStart(ops, input);
                    if (encoded.isError()) {
                        return input.script.map(script -> Codec.STRING.encodeStart(ops, script), _ -> DataResult.error(() -> "Script must be a single script"));
                    }
                }
                return encoded;
            }
        });
    }
}
