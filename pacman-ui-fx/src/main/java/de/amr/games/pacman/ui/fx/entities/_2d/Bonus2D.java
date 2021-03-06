package de.amr.games.pacman.ui.fx.entities._2d;

import java.util.Map;
import java.util.Objects;

import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.model.pacman.Bonus;
import de.amr.games.pacman.ui.fx.rendering.Rendering2D;
import de.amr.games.pacman.ui.fx.rendering.Rendering2D_MsPacMan;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;

/**
 * 2D-representation of the bonus in Pac-Man and Ms. Pac-Man.
 * 
 * @author Armin Reichert
 */
public class Bonus2D implements Renderable2D {

	private final Rendering2D rendering;
	private final Bonus bonus;
	private final Map<String, Rectangle2D> symbolSprites;
	private final Map<Integer, Rectangle2D> numberSprites;
	private final TimedSequence<Integer> jumpingAnimation; // Ms. Pac-Man only

	public Bonus2D(Bonus bonus, Rendering2D rendering) {
		this.bonus = Objects.requireNonNull(bonus);
		this.rendering = Objects.requireNonNull(rendering);
		symbolSprites = rendering.getSymbolSprites();
		numberSprites = rendering.getBonusValuesSpritesMap();
		if (rendering instanceof Rendering2D_MsPacMan) {
			Rendering2D_MsPacMan msPacManRendering = (Rendering2D_MsPacMan) rendering;
			jumpingAnimation = msPacManRendering.createBonusAnimation();
		} else {
			jumpingAnimation = null;
		}
	}

	public void startAnimation() {
		if (jumpingAnimation != null) {
			jumpingAnimation.restart();
		}
	}

	public void stopAnimation() {
		if (jumpingAnimation != null) {
			jumpingAnimation.stop();
		}
	}

	@Override
	public void render(GraphicsContext g) {
		if (!bonus.isVisible()) {
			return;
		}
		Rectangle2D sprite = currentSprite();
		// Ms. Pac.Man bonus is jumping up and down while wandering the maze
		int dy = jumpingAnimation != null ? jumpingAnimation.animate() : 0;
		g.save();
		g.translate(0, dy);
		rendering.renderEntity(g, bonus, sprite);
		g.restore();
	}

	private Rectangle2D currentSprite() {
		if (bonus.state == Bonus.EDIBLE) {
			return symbolSprites.get(bonus.symbol);
		}
		if (bonus.state == Bonus.EATEN) {
			return numberSprites.get(bonus.points);
		}
		return null;
	}
}