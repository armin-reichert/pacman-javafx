package de.amr.pacmanfx.tilemap.editor;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import org.tinylog.Logger;

import java.io.File;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.stream.Stream;

public class EditorGlobals {

    public static final short TOOL_SIZE = 32;
    public static final short MIN_GRID_SIZE = 8;
    public static final short MAX_GRID_SIZE = 80;

    public static final int EMPTY_ROWS_BEFORE_MAZE = 3;
    public static final int EMPTY_ROWS_BELOW_MAZE = 2;

    public static final byte PALETTE_ID_ACTORS  = 0;
    public static final byte PALETTE_ID_TERRAIN = 1;
    public static final byte PALETTE_ID_FOOD    = 2;

    public static final String SAMPLE_MAPS_PATH = "/de/amr/pacmanfx/tilemap/editor/maps/";

    public static final ResourceBundle TEXT_BUNDLE = ResourceBundle.getBundle(TileMapEditor.class.getPackageName() + ".texts");

    public static boolean isSupportedImageFile(File file) {
        return Stream.of(".bmp", ".gif", ".jpg", ".png").anyMatch(ext -> file.getName().toLowerCase().endsWith(ext));
    }

    public static boolean isWorldMapFile(File file) {
        return file.getName().toLowerCase().endsWith(".world");
    }

    public static final Font FONT_DROP_HINT               = Font.font("Sans", FontWeight.BOLD, 16);
    public static final Font FONT_MESSAGE                 = Font.font("Sans", FontWeight.BOLD, 14);
    public static final Font FONT_SOURCE_VIEW             = Font.font("Consolas", FontWeight.NORMAL, 14);
    public static final Font FONT_STATUS_LINE_EDIT_MODE   = Font.font("Sans", FontWeight.BOLD, 18);
    public static final Font FONT_STATUS_LINE_NORMAL      = Font.font("Sans", FontWeight.NORMAL, 14);

    public static final Color COLOR_CANVAS_BACKGROUND = Color.BLACK;

    public static final Node NO_GRAPHIC = null;

    public static final FileChooser.ExtensionFilter FILTER_WORLD_MAP_FILES = new FileChooser.ExtensionFilter("World Map Files", "*.world");
    public static final FileChooser.ExtensionFilter FILTER_IMAGE_FILES     = new FileChooser.ExtensionFilter("Image Files", "*.bmp", "*.gif", "*.jpg", "*.png");
    public static final FileChooser.ExtensionFilter FILTER_ALL_FILES       = new FileChooser.ExtensionFilter("All Files", "*.*");

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
