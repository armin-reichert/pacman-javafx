package de.amr.pacmanfx.tilemap.editor;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.lib.UsefulFunctions.tileAt;

public class Action_IdentifyObstacle extends AbstractEditorAction {

    public void setTile(Vector2i tile) {
        setArg("tile", tile);
    }

    @Override
    public Object execute(TileMapEditor editor) {
        Vector2i tile = getArg("tile", Vector2i.class);
        WorldMap worldMap = editor.currentWorldMap();
        Obstacle obstacleAtTile = worldMap.obstacles().stream()
                .filter(obstacle -> tileAt(obstacle.startPoint().minus(HTS, 0).toVector2f()).equals(tile))
                .findFirst().orElse(null);
        if (obstacleAtTile != null) {
            String encoding = obstacleAtTile.encoding();
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(encoding);
            clipboard.setContent(content);
            editor.showMessage("Obstacle (copied to clipboard)", 5, MessageType.INFO);
        } else {
            byte terrainValue = worldMap.content(LayerID.TERRAIN, tile);
            byte foodValue = worldMap.content(LayerID.FOOD, tile);
            String info = "";
            if (terrainValue != TerrainTile.EMPTY.$)
                info = "Terrain #%02X (%s)".formatted(terrainValue, terrainValue);
            if (foodValue != FoodTile.EMPTY.code())
                info = "Food #%02X (%s)".formatted(foodValue, foodValue);
            editor.showMessage(info, 4, MessageType.INFO);
        }
        return null;
    }
}
