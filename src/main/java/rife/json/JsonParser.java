/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.json;

/**
 * Parses JSON documents according to RFC 8259, this is an internal class.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.10
 */
class JsonParser {
    private static final int MAX_DEPTH = 512;

    private final String input_;
    private int position_ = 0;
    private int line_ = 1;
    private int column_ = 1;
    private int depth_ = 0;

    JsonParser(String input) {
        input_ = input;
    }

    Object parse() {
        // a single leading byte order mark is tolerated for interoperability
        if (currentIs('\uFEFF')) {
            advance();
        }
        skipWhitespace();
        var value = parseValue();
        skipWhitespace();
        if (position_ < input_.length()) {
            throw error("Unexpected content after JSON value");
        }
        return value;
    }

    private Object parseValue() {
        if (position_ >= input_.length()) {
            throw error("Unexpected end of input");
        }

        var c = input_.charAt(position_);
        return switch (c) {
            case '{' -> parseObject();
            case '[' -> parseArray();
            case '"' -> parseString();
            case 't', 'f', 'n' -> parseLiteral();
            case '-', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> parseNumber();
            default -> throw error("Unexpected character '" + c + "'");
        };
    }

    private JsonObject parseObject() {
        enterNesting();
        advance(); // {
        var object = new JsonObject();

        skipWhitespace();
        if (currentIs('}')) {
            advance();
            depth_ -= 1;
            return object;
        }

        while (true) {
            skipWhitespace();
            if (!currentIs('"')) {
                throw error("Expected a string as object member name");
            }
            var name = parseString();

            skipWhitespace();
            if (!currentIs(':')) {
                throw error("Expected ':' after object member name");
            }
            advance();

            skipWhitespace();
            object.put(name, parseValue());

            skipWhitespace();
            if (currentIs(',')) {
                advance();
            } else if (currentIs('}')) {
                advance();
                depth_ -= 1;
                return object;
            } else {
                throw error("Expected ',' or '}' in object");
            }
        }
    }

    private JsonArray parseArray() {
        enterNesting();
        advance(); // [
        var array = new JsonArray();

        skipWhitespace();
        if (currentIs(']')) {
            advance();
            depth_ -= 1;
            return array;
        }

        while (true) {
            skipWhitespace();
            array.add(parseValue());

            skipWhitespace();
            if (currentIs(',')) {
                advance();
            } else if (currentIs(']')) {
                advance();
                depth_ -= 1;
                return array;
            } else {
                throw error("Expected ',' or ']' in array");
            }
        }
    }

    private String parseString() {
        advance(); // "
        StringBuilder string = null;
        var segment_start = position_;

        while (true) {
            if (position_ >= input_.length()) {
                column_ += position_ - segment_start;
                throw error("Unterminated string");
            }

            var c = input_.charAt(position_);
            if (c == '"') {
                // strings can't contain raw newlines, the column advances
                // by the number of characters that were scanned in bulk
                column_ += position_ - segment_start + 1;
                String result;
                if (string == null) {
                    result = input_.substring(segment_start, position_);
                } else {
                    result = string.append(input_, segment_start, position_).toString();
                }
                position_ += 1;
                return result;
            }
            if (c == '\\') {
                column_ += position_ - segment_start;
                if (string == null) {
                    string = new StringBuilder();
                }
                string.append(input_, segment_start, position_);
                advance();
                if (position_ >= input_.length()) {
                    throw error("Unterminated escape sequence");
                }
                var escaped = input_.charAt(position_);
                switch (escaped) {
                    case '"' -> string.append('"');
                    case '\\' -> string.append('\\');
                    case '/' -> string.append('/');
                    case 'b' -> string.append('\b');
                    case 'f' -> string.append('\f');
                    case 'n' -> string.append('\n');
                    case 'r' -> string.append('\r');
                    case 't' -> string.append('\t');
                    case 'u' -> {
                        if (position_ + 4 >= input_.length()) {
                            throw error("Unterminated unicode escape sequence");
                        }
                        var code = 0;
                        for (var i = 1; i <= 4; ++i) {
                            var hex = input_.charAt(position_ + i);
                            var digit = hexDigit(hex);
                            if (digit < 0) {
                                throw error("Invalid unicode escape sequence '\\u" + input_.substring(position_ + 1, position_ + 5) + "'");
                            }
                            code = code * 16 + digit;
                        }
                        string.append((char) code);
                        position_ += 4;
                        column_ += 4;
                    }
                    default -> throw error("Invalid escape sequence '\\" + escaped + "'");
                }
                advance();
                segment_start = position_;
            } else if (c < 0x20) {
                column_ += position_ - segment_start;
                throw error("Unescaped control character in string");
            } else {
                // plain characters are scanned in bulk, the column is
                // updated when the segment ends
                position_ += 1;
            }
        }
    }

    private Object parseNumber() {
        var start = start();

        if (currentIs('-')) {
            advance();
        }

        // integer part, leading zeros are not allowed
        if (currentIs('0')) {
            advance();
        } else if (currentIsDigit()) {
            skipDigits();
        } else {
            throw error("Invalid number");
        }

        var integral = true;

        if (currentIs('.')) {
            integral = false;
            advance();
            if (!currentIsDigit()) {
                throw error("Expected a digit after the decimal point");
            }
            skipDigits();
        }

        if (currentIs('e') || currentIs('E')) {
            integral = false;
            advance();
            if (currentIs('+') || currentIs('-')) {
                advance();
            }
            if (!currentIsDigit()) {
                throw error("Expected a digit in the exponent");
            }
            skipDigits();
        }

        var number = input_.substring(start, position_);
        if (integral) {
            try {
                return Long.parseLong(number);
            } catch (NumberFormatException e) {
                // fall through to double for numbers that don't fit a long
            }
        }
        var result = Double.parseDouble(number);
        if (Double.isInfinite(result)) {
            throw error("Number is out of range");
        }
        return result;
    }

    private Object parseLiteral() {
        if (input_.startsWith("true", position_)) {
            skip(4);
            return Boolean.TRUE;
        }
        if (input_.startsWith("false", position_)) {
            skip(5);
            return Boolean.FALSE;
        }
        if (input_.startsWith("null", position_)) {
            skip(4);
            return null;
        }
        throw error("Unexpected character '" + input_.charAt(position_) + "'");
    }

    private void enterNesting() {
        depth_ += 1;
        if (depth_ > MAX_DEPTH) {
            throw error("Maximum nesting depth of " + MAX_DEPTH + " exceeded");
        }
    }

    private void skipWhitespace() {
        while (position_ < input_.length()) {
            var c = input_.charAt(position_);
            if (c == '\n') {
                line_ += 1;
                column_ = 1;
                position_ += 1;
            } else if (c == ' ' || c == '\t' || c == '\r') {
                column_ += 1;
                position_ += 1;
            } else {
                break;
            }
        }
    }

    private void skipDigits() {
        while (position_ < input_.length()) {
            var c = input_.charAt(position_);
            if (c < '0' || c > '9') {
                break;
            }
            column_ += 1;
            position_ += 1;
        }
    }

    private boolean currentIs(char c) {
        return position_ < input_.length() && input_.charAt(position_) == c;
    }

    private static int hexDigit(char c) {
        if (c >= '0' && c <= '9') return c - '0';
        if (c >= 'a' && c <= 'f') return c - 'a' + 10;
        if (c >= 'A' && c <= 'F') return c - 'A' + 10;
        return -1;
    }

    private boolean currentIsDigit() {
        if (position_ >= input_.length()) {
            return false;
        }
        var c = input_.charAt(position_);
        return c >= '0' && c <= '9';
    }

    private int start() {
        return position_;
    }

    private void advance() {
        if (position_ < input_.length() && input_.charAt(position_) == '\n') {
            line_ += 1;
            column_ = 1;
        } else {
            column_ += 1;
        }
        position_ += 1;
    }

    private void skip(int count) {
        for (var i = 0; i < count; ++i) {
            advance();
        }
    }

    private JsonParseException error(String message) {
        return new JsonParseException(message, line_, column_);
    }
}
