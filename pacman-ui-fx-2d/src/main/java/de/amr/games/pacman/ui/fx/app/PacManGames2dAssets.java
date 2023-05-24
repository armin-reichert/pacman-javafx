/*
MIT License

Copyright (c) 2023 Armin Reichert

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

package de.amr.games.pacman.ui.fx.app;

import java.util.ResourceBundle;

import de.amr.games.pacman.ui.fx.rendering2d.ArcadeTheme;
import de.amr.games.pacman.ui.fx.rendering2d.Theme;
import de.amr.games.pacman.ui.fx.util.ResourceManager;
import javafx.scene.media.AudioClip;

/**
 * @author Armin Reichert
 */
public class PacManGames2dAssets {

	public static final ResourceManager MGR = new ResourceManager("/de/amr/games/pacman/ui/fx/",
			PacManGames2dAssets.class);

	//@formatter:off
	public final ResourceBundle messages           = ResourceBundle.getBundle("de.amr.games.pacman.ui.fx.texts.messages");
	public final Theme arcadeTheme                 = new ArcadeTheme(MGR);
	public final AudioClip voiceExplainKeys        = MGR.audioClip("sound/voice/press-key.mp3");
	public final AudioClip voiceAutopilotOff       = MGR.audioClip("sound/voice/autopilot-off.mp3");
	public final AudioClip voiceAutopilotOn        = MGR.audioClip("sound/voice/autopilot-on.mp3");
	public final AudioClip voiceImmunityOff        = MGR.audioClip("sound/voice/immunity-off.mp3");
	public final AudioClip voiceImmunityOn         = MGR.audioClip("sound/voice/immunity-on.mp3");
	//@formatter:on
}