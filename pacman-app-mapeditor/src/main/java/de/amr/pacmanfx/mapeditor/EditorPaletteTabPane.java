/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.mapeditor;

import de.amr.pacmanfx.lib.worldmap.FoodTile;
import de.amr.pacmanfx.lib.worldmap.LayerID;
import de.amr.pacmanfx.lib.worldmap.TerrainTile;
import de.amr.pacmanfx.mapeditor.palette.ActorTool;
import de.amr.pacmanfx.mapeditor.palette.Palette;
import de.amr.pacmanfx.mapeditor.palette.PaletteID;
import de.amr.pacmanfx.mapeditor.palette.TileCodeEditorTool;
import de.amr.pacmanfx.mapeditor.rendering.ArcadeSprites;
import de.amr.pacmanfx.mapeditor.rendering.TerrainMapTileRenderer;
import de.amr.pacmanfx.uilib.rendering.FoodMapRenderer;
import javafx.geometry.Insets;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.text.Text;

import java.util.Optional;

import static de.amr.pacmanfx.mapeditor.EditorGlobals.*;
import static de.amr.pacmanfx.model.DefaultWorldMapProperties.*;

public class EditorPaletteTabPane extends TabPane {

    public EditorPaletteTabPane(TileMapEditorUI ui, TerrainMapTileRenderer terrainRenderer, FoodMapRenderer foodRenderer) {
        setMinHeight(90);
        setPadding(new Insets(5, 5, 5, 5));

        Palette terrainPalette = createTerrainPalette(ui, terrainRenderer);
        var tabTerrain = new Tab("", terrainPalette);
        tabTerrain.setGraphic(new Text(translated("terrain")));
        tabTerrain.setClosable(false);

        Palette foodPalette = createFoodPalette(ui, foodRenderer);
        var tabFood = new Tab("", foodPalette);
        tabFood.setGraphic(new Text(translated("pellets")));
        tabFood.setClosable(false);

        Palette actorPalette = createActorsPalette(ui, terrainRenderer);
        var tabActors = new Tab("", actorPalette);
        tabActors.setGraphic(new Text(translated("actors")));
        tabActors.setClosable(false);

        getTabs().setAll(tabTerrain, tabFood, tabActors);
        getSelectionModel().select(tabTerrain);

        getSelectionModel().selectedItemProperty().addListener((py, ov, selectedTab) -> highlightSelectedTab(selectedTab));
        highlightSelectedTab(getSelectionModel().getSelectedItem());
    }

    public Optional<Palette> selectedPalette() {
        Tab selectedTab = getSelectionModel().getSelectedItem();
        if (selectedTab != null && selectedTab.getContent() instanceof Palette palette) {
            return Optional.of(palette);
        }
        return Optional.empty();
    }

    private void highlightSelectedTab(Tab selectedTab) {
        for (Tab tab : getTabs()) {
            if (tab.getGraphic() instanceof Text text) {
                text.setFont(tab == selectedTab ? FONT_SELECTED_PALETTE : FONT_UNSELECTED_PALETTE);
            }
        }
    }

    private Palette createTerrainPalette(TileMapEditorUI ui, TerrainMapTileRenderer prototype) {
        var palette = new Palette(PaletteID.TERRAIN, 13);

        palette.addTool(new TileCodeEditorTool(ui.editor(), LayerID.TERRAIN, TerrainTile.EMPTY.$, "Empty Space"));
        palette.addTool(new TileCodeEditorTool(ui.editor(), LayerID.TERRAIN, TerrainTile.WALL_H.$, "Horizontal Wall"));
        palette.addTool(new TileCodeEditorTool(ui.editor(), LayerID.TERRAIN, TerrainTile.WALL_V.$, "Vertical Wall"));
        palette.addTool(new TileCodeEditorTool(ui.editor(), LayerID.TERRAIN, TerrainTile.ARC_NW.$, "NW Corner"));
        palette.addTool(new TileCodeEditorTool(ui.editor(), LayerID.TERRAIN, TerrainTile.ARC_NE.$, "NE Corner"));
        palette.addTool(new TileCodeEditorTool(ui.editor(), LayerID.TERRAIN, TerrainTile.ARC_SW.$, "SW Corner"));
        palette.addTool(new TileCodeEditorTool(ui.editor(), LayerID.TERRAIN, TerrainTile.ARC_SE.$, "SE Corner"));
        palette.addTool(new TileCodeEditorTool(ui.editor(), LayerID.TERRAIN, TerrainTile.DOOR.$, "Door"));
        palette.addTool(new TileCodeEditorTool(ui.editor(), LayerID.TERRAIN, TerrainTile.TUNNEL.$, "Tunnel"));
        palette.addTool(new TileCodeEditorTool(ui.editor(), LayerID.TERRAIN, TerrainTile.ONE_WAY_UP.$, "One-Way Up"));
        palette.addTool(new TileCodeEditorTool(ui.editor(), LayerID.TERRAIN, TerrainTile.ONE_WAY_DOWN.$, "One-Way Down"));
        palette.addTool(new TileCodeEditorTool(ui.editor(), LayerID.TERRAIN, TerrainTile.ONE_WAY_LEFT.$, "One-Way Left"));
        palette.addTool(new TileCodeEditorTool(ui.editor(), LayerID.TERRAIN, TerrainTile.ONE_WAY_RIGHT.$, "One-Way Right"));

        palette.setSelectedToolIndex(0); // "No Tile"

        var terrainRenderer = new TerrainMapTileRenderer(palette);
        terrainRenderer.backgroundColorProperty().bind(prototype.backgroundColorProperty());
        terrainRenderer.colorSchemeProperty().bind(prototype.colorSchemeProperty());
        palette.setRenderer(terrainRenderer);

        return palette;
    }

    private Palette createActorsPalette(TileMapEditorUI ui, TerrainMapTileRenderer prototype) {
        var palette = new Palette(PaletteID.ACTORS, 10);

        palette.addTool(new ActorTool(ui, POS_PAC,                  "Pac-Man", ArcadeSprites.PAC_MAN));
        palette.addTool(new ActorTool(ui, POS_GHOST_1_RED,            "Red Ghost", ArcadeSprites.RED_GHOST));
        palette.addTool(new ActorTool(ui, POS_GHOST_2_PINK,           "Pink Ghost", ArcadeSprites.PINK_GHOST));
        palette.addTool(new ActorTool(ui, POS_GHOST_3_CYAN,           "Cyan Ghost", ArcadeSprites.CYAN_GHOST));
        palette.addTool(new ActorTool(ui, POS_GHOST_4_ORANGE,         "Orange Ghost", ArcadeSprites.ORANGE_GHOST));
        palette.addTool(new ActorTool(ui, POS_BONUS,                "Bonus", ArcadeSprites.STRAWBERRY));
        palette.addTool(new ActorTool(ui, POS_SCATTER_RED_GHOST,    "Red Ghost Scatter", ArcadeSprites.RED_GHOST));
        palette.addTool(new ActorTool(ui, POS_SCATTER_PINK_GHOST,   "Pink Ghost Scatter", ArcadeSprites.PINK_GHOST));
        palette.addTool(new ActorTool(ui, POS_SCATTER_CYAN_GHOST,   "Cyan Ghost Scatter", ArcadeSprites.CYAN_GHOST));
        palette.addTool(new ActorTool(ui, POS_SCATTER_ORANGE_GHOST, "Orange Ghost Scatter", ArcadeSprites.ORANGE_GHOST));

        palette.setSelectedToolIndex(0); // "No actor"

        var actorRenderer = new TerrainMapTileRenderer(palette);
        actorRenderer.backgroundColorProperty().bind(prototype.backgroundColorProperty());
        actorRenderer.colorSchemeProperty().bind(prototype.colorSchemeProperty());
        palette.setRenderer(actorRenderer);

        return palette;
    }

    private Palette createFoodPalette(TileMapEditorUI ui, FoodMapRenderer prototype) {
        var palette = new Palette(PaletteID.FOOD, 3);

        palette.addTool(new TileCodeEditorTool(ui.editor(), LayerID.FOOD, FoodTile.EMPTY.$, "No Food"));
        palette.addTool(new TileCodeEditorTool(ui.editor(), LayerID.FOOD, FoodTile.PELLET.$, "Pellet"));
        palette.addTool(new TileCodeEditorTool(ui.editor(), LayerID.FOOD, FoodTile.ENERGIZER.$, "Energizer"));

        palette.setSelectedToolIndex(0); // "No Food"

        var foodRenderer = new FoodMapRenderer(palette);
        foodRenderer.backgroundColorProperty().bind(prototype.backgroundColorProperty());
        foodRenderer.energizerColorProperty().bind(prototype.energizerColorProperty());
        foodRenderer.pelletColorProperty().bind(prototype.pelletColorProperty());
        palette.setRenderer(foodRenderer);

        return palette;
    }
}