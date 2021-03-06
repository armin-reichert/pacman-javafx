package de.amr.games.pacman.ui.fx;

import static de.amr.games.pacman.heaven.God.clock;
import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.common.GameType.MS_PACMAN;
import static de.amr.games.pacman.model.common.GameType.PACMAN;

import java.util.EnumMap;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameType;
import de.amr.games.pacman.sound.PacManGameSoundManager;
import de.amr.games.pacman.sound.PacManGameSounds;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.PacManGameUI;
import de.amr.games.pacman.ui.animation.PacManGameAnimations;
import de.amr.games.pacman.ui.fx.common.CameraController;
import de.amr.games.pacman.ui.fx.common.FlashMessageView;
import de.amr.games.pacman.ui.fx.common.GameScene;
import de.amr.games.pacman.ui.fx.common.GameScene2D;
import de.amr.games.pacman.ui.fx.common.GameScene3D;
import de.amr.games.pacman.ui.fx.common.PlayScene2D;
import de.amr.games.pacman.ui.fx.common.PlayScene3D;
import de.amr.games.pacman.ui.fx.common.SubScene2D;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import de.amr.games.pacman.ui.fx.mspacman.MsPacMan_IntermissionScene1;
import de.amr.games.pacman.ui.fx.mspacman.MsPacMan_IntermissionScene2;
import de.amr.games.pacman.ui.fx.mspacman.MsPacMan_IntermissionScene3;
import de.amr.games.pacman.ui.fx.mspacman.MsPacMan_IntroScene;
import de.amr.games.pacman.ui.fx.pacman.PacMan_IntermissionScene1;
import de.amr.games.pacman.ui.fx.pacman.PacMan_IntermissionScene2;
import de.amr.games.pacman.ui.fx.pacman.PacMan_IntermissionScene3;
import de.amr.games.pacman.ui.fx.pacman.PacMan_IntroScene;
import de.amr.games.pacman.ui.fx.rendering.FXRendering;
import de.amr.games.pacman.ui.fx.rendering.standard.MsPacMan_StandardRendering;
import de.amr.games.pacman.ui.fx.rendering.standard.PacMan_StandardRendering;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Camera;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * JavaFX implementation of the Pac-Man game UI.
 * 
 * @author Armin Reichert
 */
public class PacManGameUI_JavaFX implements PacManGameUI {

	private boolean scenes3D_enabled = false;

	private final EnumMap<GameType, FXRendering> renderings = new EnumMap<>(GameType.class);
	private final EnumMap<GameType, SoundManager> sounds = new EnumMap<>(GameType.class);

	private GameModel game;
	private GameScene currentGameScene;

	private final PacManGameController controller;
	private final Keyboard keyboard = new Keyboard();
	private CameraController camControl;

	private final Text infoView = new Text();
	private final FlashMessageView flashMessageView = new FlashMessageView();

	private Stage stage;
	private Scene mainScene;
	private SubScene2D subScene2D;

	private boolean muted;

	public PacManGameUI_JavaFX(Stage stage, PacManGameController controller, double height) {
		this.stage = stage;
		this.controller = controller;
		renderings.put(MS_PACMAN, new MsPacMan_StandardRendering());
		renderings.put(PACMAN, new PacMan_StandardRendering());
		sounds.put(MS_PACMAN, new PacManGameSoundManager(PacManGameSounds::msPacManSoundURL));
		sounds.put(PACMAN, new PacManGameSoundManager(PacManGameSounds::mrPacManSoundURL));
		initStage(GameScene.ASPECT_RATIO * height, height);
		onGameChanged(controller.getGame());
		addResizeHandler(currentGameScene);
		log("JavaFX UI created at clock tick %d", clock.ticksTotal);
	}

	@Override
	public void show() {
//		stage.sizeToScene();
		stage.centerOnScreen();
		stage.show();
	}

	private void initStage(double sceneWidth, double sceneHeight) {
		stage.setTitle("Pac-Man / Ms. Pac-Man (JavaFX)");
		stage.getIcons().add(new Image("/pacman/graphics/pacman.png"));
		stage.setOnCloseRequest(e -> Platform.exit());
		stage.addEventHandler(KeyEvent.KEY_PRESSED, keyboard::onKeyPressed);
		stage.addEventHandler(KeyEvent.KEY_RELEASED, keyboard::onKeyReleased);

		infoView.setFill(Color.WHITE);
		infoView.setFont(Font.font("Sans", 12));
		infoView.setText("");
		StackPane.setAlignment(infoView, Pos.TOP_LEFT);

		// assuming intial game scene is 2D
		subScene2D = new SubScene2D(sceneWidth, sceneHeight);
		mainScene = new Scene(new StackPane(subScene2D.getSubScene(), flashMessageView, infoView), sceneWidth, sceneHeight,
				Color.BLACK);
		stage.setScene(mainScene);
	}

	private void addResizeHandler(GameScene scene) {
		if (scene instanceof GameScene2D) {
			mainScene.widthProperty().addListener((s, o, n) -> {
				double newWidth = (int) n.doubleValue();
				double newHeight = newWidth / GameScene.ASPECT_RATIO;
				if (newHeight < mainScene.getHeight()) {
					subScene2D.resize(newWidth, newHeight);
					log("New scene height: %f", newHeight);
				}
			});
			mainScene.heightProperty().addListener((s, o, n) -> {
				double newWidth = (int) mainScene.getWidth();
				double newHeight = n.doubleValue();
				subScene2D.resize(Math.max(newWidth, mainScene.getWidth()), newHeight);
				log("New scene height: %f", newHeight);
			});
		} else if (scene instanceof GameScene3D) {
			GameScene3D scene3D = (GameScene3D) scene;
			mainScene.widthProperty().addListener((s, o, n) -> {
				double newWidth = (int) n.doubleValue();
				double newHeight = newWidth / GameScene.ASPECT_RATIO;
				scene3D.resize(newWidth, newHeight);
				log("New scene height: %f", newHeight);
			});
			mainScene.heightProperty().addListener((s, o, n) -> {
				double newWidth = (int) mainScene.getWidth();
				double newHeight = n.doubleValue();
				scene3D.resize(newWidth, newHeight);
				log("New scene height: %f", newHeight);
			});
		}
	}

	private void setGameScene(GameScene newGameScene) {
		if (newGameScene instanceof GameScene3D) {
			GameScene3D scene3D = (GameScene3D) newGameScene;
			scene3D.resize(mainScene.getWidth(), mainScene.getHeight());
			mainScene.setRoot(new StackPane(scene3D.getSubScene(), flashMessageView, infoView));
			Camera camera = scene3D.getCamera();
			camera.setTranslateY(scene3D.getSubScene().getHeight() / 2);
			camera.setTranslateZ(-scene3D.getSubScene().getHeight());
			camera.setRotate(30);
			camControl = new CameraController(camera);
		} else {
			subScene2D.resize(mainScene.getWidth(), mainScene.getHeight());
			subScene2D.perspectiveViewOff();
			camControl = null;
			mainScene.setRoot(new StackPane(subScene2D.getSubScene(), flashMessageView, infoView));
		}
		addResizeHandler(newGameScene);
		newGameScene.start();
		currentGameScene = newGameScene;
	}

	private GameScene createGameScene(Camera camera, double height) {
		GameType currentGame = currentGame();
		if (currentGame == PACMAN) {
			FXRendering rendering = renderings.get(PACMAN);
			SoundManager soundManager = sounds.get(PACMAN);
			switch (game.state) {
			case INTRO:
				return new PacMan_IntroScene(camera, controller, rendering, soundManager);
			case INTERMISSION:
				if (game.intermissionNumber == 1) {
					return new PacMan_IntermissionScene1(camera, controller, rendering, soundManager);
				}
				if (game.intermissionNumber == 2) {
					return new PacMan_IntermissionScene2(camera, controller, rendering, soundManager);
				}
				if (game.intermissionNumber == 3) {
					return new PacMan_IntermissionScene3(camera, controller, rendering, soundManager);
				}
				throw new IllegalStateException();
			default:
				return scenes3D_enabled ? new PlayScene3D(controller, height)
						: new PlayScene2D(camera, controller, renderings.get(PACMAN), sounds.get(PACMAN));
			}
		}

		else if (currentGame == MS_PACMAN) {
			FXRendering rendering = renderings.get(MS_PACMAN);
			SoundManager soundManager = sounds.get(MS_PACMAN);
			switch (game.state) {
			case INTRO:
				return new MsPacMan_IntroScene(camera, controller, rendering, soundManager);
			case INTERMISSION:
				if (game.intermissionNumber == 1) {
					return new MsPacMan_IntermissionScene1(camera, controller, rendering, soundManager);
				}
				if (game.intermissionNumber == 2) {
					return new MsPacMan_IntermissionScene2(camera, controller, rendering, soundManager);
				}
				if (game.intermissionNumber == 3) {
					return new MsPacMan_IntermissionScene3(camera, controller, rendering, soundManager);
				}
				throw new IllegalStateException();
			default:
				return scenes3D_enabled ? new PlayScene3D(controller, height)
						: new PlayScene2D(camera, controller, rendering, soundManager);
			}
		}
		throw new IllegalStateException();
	}

	private Class<? extends GameScene> getSceneClassForCurrentGame() {
		if (currentGame() == PACMAN) {
			switch (game.state) {
			case INTRO:
				return PacMan_IntroScene.class;
			case INTERMISSION:
				if (game.intermissionNumber == 1) {
					return PacMan_IntermissionScene1.class;
				}
				if (game.intermissionNumber == 2) {
					return PacMan_IntermissionScene2.class;
				}
				if (game.intermissionNumber == 3) {
					return PacMan_IntermissionScene3.class;
				}
				break;
			default:
				return scenes3D_enabled ? PlayScene3D.class : PlayScene2D.class;
			}
		} else {
			switch (game.state) {
			case INTRO:
				return MsPacMan_IntroScene.class;
			case INTERMISSION:
				if (game.intermissionNumber == 1) {
					return MsPacMan_IntermissionScene1.class;
				}
				if (game.intermissionNumber == 2) {
					return MsPacMan_IntermissionScene2.class;
				}
				if (game.intermissionNumber == 3) {
					return MsPacMan_IntermissionScene3.class;
				}
				break;
			default:
				return scenes3D_enabled ? PlayScene3D.class : PlayScene2D.class;
			}
		}
		throw new IllegalStateException();
	}

	@Override
	public void onGameChanged(GameModel newGame) {
		game = Objects.requireNonNull(newGame);
	}

	@Override
	public void onEnterLevel(int levelNumber) {
		if (currentGameScene instanceof PlayScene3D) {
			currentGameScene.start();
			showFlashMessage("Enter level " + game.levelNumber, clock.sec(1));
		}
	}

	@Override
	public void update() {
		handleGlobalKeys();
		Class<? extends GameScene> sceneToDisplay = getSceneClassForCurrentGame();
		if (currentGameScene == null || currentGameScene.getClass() != sceneToDisplay) {
			log("%s: Scene changes from %s to %s", this, currentGameScene, sceneToDisplay);
			if (currentGameScene != null) {
				currentGameScene.end();
			}
			if (GameScene2D.class.isAssignableFrom(sceneToDisplay)) {
				GameScene scene2D = createGameScene(subScene2D.getSubScene().getCamera(), mainScene.getHeight());
				setGameScene(scene2D);
			} else if (GameScene3D.class.isAssignableFrom(sceneToDisplay)) {
				GameScene scene3D = createGameScene(null, mainScene.getHeight());
				setGameScene(scene3D);
			}
		}
		currentGameScene.update();
		flashMessageView.update();
		updateInfoView();

		// 2D content is drawn explicitly:
		if (currentGameScene instanceof GameScene2D) {
			subScene2D.draw((GameScene2D) currentGameScene);
		}
	}

	private void updateInfoView() {
		String text = "";
		if (camControl != null) {
			text += camControl.getCameraInfo();
			text += "\n";
		}
		text += String.format("Main scene: w=%.2f h=%.2f", mainScene.getWidth(), mainScene.getHeight());
		if (currentGameScene instanceof GameScene2D) {
			text += String.format("\n2D scene: w=%.2f h=%.2f", subScene2D.getSubScene().getWidth(),
					subScene2D.getSubScene().getHeight());
		} else {
			GameScene3D scene3D = (GameScene3D) currentGameScene;
			text += String.format("\n3D scene: w=%.2f h=%.2f", scene3D.getSubScene().getWidth(),
					scene3D.getSubScene().getHeight());
		}
		infoView.setText(text);
	}

	private void handleGlobalKeys() {
		if (keyboard.keyPressed("F11")) {
			stage.setFullScreen(true);
		}
		if (keyboard.keyPressed("F3")) {
			scenes3D_enabled = !scenes3D_enabled;
			showFlashMessage(String.format("3D scenes are %s", scenes3D_enabled ? "ENABLED" : "DISABLED"), clock.sec(1));
		}
		if (keyboard.keyPressed("C")) {
			toggleCameraControlFor2DScene();
		}
		if (keyboard.keyPressed("S")) {
			clock.targetFreq = clock.targetFreq != 30 ? 30 : 60;
			String text = clock.targetFreq == 60 ? "Normal speed" : "Slow speed";
			showFlashMessage(text, clock.sec(1.5));
		}
		if (keyboard.keyPressed("F")) {
			clock.targetFreq = clock.targetFreq != 120 ? 120 : 60;
			String text = clock.targetFreq == 60 ? "Normal speed" : "Fast speed";
			showFlashMessage(text, clock.sec(1.5));
		}
	}

	private GameType currentGame() {
		return Stream.of(GameType.values()).filter(controller::isPlaying).findFirst().get();
	}

	private void toggleCameraControlFor2DScene() {
		if (camControl != null) {
			stage.removeEventHandler(KeyEvent.KEY_PRESSED, camControl::handleKeyEvent);
			camControl = null;
			subScene2D.perspectiveViewOff();
			showFlashMessage("Perspective View OFF", clock.sec(0.5));
		} else {
			camControl = new CameraController(subScene2D.getPerspectiveCamera());
			stage.addEventHandler(KeyEvent.KEY_PRESSED, camControl::handleKeyEvent);
			subScene2D.perspectiveViewOn();
			showFlashMessage("Perspective View ON", clock.sec(0.5));
		}
	}

	@Override
	public void reset() {
		currentGameScene.end();
		onGameChanged(game);
	}

	@Override
	public void showFlashMessage(String message, long ticks) {
		flashMessageView.showMessage(message, ticks);
	}

	@Override
	public boolean keyPressed(String keySpec) {
		return keyboard.keyPressed(keySpec);
	}

	@Override
	public Optional<SoundManager> sound() {
		if (muted) {
			return Optional.empty(); // TODO
		}
		return Optional.of(sounds.get(currentGame()));
	}

	@Override
	public void mute(boolean state) {
		muted = state;
	}

	@Override
	public Optional<PacManGameAnimations> animation() {
		if (currentGameScene instanceof GameScene2D) {
			return Optional.of(renderings.get(currentGame()));
		}
		if (currentGameScene instanceof PlayScene3D) {
			return Optional.of((PlayScene3D) currentGameScene);
		}
		return Optional.empty();
	}
}