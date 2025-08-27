/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tilemap.editor;

import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.model.WorldMapProperty;
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

    private final IntegerProperty gridSizePy = new SimpleIntegerProperty();
    private final BooleanProperty gridVisiblePy = new SimpleBooleanProperty();
    private final ObjectProperty<Image> templateImagePy = new SimpleObjectProperty<>();
    private final ObjectProperty<WorldMap> worldMapPy = new SimpleObjectProperty<>();

    private ContextMenu colorSelectionContextMenu;
    private ColorIndicator colorIndicator;

    public TemplateImageCanvas(TileMapEditor editor) {
        gridSizePy.bind(editor.gridSizeProperty());
        gridVisiblePy.bind(editor.gridVisibleProperty());
        templateImagePy.bind(editor.templateImageProperty());
        worldMapPy.bind(editor.editedWorldMapProperty());

        widthProperty().bind(Bindings.createDoubleBinding(
            () -> {
                Image templateImage = templateImagePy.get();
                double scaling = gridSize() / (double)TS;
                return templateImage != null ? templateImage.getWidth() * scaling : 0;
            }, gridSizePy, templateImagePy));

        heightProperty().bind(Bindings.createDoubleBinding(
            () -> {
                Image templateImage = templateImagePy.get();
                double scaling = gridSize() / (double)TS;
                return templateImage != null ? templateImage.getHeight() * scaling : 0;
            }, gridSizePy, templateImagePy));

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
        templateImagePy.addListener((py, ov, nv) -> colorIndicator.setVisible(nv != null));
    }

    public ColorIndicator getColorIndicator() { return colorIndicator; }

    private int gridSize() { return gridSizePy.get(); }

    private WorldMap worldMap() { return worldMapPy.get(); }

    private Color pickColor(double x, double y) {
        Image image = templateImagePy.get();
        double pickX = x * (image.getWidth() / getBoundsInLocal().getWidth());
        double pickY = y * (image.getHeight() / getBoundsInLocal().getHeight());
        return image.getPixelReader().getColor((int) pickX, (int) pickY);
    }

    private void showColorSelectionContextMenu(TileMapEditor editor, ContextMenuEvent e) {
        if (colorSelectionContextMenu != null) {
            colorSelectionContextMenu.hide();
        }
        if (editor.isEditMode(EditMode.INSPECT)) {
            return;
        }

        colorSelectionContextMenu = new ContextMenu();

        final Color pickColor = pickColor(e.getX(), e.getY());
        final Color colorToSelect = pickColor.equals(Color.TRANSPARENT) ? Color.BLACK : pickColor;

        var miColorPreview = createColorMenuItem(colorToSelect,
                pickColor.equals(Color.TRANSPARENT) ? "TRANSPARENT -> BLACK" : formatColorHex(colorToSelect));

        Color fillColor = getColorFromMap(worldMap(), LayerID.TERRAIN, WorldMapProperty.COLOR_WALL_FILL, null);
        var miPickFillColor = createColorMenuItem(fillColor, translated("menu.pick_color.set_fill_color"));
        miPickFillColor.setOnAction(ae -> editor.setTerrainMapPropertyValue(WorldMapProperty.COLOR_WALL_FILL, formatColor(colorToSelect)));

        Color strokeColor =  getColorFromMap(worldMap(), LayerID.TERRAIN, WorldMapProperty.COLOR_WALL_STROKE, null);
        var miPickStrokeColor = createColorMenuItem(strokeColor, translated("menu.pick_color.set_stroke_color"));
        miPickStrokeColor.setOnAction(ae -> editor.setTerrainMapPropertyValue(WorldMapProperty.COLOR_WALL_STROKE, formatColor(colorToSelect)));

        Color doorColor =  getColorFromMap(worldMap(), LayerID.TERRAIN, WorldMapProperty.COLOR_DOOR, null);
        var miPickDoorColor = createColorMenuItem(doorColor, translated("menu.pick_color.set_door_color"));
        miPickDoorColor.setOnAction(ae -> editor.setTerrainMapPropertyValue(WorldMapProperty.COLOR_DOOR, formatColor(colorToSelect)));

        Color foodColor =  getColorFromMap(worldMap(), LayerID.FOOD, WorldMapProperty.COLOR_FOOD, null);
        var miPickFoodColor = createColorMenuItem(foodColor, translated("menu.pick_color.set_food_color"));
        miPickFoodColor.setOnAction(ae -> editor.setFoodMapPropertyValue(WorldMapProperty.COLOR_FOOD, formatColor(colorToSelect)));

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
        Image image = templateImagePy.get();
        if (image != null) {
            double scaling = (double) gridSize() / TS;
            double width = scaling * image.getWidth(), height = scaling * image.getHeight();
            g.setImageSmoothing(false);
            g.drawImage(image, 0, 0, width, height);
            if (gridVisiblePy.get()) {
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
