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

import static de.amr.games.pacman.model.common.world.World.t;

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.controller.pacman.Intermission2Controller;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.SpriteAnimation;
import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.ui.fx._2d.entity.common.Ghost2D;
import de.amr.games.pacman.ui.fx._2d.entity.common.LevelCounter2D;
import de.amr.games.pacman.ui.fx._2d.entity.common.Pac2D;
import de.amr.games.pacman.ui.fx._2d.entity.pacman.Nail2D;
import de.amr.games.pacman.ui.fx._2d.rendering.common.MyGhostAnimationSet;
import de.amr.games.pacman.ui.fx._2d.rendering.common.MyPacAnimationSet;
import de.amr.games.pacman.ui.fx._2d.rendering.pacman.Spritesheet_PacMan;
import de.amr.games.pacman.ui.fx._2d.scene.common.GameScene2D;
import de.amr.games.pacman.ui.fx.sound.GameSound;
import de.amr.games.pacman.ui.fx.sound.SoundManager;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;

/**
 * Second intermission scene: Blinky pursues Pac but kicks a nail that tears his dress apart.
 * 
 * @author Armin Reichert
 */
public class PacMan_IntermissionScene2 extends GameScene2D {

	private final Intermission2Controller sceneController;
	private final Intermission2Controller.Context context;

	private LevelCounter2D levelCounter2D;
	private Pac2D pacMan2D;
	private Ghost2D blinky2D;
	private Nail2D nail2D;
	private SpriteAnimation<Rectangle2D> blinkyStretchedAnimation;
	private SpriteAnimation<Rectangle2D> blinkyDamagedAnimation;

	public PacMan_IntermissionScene2(GameController gameController, V2i unscaledSize) {
		super(gameController, unscaledSize);
		sceneController = new Intermission2Controller(gameController);
		sceneController.playIntermissionSound = () -> SoundManager.get().play(GameSound.INTERMISSION_2);
		context = sceneController.context();
	}

	@Override
	public void init() {
		sceneController.init();

		levelCounter2D = new LevelCounter2D(game, unscaledSize.x - t(3), unscaledSize.y - t(2));

		pacMan2D = new Pac2D(context.pac, game, new MyPacAnimationSet(r2D));
		blinky2D = new Ghost2D(context.blinky, game, new MyGhostAnimationSet(Ghost.RED_GHOST, r2D));
		nail2D = new Nail2D(context.nail, game);
		pacMan2D.animations.restart();
		blinky2D.animations.restart();
		blinkyStretchedAnimation = ((Spritesheet_PacMan) r2D).createBlinkyStretchedAnimation();
		blinkyDamagedAnimation = ((Spritesheet_PacMan) r2D).createBlinkyDamagedAnimation();
	}

	@Override
	public void doUpdate() {
		sceneController.update();
	}

	@Override
	public void doRender(GraphicsContext g) {
		levelCounter2D.render(g, r2D);
		pacMan2D.render(g, r2D);
		nail2D.render(g, r2D);
		if (sceneController.nailDistance() < 0) {
			blinky2D.render(g, r2D);
		} else {
			drawBlinkyStretched(context.blinky, context.nail.position, sceneController.nailDistance() / 4);
		}
	}

	private void drawBlinkyStretched(Ghost blinky, V2d nailPosition, int stretching) {
		var g = canvas.getGraphicsContext2D();
		Rectangle2D stretchedDress = blinkyStretchedAnimation.frame(stretching);
		r2D.drawSprite(g, stretchedDress, (int) (nailPosition.x - 4), (int) (nailPosition.y - 4));
		if (stretching < 3) {
			blinky2D.render(g, r2D);
		} else {
			Rectangle2D damagedDress = blinkyDamagedAnimation.frame(blinky.moveDir() == Direction.UP ? 0 : 1);
			r2D.drawSprite(g, damagedDress, (int) (blinky.position.x - 4), (int) (blinky.position.y - 4));
		}
	}
}