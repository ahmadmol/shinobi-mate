package com.group_finity.mascot.ui;

import java.awt.Image;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/** Shared official artwork. PNG resources work with standard Java ImageIO. */
public final class AppIcon {
    private static final List<Image> IMAGES = load();

    private AppIcon() { }

    private static List<Image> load() {
        List<Image> images = new ArrayList<>();
        for (int size : new int[] {16, 32, 48, 64, 128, 256}) {
            String path = "/icons/ShinobiMate-" + size + ".png";
            try (var stream = AppIcon.class.getResourceAsStream(path)) {
                BufferedImage image = stream == null ? null : ImageIO.read(stream);
                if (image != null) images.add(image);
            } catch (IOException ex) {
                Logger.getLogger(AppIcon.class.getName()).log(Level.WARNING, "Cannot load " + path, ex);
            }
        }
        return List.copyOf(images);
    }

    public static void apply(Window window) {
        if (!IMAGES.isEmpty()) window.setIconImages(IMAGES);
    }

    /** Pick the smallest source at least as large as the tray's physical size. */
    public static BufferedImage trayImage(int size) {
        for (Image image : IMAGES) {
            if (image.getWidth(null) >= size) return (BufferedImage) image;
        }
        return IMAGES.isEmpty() ? null : (BufferedImage) IMAGES.getLast();
    }
}
