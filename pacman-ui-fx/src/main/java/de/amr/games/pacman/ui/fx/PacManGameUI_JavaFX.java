package de.amr.games.pacman.ui.fx;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.common.GameVariant.MS_PACMAN;
import static de.amr.games.pacman.model.common.GameVariant.PACMAN;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.event.PacManGameStateChangedEvent;
import de.amr.games.pacman.ui.PacManGameUI;
import de.amr.games.pacman.ui.fx.rendering.GameRendering2D;
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
import de.amr.games.pacman.ui.fx.sound.SoundAssets;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
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
		SCENES[MS_PACMAN.ordinal()][4][0] = new PlayScene2D(GameRendering2D.RENDERING_MS_PACMAN, SoundAssets.get(MS_PACMAN));
		SCENES[MS_PACMAN.ordinal()][4][1] = new PlayScene3D(SoundAssets.get(MS_PACMAN));

		SCENES[PACMAN.ordinal()]   [0][0] = 
		SCENES[PACMAN.ordinal()]   [0][1] = new PacMan_IntroScene();
		SCENES[PACMAN.ordinal()]   [1][0] = 
		SCENES[PACMAN.ordinal()]   [1][1] = new PacMan_IntermissionScene1();
		SCENES[PACMAN.ordinal()]   [2][0] = 
		SCENES[PACMAN.ordinal()]   [2][1] = new PacMan_IntermissionScene2();
		SCENES[PACMAN.ordinal()]   [3][0] = 
		SCENES[PACMAN.ordinal()]   [3][1] = new PacMan_IntermissionScene3();
		SCENES[PACMAN.ordinal()]   [4][0] = new PlayScene2D(GameRendering2D.RENDERING_PACMAN, SoundAssets.get(PACMAN));
		SCENES[PACMAN.ordinal()]   [4][1] = new PlayScene3D(SoundAssets.get(PACMAN));
		//@formatter:on
	}

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

		gameController.addGameEventListener(this);

		GameScene initialGameScene = sceneForCurrentGameState(Env.$use3DScenes.get());
		double aspectRatio = initialGameScene.aspectRatio().orElse(4.0 / 3.0);
		mainScene = new Scene(new StackPane(), aspectRatio * height, height, Color.rgb(20, 20, 60));
		setGameScene(initialGameScene);

		stage.addEventHandler(KeyEvent.KEY_PRESSED, keyboard::onKeyPressed);
		stage.addEventHandler(KeyEvent.KEY_RELEASED, keyboard::onKeyReleased);
		stage.addEventHandler(KeyEvent.KEY_PRESSED, this::onKeyPressed);
		stage.setTitle("Pac-Man / Ms. Pac-Man (JavaFX)");
		stage.getIcons().add(new Image(getClass().getResource("/pacman/graphics/pacman.png").toExternalForm()));
		stage.setOnCloseRequest(e -> Platform.exit());
		stage.setScene(mainScene);
		stage.centerOnScreen();
		stage.show();
	}

	private GameScene sceneForCurrentGameState(boolean use3D) {
		int sceneIndex = gameController.state == PacManGameState.INTRO ? 0
				: gameController.state == PacManGameState.INTERMISSION ? gameController.game().intermissionNumber : 4;
		return SCENES[gameController.gameVariant().ordinal()][sceneIndex][use3D ? 1 : 0];
	}

	private void setGameScene(GameScene newGameScene) {
		if (currentGameScene != newGameScene) {
			log("Change game scene from %s to %s", currentGameScene, newGameScene);
			if (currentGameScene != null) {
				currentGameScene.end();
			}
			if (newGameScene.getController() == null) {
				newGameScene.setGameController(gameController);
				newGameScene.setAvailableSize(mainScene.getWidth(), mainScene.getHeight());
				keepGameSceneMaximized(newGameScene, mainScene);
			}
			currentGameScene = newGameScene;
			if (Env.$useStaticCamera.get()) {
				currentGameScene.useMoveableCamera(false);
			} else {
				currentGameScene.useMoveableCamera(true);
			}
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
		if (event instanceof PacManGameStateChangedEvent) {
			PacManGameStateChangedEvent stateChange = (PacManGameStateChangedEvent) event;
			handleGameStateChange(stateChange.oldGameState, stateChange.newGameState);
		}
		currentGameScene.onGameEvent(event);
	}

	private void handleGameStateChange(PacManGameState oldGameState, PacManGameState newGameState) {
		if (newGameState == PacManGameState.INTRO) {
			// TODO check this
			SoundAssets.get(gameController.gameVariant()).stopAll();
		}
		setGameScene(sceneForCurrentGameState(Env.$use3DScenes.get()));
	}

	private void onKeyPressed(KeyEvent e) {
		if (e.isControlDown()) {
			onControlKeyPressed(e.getCode());
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

	private void onControlKeyPressed(KeyCode key) {
		switch (key) {

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
			Env.$useStaticCamera.set(!Env.$useStaticCamera.get());
			if (Env.$useStaticCamera.get()) {
				currentGameScene.useMoveableCamera(false);
				showFlashMessage("Static Camera");
			} else {
				currentGameScene.useMoveableCamera(true);
				showFlashMessage("Moveable Camera");
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

		case MINUS:
			Env.$slowdown.set(Math.max(1, Env.$slowdown.get() - 1));
			break;

		case PLUS:
			Env.$slowdown.set(Math.min(10, Env.$slowdown.get() + 1));
			break;

		default:
			break;
		}
	}

	private void toggleUse3DScenes() {
		Env.$use3DScenes.set(!Env.$use3DScenes.get());
		if (sceneForCurrentGameState(false) != sceneForCurrentGameState(true)) {
			SoundAssets.get(gameController.gameVariant()).stopAll();
			setGameScene(sceneForCurrentGameState(Env.$use3DScenes.get()));
		}
	}

	private void keepGameSceneMaximized(GameScene gameScene, Scene parentScene) {
		if (gameScene.aspectRatio().isPresent()) {
			double aspectRatio = gameScene.aspectRatio().getAsDouble();
			parentScene.widthProperty().addListener((s, o, newParentWidth) -> {
				double maxHeight = Math.min(newParentWidth.doubleValue() / aspectRatio, parentScene.getHeight());
				double maxWidth = maxHeight * aspectRatio;
				gameScene.setAvailableSize(maxWidth, maxHeight);
			});
			parentScene.heightProperty().addListener((s, o, newParentHeight) -> {
				double maxHeight = newParentHeight.doubleValue();
				double maxWidth = Math.min(parentScene.getHeight() * aspectRatio, parentScene.getWidth());
				gameScene.setAvailableSize(maxWidth, maxHeight);
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
			AbstractGameScene2D scene2D = (AbstractGameScene2D) currentGameScene;
			scene2D.getCanvas().getGraphicsContext2D().setFill(Color.BLACK);
			scene2D.getCanvas().getGraphicsContext2D().fillRect(0, 0, scene2D.getCanvas().getWidth(),
					scene2D.getCanvas().getHeight());
			scene2D.render();
		}
	}

	@Override
	public void reset() {
		SoundAssets.get(gameController.gameVariant()).stopAll();
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
}