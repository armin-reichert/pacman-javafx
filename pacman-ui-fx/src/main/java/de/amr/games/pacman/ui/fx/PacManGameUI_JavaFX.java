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
import de.amr.games.pacman.ui.fx.common.ControllablePerspectiveCamera;
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
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
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
	private GameScene currentGameScene;

	private final PacManGameController controller;
	private final Keyboard keyboard = new Keyboard();

	private final double scaling;
	private final Stage stage;
	private final Scene mainScene;
	private Text camInfoView;
	private Text flashMessageView;
	private final ControllablePerspectiveCamera cam = new ControllablePerspectiveCamera();

	private boolean muted;

	public PacManGameUI_JavaFX(Stage stage, PacManGameController controller, double scaling) {
		this.stage = stage;
		this.controller = controller;
		this.scaling = scaling;

		renderings.put(MS_PACMAN, new MsPacMan_StandardRendering());
		renderings.put(PACMAN, new PacMan_StandardRendering());

		sounds.put(MS_PACMAN, new PacManGameSoundManager(PacManGameSounds::msPacManSoundURL));
		sounds.put(PACMAN, new PacManGameSoundManager(PacManGameSounds::mrPacManSoundURL));

		createGameScenes();

		mainScene = new Scene(createMainSceneContent(null), UNSCALED_SCENE_WIDTH_PX * scaling,
				UNSCALED_SCENE_HEIGHT_PX * scaling, Color.BLACK);

		stage.setScene(mainScene);
		stage.setTitle("Pac-Man / Ms. Pac-Man (JavaFX)");
		stage.getIcons().add(new Image("/pacman/graphics/pacman.png"));
		stage.setOnCloseRequest(e -> {
			Platform.exit();
			System.exit(0); // TODO
		});
		stage.addEventHandler(KeyEvent.KEY_PRESSED, keyboard::onKeyPressed);
		stage.addEventHandler(KeyEvent.KEY_RELEASED, keyboard::onKeyReleased);

		// trigger scene intialization
		onGameChangedFX(controller.getGame());

		log("JavaFX UI created at clock tick %d", clock.ticksTotal);
	}

	private void createGameScenes() {
		gameScenes.put(MS_PACMAN, Arrays.asList(//
				new MsPacMan_IntroScene(scaling, renderings.get(MS_PACMAN), sounds.get(MS_PACMAN)), //
				new MsPacMan_IntermissionScene1(scaling, renderings.get(MS_PACMAN), sounds.get(MS_PACMAN)), //
				new MsPacMan_IntermissionScene2(scaling, renderings.get(MS_PACMAN), sounds.get(MS_PACMAN)), //
				new MsPacMan_IntermissionScene3(scaling, renderings.get(MS_PACMAN), sounds.get(MS_PACMAN)), //
				new PlayScene(scaling, renderings.get(MS_PACMAN), sounds.get(MS_PACMAN))//
		));
		gameScenes.get(MS_PACMAN).get(4).cameraAllowed = true;

		gameScenes.put(PACMAN, Arrays.asList(//
				new PacMan_IntroScene(scaling, renderings.get(PACMAN), sounds.get(PACMAN)), //
				new PacMan_IntermissionScene1(scaling, renderings.get(PACMAN), sounds.get(PACMAN)), //
				new PacMan_IntermissionScene2(scaling, renderings.get(PACMAN), sounds.get(PACMAN)), //
				new PacMan_IntermissionScene3(scaling, renderings.get(PACMAN), sounds.get(PACMAN)), //
				new PlayScene(scaling, renderings.get(PACMAN), sounds.get(PACMAN))//
		));
		gameScenes.get(PACMAN).get(4).cameraAllowed = true;
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
		Platform.runLater(() -> onGameChangedFX(newGame));
	}

	private void onGameChangedFX(GameModel newGame) {
		game = Objects.requireNonNull(newGame);
		gameScenes.get(currentGame()).forEach(scene -> scene.setGame(game));
		setGameScene(currentGameScene());
	}

	private void setGameScene(GameScene newScene) {
		currentGameScene = newScene;
		currentGameScene.start();
		mainScene.setRoot(createMainSceneContent(newScene));
		cameraOff();
	}

	private Parent createMainSceneContent(GameScene gameScene) {
		StackPane layout = new StackPane();

		camInfoView = new Text();
		camInfoView.setTextAlignment(TextAlignment.CENTER);
		camInfoView.setFill(Color.WHITE);
		camInfoView.setFont(Font.font("Sans", 6 * scaling));
		Bindings.bindBidirectional(camInfoView.textProperty(), cam.infoProperty);
		StackPane.setAlignment(camInfoView, Pos.CENTER);

		flashMessageView = new Text();
		flashMessageView.setFont(Font.font("Serif", FontWeight.BOLD, 10 * scaling));
		flashMessageView.setFill(Color.YELLOW);
		StackPane messageBox = new StackPane(flashMessageView);
		StackPane.setAlignment(flashMessageView, Pos.BOTTOM_CENTER);
		StackPane.setAlignment(messageBox, Pos.BOTTOM_CENTER);

		Node sceneContent = gameScene == null ? new Group() : gameScene.content;
		layout.getChildren().addAll(sceneContent, camInfoView, messageBox);

		return layout;
	}

	@Override
	public void update() {
		Platform.runLater(this::updateFX);
	}

	private void updateFX() {
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
		updateFlashMessages();
	}

	private void updateFlashMessages() {
		FlashMessage message = flashMessagesQ.peek();
		if (message != null) {
			// a message is available
			if (message.timer.expired()) {
				flashMessagesQ.remove();
				flashMessageView.setVisible(false);
				return;
			}
			message.timer.run();
			flashMessageView.setVisible(true);
			flashMessageView.setText(message.text);
			double alpha = Math.cos((message.timer.running() * Math.PI / 2.0) / message.timer.getDuration());
			flashMessageView.setFill(Color.rgb(255, 255, 0, alpha));
		}
	}

	@Override
	public void render() {
		Platform.runLater(this::renderFX);
	}

	private void renderFX() {
		try {
			if (cam != null) {
				currentGameScene.updateCamera(cam);
			}
			currentGameScene.draw();
		} catch (Exception x) {
			log("Exception occurred when rendering scene %s", currentGameScene);
			x.printStackTrace();
		}
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
			stage.setFullScreen(true);
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
		if (currentGameScene.cameraAllowed) {
			if (mainScene.getCamera() == null) {
				cameraOn();
				showFlashMessage("Camera ON", clock.sec(1));
			} else {
				cameraOff();
				showFlashMessage("Camera OFF", clock.sec(1));
			}
		}
	}

	private void cameraOff() {
		cam.setTranslateX(0);
		cam.setTranslateY(0);
		cam.setTranslateZ(0);
		cam.setRotate(0);
		mainScene.removeEventHandler(KeyEvent.KEY_PRESSED, cam::onKeyPressed);
		mainScene.setCamera(null);
		camInfoView.setVisible(false);
	}

	private void cameraOn() {
		cam.setRotationAxis(Rotate.X_AXIS);
		cam.setRotate(30);
		cam.setTranslateZ(-240);
		mainScene.setCamera(cam);
		mainScene.addEventHandler(KeyEvent.KEY_PRESSED, cam::onKeyPressed);
		camInfoView.setVisible(true);
	}
}