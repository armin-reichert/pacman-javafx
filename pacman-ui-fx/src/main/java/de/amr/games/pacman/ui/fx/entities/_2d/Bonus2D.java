package de.amr.games.pacman.ui.fx.entities._2d;

import java.util.List;
import java.util.Map;

import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.model.pacman.PacManBonus;
import de.amr.games.pacman.ui.fx.rendering.GameRendering2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;

public class Bonus2D extends Renderable2D {

	private PacManBonus bonus;
	private List<Rectangle2D> symbolSprites;
	private Map<Integer, Rectangle2D> numberSprites;
	private TimedSequence<Integer> jumpAnimation;

	public Bonus2D(GameRendering2D rendering) {
		super(rendering);
		setSymbolSprites(rendering.getSymbolSprites());
		setNumberSprites(rendering.getBonusNumbersSpritesMap());
		setJumpAnimation(rendering.createBonusAnimation());
	}

	public void setBonus(PacManBonus bonus) {
		this.bonus = bonus;
	}

	public TimedSequence<Integer> getJumpAnimation() {
		return jumpAnimation;
	}

	public void setJumpAnimation(TimedSequence<Integer> jumpAnimation) {
		this.jumpAnimation = jumpAnimation;
	}

	public void setSymbolSprites(List<Rectangle2D> symbolSprites) {
		this.symbolSprites = symbolSprites;
	}

	public void setNumberSprites(Map<Integer, Rectangle2D> numberSprites) {
		this.numberSprites = numberSprites;
	}

	@Override
	public void render(GraphicsContext g) {
		Rectangle2D sprite = currentSprite();
		if (sprite == null || !bonus.visible) {
			return;
		}
		// Ms. Pac.Man bonus is jumping up and down while wandering the maze
		int jump = jumpAnimation != null ? jumpAnimation.animate() : 0;
		g.translate(0, jump);
		renderEntity(g, bonus, sprite);
		g.translate(0, -jump);
	}

	private Rectangle2D currentSprite() {
		if (bonus == null) {
			return null;
		}
		if (bonus.edibleTicksLeft > 0) {
			return symbolSprites.get(bonus.symbol);
		}
		if (bonus.eatenTicksLeft > 0) {
			return numberSprites.get(bonus.points);
		}
		return null;
	}
}