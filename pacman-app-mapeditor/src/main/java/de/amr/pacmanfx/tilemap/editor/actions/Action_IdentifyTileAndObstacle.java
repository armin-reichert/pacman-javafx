package de.amr.pacmanfx.tilemap.editor.actions;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.*;
import de.amr.pacmanfx.tilemap.editor.EditorUI;
import de.amr.pacmanfx.tilemap.editor.MessageType;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.lib.UsefulFunctions.tileAt;
import static java.util.Objects.requireNonNull;

public class Action_IdentifyTileAndObstacle extends AbstractEditorUIAction<String> {

    private final Vector2i tile;

    public Action_IdentifyTileAndObstacle(EditorUI ui, Vector2i tile) {
        super(ui);
        this.tile = requireNonNull(tile);
    }

    @Override
    public String execute() {
        WorldMap worldMap = editor.currentWorldMap();
        byte terrainCode = worldMap.content(LayerID.TERRAIN, tile), foodCode = worldMap.content(LayerID.FOOD, tile);
        boolean terrainEmpty = terrainCode == TerrainTile.EMPTY.code(), foodEmpty = foodCode == FoodTile.EMPTY.code();
        String info = "";
        if (!terrainEmpty) {
            info += " Terrain 0x%02X (%s)".formatted(terrainCode, terrainTileName(terrainCode));
        }
        if (!foodEmpty) {
            info += " Food 0x%02X (%s)".formatted(foodCode, foodTileName(foodCode));
        }
        ui.messageDisplay().showMessage(info.trim(), 4, MessageType.INFO);

        return identifyObstacleStartingAtTile(worldMap);
    }

    private String terrainInfo(byte terrainCode) {
        return "Terrain 0x%02X (%s)".formatted(terrainCode, terrainTileName(terrainCode));
    }

    private String foodInfo(byte foodCode) {
        return "Food 0x%02X (%s)".formatted(foodCode, foodTileName(foodCode));
    }

    private String identifyObstacleStartingAtTile(WorldMap worldMap) {
        Obstacle obstacleStartingAtTile = worldMap.obstacles().stream()
            .filter(obstacle -> tileAt(obstacle.startPoint().minus(HTS, 0).toVector2f()).equals(tile))
            .findFirst().orElse(null);
        if (obstacleStartingAtTile == null) return null;
        String encoding = obstacleStartingAtTile.encoding();
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(encoding);
        clipboard.setContent(content);
        return encoding;
    }

    private String foodTileName(byte code) {
        return switch (code) {
            case 0x00 -> "EMPTY";
            case 0x01 -> "PELLET";
            case 0x02 -> "ENERGIZER";
            default -> "UNKNOWN";
        };
    }
    private String terrainTileName(byte code) {
        return switch (code) {
            case 0x00 -> "EMPTY";
            case 0x01 -> "WALL_H";
            case 0x02 -> "WALL_V";
            case 0x03 -> "ARC_NW";
            case 0x04 -> "ARC_NE";
            case 0x05 -> "ARC_SE";
            case 0x06 -> "ARC_SW";
            case 0x07 -> "TUNNEL";
            case 0x0e -> "DOOR";
            case 0x10 -> "DARC_NW";
            case 0x11 -> "DARC_NE";
            case 0x12 -> "DARC_SE";
            case 0x13 -> "DARC_SW";
            case 0x14 -> "ONE_WAY_UP";
            case 0x15 -> "ONE_WAY_RIGHT";
            case 0x16 -> "ONE_WAY_DOWN";
            case 0x17 -> "ONE_WAY_LEFT";
            default   -> "UNKNOWN";
        };
    }
}
