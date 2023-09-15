/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.app;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui.fx.app.ActionHandler;
import de.amr.games.pacman.ui.fx.app.PacManGames2dUI;
import de.amr.games.pacman.ui.fx.app.Settings;
import de.amr.games.pacman.ui.fx.input.KeyboardSteering;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.scene.GameSceneConfig;
import de.amr.games.pacman.ui.fx.scene2d.PlayScene2D;
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
												 GameSceneConfig gameScenesMsPacMan, GameSceneConfig gameScenesPacMan) {
		super(stage, settings, theme, gameScenesMsPacMan, gameScenesPacMan);

		if (gameScenesMsPacMan.playScene3D() instanceof PlayScene3D playScene3D) {
			playScene3D.bindSize(mainScene().widthProperty(), mainScene().heightProperty());
		}
		if (gameScenesPacMan.playScene3D() instanceof PlayScene3D playScene3D) {
			playScene3D.bindSize(mainScene().widthProperty(), mainScene().heightProperty());
		}
	}

	@Override
	protected void createGamePage(Theme theme) {
		gamePage = new GamePage3D(this, theme);
		gamePage.setSize(scene.getWidth(), scene.getHeight());
		// register event handler for opening play scene context menu
		mainScene().addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
			if (e.getButton() == MouseButton.SECONDARY &&
					(currentGameScene instanceof PlayScene2D || currentGameScene instanceof PlayScene3D)) {
				gamePage3D().openContextMenu(mainScene().getRoot(), e.getScreenX(), e.getScreenY());
			} else {
				gamePage3D().closeContextMenu();
			}
		});
	}

	public GamePage3D gamePage3D() {
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
		if (currentGameScene != null) {
			gamePage3D().updateBackground(currentGameScene);
		}
	}

	@Override
	protected GameScene sceneMatchingCurrentGameState() {
		var config = sceneConfig();
		var scene = super.sceneMatchingCurrentGameState();
		if (PacManGames3dApp.PY_3D_ENABLED.get() && scene == config.playScene() && config.playScene3D() != null) {
			return config.playScene3D();
		}
		return scene;
	}

	public void toggle2D3D() {
		var config = sceneConfig();
		Ufx.toggle(PacManGames3dApp.PY_3D_ENABLED);
		if (config.playScene() == currentGameScene || config.playScene3D() == currentGameScene) {
			updateOrReloadGameScene(true);
			gamePage.onGameSceneChanged();
			currentGameScene().onSceneVariantSwitch();
		}
		GameController.it().update();
		showFlashMessage(message(PacManGames3dApp.TEXTS, PacManGames3dApp.PY_3D_ENABLED.get() ? "use_3D_scene" : "use_2D_scene"));
	}

	@Override
	protected void setGameScene(GameScene newGameScene) {
		super.setGameScene(newGameScene);
		if (newGameScene.is3D()) {
			var config = sceneConfig();
			gamePage3D().pip().setMaster((PlayScene3D) config.playScene3D());
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
		String perspectiveName = message(PacManGames3dApp.TEXTS, perspective.name());
		showFlashMessage(message(PacManGames3dApp.TEXTS, "camera_perspective", perspectiveName));
	}

	public void toggleDrawMode() {
		PacManGames3dApp.PY_3D_DRAW_MODE.set(
				PacManGames3dApp.PY_3D_DRAW_MODE.get() == DrawMode.FILL ? DrawMode.LINE : DrawMode.FILL);
	}
}