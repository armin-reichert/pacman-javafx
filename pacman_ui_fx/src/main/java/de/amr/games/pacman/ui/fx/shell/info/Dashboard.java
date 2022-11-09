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

import java.util.stream.Stream;

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

	// BorderPane:
	public final VBox lhs = new VBox();
	public final VBox rhs = new VBox();

	private Section secGeneral;
	private Section secGameControl;
	private Section secGameInfo;
	private Section sec3D;
	private Section secCamera3D;
	private Section secKeys;

	public Dashboard(GameUI ui) {
		secGeneral = new SectionGeneral(ui, "General", MIN_LABEL_WIDTH, TEXT_COLOR, TEXT_FONT, LABEL_FONT);
		secGameControl = new SectionGameControl(ui, "Game Control", MIN_LABEL_WIDTH, TEXT_COLOR, TEXT_FONT, LABEL_FONT);
		secGameInfo = new SectionGameInfo(ui, "Game Info", MIN_LABEL_WIDTH, TEXT_COLOR, TEXT_FONT, LABEL_FONT);
		sec3D = new Section3D(ui, "3D Settings", MIN_LABEL_WIDTH, TEXT_COLOR, TEXT_FONT, LABEL_FONT);
		secCamera3D = new SectionCamera3D(ui, "3D Camera", MIN_LABEL_WIDTH, TEXT_COLOR, TEXT_FONT, LABEL_FONT);
		secKeys = new SectionKeys(ui, "Keyboard Shortcuts", MIN_LABEL_WIDTH, TEXT_COLOR, TEXT_FONT, LABEL_FONT);
		lhs.getChildren().addAll(secGeneral, secGameControl, secGameInfo, sec3D, secCamera3D, secKeys);
		setLeft(lhs);
		setRight(rhs);
		setVisible(false);
	}

	public Stream<Section> sections() {
		return Stream.of(secGeneral, secGameControl, secGameInfo, sec3D, secCamera3D, secKeys);
	}

	public void update() {
		sections().forEach(Section::update);
	}
}