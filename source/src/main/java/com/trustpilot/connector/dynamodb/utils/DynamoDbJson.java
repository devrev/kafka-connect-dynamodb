package com.trustpilot.connector.dynamodb.utils;
/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;


/**
 * A semantic type for a JSON string.
 *
 * @author Randall Hauch
 */
public class DynamoDbJson {

    public static final String LOGICAL_NAME = "com.trustpilot.connector.dynamodb.json";

    /**
     * Returns a {@link SchemaBuilder} for a JSON field. You can use the resulting SchemaBuilder
     * to set additional schema settings such as required/optional, default value, and documentation.
     *
     * @return the schema builder
     */
    public static SchemaBuilder builder() {
        return SchemaBuilder.string()
                            .name(LOGICAL_NAME)
                            .version(1);
    }

    /**
     * Returns a {@link SchemaBuilder} for a JSON field, with all other default Schema settings.
     *
     * @return the schema
     * @see #builder()
     */
    public static Schema schema() {
        return builder().build();
    }

    public static Schema optionalSchema() {
        return SchemaBuilder.string()
        .name(LOGICAL_NAME+".optional")
        .version(1)
        .optional()
        .build();
    }
}
