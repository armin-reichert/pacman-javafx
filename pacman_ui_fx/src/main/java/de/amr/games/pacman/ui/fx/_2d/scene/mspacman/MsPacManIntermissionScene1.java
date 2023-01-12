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

import de.amr.games.pacman.controller.mspacman.MsPacManIntermission1;
import de.amr.games.pacman.lib.anim.EntityAnimationSet;
import de.amr.games.pacman.model.common.actors.AnimKeys;
import de.amr.games.pacman.ui.fx._2d.rendering.RendererMsPacManGame;
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

	@Override
	public void init() {
		var renderer = (RendererMsPacManGame) ctx.r2D();
		intermission = new MsPacManIntermission1(ctx.gameController());
		intermission.restart(MsPacManIntermission1.IntermissionState.FLAP);
		intermission.context().clapperboard.setAnimationSet(renderer.createClapperboardAnimationSet());
		intermission.context().msPac.setAnimationSet(renderer.createPacAnimationSet(intermission.context().msPac));
		intermission.context().msPac.animationSet().ifPresent(EntityAnimationSet::ensureRunning);
		intermission.context().pacMan.setAnimationSet(renderer.createPacAnimationSet(intermission.context().pacMan));
		intermission.context().pacMan.animationSet().ifPresent(animations -> {
			var munching = renderer.createPacManMunchingAnimationMap(intermission.context().pacMan);
			animations.put(AnimKeys.PAC_MUNCHING, munching);
			animations.ensureRunning();
		});
		intermission.context().inky.setAnimationSet(renderer.createGhostAnimationSet(intermission.context().inky));
		intermission.context().inky.animationSet().ifPresent(EntityAnimationSet::ensureRunning);
		intermission.context().pinky.setAnimationSet(renderer.createGhostAnimationSet(intermission.context().pinky));
		intermission.context().pinky.animationSet().ifPresent(EntityAnimationSet::ensureRunning);
	}

	@Override
	public void update() {
		intermission.update();
	}

	@Override
	public void draw() {
		var r = (RendererMsPacManGame) ctx.r2D();
		r.drawClapperboard(g, intermission.context().clapperboard);
		r.drawPac(g, intermission.context().msPac);
		r.drawPac(g, intermission.context().pacMan);
		r.drawGhost(g, intermission.context().inky);
		r.drawGhost(g, intermission.context().pinky);
		r.drawEntitySprite(g, intermission.context().heart, r.heartSprite());
		r.drawLevelCounter(g, ctx.game().levelCounter());
	}
}