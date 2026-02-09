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

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CheckSpriteSheetColors {

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
        boolean foundIllegalColor;
        final Set<Color> NES_colors = Stream.of(NES_Palette.COLORS).map(Color::valueOf).collect(Collectors.toSet());

        Logger.info("Checking non-Arcade maps spritesheet");
        foundIllegalColor = checkForIllegalColors(NonArcadeMapsSpriteSheet.instance().sourceImage(), NES_colors);
        Logger.info((foundIllegalColor ? "Found" : "Did not find") + " non-NES palette color");

        Logger.info("Checking Arcade maps spritesheet");
        foundIllegalColor = checkForIllegalColors(ArcadeMapsSpriteSheet.instance().sourceImage(), NES_colors);
        Logger.info((foundIllegalColor ? "Found" : "Did not find") + " non-NES palette color");
    }

    public static boolean checkForIllegalColors(Image image, Set<Color> legalColors) {
        boolean foundIllegalColor = false;
        final PixelReader reader = image.getPixelReader();
        for (int y = 0; y < image.getHeight(); ++y) {
            for (int x = 0; x < image.getWidth(); ++x) {
                final Color color = reader.getColor(x, y);
                if (color.equals(Color.TRANSPARENT)) continue;
                if (!legalColors.contains(color)) {
                    Logger.warn("Found illegal color {} at x={} y={}", color, x, y);
                    foundIllegalColor = true;
                }
            }
        }
        return foundIllegalColor;
    }
}
