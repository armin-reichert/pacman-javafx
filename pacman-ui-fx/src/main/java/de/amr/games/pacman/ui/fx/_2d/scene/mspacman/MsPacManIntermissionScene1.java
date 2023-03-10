/*
MIT License

Copyright (c) 2021-2023 Armin Reichert

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
import de.amr.games.pacman.controller.mspacman.MsPacManIntermission1;
import de.amr.games.pacman.lib.anim.EntityAnimationMap;
import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.ui.fx._2d.rendering.mspacman.MsPacManGameRenderer;
import de.amr.games.pacman.ui.fx._2d.scene.common.GameScene2D;

/**
 * Intermission scene 1: "They meet".
 * <p>
 * Pac-Man leads Inky and Ms. Pac-Man leads Pinky. Soon, the two Pac-Men are about to collide, they quickly move
 * upwards, causing Inky and Pinky to collide and vanish. Finally, Pac-Man and Ms. Pac-Man face each other at the top of
 * the screen and a big pink heart appears above them. (Played after round 2)
 * 
 * @author Armin Reichert
 */
public class MsPacManIntermissionScene1 extends GameScene2D {

	private MsPacManIntermission1 intermission;

	public MsPacManIntermissionScene1(GameController gameController) {
		super(gameController);
	}

	@Override
	public void init() {
		intermission = new MsPacManIntermission1(context.gameController());
		var ic = intermission.context();
		var r = (MsPacManGameRenderer) context.r2D();
		intermission.restart(MsPacManIntermission1.IntermissionState.FLAP);
		ic.clapperboard.setAnimation(r.createClapperboardAnimation());
		ic.msPac.setAnimations(r.createPacAnimations(ic.msPac));
		ic.msPac.animations().ifPresent(EntityAnimationMap::ensureRunning);
		ic.pacMan.setAnimations(r.createPacAnimations(ic.pacMan));
		ic.pacMan.animations().ifPresent(animations -> {
			var munching = r.createPacManMunchingAnimationMap(ic.pacMan);
			animations.put(GameModel.AK_PAC_MUNCHING, munching);
			animations.ensureRunning();
		});
		ic.inky.setAnimations(r.createGhostAnimations(ic.inky));
		ic.inky.animations().ifPresent(EntityAnimationMap::ensureRunning);
		ic.pinky.setAnimations(r.createGhostAnimations(ic.pinky));
		ic.pinky.animations().ifPresent(EntityAnimationMap::ensureRunning);
	}

	@Override
	public void update() {
		intermission.update();
	}

	@Override
	public void drawSceneContent() {
		var ic = intermission.context();
		var r = (MsPacManGameRenderer) context.r2D();
		r.drawClap(g, ic.clapperboard);
		r.drawPac(g, ic.msPac);
		r.drawPac(g, ic.pacMan);
		r.drawGhost(g, ic.inky);
		r.drawGhost(g, ic.pinky);
		r.drawEntitySprite(g, ic.heart, r.heartSprite());
		r.drawLevelCounter(g, context.level().map(GameLevel::number), context.game().levelCounter());
	}
}