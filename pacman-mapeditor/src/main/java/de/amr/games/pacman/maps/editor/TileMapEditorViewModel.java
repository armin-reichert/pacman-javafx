/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.maps.editor;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ContextMenu;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * @author Armin Reichert
 */
public interface TileMapEditorViewModel {

    byte MIN_GRID_SIZE = 8;
    byte MAX_GRID_SIZE = 48;

    byte PALETTE_ID_ACTORS  = 0;
    byte PALETTE_ID_TERRAIN = 1;
    byte PALETTE_ID_FOOD    = 2;

    String PROPERTY_COLOR_FOOD               = "color_food";
    String PROPERTY_COLOR_WALL_STROKE        = "color_wall_stroke";
    String PROPERTY_COLOR_WALL_FILL          = "color_wall_fill";
    String PROPERTY_COLOR_DOOR               = "color_door";
    String PROPERTY_POS_BONUS                = "pos_bonus";
    String PROPERTY_POS_PAC                  = "pos_pac";
    String PROPERTY_POS_RED_GHOST            = "pos_ghost_1_red";
    String PROPERTY_POS_PINK_GHOST           = "pos_ghost_2_pink";
    String PROPERTY_POS_CYAN_GHOST           = "pos_ghost_3_cyan";
    String PROPERTY_POS_ORANGE_GHOST         = "pos_ghost_4_orange";
    String PROPERTY_POS_SCATTER_RED_GHOST    = "pos_scatter_ghost_1_red";
    String PROPERTY_POS_SCATTER_PINK_GHOST   = "pos_scatter_ghost_2_pink";
    String PROPERTY_POS_SCATTER_CYAN_GHOST   = "pos_scatter_ghost_3_cyan";
    String PROPERTY_POS_SCATTER_ORANGE_GHOST = "pos_scatter_ghost_4_orange";
    String PROPERTY_POS_HOUSE_MIN_TILE       = "pos_house_min_tile";

    String DEFAULT_COLOR_FOOD                = "rgb(255,255,255)";
    String DEFAULT_COLOR_WALL_STROKE         = "rgb(33,33,255)";
    String DEFAULT_COLOR_WALL_FILL           = "rgb(0,0,0)";
    String DEFAULT_COLOR_DOOR                = "rgb(255,183, 255)";

    Vector2i DEFAULT_POS_HOUSE               = new Vector2i(10, 15);
    Vector2i DEFAULT_POS_RED_GHOST           = DEFAULT_POS_HOUSE.plus(3, -1);
    Vector2i DEFAULT_POS_CYAN_GHOST          = DEFAULT_POS_HOUSE.plus(1, 2);
    Vector2i DEFAULT_POS_PINK_GHOST          = DEFAULT_POS_HOUSE.plus(3, 2);
    Vector2i DEFAULT_POS_ORANGE_GHOST        = DEFAULT_POS_HOUSE.plus(5, 2);
    Vector2i DEFAULT_POS_BONUS               = new Vector2i(13, 20);
    Vector2i DEFAULT_POS_PAC                 = new Vector2i(13, 26);

    ResourceBundle TEXT_BUNDLE = ResourceBundle.getBundle(TileMapEditorViewModel.class.getPackageName() + ".texts");

    static String tt(String key, Object... args) {
        return MessageFormat.format(TEXT_BUNDLE.getString(key), args);
    }

    ObjectProperty<WorldMap> worldMapProperty();

    IntegerProperty gridSizeProperty();

    Canvas canvas();

    ContextMenu contextMenu();

    void showMessage(String message, long seconds, MessageType type);

    void indicateEraseMode();

    void indicateEditMode();

    Palette selectedPalette();

    byte selectedPaletteID();

    PropertyEditorPane terrainPropertiesEditor();

    PropertyEditorPane foodPropertiesEditor();

    void updateSourceView();
}
