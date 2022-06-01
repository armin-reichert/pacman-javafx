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
import de.amr.games.pacman.controller.pacman.Intermission1Controller;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.ui.fx._2d.entity.common.Ghost2D;
import de.amr.games.pacman.ui.fx._2d.entity.common.LevelCounter2D;
import de.amr.games.pacman.ui.fx._2d.entity.common.Player2D;
import de.amr.games.pacman.ui.fx._2d.entity.pacman.BigPacMan2D;
import de.amr.games.pacman.ui.fx._2d.rendering.common.SpriteAnimation;
import de.amr.games.pacman.ui.fx._2d.scene.common.GameScene2D;
import de.amr.games.pacman.ui.fx.sound.GameSound;
import de.amr.games.pacman.ui.fx.sound.SoundManager;
import javafx.scene.canvas.GraphicsContext;

/**
 * First intermission scene: Blinky chases Pac-Man and is then chased by a huge Pac-Man.
 * 
 * @author Armin Reichert
 */
public class PacMan_IntermissionScene1 extends GameScene2D {

	private final Intermission1Controller sceneController;
	private final Intermission1Controller.Context context;

	private LevelCounter2D levelCounter2D;
	private Player2D pacMan2D;
	private Ghost2D blinky2D;
	private BigPacMan2D bigPacMan2D;

	public PacMan_IntermissionScene1(GameController gameController, V2i unscaledSize) {
		super(gameController, unscaledSize);
		sceneController = new Intermission1Controller(gameController);
		sceneController.playIntermissionSound = () -> SoundManager.get().loop(GameSound.INTERMISSION_1, 2);
		context = sceneController.context();
	}

	@Override
	public void init() {
		sceneController.init();

		levelCounter2D = new LevelCounter2D(game);
		levelCounter2D.right_x = unscaledSize.x - t(3);
		levelCounter2D.y = unscaledSize.y - t(2);

		pacMan2D = new Player2D(context.pac, game).createAnimations(r2D);
		blinky2D = new Ghost2D(context.blinky, game).createAnimations(r2D);
		bigPacMan2D = new BigPacMan2D(context.pac, game);

		pacMan2D.animMunching.values().forEach(SpriteAnimation::restart);
		blinky2D.animKicking.values().forEach(SpriteAnimation::restart);
		blinky2D.animFrightened.restart();
		bigPacMan2D.munchingAnimation.restart();
	}

	@Override
	public void doUpdate() {
		sceneController.update();
	}

	@Override
	public void doRender(GraphicsContext g) {
		levelCounter2D.render(g, r2D);
		blinky2D.render(g, r2D);
		if (sceneController.state() == Intermission1Controller.State.CHASING_PACMAN) {
			pacMan2D.render(g, r2D);
		} else {
			bigPacMan2D.render(g, r2D);
		}
	}
}