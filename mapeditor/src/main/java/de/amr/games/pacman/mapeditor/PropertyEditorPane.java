/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.mapeditor;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.TileMap;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static de.amr.games.pacman.lib.tilemap.TileMap.formatTile;
import static de.amr.games.pacman.lib.tilemap.TileMap.parseVector2i;
import static de.amr.games.pacman.mapeditor.TileMapUtil.formatColor;
import static de.amr.games.pacman.mapeditor.TileMapUtil.parseColor;
import static java.util.Comparator.comparing;

/**
 * @author Armin Reichert
 */
public class PropertyEditorPane extends BorderPane {

    private static final int NAME_COLUMN_MIN_WIDTH = 180;

    abstract class PropertyEditor {

        final String propertyName;
        final TextField nameEditor;

        public PropertyEditor(String propertyName) {
            this.propertyName = propertyName;
            nameEditor = new TextField(propertyName);
            nameEditor.setMinWidth(NAME_COLUMN_MIN_WIDTH);
            nameEditor.disableProperty().bind(enabledPy.not());
            nameEditor.setOnAction(e -> edit());
        }

        protected void edit() {
            doEdit(editedPropertyName(), formattedPropertyValue());
        }

        protected String editedPropertyName() {
            return nameEditor.getText().strip();
        }

        protected abstract String formattedPropertyValue();

        protected void updateEditorValue() {
        }

        protected abstract Node valueEditorNode();
    }

    class TextPropertyEditor extends PropertyEditor {

        final TextField textEditor;

        public TextPropertyEditor(String propertyName, String propertyValue) {
            super(propertyName);
            textEditor = new TextField();
            textEditor.setText(propertyValue);
            textEditor.disableProperty().bind(enabledPy.not());
            textEditor.setOnAction(e -> edit());
        }

        @Override
        protected Node valueEditorNode() {
            return textEditor;
        }

        @Override
        protected String formattedPropertyValue() {
            return textEditor.getText();
        }
    }

    class ColorPropertyEditor extends PropertyEditor {
        final ColorPicker colorPicker;

        public ColorPropertyEditor(String propertyName, String propertyValue) {
            super(propertyName);
            colorPicker = new ColorPicker();
            colorPicker.setUserData(propertyName);
            colorPicker.setValue(parseColor(propertyValue));
            colorPicker.disableProperty().bind(enabledPy.not());
            colorPicker.setOnAction(e -> edit());
        }

        @Override
        protected void updateEditorValue() {
            String propertyName = (String) colorPicker.getUserData();
            String propertyValue = (String) editedProperties.get(propertyName);
            colorPicker.setValue(parseColor(propertyValue));
        }

        @Override
        protected Node valueEditorNode() {
            return colorPicker;
        }

        @Override
        protected String formattedPropertyValue() {
            return formatColor(colorPicker.getValue());
        }
    }

    class TilePropertyEditor extends PropertyEditor {
        final HBox valueEditorPane;
        final Spinner<Integer> spinnerX;
        final Spinner<Integer> spinnerY;

        public TilePropertyEditor(String propertyName, String propertyValue) {
            super(propertyName);
            Vector2i tile = parseVector2i(propertyValue);

            spinnerX  = new Spinner<>(0, tileMap.numCols() - 1, 0);
            spinnerX.setMaxWidth(60);
            spinnerX.setUserData(propertyName);
            spinnerX.disableProperty().bind(enabledPy.not());
            if (tile != null) {
                spinnerX.getValueFactory().setValue(tile.x());
            }

            spinnerY  = new Spinner<>(0, tileMap.numRows() - 1, 0);
            spinnerY.setMaxWidth(60);
            spinnerY.setUserData(propertyName);
            spinnerY.disableProperty().bind(enabledPy.not());
            if (tile != null) {
                spinnerY.getValueFactory().setValue(tile.y());
            }

            spinnerX.valueProperty().addListener((py,ov,nv) -> edit());
            spinnerY.valueProperty().addListener((py,ov,nv) -> edit());

            valueEditorPane = new HBox(spinnerX, spinnerY);
        }

        @Override
        protected void updateEditorValue() {
            String propertyName = (String) spinnerX.getUserData();
            String propertyValue = (String) editedProperties.get(propertyName);
            Vector2i tile = parseVector2i(propertyValue);
            if (tile != null) {
                spinnerX.getValueFactory().setValue(tile.x());
                spinnerY.getValueFactory().setValue(tile.y());
            }
        }

        @Override
        protected String formattedPropertyValue() {
            return formatTile(new Vector2i(spinnerX.getValue(), spinnerY.getValue()));
        }

        @Override
        protected Node valueEditorNode() {
            return valueEditorPane;
        }
    }

    public final BooleanProperty enabledPy = new SimpleBooleanProperty(true);

    private final TileMapEditor editor;
    private final List<PropertyEditor> editors = new ArrayList<>();
    private final GridPane grid = new GridPane();
    private TileMap tileMap;
    private Properties editedProperties;

    public PropertyEditorPane(TileMapEditor editor) {
        this.editor = editor;

        var btnAddColorEntry = new Button("Color");
        btnAddColorEntry.setOnAction(e -> {
            editedProperties.put("color_new_color", "green");
            rebuildEditors();
        });
        btnAddColorEntry.disableProperty().bind(enabledPy.not());

        var btnAddPosEntry = new Button("Position");
        btnAddPosEntry.setOnAction(e -> {
            editedProperties.put("pos_new_position", "(0,0)");
            rebuildEditors();
        });
        btnAddPosEntry.disableProperty().bind(enabledPy.not());

        var btnAddGenericEntry = new Button("Text");
        btnAddGenericEntry.setOnAction(e -> {
            editedProperties.put("text_property", "any text");
            rebuildEditors();
        });
        btnAddGenericEntry.disableProperty().bind(enabledPy.not());

        var buttonBar = new HBox(new Label("New"), btnAddColorEntry, btnAddPosEntry, btnAddGenericEntry);
        buttonBar.setSpacing(3);
        buttonBar.setAlignment(Pos.CENTER_LEFT);

        grid.setHgap(2);
        grid.setVgap(1);

        setTop(buttonBar);
        setCenter(grid);
    }

    public void setMap(TileMap tileMap) {
        this.tileMap = tileMap;
        this.editedProperties = tileMap.getProperties();
        rebuildEditors();
    }

    public void updateEditorValues() {
        for (var editor : editors) {
            editor.updateEditorValue();
        }
    }

    private void doEdit(String editedPropertyName, Object editedValue) {
        //TODO provide better solution
        if (editedPropertyName.endsWith("*")) {
            String propertyName = editedPropertyName.substring(0, editedPropertyName.length() - 1);
            editedProperties.remove(propertyName);
            Logger.info("Property {} deleted", propertyName);
            rebuildEditors();
        }
        else if (!editedPropertyName.isBlank()) {
            if (editedProperties.containsKey(editedPropertyName)) {
                editedProperties.put(editedPropertyName, editedValue);
            } else {
                editedProperties.put(editedPropertyName, editedValue);
                rebuildEditors();
            }
        }
        editor.markMapEdited();
    }

    private void rebuildEditors() {
        Logger.info("Rebuild editors");
        editors.clear();

        var sortedProperties = editedProperties.entrySet().stream()
            .sorted(comparing(entry -> entry.getKey().toString()))
            .toList();

        grid.getChildren().clear();
        int row = 0;
        for (var property : sortedProperties) {
            String propertyName = property.getKey().toString();
            String propertyValue = property.getValue().toString();
            PropertyEditor editor;
            if (propertyName.startsWith("color_")) {
                editor = new ColorPropertyEditor(propertyName, propertyValue);
            } else if (propertyName.startsWith("pos_")) {
                editor = new TilePropertyEditor(propertyName, propertyValue);
            } else {
                editor = new TextPropertyEditor(propertyName, propertyValue);
            }
            editors.add(editor);
            grid.add(editor.nameEditor, 0, row);
            grid.add(editor.valueEditorNode(), 1, row);
            ++row;
        }
    }
}