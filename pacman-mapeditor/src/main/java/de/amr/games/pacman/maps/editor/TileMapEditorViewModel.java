/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.maps.editor;

import de.amr.games.pacman.lib.tilemap.WorldMap;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ContextMenu;

/**
 * @author Armin Reichert
 */
public interface TileMapEditorViewModel {

    ObjectProperty<WorldMap> worldMapProperty();

    IntegerProperty gridSizeProperty();

    Canvas canvas();

    ContextMenu contextMenu();

    void showMessage(String message, long seconds, MessageType type);

    Palette selectedPalette();

    String selectedPaletteID();

    PropertyEditorPane terrainPropertiesEditor();

    PropertyEditorPane foodPropertiesEditor();

    void updateSourceView();
}
