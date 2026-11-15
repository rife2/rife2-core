/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.tools;

import rife.datastructures.EnumClass;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static rife.tools.StringUtils.*;

public abstract class StringEncryptor extends EnumClass<String> {
    private static final String IDENTIFIER_HEX_SUFFIX = "HEX";
    private static final String IDENTIFIER_HLO_SUFFIX = "HLO";

    private static final String IDENTIFIER_OBF = "OBF";
    private static final String IDENTIFIER_OBFU = "OBFU";
    private static final String IDENTIFIER_MD5 = "MD5";
    private static final String IDENTIFIER_MD5HEX = IDENTIFIER_MD5 + IDENTIFIER_HEX_SUFFIX;
    private static final String IDENTIFIER_MD5HLO = IDENTIFIER_MD5 + IDENTIFIER_HLO_SUFFIX;
    private static final String IDENTIFIER_SHA = "SHA";
    private static final String IDENTIFIER_SHAHEX = IDENTIFIER_SHA + IDENTIFIER_HEX_SUFFIX;
    private static final String IDENTIFIER_SHAHLO = IDENTIFIER_SHA + IDENTIFIER_HLO_SUFFIX;
    private static final String IDENTIFIER_WHIRLPOOL = "WRP";
    private static final String IDENTIFIER_WHIRLPOOLHEX = IDENTIFIER_WHIRLPOOL + IDENTIFIER_HEX_SUFFIX;
    private static final String IDENTIFIER_WHIRLPOOLHLO = IDENTIFIER_WHIRLPOOL + IDENTIFIER_HLO_SUFFIX;
    private static final String IDENTIFIER_DRUPAL = "$S$";

    private static final String PREFIX_SEPARATOR_SUFFIX = ":";

    public static final StringEncryptor DRUPAL = new StringEncryptor(IDENTIFIER_DRUPAL, "", true) {
        public String performEncryption(String value, String encryptedValue)
        throws NoSuchAlgorithmException {
            var hashed = new DrupalPassword().hashPassword(value, encryptedValue);
            if (hashed == null) {
                return null;
            }
            return hashed.substring(prefix().length());
        }
    };
    public static final StringEncryptor SHA = new StringEncryptor(IDENTIFIER_SHA, PREFIX_SEPARATOR_SUFFIX) {
        public String performEncryption(String value, String encryptedValue)
        throws NoSuchAlgorithmException {
            var digest = MessageDigest.getInstance("SHA");
            digest.update(value.getBytes(StandardCharsets.UTF_8));
            return encodeBase64(digest.digest());
        }
    };
    public static final StringEncryptor SHAHEX = new StringEncryptor(IDENTIFIER_SHAHEX, PREFIX_SEPARATOR_SUFFIX) {
        public String performEncryption(String value, String encryptedValue)
        throws NoSuchAlgorithmException {
            var digest = MessageDigest.getInstance("SHA");
            digest.update(value.getBytes(StandardCharsets.UTF_8));
            return encodeHex(digest.digest());
        }
    };
    public static final StringEncryptor SHAHLO = new StringEncryptor(IDENTIFIER_SHAHLO, PREFIX_SEPARATOR_SUFFIX) {
        public String performEncryption(String value, String encryptedValue)
        throws NoSuchAlgorithmException {
            var digest = MessageDigest.getInstance("SHA");
            digest.update(value.getBytes(StandardCharsets.UTF_8));
            return encodeHexLower(digest.digest());
        }
    };
    public static final StringEncryptor WHIRLPOOL = new StringEncryptor(IDENTIFIER_WHIRLPOOL, PREFIX_SEPARATOR_SUFFIX) {
        public String performEncryption(String value, String encryptedValue) {
            var whirlpool = new Whirlpool();
            whirlpool.NESSIEinit();
            whirlpool.NESSIEadd(value);
            var digest = new byte[Whirlpool.DIGESTBYTES];
            whirlpool.NESSIEfinalize(digest);
            return encodeBase64(digest);
        }
    };
    public static final StringEncryptor WHIRLPOOLHEX = new StringEncryptor(IDENTIFIER_WHIRLPOOLHEX, PREFIX_SEPARATOR_SUFFIX) {
        public String performEncryption(String value, String encryptedValue) {
            var whirlpool = new Whirlpool();
            whirlpool.NESSIEinit();
            whirlpool.NESSIEadd(value);
            var digest = new byte[Whirlpool.DIGESTBYTES];
            whirlpool.NESSIEfinalize(digest);
            return encodeHex(digest);
        }
    };
    public static final StringEncryptor WHIRLPOOLHLO = new StringEncryptor(IDENTIFIER_WHIRLPOOLHLO, PREFIX_SEPARATOR_SUFFIX) {
        public String performEncryption(String value, String encryptedValue) {
            var whirlpool = new Whirlpool();
            whirlpool.NESSIEinit();
            whirlpool.NESSIEadd(value);
            var digest = new byte[Whirlpool.DIGESTBYTES];
            whirlpool.NESSIEfinalize(digest);
            return encodeHexLower(digest);
        }
    };
    public static final StringEncryptor MD5 = new StringEncryptor(IDENTIFIER_MD5, PREFIX_SEPARATOR_SUFFIX) {
        public String performEncryption(String value, String encryptedValue)
        throws NoSuchAlgorithmException {
            var digest = MessageDigest.getInstance("MD5");
            digest.update(value.getBytes(StandardCharsets.UTF_8));
            return encodeBase64(digest.digest());
        }
    };
    public static final StringEncryptor MD5HEX = new StringEncryptor(IDENTIFIER_MD5HEX, PREFIX_SEPARATOR_SUFFIX) {
        public String performEncryption(String value, String encryptedValue)
        throws NoSuchAlgorithmException {
            var digest = MessageDigest.getInstance("MD5");
            digest.update(value.getBytes(StandardCharsets.UTF_8));
            return encodeHex(digest.digest());
        }
    };
    public static final StringEncryptor MD5HLO = new StringEncryptor(IDENTIFIER_MD5HLO, PREFIX_SEPARATOR_SUFFIX) {
        public String performEncryption(String value, String encryptedValue)
        throws NoSuchAlgorithmException {
            var digest = MessageDigest.getInstance("MD5");
            digest.update(value.getBytes(StandardCharsets.UTF_8));
            return encodeHexLower(digest.digest());
        }
    };
    public static final StringEncryptor OBF = new StringEncryptor(IDENTIFIER_OBF, PREFIX_SEPARATOR_SUFFIX) {
        public String encrypt(String value)
        throws NoSuchAlgorithmException {
            var obfuscated = obfuscate(value);
            if (obfuscated.startsWith(OBFU.prefix())) {
                return obfuscated;
            }
            return prefix() + obfuscated;
        }

        public String performEncryption(String value, String encryptedValue) {
            return obfuscateLegacy(value);
        }
    };
    public static final StringEncryptor OBFU = new StringEncryptor(IDENTIFIER_OBFU, PREFIX_SEPARATOR_SUFFIX) {
        public String performEncryption(String value, String encryptedValue) {
            return obfuscateUtf8(value);
        }
    };

    private final String prefix_;
    private final boolean requiresAdaptiveVerification_;

    public StringEncryptor(String identifier, String prefixSeparator) {
        this(identifier, prefixSeparator, false);
    }

    public StringEncryptor(String identifier, String prefixSeparator, boolean requiresAdaptiveVerification) {
        super(StringEncryptor.class, identifier);
        prefix_ = identifier + prefixSeparator;
        requiresAdaptiveVerification_ = requiresAdaptiveVerification;
    }

    public static StringEncryptor getEncryptor(String identifier) {
        return getMember(StringEncryptor.class, identifier);
    }

    public boolean requiresAdaptiveVerification() {
        return requiresAdaptiveVerification_;
    }

    public String prefix() {
        return prefix_;
    }

    public String encrypt(String value)
    throws NoSuchAlgorithmException {
        if (null == value) throw new IllegalArgumentException("value can't be null");

        var encrypted = performEncryption(value, null);
        if (encrypted != null) {
            encrypted = prefix_ + encrypted;
        }
        return encrypted;
    }

    public String encrypt(String value, String encryptedValue)
    throws NoSuchAlgorithmException {
        if (null == value) throw new IllegalArgumentException("value can't be null");

        var encrypted = performEncryption(value, encryptedValue);
        if (encrypted != null) {
            encrypted = prefix_ + encrypted;
        }
        return encrypted;
    }

    public abstract String performEncryption(String value, String encryptedValue)
    throws NoSuchAlgorithmException;

    public static String autoEncrypt(String prefixedValue)
    throws NoSuchAlgorithmException {
        if (null == prefixedValue) throw new IllegalArgumentException("prefixedValue can't be null");

        for (var member : getMembers(StringEncryptor.class)) {
            var encryptor = (StringEncryptor) member;
            if (prefixedValue.startsWith(encryptor.prefix())) {
                return encryptor.encrypt(prefixedValue.substring(encryptor.prefix().length()));
            }
        }

        return prefixedValue;
    }

    public static boolean matches(String checkedValue, String encryptedValue)
    throws NoSuchAlgorithmException {
        if (null == checkedValue) throw new IllegalArgumentException("checkedValue can't be null");
        if (null == encryptedValue) throw new IllegalArgumentException("encryptedValue can't be null");

        return encryptedValue.equals(adaptiveEncrypt(checkedValue, encryptedValue));
    }

    public static String adaptiveEncrypt(String clearValue, String encryptedValue)
    throws NoSuchAlgorithmException {
        if (null == clearValue) throw new IllegalArgumentException("clearValue can't be null");
        if (null == encryptedValue) throw new IllegalArgumentException("encryptedValue can't be null");

        for (var member : getMembers(StringEncryptor.class)) {
            var encryptor = (StringEncryptor) member;
            if (encryptedValue.startsWith(encryptor.prefix())) {
                return encryptor.encrypt(clearValue, encryptedValue);
            }
        }

        return clearValue;
    }

    public static String obfuscate(String value) {
        if (null == value) throw new IllegalArgumentException("value can't be null");

        // the original format keeps its output for every value it can represent
        var legacy = obfuscateLegacy(value);
        if (value.equals(deobfuscateLegacyOrNull(legacy))) {
            return legacy;
        }

        return OBFU.prefix() + obfuscateUtf8(value);
    }

    private static String obfuscateUtf8(String value) {
        var bytes = value.getBytes(StandardCharsets.UTF_8);
        var buffer = new StringBuilder(bytes.length * 4);
        for (var i = 0; i < bytes.length; i++) {
            var b1 = bytes[i] & 0xff;
            var b2 = bytes[bytes.length - (i + 1)] & 0xff;
            appendObfuscated(buffer, (b1 + b2) * 511 + (b1 - b2 + 255));
        }
        return buffer.toString();
    }

    public static String deobfuscate(String value) {
        if (null == value) throw new IllegalArgumentException("value can't be null");

        if (value.startsWith(OBF.prefix())) {
            return deobfuscateLegacy(value.substring(OBF.prefix().length()));
        }
        if (!value.startsWith(OBFU.prefix())) {
            return deobfuscateLegacy(value);
        }

        value = value.substring(OBFU.prefix().length());
        var bytes = new byte[value.length() / 4];
        for (var i = 0; i < bytes.length; i++) {
            var i0 = Integer.parseInt(value, i * 4, i * 4 + 4, 36);
            bytes[i] = (byte) ((i0 / 511 + i0 % 511 - 255) / 2);
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

    // the original format uses the default charset and can't represent
    // every value, it's kept to reproduce what was stored with it
    private static String obfuscateLegacy(String value) {
        var buffer = new StringBuilder();
        var bytes = value.getBytes();
        for (var i = 0; i < bytes.length; i++) {
            var b1 = bytes[i];
            var b2 = bytes[bytes.length - (i + 1)];
            var i1 = (int) b1 + (int) b2 + 127;
            var i2 = (int) b1 - (int) b2 + 127;
            appendObfuscated(buffer, i1 * 256 + i2);
        }
        return buffer.toString();
    }

    private static void appendObfuscated(StringBuilder buffer, int value) {
        var x = Integer.toString(value, 36);
        buffer.append("0".repeat(Math.max(0, 4 - x.length()))).append(x);
    }

    private static String deobfuscateLegacyOrNull(String value) {
        try {
            return deobfuscateLegacy(value);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static String deobfuscateLegacy(String value) {
        var bytes = new byte[value.length() / 2];
        var l = 0;

        for (var i = 0; i < value.length(); i += 4) {
            var x = value.substring(i, i + 4);
            var i0 = Integer.parseInt(x, 36);
            var i1 = (i0 / 256);
            var i2 = (i0 % 256);
            bytes[l++] = (byte) ((i1 + i2 - 254) / 2);
        }

        return new String(bytes, 0, l);
    }

    public static String getHelp(String commandName) {
        if (commandName == null) {
            commandName = "";
        }
        commandName = commandName.trim();
        if (!commandName.isEmpty()) {
            commandName = commandName + " ";
        }

        return StringUtils.replace("""
            Encrypts strings for usage with RIFE2.

            Usage : ${commandName}[-edc] string {encrypted}
              -e  encrypt a string (default)
              -d  decrypt a string if the algorithm support it
              -c  check the validity of the string against an encrypted version""", "${commandName}", commandName);
    }

    public static void main(String[] arguments) {
        var valid_arguments = true;
        if (arguments.length < 1 ||
            arguments.length > 3) {
            valid_arguments = false;
        } else if (!arguments[0].startsWith("-")) {
            if (arguments.length > 1) {
                valid_arguments = false;
            }
        } else {
            if (!arguments[0].equals("-e") &&
                !arguments[0].equals("-d") &&
                !arguments[0].equals("-c")) {
                valid_arguments = false;
            } else if (!arguments[0].equals("-c") &&
                       3 == arguments.length) {
                valid_arguments = false;
            }
        }

        if (!valid_arguments) {
            System.err.println(getHelp("java " + StringEncryptor.class.getName()));
            System.exit(1);
        }
        try {
            if (1 == arguments.length) {
                System.err.println(autoEncrypt(arguments[0]));
                System.exit(0);
            } else if (arguments[0].equals("-e")) {
                System.err.println(autoEncrypt(arguments[1]));
                System.exit(0);
            }
            if (arguments[0].equals("-d")) {
                if (arguments[1].startsWith(OBF.prefix()) || arguments[1].startsWith(OBFU.prefix())) {
                    System.err.println(deobfuscate(arguments[1]));
                } else {
                    System.err.println("ERROR: the algorithm doesn't support decoding.");
                }
                System.exit(0);
            }
            if (arguments[0].equals("-c")) {
                if (matches(arguments[1], arguments[2])) {
                    System.err.println("VALID: the strings match.");
                } else {
                    System.err.println("invalid: the strings don't match.");
                }
                System.exit(0);
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}
