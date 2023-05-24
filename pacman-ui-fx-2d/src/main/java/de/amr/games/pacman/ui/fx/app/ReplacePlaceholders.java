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

/**
 * @author Armin Reichert
 *
 */
public class ReplacePlaceholders {

	private static final char PLACEHOLDER_START = '{';
	private static final char PLACEHOLDER_END = '}';

	static String format(String text, Object... args) {
		if (text == null || args == null) {
			return text;
		}

		// start and end index of placeholders in text
		int[][] placeholders = new int[text.length() / 2][2];
		int count = 0;
		int begin = 0;
		boolean started = false;
		int error = -1;
		char[] chars = text.toCharArray();
		for (int i = 0; i < chars.length; ++i) {
			if (error != -1) {
				break;
			}
			if (chars[i] == PLACEHOLDER_START) {
				if (started) {
					error = i;
				} else {
					begin = i;
					started = true;
				}
			} else if (chars[i] == PLACEHOLDER_END) {
				if (started) {
					placeholders[count][0] = begin;
					placeholders[count][1] = i;
					++count;
					started = false;
				} else {
					error = i;
				}
			}
		}

		if (error != -1) {
			return "Error in format string '" + text + "' at index " + error + ", unexpected: " + chars[error];
		}

		if (args.length == 0 || count == 0) {
			return text;
		}

		var s = new StringBuilder();
		int phi = 0; // placeholder index
		int i = 0;
		while (i < chars.length) {
			if (phi < count && i == placeholders[phi][0] && phi < args.length) {
				s.append(args[phi]);
				i = placeholders[phi][1] + 1; // position after '}'
				++phi;
			} else {
				s.append(chars[i++]);
			}
		}
		return s.toString();
	}

	private static void debug(String text, int[][] braces, int bracesCount) {
		System.out.println("Text: " + text);
		System.out.println(bracesCount + " placeholders found:");
		for (int i = 0; i < bracesCount; ++i) {
			int begin = braces[i][0];
			int end = braces[i][1];
			var content = text.substring(begin + 1, end);
			System.out.print("{" + content + "} ");
		}
		System.out.println();
	}

	public static void main(String[] args) {
		System.out.println(format("{}}{}"));
		System.out.println(format("}}{"));
		System.out.println(format("{{"));
		System.out.println(format("{}}"));
		System.out.println(format(null));
		System.out.println(format(""));
		System.out.println(format("", (Object[]) null));
		System.out.println(format("no-args"));
		System.out.println(format("aaa{0.00}bbb{...}ccc{}{0}dd"));
		System.out.println(format("aaa{0.00}bbb{...}ccc{}{0}dd", 1));
		System.out.println(format("aaa{0.00}bbb{...}ccc{}{0}dd", 1, 2));
		System.out.println(format("aaa{0.00}bbb{...}ccc{}{0}dd", 1, 2, 3));
		System.out.println(format("aaa{0.00}bbb{...}ccc{}{0}dd", 1, 2, 3, 4));
		System.out.println(format("aaa{0.00}bbb{...}ccc{}{0}dd", 1, 2, 3, 4, 5));
	}
}