package io.github.ultimateboomer.niapi;

import net.minecraft.client.texture.NativeImage;

/**
 * Basic functional interface for modifying native images
 */
@FunctionalInterface
public interface NativeImageModifier {
    void apply(NativeImage image);
}
