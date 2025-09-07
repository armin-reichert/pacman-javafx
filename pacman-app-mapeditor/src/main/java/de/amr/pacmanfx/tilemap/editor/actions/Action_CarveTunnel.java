package de.amr.pacmanfx.tilemap.editor.actions;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.TerrainTile;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.tilemap.editor.EditorUI;
import org.tinylog.Logger;

import static java.util.Objects.requireNonNull;

public class Action_CarveTunnel extends AbstractEditorUIAction<Void> {

    private final WorldMap worldMap;
    private final Vector2i borderTile;
    private final int depth;

    public Action_CarveTunnel(EditorUI ui, WorldMap worldMap, Vector2i borderTile, int depth) {
        super(ui);
        this.worldMap = requireNonNull(worldMap);
        this.borderTile = requireNonNull(borderTile);
        this.depth = depth;
    }

    public Action_CarveTunnel(EditorUI ui, Vector2i borderTile, int depth) {
        this(ui, ui.editor().currentWorldMap(), borderTile, depth);
    }

    @Override
    public Void execute() {
        if (borderTile.x() != 0) {
            return null; // TODO disable action
        }
        if (borderTile.y() < GameLevel.EMPTY_ROWS_OVER_MAZE + 1) {
            return null;
        }
        if (borderTile.y() > worldMap.numRows() - 2 - GameLevel.EMPTY_ROWS_BELOW_MAZE) {
            return null;
        }
        boolean symmetricModeBefore = editor.symmetricEditMode();
        editor.setSymmetricEditMode(true);
        switch (depth) {
            case 1 -> {
                Vector2i tileAbove = borderTile.minus(0, 1);
                Vector2i tileBelow = borderTile.plus(0, 1);
                byte above = tileAbove.y() == GameLevel.EMPTY_ROWS_OVER_MAZE ? TerrainTile.WALL_H.$ : TerrainTile.ARC_SE.$;
                byte below = tileBelow.y() == worldMap.numRows() - 1 - GameLevel.EMPTY_ROWS_BELOW_MAZE ? TerrainTile.WALL_H.$ : TerrainTile.ARC_NE.$;
                new Action_SetTerrainTileCode(editor, worldMap, tileAbove, above).execute();
                new Action_SetTerrainTileCode(editor, worldMap, borderTile, TerrainTile.TUNNEL.$).execute();
                new Action_SetTerrainTileCode(editor, worldMap, tileBelow, below).execute();
            }
            default -> Logger.info("Tunnel of depth > 1 not yet supported");
        }

        editor.setSymmetricEditMode(symmetricModeBefore);
        return null;
    }
}
