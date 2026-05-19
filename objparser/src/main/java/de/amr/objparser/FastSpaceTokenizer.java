package de.amr.objparser;

public final class FastSpaceTokenizer {
    private String s;
    private int len;
    private int pos;
    private int start;
    private int end;

    public void reset(String s) {
        this.s = s;
        this.len = s.length();
        this.pos = 0;
    }

    public boolean next() {
        while (pos < len && Character.isWhitespace(s.charAt(pos))) pos++;
        if (pos >= len) return false;

        start = pos;
        while (pos < len && !Character.isWhitespace(s.charAt(pos))) pos++;
        end = pos;
        return true;
    }

    public String source() {
        return s;
    }

    public int tokenStart() {
        return start;
    }

    public int tokenEnd() {
        return end;
    }

    public String token() {
        return s.substring(start, end);
    }
}
