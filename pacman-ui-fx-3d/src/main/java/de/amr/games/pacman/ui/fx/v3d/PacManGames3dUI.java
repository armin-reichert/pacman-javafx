/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui.fx.PacManGames2dUI;
import de.amr.games.pacman.ui.fx.Settings;
import de.amr.games.pacman.ui.fx.input.KeyboardSteering;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.util.Theme;
import de.amr.games.pacman.ui.fx.util.Ufx;
import de.amr.games.pacman.ui.fx.v3d.scene.PlayScene3D;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.DrawMode;
import javafx.stage.Stage;

import static de.amr.games.pacman.ui.fx.v3d.PacManGames3dApp.*;

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
		PY_3D_DRAW_MODE.addListener((py, ov, nv) -> updateStage());
		PY_3D_ENABLED.addListener((py, ov, nv) -> updateStage());
		gamePage().dashboard().sections().forEach(section -> section.init(this, this));
	}

	@Override
	protected void addGameScenes() {
		super.addGameScenes();
		{
			var playScene3D = new PlayScene3D();
			playScene3D.bindSize(mainScene.widthProperty(), mainScene.heightProperty());
			gameScenes.get(GameVariant.MS_PACMAN).put("play3D", playScene3D);
		}
		{
			var playScene3D = new PlayScene3D();
			playScene3D.bindSize(mainScene.widthProperty(), mainScene.heightProperty());
			gameScenes.get(GameVariant.PACMAN).put("play3D", playScene3D);
		}
	}

	@Override
	protected GamePage3D createGamePage() {
		var page = new GamePage3D(this);
		page.setSize(mainScene.getWidth(), mainScene.getHeight());
		// register event handler for opening page context menu
		mainScene.addEventHandler(MouseEvent.MOUSE_CLICKED, e ->
			currentGameScene().ifPresent(gameScene -> {
				page.contextMenu().hide();
				if (e.getButton() == MouseButton.SECONDARY && isPlayScene(gameScene)) {
					page.contextMenu().rebuild(actionHandler(), gameScene);
					page.contextMenu().show(mainScene.getRoot(), e.getScreenX(), e.getScreenY());
				}
			})
		);
		gameScenePy.addListener((obj, ov, newGameScene) -> page.onGameSceneChanged(newGameScene));
		return page;
	}

	public boolean isPlayScene(GameScene gameScene) {
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
		// Enable steering with unmodified and CONTROL + cursor key
		var steering = new KeyboardSteering();
		steering.define(Direction.UP,    KeyCode.UP,    KeyCombination.CONTROL_DOWN);
		steering.define(Direction.DOWN,  KeyCode.DOWN,  KeyCombination.CONTROL_DOWN);
		steering.define(Direction.LEFT,  KeyCode.LEFT,  KeyCombination.CONTROL_DOWN);
		steering.define(Direction.RIGHT, KeyCode.RIGHT, KeyCombination.CONTROL_DOWN);
		gameController().setManualPacSteering(steering);
	}

	@Override
	protected void updateStage() {
		var variantKey = gameVariant() == GameVariant.MS_PACMAN ? "mspacman" : "pacman";
		var titleKey = "app.title." + variantKey + (gameClock().isPaused()? ".paused" : "");
		var dimension = message(PY_3D_ENABLED.get() ? "threeD" : "twoD");
		stage.setTitle(message(titleKey, dimension));
		stage.getIcons().setAll(theme.image(variantKey + ".icon"));
		gamePage().updateBackground();
	}

	@Override
	protected GameScene sceneMatchingCurrentGameState() {
		var gameScene = super.sceneMatchingCurrentGameState();
		if (PY_3D_ENABLED.get() && gameScene == sceneConfig().get("play")) {
			return sceneConfig().getOrDefault("play3D", gameScene);
		}
		return gameScene;
	}

	@Override
	public void toggle2D3D() {
		currentGameScene().ifPresent(gameScene -> {
			Ufx.toggle(PY_3D_ENABLED);
			if (isPlayScene(gameScene)) {
				updateOrReloadGameScene(true);
				gameScene.onSceneVariantSwitch();
			}
			gameController().update();
			showFlashMessage(message(PY_3D_ENABLED.get() ? "use_3D_scene" : "use_2D_scene"));
		});
	}

	@Override
	public void togglePipVisible() {
		Ufx.toggle(PY_PIP_ON);
		showFlashMessage(message(PY_PIP_ON.get() ? "pip_on" : "pip_off"));
	}

	@Override
	public void selectNextPerspective() {
		PY_3D_PERSPECTIVE.set(PY_3D_PERSPECTIVE.get().next());
		showFlashMessage(message("camera_perspective", message(PY_3D_PERSPECTIVE.get().name())));
	}

	@Override
	public void selectPrevPerspective() {
		PY_3D_PERSPECTIVE.set(PY_3D_PERSPECTIVE.get().prev());
		showFlashMessage(message("camera_perspective", message(PY_3D_PERSPECTIVE.get().name())));
	}

	@Override
	public void toggleDrawMode() {
		PY_3D_DRAW_MODE.set(PY_3D_DRAW_MODE.get() == DrawMode.FILL ? DrawMode.LINE : DrawMode.FILL);
	}
}