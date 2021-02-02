package io.github.ultimateboomer.niapi;

import net.minecraft.client.texture.NativeImage;

import java.util.ArrayDeque;
import java.util.Queue;

public class NativeImageBuilder {
    private NativeImage image;
    private Queue<NativeImageModifier> modifierStack = new ArrayDeque<>();

    public NativeImageBuilder(NativeImage image) {
        this.image = image;
    }

    /**
     * Get the NativeImage instance before any modifiers are applied
     *
     * @return image
     */
    public NativeImage getImage() {
        return image;
    }

    /**
     * Apply all modifiers and return the result image
     *
     * @return image
     */
    public NativeImage getResultImage() {
        modifierStack.forEach(modifier -> modifier.apply(image));
        return image;
    }

    /**
     * @param modifier
     */
    public NativeImageBuilder addModifier(NativeImageModifier modifier) {
        this.modifierStack.add(modifier);
        return this;
    }

    public NativeImageBuilder addModifier(PerPixelModifier modifier) {
        this.modifierStack.add(modifier);
        return this;
    }
}
