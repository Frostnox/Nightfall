package frostnox.nightfall.data;

import com.mojang.logging.LogUtils;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.*;

/**
 * Data provider for png and mcmeta files.
 * Place files in the "textures" folder located in the project's root folder to exclude files from the jar file.
 * The folder's structure must be identical to how it appears in the resources folder.
 */

/*
 * This file is exempt from Nightfall's standard licensing and is released under the Creative Commons Zero v1.0 Universal license.
 * - Frostnox
 */
public abstract class TextureProvider implements DataProvider {
    private static final Logger LOGGER = LogUtils.getLogger();
    protected final String temp;
    protected final DataGenerator generator;
    protected final Map<ResourceLocation, BufferedImage> images = new LinkedHashMap<>();
    protected final Map<ResourceLocation, Path> mcmetaPaths = new LinkedHashMap<>();
    protected final String modId, inputPath, externalPath;
    protected final ExistingFileHelper existingFileHelper;
    private final ExistingFileHelper.IResourceType resourceType;

    protected TextureProvider(DataGenerator pGenerator, String modId, @Nullable ExistingFileHelper existingFileHelper) {
        this.generator = pGenerator;
        this.modId = modId;
        String outputString = generator.getOutputFolder().toString();
        this.inputPath = outputString.substring(0, outputString.lastIndexOf("\\generated\\")) + "\\main\\resources\\assets\\";
        this.externalPath = outputString.substring(0, outputString.lastIndexOf("\\src\\")) + "\\textures\\";
        this.existingFileHelper = existingFileHelper;
        this.resourceType = new ExistingFileHelper.ResourceType(PackType.CLIENT_RESOURCES, ".png", "textures");
        temp = "_" + modId + "_texture_provider_temporary";
    }

    protected abstract void addTextures();

    protected void add(ResourceLocation loc, ResourceLocation baseImageLoc, BufferedImage image) {
        if(images.put(loc, image) != null) {
            throw new IllegalStateException("Duplicate image location " + loc);
        }
        Path metaPath = findMetaPath(baseImageLoc);
        if(Files.exists(metaPath)) mcmetaPaths.put(loc, metaPath);
    }

    protected Path getExternalImagePath(ResourceLocation loc) {
        return generator.getOutputFolder().getFileSystem().getPath(externalPath + loc.getPath() + ".png");
    }

    protected Path getExistingImagePath(ResourceLocation loc) {
        return generator.getOutputFolder().getFileSystem().getPath(inputPath + loc.getNamespace() + "\\textures\\" + loc.getPath() + ".png");
    }

    private Path getPath(ResourceLocation loc) {
        return generator.getOutputFolder().resolve("assets/" + loc.getNamespace() + "/textures/" + loc.getPath() + ".png");
    }

    private Path getExternalImageMetaPath(ResourceLocation loc) {
        return generator.getOutputFolder().getFileSystem().getPath(externalPath + loc.getPath() + ".png.mcmeta");
    }

    private Path getExistingImageMetaPath(ResourceLocation loc) {
        return generator.getOutputFolder().getFileSystem().getPath(inputPath + loc.getNamespace() + "\\textures\\" + loc.getPath() + ".png.mcmeta");
    }

    private Path findMetaPath(ResourceLocation loc) {
        if(mcmetaPaths.containsKey(loc)) return mcmetaPaths.get(loc);
        else {
            Path path = getExternalImageMetaPath(loc);
            if(Files.exists(path)) return path;
            else return getExistingImageMetaPath(loc);
        }
    }

    protected BufferedImage getImage(ResourceLocation loc) {
        if(images.containsKey(loc)) return deepCopyImage(loc);
        BufferedImage image;
        String externalPath = getExternalImagePath(loc).toString();
        try {
            image = ImageIO.read(new File(externalPath));
        }
        catch (IOException ioException) {
            String inputPath = getExistingImagePath(loc).toString();
            try {
                image = ImageIO.read(new File(inputPath));
            }
            catch (IOException ioException2) {
                throw new IllegalStateException("Couldn't read image at path " + inputPath, ioException2);
            }
        }
        return image;
    }

    private BufferedImage deepCopyImage(ResourceLocation loc) {
        BufferedImage copy = new BufferedImage(images.get(loc).getWidth(), images.get(loc).getHeight(), images.get(loc).getType());
        Graphics2D graphics = copy.createGraphics();
        graphics.drawImage(images.get(loc), 0, 0, null);
        graphics.dispose();
        return copy; //Deep copy to make image safe to use as a base
    }

    protected record PixelData(int x, int y, int color, int alpha) {}

    /**
     * Create blank image with specified size
     */
    protected void blankImage(ResourceLocation loc, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        if(images.put(loc, image) != null) {
            throw new IllegalStateException("Duplicate image location " + loc);
        }
    }

    protected void blankImage(ResourceLocation loc) {
        blankImage(loc, 16, 16);
    }

    /**
     * Colorize image with simple multiplication for each color channel
     */
    protected void colorizeImageRGB(ResourceLocation loc, ResourceLocation baseImageLoc, Color color) {
        BufferedImage image = getImage(baseImageLoc);
        for(int x = 0; x < image.getWidth(); x++) {
            for(int y = 0; y < image.getHeight(); y++) {
                int rgb = image.getRGB(x, y);
                int alpha = (rgb & 0xff000000) >>> 24;
                if(alpha == 0) continue;
                float fAlpha = alpha / 255F;
                float red = (((rgb & 0xff0000) >> 16) / 255F) * (color.getRed() / 255F);
                float green = (((rgb & 0xff00) >> 8) / 255F) * (color.getGreen() / 255F);
                float blue = ((rgb & 0xff) / 255F) * (color.getBlue() / 255F);
                image.setRGB(x, y, new Color(red, green, blue, fAlpha).getRGB());
            }
        }
        add(loc, baseImageLoc, image);
    }

    /**
     * @param degrees rotation degrees in intervals of 90
     */
    protected void rotateImage(ResourceLocation loc, ResourceLocation baseImageLoc, int degrees) {
        BufferedImage baseImage = getImage(baseImageLoc);
        BufferedImage image = new BufferedImage(baseImage.getWidth(), baseImage.getHeight(), baseImage.getType());
        Graphics2D graphics = image.createGraphics();
        graphics.rotate(Math.toRadians(degrees), image.getWidth() / 2, image.getHeight() / 2);
        graphics.drawImage(baseImage, null, 0, 0);
        graphics.dispose();
        add(loc, baseImageLoc, image);
    }

    /**
     * Replaces the grayscale pixels in an image with provided color palette
     * @param palettes new color palettes to replace with in order of darkest to brightest (preserves original alpha)
     */
    @SafeVarargs
    protected final void replaceImagePalette(ResourceLocation loc, ResourceLocation baseImageLoc, List<Integer>... palettes) {
        BufferedImage image = getImage(baseImageLoc);
        List<Integer> newColors = new ArrayList<>();
        for(List<Integer> palette : palettes) {
            if(palette == null) continue;
            newColors.addAll(palette);
        }
        List<Integer> grayColors = new ArrayList<>();
        List<PixelData> grayPixels = new ArrayList<>();
        //Collect gray colors
        for(int x = 0; x < image.getWidth(); x++) {
            for(int y = 0; y < image.getHeight(); y++) {
                int rgb = image.getRGB(x, y);
                int alpha = (rgb & 0xff000000) >>> 24;
                if(alpha == 0) continue;
                int red = (rgb & 0xff0000) >> 16;
                int green = (rgb & 0xff00) >> 8;
                int blue = rgb & 0xff;
                if(red == green && green == blue) {
                    if(!grayColors.contains(blue)) grayColors.add(blue);
                    grayPixels.add(new PixelData(x, y, blue, alpha));
                }
            }
        }
        if(grayColors.size() != newColors.size()) throw new IllegalStateException("Palette size " + newColors.size() + " of " + loc + " does not match " + grayColors.size() + " unique gray colors in " + baseImageLoc);
        grayColors.sort(Integer::compareTo); //Sort from darkest to brightest
        HashMap<Integer, Integer> paletteMap = new LinkedHashMap<>();
        for(int i = 0; i < newColors.size(); i++) paletteMap.put(grayColors.get(i), (newColors.get(i) << 8) >>> 8);
        //Replace gray colors using palette map
        for(PixelData pixel : grayPixels) {
            image.setRGB(pixel.x, pixel.y, (pixel.alpha << 24) | paletteMap.get(pixel.color));
        }
        add(loc, baseImageLoc, image);
    }

    /**
     * Stacks the layer image on top of the base image, allows tiling for base image if it is smaller
     */
    protected void layerImages(ResourceLocation loc, ResourceLocation baseImageLoc, ResourceLocation layerImageLoc) {
        BufferedImage baseImage = getImage(baseImageLoc);
        BufferedImage layerImage = getImage(layerImageLoc);
        if(layerImage.getWidth() % baseImage.getWidth() != 0 || layerImage.getHeight() % baseImage.getHeight() != 0) {
            throw new IllegalStateException("Base image " + baseImageLoc + " and layer image " + layerImageLoc + " do not have compatible dimensions");
        }
        for(int x = 0; x < layerImage.getWidth(); x++) {
            for(int y = 0; y < layerImage.getHeight(); y++) {
                int rgbLayer = layerImage.getRGB(x, y);
                float alphaL = ((rgbLayer & 0xff000000) >>> 24) / 255F;
                float redL = (((rgbLayer & 0xff0000) >> 16) / 255F);
                float greenL = (((rgbLayer & 0xff00) >> 8) / 255F);
                float blueL = ((rgbLayer & 0xff) / 255F);

                int rgbBase = baseImage.getRGB(x % baseImage.getWidth(), y % baseImage.getHeight());
                float alphaB = ((rgbBase & 0xff000000) >>> 24) / 255F;
                if(alphaB == 0F && alphaL == 0F) continue;
                float redB = (((rgbBase & 0xff0000) >> 16) / 255F);
                float greenB = (((rgbBase & 0xff00) >> 8) / 255F);
                float blueB = ((rgbBase & 0xff) / 255F);

                float alpha = 1 - (1 - alphaL) * (1 - alphaB);
                float red = redL * alphaL / alpha + redB * alphaB * (1 - alphaL) / alpha;
                float green = greenL * alphaL / alpha + greenB * alphaB * (1 - alphaL) / alpha;
                float blue = blueL * alphaL / alpha + blueB * alphaB * (1 - alphaL) / alpha;
                layerImage.setRGB(x, y, new Color(red, green, blue, alpha).getRGB());
            }
        }
        add(loc, layerImageLoc, layerImage);
    }

    /**
     * Separates the base image into two new images: one containing all grayscale colors and one containing all others.
     * Saturation values must be 0 through 1, may wrap around (so 0.9 to 0.1 is a valid range), and are inclusive.
     */
    protected void splitImageBySaturation(float minSaturation, float maxSaturation, ResourceLocation incImageLoc, ResourceLocation excImageLoc, ResourceLocation baseImageLoc) {
        if(minSaturation < 0F || minSaturation > 1F || maxSaturation < 0F || maxSaturation > 1F) {
            throw new IllegalArgumentException("Saturation threshold values must be between 0 and 1, but min was " + minSaturation + " and max was " + maxSaturation);
        }
        BufferedImage baseImage = getImage(baseImageLoc);
        BufferedImage grayImage = new BufferedImage(baseImage.getWidth(), baseImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        BufferedImage colorImage = new BufferedImage(baseImage.getWidth(), baseImage.getHeight(), BufferedImage.TYPE_INT_ARGB);

        List<PixelData> grayPixels = new ArrayList<>();
        List<PixelData> colorPixels = new ArrayList<>();
        //Separate pixels
        for(int x = 0; x < baseImage.getWidth(); x++) {
            for(int y = 0; y < baseImage.getHeight(); y++) {
                int rgb = baseImage.getRGB(x, y);
                int alpha = (rgb & 0xff000000) >>> 24;
                if(alpha == 0) continue;
                int red = (rgb & 0xff0000) >> 16;
                int green = (rgb & 0xff00) >> 8;
                int blue = rgb & 0xff;
                //Convert RGB to HSB
                float[] hsb = new float[3];
                Color.RGBtoHSB(red, green, blue, hsb);
                if(minSaturation <= maxSaturation) {
                    if(hsb[1] >= minSaturation && hsb[1] <= maxSaturation) grayPixels.add(new PixelData(x, y, rgb, alpha));
                    else colorPixels.add(new PixelData(x, y, rgb, alpha));
                }
                else {
                    if(hsb[1] >= minSaturation || hsb[1] <= maxSaturation) grayPixels.add(new PixelData(x, y, rgb, alpha));
                    else colorPixels.add(new PixelData(x, y, rgb, alpha));
                }
            }
        }
        //Draw new images
        for(PixelData pixel : grayPixels) {
            grayImage.setRGB(pixel.x, pixel.y, pixel.color);
        }
        for(PixelData pixel : colorPixels) {
            colorImage.setRGB(pixel.x, pixel.y, pixel.color);
        }

        add(incImageLoc, baseImageLoc, grayImage);
        add(excImageLoc, baseImageLoc, colorImage);
    }

    @Override
    public void run(HashCache pCache) {
        images.clear();
        addTextures();
        images.forEach((loc, image) -> {
            if(loc.getPath().contains(temp)) return;
            if(existingFileHelper != null && existingFileHelper.exists(loc, resourceType)) {
                throw new IllegalArgumentException("Couldn't create image since texture already exists at location " + loc.toString());
            }
            else {
                Path path = getPath(loc);
                try {
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    ImageIO.write(image, "png", bytes);
                    String hash = SHA1.hashBytes(bytes.toByteArray()).toString();
                    if(!Files.exists(path) || !Objects.equals(pCache.getHash(path), hash)) {
                        Files.createDirectories(path.getParent());
                        try {
                            ImageIO.write(image, "png", path.toFile());
                        }
                        catch (IOException ioException) {
                            LOGGER.error("Failed to write image to {}", path, ioException);
                        }
                    }
                    pCache.putNew(path, hash);
                }
                catch (IOException ioException) {
                    LOGGER.error("Couldn't save textures to {}", path, ioException);
                }

                if(mcmetaPaths.containsKey(loc)) {
                    try {
                        Path metaTarget = Path.of(path + ".mcmeta");
                        Path metaSource = mcmetaPaths.get(loc);
                        String hash = SHA1.hashBytes(Files.readAllBytes(metaSource)).toString();
                        if(!Files.exists(metaTarget) || !Objects.equals(pCache.getHash(metaTarget), hash)) {
                            Files.copy(metaSource, metaTarget, StandardCopyOption.REPLACE_EXISTING);
                        }
                        pCache.putNew(metaTarget, hash);
                    }
                    catch (IOException ioException) {
                        LOGGER.error("Couldn't copy mcmeta file to {}", Path.of(path + ".mcmeta"), ioException);
                    }
                }
            }
            if(existingFileHelper != null) existingFileHelper.trackGenerated(loc, resourceType);
        });
    }

    @Override
    public String getName() {
        return "Textures";
    }

    protected ResourceLocation block(RegistryObject<?> block) {
        return ResourceLocation.fromNamespaceAndPath(modId, "block/" + block.getId().getPath());
    }

    protected ResourceLocation block(RegistryObject<?> block, String suffix) {
        return ResourceLocation.fromNamespaceAndPath(modId, "block/" + block.getId().getPath() + suffix);
    }

    protected ResourceLocation block(String name) {
        return ResourceLocation.fromNamespaceAndPath(modId, "block/" + name);
    }

    protected ResourceLocation tempBlock(RegistryObject<?> block) {
        return block(block, temp);
    }

    protected ResourceLocation tempBlock(RegistryObject<?> block, String suffix) {
        return block(block, temp + suffix);
    }

    protected ResourceLocation tempBlock(String name) {
        return block(name + temp);
    }

    protected ResourceLocation item(RegistryObject<?> item) {
        return ResourceLocation.fromNamespaceAndPath(modId, "item/" + item.getId().getPath());
    }

    protected ResourceLocation item(RegistryObject<?> item, String suffix) {
        return ResourceLocation.fromNamespaceAndPath(modId, "item/" + item.getId().getPath() + suffix);
    }

    protected ResourceLocation item(String name) {
        return ResourceLocation.fromNamespaceAndPath(modId, "item/" + name);
    }

    protected ResourceLocation tempItem(RegistryObject<?> item) {
        return item(item, temp);
    }

    protected ResourceLocation tempItem(RegistryObject<?> item, String suffix) {
        return item(item, temp + suffix);
    }

    protected ResourceLocation tempItem(String name) {
        return item(name + temp);
    }

    protected ResourceLocation entity(RegistryObject<?> entity, String folder) {
        return ResourceLocation.fromNamespaceAndPath(modId, "entity/" + folder + "/" + entity.getId().getPath());
    }

    protected ResourceLocation entity(RegistryObject<?> entity, String folder, String suffix) {
        return ResourceLocation.fromNamespaceAndPath(modId, "entity/" + folder + "/" + entity.getId().getPath() + suffix);
    }

    protected ResourceLocation entity(String name) {
        return ResourceLocation.fromNamespaceAndPath(modId, "entity/" + name);
    }

    protected ResourceLocation tempEntity(RegistryObject<?> entity, String folder) {
        return entity(entity, folder, temp);
    }

    protected ResourceLocation tempEntity(RegistryObject<?> entity, String folder, String suffix) {
        return entity(entity, folder, temp + suffix);
    }

    protected ResourceLocation tempEntity(String name) {
        return entity(name + temp);
    }

    protected ResourceLocation blank(RegistryObject<?> object) {
        return ResourceLocation.fromNamespaceAndPath(modId, object.getId().getPath());
    }

    protected ResourceLocation blank(String name) {
        return ResourceLocation.fromNamespaceAndPath(modId, name);
    }
}
