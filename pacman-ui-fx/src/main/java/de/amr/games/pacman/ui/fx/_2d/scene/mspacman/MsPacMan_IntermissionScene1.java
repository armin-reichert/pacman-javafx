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
package de.amr.games.pacman.ui.fx._2d.scene.mspacman;

import static de.amr.games.pacman.model.common.world.World.t;

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.controller.mspacman.Intermission1Controller;
import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.ui.fx._2d.entity.common.Ghost2D;
import de.amr.games.pacman.ui.fx._2d.entity.common.Ghost2D.GhostAnimation;
import de.amr.games.pacman.ui.fx._2d.entity.common.LevelCounter2D;
import de.amr.games.pacman.ui.fx._2d.entity.common.Pac2D;
import de.amr.games.pacman.ui.fx._2d.entity.mspacman.Flap2D;
import de.amr.games.pacman.ui.fx._2d.entity.mspacman.Heart2D;
import de.amr.games.pacman.ui.fx._2d.rendering.common.GhostAnimations;
import de.amr.games.pacman.ui.fx._2d.rendering.common.PacAnimations;
import de.amr.games.pacman.ui.fx._2d.scene.common.GameScene2D;
import de.amr.games.pacman.ui.fx.sound.GameSound;
import de.amr.games.pacman.ui.fx.sound.SoundManager;
import javafx.scene.canvas.GraphicsContext;

/**
 * Intermission scene 1: "They meet".
 * <p>
 * Pac-Man leads Inky and Ms. Pac-Man leads Pinky. Soon, the two Pac-Men are about to collide, they quickly move
 * upwards, causing Inky and Pinky to collide and vanish. Finally, Pac-Man and Ms. Pac-Man face each other at the top of
 * the screen and a big pink heart appears above them. (Played after round 2)
 * 
 * @author Armin Reichert
 */
public class MsPacMan_IntermissionScene1 extends GameScene2D {

	private final Intermission1Controller sceneController;
	private final Intermission1Controller.Context context;
	private LevelCounter2D levelCounter2D;
	private Pac2D msPacMan2D;
	private Pac2D pacMan2D;
	private Ghost2D inky2D;
	private Ghost2D pinky2D;
	private Flap2D flap2D;
	private Heart2D heart2D;

	public MsPacMan_IntermissionScene1(GameController gameController, V2i unscaledSize) {
		super(gameController, unscaledSize);
		sceneController = new Intermission1Controller(gameController);
		context = sceneController.context();
		sceneController.playIntermissionSound = () -> SoundManager.get().loop(GameSound.INTERMISSION_1, 1);
		sceneController.playFlapAnimation = () -> flap2D.animation.restart();
	}

	@Override
	public void init() {
		sceneController.restartInInitialState(Intermission1Controller.State.FLAP);

		levelCounter2D = new LevelCounter2D(game, unscaledSize.x - t(3), unscaledSize.y - t(2));
		flap2D = new Flap2D(context.flap, game);
		msPacMan2D = new Pac2D(context.msPac, game, new PacAnimations(r2D));
		pacMan2D = new Pac2D(context.pacMan, game, new PacAnimations(r2D));
		// TODO fixme
//		pacMan2D.animMunching = ((Rendering2D_MsPacMan) r2D).createHusbandMunchingAnimations();
		inky2D = new Ghost2D(context.inky, game, new GhostAnimations(Ghost.CYAN_GHOST, r2D));
		pinky2D = new Ghost2D(context.pinky, game, new GhostAnimations(Ghost.PINK_GHOST, r2D));
		heart2D = new Heart2D(context.heart, game);

		// start animations
		msPacMan2D.animations.restart();
		pacMan2D.animations.restart();
		inky2D.animations.restart();
		pinky2D.animations.restart();
	}

	@Override
	public void doUpdate() {
		sceneController.update();
		// stop ghost animation when Pac-Man and Ms. Pac-Man are in heaven
		if (sceneController.state() == Intermission1Controller.State.IN_HEAVEN
				&& context.pacMan.velocity.equals(V2d.NULL)) {
			inky2D.animations.stop(GhostAnimation.ALIVE);
			pinky2D.animations.stop(GhostAnimation.ALIVE);
		}
	}

	@Override
	public void doRender(GraphicsContext g) {
		levelCounter2D.render(g, r2D);
		flap2D.render(g, r2D);
		msPacMan2D.render(g, r2D);
		pacMan2D.render(g, r2D);
		inky2D.render(g, r2D);
		pinky2D.render(g, r2D);
		heart2D.render(g, r2D);
	}
}