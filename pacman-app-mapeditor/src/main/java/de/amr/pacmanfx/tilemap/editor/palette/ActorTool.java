package de.amr.pacmanfx.tilemap.editor.palette;

import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.tilemap.LayerID;
import de.amr.pacmanfx.model.WorldMapProperty;
import de.amr.pacmanfx.tilemap.editor.rendering.ArcadeSprites;
import de.amr.pacmanfx.uilib.tilemap.TileRenderer;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.function.BiConsumer;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.tilemap.editor.rendering.ArcadeSprites.PAC_MAN;

public class ActorTool extends PropertyValueEditorTool {

    public ActorTool(BiConsumer<LayerID, Vector2i> editor, double size, String propertyName, String description) {
        super(editor, size, propertyName, description);
    }

    @Override
    public void draw(TileRenderer renderer, int row, int col) {
        GraphicsContext g = renderer.ctx();
        g.setFill(Color.BLACK);
        g.fillRect(col * size, row * size, size, size);
        g.save();
        g.setImageSmoothing(true);
        g.scale(size / (double) TS, size / (double) TS);
        double x = col * TS, y = row * TS;
        switch (propertyName) {
            case WorldMapProperty.POS_PAC -> drawSprite(g, x, y, PAC_MAN);
            case WorldMapProperty.POS_RED_GHOST -> drawSprite(g, x, y, ArcadeSprites.RED_GHOST);
            case WorldMapProperty.POS_PINK_GHOST -> drawSprite(g, x, y, ArcadeSprites.PINK_GHOST);
            case WorldMapProperty.POS_CYAN_GHOST -> drawSprite(g, x, y, ArcadeSprites.CYAN_GHOST);
            case WorldMapProperty.POS_ORANGE_GHOST -> drawSprite(g, x, y, ArcadeSprites.ORANGE_GHOST);
            case WorldMapProperty.POS_BONUS -> drawSprite(g, x, y, ArcadeSprites.STRAWBERRY);
        }
        g.restore();
    }
}
