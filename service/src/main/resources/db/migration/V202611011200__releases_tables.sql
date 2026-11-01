-- releases: a JDK version and the JEPs it shipped; natural keys, the feature number and the JEP number
CREATE TABLE jdk_version (
    feature_number INTEGER NOT NULL,
    CONSTRAINT pk_jdk_version PRIMARY KEY (feature_number),
    CONSTRAINT ck_jdk_version_positive CHECK (feature_number >= 1)
);

CREATE TABLE jep (
    jep_number INTEGER NOT NULL,
    title TEXT NOT NULL,
    feature_number INTEGER NOT NULL,
    CONSTRAINT pk_jep PRIMARY KEY (jep_number),
    CONSTRAINT ck_jep_number_positive CHECK (jep_number >= 1),
    CONSTRAINT ck_jep_title_present CHECK (length(title) > 0),
    CONSTRAINT fk_jep_jdk_version FOREIGN KEY (feature_number) REFERENCES jdk_version (feature_number)
);

CREATE INDEX ix_jep_feature_number ON jep (feature_number);
