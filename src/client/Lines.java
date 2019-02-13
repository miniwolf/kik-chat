package client;

import java.util.regex.Pattern;

public class Lines {
    static Pattern reSplitIntoLines = Pattern.compile(" *\r?\n *");

    public String[] value;

    public Lines(String[] lines) {
        value = lines;
    }

    public Lines(String text) {
        this(reSplitIntoLines.split(text.trim()));
    }

    @Override
    public String toString() {
        return String.join("\r\n", value) + "\r\n";
    }

    public String get(String headerName) throws KeyNotFoundException {
        var prefix = (headerName + ":").toUpperCase();
        for (int i = 1; i < value.length; i++) {
            var line = value[i];
            if (line.toUpperCase().startsWith(prefix))
                return line.substring(prefix.length());
        }

        throw new KeyNotFoundException("Header '" + headerName + "' not found");
    }

    protected class KeyNotFoundException extends Throwable {
        public KeyNotFoundException(String message) {
            super(message);
        }
    }
}
