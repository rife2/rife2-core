/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife;

import rife.bld.operations.*;
import rife.bld.publish.*;
import rife.tools.FileUtils;

import java.io.File;
import java.util.List;

import static rife.bld.operations.TemplateType.*;

public class CoreBuild extends AbstractRife2Build {
    public CoreBuild()
    throws Exception {
        name = "rife2-core";

        version = version(FileUtils.readString(new File(srcMainResourcesDirectory(), "CORE_VERSION")));

        antlr4Operation
            .sourceDirectories(List.of(new File(srcMainDirectory(), "antlr")))
            .outputDirectory(new File(buildDirectory(), "generated/rife/template/antlr"));

        precompileOperation()
            .templateTypes(HTML, XML, SQL);

        compileOperation()
            .mainSourceDirectories(antlr4Operation.outputDirectory())
            .compileOptions()
                .debuggingInfo(JavacOptions.DebuggingInfo.ALL)
                .addAll(List.of("-encoding", "UTF-8"));

        javadocOperation()
            .javadocOptions()
                .docTitle("<a href=\"https://rife2.com\">RIFE2/core</a> " + version());

        publishOperation()
            .repository(version.isSnapshot() ? repository("rife2-snapshots") : repository("rife2-releases"))
            .repository(version.isSnapshot() ? repository("sonatype-snapshots") : repository("sonatype-releases"))
            .info(new PublishInfo()
                .groupId("com.uwyn.rife2")
                .artifactId("rife2-core")
                .name("RIFE2/core")
                .description("RIFE2 core library with foundational features.")
                .url("https://github.com/rife2/core")
                .developer(new PublishDeveloper()
                    .id("gbevin")
                    .name("Geert Bevin")
                    .email("gbevin@uwyn.com")
                    .url("https://github.com/gbevin"))
                .license(new PublishLicense()
                    .name("The Apache License, Version 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0.txt"))
                .scm(new PublishScm()
                    .connection("scm:git:https://github.com/rife2/core.git")
                    .developerConnection("scm:git:git@github.com:rife2/core.git")
                    .url("https://github.com/rife2/core"))
                .signKey(property("sign.key"))
                .signPassphrase(property("sign.passphrase")));
    }

    public static void main(String[] args)
    throws Exception {
        new CoreBuild().start(args);
    }
}