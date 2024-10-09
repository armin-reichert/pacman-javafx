/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.maps.editor;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.TileMap;
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
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.lib.tilemap.TileMap.formatTile;
import static de.amr.games.pacman.lib.tilemap.TileMap.parseVector2i;
import static de.amr.games.pacman.maps.editor.TileMapUtil.formatColor;
import static de.amr.games.pacman.maps.editor.TileMapUtil.parseColor;
import static java.util.Objects.requireNonNull;

/**
 * @author Armin Reichert
 */
public class PropertyEditorPane extends BorderPane {

    private static final int NAME_COLUMN_MIN_WIDTH = 180;
    private static final Pattern PATTERN_PROPERTY_NAME = Pattern.compile("[a-zA-Z]([a-zA-Z0-9_])*");

    private static final String DELETE_COMMAND = ":del";

    private static boolean isValidPropertyName(String s) {
        return PATTERN_PROPERTY_NAME.matcher(s).matches();
    }

    // assumes s ends with suffix
    private static String chop(String s, String suffix) {
        return s.substring(0, s.length() - suffix.length());
    }

    private static boolean matchesDeleteCommand(String s) {
        return s != null && s.endsWith(DELETE_COMMAND) && isValidPropertyName(chop(s, DELETE_COMMAND));
    }

    private static String chopDeleteCommand(String s) {
        return matchesDeleteCommand(s) ? chop(s, DELETE_COMMAND) : s;
    }

    public final BooleanProperty enabledPy = new SimpleBooleanProperty(this, "enabled", true);

    private final ObjectProperty<TileMap> tileMapPy = new SimpleObjectProperty<>(this, "tileMap") {
        @Override
        protected void invalidated() {
            rebuildPropertyEditors();
        }
    };

    public void setTileMap(TileMap tileMap) {
        tileMapPy.set(requireNonNull(tileMap));
    }

    public TileMap tileMap() {
        return tileMapPy.get();
    }

    private final EditController editController;
    private final GridPane grid = new GridPane();
    private final List<AbstractPropertyEditor> propertyEditors = new ArrayList<>();

    private abstract class AbstractPropertyEditor {

        String propertyName;
        final TextField nameEditor;

        AbstractPropertyEditor(String propertyName) {
            this.propertyName = propertyName;
            nameEditor = new TextField(propertyName);
            nameEditor.setMinWidth(NAME_COLUMN_MIN_WIDTH);
            nameEditor.disableProperty().bind(enabledPy.not());
            nameEditor.setOnAction(e -> onPropertyNameEdited());
        }

        void onPropertyNameEdited() {
            String editedName = nameEditor.getText().trim();
            if (editedName.isBlank() || propertyName.equals(editedName)) {
                nameEditor.setText(propertyName);
                return;
            }
            if (matchesDeleteCommand(editedName)) {
                String deletePropertyName = chopDeleteCommand(editedName);
                if (deletePropertyName.equals(propertyName)) {
                    tileMap().getProperties().remove(propertyName);
                    Logger.debug("Property {} deleted", propertyName);
                    editController.markTileMapEdited(tileMap());
                    rebuildPropertyEditors(); //TODO check
                    editController.showInfoMessage("Property %s deleted".formatted(propertyName), 1);
                } else {
                    nameEditor.setText(propertyName);
                    editController.showErrorMessage("Cannot delete other property %s".formatted(deletePropertyName), 2);
                }
                return;
            }
            if (!isValidPropertyName(editedName)) {
                nameEditor.setText(propertyName);
                editController.showErrorMessage("Property name %s is invalid".formatted(editedName), 2);
                return;
            }
            if (tileMap().getProperties().get(editedName) != null) {
                editController.showErrorMessage("Property name already used", 2);
                nameEditor.setText(propertyName);
                return;
            }
            tileMap().getProperties().remove(propertyName);
            tileMap().getProperties().put(editedName, formattedPropertyValue());
            editController.showInfoMessage("Property %s renamed to %s".formatted(propertyName, editedName), 2);
            propertyName = editedName;
            rebuildPropertyEditors(); // sort order might have changed
            editController.markTileMapEdited(tileMap());
        }

        void storePropertyValue() {
            tileMap().getProperties().put(propertyName, formattedPropertyValue());
            editController.markTileMapEdited(tileMap());
        }

        abstract String formattedPropertyValue();

        abstract void updateEditorFromProperty();

        abstract Node valueEditor();

        Node nameEditor() {
            return nameEditor;
        }
    }

    private class TextPropertyEditor extends AbstractPropertyEditor {

        final TextField textEditor;

        TextPropertyEditor(String propertyName, String propertyValue) {
            super(propertyName);
            textEditor = new TextField();
            textEditor.setText(propertyValue);
            textEditor.disableProperty().bind(enabledPy.not());
            textEditor.setOnAction(e -> storePropertyValue());
        }

        @Override
        void updateEditorFromProperty() {
            textEditor.setText(tileMap().getProperties().getProperty(propertyName));
        }

        @Override
        Node valueEditor() {
            return textEditor;
        }

        @Override
        String formattedPropertyValue() {
            return textEditor.getText().strip();
        }
    }

    private class ColorPropertyEditor extends AbstractPropertyEditor {
        final ColorPicker colorPicker;

        ColorPropertyEditor(String propertyName, String propertyValue) {
            super(propertyName);
            colorPicker = new ColorPicker();
            colorPicker.setUserData(propertyName);
            colorPicker.setValue(parseColor(propertyValue));
            colorPicker.disableProperty().bind(enabledPy.not());
            colorPicker.setOnAction(e -> storePropertyValue());
        }

        @Override
        void updateEditorFromProperty() {
            String propertyValue = tileMap().getProperties().getProperty(propertyName);
            colorPicker.setValue(parseColor(propertyValue));
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

    private class TilePropertyEditor extends AbstractPropertyEditor {
        final Spinner<Integer> spinnerX;
        final Spinner<Integer> spinnerY;
        final HBox valueEditorPane;

        TilePropertyEditor(String propertyName, String propertyValue) {
            super(propertyName);
            Vector2i tile = parseVector2i(propertyValue);

            spinnerX = new Spinner<>(0, tileMap().numCols() - 1, 0);
            spinnerX.setMaxWidth(60);
            spinnerX.disableProperty().bind(enabledPy.not());
            if (tile != null) {
                spinnerX.getValueFactory().setValue(tile.x());
            }

            spinnerY = new Spinner<>(0, tileMap().numRows() - 1, 0);
            spinnerY.setMaxWidth(60);
            spinnerY.disableProperty().bind(enabledPy.not());
            if (tile != null) {
                spinnerY.getValueFactory().setValue(tile.y());
            }

            spinnerX.valueProperty().addListener((py,ov,nv) -> storePropertyValue());
            spinnerY.valueProperty().addListener((py,ov,nv) -> storePropertyValue());

            valueEditorPane = new HBox(spinnerX, spinnerY);
        }

        @Override
        protected void updateEditorFromProperty() {
            String propertyValue = tileMap().getProperties().getProperty(propertyName);
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
        protected Node valueEditor() {
            return valueEditorPane;
        }
    }

    public PropertyEditorPane(EditController editController) {
        this.editController = checkNotNull(editController);

        var btnAddColorEntry = new Button("Color");
        btnAddColorEntry.setOnAction(e -> {
            String propertyName = "color_RENAME_ME";
            tileMap().getProperties().put(propertyName, "green");
            editController.showInfoMessage("New property %s added".formatted(propertyName), 1);
            rebuildPropertyEditors();
        });
        btnAddColorEntry.disableProperty().bind(enabledPy.not());

        var btnAddPosEntry = new Button("Position");
        btnAddPosEntry.setOnAction(e -> {
            String propertyName = "pos_RENAME_ME";
            tileMap().getProperties().put(propertyName, "(0,0)");
            editController.showInfoMessage("New property %s added".formatted(propertyName), 1);
            rebuildPropertyEditors();
        });
        btnAddPosEntry.disableProperty().bind(enabledPy.not());

        var btnAddGenericEntry = new Button("Text");
        btnAddGenericEntry.setOnAction(e -> {
            String propertyName = "RENAME_ME";
            tileMap().getProperties().put(propertyName, "any text");
            editController.showInfoMessage("New property %s added".formatted(propertyName), 1);
            rebuildPropertyEditors();
        });
        btnAddGenericEntry.disableProperty().bind(enabledPy.not());

        var buttonBar = new HBox(new Label("New"), btnAddColorEntry, btnAddPosEntry, btnAddGenericEntry);
        buttonBar.setSpacing(3);
        buttonBar.setPadding(new Insets(2,2,6,2));
        buttonBar.setAlignment(Pos.CENTER_LEFT);

        grid.setHgap(2);
        grid.setVgap(2);

        setTop(buttonBar);
        setCenter(grid);
    }

    public void updatePropertyEditorValues() {
        for (var editor : propertyEditors) {
            editor.updateEditorFromProperty();
        }
    }

    public void rebuildPropertyEditors() {
        Logger.debug("Rebuild editors");
        propertyEditors.clear();
        tileMap().getProperties().stringPropertyNames().stream().sorted().forEach(propertyName -> {
            String propertyValue = tileMap().getProperty(propertyName);
            // primitive way of discriminating but fulfills its purpose
            if (propertyName.startsWith("color_")) {
                propertyEditors.add(new ColorPropertyEditor(propertyName, propertyValue));
            } else if (propertyName.startsWith("pos_") || propertyName.startsWith("tile_") || propertyName.startsWith("vec_")) {
                propertyEditors.add(new TilePropertyEditor(propertyName, propertyValue));
            } else {
                propertyEditors.add(new TextPropertyEditor(propertyName, propertyValue));
            }
        });

        int row = 0;
        grid.getChildren().clear();
        for (var propertyEditor : propertyEditors) {
            grid.add(propertyEditor.nameEditor(), 0, row);
            grid.add(propertyEditor.valueEditor(), 1, row);
            ++row;
        }
    }
}