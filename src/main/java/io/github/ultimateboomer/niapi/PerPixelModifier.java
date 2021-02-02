package io.github.ultimateboomer.niapi;

import net.minecraft.client.texture.NativeImage;

/**
 * NativeImage modifier that applies for each pixel.
 */
@FunctionalInterface
public interface PerPixelModifier extends NativeImageModifier {
    int applyPixel(int color, int x, int y, int width, int height);

    @Override
    default void apply(NativeImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                int color = applyPixel(image.getPixelColor(x, y), x, y, width, height);
                image.setPixelColor(x, y, color);
            }
        }
    }
}
