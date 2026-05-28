/*
 * Copyright 2001-2026 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools;

import java.util.Objects;

/**
 * Represents a product that identifies itself in HTTP {@code User-Agent} headers.
 * <p>
 * Each product defines the exact token used in the {@code Product/Version} portion
 * and a canonical URL for the comment section, following RFC 9110 conventions.
 * <p>
 * Examples:
 * <ul>
 * <li>{@code bld/2.3.1 (Java 21.0.1; Linux 6.5.0) (+https://rife2.com/bld)}</li>
 * <li>{@code RIFE2/1.9.1 (Java 26.0.1; Mac OS X 10.16) (+https://rife2.com)}</li>
 * </ul>
 *
 * @author <a href="https://erik.thauvin.net/">Erik Thauvin</a>
 * @since 1.9.4
 */
public enum Product {
    /**
     * The Bld build system.
     * <p>
     * Uses lowercase {@code bld} as the product token in the User-Agent.
     *
     * @see <a href="https://rife2.com/bld">Bld Project Page</a>
     */
    BLD("bld", "https://rife2.com/bld"),

    /**
     * The RIFE2 full-stack web framework.
     * <p>
     * Uses uppercase {@code RIFE2} as the product token in the User-Agent.
     *
     * @see <a href="https://rife2.com">RIFE2 Project Page</a>
     */
    RIFE2("RIFE2", "https://rife2.com");

    private static final String SYSTEM_INFO;

    static {
        var java = System.getProperty("java.version", "unknown");
        var osName = System.getProperty("os.name", "unknown");
        var osVersion = System.getProperty("os.version", "unknown");
        SYSTEM_INFO = String.format("Java %s; %s %s", java, osName, osVersion);
    }

    private final String token_;
    private final String url_;

    /**
     * Constructs a new {@code Product} enum constant.
     *
     * @param token the exact product token for the User-Agent, with required casing
     * @param url   the canonical project URL for the User-Agent comment
     */
    Product(String token, String url) {
        this.token_ = token;
        this.url_ = url;
    }

    /**
     * Returns the exact product token to use in the User-Agent header.
     * <p>
     * This value preserves the intended casing and should be used directly
     * in the {@code Product/Version} portion of the header.
     *
     * @return the product token, e.g. {@code "bld"} or {@code "RIFE2"}
     */
    public String token() {
        return token_;
    }

    /**
     * Returns the canonical project URL for this product.
     * <p>
     * Intended for inclusion in the User-Agent comment section as {@code (+url)}
     * so server operators can identify the client.
     *
     * @return the project URL, never {@code null}
     */
    public String url() {
        return url_;
    }

    /**
     * Builds a complete User-Agent string for this product and the given version.
     * <p>
     * The format follows RFC 9110: {@code Product/Version (system-info) (+url)}
     *
     * @param version the version of the product, non-null, e.g. {@code "2.1.0"}
     * @return a formatted User-Agent string
     * @throws NullPointerException if {@code version} is {@code null}
     */
    public String toUserAgent(String version) {
        Objects.requireNonNull(version, "User-Agent version must not be null");
        return String.format("%s/%s (%s) (+%s)", token_, version, SYSTEM_INFO, url_);
    }
}