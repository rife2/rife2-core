/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife;

import rife.bld.BuildCommand;
import rife.bld.Project;
import rife.bld.extension.Antlr4Operation;
import rife.bld.operations.*;
import rife.bld.publish.*;
import rife.tools.FileUtils;
import rife.tools.StringUtils;

import java.io.File;
import java.util.List;

import static rife.bld.dependencies.Repository.*;
import static rife.bld.dependencies.Scope.*;
import static rife.bld.operations.JavadocOptions.DocLinkOption.NO_MISSING;
import static rife.bld.operations.TemplateType.*;

public class CoreBuild extends Project {
    public CoreBuild()
    throws Exception {
        pkg = "rife";
        name = "rife2-core";

        version = version(FileUtils.readString(new File(srcMainResourcesDirectory(), "CORE_VERSION")));

        javaRelease = 17;
        downloadSources = true;
        autoDownloadPurge = true;

        repositories = List.of(MAVEN_CENTRAL, RIFE2_RELEASES);
        scope(test)
            .include(dependency("org.slf4j", "slf4j-simple", version(2, 0, 7)))
            .include(dependency("org.junit.jupiter", "junit-jupiter", version(5, 9, 3)))
            .include(dependency("org.junit.platform", "junit-platform-console-standalone", version(1, 9, 3)))
            .include(dependency("com.h2database", "h2", version(2, 1, 214)))
            .include(dependency("net.sourceforge.htmlunit", "htmlunit", version(2, 70, 0)))
            .include(dependency("org.postgresql", "postgresql", version(42, 6, 0)))
            .include(dependency("com.mysql", "mysql-connector-j", version(8, 0, 33)))
            .include(dependency("org.mariadb.jdbc", "mariadb-java-client", version(3, 1, 4)))
            .include(dependency("org.hsqldb", "hsqldb", version(2, 7, 1)))
            .include(dependency("org.apache.derby", "derby", version("10.16.1.1")))
            .include(dependency("org.apache.derby", "derbytools", version("10.16.1.1")))
            .include(dependency("com.oracle.database.jdbc", "ojdbc11", version("23.2.0.0")));

        cleanOperation()
            .directories(
                new File(workDirectory(), "embedded_dbs"),
                new File(workDirectory(), "logs"));

        antlr4Operation
            .sourceDirectories(List.of(new File(srcMainDirectory(), "antlr")))
            .outputDirectory(new File(buildDirectory(), "generated/rife/template/antlr"))
            .visitor()
            .longMessages();

        precompileOperation()
            .templateTypes(HTML, XML, SQL);

        compileOperation()
            .mainSourceDirectories(antlr4Operation.outputDirectory())
            .compileOptions()
            .debuggingInfo(JavacOptions.DebuggingInfo.ALL);

        propagateJavaProperties(testOperation().javaOptions(),
            "test.postgres",
            "test.mysql",
            "test.mariadb",
            "test.oracle",
            "test.oracle-free",
            "test.derby",
            "test.hsqldb",
            "test.h2");

        javadocOperation()
            .excluded(
                "rife/antlr/",
                "rife/asm/",
                "rife/.*/databasedrivers/",
                "rife/.*/imagestoredrivers/",
                "rife/.*/rawstoredrivers/",
                "rife/.*/textstoredrivers/",
                "rife/database/capabilities/"
            )
            .javadocOptions()
            .docTitle("<a href=\"https://rife2.com\">RIFE2/core</a> " + version())
            .docLint(NO_MISSING)
            .keywords()
            .splitIndex()
            .tag("apiNote", "a", "API Note:")
            .link("https://jakarta.ee/specifications/servlet/5.0/apidocs/")
            .link("https://jsoup.org/apidocs/")
            .overview(new File(srcMainJavaDirectory(), "overview.html"));

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

    void propagateJavaProperties(JavaOptions options, String... names) {
        for (var name : names) {
            if (properties().contains(name)) {
                options.property(name, properties().getValueString(name));
            }
        }
    }

    final Antlr4Operation antlr4Operation = new Antlr4Operation() {
        @Override
        public void execute()
        throws Exception {
            super.execute();
            // replace the package name so that it becomes part of RIFE2
            FileUtils.transformFiles(outputDirectory(), FileUtils.JAVA_FILE_PATTERN, null, s ->
                StringUtils.replace(s, "org.antlr.v4.runtime", "rife.antlr.v4.runtime"));
        }
    };

    @BuildCommand(summary = "Generates the grammar Java sources")
    public void generateGrammar()
    throws Exception {
        antlr4Operation.executeOnce();
    }

    public void compile()
    throws Exception {
        generateGrammar();
        super.compile();
    }

    public static void main(String[] args)
    throws Exception {
        new CoreBuild().start(args);
    }
}