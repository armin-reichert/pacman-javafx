package de.amr.games.pacman.ui.fx.entities._2d;

import static de.amr.games.pacman.model.world.PacManGameWorld.TS;
import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import java.util.List;
import java.util.function.IntSupplier;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.ui.fx.rendering.GameRendering2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;

/**
 * 2D level counter.
 * 
 * @author Armin Reichert
 */
public class LevelCounter2D<RENDERING extends GameRendering2D> extends Renderable2D<RENDERING> {

	private V2i tileRight;
	private IntSupplier levelNumberSupplier;
	private List<Byte> levelSymbols;

	public LevelCounter2D(RENDERING rendering) {
		super(rendering);
	}

	public void setRightUpperCorner(V2i tileRight) {
		this.tileRight = tileRight;
	}

	public void setLevelNumberSupplier(IntSupplier levelNumberSupplier) {
		this.levelNumberSupplier = levelNumberSupplier;
	}

	public void setLevelSymbols(List<Byte> levelSymbols) {
		this.levelSymbols = levelSymbols;
	}

	@Override
	public void render(GraphicsContext g) {
		int x = tileRight.x * TS, y = tileRight.y * TS;
		int firstLevel = Math.max(1, levelNumberSupplier.getAsInt() - 6);
		for (int level = firstLevel; level <= levelNumberSupplier.getAsInt(); ++level) {
			Rectangle2D sprite = rendering.getSymbolSprites().get(levelSymbols.get(level - 1));
			rendering.drawSprite(g, sprite, x, y);
			x -= t(2);
		}
	}
}