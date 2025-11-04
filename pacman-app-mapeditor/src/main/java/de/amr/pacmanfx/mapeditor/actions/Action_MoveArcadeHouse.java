package de.amr.pacmanfx.mapeditor.actions;

import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.mapeditor.TileMapEditor;

import static java.util.Objects.requireNonNull;

public class Action_MoveArcadeHouse extends EditorAction<Void> {

    private final Vector2i minTile;

    public Action_MoveArcadeHouse(TileMapEditor editor, Vector2i minTile) {
        super(editor);
        this.minTile = requireNonNull(minTile);
    }

    @Override
    public Void execute() {
        //TODO if house cannot be placed at given position, do not delete it!
        new Action_DeleteArcadeHouse(editor).execute();
        new Action_PlaceArcadeHouse(editor, minTile).execute();
        return null;
    }
}