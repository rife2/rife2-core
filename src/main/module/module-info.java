module rife.core {
    requires java.desktop;
    requires java.logging;
    requires java.prefs;
    requires static java.sql;
    requires java.xml;

    exports rife;
    exports rife.cmf;
    exports rife.cmf.transform;
    exports rife.config;
    exports rife.config.exceptions;
    exports rife.database;
    exports rife.database.exceptions;
    exports rife.database.queries;
    exports rife.database.querymanagers.generic;
    exports rife.database.querymanagers.generic.exceptions;
    exports rife.database.types;
    exports rife.datastructures;
    exports rife.engine;
    exports rife.forms;
    exports rife.ioc;
    exports rife.ioc.exceptions;
    exports rife.resources;
    exports rife.resources.exceptions;
    exports rife.selector;
    exports rife.template;
    exports rife.template.exceptions;
    exports rife.tools;
    exports rife.tools.exceptions;
    exports rife.validation;
    exports rife.validation.annotations;
    exports rife.validation.exceptions;
    exports rife.xml;
    exports rife.xml.exceptions;
}