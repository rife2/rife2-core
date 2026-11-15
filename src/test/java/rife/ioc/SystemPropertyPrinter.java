/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.ioc;

import java.nio.charset.StandardCharsets;

public class SystemPropertyPrinter {
    public static void main(String[] arguments) {
        var value = HierarchicalProperties.createSystemInstance().get(arguments[0]).getValueString();
        System.out.write(value.getBytes(StandardCharsets.UTF_8), 0, value.getBytes(StandardCharsets.UTF_8).length);
        System.out.flush();
    }
}
