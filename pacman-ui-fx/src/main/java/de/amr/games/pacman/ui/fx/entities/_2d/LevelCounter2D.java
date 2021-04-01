package de.amr.games.pacman.ui.fx.entities._2d;

import static de.amr.games.pacman.model.world.PacManGameWorld.TS;
import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import java.util.List;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.ui.fx.rendering.GameRendering2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class LevelCounter2D {

	private V2i tileRight;
	private int currentLevelNumber;
	private Image spritesheet;
	private List<Rectangle2D> symbolSprites;
	private List<Byte> levelSymbols;

	public void render(GraphicsContext g) {
		int x = tileRight.x * TS, y = tileRight.y * TS;
		int firstLevel = Math.max(1, currentLevelNumber - 6);
		for (int level = firstLevel; level <= currentLevelNumber; ++level) {
			Rectangle2D sprite = symbolSprites.get(levelSymbols.get(level - 1));
			g.drawImage(spritesheet, sprite.getMinX(), sprite.getMinY(), sprite.getWidth(), sprite.getHeight(), x, y,
					sprite.getWidth(), sprite.getHeight());
			x -= t(2);
		}
	}

	public void setTileRight(V2i tileRight) {
		this.tileRight = tileRight;
	}

	public void setCurrentLevelNumber(int currentLevelNumber) {
		this.currentLevelNumber = currentLevelNumber;
	}

	public void setSpritesheet(Image spritesheet) {
		this.spritesheet = spritesheet;
	}

	public void setSymbolSprites(List<Rectangle2D> symbolSprites) {
		this.symbolSprites = symbolSprites;
	}

	public void setLevelSymbols(List<Byte> levelSymbols) {
		this.levelSymbols = levelSymbols;
	}

	public void setRendering(GameRendering2D rendering) {
		setSpritesheet(rendering.spritesheet);
		setSymbolSprites(rendering.getSymbolSprites());
	}
}