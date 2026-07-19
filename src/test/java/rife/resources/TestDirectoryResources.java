/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.resources;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rife.resources.exceptions.ResourceWriterErrorException;
import rife.tools.FileUtils;

import java.io.File;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

public class TestDirectoryResources {
    private File directory_;
    private DirectoryResources resources_;

    @BeforeEach
    void setup()
    throws Exception {
        directory_ = Files.createTempDirectory("directoryresources").toFile();
        resources_ = new DirectoryResources(directory_);
    }

    @AfterEach
    void tearDown()
    throws Exception {
        FileUtils.deleteDirectory(directory_);
    }

    @Test
    void testInstantiation() {
        try {
            new DirectoryResources(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("directory"));
        }

        // a directory that doesn't exist yet is created
        var fresh = new File(directory_, "fresh");
        assertFalse(fresh.exists());
        new DirectoryResources(fresh);
        assertTrue(fresh.isDirectory());
    }

    @Test
    void testAddAndRead()
    throws Exception {
        resources_.addResource("plain.txt", "the content");
        assertEquals("the content", resources_.getContent("plain.txt"));
        assertNotNull(resources_.getResource("plain.txt"));
        assertTrue(resources_.getModificationTime("plain.txt") > 0);

        // a name with separators maps to sub-directories
        resources_.addResource("some/nested/name", "nested content");
        assertEquals("nested content", resources_.getContent("some/nested/name"));
        assertTrue(new File(directory_, "some" + File.separator + "nested" + File.separator + "name").isFile());

        // adding again overwrites
        resources_.addResource("plain.txt", "other content");
        assertEquals("other content", resources_.getContent("plain.txt"));
    }

    @Test
    void testUpdate()
    throws Exception {
        // updating an absent resource reports false
        assertFalse(resources_.updateResource("absent.txt", "content"));

        resources_.addResource("present.txt", "before");
        assertTrue(resources_.updateResource("present.txt", "after"));
        assertEquals("after", resources_.getContent("present.txt"));
    }

    @Test
    void testRemove()
    throws Exception {
        assertFalse(resources_.removeResource("absent.txt"));

        resources_.addResource("present.txt", "content");
        assertTrue(resources_.removeResource("present.txt"));
        assertNull(resources_.getContent("present.txt"));
        assertFalse(resources_.removeResource("present.txt"));
    }

    @Test
    void testPersistence()
    throws Exception {
        resources_.addResource("kept.txt", "persistent content");

        // a fresh instance over the same directory sees the resource
        var other = new DirectoryResources(directory_);
        assertEquals("persistent content", other.getContent("kept.txt"));
    }

    @Test
    void testNameCanNotEscapeDirectory() {
        try {
            resources_.addResource("../escaped.txt", "content");
            fail();
        } catch (ResourceWriterErrorException e) {
            assertTrue(e.getMessage().contains("outside"));
        }
        assertFalse(new File(directory_.getParentFile(), "escaped.txt").exists());
    }
}
