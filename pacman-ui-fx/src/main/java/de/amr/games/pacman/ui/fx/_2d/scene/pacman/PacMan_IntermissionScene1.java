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
import de.amr.games.pacman.controller.pacman.Intermission1Controller;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.ui.fx._2d.entity.common.Ghost2D;
import de.amr.games.pacman.ui.fx._2d.entity.common.LevelCounter2D;
import de.amr.games.pacman.ui.fx._2d.entity.common.Pac2D;
import de.amr.games.pacman.ui.fx._2d.entity.pacman.BigPacMan2D;
import de.amr.games.pacman.ui.fx._2d.rendering.common.GhostAnimations;
import de.amr.games.pacman.ui.fx._2d.rendering.common.PacAnimations;
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

	private Intermission1Controller sceneController;
	private Intermission1Controller.Context context;
	private LevelCounter2D levelCounter2D;
	private Pac2D pacMan2D;
	private Ghost2D blinky2D;
	private BigPacMan2D bigPacMan2D;

	@Override
	public void setSceneContext(GameController gameController) {
		super.setSceneContext(gameController);
		sceneController = new Intermission1Controller(gameController);
		sceneController.playIntermissionSound = () -> SoundManager.get().loop(GameSound.INTERMISSION_1, 2);
		context = sceneController.context();
	}

	@Override
	public void init() {
		sceneController.init();
		levelCounter2D = new LevelCounter2D(game.levelCounter, r2D);
		pacMan2D = new Pac2D(context.pac, new PacAnimations(r2D));
		blinky2D = new Ghost2D(context.blinky, new GhostAnimations(Ghost.RED_GHOST, r2D));
		bigPacMan2D = new BigPacMan2D(context.pac);
		bigPacMan2D.startMunching();
	}

	@Override
	public void doUpdate() {
		sceneController.update();
		blinky2D.animations.select(switch (context.blinky.state) {
		case FRIGHTENED -> GhostAnimations.Key.ANIM_BLUE;
		case HUNTING_PAC -> GhostAnimations.Key.ANIM_COLOR;
		default -> blinky2D.animations.selectedKey();
		});
	}

	@Override
	public void doRender(GraphicsContext g) {
		blinky2D.render(g, r2D);
		if (sceneController.state() == Intermission1Controller.State.CHASING_PACMAN) {
			pacMan2D.render(g, r2D);
		} else {
			bigPacMan2D.render(g, r2D);
		}
		levelCounter2D.render(g, r2D);
	}
}