/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.mapeditor.palette;

import de.amr.basics.math.RectShort;
import de.amr.pacmanfx.core.model.world.map.FoodTile;
import de.amr.pacmanfx.core.model.world.map.TerrainTile;
import de.amr.pacmanfx.core.model.world.map.WorldMapLayerID;
import de.amr.pacmanfx.core.model.world.map.WorldMapPropertyName;
import de.amr.pacmanfx.mapeditor.TileMapEditor;
import de.amr.pacmanfx.mapeditor.rendering.ArcadeSprites;
import de.amr.pacmanfx.mapeditor.rendering.TerrainMapTileRenderer;
import de.amr.pacmanfx.uilib.rendering.FoodMapRenderer;
import de.amr.pacmanfx.uilib.rendering.TileRenderer;
import javafx.geometry.Insets;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.text.Text;

import java.util.Optional;

import static de.amr.pacmanfx.mapeditor.TileMapEditorGlobals.*;

public class EditorPaletteTabPane extends TabPane {

    public EditorPaletteTabPane(TileMapEditor editor, TerrainMapTileRenderer terrainRenderer, FoodMapRenderer foodRenderer) {
        setMinHeight(90);
        setPadding(new Insets(5, 5, 5, 5));

        final Palette terrainPalette = createTerrainPalette(editor, terrainRenderer);
        final var tabTerrain = new Tab("", terrainPalette);
        tabTerrain.setGraphic(new Text(translated("terrain")));
        tabTerrain.setClosable(false);

        final Palette foodPalette = createFoodPalette(editor, foodRenderer);
        final var tabFood = new Tab("", foodPalette);
        tabFood.setGraphic(new Text(translated("pellets")));
        tabFood.setClosable(false);

        final Palette actorPalette = createActorsPalette(editor, terrainRenderer);
        final var tabActors = new Tab("", actorPalette);
        tabActors.setGraphic(new Text(translated("actors")));
        tabActors.setClosable(false);

        getTabs().setAll(tabTerrain, tabFood, tabActors);
        getSelectionModel().select(tabTerrain);

        getSelectionModel().selectedItemProperty().addListener((_, _, selectedTab) -> highlightSelectedTab(selectedTab));
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

    private static TileCodeTool terrainTool(TileMapEditor editor, TileRenderer renderer, byte code, String description) {
        final var tool = new TileCodeTool(editor, WorldMapLayerID.TERRAIN, code, description);
        tool.setTileRenderer(renderer);
        return tool;
    }

    private Palette createTerrainPalette(TileMapEditor editor, TerrainMapTileRenderer prototype) {
        final var palette = new Palette(PaletteID.TERRAIN, 13);

        final var renderer = new TerrainMapTileRenderer(palette);
        renderer.backgroundColorProperty().bind(prototype.backgroundColorProperty());
        renderer.mapColoringProperty().bind(prototype.mapColoringProperty());
        palette.setRenderer(renderer);

        palette.addTool(terrainTool(editor, renderer, TerrainTile.EMPTY.$, "Empty Space"));
        palette.addTool(terrainTool(editor, renderer, TerrainTile.WALL_H.$, "Horizontal Wall"));
        palette.addTool(terrainTool(editor, renderer, TerrainTile.WALL_V.$, "Vertical Wall"));
        palette.addTool(terrainTool(editor, renderer, TerrainTile.ARC_NW.$, "NW Corner"));
        palette.addTool(terrainTool(editor, renderer, TerrainTile.ARC_NE.$, "NE Corner"));
        palette.addTool(terrainTool(editor, renderer, TerrainTile.ARC_SW.$, "SW Corner"));
        palette.addTool(terrainTool(editor, renderer, TerrainTile.ARC_SE.$, "SE Corner"));
        palette.addTool(terrainTool(editor, renderer, TerrainTile.DOOR.$, "Door"));
        palette.addTool(terrainTool(editor, renderer, TerrainTile.TUNNEL.$, "Tunnel"));
        palette.addTool(terrainTool(editor, renderer, TerrainTile.ONE_WAY_UP.$, "One-Way Up"));
        palette.addTool(terrainTool(editor, renderer, TerrainTile.ONE_WAY_DOWN.$, "One-Way Down"));
        palette.addTool(terrainTool(editor, renderer, TerrainTile.ONE_WAY_LEFT.$, "One-Way Left"));
        palette.addTool(terrainTool(editor, renderer, TerrainTile.ONE_WAY_RIGHT.$, "One-Way Right"));

        palette.setSelectedToolIndex(0); // "No Tile"

        return palette;
    }

    private static ActorTool actorTool(TileMapEditor editor, String propertyName, String description, RectShort sprite) {
        return new ActorTool(editor, propertyName, description, sprite);
    }

    private Palette createActorsPalette(TileMapEditor editor, TerrainMapTileRenderer prototype) {
        var palette = new Palette(PaletteID.ACTORS, 10);

        palette.addTool(actorTool(editor, WorldMapPropertyName.POS_PAC,                  "Pac-Man", ArcadeSprites.PAC_MAN));
        palette.addTool(actorTool(editor, WorldMapPropertyName.POS_GHOST_1_RED,          "Red Ghost", ArcadeSprites.RED_GHOST));
        palette.addTool(actorTool(editor, WorldMapPropertyName.POS_GHOST_2_PINK,         "Pink Ghost", ArcadeSprites.PINK_GHOST));
        palette.addTool(actorTool(editor, WorldMapPropertyName.POS_GHOST_3_CYAN,         "Cyan Ghost", ArcadeSprites.CYAN_GHOST));
        palette.addTool(actorTool(editor, WorldMapPropertyName.POS_GHOST_4_ORANGE,       "Orange Ghost", ArcadeSprites.ORANGE_GHOST));
        palette.addTool(actorTool(editor, WorldMapPropertyName.POS_BONUS,                "Bonus", ArcadeSprites.STRAWBERRY));
        palette.addTool(actorTool(editor, WorldMapPropertyName.POS_SCATTER_RED_GHOST,    "Red Ghost Scatter", ArcadeSprites.RED_GHOST));
        palette.addTool(actorTool(editor, WorldMapPropertyName.POS_SCATTER_PINK_GHOST,   "Pink Ghost Scatter", ArcadeSprites.PINK_GHOST));
        palette.addTool(actorTool(editor, WorldMapPropertyName.POS_SCATTER_CYAN_GHOST,   "Cyan Ghost Scatter", ArcadeSprites.CYAN_GHOST));
        palette.addTool(actorTool(editor, WorldMapPropertyName.POS_SCATTER_ORANGE_GHOST, "Orange Ghost Scatter", ArcadeSprites.ORANGE_GHOST));

        palette.setSelectedToolIndex(0); // "No actor"

        var actorRenderer = new TerrainMapTileRenderer(palette);
        actorRenderer.backgroundColorProperty().bind(prototype.backgroundColorProperty());
        actorRenderer.mapColoringProperty().bind(prototype.mapColoringProperty());
        palette.setRenderer(actorRenderer);

        return palette;
    }

    private Palette createFoodPalette(TileMapEditor editor, FoodMapRenderer prototype) {
        var palette = new Palette(PaletteID.FOOD, 3);

        palette.addTool(new TileCodeTool(editor, WorldMapLayerID.FOOD, FoodTile.EMPTY.$, "No Food"));
        palette.addTool(new TileCodeTool(editor, WorldMapLayerID.FOOD, FoodTile.PELLET.$, "Pellet"));
        palette.addTool(new TileCodeTool(editor, WorldMapLayerID.FOOD, FoodTile.ENERGIZER.$, "Energizer"));

        palette.setSelectedToolIndex(0); // "No Food"

        var foodRenderer = new FoodMapRenderer(palette);
        foodRenderer.backgroundColorProperty().bind(prototype.backgroundColorProperty());
        foodRenderer.energizerColorProperty().bind(prototype.energizerColorProperty());
        foodRenderer.pelletColorProperty().bind(prototype.pelletColorProperty());
        palette.setRenderer(foodRenderer);

        return palette;
    }
}