/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui.fx.PacManGames2dUI;
import de.amr.games.pacman.ui.fx.Settings;
import de.amr.games.pacman.ui.fx.input.KeyboardSteering;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.scene2d.*;
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

import static de.amr.games.pacman.ui.fx.v3d.PacManGames3dApp.message;

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

	public PacManGames3dUI(Stage stage, Settings settings, Theme theme) {
		super(stage, settings, theme);
	}

	@Override
	protected void addGameScenes() {
		gameScenes.put(GameVariant.MS_PACMAN, Map.of(
			"boot",   new BootScene(),
			"intro",  new MsPacManIntroScene(),
			"credit", new MsPacManCreditScene(),
			"play",   new PlayScene2D(),
			"play3D", new PlayScene3D(),
			"cut1",   new MsPacManCutscene1(),
			"cut2",   new MsPacManCutscene2(),
			"cut3",   new MsPacManCutscene3()
		));
		if (gameScenes.get(GameVariant.MS_PACMAN).get("play3D") instanceof PlayScene3D playScene3D) {
			playScene3D.bindSize(mainScene().widthProperty(), mainScene().heightProperty());
		}
		gameScenes.put(GameVariant.PACMAN, Map.of(
			"boot",   new BootScene(),
			"intro",  new PacManIntroScene(),
			"credit", new PacManCreditScene(),
			"play",   new PlayScene2D(),
			"play3D", new PlayScene3D(),
			"cut1",   new PacManCutscene1(),
			"cut2",   new PacManCutscene2(),
			"cut3",   new PacManCutscene3()
		));
		if (gameScenes.get(GameVariant.PACMAN).get("play3D") instanceof PlayScene3D playScene3D) {
			playScene3D.bindSize(mainScene().widthProperty(), mainScene().heightProperty());
		}
	}

	@Override
	protected GamePage3D createGamePage(Theme theme) {
		var gamePage = new GamePage3D(this, theme);
		gamePage.setSize(mainScene().getWidth(), mainScene().getHeight());
		// register event handler for opening play scene context menu
		mainScene().addEventHandler(MouseEvent.MOUSE_CLICKED, e ->
			currentScene().ifPresent(gameScene -> {
				if (e.getButton() == MouseButton.SECONDARY && isPlayScene(gameScene)) {
					gamePage().openContextMenu(mainScene().getRoot(), e.getScreenX(), e.getScreenY());
				} else {
					gamePage().closeContextMenu();
				}
			})
		);
		return gamePage;
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
		var dimension = message(PacManGames3dApp.PY_3D_ENABLED.get() ? "threeD" : "twoD");
		stage.setTitle(message(titleKey, dimension));
		stage.getIcons().setAll(theme.image(variantKey + ".icon"));
		gamePage().updateBackground();
	}

	@Override
	protected GameScene sceneMatchingCurrentGameState() {
		var gameScene = super.sceneMatchingCurrentGameState();
		var config = sceneConfig();
		var playScene2D = config.get("play");
		var playScene3D = config.get("play3D");
		if (gameScene == playScene2D && PacManGames3dApp.PY_3D_ENABLED.get() && playScene3D != null) {
			return playScene3D;
		}
		return gameScene;
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
			showFlashMessage(message(PacManGames3dApp.PY_3D_ENABLED.get() ? "use_3D_scene" : "use_2D_scene"));
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
		var perspectiveName = message(perspective.name());
		showFlashMessage(message("camera_perspective", perspectiveName));
	}

	public void toggleDrawMode() {
		PacManGames3dApp.PY_3D_DRAW_MODE.set(
				PacManGames3dApp.PY_3D_DRAW_MODE.get() == DrawMode.FILL ? DrawMode.LINE : DrawMode.FILL);
	}
}