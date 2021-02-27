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
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

/**
 * JavaFX implementation of the Pac-Man game UI.
 * 
 * @author Armin Reichert
 */
public class PacManGameUI_JavaFX implements PacManGameUI {

	public static final int SCENE_WIDTH_TILES = 28;
	public static final int SCENE_HEIGHT_TILES = 36;

	public static final int SCENE_WIDTH_PX = SCENE_WIDTH_TILES * TS;
	public static final int SCENE_HEIGHT_PX = SCENE_HEIGHT_TILES * TS;

	private final Deque<FlashMessage> flashMessagesQ = new ArrayDeque<>();

	private final EnumMap<GameType, FXRendering> renderings = new EnumMap<>(GameType.class);
	private final EnumMap<GameType, SoundManager> sounds = new EnumMap<>(GameType.class);
	private final EnumMap<GameType, List<GameScene>> scenes = new EnumMap<>(GameType.class);

	private final PacManGameController controller;
	private final Stage stage;
	private GameScene currentScene;
	private Keyboard keyboard;
	private GameModel game;
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

		keyboard = new Keyboard();
		stage.addEventHandler(KeyEvent.KEY_PRESSED, keyboard::onKeyPressed);
		stage.addEventHandler(KeyEvent.KEY_RELEASED, keyboard::onKeyReleased);
		stage.addEventHandler(KeyEvent.KEY_PRESSED, this::handleCameraKeys);

		renderings.put(MS_PACMAN, new MsPacMan_StandardRendering());
		renderings.put(PACMAN, new PacMan_StandardRendering());

		sounds.put(MS_PACMAN, new PacManGameSoundManager(PacManGameSounds::msPacManSoundURL));
		sounds.put(PACMAN, new PacManGameSoundManager(PacManGameSounds::mrPacManSoundURL));

		scenes.put(MS_PACMAN, Arrays.asList(//
				new MsPacMan_IntroScene(scaling, renderings.get(MS_PACMAN), sounds.get(MS_PACMAN)), //
				new MsPacMan_IntermissionScene1(scaling, renderings.get(MS_PACMAN), sounds.get(MS_PACMAN)), //
				new MsPacMan_IntermissionScene2(scaling, renderings.get(MS_PACMAN), sounds.get(MS_PACMAN)), //
				new MsPacMan_IntermissionScene3(scaling, renderings.get(MS_PACMAN), sounds.get(MS_PACMAN)), //
				new PlayScene(scaling, renderings.get(MS_PACMAN), sounds.get(MS_PACMAN))//
		));

		scenes.put(PACMAN, Arrays.asList(//
				new PacMan_IntroScene(scaling, renderings.get(PACMAN), sounds.get(PACMAN)), //
				new PacMan_IntermissionScene1(scaling, renderings.get(PACMAN), sounds.get(PACMAN)), //
				new PacMan_IntermissionScene2(scaling, renderings.get(PACMAN), sounds.get(PACMAN)), //
				new PacMan_IntermissionScene3(scaling, renderings.get(PACMAN), sounds.get(PACMAN)), //
				new PlayScene(scaling, renderings.get(PACMAN), sounds.get(PACMAN))//
		));

		onGameChanged(controller.getGame());
		log("JavaFX UI created at clock tick %d", clock.ticksTotal);
	}

	private GameType currentGame() {
		return Stream.of(GameType.values()).filter(controller::isPlaying).findFirst().get();
	}

	private GameScene currentScene() {
		GameType currentGame = currentGame();
		switch (game.state) {
		case INTRO:
			return scenes.get(currentGame).get(0);
		case INTERMISSION:
			return scenes.get(currentGame).get(game.intermissionNumber);
		default:
			return scenes.get(currentGame).get(4);
		}
	}

	@Override
	public void onGameChanged(GameModel newGame) {
		game = Objects.requireNonNull(newGame);
		scenes.get(currentGame()).forEach(scene -> scene.setGame(game));
		changeScene(currentScene());
	}

	private void changeScene(GameScene newScene) {
		currentScene = newScene;
		currentScene.start();
	}

	@Override
	public void show() {
		stage.setScene(currentScene.fxScene);
		stage.sizeToScene();
		stage.centerOnScreen();
		stage.show();
	}

	@Override
	public void update() {
		handleGlobalKeys();
		GameScene sceneToDisplay = currentScene();
		if (currentScene != sceneToDisplay) {
			log("%s: Scene changes from %s to %s", this, currentScene, sceneToDisplay);
			if (currentScene != null) {
				currentScene.end();
			}
			changeScene(sceneToDisplay);
		}
		currentScene.update();
		updateCamera(currentScene);

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
			if (stage.getScene() != currentScene.fxScene) {
				stage.setScene(currentScene.fxScene);
			}
			try {
				currentScene.clear();
				currentScene.doRender();
				if (!flashMessagesQ.isEmpty()) {
					currentScene.drawFlashMessage(flashMessagesQ.peek());
				}
			} catch (Exception x) {
				log("Exception occurred when rendering scene %s", currentScene);
				x.printStackTrace();
			}
		});
	}

	@Override
	public void reset() {
		currentScene.end();
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
		if (keyboard.keyPressed("C")) {
			Scene scene = currentScene.fxScene;
			if (scene.getCamera() == null) {
				Camera cam = new PerspectiveCamera();
				cam.setTranslateZ(300);
				scene.setCamera(cam);
				updateCamera(currentScene);
				showFlashMessage("Camera ON", clock.sec(1));
			} else {
				Camera cam = scene.getCamera();
				cam.setTranslateX(0);
				cam.setTranslateY(0);
				cam.setTranslateZ(0);
				cam.setRotate(0);
				scene.setCamera(null);
				showFlashMessage("Camera OFF", clock.sec(1));
			}
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

	private void updateCamera(GameScene scene) {
		Camera cam = scene.fxScene.getCamera();
		if (cam != null) {
			scene.updateCamera(cam);
			if (clock.ticksTotal % 20 == 0) {
				String text = String.format("Camera\nx:%3.0f y:%3.0f z:%3.0f rot:%3.0f", cam.getTranslateX(),
						cam.getTranslateY(), cam.getTranslateZ(), cam.getRotate());
				scene.camInfo.setText(text);
			}
		}
	}

	private void handleCameraKeys(KeyEvent e) {
		Camera camera = currentScene().fxScene.getCamera();
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
				camera.setRotate(camera.getRotate() - 10);
				log("Cam rotates FORWARD");
				break;
			case UP:
				camera.setRotate(camera.getRotate() + 10);
				log("Cam rotates BACKWARDS");
				break;
			default:
				break;
			}
		}
	}
}