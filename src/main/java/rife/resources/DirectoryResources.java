/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.resources;

import rife.resources.exceptions.ResourceWriterErrorException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * This class offers {@code ResourceFinder} and {@code ResourceWriter}
 * capabilities for resources that are stored as files in a directory on
 * the file system.
 * <p>
 * Resource names are relative paths inside the directory, a {@code /}
 * separator maps to a sub-directory. The directory is created when it
 * doesn't exist yet and the content is stored as UTF-8.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @see rife.resources.ResourceFinder
 * @see rife.resources.ResourceWriter
 * @since 1.10
 */
public class DirectoryResources extends ResourceFinderDirectories implements ResourceWriter {
    private final File directory_;

    /**
     * Creates a new instance for the provided directory.
     *
     * @param directory the directory that the resources are stored in,
     *                  it is created when it doesn't exist yet
     * @since 1.10
     */
    public DirectoryResources(File directory) {
        super(prepare(directory));

        directory_ = directory;
    }

    private static File prepare(File directory) {
        if (null == directory) throw new IllegalArgumentException("directory can't be null.");

        directory.mkdirs();
        return directory;
    }

    public void addResource(String name, String content)
    throws ResourceWriterErrorException {
        var file = resolve(name);
        try {
            var parent = file.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }
            Files.writeString(file.toPath(), content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ResourceWriterErrorException("Error while adding the resource with the name '" + name + "'.", e);
        }
    }

    public boolean updateResource(String name, String content)
    throws ResourceWriterErrorException {
        var file = resolve(name);
        if (!file.isFile()) {
            return false;
        }
        try {
            Files.writeString(file.toPath(), content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ResourceWriterErrorException("Error while updating the resource with the name '" + name + "'.", e);
        }
        return true;
    }

    public boolean removeResource(String name)
    throws ResourceWriterErrorException {
        var file = resolve(name);
        if (!file.isFile()) {
            return false;
        }
        return file.delete();
    }

    private File resolve(String name)
    throws ResourceWriterErrorException {
        if (null == name) throw new IllegalArgumentException("name can't be null.");
        if (name.isEmpty()) throw new IllegalArgumentException("name can't be empty.");

        var file = new File(directory_, name.replace('/', File.separatorChar));
        try {
            // a resource name can never escape the directory
            if (!file.getCanonicalPath().startsWith(directory_.getCanonicalPath() + File.separator)) {
                throw new ResourceWriterErrorException("The resource name '" + name + "' resolves outside of the directory.", null);
            }
        } catch (IOException e) {
            throw new ResourceWriterErrorException("Error while resolving the resource with the name '" + name + "'.", e);
        }
        return file;
    }
}
