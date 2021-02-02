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

//    /**
//     * Create a new NativeImage that is upscaled by a power of 2 using nearest neighbor scaling.
//     * Because of the scaling algorithm, the resulting image will look identical to the input.
//     *
//     * @param image input image
//     * @param power scaling power, must be positive
//     * @return scaled image
//     */
//    public static NativeImage upscaleImageNearest(NativeImage image, int power) {
//        int scale = 1 << power;
//        NativeImage newImage = new NativeImage(image.getFormat(),
//                image.getWidth() * scale, image.getHeight() * scale, false);
//        for (int x = 0; x < image.getWidth(); ++x) {
//            for (int y = 0; y < image.getHeight(); ++y) {
//                newImage.fillRect(x * scale, y * scale, scale, scale, image.getPixelColor(x, y));
//            }
//        }
//        image.close();
//        return newImage;
//    }
//
//    /**
//     * Create a new NativeImage that is downscaled by a power of 2 using nearest neighbor scaling.
//     * This method may produce a sharper result, but is less accurate.
//     *
//     * @param image input image
//     * @param power scaling power, must be positive
//     * @return scaled image
//     */
//    public static NativeImage downscaleImageNearest(NativeImage image, int power) {
//        int scale = 1 << power;
//        NativeImage newImage = new NativeImage(image.getFormat(),
//                image.getWidth() / scale, image.getHeight() / scale, false);
//        for (int x = 0; x < newImage.getWidth(); ++x) {
//            for (int y = 0; y < newImage.getHeight(); ++y) {
//                newImage.setPixelColor(x, y, image.getPixelColor(x * scale, y * scale));
//            }
//        }
//        image.close();
//        return newImage;
//    }
//
//    /**
//     * Create a new NativeImage that is downscaled by a power of 2 using linear scaling.
//     * Average luminosity is preserved.
//     * This is probably the best downscaling algorithm for most purposes.
//     *
//     * @param image input image
//     * @param power scaling power, must be positive
//     * @return scaled image
//     */
//    public static NativeImage downscaleImageLinear(NativeImage image, int power) {
//        int scale = 1 << power;
//        NativeImage newImage = new NativeImage(NativeImage.Format.ABGR,
//                image.getWidth() / scale, image.getHeight() / scale, false);
//        for (int x = 0; x < newImage.getWidth(); ++x) {
//            for (int y = 0; y < newImage.getHeight(); ++y) {
//                int a = 0xFF;
//                long r = 0;
//                long g = 0;
//                long b = 0;
//                ByteBuffer buffer = ByteBuffer.allocate(4);
//                for (int x1 = 0; x1 < scale; ++x1) {
//                    for (int y1 = 0; y1 < scale; ++y1) {
//                        buffer.putInt(image.getPixelColor(x * scale + x1, y * scale + y1));
//                        a = Math.min(a, buffer.get(0));
//                        r += Math.pow(Byte.toUnsignedInt(buffer.get(1)), 2);
//                        g += Math.pow(Byte.toUnsignedInt(buffer.get(2)), 2);
//                        b += Math.pow(Byte.toUnsignedInt(buffer.get(3)), 2);
//                        buffer.clear();
//                    }
//                }
//                double n = scale * scale;
//                int newColor = (a << 24)
//                        + (MathHelper.floor(Math.sqrt(r / n)) << 16)
//                        + (MathHelper.floor(Math.sqrt(g / n)) << 8)
//                        + MathHelper.floor(Math.sqrt(b / n));
//
//                newImage.setPixelColor(x, y, newColor);
//            }
//        }
//        image.close();
//        return newImage;
//    }

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
