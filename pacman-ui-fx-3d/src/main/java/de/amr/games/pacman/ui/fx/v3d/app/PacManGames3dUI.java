/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
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
import de.amr.games.pacman.ui.fx.scene.GameSceneConfiguration;
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
 * The picture-in-picture view shows the 2D version of the 3D play scene. It is activated/deactivated by pressing key F2.
 * Size and transparency can be controlled using the dashboard.
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
		resizeGamePage(scene.getHeight());
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
	}

	@Override
	protected void configureBindings(Settings settings) {
		super.configureBindings(settings);
		PacManGames3d.PY_3D_DRAW_MODE.addListener((py, ov, nv) -> updateStage());
		PacManGames3d.PY_3D_ENABLED.addListener((py, ov, nv) -> updateStage());
	}

	@Override
	protected void updateStage() {
		switch (GameController.it().game().variant()) {
		case MS_PACMAN -> {
			var key = clock().isPaused() ? "app.title.ms_pacman.paused" : "app.title.ms_pacman";
			var dimensionMsg = fmtMessage(PacManGames3d.TEXTS, PacManGames3d.PY_3D_ENABLED.get() ? "threeD" : "twoD");
			stage.setTitle(fmtMessage(PacManGames3d.TEXTS, key, dimensionMsg));
			stage.getIcons().setAll(theme.image("mspacman.icon"));
		}
		case PACMAN -> {
			var key = clock().isPaused() ? "app.title.pacman.paused" : "app.title.pacman";
			var dimensionMsg = fmtMessage(PacManGames3d.TEXTS, PacManGames3d.PY_3D_ENABLED.get() ? "threeD" : "twoD");
			stage.setTitle(fmtMessage(PacManGames3d.TEXTS, key, dimensionMsg));
			stage.getIcons().setAll(theme.image("pacman.icon"));
		}
		default -> throw new IllegalGameVariantException(GameController.it().game().variant());
		}
		gamePage().getPip().update(currentGameScene, PacManGames3d.PY_PIP_ON.get());
		gamePage().updateBackground(currentGameScene);
	}

	@Override
	protected GameScene sceneMatchingCurrentGameState() {
		var config = sceneConfig();
		var scene = super.sceneMatchingCurrentGameState();
		if (PacManGames3d.PY_3D_ENABLED.get() && scene == config.playScene() && config.playScene3D() != null) {
			return config.playScene3D();
		}
		return scene;
	}

	public void toggle2D3D() {
		var config = sceneConfig();
		Ufx.toggle(PacManGames3d.PY_3D_ENABLED);
		if (config.playScene() == currentGameScene || config.playScene3D() == currentGameScene) {
			updateOrReloadGameScene(true);
			gamePage.setGameScene(currentGameScene);
			currentGameScene().onSceneVariantSwitch();
		}
		onTick();
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

	private GamePage3D gamePage() {
		return (GamePage3D) gamePage;
	}

	private GameSceneConfiguration sceneConfig() {
		return game().variant() == GameVariant.MS_PACMAN ? configMsPacMan : configPacMan;
	}
}