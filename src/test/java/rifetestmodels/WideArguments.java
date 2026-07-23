/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rifetestmodels;

public interface WideArguments {
    String combine(long first, String second, double third, String fourth);

    Object associatedBean();
}
