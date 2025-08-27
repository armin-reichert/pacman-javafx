package de.amr.pacmanfx.tilemap.editor;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.FoodTile;
import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.lib.tilemap.TerrainTile;
import javafx.beans.binding.Bindings;
import javafx.scene.control.*;

import static de.amr.pacmanfx.tilemap.editor.EditorGlobals.NO_GRAPHIC;
import static de.amr.pacmanfx.tilemap.editor.EditorGlobals.translated;

public class EditorMenuBar extends MenuBar {

    private final Menu menuFile;
    private final Menu menuEdit;
    private final Menu menuLoadMap;
    private final Menu menuView;

    public Menu menuEdit() {
        return menuEdit;
    }

    public Menu menuFile() {
        return menuFile;
    }

    public Menu menuLoadMap() {
        return menuLoadMap;
    }

    public Menu menuView() {
        return menuView;
    }

    public EditorMenuBar(TileMapEditor editor) {

        // File
        var miNewPreconfiguredMap = new MenuItem(translated("menu.file.new"));
        miNewPreconfiguredMap.setOnAction(e -> {
            EditorActions.SHOW_NEW_MAP_DIALOG.setPreconfigureMap(true);
            EditorActions.SHOW_NEW_MAP_DIALOG.execute(editor);
        });

        var miNewBlankMap = new MenuItem(translated("menu.file.new_blank_map"));
        miNewBlankMap.setOnAction(e -> {
            EditorActions.SHOW_NEW_MAP_DIALOG.setPreconfigureMap(false);
            EditorActions.SHOW_NEW_MAP_DIALOG.execute(editor);
        });

        var miOpenMapFile = new MenuItem(translated("menu.file.open"));
        miOpenMapFile.setOnAction(e -> EditorActions.OPEN_MAP_FILE.execute(editor));

        var miSaveMapFileAs = new MenuItem(translated("menu.file.save_as"));
        miSaveMapFileAs.setOnAction(e -> EditorActions.SAVE_MAP_FILE.execute(editor));

        var miOpenTemplateImage = new MenuItem(translated("menu.file.open_template_image"));
        miOpenTemplateImage.setOnAction(e -> editor.initWorldMapForTemplateImage());

        var miCloseTemplateImage = new MenuItem(translated("menu.file.close_template_image"));
        miCloseTemplateImage.setOnAction(e -> editor.setTemplateImage(null));

        menuFile = new Menu(translated("menu.file"), NO_GRAPHIC,
            miNewPreconfiguredMap,
            miNewBlankMap,
            miOpenMapFile,
            miSaveMapFileAs,
            new SeparatorMenuItem(),
            miOpenTemplateImage,
            miCloseTemplateImage);

        // Edit
        var miObstacleJoining = new CheckMenuItem(translated("menu.edit.obstacles_joining"));
        miObstacleJoining.selectedProperty().bindBidirectional(editor.obstaclesJoiningProperty());

        var miAddBorder = new MenuItem(translated("menu.edit.add_border"));
        miAddBorder.setOnAction(e -> {
            EditorActions.ADD_BORDER_WALL.setWorldMap(editor.currentWorldMap());
            EditorActions.ADD_BORDER_WALL.execute(editor);
        });
        miAddBorder.disableProperty().bind(editor.editModeProperty().map(mode -> mode == EditMode.INSPECT));

        var miAddHouse = new MenuItem(translated("menu.edit.add_house"));
        miAddHouse.setOnAction(e -> {
            int numRows = editor.currentWorldMap().numRows(), numCols = editor.currentWorldMap().numCols();
            int houseMinX = numCols / 2 - 4, houseMinY = numRows / 2 - 3;
            EditorActions.PLACE_ARCADE_HOUSE.setHouseMinTile(Vector2i.of(houseMinX, houseMinY));
            EditorActions.PLACE_ARCADE_HOUSE.setWorldMap(editor.currentWorldMap());
            EditorActions.PLACE_ARCADE_HOUSE.execute(editor);
        });

        miAddHouse.disableProperty().bind(editor.editModeProperty().map(mode -> mode == EditMode.INSPECT));

        var miClearTerrain = new MenuItem(translated("menu.edit.clear_terrain"));
        miClearTerrain.setOnAction(e -> {
            editor.currentWorldMap().layer(LayerID.TERRAIN).setAll(TerrainTile.EMPTY.$);
            editor.changeManager().setTerrainMapChanged();
            editor.changeManager().setEdited(true);
        });
        miClearTerrain.disableProperty().bind(editor.editModeProperty().map(mode -> mode == EditMode.INSPECT));

        var miClearFood = new MenuItem(translated("menu.edit.clear_food"));
        miClearFood.setOnAction(e -> {
            editor.currentWorldMap().layer(LayerID.FOOD).setAll(FoodTile.EMPTY.code());
            editor.changeManager().setFoodMapChanged();
            editor.changeManager().setEdited(true);
        });
        miClearFood.disableProperty().bind(editor.editModeProperty().map(mode -> mode == EditMode.INSPECT));

        var miIdentifyTiles = new MenuItem(translated("menu.edit.identify_tiles"));
        miIdentifyTiles.disableProperty().bind(Bindings.createBooleanBinding(
                () -> editor.editMode() == EditMode.INSPECT
                        || editor.templateImageProperty().get() == null,
                editor.editModeProperty(), editor.templateImageProperty()));
        miIdentifyTiles.setOnAction(e -> editor.populateMapFromTemplateImage(editor.currentWorldMap(), editor.templateImage()));

        var miAssignDefaultColors = new MenuItem("Assign default colors"); //TODO localize
        miAssignDefaultColors.setOnAction(e -> editor.setDefaultColors(editor.currentWorldMap()));
        miAssignDefaultColors.disableProperty().bind(editor.editModeProperty().map(mode -> mode == EditMode.INSPECT));

        menuEdit = new Menu(translated("menu.edit"), NO_GRAPHIC,
            miObstacleJoining,
            new SeparatorMenuItem(),
            miAddBorder,
            miAddHouse,
            miClearTerrain,
            miClearFood,
            miIdentifyTiles,
            miAssignDefaultColors);

        // Maps
        menuLoadMap = new Menu(translated("menu.load_map"));

        // View
        var miPropertiesVisible = new CheckMenuItem(translated("menu.view.properties"));
        miPropertiesVisible.selectedProperty().bindBidirectional(editor.propertyEditorsVisibleProperty());

        var miTerrainVisible = new CheckMenuItem(translated("menu.view.terrain"));
        miTerrainVisible.selectedProperty().bindBidirectional(editor.terrainVisibleProperty());

        var miFoodVisible = new CheckMenuItem(translated("menu.view.food"));
        miFoodVisible.selectedProperty().bindBidirectional(editor.foodVisibleProperty());

        var miActorsVisible = new CheckMenuItem("Actors"); //TODO localize
        miActorsVisible.selectedProperty().bindBidirectional(editor.actorsVisibleProperty());

        var miGridVisible = new CheckMenuItem(translated("menu.view.grid"));
        miGridVisible.selectedProperty().bindBidirectional(editor.gridVisibleProperty());

        var miSegmentNumbersVisible = new CheckMenuItem(translated("menu.view.segment_numbers"));
        miSegmentNumbersVisible.selectedProperty().bindBidirectional(editor.segmentNumbersVisibleProperty());

        var miObstacleInnerAreaVisible = new CheckMenuItem(translated("inner_obstacle_area"));
        miObstacleInnerAreaVisible.selectedProperty().bindBidirectional(editor.obstacleInnerAreaDisplayedProperty());

        menuView = new Menu(translated("menu.view"), NO_GRAPHIC,
            miPropertiesVisible,
            miTerrainVisible,
            miFoodVisible,
            miActorsVisible,
            miSegmentNumbersVisible,
            miObstacleInnerAreaVisible,
            miGridVisible);

        getMenus().addAll(menuFile, menuEdit, menuLoadMap, menuView);
    }
}
