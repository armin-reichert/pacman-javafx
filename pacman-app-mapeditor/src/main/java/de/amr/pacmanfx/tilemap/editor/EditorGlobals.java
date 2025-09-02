/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tilemap.editor;

import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.tilemap.editor.rendering.ArcadeSprites;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser.ExtensionFilter;
import org.tinylog.Logger;

import java.io.File;
import java.text.MessageFormat;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import static de.amr.pacmanfx.model.WorldMapProperty.*;
import static java.util.Objects.requireNonNull;

public class EditorGlobals {

    public static final int UPDATE_FREQ = 30; // Hz

    public static final short TOOL_SIZE = 32;
    public static final short MIN_GRID_SIZE = 8;
    public static final short MAX_GRID_SIZE = 80;

    public static final String SAMPLE_MAPS_PATH = "/de/amr/pacmanfx/tilemap/editor/maps/";

    public static final ResourceBundle TEXT_BUNDLE = ResourceBundle.getBundle(TileMapEditor.class.getPackageName() + ".texts");

    public static final Font FONT_DROP_HINT               = Font.font("Sans", FontWeight.BOLD, 16);
    public static final Font FONT_MESSAGE                 = Font.font("Sans", FontWeight.BOLD, 14);

    public static final Color COLOR_PREVIEW_3D_OVERLAY    = Color.LIGHTGRAY;
    public static final Font FONT_PREVIEW_3D_OVERLAY      = Font.font("Sans", FontWeight.SEMI_BOLD, 14);

    public static final Font FONT_SOURCE_VIEW             = Font.font("Consolas", FontWeight.NORMAL, 14);
    public static final String STYLE_SOURCE_VIEW          = "-fx-control-inner-background:#222; -fx-text-fill:#f0f0f0";

    public static final Font FONT_STATUS_LINE_EDIT_MODE   = Font.font("Sans", FontWeight.BOLD, 16);
    public static final Font FONT_STATUS_LINE_NORMAL      = Font.font("Sans", FontWeight.NORMAL, 14);

    public static final Font FONT_SELECTED_PALETTE        = Font.font("Sans", FontWeight.BOLD, 14);
    public static final Font FONT_UNSELECTED_PALETTE      = Font.font("Sans", FontWeight.NORMAL, 14);

    public static final Color COLOR_CANVAS_BACKGROUND = Color.BLACK;

    public static final Node NO_GRAPHIC = null;

    public static final ExtensionFilter FILTER_WORLD_MAP_FILES = new ExtensionFilter("World Map", "*.world");
    public static final ExtensionFilter FILTER_IMAGE_FILES     = new ExtensionFilter("Image", "*.bmp", "*.gif", "*.jpg", "*.png");
    public static final ExtensionFilter FILTER_ALL_FILES       = new ExtensionFilter("Any File", "*.*");

    public static final Map<String, RectShort> ACTOR_SPRITES = Map.of(
        POS_PAC,          ArcadeSprites.PAC_MAN,
        POS_RED_GHOST,    ArcadeSprites.RED_GHOST,
        POS_PINK_GHOST,   ArcadeSprites.PINK_GHOST,
        POS_CYAN_GHOST,   ArcadeSprites.CYAN_GHOST,
        POS_ORANGE_GHOST, ArcadeSprites.ORANGE_GHOST,
        POS_BONUS,        ArcadeSprites.STRAWBERRY
    );

    public static boolean matchesExtensionFilter(File file, ExtensionFilter filter) {
        requireNonNull(file);
        requireNonNull(filter);
        if (!file.isFile()) return false;
        String fileNameLC = file.getName().toLowerCase();
        return filter.getExtensions().stream()
            .map(ext -> ext.replace("*.", ""))
            .map(String::toLowerCase)
            .anyMatch(fileNameLC::endsWith);
    }

    public static boolean isImageFile(File file) {
        return matchesExtensionFilter(file, FILTER_IMAGE_FILES);
    }

    public static boolean isWorldMapFile(File file) {
        return matchesExtensionFilter(file, FILTER_WORLD_MAP_FILES);
    }

    public static String translated(String key, Object... args) {
        try {
            return MessageFormat.format(TEXT_BUNDLE.getString(key), args);
        }
        catch (MissingResourceException x) {
            Logger.error("No resource with key {} found in {}", key, TEXT_BUNDLE);
            return "[%s]".formatted(key);
        }
    }

}
