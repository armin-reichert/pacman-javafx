/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.mapeditor.properties;

import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.mapeditor.TileMapEditorUI;
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.model.world.WorldMapLayer;
import de.amr.pacmanfx.model.world.WorldMapLayerID;
import javafx.scene.Node;
import javafx.scene.control.Spinner;
import javafx.scene.layout.HBox;
import org.tinylog.Logger;

import static de.amr.pacmanfx.lib.math.Vector2i.vec2_int;

class TilePropertyEditor extends AbstractPropertyEditor {

    private final Spinner<Integer> spinnerX;
    private final Spinner<Integer> spinnerY;
    private final HBox valueEditorPane;

    public TilePropertyEditor(TileMapEditorUI ui, WorldMapLayerID layerID, WorldMapLayer layer, MapEditorProperty property) {
        super(ui, layerID, layer, property);

        spinnerX = new Spinner<>(0, layer.numCols() - 1, 0);
        spinnerX.setMaxWidth(60);
        spinnerX.disableProperty().bind(enabled.not());

        spinnerY = new Spinner<>(0, layer.numRows() - 1, 0);
        spinnerY.setMaxWidth(60);
        spinnerY.disableProperty().bind(enabled.not());

        final String value = property.value();
        try {
            final Vector2i tile = WorldMap.parseTile(value);
            spinnerX.getValueFactory().setValue(tile.x());
            spinnerY.getValueFactory().setValue(tile.y());

        } catch (IllegalArgumentException x) {
            Logger.error(x, "Could not parse tile value '{}'", value);
        }

        spinnerX.valueProperty().addListener((_, _, _) -> storeValueInMapLayer());
        spinnerY.valueProperty().addListener((_, _, _) -> storeValueInMapLayer());

        valueEditorPane = new HBox(spinnerX, spinnerY);
        valueEditorPane.setPrefWidth(MapLayerPropertiesEditor.VALUE_EDITOR_WIDTH);
        valueEditorPane.setMinWidth(MapLayerPropertiesEditor.VALUE_EDITOR_WIDTH);
    }

    @Override
    public void updateState() {
        final String value = property().value();
        if (value != null) {
            try {
                final Vector2i tile = WorldMap.parseTile(value);
                spinnerX.getValueFactory().setValue(tile.x());
                spinnerY.getValueFactory().setValue(tile.y());
            } catch (IllegalArgumentException x) {
                Logger.error(x, "Could not parse tile value '{}'", value);
            }
        }
    }

    @Override
    protected String formattedValue() {
        return MapEditorPropertyType.TILE.format(vec2_int(spinnerX.getValue(), spinnerY.getValue()));
    }

    @Override
    protected Node valueEditor() {
        return valueEditorPane;
    }
}