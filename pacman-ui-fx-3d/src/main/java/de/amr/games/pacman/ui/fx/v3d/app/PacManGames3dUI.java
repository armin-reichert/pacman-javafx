/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.app;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui.fx.app.PacManGames2dUI;
import de.amr.games.pacman.ui.fx.app.Settings;
import de.amr.games.pacman.ui.fx.input.KeyboardSteering;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.util.Theme;
import de.amr.games.pacman.ui.fx.util.Ufx;
import de.amr.games.pacman.ui.fx.v3d.scene.Perspective;
import de.amr.games.pacman.ui.fx.v3d.scene.PlayScene3D;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.DrawMode;
import javafx.stage.Stage;

import java.util.Map;

import static de.amr.games.pacman.ui.fx.util.ResourceManager.message;

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
public class PacManGames3dUI extends PacManGames2dUI implements ActionHandler3D {

	public PacManGames3dUI(Stage stage, Settings settings, Theme theme,
												 Map<String, GameScene> gameScenesMsPacMan, Map<String, GameScene> gameScenesPacMan) {
		super(stage, settings, theme, gameScenesMsPacMan, gameScenesPacMan);

		if (gameScenesMsPacMan.get("play3D") instanceof PlayScene3D playScene3D) {
			playScene3D.bindSize(mainScene().widthProperty(), mainScene().heightProperty());
		}
		if (gameScenesPacMan.get("play3D") instanceof PlayScene3D playScene3D) {
			playScene3D.bindSize(mainScene().widthProperty(), mainScene().heightProperty());
		}
	}

	@Override
	protected void createGamePage(Theme theme) {
		gamePage = new GamePage3D(this, theme);
		gamePage.setSize(scene.getWidth(), scene.getHeight());
		// register event handler for opening play scene context menu
		mainScene().addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
			currentScene().ifPresent(gameScene -> {
				if (e.getButton() == MouseButton.SECONDARY && isPlayScene(gameScene)) {
					gamePage().openContextMenu(mainScene().getRoot(), e.getScreenX(), e.getScreenY());
				} else {
					gamePage().closeContextMenu();
				}
			});
		});
	}

	private boolean isPlayScene(GameScene gameScene) {
		var config = sceneConfig();
		return gameScene == config.get("play") || gameScene == config.get("play3D");
	}

	public GamePage3D gamePage() {
		return (GamePage3D) gamePage;
	}

	@Override
	public ActionHandler3D actionHandler() {
		return this;
	}

	@Override
	protected void configurePacSteering() {
		// Steer with unmodified or with CONTROL + cursor key
		var keyboardPlayerSteering = new KeyboardSteering();
		keyboardPlayerSteering.define(Direction.UP,    KeyCode.UP,    KeyCombination.CONTROL_DOWN);
		keyboardPlayerSteering.define(Direction.DOWN,  KeyCode.DOWN,  KeyCombination.CONTROL_DOWN);
		keyboardPlayerSteering.define(Direction.LEFT,  KeyCode.LEFT,  KeyCombination.CONTROL_DOWN);
		keyboardPlayerSteering.define(Direction.RIGHT, KeyCode.RIGHT, KeyCombination.CONTROL_DOWN);

		GameController.it().setManualPacSteering(keyboardPlayerSteering);
	}

	@Override
	protected void configureBindings(Settings settings) {
		super.configureBindings(settings);
		PacManGames3dApp.PY_3D_DRAW_MODE.addListener((py, ov, nv) -> updateStage());
		PacManGames3dApp.PY_3D_ENABLED.addListener((py, ov, nv) -> updateStage());
	}

	@Override
	protected void updateStage() {
		var variant = game().variant();
		var variantKey = variant == GameVariant.MS_PACMAN ? "mspacman" : "pacman";
		var titleKey = "app.title." + variantKey;
		if (clock().isPaused()) {
			titleKey += ".paused";
		}
		var dimension = message(PacManGames3dApp.TEXTS, PacManGames3dApp.PY_3D_ENABLED.get() ? "threeD" : "twoD");
		stage.setTitle(message(PacManGames3dApp.TEXTS, titleKey, dimension));
		stage.getIcons().setAll(theme.image(variantKey + ".icon"));
		gamePage().updateBackground();
	}

	@Override
	protected GameScene sceneMatchingCurrentGameState() {
		var scene = super.sceneMatchingCurrentGameState();
		var config = sceneConfig();
		var playScene2D = config.get("play");
		var playScene3D = config.get("play3D");
		if (scene == playScene2D && PacManGames3dApp.PY_3D_ENABLED.get() && playScene3D != null) {
			return playScene3D;
		}
		return scene;
	}

	public void toggle2D3D() {
		currentScene().ifPresent(gameScene -> {
			Ufx.toggle(PacManGames3dApp.PY_3D_ENABLED);
			if (isPlayScene(gameScene)) {
				updateOrReloadGameScene(true);
				gamePage.onGameSceneChanged();
				gameScene.onSceneVariantSwitch();
			}
			GameController.it().update();
			showFlashMessage(message(PacManGames3dApp.TEXTS, PacManGames3dApp.PY_3D_ENABLED.get() ? "use_3D_scene" : "use_2D_scene"));
		});
	}

	@Override
	protected void setGameScene(GameScene newGameScene) {
		super.setGameScene(newGameScene);
		var config = sceneConfig();
		if (newGameScene == config.get("play3D")) {
			gamePage().pip().setGameSceneContext(newGameScene.context());
		}
	}

	public void selectNextPerspective() {
		selectPerspective(PacManGames3dApp.PY_3D_PERSPECTIVE.get().next());
	}

	public void selectPrevPerspective() {
		selectPerspective(PacManGames3dApp.PY_3D_PERSPECTIVE.get().prev());
	}

	private void selectPerspective(Perspective perspective) {
		PacManGames3dApp.PY_3D_PERSPECTIVE.set(perspective);
		var perspectiveName = message(PacManGames3dApp.TEXTS, perspective.name());
		showFlashMessage(message(PacManGames3dApp.TEXTS, "camera_perspective", perspectiveName));
	}

	public void toggleDrawMode() {
		PacManGames3dApp.PY_3D_DRAW_MODE.set(
				PacManGames3dApp.PY_3D_DRAW_MODE.get() == DrawMode.FILL ? DrawMode.LINE : DrawMode.FILL);
	}
}