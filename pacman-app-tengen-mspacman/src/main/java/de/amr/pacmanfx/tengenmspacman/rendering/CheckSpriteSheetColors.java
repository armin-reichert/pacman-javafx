/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.rendering;

import de.amr.pacmanfx.lib.nes.NES_Palette;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CheckSpriteSheetColors {

    public record Pixel(int x, int y, Color color) {}

    public static void main(String[] args) {
        Platform.startup(() ->  {
            Logger.info("Starting JavaFX");
        });
        Platform.runLater(() -> {
            runChecks();
            Platform.exit();
        });
    }

    private static void runChecks() {
        final Set<Color> NES_colors = Stream.of(NES_Palette.COLORS).map(Color::valueOf).collect(Collectors.toSet());

        Logger.info("Checking non-Arcade maps spritesheet");
        checkForIllegalPixels(NonArcadeMapsSpriteSheet.instance().sourceImage(), NES_colors);

        Logger.info("Checking Arcade maps spritesheet");
        checkForIllegalPixels(ArcadeMapsSpriteSheet.instance().sourceImage(), NES_colors);
    }

    private static void checkForIllegalPixels(Image image, Set<Color> legalColors) {
        var pixels = checkForIllegalColors(ArcadeMapsSpriteSheet.instance().sourceImage(), legalColors);
        if (!pixels.isEmpty()) {
            Logger.info("Found illegal pixels");
            printPixels(pixels);
        } else {
            Logger.info("All pixels have legal colors");
        }
    }

    private static void printPixels(List<Pixel> pixels) {
        for (Pixel pixel : pixels) {
            Logger.info(pixel);
            Logger.info("\n");
        }
    }

    public static List<Pixel> checkForIllegalColors(Image image, Set<Color> legalColors) {
        final List<Pixel> pixels = new ArrayList<>();
        final PixelReader reader = image.getPixelReader();
        for (int y = 0; y < image.getHeight(); ++y) {
            for (int x = 0; x < image.getWidth(); ++x) {
                final Color color = reader.getColor(x, y);
                if (color.equals(Color.TRANSPARENT)) continue;
                if (!legalColors.contains(color)) {
                    pixels.add(new Pixel(x, y, color));
                }
            }
        }
        return pixels;
    }
}
