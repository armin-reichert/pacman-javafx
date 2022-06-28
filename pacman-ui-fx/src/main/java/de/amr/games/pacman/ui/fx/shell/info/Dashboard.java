/*
MIT License

Copyright (c) 2021-22 Armin Reichert

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
package de.amr.games.pacman.ui.fx.shell.info;

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.ui.fx.shell.GameUI;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Dashboard with different sections showing info and allowing configuration.
 * 
 * @author Armin Reichert
 */
public class Dashboard extends BorderPane {

	public static final int MIN_COL_WIDTH = 100;
	public static final int MIN_LABEL_WIDTH = 120;
	public static final Color TEXT_COLOR = Color.WHITE;
	public static final Font LABEL_FONT = Font.font("Tahoma", 12);
	public static final Font TEXT_FONT = Font.font("Tahoma", 12);

	private final VBox leftSide = new VBox();
	private final VBox rightSide = new VBox();
	private final SectionGeneral sectionGeneral;
	private final SectionGame sectionGame;
	private final Section3D section3D;
	private final SectionKeys sectionKeys;

	public Dashboard(GameUI ui, GameController gc) {
		sectionGeneral = new SectionGeneral(ui, gc, "General", MIN_LABEL_WIDTH, TEXT_COLOR, TEXT_FONT, LABEL_FONT);
		sectionGame = new SectionGame(ui, gc, "Game", MIN_LABEL_WIDTH, TEXT_COLOR, TEXT_FONT, LABEL_FONT);
		section3D = new Section3D(ui, gc, "3D Settings", MIN_LABEL_WIDTH, TEXT_COLOR, TEXT_FONT, LABEL_FONT);
		sectionKeys = new SectionKeys(ui, gc, "Keyboard Shortcuts", MIN_LABEL_WIDTH, TEXT_COLOR, TEXT_FONT, LABEL_FONT);

		leftSide.getChildren().addAll(sectionGeneral, sectionGame);
		setLeft(leftSide);

		rightSide.getChildren().addAll(section3D, sectionKeys);
		setRight(rightSide);

		sectionGeneral.setExpanded(false);
		sectionGame.setExpanded(false);
		section3D.setExpanded(false);
		sectionKeys.setExpanded(false);

		setVisible(false);
	}

	public void update() {
		sectionGame.update();
		sectionGeneral.update();
		sectionKeys.update();
		section3D.update();
	}
}