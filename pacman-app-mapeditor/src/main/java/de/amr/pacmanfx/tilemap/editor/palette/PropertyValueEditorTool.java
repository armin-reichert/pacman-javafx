/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tilemap.editor.palette;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.uilib.rendering.CanvasRenderer;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.function.BiConsumer;

import static de.amr.pacmanfx.tilemap.editor.EditorGlobals.TOOL_SIZE;
import static java.util.Objects.requireNonNull;

public abstract class PropertyValueEditorTool implements EditorTool {

    protected final String propertyName;
    protected final String description;

    protected BiConsumer<LayerID, Vector2i> editor;

    public PropertyValueEditorTool(BiConsumer<LayerID, Vector2i> editor, String propertyName, String description) {
        this.editor = requireNonNull(editor);
        this.propertyName = propertyName;
        this.description = description;
    }

    protected PropertyValueEditorTool(String propertyName, String description) {
        this.propertyName = propertyName;
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
    public void draw(CanvasRenderer renderer, int row, int col) {
        GraphicsContext g = renderer.ctx();
        g.setFill(Color.BLACK);
        g.fillRect(col * TOOL_SIZE, row * TOOL_SIZE, TOOL_SIZE, TOOL_SIZE);
    }
}
