/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.mapeditor;

import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.mapeditor.rendering.ArcadeSprites;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser.ExtensionFilter;
import org.tinylog.Logger;

import java.io.File;
import java.text.MessageFormat;
import java.util.Map;
import java.util.ResourceBundle;

import static de.amr.pacmanfx.model.DefaultWorldMapPropertyName.*;
import static java.util.Objects.requireNonNull;

public interface EditorGlobals {

    int UPDATE_FREQ = 20; // Hz

    short TOOL_SIZE = 32;
    short MIN_GRID_SIZE = 8;
    short MAX_GRID_SIZE = 80;

    String SAMPLE_MAPS_PATH_PREFIX = "/de/amr/pacmanfx/mapeditor/maps/";
    String SAMPLE_MAP_PATH_PACMAN = SAMPLE_MAPS_PATH_PREFIX + "pacman/pacman.world";
    String SAMPLE_MAP_PATH_MS_PACMAN = SAMPLE_MAPS_PATH_PREFIX + "mspacman/mspacman_%d.world";
    String SAMPLE_MAP_PATH_MASONIC = SAMPLE_MAPS_PATH_PREFIX + "pacman_xxl/masonic_%d.world";

    ResourceBundle TEXT_BUNDLE = ResourceBundle.getBundle(TileMapEditor.class.getPackageName() + ".texts");

    Font FONT_TOOL_TIPS               = Font.font("Sans", FontWeight.NORMAL, 12);

    Font FONT_DROP_HINT               = Font.font("Sans", FontWeight.BOLD, 16);
    Font FONT_MESSAGE                 = Font.font("Sans", FontWeight.BOLD, 14);

    Color COLOR_PREVIEW_3D_OVERLAY    = Color.LIGHTGRAY;
    Font FONT_PREVIEW_3D_OVERLAY      = Font.font("Sans", FontWeight.SEMI_BOLD, 14);

    Font FONT_SOURCE_VIEW             = Font.font("Consolas", FontWeight.NORMAL, 14);
    String STYLE_SOURCE_VIEW          = "-fx-control-inner-background:#222; -fx-text-fill:#f0f0f0";

    Font FONT_STATUS_LINE_EDIT_MODE   = Font.font("Sans", FontWeight.BOLD, 14);
    Font FONT_STATUS_LINE_NORMAL      = Font.font("Sans", FontWeight.NORMAL, 14);

    Font FONT_SELECTED_PALETTE        = Font.font("Sans", FontWeight.BOLD, 14);
    Font FONT_UNSELECTED_PALETTE      = Font.font("Sans", FontWeight.NORMAL, 14);

    Color COLOR_CANVAS_BACKGROUND = Color.BLACK;

    Node NO_GRAPHIC = null;

    ExtensionFilter FILTER_WORLD_MAP_FILES = new ExtensionFilter("World Map", "*.world");
    ExtensionFilter FILTER_IMAGE_FILES     = new ExtensionFilter("Image", "*.bmp", "*.gif", "*.jpg", "*.png");
    ExtensionFilter FILTER_ALL_FILES       = new ExtensionFilter("Any File", "*.*");

    Map<String, RectShort> ACTOR_SPRITES = Map.of(
        POS_PAC,            ArcadeSprites.PAC_MAN,
        POS_GHOST_1_RED,    ArcadeSprites.RED_GHOST,
        POS_GHOST_2_PINK,   ArcadeSprites.PINK_GHOST,
        POS_GHOST_3_CYAN,   ArcadeSprites.CYAN_GHOST,
        POS_GHOST_4_ORANGE, ArcadeSprites.ORANGE_GHOST,
        POS_BONUS,          ArcadeSprites.STRAWBERRY
    );

    static boolean matchesExtensionFilter(File file, ExtensionFilter filter) {
        requireNonNull(file);
        requireNonNull(filter);
        if (!file.isFile()) return false;
        String fileNameLC = file.getName().toLowerCase();
        return filter.getExtensions().stream()
            .map(ext -> ext.replace("*.", ""))
            .map(String::toLowerCase)
            .anyMatch(fileNameLC::endsWith);
    }

    static boolean isImageFile(File file) {
        return matchesExtensionFilter(file, FILTER_IMAGE_FILES);
    }

    static boolean isWorldMapFile(File file) {
        return matchesExtensionFilter(file, FILTER_WORLD_MAP_FILES);
    }

    static String translated(String key, Object... args) {
        try {
            return MessageFormat.format(TEXT_BUNDLE.getString(key), args);
        }
        catch (Exception x) {
            Logger.error("Error processing translated text with key {} from bundle {}", key, TEXT_BUNDLE);
            return "[%s]".formatted(key);
        }
    }
}