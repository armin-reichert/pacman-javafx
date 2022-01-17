/*
MIT License

Copyright (c) 2021 Armin Reichert

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

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.controller.pacman.Intermission2Controller;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.ui.PacManGameSound;
import de.amr.games.pacman.ui.PacManGameUI;
import de.amr.games.pacman.ui.fx._2d.entity.common.Ghost2D;
import de.amr.games.pacman.ui.fx._2d.entity.common.Player2D;
import de.amr.games.pacman.ui.fx._2d.entity.pacman.Nail2D;
import de.amr.games.pacman.ui.fx._2d.rendering.pacman.Rendering2D_PacMan;
import de.amr.games.pacman.ui.fx._2d.scene.common.AbstractGameScene2D;
import de.amr.games.pacman.ui.fx.scene.ScenesPacMan;
import javafx.geometry.Rectangle2D;

/**
 * Second intermission scene: Blinky pursues Pac but kicks a nail that tears his dress apart.
 * 
 * @author Armin Reichert
 */
public class PacMan_IntermissionScene2 extends AbstractGameScene2D {

	private final Intermission2Controller sceneController = new Intermission2Controller();

	private Player2D pacMan2D;
	private Ghost2D blinky2D;
	private Nail2D nail2D;
	private TimedSequence<Rectangle2D> blinkyStretchedAnimation;
	private TimedSequence<Rectangle2D> blinkyDamagedAnimation;

	public PacMan_IntermissionScene2(PacManGameUI ui) {
		super(ui, ScenesPacMan.RENDERING, ScenesPacMan.SOUNDS);
	}

	@Override
	public void init(PacManGameController gameController) {
		super.init(gameController);

		sceneController.playIntermissionSound = () -> sounds.play(PacManGameSound.INTERMISSION_2);
		sceneController.init(gameController);

		pacMan2D = new Player2D(sceneController.pac, rendering);
		blinky2D = new Ghost2D(sceneController.blinky, rendering);
		nail2D = new Nail2D(sceneController.nail, (Rendering2D_PacMan) rendering);
		pacMan2D.munchingAnimations.values().forEach(TimedSequence::restart);
		blinky2D.kickingAnimations.values().forEach(TimedSequence::restart);
		blinkyStretchedAnimation = ScenesPacMan.RENDERING.createBlinkyStretchedAnimation();
		blinkyDamagedAnimation = ScenesPacMan.RENDERING.createBlinkyDamagedAnimation();
	}

	@Override
	public void doUpdate() {
		sceneController.updateState();
	}

	@Override
	public void doRender() {
		renderLevelCounter();
		pacMan2D.render(gc);
		nail2D.render(gc);
		if (sceneController.nailDistance() < 0) {
			blinky2D.render(gc);
		} else {
			drawBlinkyStretched(sceneController.blinky, sceneController.nail.position, sceneController.nailDistance() / 4);
		}
	}

	private void drawBlinkyStretched(Ghost blinky, V2d nailPosition, int stretching) {
		Rectangle2D stretchedDress = blinkyStretchedAnimation.frame(stretching);
		rendering.renderSprite(gc, stretchedDress, (int) (nailPosition.x - 4), (int) (nailPosition.y - 4));
		if (stretching < 3) {
			blinky2D.render(gc);
		} else {
			Rectangle2D damagedDress = blinkyDamagedAnimation.frame(blinky.dir() == Direction.UP ? 0 : 1);
			rendering.renderSprite(gc, damagedDress, (int) (blinky.position.x - 4), (int) (blinky.position.y - 4));
		}
	}
}