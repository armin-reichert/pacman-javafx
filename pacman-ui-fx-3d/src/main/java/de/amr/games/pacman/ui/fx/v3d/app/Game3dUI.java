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

import static de.amr.games.pacman.ui.fx.util.ResourceManager.fmtMessage;

import java.util.Optional;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.IllegalGameVariantException;
import de.amr.games.pacman.ui.fx.app.Game2d;
import de.amr.games.pacman.ui.fx.app.Game2dUI;
import de.amr.games.pacman.ui.fx.app.Settings;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.scene.GameSceneChoice;
import de.amr.games.pacman.ui.fx.scene2d.PlayScene2D;
import de.amr.games.pacman.ui.fx.util.ResourceManager;
import de.amr.games.pacman.ui.fx.util.Ufx;
import de.amr.games.pacman.ui.fx.v3d.dashboard.Dashboard;
import de.amr.games.pacman.ui.fx.v3d.scene.Perspective;
import de.amr.games.pacman.ui.fx.v3d.scene.PlayScene3D;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * User interface for Pac-Man and Ms. Pac-Man games.
 * <p>
 * The <strong>play scene</strong> is available in a {@link PlayScene2D 2D} and a {@link PlayScene3D 3D} version. All
 * others scenes are 2D only.
 * <p>
 * The picture-in-picture view shows the 2D version of the current game scene (in case this is the play scene). It is
 * activated/deactivated by pressing key F2. Size and transparency can be controlled using the dashboard.
 * <p>
 * 
 * @author Armin Reichert
 */
public class Game3dUI extends Game2dUI {

	public class PictureInPicture {

		public static final float MIN_HEIGHT = 36 * 8;
		public static final float MAX_HEIGHT = 2.5f * MIN_HEIGHT;

		public final BooleanProperty visiblePy = new SimpleBooleanProperty(false) {
			@Override
			protected void invalidated() {
				update();
			}
		};

		public final DoubleProperty opacityPy = new SimpleDoubleProperty(1.0);

		public final DoubleProperty heightPy = new SimpleDoubleProperty(MIN_HEIGHT) {
			@Override
			protected void invalidated() {
				playScene.resize(get());
			}
		};

		private final PlayScene2D playScene;

		public PictureInPicture(GameController gameController) {
			playScene = new PlayScene2D(gameController);
			playScene.fxSubScene().opacityProperty().bind(opacityPy);
			playScene.fxSubScene().setVisible(false);
		}

		public void update() {
			if (currentGameScene != null) {
				boolean isPlayScene = gameSceneConfig.get(game().variant()).isPlayScene(currentGameScene);
				playScene.fxSubScene().setVisible(visiblePy.get() && currentGameScene.is3D() && isPlayScene);
				playScene.context().setCreditVisible(false);
				playScene.context().setScoreVisible(true);
				playScene.context().setRendering2D(currentGameScene.context().rendering2D());
			}
		}

		public void render() {
			if (playScene.context().rendering2D() != null) {
				playScene.render();
			}
		}

		public SubScene fxSubScene() {
			return playScene.fxSubScene();
		}
	}

	private final PictureInPicture pip;
	private final Dashboard dashboard;

	public Game3dUI(GameVariant gameVariant, Stage stage, double width, double height) {
		super(gameVariant, stage, width, height);
		pip = new PictureInPicture(gameController);
		dashboard = new Dashboard(this);
	}

	@Override
	public void doRender() {
		flashMessageView.update();
		currentGameScene.render();
		dashboard.update();
		pip.render();
	}

	@Override
	public void init(Settings settings) {
		super.init(settings);
		dashboard.init();
	}

	@Override
	protected void configureGameScenes() {
		super.configureGameScenes();
		gameSceneConfig.get(GameVariant.MS_PACMAN).playSceneChoice().setScene3D(new PlayScene3D(gameController));
		gameSceneConfig.get(GameVariant.PACMAN).playSceneChoice().setScene3D(new PlayScene3D(gameController));
	}

	@Override
	protected void configureMainScene(Scene mainScene, Settings settings) {
		dashboard.setVisible(false);
		var dashboardLayer = new BorderPane();
		dashboardLayer.setLeft(dashboard);
		dashboardLayer.setRight(pip.fxSubScene());

		mainSceneRoot = new StackPane();
		mainSceneRoot.getChildren().add(new Text("(Game scene)"));
		mainSceneRoot.getChildren().add(flashMessageView);
		mainSceneRoot.getChildren().add(dashboardLayer);

		mainScene.setRoot(mainSceneRoot);
		mainScene.heightProperty().addListener((py, ov, nv) -> {
			if (currentGameScene != null) {
				currentGameScene.onParentSceneResize(mainScene);
			}
		});
		mainScene.setOnKeyPressed(this::handleKeyPressed);
		mainScene.setOnMouseClicked(e -> {
			if (e.getClickCount() == 2) {
				resizeStageToFitCurrentGameScene();
			}
		});
	}

	@Override
	protected void configureBindings(Settings settings) {
		super.configureBindings(settings);

		pip.opacityPy.bind(Game3d.pipOpacityPy);
		pip.heightPy.bind(Game3d.pipHeightPy);

		Game3d.d3_drawModePy.addListener((py, ov, nv) -> updateStage());
		Game3d.d3_enabledPy.addListener((py, ov, nv) -> updateStage());
		Game3d.d3_enabledPy.set(true);
		Game3d.d3_perspectivePy.set(Perspective.NEAR_PLAYER);
	}

	@Override
	protected void updateStage() {
		if (pip.visiblePy.get()) {
			pip.update();
		}
		if (currentGameScene != null && currentGameScene.is3D()) {
			if (Game3d.d3_drawModePy.get() == DrawMode.LINE) {
				mainSceneRoot.setBackground(ResourceManager.colorBackground(Color.BLACK));
			} else {
				mainSceneRoot.setBackground(Game3d.assets.wallpaper3D);
			}
		} else {
			mainSceneRoot.setBackground(Game2d.assets.wallpaper2D);
		}
		var paused = clock().pausedPy.get();
		var dimensionMsg = fmtMessage(Game3d.assets.messages, Game3d.d3_enabledPy.get() ? "threeD" : "twoD"); // TODO
		switch (gameController.game().variant()) {
		case MS_PACMAN -> {
			var messageKey = paused ? "app.title.ms_pacman.paused" : "app.title.ms_pacman";
			stage.setTitle(fmtMessage(Game3d.assets.messages, messageKey, dimensionMsg));
			stage.getIcons().setAll(Game2d.assets.iconMsPacManGame);
		}
		case PACMAN -> {
			var messageKey = paused ? "app.title.pacman.paused" : "app.title.pacman";
			stage.setTitle(fmtMessage(Game3d.assets.messages, messageKey, dimensionMsg));
			stage.getIcons().setAll(Game2d.assets.iconPacManGame);
		}
		default -> throw new IllegalGameVariantException(gameController.game().variant());
		}
	}

	@Override
	protected Optional<GameScene> findGameScene(int dimension) {
		if (dimension != 2 && dimension != 3) {
			throw new IllegalArgumentException("Dimension must be 2 or 3, but is %d".formatted(dimension));
		}
		var choice = sceneChoiceMatchingCurrentGameState();
		return Optional.ofNullable(dimension == 3 ? choice.scene3D() : choice.scene2D());
	}

	@Override
	protected GameScene chooseGameScene(GameSceneChoice choice) {
		var use3D = Game3d.d3_enabledPy.get();
		return (use3D && choice.scene3D() != null) ? choice.scene3D() : choice.scene2D();
	}

	@Override
	protected void handleKeyboardInput() {
		super.handleKeyboardInput();
		if (Keyboard.pressed(Game3d.Keys.TOGGLE_3D_ENABLED)) {
			toggle3DEnabled();
		} else if (Keyboard.pressed(Game3d.Keys.TOGGLE_DASHBOARD_VISIBLE)
				|| Keyboard.pressed(Game3d.Keys.TOGGLE_DASHBOARD_VISIBLE_2)) {
			Game3d.actions.toggleDashboardVisible();
		} else if (Keyboard.pressed(Game3d.Keys.TOGGLE_PIP_VIEW_VISIBLE)) {
			Game3d.actions.togglePipVisibility();
		}
	}

	public void toggle3DEnabled() {
		Ufx.toggle(Game3d.d3_enabledPy);
		if (findGameScene(3).isPresent()) {
			updateGameScene(true);
			currentGameScene().onSceneVariantSwitch();
		} else {
			// if for example toggle action occurs in intro scene, show message indicating which variant is used
			var message = fmtMessage(Game3d.assets.messages, Game3d.d3_enabledPy.get() ? "use_3D_scene" : "use_2D_scene");
			Game2d.actions.showFlashMessage(message);
		}
	}

	public Dashboard dashboard() {
		return dashboard;
	}

	public PictureInPicture pip() {
		return pip;
	}
}