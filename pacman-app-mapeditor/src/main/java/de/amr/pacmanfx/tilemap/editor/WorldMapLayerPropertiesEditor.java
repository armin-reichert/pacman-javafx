/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tilemap.editor;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.lib.tilemap.WorldMapFormatter;
import de.amr.pacmanfx.lib.tilemap.WorldMapParser;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static de.amr.pacmanfx.tilemap.editor.EditorUtil.formatColor;
import static de.amr.pacmanfx.tilemap.editor.EditorUtil.parseColor;
import static java.util.Objects.requireNonNull;

public class WorldMapLayerPropertiesEditor extends BorderPane {

    static final int NAME_EDITOR_MIN_WIDTH = 180;
    static final Pattern PATTERN_PROPERTY_NAME = Pattern.compile("[a-zA-Z]([a-zA-Z0-9_])*");
    static final String SYMBOL_DELETE = "\u274C";

    private static boolean isValidPropertyName(String s) {
        return PATTERN_PROPERTY_NAME.matcher(s).matches();
    }

    private abstract class SinglePropertyEditor {

        protected final ObjectProperty<WorldMap> worldMap = new SimpleObjectProperty<>();
        protected PropertyInfo propertyInfo;
        protected final TextField nameEditor;

        protected SinglePropertyEditor(PropertyInfo propertyInfo) {
            this.propertyInfo = requireNonNull(propertyInfo);

            nameEditor = new TextField(propertyInfo.name());
            nameEditor.setMinWidth(NAME_EDITOR_MIN_WIDTH);
            nameEditor.disableProperty().bind(enabled.not());
            nameEditor.setOnAction(e -> onPropertyNameEdited());
        }

        public ObjectProperty<WorldMap> worldMapProperty() {
            return worldMap;
        }

        public WorldMap worldMap() {
            return worldMap.get();
        }

        protected void onPropertyNameEdited() {
            if (worldMap() == null) {
                return;
            }
            String editedName = nameEditor.getText().trim();
            if (editedName.isBlank()) {
                nameEditor.setText(propertyInfo.name());
                return;
            }
            if (propertyInfo.name().equals(editedName)) {
                return;
            }
            if (!isValidPropertyName(editedName)) {
                nameEditor.setText(propertyInfo.name());
                messageDisplay.showMessage("Property name '%s' is invalid".formatted(editedName), 2, MessageType.ERROR);
                return;
            }
            if (worldMap().properties(layerID).containsKey(editedName)) {
                messageDisplay.showMessage("Property name already in use", 2, MessageType.ERROR);
                nameEditor.setText(propertyInfo.name());
                return;
            }
            messageDisplay.showMessage("Property '%s' renamed to '%s'"
                .formatted(propertyInfo.name(), editedName), 2, MessageType.INFO);

            worldMap().properties(layerID).remove(propertyInfo.name());
            worldMap().properties(layerID).put(editedName, formattedPropertyValue());
            propertyInfo = new PropertyInfo(editedName, propertyInfo.type());

            rebuildPropertyEditors(); // sort order might have changed

            editor.setWorldMapChanged();
            editor.setEdited(true);
        }

        protected void storePropertyValue() {
            worldMap().properties(layerID).put(propertyInfo.name(), formattedPropertyValue());
            editor.setWorldMapChanged();
            editor.setEdited(true);
        }

        protected abstract String formattedPropertyValue();

        protected abstract void updateEditorFromProperty();

        protected abstract Node valueEditor();

        protected Node nameEditor() {
            return nameEditor;
        }
    }

    private class TextPropertyEditor extends SinglePropertyEditor {

        private final TextField textEditor;

        public TextPropertyEditor(PropertyInfo propertyInfo, String propertyValue) {
            super(propertyInfo);
            textEditor = new TextField();
            textEditor.setText(propertyValue);
            textEditor.disableProperty().bind(enabled.not());
            textEditor.setOnAction(e -> storePropertyValue());
        }

        @Override
        protected void updateEditorFromProperty() {
            String text = worldMap().properties(layerID).get(propertyInfo.name());
            textEditor.setText(text);
        }

        @Override
        protected Node valueEditor() {
            return textEditor;
        }

        @Override
        protected String formattedPropertyValue() {
            return textEditor.getText().strip();
        }
    }

    private class ColorPropertyEditor extends SinglePropertyEditor {

        private final ColorPicker colorPicker;

        public ColorPropertyEditor(PropertyInfo propertyInfo, String propertyValue) {
            super(propertyInfo);
            colorPicker = new ColorPicker();
            colorPicker.setValue(parseColor(propertyValue));
            colorPicker.disableProperty().bind(enabled.not());
            colorPicker.setOnAction(e -> storePropertyValue());
        }

        @Override
        protected void updateEditorFromProperty() {
            String colorExpression = worldMap().properties(layerID).get(propertyInfo.name());
            colorPicker.setValue(parseColor(colorExpression));
        }

        @Override
        protected Node valueEditor() {
            return colorPicker;
        }

        @Override
        protected String formattedPropertyValue() {
            return formatColor(colorPicker.getValue());
        }
    }

    private class TilePropertyEditor extends SinglePropertyEditor {

        private final Spinner<Integer> spinnerX;
        private final Spinner<Integer> spinnerY;
        private final SpinnerValueFactory.IntegerSpinnerValueFactory spinnerXModel;
        private final SpinnerValueFactory.IntegerSpinnerValueFactory spinnerYModel;
        private final HBox valueEditorPane;

        public TilePropertyEditor(PropertyInfo propertyInfo, String propertyValue) {
            super(propertyInfo);

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

            spinnerX.valueProperty().addListener((py,ov,nv) -> storePropertyValue());
            spinnerY.valueProperty().addListener((py,ov,nv) -> storePropertyValue());

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

    // main class

    private final TileMapEditor editor;
    private final MessageDisplay messageDisplay;
    private final LayerID layerID;

    private final BooleanProperty enabled = new SimpleBooleanProperty(true);

    private final ObjectProperty<WorldMap> worldMap = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
            rebuildPropertyEditors();
        }
    };

    private final List<SinglePropertyEditor> propertyEditors = new ArrayList<>();
    private final GridPane grid = new GridPane();

    public WorldMapLayerPropertiesEditor(TileMapEditor editor, MessageDisplay messageDisplay, LayerID layerID) {
        this.editor = requireNonNull(editor);
        this.messageDisplay = requireNonNull(messageDisplay);
        this.layerID = requireNonNull(layerID);
        worldMap.bind(editor.currentWorldMapProperty());

        var btnAddColorEntry = new Button("Color");
        btnAddColorEntry.disableProperty().bind(enabled.not());
        btnAddColorEntry.setOnAction(e -> addNewColorProperty("color_RENAME_ME"));

        var btnAddPosEntry = new Button("Position");
        btnAddPosEntry.setOnAction(e -> addNewPositionProperty("pos_RENAME_ME"));
        btnAddPosEntry.disableProperty().bind(enabled.not());

        var btnAddTextEntry = new Button("Text");
        btnAddTextEntry.setOnAction(e -> addNewTextProperty("RENAME_ME"));
        btnAddTextEntry.disableProperty().bind(enabled.not());

        var buttonBar = new HBox(new Label("New"), btnAddColorEntry, btnAddPosEntry, btnAddTextEntry);
        buttonBar.setSpacing(3);
        buttonBar.setPadding(new Insets(2,2,6,2));
        buttonBar.setAlignment(Pos.CENTER_LEFT);

        grid.setHgap(2);
        grid.setVgap(2);

        setTop(buttonBar);
        setCenter(grid);
    }

    private void addNewColorProperty(String propertyName) {
        if (worldMap.get().properties(layerID).containsKey(propertyName)) {
            messageDisplay.showMessage("Property %s already exists".formatted(propertyName), 1, MessageType.INFO);
            return;
        }
        worldMap.get().properties(layerID).put(propertyName, "green");
        editor.setWorldMapChanged();
        editor.setEdited(true);
        messageDisplay.showMessage("New property %s added".formatted(propertyName), 1, MessageType.INFO);
        rebuildPropertyEditors();
    }

    private void addNewPositionProperty(String propertyName) {
        if (worldMap.get().properties(layerID).containsKey(propertyName)) {
            messageDisplay.showMessage("Property %s already exists".formatted(propertyName), 1, MessageType.INFO);
            return;
        }
        worldMap.get().properties(layerID).put(propertyName, "(0,0)");
        messageDisplay.showMessage("New property %s added".formatted(propertyName), 1, MessageType.INFO);
        editor.setWorldMapChanged();
        editor.setEdited(true);
        rebuildPropertyEditors();
    }

    private void addNewTextProperty(String propertyName) {
        if (worldMap.get().properties(layerID).containsKey(propertyName)) {
            messageDisplay.showMessage("Property %s already exists".formatted(propertyName), 1, MessageType.INFO);
            return;
        }
        worldMap.get().properties(layerID).put(propertyName, "any text");
        editor.setWorldMapChanged();
        editor.setEdited(true);
        messageDisplay.showMessage("New property %s added".formatted(propertyName), 1, MessageType.INFO);
        rebuildPropertyEditors();
    }


    private void deleteProperty(String propertyName) {
        if (worldMap().properties(layerID).containsKey(propertyName)) {
            worldMap().properties(layerID).remove(propertyName);
            editor.setWorldMapChanged();
            editor.setEdited(true);
            messageDisplay.showMessage("Property '%s' deleted".formatted(propertyName), 3, MessageType.INFO);
        }
    }

    public BooleanProperty enabledProperty() {
        return enabled;
    }

    public WorldMap worldMap() {
        return worldMap.get();
    }

    public void updatePropertyEditorValues() {
        for (var propertyEditor : propertyEditors) {
            propertyEditor.updateEditorFromProperty();
        }
    }

    public void rebuildPropertyEditors() {
        propertyEditors.clear();
        if (worldMap() == null) {
            Logger.info("World map not yet set, cannot build property editors");
            return;
        }
        worldMap().propertyNames(layerID).forEach(propertyName -> {
            String propertyValue = worldMap.get().properties(layerID).get(propertyName);
            // primitive way of discriminating but fulfills its purpose
            SinglePropertyEditor propertyEditor;
            if (propertyName.startsWith("color_")) {
                propertyEditor = new ColorPropertyEditor(new PropertyInfo(propertyName, Color.class), propertyValue);
            } else if (propertyName.startsWith("pos_") || propertyName.startsWith("tile_") || propertyName.startsWith("vec_")) {
                propertyEditor = new TilePropertyEditor(new PropertyInfo(propertyName, Vector2i.class), propertyValue);
            } else {
                propertyEditor = new TextPropertyEditor(new PropertyInfo(propertyName, String.class), propertyValue);
            }
            propertyEditor.worldMapProperty().bind(worldMap);
            propertyEditors.add(propertyEditor);
        });

        int row = 0;
        grid.getChildren().clear();
        for (var propertyEditor : propertyEditors) {
            grid.add(propertyEditor.nameEditor(), 0, row);
            grid.add(propertyEditor.valueEditor(), 1, row);
            var btnDelete = new Button(SYMBOL_DELETE);
            btnDelete.disableProperty().bind(enabled.not());
            btnDelete.setOnAction(e -> deleteProperty(propertyEditor.propertyInfo.name()));
            Tooltip tooltip = new Tooltip("Delete"); //TODO localize
            tooltip.setFont(EditorGlobals.FONT_TOOL_TIPS);
            btnDelete.setTooltip(tooltip);
            grid.add(btnDelete, 2, row);
            ++row;
        }

        updatePropertyEditorValues();
    }
}