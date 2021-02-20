package de.amr.games.pacman.ui.fx;

import static de.amr.games.pacman.heaven.God.clock;
import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.world.PacManGameWorld.TS;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.EnumMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameType;
import de.amr.games.pacman.sound.PacManGameSoundManager;
import de.amr.games.pacman.sound.PacManGameSounds;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.FlashMessage;
import de.amr.games.pacman.ui.PacManGameAnimation;
import de.amr.games.pacman.ui.PacManGameUI;
import de.amr.games.pacman.ui.fx.common.GameScene;
import de.amr.games.pacman.ui.fx.common.PlayScene;
import de.amr.games.pacman.ui.fx.mspacman.MsPacMan_IntermissionScene1;
import de.amr.games.pacman.ui.fx.mspacman.MsPacMan_IntermissionScene2;
import de.amr.games.pacman.ui.fx.mspacman.MsPacMan_IntermissionScene3;
import de.amr.games.pacman.ui.fx.mspacman.MsPacMan_IntroScene;
import de.amr.games.pacman.ui.fx.mspacman.MsPacMan_SceneRendering;
import de.amr.games.pacman.ui.fx.pacman.PacMan_IntermissionScene1;
import de.amr.games.pacman.ui.fx.pacman.PacMan_IntermissionScene2;
import de.amr.games.pacman.ui.fx.pacman.PacMan_IntermissionScene3;
import de.amr.games.pacman.ui.fx.pacman.PacMan_IntroScene;
import de.amr.games.pacman.ui.fx.pacman.PacMan_SceneRendering;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

/**
 * JavaFX implementation of the Pac-Man game UI.
 * 
 * @author Armin Reichert
 */
public class PacManGameFXUI implements PacManGameUI {

	public static final MsPacMan_SceneRendering MS_PACMAN_RENDERING = new MsPacMan_SceneRendering();
	public static final PacMan_SceneRendering PACMAN_RENDERING = new PacMan_SceneRendering();

	public static final SoundManager MS_PACMAN_SOUNDS = new PacManGameSoundManager(PacManGameSounds::getMsPacManSoundURL);
	public static final SoundManager PACMAN_SOUNDS = new PacManGameSoundManager(PacManGameSounds::getPacManSoundURL);

	private static final Deque<FlashMessage> FLASH_MESSAGE_Q = new ArrayDeque<>();

	public static Optional<FlashMessage> flashMessage() {
		return Optional.ofNullable(FLASH_MESSAGE_Q.peek());
	}

	private final PacManGameController controller;
	private final Stage stage;

	private final EnumMap<GameType, List<GameScene>> scenes = new EnumMap<>(GameType.class);
	private GameScene currentScene;

	private GameModel game;
	private boolean muted;

	public PacManGameFXUI(Stage stage, PacManGameController controller, double scaling) {
		this.controller = controller;
		double width = 28 * TS * scaling;
		double height = 36 * TS * scaling;
		this.stage = stage;
		stage.setTitle("JavaFX: Pac-Man / Ms. Pac-Man");
		stage.getIcons().add(new Image("/pacman/graphics/pacman.png"));
		stage.setOnCloseRequest(e -> {
			Platform.exit();
		});
		stage.addEventHandler(javafx.scene.input.KeyEvent.KEY_PRESSED, e -> {
			handleGlobalKeys(e);
		});

		scenes.put(GameType.MS_PACMAN, Arrays.asList(//
				new MsPacMan_IntroScene(new Group(), width, height, scaling), //
				new MsPacMan_IntermissionScene1(new Group(), width, height, scaling), //
				new MsPacMan_IntermissionScene2(new Group(), width, height, scaling), //
				new MsPacMan_IntermissionScene3(new Group(), width, height, scaling), //
				new PlayScene(new Group(), width, height, scaling, MS_PACMAN_RENDERING)//
		));
		scenes.put(GameType.PACMAN, Arrays.asList(//
				new PacMan_IntroScene(new Group(), width, height, scaling), //
				new PacMan_IntermissionScene1(new Group(), width, height, scaling), //
				new PacMan_IntermissionScene2(new Group(), width, height, scaling), //
				new PacMan_IntermissionScene3(new Group(), width, height, scaling), //
				new PlayScene(new Group(), width, height, scaling, PACMAN_RENDERING)//
		));

		onGameChanged(controller.getGame());
		log("JavaFX UI created at clock tick %d", clock.ticksTotal);
	}

	private void handleGlobalKeys(KeyEvent e) {
		switch (e.getCode()) {
		case S: {
			clock.targetFreq = clock.targetFreq != 30 ? 30 : 60;
			String text = clock.targetFreq == 60 ? "Normal speed" : "Slow speed";
			showFlashMessage(text, clock.sec(1.5));
			log("Clock frequency changed to %d Hz", clock.targetFreq);
			break;
		}
		case F: {
			clock.targetFreq = clock.targetFreq != 120 ? 120 : 60;
			String text = clock.targetFreq == 60 ? "Normal speed" : "Fast speed";
			showFlashMessage(text, clock.sec(1.5));
			log("Clock frequency changed to %d Hz", clock.targetFreq);
			break;
		}
		default:
			break;
		}
	}

	private GameScene currentGameScene() {
		switch (game.state) {
		case INTRO:
			return scenes.get(controller.currentGameType()).get(0);
		case INTERMISSION:
			return scenes.get(controller.currentGameType()).get(game.intermissionNumber);
		default:
			return scenes.get(controller.currentGameType()).get(4);
		}
	}

	@Override
	public void onGameChanged(GameModel newGame) {
		game = Objects.requireNonNull(newGame);
		scenes.get(controller.currentGameType()).forEach(scene -> scene.setGame(game));
		currentScene = currentGameScene();
		currentScene.start();
	}

	@Override
	public void show() {
		stage.setScene(currentScene);
		stage.sizeToScene();
		stage.centerOnScreen();
		stage.show();
	}

	@Override
	public void update() {
		GameScene sceneToDisplay = currentGameScene();
		if (currentScene != sceneToDisplay) {
			log("%s: Scene changes from %s to %s", this, currentScene, sceneToDisplay);
			if (currentScene != null) {
				currentScene.end();
			}
			sceneToDisplay.start();
			currentScene = sceneToDisplay;
		}
		currentScene.update();

		FlashMessage message = FLASH_MESSAGE_Q.peek();
		if (message != null) {
			message.timer.run();
			if (message.timer.expired()) {
				FLASH_MESSAGE_Q.remove();
			}
		}
	}

	@Override
	public void render() {
		// TODO Should the game loop run on the JavaFX application thread?
		Platform.runLater(() -> {
			if (stage.getScene() != currentScene) {
				stage.setScene(currentScene);
			}
			try {
				currentScene.render();
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
		FLASH_MESSAGE_Q.add(new FlashMessage(message, ticks));
	}

	@Override
	public boolean keyPressed(String keySpec) {
		boolean pressed = currentScene.keyboard().keyPressed(keySpec);
		currentScene.keyboard().clearKey(keySpec); // TODO
		return pressed;
	}

	@Override
	public Optional<SoundManager> sound() {
		if (muted) {
			return Optional.empty(); // TODO
		}
		return Optional.of(controller.currentGameType() == GameType.MS_PACMAN ? MS_PACMAN_SOUNDS : PACMAN_SOUNDS);
	}

	@Override
	public void mute(boolean state) {
		muted = state;
	}

	@Override
	public Optional<PacManGameAnimation> animation() {
		return Optional.of(controller.currentGameType() == GameType.MS_PACMAN ? MS_PACMAN_RENDERING : PACMAN_RENDERING);
	}
}