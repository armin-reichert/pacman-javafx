package de.amr.games.pacman.ui.fx.app;

import static javafx.scene.layout.BackgroundSize.AUTO;

import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui.fx.util.ResourceManager;
import de.amr.games.pacman.ui.fx.util.Theme;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

/**
 * @author Armin Reichert
 */
public class StartPage extends StackPane {

	private BorderPane borderPane = new BorderPane();
	private Runnable playAction;

	public StartPage(Theme theme, GameVariant gameVariant, Runnable playAction) {
		this.playAction = playAction;

		var ds = new DropShadow();
		ds.setOffsetY(3.0f);
		ds.setColor(Color.color(0.2f, 0.2f, 0.2f));

		var buttonFont = theme.font("font.arcade", 30);
		var playButtonText = new Text("Play!");
		playButtonText.setEffect(ds);
		playButtonText.setCache(true);
		playButtonText.setFill(Color.WHITE);
		playButtonText.setFont(buttonFont);
		BorderPane.setAlignment(playButtonText, Pos.CENTER);

		// TODO that should be a graphical button, but GWT has its problems with those...
		var color = Color.rgb(0, 155, 252, 0.9);
		var playButton = new StackPane(playButtonText);
		playButton.setMaxSize(200, 100);
		playButton.setPadding(new Insets(10));
		playButton.setCursor(Cursor.HAND);
		playButton.setBackground(ResourceManager.coloredRoundedBackground(color, 20));
		playButton.setOnMouseClicked(e -> {
			if (e.getButton().equals(MouseButton.PRIMARY)) {
				playAction.run();
			}
		});

		borderPane.setBottom(playButton);
		BorderPane.setAlignment(playButton, Pos.CENTER);
		playButton.setTranslateY(-10);

		boolean msPacMan = gameVariant == GameVariant.MS_PACMAN;
		var image = PacManGames2d.MGR
				.image(msPacMan ? "graphics/mspacman/wallpaper-midway.png" : "graphics/pacman/1980-Flyer-USA-Midway-front.jpg");
		var pageBackgroundImage = new BackgroundImage(image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
				BackgroundPosition.CENTER, new BackgroundSize(AUTO, AUTO, false, false, true, false));
		borderPane.setBackground(new Background(pageBackgroundImage));

		setBackground(ResourceManager.coloredBackground(Color.BLACK));

		getChildren().add(borderPane);
	}

	public void handleKeyPressed(KeyEvent e) {
		if (e.getCode() == KeyCode.ENTER) {
			playAction.run();
		}
	}
}