package de.amr.pacmanfx.tilemap.editor.properties;

import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.lib.tilemap.WorldMapFormatter;
import de.amr.pacmanfx.lib.tilemap.WorldMapParser;
import de.amr.pacmanfx.tilemap.editor.EditorUI;
import javafx.scene.Node;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.HBox;

class TilePropertyEditor extends SinglePropertyEditor {

    private final Spinner<Integer> spinnerX;
    private final Spinner<Integer> spinnerY;
    private final SpinnerValueFactory.IntegerSpinnerValueFactory spinnerXModel;
    private final SpinnerValueFactory.IntegerSpinnerValueFactory spinnerYModel;
    private final HBox valueEditorPane;

    public TilePropertyEditor(EditorUI ui, LayerID layerID, PropertyInfo propertyInfo, String propertyValue) {
        super(ui, layerID, propertyInfo);

        spinnerX = new Spinner<>(0, 1000, 0);
        spinnerXModel = (SpinnerValueFactory.IntegerSpinnerValueFactory) spinnerX.getValueFactory();
        spinnerX.setMaxWidth(60);
        spinnerX.disableProperty().bind(enabled.not());

        spinnerY = new Spinner<>(0, 1000, 0);
        spinnerYModel = (SpinnerValueFactory.IntegerSpinnerValueFactory) spinnerY.getValueFactory();
        spinnerY.setMaxWidth(60);
        spinnerY.disableProperty().bind(enabled.not());

        WorldMapParser.parseTile(propertyValue).ifPresent(tile -> {
            spinnerX.getValueFactory().setValue(tile.x());
            spinnerY.getValueFactory().setValue(tile.y());
        });

        spinnerX.valueProperty().addListener((py, ov, nv) -> storePropertyValue(ui));
        spinnerY.valueProperty().addListener((py, ov, nv) -> storePropertyValue(ui));

        valueEditorPane = new HBox(spinnerX, spinnerY);
    }

    @Override
    protected void updateEditorFromProperty() {
        spinnerXModel.setMax(worldMap().numCols() - 1);
        spinnerYModel.setMax(worldMap().numRows() - 1);
        String propertyValue = worldMap().properties(layerID).get(propertyInfo.name());
        WorldMapParser.parseTile(propertyValue).ifPresent(tile -> {
            spinnerX.getValueFactory().setValue(tile.x());
            spinnerY.getValueFactory().setValue(tile.y());
        });
    }

    @Override
    protected String formattedPropertyValue() {
        return WorldMapFormatter.formatTile(spinnerX.getValue(), spinnerY.getValue());
    }

    @Override
    protected Node valueEditor() {
        return valueEditorPane;
    }
}
