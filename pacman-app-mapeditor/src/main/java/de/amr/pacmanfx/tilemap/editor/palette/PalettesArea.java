/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tilemap.editor.palette;

import de.amr.pacmanfx.lib.tilemap.FoodTile;
import de.amr.pacmanfx.lib.tilemap.TerrainTile;
import de.amr.pacmanfx.model.WorldMapProperty;
import de.amr.pacmanfx.tilemap.editor.EditorUI;
import de.amr.pacmanfx.tilemap.editor.rendering.ArcadeSprites;
import de.amr.pacmanfx.tilemap.editor.rendering.TerrainMapTileRenderer;
import de.amr.pacmanfx.uilib.tilemap.FoodMapRenderer;
import javafx.geometry.Insets;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.text.Text;

import java.util.EnumMap;
import java.util.Map;

import static de.amr.pacmanfx.tilemap.editor.EditorGlobals.*;

public class PalettesArea extends TabPane {

    private final Map<PaletteID, Palette> palettes = new EnumMap<>(PaletteID.class);

    // Must be called after edit canvas creation because it binds to the renderers of the edit canvas!
    public PalettesArea(EditorUI ui, TerrainMapTileRenderer terrainRenderer, FoodMapRenderer foodRenderer) {

        palettes.put(PaletteID.TERRAIN, createTerrainPalette(ui, terrainRenderer));
        palettes.put(PaletteID.FOOD, createFoodPalette(ui, foodRenderer));
        palettes.put(PaletteID.ACTORS, createActorsPalette(ui, terrainRenderer));

        var tabTerrain = new Tab("", palettes.get(PaletteID.TERRAIN).root());
        tabTerrain.setGraphic(new Text(translated("terrain")));
        tabTerrain.setClosable(false);
        tabTerrain.setUserData(PaletteID.TERRAIN);

        var tabPellets = new Tab("", palettes.get(PaletteID.FOOD).root());
        tabPellets.setGraphic(new Text(translated("pellets")));
        tabPellets.setClosable(false);
        tabPellets.setUserData(PaletteID.FOOD);

        var tabActors = new Tab("", palettes.get(PaletteID.ACTORS).root());
        tabActors.setGraphic(new Text(translated("actors")));
        tabActors.setClosable(false);
        tabActors.setUserData(PaletteID.ACTORS);

        getTabs().setAll(tabTerrain, tabPellets, tabActors);

        setMinHeight(80);
        setPadding(new Insets(5, 5, 5, 5));

        getSelectionModel().selectedItemProperty().addListener((py, ov, selectedTab) -> markSelectedPalettesTab(selectedTab));
        markSelectedPalettesTab(getSelectionModel().getSelectedItem());
    }

    private void markSelectedPalettesTab(Tab selectedTab) {
        for (Tab tab : getTabs()) {
            if (tab.getGraphic() instanceof Text text) {
                text.setFont(tab == selectedTab ? FONT_SELECTED_PALETTE : FONT_UNSELECTED_PALETTE);
            }
        }
    }

    private Palette createTerrainPalette(EditorUI ui, TerrainMapTileRenderer renderer) {
        var palette = new Palette(PaletteID.TERRAIN, 1, 13);
        palette.addTool(new TileCodeEditorTool(ui, TerrainTile.EMPTY.$, "Empty Space"));
        palette.addTool(new TileCodeEditorTool(ui, TerrainTile.WALL_H.$, "Horizontal Wall"));
        palette.addTool(new TileCodeEditorTool(ui, TerrainTile.WALL_V.$, "Vertical Wall"));
        palette.addTool(new TileCodeEditorTool(ui, TerrainTile.ARC_NW.$, "NW Corner"));
        palette.addTool(new TileCodeEditorTool(ui, TerrainTile.ARC_NE.$, "NE Corner"));
        palette.addTool(new TileCodeEditorTool(ui, TerrainTile.ARC_SW.$, "SW Corner"));
        palette.addTool(new TileCodeEditorTool(ui, TerrainTile.ARC_SE.$, "SE Corner"));
        palette.addTool(new TileCodeEditorTool(ui, TerrainTile.TUNNEL.$, "Tunnel"));
        palette.addTool(new TileCodeEditorTool(ui, TerrainTile.DOOR.$, "Door"));
        palette.addTool(new TileCodeEditorTool(ui, TerrainTile.ONE_WAY_UP.$, "One-Way Up"));
        palette.addTool(new TileCodeEditorTool(ui, TerrainTile.ONE_WAY_RIGHT.$, "One-Way Right"));
        palette.addTool(new TileCodeEditorTool(ui, TerrainTile.ONE_WAY_DOWN.$, "One-Way Down"));
        palette.addTool(new TileCodeEditorTool(ui, TerrainTile.ONE_WAY_LEFT.$, "One-Way Left"));

        palette.selectTool(0); // "No Tile"

        TerrainMapTileRenderer paletteRenderer = new TerrainMapTileRenderer(palette.canvas());
        paletteRenderer.backgroundColorProperty().bind(renderer.backgroundColorProperty());
        paletteRenderer.colorSchemeProperty().bind(renderer.colorSchemeProperty());
        palette.setRenderer(paletteRenderer);

        return palette;
    }

    private Palette createActorsPalette(EditorUI ui, TerrainMapTileRenderer renderer) {
        var palette = new Palette(PaletteID.ACTORS, 1, 11);
        palette.addTool(new ActorTool(ui, WorldMapProperty.POS_PAC, "Pac-Man", ArcadeSprites.PAC_MAN));
        palette.addTool(new ActorTool(ui, WorldMapProperty.POS_RED_GHOST, "Red Ghost", ArcadeSprites.RED_GHOST));
        palette.addTool(new ActorTool(ui, WorldMapProperty.POS_PINK_GHOST, "Pink Ghost", ArcadeSprites.PINK_GHOST));
        palette.addTool(new ActorTool(ui, WorldMapProperty.POS_CYAN_GHOST, "Cyan Ghost", ArcadeSprites.CYAN_GHOST));
        palette.addTool(new ActorTool(ui, WorldMapProperty.POS_ORANGE_GHOST, "Orange Ghost", ArcadeSprites.ORANGE_GHOST));
        palette.addTool(new ActorTool(ui, WorldMapProperty.POS_BONUS, "Bonus", ArcadeSprites.STRAWBERRY));
        palette.addTool(new ScatterTileTool(ui, WorldMapProperty.POS_SCATTER_RED_GHOST, "Red Ghost Scatter"));
        palette.addTool(new ScatterTileTool(ui, WorldMapProperty.POS_SCATTER_PINK_GHOST, "Pink Ghost Scatter"));
        palette.addTool(new ScatterTileTool(ui, WorldMapProperty.POS_SCATTER_CYAN_GHOST, "Cyan Ghost Scatter"));
        palette.addTool(new ScatterTileTool(ui, WorldMapProperty.POS_SCATTER_ORANGE_GHOST, "Orange Ghost Scatter"));
        palette.selectTool(0); // "No actor"

        TerrainMapTileRenderer paletteRenderer = new TerrainMapTileRenderer(palette.canvas());
        paletteRenderer.backgroundColorProperty().bind(renderer.backgroundColorProperty());
        paletteRenderer.colorSchemeProperty().bind(renderer.colorSchemeProperty());
        palette.setRenderer(paletteRenderer);

        return palette;
    }

    private Palette createFoodPalette(EditorUI ui, FoodMapRenderer renderer) {
        var palette = new Palette(PaletteID.FOOD, 1, 3);
        palette.addTool(new TileCodeEditorTool(ui, FoodTile.EMPTY.code(), "No Food"));
        palette.addTool(new TileCodeEditorTool(ui, FoodTile.PELLET.code(), "Pellet"));
        palette.addTool(new TileCodeEditorTool(ui, FoodTile.ENERGIZER.code(), "Energizer"));
        palette.selectTool(0); // "No Food"

        FoodMapRenderer foodRenderer = new FoodMapRenderer(palette.canvas());
        foodRenderer.backgroundColorProperty().bind(renderer.backgroundColorProperty());
        foodRenderer.energizerColorProperty().bind(renderer.energizerColorProperty());
        foodRenderer.pelletColorProperty().bind(renderer.pelletColorProperty());
        palette.setRenderer(foodRenderer);

        return palette;
    }

    public void draw() {
        palettes.values().forEach(Palette::draw);
        palettes.get(selectedPaletteID()).draw();
    }

    public PaletteID selectedPaletteID() {
        return (PaletteID) getSelectionModel().getSelectedItem().getUserData();
    }

    public Palette selectedPalette() {
        return palettes.get(selectedPaletteID());
    }
}
