/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.mapeditor;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.mapeditor.actions.*;
import de.amr.pacmanfx.model.world.TerrainLayer;
import de.amr.pacmanfx.model.world.TerrainTile;
import de.amr.pacmanfx.model.world.WorldMap;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.scene.control.*;

import static de.amr.pacmanfx.mapeditor.Globals_MapEditor.NO_GRAPHIC;
import static de.amr.pacmanfx.mapeditor.Globals_MapEditor.translated;
import static de.amr.pacmanfx.model.world.WorldMap.tile;

public class EditorMenuBar extends MenuBar {

    private final Menu menuFile;

    private final Menu menuMaps;

    public Menu menuFile() {
        return menuFile;
    }

    public Menu menuMaps() {
        return menuMaps;
    }

    public EditorMenuBar(TileMapEditorUI ui) {
        menuFile = buildFileMenu(ui);
        menuMaps = new Menu(translated("menu.load_map"));
        getMenus().addAll(
            menuFile,
            buildEditMenu(ui),
            buildViewMenu(ui),
            menuMaps);
    }

    private Menu buildFileMenu(TileMapEditorUI ui) {
        final TileMapEditor editor = ui.editor();

        var miNewPreconfiguredMap = new MenuItem(translated("menu.file.new"));
        miNewPreconfiguredMap.setOnAction(_ -> new Action_SetNewMapInteractively(ui, true).execute());

        var miNewBlankMap = new MenuItem(translated("menu.file.new_blank_map"));
        miNewBlankMap.setOnAction(_ -> new Action_SetNewMapInteractively(ui, false).execute());

        var miOpenMapFile = new MenuItem(translated("menu.file.open"));
        miOpenMapFile.setOnAction(_ -> new Action_OpenMapFileInteractively(ui).execute());

        var miSaveMapFileAs = new MenuItem(translated("menu.file.save_as"));
        miSaveMapFileAs.setOnAction(_ -> new Action_SaveMapFileInteractively(ui).execute());

        var miOpenTemplateImage = new MenuItem(translated("menu.file.open_template_image"));
        miOpenTemplateImage.setOnAction(_ -> new Action_OpenTemplateCreateMap(ui).execute());

        var miCloseTemplateImage = new MenuItem(translated("menu.file.close_template_image"));
        miCloseTemplateImage.setOnAction(_ -> editor.setTemplateImage(null));

        return new Menu(translated("menu.file"), NO_GRAPHIC,
            miNewPreconfiguredMap,
            miNewBlankMap,
            miOpenMapFile,
            miSaveMapFileAs,
            new SeparatorMenuItem(),
            miOpenTemplateImage,
            miCloseTemplateImage);
    }

    private Menu buildEditMenu(TileMapEditorUI ui) {
        final TileMapEditor editor = ui.editor();
        final BooleanBinding isInspectingMap = ui.editModeProperty().isEqualTo(EditMode.INSPECT);

        final var miObstacleJoining = new CheckMenuItem(translated("menu.edit.obstacles_joining"));
        miObstacleJoining.selectedProperty().bindBidirectional(ui.obstaclesJoiningProperty());

        final var miAddBorder = new MenuItem(translated("menu.edit.add_border"));
        miAddBorder.setOnAction(_ -> new Action_AddBorderWall(editor).execute());

        final var miAddHouse = new MenuItem(translated("menu.edit.add_house"));
        miAddHouse.setOnAction(_ -> {
            int numRows = editor.currentWorldMap().numRows(), numCols = editor.currentWorldMap().numCols();
            new Action_MoveArcadeHouse(editor, tile(numCols / 2 - 4, numRows / 2 - 3)).execute();
        });

        final var miDeleteHouse = new MenuItem(translated("menu.edit.delete_house"));
        miDeleteHouse.setOnAction(_ -> new Action_DeleteArcadeHouse(editor).execute());

        final var miClearTerrain = new MenuItem(translated("menu.edit.clear_terrain"));
        miClearTerrain.setOnAction(_ -> new Action_ClearTerrain(editor).execute());

        final var miFloodWithPellets = new MenuItem(translated("menu.edit.flood_with_pellets"));
        miFloodWithPellets.setOnAction(_ -> {
            final Vector2i tile = findFreeTileInsideMap(editor.currentWorldMap());
            if (tile != null) {
                new Action_FloodWithPellets(editor, tile).execute();
            }
        });

        final var miClearFood = new MenuItem(translated("menu.edit.clear_food"));
        miClearFood.setOnAction(_ -> new Action_ClearFood(editor).execute());

        final var miClearFoodAroundHouse = new MenuItem(translated("menu.edit.clear_food_around_house"));
        miClearFoodAroundHouse.setOnAction(_ -> new Action_ClearFoodAroundHouse(editor, editor.currentWorldMap()).execute());

        final var miIdentifyTiles = new MenuItem(translated("menu.edit.identify_tiles"));
        miIdentifyTiles.disableProperty().bind(Bindings.createBooleanBinding(
            () -> ui.editMode() == EditMode.INSPECT || editor.templateImageProperty().get() == null,
            ui.editModeProperty(), editor.templateImageProperty())
        );
        miIdentifyTiles.setOnAction(_ -> new Action_FillMapFromTemplate(ui).execute());

        final var miAssignDefaultColors = new MenuItem(translated("menu.edit.assign_default_colors"));
        miAssignDefaultColors.setOnAction(_ -> new Action_SetDefaultMapColors(editor).execute());

        final Menu menu = new Menu(translated("menu.edit"), NO_GRAPHIC,
            miObstacleJoining,
            new SeparatorMenuItem(),
            miAddBorder,
            miAddHouse,
            miDeleteHouse,
            miClearTerrain,
            new SeparatorMenuItem(),
            miFloodWithPellets,
            miClearFood,
            miClearFoodAroundHouse,
            new SeparatorMenuItem(),
            miIdentifyTiles,
            miAssignDefaultColors);

        menu.disableProperty().bind(isInspectingMap);

        return menu;
    }

    private Menu buildViewMenu(TileMapEditorUI ui) {
        var miPropertiesVisible = new CheckMenuItem(translated("menu.view.properties"));
        miPropertiesVisible.selectedProperty().bindBidirectional(ui.propertyEditorsVisibleProperty());

        var miTerrainVisible = new CheckMenuItem(translated("menu.view.terrain"));
        miTerrainVisible.selectedProperty().bindBidirectional(ui.terrainVisibleProperty());

        var miFoodVisible = new CheckMenuItem(translated("menu.view.food"));
        miFoodVisible.selectedProperty().bindBidirectional(ui.foodVisibleProperty());

        var miActorsVisible = new CheckMenuItem(translated("menu.view.actors"));
        miActorsVisible.selectedProperty().bindBidirectional(ui.actorsVisibleProperty());

        var miGridVisible = new CheckMenuItem(translated("menu.view.grid"));
        miGridVisible.selectedProperty().bindBidirectional(ui.gridVisibleProperty());

        var miSegmentNumbersVisible = new CheckMenuItem(translated("menu.view.segment_numbers"));
        miSegmentNumbersVisible.selectedProperty().bindBidirectional(ui.segmentNumbersVisibleProperty());

        var miObstacleInnerAreaVisible = new CheckMenuItem(translated("inner_obstacle_area"));
        miObstacleInnerAreaVisible.selectedProperty().bindBidirectional(ui.obstacleInnerAreaDisplayedProperty());

        return new Menu(translated("menu.view"), NO_GRAPHIC,
            miPropertiesVisible,
            miTerrainVisible,
            miFoodVisible,
            miActorsVisible,
            miSegmentNumbersVisible,
            miObstacleInnerAreaVisible,
            miGridVisible);
    }

    //TODO This is not 100% correct because it could start outside the outer wall and flood the wrong area
    private Vector2i findFreeTileInsideMap(WorldMap worldMap) {
        final TerrainLayer terrain = worldMap.terrainLayer();
        int startRow = terrain.emptyRowsOverMaze();
        int endRow = terrain.numRows() - terrain.emptyRowsBelowMaze();
        for (int r = startRow; r <= endRow; ++r) {
            for (int c = 1; c < worldMap.numCols() - 1; ++c) {
                if (terrain.content(r, c) == TerrainTile.EMPTY.$) {
                    return new Vector2i(c, r);
                }
            }
        }
        return null;
    }
}