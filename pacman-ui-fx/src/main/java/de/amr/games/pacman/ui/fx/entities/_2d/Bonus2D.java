package de.amr.games.pacman.ui.fx.entities._2d;

import java.util.Map;

import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.model.pacman.Bonus;
import de.amr.games.pacman.ui.fx.rendering.Rendering2D;
import de.amr.games.pacman.ui.fx.rendering.Rendering2D_MsPacMan;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;

/**
 * 2D representation of the bonus in Pac-Man and Ms. Pac-Man.
 * 
 * @author Armin Reichert
 */
public class Bonus2D implements Renderable2D {

	private Rendering2D rendering;
	private Bonus bonus;
	private Map<String, Rectangle2D> symbolSprites;
	private Map<Integer, Rectangle2D> numberSprites;
	private TimedSequence<Integer> jumpAnimation;

	public Bonus2D(Rendering2D rendering) {
		this.rendering = rendering;
		symbolSprites = rendering.getSymbolSprites();
		numberSprites = rendering.getBonusValuesSpritesMap();
		if (rendering instanceof Rendering2D_MsPacMan) {
			Rendering2D_MsPacMan msPacManRendering = (Rendering2D_MsPacMan) rendering;
			setJumpAnimation(msPacManRendering.createBonusAnimation());
		}
	}

	public void setBonus(Bonus bonus) {
		this.bonus = bonus;
	}

	public TimedSequence<Integer> getJumpAnimation() {
		return jumpAnimation;
	}

	public void setJumpAnimation(TimedSequence<Integer> jumpAnimation) {
		this.jumpAnimation = jumpAnimation;
	}

	@Override
	public void render(GraphicsContext g) {
		Rectangle2D sprite = currentSprite();
		if (sprite == null || !bonus.visible) {
			return;
		}
		// Ms. Pac.Man bonus is jumping up and down while wandering the maze
		int jump = jumpAnimation != null ? jumpAnimation.animate() : 0;
		g.save();
		g.translate(0, jump);
		rendering.renderEntity(g, bonus, sprite);
		g.restore();
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