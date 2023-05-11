/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife;

import rife.tools.FileUtils;

/**
 * Singleton class that provides access to the current core version as a string.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.7
 */
public class CoreVersion {
    private final String version_;

    CoreVersion() {
        version_ = FileUtils.versionFromResource("CORE_VERSION");
    }

    private String getVersionString() {
        return version_;
    }

    public static String getVersion() {
        return CoreVersionSingleton.INSTANCE.getVersionString();
    }
}

