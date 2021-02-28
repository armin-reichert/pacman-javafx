package de.amr.games.pacman.ui.fx;

import static de.amr.games.pacman.heaven.God.clock;
import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.common.GameType.MS_PACMAN;
import static de.amr.games.pacman.model.common.GameType.PACMAN;
import static de.amr.games.pacman.world.PacManGameWorld.TS;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.EnumMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameType;
import de.amr.games.pacman.sound.PacManGameSoundManager;
import de.amr.games.pacman.sound.PacManGameSounds;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.FlashMessage;
import de.amr.games.pacman.ui.PacManGameUI;
import de.amr.games.pacman.ui.animation.PacManGameAnimations;
import de.amr.games.pacman.ui.fx.common.GameScene;
import de.amr.games.pacman.ui.fx.common.PlayScene;
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
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

/**
 * JavaFX implementation of the Pac-Man game UI.
 * 
 * @author Armin Reichert
 */
public class PacManGameUI_JavaFX implements PacManGameUI {

	public static final int SCENE_WIDTH_TILES = 28;
	public static final int SCENE_HEIGHT_TILES = 36;

	public static final int UNSCALED_SCENE_WIDTH_PX = SCENE_WIDTH_TILES * TS;
	public static final int UNSCALED_SCENE_HEIGHT_PX = SCENE_HEIGHT_TILES * TS;

	private final Deque<FlashMessage> flashMessagesQ = new ArrayDeque<>();

	private final EnumMap<GameType, FXRendering> renderings = new EnumMap<>(GameType.class);
	private final EnumMap<GameType, SoundManager> sounds = new EnumMap<>(GameType.class);
	private final EnumMap<GameType, List<GameScene>> gameScenes = new EnumMap<>(GameType.class);

	private GameModel game;

	private final PacManGameController controller;
	private final Keyboard keyboard = new Keyboard();

	private final Stage stage;
	private final Scene primaryScene;
	private GameScene currentGameScene;

	private boolean muted;

	public PacManGameUI_JavaFX(Stage stage, PacManGameController controller, double scaling) {
		this.stage = stage;
		this.controller = controller;

		stage.setTitle("Pac-Man / Ms. Pac-Man (JavaFX)");
		stage.getIcons().add(new Image("/pacman/graphics/pacman.png"));
		stage.setOnCloseRequest(e -> {
			Platform.exit();
			System.exit(0); // TODO
		});

		primaryScene = new Scene(new Group(), UNSCALED_SCENE_WIDTH_PX * scaling, UNSCALED_SCENE_HEIGHT_PX * scaling,
				Color.BLACK);
		stage.setScene(primaryScene);

		stage.addEventHandler(KeyEvent.KEY_PRESSED, keyboard::onKeyPressed);
		stage.addEventHandler(KeyEvent.KEY_RELEASED, keyboard::onKeyReleased);
		stage.addEventHandler(KeyEvent.KEY_PRESSED, this::handleCameraKeys);

		renderings.put(MS_PACMAN, new MsPacMan_StandardRendering());
		renderings.put(PACMAN, new PacMan_StandardRendering());

		sounds.put(MS_PACMAN, new PacManGameSoundManager(PacManGameSounds::msPacManSoundURL));
		sounds.put(PACMAN, new PacManGameSoundManager(PacManGameSounds::mrPacManSoundURL));

		gameScenes.put(MS_PACMAN, Arrays.asList(//
				new MsPacMan_IntroScene(scaling, renderings.get(MS_PACMAN), sounds.get(MS_PACMAN)), //
				new MsPacMan_IntermissionScene1(scaling, renderings.get(MS_PACMAN), sounds.get(MS_PACMAN)), //
				new MsPacMan_IntermissionScene2(scaling, renderings.get(MS_PACMAN), sounds.get(MS_PACMAN)), //
				new MsPacMan_IntermissionScene3(scaling, renderings.get(MS_PACMAN), sounds.get(MS_PACMAN)), //
				new PlayScene(scaling, renderings.get(MS_PACMAN), sounds.get(MS_PACMAN))//
		));

		gameScenes.put(PACMAN, Arrays.asList(//
				new PacMan_IntroScene(scaling, renderings.get(PACMAN), sounds.get(PACMAN)), //
				new PacMan_IntermissionScene1(scaling, renderings.get(PACMAN), sounds.get(PACMAN)), //
				new PacMan_IntermissionScene2(scaling, renderings.get(PACMAN), sounds.get(PACMAN)), //
				new PacMan_IntermissionScene3(scaling, renderings.get(PACMAN), sounds.get(PACMAN)), //
				new PlayScene(scaling, renderings.get(PACMAN), sounds.get(PACMAN))//
		));

		onGameChanged(controller.getGame());
		log("JavaFX UI created at clock tick %d", clock.ticksTotal);
	}

	@Override
	public void show() {
		stage.sizeToScene();
		stage.centerOnScreen();
		stage.show();
	}

	private GameType currentGame() {
		return Stream.of(GameType.values()).filter(controller::isPlaying).findFirst().get();
	}

	private GameScene currentGameScene() {
		GameType currentGame = currentGame();
		switch (game.state) {
		case INTRO:
			return gameScenes.get(currentGame).get(0);
		case INTERMISSION:
			return gameScenes.get(currentGame).get(game.intermissionNumber);
		default:
			return gameScenes.get(currentGame).get(4);
		}
	}

	@Override
	public void onGameChanged(GameModel newGame) {
		game = Objects.requireNonNull(newGame);
		gameScenes.get(currentGame()).forEach(scene -> scene.setGame(game));
		setGameScene(currentGameScene());
	}

	private void setGameScene(GameScene newScene) {
		currentGameScene = newScene;
		currentGameScene.start();
		Platform.runLater(() -> {
			primaryScene.setRoot(new StackPane(newScene.root));
		});
	}

	@Override
	public void update() {
		handleGlobalKeys();
		GameScene sceneToDisplay = currentGameScene();
		if (currentGameScene != sceneToDisplay) {
			log("%s: Scene changes from %s to %s", this, currentGameScene, sceneToDisplay);
			if (currentGameScene != null) {
				currentGameScene.end();
			}
			setGameScene(sceneToDisplay);
		}
		currentGameScene.update();
		updateCamera();

		FlashMessage message = flashMessagesQ.peek();
		if (message != null) {
			message.timer.run();
			if (message.timer.expired()) {
				flashMessagesQ.remove();
			}
		}
	}

	@Override
	public void render() {
		// TODO Should the game loop run on the JavaFX application thread?
		Platform.runLater(() -> {
			try {
				currentGameScene.draw();
				// TODO more FX-like solution
				if (!flashMessagesQ.isEmpty()) {
					currentGameScene.drawFlashMessage(flashMessagesQ.peek());
				}
			} catch (Exception x) {
				log("Exception occurred when rendering scene %s", currentGameScene);
				x.printStackTrace();
			}
		});
	}

	@Override
	public void reset() {
		currentGameScene.end();
		onGameChanged(game);
	}

	@Override
	public void showFlashMessage(String message, long ticks) {
		flashMessagesQ.add(new FlashMessage(message, ticks));
	}

	@Override
	public boolean keyPressed(String keySpec) {
		boolean pressed = keyboard.keyPressed(keySpec);
		keyboard.clearKey(keySpec);
		return pressed;
	}

	@Override
	public void mute(boolean state) {
		muted = state;
	}

	@Override
	public Optional<SoundManager> sound() {
		if (muted) {
			return Optional.empty(); // TODO
		}
		return Optional.of(sounds.get(currentGame()));
	}

	@Override
	public Optional<PacManGameAnimations> animation() {
		return Optional.of(renderings.get(currentGame()));
	}

	private void handleGlobalKeys() {
		if (keyboard.keyPressed(KeyCode.F11.getName())) {
			Platform.runLater(() -> stage.setFullScreen(true));
		}
		if (keyboard.keyPressed("C")) {
			toggleSceneCamera();
		}
		if (keyboard.keyPressed("S")) {
			clock.targetFreq = clock.targetFreq != 30 ? 30 : 60;
			String text = clock.targetFreq == 60 ? "Normal speed" : "Slow speed";
			showFlashMessage(text, clock.sec(1.5));
			log("Clock frequency changed to %d Hz", clock.targetFreq);
		}
		if (keyboard.keyPressed("F")) {
			clock.targetFreq = clock.targetFreq != 120 ? 120 : 60;
			String text = clock.targetFreq == 60 ? "Normal speed" : "Fast speed";
			showFlashMessage(text, clock.sec(1.5));
			log("Clock frequency changed to %d Hz", clock.targetFreq);
		}
	}

	private void toggleSceneCamera() {
		if (primaryScene.getCamera() == null) {
			Camera cam = new PerspectiveCamera();
			cam.setRotationAxis(Rotate.X_AXIS);
			cam.setTranslateZ(300);
			primaryScene.setCamera(cam);
			updateCamera();
			showFlashMessage("Camera ON", clock.sec(1));
		} else {
			Camera cam = primaryScene.getCamera();
			cam.setTranslateX(0);
			cam.setTranslateY(0);
			cam.setTranslateZ(0);
			cam.setRotate(0);
			primaryScene.setCamera(null);
			showFlashMessage("Camera OFF", clock.sec(1));
		}
	}

	private void updateCamera() {
		Camera cam = primaryScene.getCamera();
		if (cam != null) {
			currentGameScene.updateCamera(cam);
			if (clock.ticksTotal % 20 == 0) {
				String text = String.format("Camera\nx:%3.0f y:%3.0f z:%3.0f rot:%3.0f", cam.getTranslateX(),
						cam.getTranslateY(), cam.getTranslateZ(), cam.getRotate());
				currentGameScene.camInfo.setText(text);
			}
		} else {
			currentGameScene.camInfo.setText("");
		}
	}

	private void handleCameraKeys(KeyEvent e) {
		Camera camera = primaryScene.getCamera();
		if (camera == null) {
			return;
		}
		if (e.isControlDown()) {
			switch (e.getCode()) {
			case DIGIT0:
				camera.setTranslateX(0);
				camera.setTranslateY(0);
				camera.setTranslateZ(0);
				break;
			case LEFT:
				camera.setTranslateX(camera.getTranslateX() + 10);
				log("Cam moves LEFT");
				break;
			case RIGHT:
				camera.setTranslateX(camera.getTranslateX() - 10);
				log("Cam moves RIGHT");
				break;
			case UP:
				camera.setTranslateY(camera.getTranslateY() + 10);
				log("Cam moves UP");
				break;
			case DOWN:
				camera.setTranslateY(camera.getTranslateY() - 10);
				log("Cam moves DOWN");
				break;
			case PLUS:
				camera.setTranslateZ(camera.getTranslateZ() + 10);
				log("Cam zoomes IN");
				break;
			case MINUS:
				camera.setTranslateZ(camera.getTranslateZ() - 10);
				log("Cam zoomes OUT");
				break;
			default:
				break;
			}
		}
		if (e.isShiftDown()) {
			switch (e.getCode()) {
			case DOWN:
				camera.setRotate(camera.getRotate() - 1);
				log("Cam rotates FORWARD");
				break;
			case UP:
				camera.setRotate(camera.getRotate() + 1);
				log("Cam rotates BACKWARDS");
				break;
			default:
				break;
			}
		}
	}
}