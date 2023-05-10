/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf;

import rife.datastructures.EnumClass;

/**
 * This is a typed enumeration of all the mime types that the content
 * management framework specifically knows about.
 * <p>The types that are defined here can be validated and transformed.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public class MimeType extends EnumClass<String> {
    public static final String APPLICATION_XHTML_IDENTIFIER = "application/xhtml+xml";
    public static final String IMAGE_GIF_IDENTIFIER = "image/gif";
    public static final String IMAGE_JPEG_IDENTIFIER = "image/jpeg";
    public static final String IMAGE_PNG_IDENTIFIER = "image/png";
    public static final String TEXT_PLAIN_IDENTIFIER = "text/plain";
    public static final String TEXT_XML_IDENTIFIER = "text/xml";
    public static final String RAW_IDENTIFIER = "raw";

    /**
     * The {@code application/xhtml+xml} mime type.
     */
    public static final MimeType APPLICATION_XHTML = new MimeType(APPLICATION_XHTML_IDENTIFIER);
    /**
     * The {@code image/gif} mime type.
     */
    public static final MimeType IMAGE_GIF = new MimeType(IMAGE_GIF_IDENTIFIER);
    /**
     * The {@code image/jpeg} mime type.
     */
    public static final MimeType IMAGE_JPEG = new MimeType(IMAGE_JPEG_IDENTIFIER);
    /**
     * The {@code image/png} mime type.
     */
    public static final MimeType IMAGE_PNG = new MimeType(IMAGE_PNG_IDENTIFIER);
    /**
     * The {@code text/plain} mime type.
     */
    public static final MimeType TEXT_PLAIN = new MimeType(TEXT_PLAIN_IDENTIFIER);
    /**
     * The {@code text/plain} mime type.
     */
    public static final MimeType TEXT_XML = new MimeType(TEXT_XML_IDENTIFIER);
    /**
     * A generic mime type indicating that the content should be stored as raw
     * data without any mime-type related processing.
     */
    public static final MimeType RAW = new MimeType(RAW_IDENTIFIER);

    /**
     * Returns the {@code MimeType} instance that corresponds to a given
     * textual identifier.
     *
     * @param identifier the identifier of the mime type that has to be
     *                   retrieved
     * @return the requested {@code MimeType}; or
     * <p>{@code null} if the {@code MimeType} is not supported
     * @since 1.0
     */
    public static MimeType getMimeType(String identifier) {
        return getMember(MimeType.class, identifier);
    }

    MimeType(String identifier) {
        super(MimeType.class, identifier);
    }
}
