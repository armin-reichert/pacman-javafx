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

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.controller.mspacman.Intermission3Controller;
import de.amr.games.pacman.lib.animation.SimpleThingAnimation;
import de.amr.games.pacman.model.common.GameSound;
import de.amr.games.pacman.ui.fx._2d.rendering.common.PacAnimations;
import de.amr.games.pacman.ui.fx._2d.rendering.mspacman.MsPacMansHusbandAnimations;
import de.amr.games.pacman.ui.fx._2d.rendering.mspacman.Spritesheet_MsPacMan;
import de.amr.games.pacman.ui.fx._2d.scene.common.GameScene2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;

/**
 * Intermission scene 3: "Junior".
 * 
 * <p>
 * Pac-Man and Ms. Pac-Man gradually wait for a stork, who flies overhead with a little blue bundle. The stork drops the
 * bundle, which falls to the ground in front of Pac-Man and Ms. Pac-Man, and finally opens up to reveal a tiny Pac-Man.
 * (Played after rounds 9, 13, and 17)
 * 
 * @author Armin Reichert
 */
public class MsPacMan_IntermissionScene3 extends GameScene2D {

	private Intermission3Controller sceneController;
	private Intermission3Controller.Context $;
	private SimpleThingAnimation<Rectangle2D> storkAnimation;

	@Override
	public void setSceneContext(GameController gameController) {
		super.setSceneContext(gameController);
		sceneController = new Intermission3Controller(gameController);
		sceneController.playIntermissionSound = () -> game.sounds().ifPresent(snd -> snd.play(GameSound.INTERMISSION_3));
		$ = sceneController.context();
	}

	@Override
	public void init() {
		sceneController.restartInInitialState(Intermission3Controller.State.FLAP);
		$.flap.animation = Spritesheet_MsPacMan.get().createFlapAnimation();
		$.msPacMan.setAnimations(new PacAnimations(r2D));
		$.pacMan.setAnimations(new MsPacMansHusbandAnimations());
		$.flap.animation = Spritesheet_MsPacMan.get().createFlapAnimation();
		storkAnimation = Spritesheet_MsPacMan.get().createStorkFlyingAnimation();
		storkAnimation.ensureRunning();
	}

	@Override
	public void doUpdate() {
		sceneController.update();
	}

	@Override
	public void doRender(GraphicsContext g) {
		var ssmp = ((Spritesheet_MsPacMan) r2D);
		ssmp.drawFlap(g, $.flap);
		r2D.drawPac(g, $.msPacMan);
		r2D.drawPac(g, $.pacMan);
		r2D.drawEntity(g, $.stork, storkAnimation.animate());
		r2D.drawEntity(g, $.bag, $.bag.open ? ssmp.getJunior() : ssmp.getBlueBag());
		r2D.drawLevelCounter(g, game.levelCounter);
	}
}