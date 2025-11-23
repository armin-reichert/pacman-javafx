/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.mapeditor.palette;

import de.amr.pacmanfx.mapeditor.EditorGlobals;
import de.amr.pacmanfx.uilib.rendering.Renderer;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public abstract class PropertyValueEditorTool implements PaletteTool {

    protected final String propertyName;
    protected final String description;

    protected PropertyValueEditorTool(String propertyName, String description) {
        this.propertyName = propertyName;
        this.description = description;
    }

    @Override
    public String description() {
        return description;
    }

    @Override
    public void draw(Renderer renderer, int row, int col) {
        GraphicsContext g = renderer.ctx();
        g.setFill(Color.BLACK);
        g.fillRect(col * EditorGlobals.TOOL_SIZE, row * EditorGlobals.TOOL_SIZE, EditorGlobals.TOOL_SIZE, EditorGlobals.TOOL_SIZE);
    }
}