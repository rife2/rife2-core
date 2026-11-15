/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools;

import java.nio.charset.StandardCharsets;

public class ObfuscationPrinter {
    public static void main(String[] arguments)
    throws Exception {
        // values are percent-encoded, since arguments are decoded with the
        // locale of the system, not with the default charset
        var value = StringUtils.decodeUrl(arguments[1]);
        var result = switch (arguments[0]) {
            case "obfuscate" -> StringEncryptor.obfuscate(value);
            case "deobfuscate" -> StringEncryptor.deobfuscate(value);
            case "roundtrip" -> StringEncryptor.deobfuscate(StringEncryptor.obfuscate(value));
            case "matches" -> String.valueOf(StringEncryptor.matches(value, arguments[2]));
            default -> throw new IllegalArgumentException(arguments[0]);
        };
        var bytes = result.getBytes(StandardCharsets.UTF_8);
        System.out.write(bytes, 0, bytes.length);
        System.out.flush();
    }
}
