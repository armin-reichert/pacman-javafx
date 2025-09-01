/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tilemap.editor.palette;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.uilib.tilemap.TileRenderer;
import javafx.scene.paint.Color;

import java.util.function.BiConsumer;

import static java.util.Objects.requireNonNull;

public class TileValueEditorTool implements EditorTool {

    private final double size;
    private final byte value;
    private final String description;

    private final BiConsumer<LayerID, Vector2i> editor;

    public TileValueEditorTool(BiConsumer<LayerID, Vector2i> editor, double size, byte value, String description) {
        this.editor = requireNonNull(editor);
        this.size = size;
        this.value = value;
        this.description = description;
    }

    @Override
    public String description() {
        return description;
    }

    @Override
    public BiConsumer<LayerID, Vector2i> editor() {
        return editor;
    }

    @Override
    public void draw(TileRenderer renderer, int row, int col) {
        renderer.ctx().setFill(Color.BLACK);
        renderer.ctx().fillRect(col * size, row * size, size, size);
        renderer.drawTile(new Vector2i(col, row), value);
    }
}
