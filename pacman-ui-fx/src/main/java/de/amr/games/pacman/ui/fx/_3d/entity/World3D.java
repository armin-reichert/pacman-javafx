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
package de.amr.games.pacman.ui.fx._3d.entity;

import static de.amr.games.pacman.model.common.world.World.HTS;
import static de.amr.games.pacman.model.common.world.World.TS;

import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import de.amr.games.pacman.ui.fx._3d.animation.Rendering3D;
import de.amr.games.pacman.ui.fx._3d.entity.Maze3D.MazeStyle;
import de.amr.games.pacman.ui.fx._3d.model.PacModel3D;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.scene.Group;
import javafx.scene.paint.Color;

/**
 * @author Armin Reichert
 */
public class World3D extends Group {

	private final Maze3D maze3D;
	private final Scores3D scores3D;
	private final LevelCounter3D levelCounter3D;
	private final LivesCounter3D livesCounter3D;

	public World3D(GameModel game, PacModel3D model3D, Rendering2D r2D) {
		scores3D = new Scores3D();
		scores3D.setFont(r2D.getArcadeFont());
		if (game.credit > 0) {
			scores3D.setComputeScoreText(true);
		} else {
			scores3D.setComputeScoreText(false);
			scores3D.txtScore.setFill(Color.RED);
			scores3D.txtScore.setText("GAME OVER!");
		}
		getChildren().add(scores3D);

		var mazeStyle = new MazeStyle();
		mazeStyle.wallSideColor = Rendering3D.getMazeSideColor(game.variant, game.level.mazeNumber);
		mazeStyle.wallTopColor = Rendering3D.getMazeTopColor(game.variant, game.level.mazeNumber);
		mazeStyle.doorColor = Rendering3D.getGhostHouseDoorColor(game.variant);
		mazeStyle.foodColor = Rendering3D.getMazeFoodColor(game.variant, game.level.mazeNumber);

		maze3D = new Maze3D(game.level.world, mazeStyle);
		maze3D.setFloorTexture(Ufx.image("/common/escher-texture.jpg"), Color.rgb(51, 0, 102));
		maze3D.resolution.bind(Env.mazeResolution);
		maze3D.wallHeight.bind(Env.mazeWallHeight);
		getChildren().add(maze3D);
		Env.mazeFloorHasTexture.set(true);
		Env.mazeFloorHasTexture.addListener((x, y, newVal) -> {
			if (newVal.booleanValue()) {
				maze3D.setFloorTexture(Ufx.image("/common/escher-texture.jpg"), Color.rgb(51, 0, 102));
			} else {
				maze3D.setFloorTexture(null, Color.rgb(51, 0, 102));
			}
		});

		levelCounter3D = new LevelCounter3D(symbol -> r2D.getSpriteImage(r2D.getBonusSymbolSprite(symbol)));
		levelCounter3D.setRightPosition((game.level.world.numCols() - 1) * TS, TS);
		levelCounter3D.update(game.levelCounter);
		getChildren().add(levelCounter3D);

		livesCounter3D = new LivesCounter3D(model3D);
		livesCounter3D.setTranslateX(TS);
		livesCounter3D.setTranslateY(TS);
		livesCounter3D.setTranslateZ(-HTS);
		livesCounter3D.setVisible(game.credit > 0);
		getChildren().add(livesCounter3D);
	}

	public Scores3D getScores3D() {
		return scores3D;
	}

	public Maze3D getMaze3D() {
		return maze3D;
	}

	public void update(GameModel game) {
		scores3D.update(game);
		maze3D.updateDoorState(game.ghosts(), game.level.world.ghostHouse().doorsCenter());
		livesCounter3D.update(game.playing ? game.lives - 1 : game.lives);
	}
}