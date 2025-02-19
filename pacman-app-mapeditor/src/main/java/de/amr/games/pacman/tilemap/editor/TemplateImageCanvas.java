/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tilemap.editor;

import de.amr.games.pacman.lib.tilemap.WorldMap;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.tilemap.WorldMap.*;
import static de.amr.games.pacman.tilemap.editor.TileMapEditor.tt;
import static de.amr.games.pacman.tilemap.editor.TileMapEditorUtil.*;

public class TemplateImageCanvas extends Canvas {

    private final IntegerProperty gridSizePy = new SimpleIntegerProperty();
    private final BooleanProperty gridVisiblePy = new SimpleBooleanProperty();
    private final ObjectProperty<Image> templateImagePy = new SimpleObjectProperty<>();
    private final ObjectProperty<WorldMap> worldMapPy = new SimpleObjectProperty<>();

    private ContextMenu colorSelectionContextMenu;
    private ColorIndicator colorIndicator;

    public static class ColorIndicator extends HBox {
        private final VBox colorBox = new VBox();
        private final Text colorText = new Text("Color picked here");

        public ColorIndicator() {
            setMinWidth(120);
            setMinHeight(30);
            setSpacing(10);
            setPadding(new Insets(3));
            setBackground(Background.fill(Color.BLACK));

            colorBox.setMinWidth(30);
            colorBox.setMaxWidth(30);
            colorBox.setMinHeight(30);
            colorBox.setMaxHeight(30);
            colorBox.setBorder(Border.stroke(Color.WHITE));

            colorText.setFont(Font.font("Sans", FontWeight.BOLD, 20));
            colorText.setFill(Color.WHITE);

            getChildren().addAll(colorBox, colorText);
        }

        public void setColor(Color color) {
            colorBox.setBackground(Background.fill(color));
            colorText.setText(color.equals(Color.TRANSPARENT) ? "Transparent" : formatColorHex(color));
        }
    }

    public TemplateImageCanvas(TileMapEditor editor) {

        gridSizePy.bind(editor.gridSizeProperty());
        gridVisiblePy.bind(editor.gridVisibleProperty());
        templateImagePy.bind(editor.templateImageProperty());
        worldMapPy.bind(editor.worldMapProperty());

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
            colorIndicator.setLayoutX(e.getX() + 20);
            colorIndicator.setLayoutY(e.getY() + 40);
            colorIndicator.setVisible(e.getX() < getWidth() && e.getY() < getHeight());
        });

        setOnMouseExited(e -> colorIndicator.setVisible(false));

        colorIndicator = new ColorIndicator();
        colorIndicator.setVisible(false);
        templateImagePy.addListener((py, ov, nv) -> colorIndicator.setVisible(nv != null));
    }

    public ColorIndicator getColorIndicator() {
        return colorIndicator;
    }

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
        colorSelectionContextMenu = new ContextMenu();

        Color colorAtMousePosition = pickColor(e.getX(), e.getY());
        if (colorAtMousePosition.equals(Color.TRANSPARENT)) {
            return;
        }

        var miColorPreview = createColorMenuItem(colorAtMousePosition, null);

        Color fillColor = getColorFromMap(worldMap().terrain(), PROPERTY_COLOR_WALL_FILL, null);
        var miPickFillColor = createColorMenuItem(fillColor, tt("menu.pick_color.set_fill_color"));
        miPickFillColor.setOnAction(ae -> editor.setTerrainMapPropertyValue(PROPERTY_COLOR_WALL_FILL, formatColor(colorAtMousePosition)));

        Color strokeColor =  getColorFromMap(worldMap().terrain(), PROPERTY_COLOR_WALL_STROKE, null);
        var miPickStrokeColor = createColorMenuItem(strokeColor, tt("menu.pick_color.set_stroke_color"));
        miPickStrokeColor.setOnAction(ae -> editor.setTerrainMapPropertyValue(PROPERTY_COLOR_WALL_STROKE, formatColor(colorAtMousePosition)));

        Color doorColor =  getColorFromMap(worldMap().terrain(), PROPERTY_COLOR_DOOR, null);
        var miPickDoorColor = createColorMenuItem(doorColor, tt("menu.pick_color.set_door_color"));
        miPickDoorColor.setOnAction(ae -> editor.setTerrainMapPropertyValue(PROPERTY_COLOR_DOOR, formatColor(colorAtMousePosition)));

        Color foodColor =  getColorFromMap(worldMap().food(), PROPERTY_COLOR_FOOD, null);
        var miPickFoodColor = createColorMenuItem(foodColor, tt("menu.pick_color.set_food_color"));
        miPickFoodColor.setOnAction(ae -> editor.setFoodMapPropertyValue(PROPERTY_COLOR_FOOD, formatColor(colorAtMousePosition)));

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
        var colorBox = new HBox();
        colorBox.setMinWidth(32);
        colorBox.setMinHeight(16);
        if (color != null) {
            colorBox.setBackground(Background.fill(color));
            colorBox.setBorder(Border.stroke(Color.BLACK));
            Text text = new Text(formatColorHex(color));
            text.setFont(TileMapEditor.FONT_CONTEXT_MENU_COLOR_TEXT);
            text.setFill(Color.BLACK);
            colorBox.getChildren().add(text);
        } else {
            colorBox.getChildren().add(new Text(tt("use_color_as")));
        }

        var content = itemText != null ? new HBox(colorBox, new Text(itemText))  : new HBox(colorBox);
        content.setSpacing(3);

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
                g.setStroke(Color.GRAY);
                g.setLineWidth(0.5);
                for (int r = 1; r < height / TS; ++r) {
                    g.strokeLine(0, scaling * r * TS, width, scaling * r * TS);
                }
                for (int c = 1; c < width / TS; ++c) {
                    g.strokeLine(scaling * c * TS, 0, scaling * c * TS, height);
                }
            }
        }
    }
}
