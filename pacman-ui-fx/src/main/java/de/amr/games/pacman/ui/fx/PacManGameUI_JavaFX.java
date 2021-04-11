package de.amr.games.pacman.ui.fx;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.common.GameVariant.MS_PACMAN;
import static de.amr.games.pacman.model.common.GameVariant.PACMAN;
import static de.amr.games.pacman.ui.fx.rendering.GameRendering2D.RENDERING_MS_PACMAN;
import static de.amr.games.pacman.ui.fx.rendering.GameRendering2D.RENDERING_PACMAN;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.event.PacManGameStateChangedEvent;
import de.amr.games.pacman.ui.PacManGameUI;
import de.amr.games.pacman.ui.fx.rendering.GameRendering2D;
import de.amr.games.pacman.ui.fx.scenes.common.CameraType;
import de.amr.games.pacman.ui.fx.scenes.common.GameScene;
import de.amr.games.pacman.ui.fx.scenes.common._2d.AbstractGameScene2D;
import de.amr.games.pacman.ui.fx.scenes.common._2d.PlayScene2D;
import de.amr.games.pacman.ui.fx.scenes.common._3d.PlayScene3D;
import de.amr.games.pacman.ui.fx.scenes.mspacman.MsPacMan_IntermissionScene1;
import de.amr.games.pacman.ui.fx.scenes.mspacman.MsPacMan_IntermissionScene2;
import de.amr.games.pacman.ui.fx.scenes.mspacman.MsPacMan_IntermissionScene3;
import de.amr.games.pacman.ui.fx.scenes.mspacman.MsPacMan_IntroScene;
import de.amr.games.pacman.ui.fx.scenes.pacman.PacMan_IntermissionScene1;
import de.amr.games.pacman.ui.fx.scenes.pacman.PacMan_IntermissionScene2;
import de.amr.games.pacman.ui.fx.scenes.pacman.PacMan_IntermissionScene3;
import de.amr.games.pacman.ui.fx.scenes.pacman.PacMan_IntroScene;
import de.amr.games.pacman.ui.fx.sound.PacManGameSounds;
import de.amr.games.pacman.ui.fx.sound.SoundManager;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * JavaFX implementation of the Pac-Man game UI.
 * 
 * @author Armin Reichert
 */
public class PacManGameUI_JavaFX implements PacManGameUI {

	public static final SoundManager SOUNDS_MS_PACMAN = new SoundManager(PacManGameSounds::msPacManSoundURL);
	public static final SoundManager SOUNDS_PACMAN = new SoundManager(PacManGameSounds::mrPacManSoundURL);

	private static final GameScene SCENES_MS_PACMAN[][] = new GameScene[5][2];
	private static final GameScene SCENES_PACMAN[][] = new GameScene[5][2];

	static {
		//@formatter:off
		SCENES_MS_PACMAN[0][0] = 
		SCENES_MS_PACMAN[0][1] = new MsPacMan_IntroScene();
		SCENES_MS_PACMAN[1][0] = 
		SCENES_MS_PACMAN[1][1] = new MsPacMan_IntermissionScene1();
		SCENES_MS_PACMAN[2][0] = 
		SCENES_MS_PACMAN[2][1] = new MsPacMan_IntermissionScene2();
		SCENES_MS_PACMAN[3][0] = 
		SCENES_MS_PACMAN[3][1] = new MsPacMan_IntermissionScene3();
		SCENES_MS_PACMAN[4][0] = new PlayScene2D<>(RENDERING_MS_PACMAN, SOUNDS_MS_PACMAN);
		SCENES_MS_PACMAN[4][1] = new PlayScene3D(SOUNDS_MS_PACMAN);

		SCENES_PACMAN   [0][0] = 
		SCENES_PACMAN   [0][1] = new PacMan_IntroScene();
		SCENES_PACMAN   [1][0] = 
		SCENES_PACMAN   [1][1] = new PacMan_IntermissionScene1();
		SCENES_PACMAN   [2][0] = 
		SCENES_PACMAN   [2][1] = new PacMan_IntermissionScene2();
		SCENES_PACMAN   [3][0] = 
		SCENES_PACMAN   [3][1] = new PacMan_IntermissionScene3();
		SCENES_PACMAN   [4][0] = new PlayScene2D<>(RENDERING_PACMAN, SOUNDS_PACMAN);
		SCENES_PACMAN   [4][1] = new PlayScene3D(SOUNDS_PACMAN);
		//@formatter:on
	}

	public final IntegerProperty $fps = new SimpleIntegerProperty();
	public final Stage stage;
	public final PacManGameController gameController;
	public final HUD hud = new HUD(this);
	public final Scene mainScene;
	public GameScene currentGameScene;

	private final Keyboard keyboard = new Keyboard();
	private final FlashMessageView flashMessageView = new FlashMessageView();;

	public PacManGameUI_JavaFX(Stage stage, PacManGameController gameController, double height) {
		this.stage = stage;
		this.gameController = gameController;

		GameScene gameScene = sceneForCurrentGameState(Env.$use3DScenes.get());
		double aspectRatio = gameScene.aspectRatio().orElse(getScreenAspectRatio());
		mainScene = new Scene(new StackPane(), aspectRatio * height, height, Color.rgb(20, 20, 60));
		setGameScene(gameScene);

		stage.addEventHandler(KeyEvent.KEY_PRESSED, keyboard::onKeyPressed);
		stage.addEventHandler(KeyEvent.KEY_RELEASED, keyboard::onKeyReleased);
		stage.addEventHandler(KeyEvent.KEY_PRESSED, this::onKeyPressed);
		stage.setTitle("Pac-Man / Ms. Pac-Man (JavaFX)");
		stage.getIcons().add(new Image(getClass().getResource("/pacman/graphics/pacman.png").toExternalForm()));
		stage.setScene(mainScene);
		stage.centerOnScreen();
		stage.show();
	}

	private double getScreenAspectRatio() {
		Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
		return bounds.getWidth() / bounds.getHeight();
	}

	private GameScene sceneForCurrentGameState(boolean use3D) {
		int sceneIndex = gameController.state == PacManGameState.INTRO ? 0
				: gameController.state == PacManGameState.INTERMISSION ? gameController.game().intermissionNumber : 4;
		if (gameController.gameVariant() == MS_PACMAN) {
			return SCENES_MS_PACMAN[sceneIndex][use3D ? 1 : 0];
		} else if (gameController.gameVariant() == PACMAN) {
			return SCENES_PACMAN[sceneIndex][use3D ? 1 : 0];
		}
		throw new IllegalStateException();
	}

	private void setGameScene(GameScene newGameScene) {
		if (currentGameScene != newGameScene) {
			log("Change game scene from %s to %s", currentGameScene, newGameScene);
			if (currentGameScene != null) {
				currentGameScene.end();
			}
			if (newGameScene.getGameController() == null) {
				newGameScene.setGameController(gameController);
				newGameScene.stretchTo(mainScene.getWidth(), mainScene.getHeight());
				keepGameSceneMaximized(newGameScene, mainScene);
			}
			currentGameScene = newGameScene;
			currentGameScene.start();

			// TODO is there a more elegant way?
			StackPane root = (StackPane) mainScene.getRoot();
			StackPane.setAlignment(hud, Pos.TOP_LEFT);
			root.getChildren().clear();
			root.getChildren().addAll(currentGameScene.getFXSubScene(), flashMessageView, hud);

			// must be done after update of scene
			currentGameScene.getFXSubScene().requestFocus();
		}
	}

	@Override
	public void onGameEvent(PacManGameEvent event) {
		log("%s received game event %s", getClass().getSimpleName(), event.getClass().getSimpleName());
		if (event instanceof PacManGameStateChangedEvent) {
			PacManGameStateChangedEvent stateChange = (PacManGameStateChangedEvent) event;
			if (stateChange.newGameState == PacManGameState.INTRO) {
				stopAllSounds();
			}
			setGameScene(sceneForCurrentGameState(Env.$use3DScenes.get()));
		}
		currentGameScene.onGameEvent(event);
	}

	private void onKeyPressed(KeyEvent e) {
		if (e.isControlDown()) {
			onControlKeyPressed(e);
			return;
		}

		switch (e.getCode()) {
		case A:
			gameController.autopilot.enabled = !gameController.autopilot.enabled;
			showFlashMessage(gameController.autopilot.enabled ? "Autopilot ON" : "Autopilot OFF");
			break;

		case E:
			gameController.eatAllPellets();
			break;

		case I:
			gameController.setPlayerImmune(!gameController.isPlayerImmune());
			showFlashMessage(gameController.isPlayerImmune() ? "Player IMMUNE" : "Player VULNERABLE");
			break;

		case L:
			gameController.game().lives++;
			break;

		case N:
			if (gameController.isGameRunning()) {
				gameController.changeState(PacManGameState.LEVEL_COMPLETE);
			}
			break;

		case Q:
			reset();
			gameController.changeState(PacManGameState.INTRO);
			break;

		case V:
			gameController.toggleGameVariant();
			break;

		case X:
			gameController.killGhosts();
			break;

		case DIGIT1:
			if (gameController.state == PacManGameState.INTRO) {
				showFlashMessage("Test Intermission #1");
				gameController.game().intermissionNumber = 1;
				gameController.changeState(PacManGameState.INTERMISSION);
			}
			break;

		case DIGIT2:
			if (gameController.state == PacManGameState.INTRO) {
				showFlashMessage("Test Intermission #2");
				gameController.game().intermissionNumber = 2;
				gameController.changeState(PacManGameState.INTERMISSION);
			}
			break;

		case DIGIT3:
			if (gameController.state == PacManGameState.INTRO) {
				showFlashMessage("Test Intermission #3");
				gameController.game().intermissionNumber = 3;
				gameController.changeState(PacManGameState.INTERMISSION);
			}
			break;

		case F11:
			stage.setFullScreen(true);
			break;

		default:
			break;
		}
	}

	private void onControlKeyPressed(KeyEvent e) {
		switch (e.getCode()) {

		case C:
			int next = Env.$cameraType.get().ordinal() + 1;
			if (next == CameraType.values().length) {
				next = 0;
			}
			Env.$cameraType.set(CameraType.values()[next]);
			currentGameScene.selectCamera(Env.$cameraType.get());
			showFlashMessage(String.format("Use %s camera", Env.$cameraType.get()));
			break;

		case I:
			Env.$infoViewVisible.set(!Env.$infoViewVisible.get());
			break;

		case L:
			Env.$drawMode.set(Env.$drawMode.get() == DrawMode.FILL ? DrawMode.LINE : DrawMode.FILL);
			break;

		case P:
			Env.$paused.set(!Env.$paused.get());
			break;

		case S:
			if (!e.isShiftDown()) {
				Env.$slowdown.set(Math.max(1, Env.$slowdown.get() - 1));
			} else {
				Env.$slowdown.set(Math.min(10, Env.$slowdown.get() + 1));
			}
			break;

		case T:
			Env.$measureTime.set(!Env.$measureTime.get());
			break;

		case X:
			Env.$showAxes.set(!Env.$showAxes.get());
			break;

		case DIGIT3:
			toggleUse3DScenes();
			String message = String.format("3D scenes %s", Env.$use3DScenes.get() ? "ON" : "OFF");
			showFlashMessage(message);
			break;

		default:
			break;
		}
	}

	private void stopAllSounds() {
		SOUNDS_MS_PACMAN.stopAll();
		SOUNDS_PACMAN.stopAll();
	}

	private void toggleUse3DScenes() {
		Env.$use3DScenes.set(!Env.$use3DScenes.get());
		if (sceneForCurrentGameState(false) != sceneForCurrentGameState(true)) {
			stopAllSounds();
			setGameScene(sceneForCurrentGameState(Env.$use3DScenes.get()));
		}
	}

	private void keepGameSceneMaximized(GameScene gameScene, Scene parentScene) {
		if (gameScene.aspectRatio().isPresent()) {
			double aspectRatio = gameScene.aspectRatio().getAsDouble();
			parentScene.widthProperty().addListener((s, o, newParentWidth) -> {
				double maxHeight = Math.min(newParentWidth.doubleValue() / aspectRatio, parentScene.getHeight());
				double maxWidth = maxHeight * aspectRatio;
				gameScene.stretchTo(maxWidth, maxHeight);
			});
			parentScene.heightProperty().addListener((s, o, newParentHeight) -> {
				double maxHeight = newParentHeight.doubleValue();
				double maxWidth = Math.min(parentScene.getHeight() * aspectRatio, parentScene.getWidth());
				gameScene.stretchTo(maxWidth, maxHeight);
			});
		} else {
			gameScene.getFXSubScene().widthProperty().bind(parentScene.widthProperty());
			gameScene.getFXSubScene().heightProperty().bind(parentScene.heightProperty());
		}
	}

	@Override
	public void update() {
		currentGameScene.update();
		flashMessageView.update();
		hud.update();
		if (currentGameScene instanceof AbstractGameScene2D) {
			AbstractGameScene2D<?> scene2D = (AbstractGameScene2D<?>) currentGameScene;
			scene2D.clearCanvas(Color.BLACK);
			scene2D.render();
		}
	}

	@Override
	public void reset() {
		stopAllSounds();
		currentGameScene.end();
	}

	@Override
	public void showFlashMessage(String message, double seconds) {
		flashMessageView.showMessage(message, (long) (60 * seconds));
	}

	@Override
	public boolean keyPressed(String keySpec) {
		return keyboard.keyPressed(keySpec);
	}

	public void setTitle(String title) {
		stage.setTitle(title);
	}
}