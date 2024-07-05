/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.mapeditor;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.TileMap;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.tinylog.Logger;

import java.util.*;

import static de.amr.games.pacman.lib.tilemap.TileMap.formatTile;
import static de.amr.games.pacman.lib.tilemap.TileMap.parseVector2i;
import static de.amr.games.pacman.mapeditor.TileMapUtil.formatColor;
import static de.amr.games.pacman.mapeditor.TileMapUtil.parseColor;
import static java.util.Comparator.comparing;

/**
 * @author Armin Reichert
 */
public class PropertyEditorPane extends BorderPane {

    public final BooleanProperty enabledPy = new SimpleBooleanProperty(true);

    private final TileMapEditor editor;
    private TileMap tileMap;
    private Properties editedProperties;
    private final GridPane grid = new GridPane();
    private final List<ColorPicker> colorPickers = new ArrayList<>();
    private final List<Spinner<Integer>> tileXEditors = new ArrayList<>();
    private final List<Spinner<Integer>> tileYEditors = new ArrayList<>();

    private int numRows;

    public PropertyEditorPane(String title, TileMapEditor editor) {
        this.editor = editor;

        var lblTitle = new Label(title);
        lblTitle.setFont(Font.font("Sans", FontWeight.BOLD, 14));

        var btnAddEntry = new Button("+");
        btnAddEntry.setStyle("-fx-padding: 0 2 0 2");
        btnAddEntry.setOnAction(e -> {
            editedProperties.put("a_new_property", "42");
            rebuildEditors();
        });
        btnAddEntry.disableProperty().bind(enabledPy.not());
        var header = new HBox(lblTitle, btnAddEntry);
        header.setSpacing(5);

        setTop(header);
        setCenter(grid);
    }

    public void setMap(TileMap tileMap) {
        this.tileMap = tileMap;
        this.editedProperties = tileMap.getProperties();
        rebuildEditors();
    }

    private void rebuildEditors() {
        Logger.info("Rebuild editors");
        colorPickers.clear();
        tileXEditors.clear();
        tileYEditors.clear();
        grid.getChildren().clear();
        grid.setHgap(2);
        grid.setVgap(1);
        int row = 0;
        var sortedEntries = editedProperties.entrySet().stream()
            .sorted(comparing(entry -> entry.getKey().toString()))
            .toList();

        int nameColumnMinWidth = 160;
        for (var entry : sortedEntries) {
            String propertyName = entry.getKey().toString();
            String propertyValue = entry.getValue().toString();

            var nameEditor = new TextField(propertyName);
            nameEditor.setMinWidth(nameColumnMinWidth);
            nameEditor.disableProperty().bind(enabledPy.not());
            grid.add(nameEditor, 0, row);

            if (propertyName.startsWith("color_")) {
                var colorPicker = new ColorPicker();
                colorPicker.setUserData(propertyName);
                colorPicker.setValue(parseColor(propertyValue));
                colorPicker.disableProperty().bind(enabledPy.not());
                colorPickers.add(colorPicker);

                nameEditor.setOnAction(e -> edit(nameEditor, formatColor(colorPicker.getValue())));
                colorPicker.setOnAction(e -> edit(nameEditor, formatColor(colorPicker.getValue())));

                grid.add(colorPicker, 1, row);
            }
            else if (propertyName.startsWith("pos_")) {
                var spinnerX  = new Spinner<Integer>(0, tileMap.numCols() - 1, 0);
                spinnerX.setMaxWidth(60);
                spinnerX.setUserData(propertyName);
                spinnerX.disableProperty().bind(enabledPy.not());
                tileXEditors.add(spinnerX);

                var spinnerY  = new Spinner<Integer>(0, tileMap.numRows() - 1, 0);
                spinnerY.setMaxWidth(60);
                spinnerY.setUserData(propertyName);
                spinnerY.disableProperty().bind(enabledPy.not());
                tileYEditors.add(spinnerY);

                HBox hbox = new HBox(spinnerX, spinnerY);
                Vector2i tile = parseVector2i(propertyValue);
                if (tile != null) {
                    spinnerX.getValueFactory().setValue(tile.x());
                    spinnerY.getValueFactory().setValue(tile.y());
                }

                Runnable doEdit = () -> edit(nameEditor, formatTile(new Vector2i(spinnerX.getValue(), spinnerY.getValue())));
                nameEditor.setOnAction(e -> doEdit.run());
                spinnerX.valueProperty().addListener((py,ov,nv) -> doEdit.run());
                spinnerY.valueProperty().addListener((py,ov,nv) -> doEdit.run());

                grid.add(hbox, 1, row);
            }
            else {
                var textEditor = new TextField();
                textEditor.setText(propertyValue);
                textEditor.disableProperty().bind(enabledPy.not());

                nameEditor.setOnAction(e -> edit(nameEditor, textEditor.getText()));
                textEditor.setOnAction(e -> edit(nameEditor, textEditor.getText()));

                grid.add(textEditor, 1, row);
            }
            ++row;
        }
        numRows = row;
    }

    public void updateEditorValues() {
        for (var colorPicker : colorPickers) {
            String propertyName = (String) colorPicker.getUserData();
            String propertyValue = (String) editedProperties.get(propertyName);
            colorPicker.setValue(parseColor(propertyValue));
        }
        for (int i = 0; i < tileXEditors.size(); ++i) {
            String propertyName = (String) tileXEditors.get(i).getUserData();
            String propertyValue = (String) editedProperties.get(propertyName);
            Vector2i tile = parseVector2i(propertyValue);
            if (tile != null) {
                tileXEditors.get(i).getValueFactory().setValue(tile.x());
                tileYEditors.get(i).getValueFactory().setValue(tile.y());
            }
        }
    }

    private void edit(TextField nameEditor, Object value) {
        String name = nameEditor.getText().trim();
        if (name.endsWith("*")) {
            name = name.substring(0, name.length()-1);
            editedProperties.remove(name);
            Logger.info("Property {} deleted", name);
            rebuildEditors();
        }
        else if (!name.isBlank()) {
            if (editedProperties.containsKey(name)) {
                editedProperties.put(name, value);
            } else {
                rebuildEditors();
                editedProperties.put(name, value);
            }
        }
        editor.markMapEdited();
    }
}
