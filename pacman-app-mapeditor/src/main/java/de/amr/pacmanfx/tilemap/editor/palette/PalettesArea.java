/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tilemap.editor.palette;

import de.amr.pacmanfx.lib.tilemap.FoodTile;
import de.amr.pacmanfx.lib.tilemap.TerrainTile;
import de.amr.pacmanfx.model.WorldMapProperty;
import de.amr.pacmanfx.tilemap.editor.EditorUI;
import de.amr.pacmanfx.tilemap.editor.actions.Action_SetFoodProperty;
import de.amr.pacmanfx.tilemap.editor.actions.Action_SetTerrainProperty;
import de.amr.pacmanfx.tilemap.editor.actions.Action_SetTileCode;
import de.amr.pacmanfx.tilemap.editor.rendering.ArcadeSprites;
import de.amr.pacmanfx.tilemap.editor.rendering.TerrainTileMapRenderer;
import de.amr.pacmanfx.uilib.tilemap.FoodMapRenderer;
import javafx.geometry.Insets;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.text.Text;

import java.util.EnumMap;
import java.util.Map;

import static de.amr.pacmanfx.lib.tilemap.WorldMapFormatter.formatTile;
import static de.amr.pacmanfx.tilemap.editor.EditorGlobals.*;

public class PalettesArea extends TabPane {

    private final Map<PaletteID, Palette> palettes = new EnumMap<>(PaletteID.class);

    // Must be called after edit canvas creation because it binds to the renderers of the edit canvas!
    public PalettesArea(EditorUI ui, TerrainTileMapRenderer terrainRenderer, FoodMapRenderer foodRenderer) {

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
        setPadding(new Insets(5, 5, 5, 5));
        setMinHeight(75);

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

    private Palette createTerrainPalette(EditorUI ui, TerrainTileMapRenderer renderer) {
        var palette = new Palette(PaletteID.TERRAIN, TOOL_SIZE, 1, 13);
        palette.addTool(makeTileTool(ui, TerrainTile.EMPTY.$, "Empty Space"));
        palette.addTool(makeTileTool(ui, TerrainTile.WALL_H.$, "Horizontal Wall"));
        palette.addTool(makeTileTool(ui, TerrainTile.WALL_V.$, "Vertical Wall"));
        palette.addTool(makeTileTool(ui, TerrainTile.ARC_NW.$, "NW Corner"));
        palette.addTool(makeTileTool(ui, TerrainTile.ARC_NE.$, "NE Corner"));
        palette.addTool(makeTileTool(ui, TerrainTile.ARC_SW.$, "SW Corner"));
        palette.addTool(makeTileTool(ui, TerrainTile.ARC_SE.$, "SE Corner"));
        palette.addTool(makeTileTool(ui, TerrainTile.TUNNEL.$, "Tunnel"));
        palette.addTool(makeTileTool(ui, TerrainTile.DOOR.$, "Door"));
        palette.addTool(makeTileTool(ui, TerrainTile.ONE_WAY_UP.$, "One-Way Up"));
        palette.addTool(makeTileTool(ui, TerrainTile.ONE_WAY_RIGHT.$, "One-Way Right"));
        palette.addTool(makeTileTool(ui, TerrainTile.ONE_WAY_DOWN.$, "One-Way Down"));
        palette.addTool(makeTileTool(ui, TerrainTile.ONE_WAY_LEFT.$, "One-Way Left"));

        palette.selectTool(0); // "No Tile"

        TerrainTileMapRenderer paletteRenderer = new TerrainTileMapRenderer(palette.canvas());
        paletteRenderer.backgroundColorProperty().bind(renderer.backgroundColorProperty());
        paletteRenderer.colorSchemeProperty().bind(renderer.colorSchemeProperty());
        palette.setRenderer(paletteRenderer);

        return palette;
    }

    private Palette createActorsPalette(EditorUI ui, TerrainTileMapRenderer renderer) {
        var palette = new Palette(PaletteID.ACTORS, TOOL_SIZE, 1, 11);
        palette.addTool(new ActorTool(ui, WorldMapProperty.POS_PAC, "Pac-Man", ArcadeSprites.PAC_MAN));
        palette.addTool(new ActorTool(ui, WorldMapProperty.POS_RED_GHOST, "Red Ghost", ArcadeSprites.RED_GHOST));
        palette.addTool(new ActorTool(ui, WorldMapProperty.POS_PINK_GHOST, "Pink Ghost", ArcadeSprites.PINK_GHOST));
        palette.addTool(new ActorTool(ui, WorldMapProperty.POS_CYAN_GHOST, "Cyan Ghost", ArcadeSprites.CYAN_GHOST));
        palette.addTool(new ActorTool(ui, WorldMapProperty.POS_ORANGE_GHOST, "Orange Ghost", ArcadeSprites.ORANGE_GHOST));
        palette.addTool(new ActorTool(ui, WorldMapProperty.POS_BONUS, "Bonus", ArcadeSprites.STRAWBERRY));
        palette.addTool(makePropertyTool(ui, WorldMapProperty.POS_SCATTER_RED_GHOST, "Red Ghost Scatter"));
        palette.addTool(makePropertyTool(ui, WorldMapProperty.POS_SCATTER_PINK_GHOST, "Pink Ghost Scatter"));
        palette.addTool(makePropertyTool(ui, WorldMapProperty.POS_SCATTER_CYAN_GHOST, "Cyan Ghost Scatter"));
        palette.addTool(makePropertyTool(ui, WorldMapProperty.POS_SCATTER_ORANGE_GHOST, "Orange Ghost Scatter"));
        palette.selectTool(0); // "No actor"

        TerrainTileMapRenderer paletteRenderer = new TerrainTileMapRenderer(palette.canvas());
        paletteRenderer.backgroundColorProperty().bind(renderer.backgroundColorProperty());
        paletteRenderer.colorSchemeProperty().bind(renderer.colorSchemeProperty());
        palette.setRenderer(paletteRenderer);

        return palette;
    }

    private Palette createFoodPalette(EditorUI ui, FoodMapRenderer renderer) {
        var palette = new Palette(PaletteID.FOOD, TOOL_SIZE, 1, 3);
        palette.addTool(makeTileTool(ui, FoodTile.EMPTY.code(), "No Food"));
        palette.addTool(makeTileTool(ui, FoodTile.PELLET.code(), "Pellet"));
        palette.addTool(makeTileTool(ui, FoodTile.ENERGIZER.code(), "Energizer"));
        palette.selectTool(0); // "No Food"

        FoodMapRenderer foodRenderer = new FoodMapRenderer(palette.canvas());
        foodRenderer.backgroundColorProperty().bind(renderer.backgroundColorProperty());
        foodRenderer.energizerColorProperty().bind(renderer.energizerColorProperty());
        foodRenderer.pelletColorProperty().bind(renderer.pelletColorProperty());
        palette.setRenderer(foodRenderer);

        return palette;
    }

    private TileValueEditorTool makeTileTool(EditorUI ui, byte code, String description) {
        return new TileValueEditorTool(
            (layerID, tile) -> new Action_SetTileCode(ui, ui.editor().currentWorldMap(), layerID, tile, code).execute(),
            TOOL_SIZE, code, description);
    }

    protected PropertyValueEditorTool makePropertyTool(EditorUI ui, String propertyName, String description) {
        return new PropertyValueEditorTool(
            (layerID, tile) -> {
                switch (layerID) {
                    case FOOD -> new Action_SetFoodProperty(ui.editor(), propertyName, formatTile(tile)).execute();
                    case TERRAIN -> new Action_SetTerrainProperty(ui.editor(), propertyName, formatTile(tile)).execute();
                }
            },
            TOOL_SIZE, propertyName, description);
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
