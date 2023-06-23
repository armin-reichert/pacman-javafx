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
package de.amr.games.pacman.ui.fx.v3d.app;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.IllegalGameVariantException;
import de.amr.games.pacman.ui.fx.app.PacManGames2dUI;
import de.amr.games.pacman.ui.fx.app.Settings;
import de.amr.games.pacman.ui.fx.input.KeyboardSteering;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.util.ResourceManager;
import de.amr.games.pacman.ui.fx.util.Ufx;
import de.amr.games.pacman.ui.fx.v3d.scene.Perspective;
import de.amr.games.pacman.ui.fx.v3d.scene.PlayScene3D;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;

import static de.amr.games.pacman.ui.fx.util.ResourceManager.fmtMessage;

/**
 * User interface for Pac-Man and Ms. Pac-Man games.
 * <p>
 * The <strong>play scene</strong> is available in a 2D and a 3D version. All others scenes are 2D only.
 * <p>
 * The picture-in-picture view shows the 2D version of the current game scene (in case this is the play scene). It is
 * activated/deactivated by pressing key F2. Size and transparency can be controlled using the dashboard.
 * <p>
 * 
 * @author Armin Reichert
 */
public class PacManGames3dUI extends PacManGames2dUI {

	@Override
	protected void configureGameScenes() {
		super.configureGameScenes();
		configMsPacMan.setPlayScene3D(new PlayScene3D(this));
		configPacMan.setPlayScene3D(new PlayScene3D(this));
	}

	@Override
	protected void createGamePage() {
		gamePage = new GamePage3D(this);
	}

	@Override
	protected void configurePacSteering() {
		// Steering with unmodified or with CONTROL+cursor key
		keyboardPlayerSteering = new KeyboardSteering();
		keyboardPlayerSteering.define(Direction.UP, KeyCode.UP, KeyCombination.CONTROL_DOWN);
		keyboardPlayerSteering.define(Direction.DOWN, KeyCode.DOWN, KeyCombination.CONTROL_DOWN);
		keyboardPlayerSteering.define(Direction.LEFT, KeyCode.LEFT, KeyCombination.CONTROL_DOWN);
		keyboardPlayerSteering.define(Direction.RIGHT, KeyCode.RIGHT, KeyCombination.CONTROL_DOWN);

		GameController.it().setManualPacSteering(keyboardPlayerSteering);
		scene.addEventHandler(KeyEvent.KEY_PRESSED, keyboardPlayerSteering);
	}

	@Override
	protected void configureBindings(Settings settings) {
		super.configureBindings(settings);
		PacManGames3d.PY_3D_DRAW_MODE.addListener((py, ov, nv) -> updateStage());
		PacManGames3d.PY_3D_ENABLED.addListener((py, ov, nv) -> updateStage());
		PacManGames3d.PY_3D_PERSPECTIVE.set(Perspective.NEAR_PLAYER);
	}

	@Override
	protected void updateStage() {
		var gamePage3D = (GamePage3D) gamePage;
		gamePage3D.getPip().update(currentGameScene, PacManGames3d.PY_PIP_ON.get());
		if (currentGameScene != null && currentGameScene.is3D()) {
			if (PacManGames3d.PY_3D_DRAW_MODE.get() == DrawMode.LINE) {
				gamePage.layoutPane().setBackground(ResourceManager.coloredBackground(Color.BLACK));
			} else {
				gamePage.layoutPane().setBackground(theme.background("model3D.wallpaper"));
			}
		} else {
			gamePage.layoutPane().setBackground(theme.background("wallpaper.background"));
		}
		var paused = clock != null && clock().isPaused();
		var dimensionMsg = fmtMessage(PacManGames3d.TEXTS, PacManGames3d.PY_3D_ENABLED.get() ? "threeD" : "twoD"); // TODO
		switch (GameController.it().game().variant()) {
		case MS_PACMAN -> {
			var messageKey = paused ? "app.title.ms_pacman.paused" : "app.title.ms_pacman";
			stage.setTitle(fmtMessage(PacManGames3d.TEXTS, messageKey, dimensionMsg));
			stage.getIcons().setAll(theme.image("mspacman.icon"));
		}
		case PACMAN -> {
			var messageKey = paused ? "app.title.pacman.paused" : "app.title.pacman";
			stage.setTitle(fmtMessage(PacManGames3d.TEXTS, messageKey, dimensionMsg));
			stage.getIcons().setAll(theme.image("pacman.icon"));
		}
		default -> throw new IllegalGameVariantException(GameController.it().game().variant());
		}
	}

	@Override
	protected GameScene sceneMatchingCurrentGameState() {
		var scene = super.sceneMatchingCurrentGameState();
		if (PacManGames3d.PY_3D_ENABLED.get()) {
			var config = game().variant() == GameVariant.MS_PACMAN ? configMsPacMan : configPacMan;
			if (scene == config.playScene() && config.playScene3D() != null) {
				scene = config.playScene3D();
			}
		}
		return scene;
	}

	public void toggle2D3D() {
		var config = game().variant() == GameVariant.MS_PACMAN ? configMsPacMan : configPacMan;
		Ufx.toggle(PacManGames3d.PY_3D_ENABLED);
		if (config.playScene() == currentGameScene || config.playScene3D() == currentGameScene) {
			updateOrReloadGameScene(true);
			gamePage.setGameScene(currentGameScene);
			currentGameScene().onSceneVariantSwitch();
		}
	}

	public void selectNextPerspective() {
		var next = PacManGames3d.PY_3D_PERSPECTIVE.get().next();
		PacManGames3d.PY_3D_PERSPECTIVE.set(next);
		String perspectiveName = fmtMessage(PacManGames3d.TEXTS, next.name());
		showFlashMessage(fmtMessage(PacManGames3d.TEXTS, "camera_perspective", perspectiveName));
	}

	public void selectPrevPerspective() {
		var prev = PacManGames3d.PY_3D_PERSPECTIVE.get().prev();
		PacManGames3d.PY_3D_PERSPECTIVE.set(prev);
		String perspectiveName = fmtMessage(PacManGames3d.TEXTS, prev.name());
		showFlashMessage(fmtMessage(PacManGames3d.TEXTS, "camera_perspective", perspectiveName));
	}

	public void toggleDrawMode() {
		PacManGames3d.PY_3D_DRAW_MODE
				.set(PacManGames3d.PY_3D_DRAW_MODE.get() == DrawMode.FILL ? DrawMode.LINE : DrawMode.FILL);
	}
}