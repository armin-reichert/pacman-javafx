/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tilemap.editor;

import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.model.WorldMapProperty;
import de.amr.pacmanfx.tilemap.editor.actions.Action_SetFoodProperty;
import de.amr.pacmanfx.tilemap.editor.actions.Action_SetTerrainProperty;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.tilemap.editor.EditorGlobals.translated;
import static de.amr.pacmanfx.tilemap.editor.TileMapEditorUtil.*;

public class TemplateImageCanvas extends Canvas {

    private final IntegerProperty gridSize = new SimpleIntegerProperty();
    private final BooleanProperty gridVisible = new SimpleBooleanProperty();
    private final ObjectProperty<Image> templateImage = new SimpleObjectProperty<>();
    private final ObjectProperty<WorldMap> worldMap = new SimpleObjectProperty<>();

    private ContextMenu colorSelectionContextMenu;
    private ColorIndicator colorIndicator;

    public TemplateImageCanvas(TileMapEditor editor) {
        gridSize.bind(editor.gridSizeProperty());
        gridVisible.bind(editor.gridVisibleProperty());
        templateImage.bind(editor.templateImageProperty());
        worldMap.bind(editor.currentWorldMapProperty());

        widthProperty().bind(Bindings.createDoubleBinding(
            () -> {
                Image image = templateImage.get();
                double scaling = gridSize() / (double)TS;
                return image != null ? image.getWidth() * scaling : 0;
            }, gridSize, templateImage));

        heightProperty().bind(Bindings.createDoubleBinding(
            () -> {
                Image image = templateImage.get();
                double scaling = gridSize() / (double)TS;
                return image != null ? image.getHeight() * scaling : 0;
            }, gridSize, templateImage));

        setOnContextMenuRequested(e -> showColorSelectionContextMenu(editor, e));

        setOnMouseClicked(e -> {
            if (colorSelectionContextMenu != null) {
                colorSelectionContextMenu.hide();
                colorSelectionContextMenu = null;
            }
            colorIndicator.setVisible(false);
        });

        setOnMouseMoved(e -> {
            colorIndicator.setColor(pickColor(e.getX(), e.getY()));
            double correction = 1.5 * (e.getX() - getWidth() * 0.5) / getWidth();
            colorIndicator.setLayoutX(e.getX() - colorIndicator.getWidth() * 0.5 - correction * colorIndicator.getWidth());
            colorIndicator.setLayoutY(e.getY() + 20);
            colorIndicator.setVisible(e.getX() < getWidth() && e.getY() < getHeight());
        });

        setOnMouseExited(e -> colorIndicator.setVisible(false));

        colorIndicator = new ColorIndicator(170);
        colorIndicator.setVisible(false);
        templateImage.addListener((py, ov, nv) -> colorIndicator.setVisible(nv != null));
    }

    public ColorIndicator getColorIndicator() { return colorIndicator; }

    private int gridSize() { return gridSize.get(); }

    private WorldMap worldMap() { return worldMap.get(); }

    private Color pickColor(double x, double y) {
        Image image = templateImage.get();
        double pickX = x * (image.getWidth() / getBoundsInLocal().getWidth());
        double pickY = y * (image.getHeight() / getBoundsInLocal().getHeight());
        return image.getPixelReader().getColor((int) pickX, (int) pickY);
    }

    private void showColorSelectionContextMenu(TileMapEditor editor, ContextMenuEvent e) {
        if (colorSelectionContextMenu != null) {
            colorSelectionContextMenu.hide();
        }
        if (editor.editModeIs(EditMode.INSPECT)) {
            return;
        }

        colorSelectionContextMenu = new ContextMenu();

        final Color pickColor = pickColor(e.getX(), e.getY());
        final Color colorToSelect = pickColor.equals(Color.TRANSPARENT) ? Color.BLACK : pickColor;

        var miColorPreview = createColorMenuItem(colorToSelect,
                pickColor.equals(Color.TRANSPARENT) ? "TRANSPARENT -> BLACK" : formatColorHex(colorToSelect));

        Color fillColor = getColorFromMap(worldMap(), LayerID.TERRAIN, WorldMapProperty.COLOR_WALL_FILL, null);
        var miPickFillColor = createColorMenuItem(fillColor, translated("menu.pick_color.set_fill_color"));
        miPickFillColor.setOnAction(ae -> new Action_SetTerrainProperty(editor,
            WorldMapProperty.COLOR_WALL_FILL, formatColor(colorToSelect)).execute());

        Color strokeColor =  getColorFromMap(worldMap(), LayerID.TERRAIN, WorldMapProperty.COLOR_WALL_STROKE, null);
        var miPickStrokeColor = createColorMenuItem(strokeColor, translated("menu.pick_color.set_stroke_color"));
        miPickStrokeColor.setOnAction(ae -> new Action_SetTerrainProperty(editor,
            WorldMapProperty.COLOR_WALL_STROKE, formatColor(colorToSelect)).execute());

        Color doorColor =  getColorFromMap(worldMap(), LayerID.TERRAIN, WorldMapProperty.COLOR_DOOR, null);
        var miPickDoorColor = createColorMenuItem(doorColor, translated("menu.pick_color.set_door_color"));
        miPickDoorColor.setOnAction(ae -> new Action_SetTerrainProperty(editor,
            WorldMapProperty.COLOR_DOOR, formatColor(colorToSelect)).execute());

        Color foodColor =  getColorFromMap(worldMap(), LayerID.FOOD, WorldMapProperty.COLOR_FOOD, null);
        var miPickFoodColor = createColorMenuItem(foodColor, translated("menu.pick_color.set_food_color"));
        miPickFoodColor.setOnAction(ae -> new Action_SetFoodProperty(editor,
            WorldMapProperty.COLOR_FOOD, formatColor(colorToSelect)).execute());

        colorSelectionContextMenu.getItems().addAll(
            miColorPreview,
            new SeparatorMenuItem(),
            miPickFillColor,
            miPickStrokeColor,
            miPickDoorColor,
            miPickFoodColor
        );
        colorSelectionContextMenu.show(this, e.getScreenX(), e.getScreenY());
    }

    private CustomMenuItem createColorMenuItem(Color color, String itemText) {
        BorderPane colorBox = new BorderPane();
        colorBox.setMinWidth(30);
        colorBox.setMaxWidth(30);
        colorBox.setMinHeight(30);
        colorBox.setMaxHeight(30);
        colorBox.setBorder(Border.stroke(Color.BLACK));
        colorBox.setBackground(Background.fill(color)); // color == null -> TRANSPARENT!
        if (color == null) {
            var undefinedHint = new Text("???");
            undefinedHint.setFont(Font.font("Sans", FontWeight.BOLD, 14));
            colorBox.setCenter(undefinedHint);
        }

        Text colorText = new Text();
        colorText.setText(itemText);
        colorText.setFont(Font.font("Sans", FontWeight.BOLD, 16));
        colorText.setFill(Color.BLACK);

        HBox content = new HBox();
        content.setMinWidth(120);
        content.setMinHeight(30);
        content.setSpacing(10);
        content.setPadding(new Insets(3));
        content.setBackground(Background.fill(Color.TRANSPARENT));
        content.getChildren().addAll(colorBox, colorText);

        return new CustomMenuItem(content);
    }

    public void draw() {
        GraphicsContext g = getGraphicsContext2D();
        g.setFill(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
        Image image = templateImage.get();
        if (image != null) {
            double scaling = (double) gridSize() / TS;
            double width = scaling * image.getWidth(), height = scaling * image.getHeight();
            g.setImageSmoothing(false);
            g.drawImage(image, 0, 0, width, height);
            if (gridVisible.get()) {
                g.setStroke(Color.grayRgb(180));
                g.setLineWidth(0.5);
                for (int row = 1; row < height / TS; ++row) {
                    double y = scaling * row * TS;
                    g.strokeLine(0, y, width, y);
                }
                for (int col = 1; col < width / TS; ++col) {
                    double x = scaling * col * TS;
                    g.strokeLine(x, 0, x, height);
                }
            }
        }
    }
}
