/**
 *
 * Copyright (c) 2006-2019, Speedment, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); You may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.speedment.generator;

import com.speedment.common.codegen.Meta;
import com.speedment.common.codegen.model.File;
import com.speedment.generator.core.GeneratorBundle;
import com.speedment.generator.core.translator.AbstractTranslatorManager;
import com.speedment.runtime.config.*;
import com.speedment.runtime.config.trait.HasNameUtil;
import com.speedment.runtime.core.Speedment;
import com.speedment.runtime.core.component.ProjectComponent;
import com.speedment.runtime.application.AbstractApplicationMetadata;
import com.speedment.runtime.application.internal.DefaultApplicationBuilder;
import org.junit.jupiter.api.BeforeEach;

import java.nio.file.Path;
import java.util.Optional;
import static java.util.stream.Collectors.joining;
import java.util.stream.Stream;

/**
 *
 * @author Per Minborg
 */
public abstract class SimpleModel {

    protected static final String TABLE_NAME = "user";
    protected static final String TABLE_NAME2 = "S_P";
    protected static final String SCHEMA_NAME = "mySchema";
    protected static final String COLUMN_NAME = "first_name";
    protected static final String COLUMN_NAME2 = "item";

    protected Speedment speedment;
    protected Project project;
    protected Dbms dbms;
    protected Schema schema;
    protected Table table;
    protected Column column;
    protected PrimaryKeyColumn pkColumn;
    protected Table table2;
    protected Column column2;
    
    private final static class SilentTranslatorManager extends AbstractTranslatorManager {

        @Override
        public void clearExistingFiles(Project project) {}

        @Override
        public void writeToFile(Project project, Meta<File, String> meta, boolean overwriteExisting) {}

        @Override
        public void writeToFile(Project project, String filename, String content, boolean overwriteExisting) {}

        @Override
        public void writeToFile(Path location, String content, boolean overwriteExisting) {}

        @Override
        public int getFilesCreated() {return 0;}
    }

    @BeforeEach
    public void simpleModelTestSetUp() {

        speedment = new DefaultApplicationBuilder(SimpleMetadata.class)
            .withBundle(GeneratorBundle.class)
            .withComponent(SilentTranslatorManager.class)
            .withSkipCheckDatabaseConnectivity()
            .withSkipValidateRuntimeConfig()
            .build();
        
        project  = speedment.getOrThrow(ProjectComponent.class).getProject();
        dbms     = project.dbmses().findAny().get();
        schema   = dbms.schemas().findAny().get();
        table    = schema.tables().filter(t -> TABLE_NAME.equals(t.getId())).findAny().get();
        column   = table.columns().findAny().get();
        pkColumn = table.primaryKeyColumns().findAny().get();

        table2  = schema.tables().filter(t -> TABLE_NAME2.equals(t.getId())).findAny().get();
        column2 = table2.columns().findAny().get();
    }
    
    private final static class SimpleMetadata extends AbstractApplicationMetadata {

        private String quote(String s) {
            return "\"" + s + "\"";
        }

        private String name(String s) {
            return quote(HasNameUtil.NAME) + " : " + quote(s);
        }

        private String dbTypeName(String dbmsTypeName) {
            return quote(Dbms.TYPE_NAME) + " : " + quote(dbmsTypeName);
        }

        private String columnDatabaseType(String typeName) {
            return quote(Column.DATABASE_TYPE) + " : " + quote(typeName);
        }

        private String array(String name, String... s) {
            return quote(name) + " : [\n" + Stream.of(s)
                .map(line -> line.replace("\n", "\n    "))
                .collect(joining(",\n    ")
            ) + "\n]";
        }

        private String objectWithKey(String name, String... s) {
            return quote(name) + " : " + object(s);
        }

        private String object(String... s) {
            return "{\n" + Stream.of(s)
                .map(line -> line.replace("\n", "\n    "))
                .collect(joining(",\n    ")) + "\n}";
        }
        
        @Override
        public Optional<String> getMetadata() {
            return Optional.of("{"
                + objectWithKey("config",
                    name("myProject"),
                    array(ProjectUtil.DBMSES,
                        object(name("myDbms"),
                            dbTypeName("MySQL"),
                            array(Dbms.SCHEMAS,
                                object(
                                    name(SCHEMA_NAME),
                                    array(Schema.TABLES,
                                        object(
                                            name(TABLE_NAME),
                                            array(Table.COLUMNS,
                                                object(
                                                    name(COLUMN_NAME),
                                                    columnDatabaseType(String.class.getName())
                                                )
                                            ),
                                            array(Table.PRIMARY_KEY_COLUMNS,
                                                object(
                                                    name(COLUMN_NAME)
                                                )
                                            )
                                        ),
                                        object(
                                            name(TABLE_NAME2),
                                            array(Table.COLUMNS,
                                                object(
                                                    name(COLUMN_NAME2),
                                                    columnDatabaseType(String.class.getName())
                                                )
                                            ),
                                            array(Table.PRIMARY_KEY_COLUMNS,
                                                object(
                                                    name(COLUMN_NAME2)
                                                )
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
                + "}"
            );
        }
    }
}