package de.amr.games.pacman.ui.fx;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.common.GameVariant.MS_PACMAN;
import static de.amr.games.pacman.model.common.GameVariant.PACMAN;

import java.util.Optional;
import java.util.OptionalDouble;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.ui.PacManGameUI;
import de.amr.games.pacman.ui.animation.PacManGameAnimations2D;
import de.amr.games.pacman.ui.fx.rendering.standard.Assets2D;
import de.amr.games.pacman.ui.fx.scenes.common.Env;
import de.amr.games.pacman.ui.fx.scenes.common.FlashMessageView;
import de.amr.games.pacman.ui.fx.scenes.common.GameScene;
import de.amr.games.pacman.ui.fx.scenes.common.scene2d.AbstractGameScene2D;
import de.amr.games.pacman.ui.fx.scenes.common.scene3d.PlayScene3D;
import de.amr.games.pacman.ui.fx.scenes.mspacman.MsPacMan_IntermissionScene1;
import de.amr.games.pacman.ui.fx.scenes.mspacman.MsPacMan_IntermissionScene2;
import de.amr.games.pacman.ui.fx.scenes.mspacman.MsPacMan_IntermissionScene3;
import de.amr.games.pacman.ui.fx.scenes.mspacman.MsPacMan_IntroScene;
import de.amr.games.pacman.ui.fx.scenes.mspacman.MsPacMan_PlayScene;
import de.amr.games.pacman.ui.fx.scenes.pacman.PacMan_IntermissionScene1;
import de.amr.games.pacman.ui.fx.scenes.pacman.PacMan_IntermissionScene2;
import de.amr.games.pacman.ui.fx.scenes.pacman.PacMan_IntermissionScene3;
import de.amr.games.pacman.ui.fx.scenes.pacman.PacMan_IntroScene;
import de.amr.games.pacman.ui.fx.scenes.pacman.PacMan_PlayScene;
import de.amr.games.pacman.ui.sound.SoundManager;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.stage.Stage;

/**
 * JavaFX implementation of the Pac-Man game UI.
 * 
 * @author Armin Reichert
 */
public class PacManGameUI_JavaFX implements PacManGameUI {

	private static final GameScene SCENES[][][] = new GameScene[2][5][2];

	static {
		//@formatter:off
		SCENES[MS_PACMAN.ordinal()][0][0] = 
		SCENES[MS_PACMAN.ordinal()][0][1] = new MsPacMan_IntroScene();
		SCENES[MS_PACMAN.ordinal()][1][0] = 
		SCENES[MS_PACMAN.ordinal()][1][1] = new MsPacMan_IntermissionScene1();
		SCENES[MS_PACMAN.ordinal()][2][0] = 
		SCENES[MS_PACMAN.ordinal()][2][1] = new MsPacMan_IntermissionScene2();
		SCENES[MS_PACMAN.ordinal()][3][0] = 
		SCENES[MS_PACMAN.ordinal()][3][1] = new MsPacMan_IntermissionScene3();
		SCENES[MS_PACMAN.ordinal()][4][0] = new MsPacMan_PlayScene();
		SCENES[MS_PACMAN.ordinal()][4][1] = new PlayScene3D();

		SCENES[PACMAN.ordinal()]   [0][0] = 
		SCENES[PACMAN.ordinal()]   [0][1] = new PacMan_IntroScene();
		SCENES[PACMAN.ordinal()]   [1][0] = 
		SCENES[PACMAN.ordinal()]   [1][1] = new PacMan_IntermissionScene1();
		SCENES[PACMAN.ordinal()]   [2][0] = 
		SCENES[PACMAN.ordinal()]   [2][1] = new PacMan_IntermissionScene2();
		SCENES[PACMAN.ordinal()]   [3][0] = 
		SCENES[PACMAN.ordinal()]   [3][1] = new PacMan_IntermissionScene3();
		SCENES[PACMAN.ordinal()]   [4][0] = new PacMan_PlayScene();
		SCENES[PACMAN.ordinal()]   [4][1] = new PlayScene3D();
		//@formatter:on
	}

	static GameScene scene(GameVariant gameVariant, PacManGameState gameState, GameModel game, boolean _3D) {
		return SCENES[gameVariant.ordinal()][gameState == PacManGameState.INTRO ? 0
				: gameState == PacManGameState.INTERMISSION ? game.intermissionNumber : 4][_3D ? 1 : 0];
	}

	public final HUD hud;

	final Stage stage;
	final PacManGameController controller;
	final Keyboard keyboard;
	final Scene mainScene;
	final StackPane mainSceneRoot;
	final FlashMessageView flashMessageView;
	GameScene currentGameScene;

	public PacManGameUI_JavaFX(Stage stage, PacManGameController controller, double height) {
		this.stage = stage;
		this.controller = controller;
		keyboard = new Keyboard();
		flashMessageView = new FlashMessageView();
		mainSceneRoot = new StackPane();
		mainScene = new Scene(mainSceneRoot, GameScene.ASPECT_RATIO * height, height, Color.rgb(20, 20, 60));

		hud = new HUD(this, Pos.TOP_LEFT);

		controller.addStateChangeListener(this::handleGameStateChange);

		stage.addEventHandler(KeyEvent.KEY_PRESSED, keyboard::onKeyPressed);
		stage.addEventHandler(KeyEvent.KEY_RELEASED, keyboard::onKeyReleased);
		stage.addEventHandler(KeyEvent.KEY_PRESSED, this::handleKeys);

		stage.setTitle("Pac-Man / Ms. Pac-Man (JavaFX)");
		stage.getIcons().add(new Image(getClass().getResource("/pacman/graphics/pacman.png").toExternalForm()));
		stage.setOnCloseRequest(e -> Platform.exit());
		stage.setScene(mainScene);

		GameScene startScene = scene(controller.gameVariant(), controller.state, controller.game(), Env.$use3DScenes.get());
		changeGameScene(null, startScene);

		stage.centerOnScreen();
		stage.show();
	}

	private void handleGameStateChange(PacManGameState oldState, PacManGameState newState) {
		GameVariant gameVariant = controller.gameVariant();
		GameModel game = controller.game();
		boolean _3D = Env.$use3DScenes.get();
		log("Handle game state change %s to %s", oldState, newState);
		if (currentGameScene != scene(gameVariant, newState, game, _3D)) {
			selectScene(gameVariant, newState, game, _3D);
		}
		currentGameScene.onGameStateChange(oldState, newState);
	}

	private void selectScene(GameVariant gameVariant, PacManGameState gameState, GameModel game, boolean _3D) {
		GameScene newGameScene = scene(gameVariant, gameState, game, _3D);
		newGameScene.setController(controller);
		newGameScene.setAvailableSize(mainScene.getWidth(), mainScene.getHeight());
		keepGameSceneMaximized(newGameScene, mainScene, newGameScene.aspectRatio());
		changeGameScene(currentGameScene, newGameScene);
	}

	private void changeGameScene(GameScene oldGameScene, GameScene newGameScene) {
		log("Change game scene %s to %s", oldGameScene, newGameScene);
		if (oldGameScene == newGameScene) {
			log("Ignored");
			return;
		}
		if (oldGameScene != null) {
			oldGameScene.end();
		}
		currentGameScene = newGameScene;
		currentGameScene.setAvailableSize(mainScene.getWidth(), mainScene.getHeight());
		if (Env.$useStaticCamera.get()) {
			currentGameScene.useMoveableCamera(false);
		} else {
			currentGameScene.useMoveableCamera(true);
		}
		currentGameScene.start();
		mainSceneRoot.getChildren().clear();
		mainSceneRoot.getChildren().addAll(currentGameScene.getFXSubScene(), flashMessageView, hud);
	}

	private void handleKeys(KeyEvent e) {
		boolean control = e.isControlDown();
		switch (e.getCode()) {
		case F11:
			stage.setFullScreen(true);
			break;
		case DIGIT3:
			if (control) {
				toggleUse3DScenes();
			}
			break;
		case I:
			if (control) {
				Env.$infoViewVisible.set(!Env.$infoViewVisible.get());
			}
			break;
		case L:
			if (control) {
				Env.$drawMode.set(Env.$drawMode.get() == DrawMode.FILL ? DrawMode.LINE : DrawMode.FILL);
			}
			break;
		case P:
			if (control) {
				Env.$paused.set(!Env.$paused.get());
			}
			break;
		case V:
			controller.toggleGameVariant();
			break;
		case S:
			if (control) {
				Env.$useStaticCamera.set(!Env.$useStaticCamera.get());
				if (Env.$useStaticCamera.get()) {
					currentGameScene.useMoveableCamera(false);
					showFlashMessage("Static Camera");
				} else {
					currentGameScene.useMoveableCamera(true);
					showFlashMessage("Moveable Camera");
				}
			}
			break;
		case T:
			if (control) {
				Env.$measureTime.set(!Env.$measureTime.get());
			}
			break;
		case X:
			if (control)
				Env.$showAxes.set(!Env.$showAxes.get());
			break;
		default:
			break;
		}
	}

	private void toggleUse3DScenes() {
		Env.$use3DScenes.set(!Env.$use3DScenes.get());
		GameVariant gameVariant = controller.gameVariant();
		GameModel game = controller.game();
		if (scene(gameVariant, controller.state, game, false) != scene(gameVariant, controller.state, game, true)) {
			selectScene(gameVariant, controller.state, game, Env.$use3DScenes.get());
		}
		String message = String.format("3D scenes %s", Env.$use3DScenes.get() ? "ON" : "OFF");
		showFlashMessage(message);
	}

	private void keepGameSceneMaximized(GameScene gameScene, Scene parentScene, OptionalDouble optionalAspectRatio) {
		if (optionalAspectRatio.isPresent()) {
			double aspectRatio = optionalAspectRatio.getAsDouble();
			parentScene.widthProperty().addListener((s, o, n) -> {
				double newHeight = Math.min(n.doubleValue() / aspectRatio, parentScene.getHeight());
				double newWidth = newHeight * aspectRatio;
				gameScene.setAvailableSize(newWidth, newHeight);
			});
			parentScene.heightProperty().addListener((s, o, n) -> {
				double newHeight = n.doubleValue();
				double newWidth = Math.min(parentScene.getHeight() * aspectRatio, parentScene.getWidth());
				gameScene.setAvailableSize(newWidth, newHeight);
			});
		} else {
			gameScene.getFXSubScene().widthProperty().bind(parentScene.widthProperty());
			gameScene.getFXSubScene().heightProperty().bind(parentScene.heightProperty());
		}
	}

	@Override
	public void update() {
		if (currentGameScene == null) {
			log("Cannot update: no game scene");
			return;
		}
		if (currentGameScene instanceof AbstractGameScene2D) {
			AbstractGameScene2D scene2D = (AbstractGameScene2D) currentGameScene;
			scene2D.clearCanvas();
		}
		currentGameScene.update();
		flashMessageView.update();
		hud.update();
	}

	@Override
	public void reset() {
		if (currentGameScene != null) {
			currentGameScene.end();
		}
	}

	@Override
	public void showFlashMessage(String message, double seconds) {
		flashMessageView.showMessage(message, (long) (60 * seconds));
	}

	@Override
	public boolean keyPressed(String keySpec) {
		return keyboard.keyPressed(keySpec);
	}

	@Override
	public Optional<SoundManager> sound() {
		return Optional.of(Assets2D.SOUND.get(controller.gameVariant()));
	}

	@Override
	public Optional<PacManGameAnimations2D> animation() {
		if (currentGameScene instanceof AbstractGameScene2D) {
			AbstractGameScene2D scene2D = (AbstractGameScene2D) currentGameScene;
			return Optional.of(scene2D.getRendering());
		}
		return Optional.empty();
	}
}