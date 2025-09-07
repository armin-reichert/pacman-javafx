package de.amr.pacmanfx.tilemap.editor;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.tilemap.editor.actions.*;
import javafx.beans.binding.Bindings;
import javafx.scene.control.*;

import static de.amr.pacmanfx.tilemap.editor.EditorGlobals.NO_GRAPHIC;
import static de.amr.pacmanfx.tilemap.editor.EditorGlobals.translated;

public class EditorMenuBar extends MenuBar {

    private final Menu menuFile;
    private final Menu menuMaps;

    public Menu menuFile() {
        return menuFile;
    }

    public Menu menuMaps() {
        return menuMaps;
    }

    public EditorMenuBar(EditorUI ui) {

        final TileMapEditor editor = ui.editor();

        // "File" menu

        var miNewPreconfiguredMap = new MenuItem(translated("menu.file.new"));
        miNewPreconfiguredMap.setOnAction(actionEvent -> new Action_SetNewMapInteractively(ui, true).execute());

        var miNewBlankMap = new MenuItem(translated("menu.file.new_blank_map"));
        miNewBlankMap.setOnAction(actionEvent -> new Action_SetNewMapInteractively(ui, false).execute());

        var miOpenMapFile = new MenuItem(translated("menu.file.open"));
        miOpenMapFile.setOnAction(actionEvent -> new Action_OpenMapFileInteractively(ui).execute());

        var miSaveMapFileAs = new MenuItem(translated("menu.file.save_as"));
        miSaveMapFileAs.setOnAction(actionEvent -> new Action_SaveMapFileInteractively(ui).execute());

        var miOpenTemplateImage = new MenuItem(translated("menu.file.open_template_image"));
        miOpenTemplateImage.setOnAction(actionEvent -> new Action_OpenTemplateCreateMap(ui).execute());

        var miCloseTemplateImage = new MenuItem(translated("menu.file.close_template_image"));
        miCloseTemplateImage.setOnAction(actionEvent -> editor.setTemplateImage(null));

        menuFile = new Menu(translated("menu.file"), NO_GRAPHIC,
            miNewPreconfiguredMap,
            miNewBlankMap,
            miOpenMapFile,
            miSaveMapFileAs,
            new SeparatorMenuItem(),
            miOpenTemplateImage,
            miCloseTemplateImage);

        // "Edit" menu

        var miObstacleJoining = new CheckMenuItem(translated("menu.edit.obstacles_joining"));
        miObstacleJoining.selectedProperty().bindBidirectional(ui.obstaclesJoiningProperty());

        var miAddBorder = new MenuItem(translated("menu.edit.add_border"));
        miAddBorder.setOnAction(actionEvent -> new Action_AddBorderWall(editor).execute());
        miAddBorder.disableProperty().bind(ui.editModeProperty().map(mode -> mode == EditMode.INSPECT));

        var miAddHouse = new MenuItem(translated("menu.edit.add_house"));
        miAddHouse.setOnAction(actionEvent -> {
            int numRows = editor.currentWorldMap().numRows(), numCols = editor.currentWorldMap().numCols();
            int houseMinX = numCols / 2 - 4, houseMinY = numRows / 2 - 3;
            new Action_PlaceArcadeHouse(editor, Vector2i.of(houseMinX, houseMinY)).execute();
        });

        miAddHouse.disableProperty().bind(ui.editModeProperty().map(mode -> mode == EditMode.INSPECT));

        var miDeleteHouse = new MenuItem(translated("menu.edit.delete_house"));
        miDeleteHouse.setOnAction(actionEvent -> new Action_DeleteArcadeHouse(editor).execute());
        miDeleteHouse.disableProperty().bind(ui.editModeProperty().map(mode -> mode == EditMode.INSPECT));

        var miClearTerrain = new MenuItem(translated("menu.edit.clear_terrain"));
        miClearTerrain.setOnAction(actionEvent -> new Action_ClearTerrain(editor).execute());
        miClearTerrain.disableProperty().bind(ui.editModeProperty().map(mode -> mode == EditMode.INSPECT));

        var miClearFood = new MenuItem(translated("menu.edit.clear_food"));
        miClearFood.setOnAction(actionEvent -> new Action_ClearFood(editor).execute());
        miClearFood.disableProperty().bind(ui.editModeProperty().map(mode -> mode == EditMode.INSPECT));

        var miIdentifyTiles = new MenuItem(translated("menu.edit.identify_tiles"));
        miIdentifyTiles.disableProperty().bind(Bindings.createBooleanBinding(
                () -> ui.editMode() == EditMode.INSPECT
                        || editor.templateImageProperty().get() == null,
                ui.editModeProperty(), editor.templateImageProperty()));

        miIdentifyTiles.setOnAction(actionEvent -> new Action_FillMapFromTemplate(ui).execute());

        var miAssignDefaultColors = new MenuItem("menu.edit.assign_default_colors");
        miAssignDefaultColors.setOnAction(actionEvent -> new Action_SetDefaultMapColors(editor).execute());
        miAssignDefaultColors.disableProperty().bind(ui.editModeProperty().map(mode -> mode == EditMode.INSPECT));

        Menu menuEdit = new Menu(translated("menu.edit"), NO_GRAPHIC,
            miObstacleJoining,
            new SeparatorMenuItem(),
            miAddBorder,
            miAddHouse,
            miDeleteHouse,
            miClearTerrain,
            miClearFood,
            miIdentifyTiles,
            miAssignDefaultColors);

        // "Maps" menu

        menuMaps = new Menu(translated("menu.load_map"));

        // "View" menu

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

        Menu menuView = new Menu(translated("menu.view"), NO_GRAPHIC,
            miPropertiesVisible,
            miTerrainVisible,
            miFoodVisible,
            miActorsVisible,
            miSegmentNumbersVisible,
            miObstacleInnerAreaVisible,
            miGridVisible);

        getMenus().addAll(menuFile, menuEdit, menuView, menuMaps);
    }
}