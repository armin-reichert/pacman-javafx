package de.amr.pacmanfx.tilemap.editor;

import de.amr.pacmanfx.lib.tilemap.FoodTile;
import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.lib.tilemap.TerrainTile;
import javafx.beans.binding.Bindings;
import javafx.scene.control.*;

import static de.amr.pacmanfx.tilemap.editor.TileMapEditor.NO_GRAPHIC;
import static de.amr.pacmanfx.tilemap.editor.TileMapEditor.translated;

public class EditorMenuBar extends MenuBar {

    private final Menu menuFile;
    private final Menu menuEdit;
    private final Menu menuLoadMap;
    private final Menu menuView;

    public EditorMenuBar(TileMapEditor editor) {

        // File
        var miNewPreconfiguredMap = new MenuItem(translated("menu.file.new"));
        miNewPreconfiguredMap.setOnAction(e -> editor.showNewMapDialog(true));

        var miNewBlankMap = new MenuItem(translated("menu.file.new_blank_map"));
        miNewBlankMap.setOnAction(e -> editor.showNewMapDialog(false));

        var miOpenMapFile = new MenuItem(translated("menu.file.open"));
        miOpenMapFile.setOnAction(e -> editor.openMapFileInteractively());

        var miSaveMapFileAs = new MenuItem(translated("menu.file.save_as"));
        miSaveMapFileAs.setOnAction(e -> editor.showSaveDialog());

        var miOpenTemplateImage = new MenuItem(translated("menu.file.open_template_image"));
        miOpenTemplateImage.setOnAction(e -> editor.initWorldMapForTemplateImage());

        var miCloseTemplateImage = new MenuItem(translated("menu.file.close_template_image"));
        miCloseTemplateImage.setOnAction(e -> editor.closeTemplateImage());

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
        miAddBorder.setOnAction(e -> editor.addBorderWall(editor.editedWorldMap()));
        miAddBorder.disableProperty().bind(editor.editModeProperty().map(mode -> mode == EditMode.INSPECT));

        var miAddHouse = new MenuItem(translated("menu.edit.add_house"));
        miAddHouse.setOnAction(e -> editor.addArcadeHouseAtMapCenter(editor.editedWorldMap()));
        miAddHouse.disableProperty().bind(editor.editModeProperty().map(mode -> mode == EditMode.INSPECT));

        var miClearTerrain = new MenuItem(translated("menu.edit.clear_terrain"));
        miClearTerrain.setOnAction(e -> {
            editor.editedWorldMap().layer(LayerID.TERRAIN).setAll(TerrainTile.EMPTY.$);
            editor.changeManager().setTerrainMapChanged();
            editor.changeManager().setEdited(true);
        });
        miClearTerrain.disableProperty().bind(editor.editModeProperty().map(mode -> mode == EditMode.INSPECT));

        var miClearFood = new MenuItem(translated("menu.edit.clear_food"));
        miClearFood.setOnAction(e -> {
            editor.editedWorldMap().layer(LayerID.FOOD).setAll(FoodTile.EMPTY.code());
            editor.changeManager().setFoodMapChanged();
            editor.changeManager().setEdited(true);
        });
        miClearFood.disableProperty().bind(editor.editModeProperty().map(mode -> mode == EditMode.INSPECT));

        var miIdentifyTiles = new MenuItem(translated("menu.edit.identify_tiles"));
        miIdentifyTiles.disableProperty().bind(Bindings.createBooleanBinding(
                () -> editor.editMode() == EditMode.INSPECT
                        || editor.templateImageProperty().get() == null,
                editor.editModeProperty(), editor.templateImageProperty()));
        miIdentifyTiles.setOnAction(e -> editor.populateMapFromTemplateImage(editor.editedWorldMap()));

        var miAssignDefaultColors = new MenuItem("Assign default colors"); //TODO localize
        miAssignDefaultColors.setOnAction(e -> editor.setDefaultColors(editor.editedWorldMap()));
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
    public Menu getFileMenu() {
        return menuFile;
    }

    public Menu getLoadMapMenu() {
        return menuLoadMap;
    }
}
