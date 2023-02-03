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
package de.amr.games.pacman.ui.fx.dashboard;

import java.util.stream.Stream;

import de.amr.games.pacman.ui.fx.shell.GameUI;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Dashboard with different sections showing info and allowing configuration.
 * 
 * @author Armin Reichert
 */
public class Dashboard extends VBox {

	public static final int MIN_COL_WIDTH = 100;
	public static final int MIN_LABEL_WIDTH = 120;
	public static final Color TEXT_COLOR = Color.WHITE;
	public static final Font LABEL_FONT = Font.font("Tahoma", 12);
	public static final Font TEXT_FONT = Font.font("Tahoma", 12);

	private Section secGeneral;
	private Section secGameControl;
	private Section secGameInfo;
	private Section secGhostsInfo;
	private Section sec3D;
	private Section secCamera3D;
	private Section secKeys;

	public void init(GameUI ui) {
		secGeneral = new SectionGeneral(ui, "General");
		secGameControl = new SectionGameControl(ui, "Game Control");
		secGameInfo = new SectionGameInfo(ui, "Game Info");
		secGhostsInfo = new SectionGhostsInfo(ui, "Ghosts Info");
		sec3D = new Section3D(ui, "3D Settings");
		secCamera3D = new SectionCamera3D(ui, "3D Camera");
		secKeys = new SectionKeys(ui, "Keyboard Shortcuts");
		getChildren().addAll(secGeneral, secGameControl, secGameInfo, secGhostsInfo, sec3D, secCamera3D, secKeys);
		setVisible(false);
	}

	public Stream<Section> sections() {
		return Stream.of(secGeneral, secGameControl, secGameInfo, secGhostsInfo, sec3D, secCamera3D, secKeys);
	}

	public void update() {
		sections().forEach(Section::update);
	}
}