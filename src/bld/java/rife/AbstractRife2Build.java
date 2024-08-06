/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife;

import rife.bld.BuildCommand;
import rife.bld.Project;
import rife.bld.extension.Antlr4Operation;
import rife.bld.extension.TestsBadgeOperation;
import rife.bld.operations.JavaOptions;
import rife.tools.FileUtils;
import rife.tools.StringUtils;

import java.io.File;
import java.util.List;

import static rife.bld.dependencies.Repository.MAVEN_CENTRAL;
import static rife.bld.dependencies.Repository.RIFE2_RELEASES;
import static rife.bld.dependencies.Scope.test;
import static rife.bld.operations.JavadocOptions.DocLinkOption.NO_MISSING;

public class AbstractRife2Build extends Project {
    public AbstractRife2Build()
    throws Exception {
        pkg = "rife";

        javaRelease = 17;
        downloadSources = true;
        autoDownloadPurge = true;

        repositories = List.of(MAVEN_CENTRAL, RIFE2_RELEASES);
        scope(test)
            .include(dependency("org.slf4j", "slf4j-simple", version(2,0,13)))
            .include(dependency("org.junit.jupiter", "junit-jupiter", version(5,10,3)))
            .include(dependency("org.junit.platform", "junit-platform-console-standalone", version(1,10,3)))
            .include(dependency("com.h2database", "h2", version(2,3,230)))
            .include(dependency("net.sourceforge.htmlunit", "htmlunit", version(2,70,0)))
            .include(dependency("org.postgresql", "postgresql", version(42,7,3)))
            .include(dependency("com.mysql", "mysql-connector-j", version(9,0,0)))
            .include(dependency("org.mariadb.jdbc", "mariadb-java-client", version(3,4,1)))
            .include(dependency("org.hsqldb", "hsqldb", version(2,7,3)))
            .include(dependency("org.apache.derby", "derby", version("10.16.1.1")))
            .include(dependency("org.apache.derby", "derbytools", version("10.16.1.1")))
            .include(dependency("com.oracle.database.jdbc", "ojdbc11", version("23.4.0.24.05")));

        cleanOperation()
            .directories(
                new File(workDirectory(), "embedded_dbs"),
                new File(workDirectory(), "logs"));

        buildGeneratedDir = new File(buildDirectory(), "generated");
        antlr4Operation
            .outputDirectory(new File(buildGeneratedDir, "rife/template/antlr"))
            .visitor()
            .longMessages();

        propagateJavaProperties(testsBadgeOperation.javaOptions(),
            "test.postgres",
            "test.mysql",
            "test.mariadb",
            "test.oracle",
            "test.oracle-free",
            "test.derby",
            "test.hsqldb",
            "test.h2");

        javadocOperation()
            .javadocOptions()
                .docLint(NO_MISSING)
                .keywords()
                .splitIndex()
                .link("https://jakarta.ee/specifications/servlet/5.0/apidocs/")
                .link("https://jsoup.org/apidocs/");
    }

    final File buildGeneratedDir;

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

    @BuildCommand(value = "generate-grammar", summary = "Generates the grammar Java sources")
    public void generateGrammar()
    throws Exception {
        antlr4Operation.executeOnce();
    }

    public void compile()
    throws Exception {
        generateGrammar();
        super.compile();
    }

    final TestsBadgeOperation testsBadgeOperation = new TestsBadgeOperation();
    public void test()
    throws Exception {
        testsBadgeOperation.executeOnce(() -> testsBadgeOperation
            .url(property("testsBadgeUrl"))
            .apiKey(property("testsBadgeApiKey"))
            .fromProject(this));
    }
}