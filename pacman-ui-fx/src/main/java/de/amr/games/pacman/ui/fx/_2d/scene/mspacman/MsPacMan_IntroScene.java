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
package de.amr.games.pacman.ui.fx._2d.scene.mspacman;

import static de.amr.games.pacman.model.common.world.World.t;

import java.util.stream.Stream;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.mspacman.IntroController;
import de.amr.games.pacman.lib.TimedSeq;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.ui.fx._2d.entity.common.Ghost2D;
import de.amr.games.pacman.ui.fx._2d.entity.common.Player2D;
import de.amr.games.pacman.ui.fx._2d.scene.common.GameScene2D;
import de.amr.games.pacman.ui.fx.util.U;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Intro scene of the Ms. Pac-Man game.
 * <p>
 * The ghosts and Ms. Pac-Man are introduced on a billboard and are marching in one after another.
 * 
 * @author Armin Reichert
 */
public class MsPacMan_IntroScene extends GameScene2D {

	private final IntroController sc;
	private final Image midwayLogo = U.image("/mspacman/graphics/midway.png");
	private Player2D msPacMan2D;
	private Ghost2D[] ghosts2D;

	public MsPacMan_IntroScene(GameController gameController, V2i unscaledSize) {
		super(gameController, unscaledSize);
		sc = new IntroController(gameController);
	}

	@Override
	public void init() {
		sc.init();
		createScores();
		score2D.showPoints = false;
		msPacMan2D = new Player2D(sc.msPacMan, game, r2D);
		msPacMan2D.animMunching.values().forEach(TimedSeq::restart);
		ghosts2D = Stream.of(sc.ghosts).map(ghost -> new Ghost2D(ghost, game, r2D)).toArray(Ghost2D[]::new);
		Stream.of(ghosts2D).forEach(ghost2D -> ghost2D.animKicking.values().forEach(TimedSeq::restart));
	}

	@Override
	public void doUpdate() {
		sc.updateState();
	}

	@Override
	public void doRender(GraphicsContext g) {
		score2D.render(g);
		highScore2D.render(g);

		g.setFont(r2D.getArcadeFont());
		g.setFill(Color.ORANGE);
		g.fillText("\"MS PAC-MAN\"", sc.titlePosition.x, sc.titlePosition.y);

		drawAnimatedBoard(32, 16);
		switch (sc.state) {
		case GHOSTS -> drawGhostText();
		case MSPACMAN -> drawMsPacManText();
		case READY -> {
			drawMsPacManText();
			drawPressKeyToStart(26);
		}
		default -> {
		}
		}

		Stream.of(ghosts2D).forEach(ghost2D -> ghost2D.render(g));
		msPacMan2D.render(g);
		drawCopyright();
	}

	private void drawGhostText() {
		var g = canvas.getGraphicsContext2D();
		g.setFill(Color.WHITE);
		g.setFont(r2D.getArcadeFont());
		if (sc.ghostIndex == 0) {
			g.fillText("WITH", sc.titlePosition.x, sc.boardTopLeft.y + t(3));
		}
		Ghost ghost = sc.ghosts[sc.ghostIndex];
		g.setFill(r2D.getGhostSkinColor(ghost.id));
		g.fillText(ghost.name.toUpperCase(), t(14 - ghost.name.length() / 2), sc.boardTopLeft.y + t(6));
	}

	private void drawMsPacManText() {
		var g = canvas.getGraphicsContext2D();
		g.setFill(Color.WHITE);
		g.setFont(r2D.getArcadeFont());
		g.fillText("STARRING", sc.titlePosition.x, sc.boardTopLeft.y + t(3));
		g.setFill(Color.YELLOW);
		g.fillText("MS PAC-MAN", sc.titlePosition.x, sc.boardTopLeft.y + t(6));
	}

	private void drawAnimatedBoard(int numDotsX, int numDotsY) {
		var g = canvas.getGraphicsContext2D();
		long time = sc.boardAnimationTimer.ticked();
		int light = (int) (time / 2) % (numDotsX / 2);
		for (int dot = 0; dot < 2 * (numDotsX + numDotsY); ++dot) {
			int x = 0, y = 0;
			if (dot <= numDotsX) {
				x = dot;
			} else if (dot < numDotsX + numDotsY) {
				x = numDotsX;
				y = dot - numDotsX;
			} else if (dot < 2 * numDotsX + numDotsY + 1) {
				x = 2 * numDotsX + numDotsY - dot;
				y = numDotsY;
			} else {
				y = 2 * (numDotsX + numDotsY) - dot;
			}
			g.setFill((dot + light) % (numDotsX / 2) == 0 ? Color.PINK : Color.RED);
			g.fillRect(sc.boardTopLeft.x + 4 * x, sc.boardTopLeft.y + 4 * y, 2, 2);
		}
	}

	private void drawPressKeyToStart(int tileY) {
		var g = canvas.getGraphicsContext2D();
		if (sc.blinking.frame()) {
			String text = "PRESS SPACE TO PLAY";
			g.setFill(Color.WHITE);
			g.setFont(r2D.getArcadeFont());
			g.fillText(text, t(13 - text.length() / 2), t(tileY));
		}
	}

	private void drawCopyright() {
		var g = canvas.getGraphicsContext2D();
		double scale = 36.0 / midwayLogo.getHeight();
		g.drawImage(midwayLogo, t(4), t(28) + 3, scale * midwayLogo.getWidth(), scale * midwayLogo.getHeight());
		g.setFill(Color.RED);
		g.setFont(Font.font("Dialog", 11.0));
		g.fillText("\u00a9", t(9), t(30) + 2); // (c) symbol
		g.setFont(r2D.getArcadeFont());
		g.fillText("MIDWAY MFG CO", t(11), t(30));
		g.fillText("1980/1981", t(12), t(32));
	}
}