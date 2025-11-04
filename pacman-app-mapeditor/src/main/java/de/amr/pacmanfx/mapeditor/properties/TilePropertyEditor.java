/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.mapeditor.properties;

import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.lib.worldmap.LayerID;
import de.amr.pacmanfx.lib.worldmap.WorldMapLayer;
import de.amr.pacmanfx.lib.worldmap.WorldMapParser;
import de.amr.pacmanfx.mapeditor.TileMapEditorUI;
import javafx.scene.Node;
import javafx.scene.control.Spinner;
import javafx.scene.layout.HBox;

class TilePropertyEditor extends AbstractPropertyEditor {

    private final Spinner<Integer> spinnerX;
    private final Spinner<Integer> spinnerY;
    private final HBox valueEditorPane;

    public TilePropertyEditor(TileMapEditorUI ui, LayerID layerID, WorldMapLayer layer, MapEditorProperty property) {
        super(ui, layerID, layer, property);

        spinnerX = new Spinner<>(0, layer.numCols() - 1, 0);
        spinnerX.setMaxWidth(60);
        spinnerX.disableProperty().bind(enabled.not());

        spinnerY = new Spinner<>(0, layer.numRows() - 1, 0);
        spinnerY.setMaxWidth(60);
        spinnerY.disableProperty().bind(enabled.not());

        WorldMapParser.parseTile(property.value()).ifPresent(tile -> {
            spinnerX.getValueFactory().setValue(tile.x());
            spinnerY.getValueFactory().setValue(tile.y());
        });

        spinnerX.valueProperty().addListener((py, ov, nv) -> storeValueInMapLayer());
        spinnerY.valueProperty().addListener((py, ov, nv) -> storeValueInMapLayer());

        valueEditorPane = new HBox(spinnerX, spinnerY);
        valueEditorPane.setPrefWidth(MapLayerPropertiesEditor.VALUE_EDITOR_WIDTH);
        valueEditorPane.setMinWidth(MapLayerPropertiesEditor.VALUE_EDITOR_WIDTH);
    }

    @Override
    public void updateState() {
        WorldMapParser.parseTile(property().value()).ifPresent(tile -> {
            spinnerX.getValueFactory().setValue(tile.x());
            spinnerY.getValueFactory().setValue(tile.y());
        });
    }

    @Override
    protected String formattedValue() {
        return MapEditorPropertyType.TILE.format(Vector2i.of(spinnerX.getValue(), spinnerY.getValue()));
    }

    @Override
    protected Node valueEditor() {
        return valueEditorPane;
    }
}