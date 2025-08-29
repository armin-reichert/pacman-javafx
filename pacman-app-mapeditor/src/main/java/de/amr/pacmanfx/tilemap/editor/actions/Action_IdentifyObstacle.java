package de.amr.pacmanfx.tilemap.editor.actions;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.*;
import de.amr.pacmanfx.tilemap.editor.MessageType;
import de.amr.pacmanfx.tilemap.editor.TileMapEditorUI;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.lib.UsefulFunctions.tileAt;
import static java.util.Objects.requireNonNull;

public class Action_IdentifyObstacle extends AbstractEditorUIAction<String> {

    private final Vector2i tile;

    public Action_IdentifyObstacle(TileMapEditorUI ui, Vector2i tile) {
        super(ui);
        this.tile = requireNonNull(tile);
    }

    @Override
    public String execute() {
        WorldMap worldMap = editor.currentWorldMap();
        Obstacle obstacleStartingAtTile = worldMap.obstacles().stream()
            .filter(obstacle -> tileAt(obstacle.startPoint().minus(HTS, 0).toVector2f()).equals(tile))
            .findFirst().orElse(null);
        if (obstacleStartingAtTile != null) {
            String encoding = obstacleStartingAtTile.encoding();
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(encoding);
            clipboard.setContent(content);
            ui.messageDisplay().showMessage("Obstacle (copied to clipboard)", 5, MessageType.INFO);
            return encoding;
        } else {
            byte terrainCode = worldMap.content(LayerID.TERRAIN, tile);
            byte foodCode = worldMap.content(LayerID.FOOD, tile);
            String info = "";
            if (terrainCode != TerrainTile.EMPTY.code())
                info = "Terrain #%02X (%s)".formatted(terrainCode, terrainCode);
            if (foodCode != FoodTile.EMPTY.code())
                info = "Food #%02X (%s)".formatted(foodCode, foodCode);
            ui.messageDisplay().showMessage(info, 4, MessageType.INFO);
            return null;
        }
    }
}
