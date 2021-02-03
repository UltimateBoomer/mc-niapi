package io.github.ultimateboomer.niapi;

import net.minecraft.client.texture.NativeImage;
import org.lwjgl.stb.STBImage;
import org.lwjgl.stb.STBImageResize;
import org.lwjgl.system.MemoryUtil;

public final class NativeImageUtil {
    /**
     * Scale NativeImage using {@link STBImageResize}.
     *
     * @param image input image
     * @param mul multiplier
     */
    public static void scaleImage(NativeImage image, double mul) {
        scaleImage(image, mul, mul);
    }

    /**
     * Scale NativeImage using {@link STBImageResize}.
     *
     * @param image input image
     * @param widthMul width multiplier
     * @param heightMul height multiplier
     */
    public static void scaleImage(NativeImage image, double widthMul, double heightMul) {
        if (widthMul == 1.0 && heightMul == 1.0) {
            return;
        }

        // Prepare new image attributes
        int newWidth = (int) (image.width * widthMul);
        int newHeight = (int) (image.height * heightMul);
        long newSize = (long) newWidth * newHeight * image.getFormat().getChannelCount();
        long newPointer = MemoryUtil.nmemAlloc(newSize);;

        // Upscale with STBImageResize
        STBImageResize.nstbir_resize_uint8(image.pointer, image.getWidth(), image.getHeight(), 0, newPointer,
                newWidth, newHeight, 0, image.getFormat().getChannelCount());

        // Free old pointer
        if (image.isStbImage) {
            STBImage.nstbi_image_free(image.pointer);
        } else {
            MemoryUtil.nmemFree(image.pointer);
        }

        // Update attributes
        image.pointer = newPointer;
        image.width = newWidth;
        image.height = newHeight;
        image.sizeBytes = newSize;
    }

    /**
     * Scale NativeImage using nearest neighbor interpolation.
     *
     * @param image input image
     * @param mul multiplier
     */
    public static void scaleImageNearest(NativeImage image, double mul) {
        scaleImageNearest(image, mul, mul);
    }

    /**
     * Scale NativeImage using nearest neighbor interpolation.
     *
     * @param image input image
     * @param widthMul width multiplier
     * @param heightMul height multiplier
     */
    public static void scaleImageNearest(NativeImage image, double widthMul, double heightMul) {
        if (widthMul == 1.0 && heightMul == 1.0) {
            return;
        }

        // Make copy of original image
        NativeImage copy = copyImage(image);

        // Update image attributes
        image.width *= widthMul;
        image.height *= heightMul;
        image.sizeBytes = (long) image.getWidth() * image.getHeight() * image.getFormat().getChannelCount();
        image.pointer = MemoryUtil.nmemAlloc(image.sizeBytes);

        if (image.getWidth() * image.getHeight() > copy.getWidth() * copy.getHeight()) {
            // Scaled image is larger than original image
            for (int x = 0; x < copy.getWidth(); ++x) {
                for (int y = 0; y < copy.getHeight(); ++y) {
                    for (int x1 = (int) (x * widthMul); x1 < (x + 1) * widthMul; ++x1) {
                        for (int y1 = (int) (y * heightMul); y1 < (y + 1) * heightMul; ++y1) {
                            image.setPixelColor(x1, y1, copy.getPixelColor(x, y));
                        }
                    }
                }
            }
        } else {
            // Scaled image is smaller than original image
            for (int x = 0; x < image.getWidth(); ++x) {
                for (int y = 0; y < image.getHeight(); ++y) {
                    image.setPixelColor(x, y, copy.getPixelColor((int) (x / widthMul), (int) (y / heightMul)));
                }
            }
        }

        // Free old pointer
        copy.close();
    }

    /**
     * Replace the contents of the image with the other image, tiling if needed.
     *
     * @param image target image
     * @param replacer replacer image
     * @param keepTransparency whether to keep original transparency
     */
    public static void replaceImage(NativeImage image, NativeImage replacer, boolean keepTransparency) {
        for (int x = 0; x < image.getWidth(); ++x) {
            for (int y = 0; y < image.getHeight(); ++y) {
                int color = replacer.getPixelColor(x % replacer.getWidth(), y % replacer.getHeight());
                if (keepTransparency) {
                    color &= 0xFFFFFF;
                    color += image.getPixelOpacity(x, y) << 24;
                }
                image.setPixelColor(x, y, color);
            }
        }
    }

    /**
     * Clone the NativeImage object (a new pointer is created).
     *
     * @param image
     * @return cloned image
     */
    public static NativeImage cloneImage(NativeImage image) {
        long newPointer = MemoryUtil.nmemAlloc(image.sizeBytes);
        MemoryUtil.memCopy(image.pointer, newPointer, image.sizeBytes);
        return new NativeImage(image.getFormat(), image.getWidth(), image.getHeight(), image.isStbImage, newPointer);
    }

    /**
     * Make a copy of the NativeImage object with the same pointer
     *
     * @param image
     * @return copied image
     */
    public static NativeImage copyImage(NativeImage image) {
        return new NativeImage(image.getFormat(), image.getWidth(), image.getHeight(), image.isStbImage, image.pointer);
    }
}
