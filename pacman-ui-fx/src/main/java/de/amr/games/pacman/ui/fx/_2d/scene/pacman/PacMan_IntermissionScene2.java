/*
MIT License

Copyright (c) 2021-22 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacman.ui.fx._2d.scene.pacman;

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.controller.pacman.Intermission2Controller;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.lib.animation.ThingAnimation;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.ui.fx._2d.rendering.common.GhostAnimations;
import de.amr.games.pacman.ui.fx._2d.rendering.common.PacAnimations;
import de.amr.games.pacman.ui.fx._2d.rendering.pacman.Spritesheet_PacMan;
import de.amr.games.pacman.ui.fx._2d.scene.common.GameScene2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;

/**
 * Second intermission scene: Blinky pursues Pac but kicks a nail that tears his dress apart.
 * 
 * @author Armin Reichert
 */
public class PacMan_IntermissionScene2 extends GameScene2D {

	private Intermission2Controller sceneController;
	private Intermission2Controller.Context $;
	private ThingAnimation<Rectangle2D> blinkyStretchedAnimation;
	private ThingAnimation<Rectangle2D> blinkyDamagedAnimation;

	@Override
	public void setSceneContext(GameController gameController) {
		super.setSceneContext(gameController);
		sceneController = new Intermission2Controller(gameController);
		$ = sceneController.context();
	}

	@Override
	public void init() {
		sceneController.init();

		$.pac.setAnimations(new PacAnimations(r2D));
		$.pac.animations().get().ensureRunning();
		$.blinky.setAnimations(new GhostAnimations($.blinky.id, r2D));
		$.blinky.animations().get().ensureRunning();
		blinkyStretchedAnimation = ((Spritesheet_PacMan) r2D).createBlinkyStretchedAnimation();
		blinkyDamagedAnimation = ((Spritesheet_PacMan) r2D).createBlinkyDamagedAnimation();
	}

	@Override
	public void doUpdate() {
		sceneController.update();
	}

	@Override
	public void doRender(GraphicsContext g) {
		r2D.drawLevelCounter(g, game.levelCounter);
		r2D.drawPac(g, $.pac);
		r2D.drawEntity(g, $.nail, Spritesheet_PacMan.get().getNail());
		if (sceneController.context().nailDistance() < 0) {
			r2D.drawGhost(g, $.blinky);
		} else {
			drawBlinkyStretched($.blinky, $.nail.position, sceneController.context().nailDistance() / 4);
		}
	}

	private void drawBlinkyStretched(Ghost blinky, V2d nailPosition, int stretching) {
		var g = canvas.getGraphicsContext2D();
		Rectangle2D stretchedDress = blinkyStretchedAnimation.frame(stretching);
		r2D.drawSprite(g, stretchedDress, (int) (nailPosition.x - 4), (int) (nailPosition.y - 4));
		if (stretching < 3) {
			r2D.drawGhost(g, $.blinky);
		} else {
			Rectangle2D damagedDress = blinkyDamagedAnimation.frame(blinky.moveDir() == Direction.UP ? 0 : 1);
			r2D.drawSprite(g, damagedDress, (int) (blinky.position.x - 4), (int) (blinky.position.y - 4));
		}
	}
}