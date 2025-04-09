package com.desafio.nava_log_infra.stacks;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.services.dynamodb.*;
import software.constructs.Construct;

public class DynamoDbStack extends Stack {

    private final Table dynamoTable;

    public DynamoDbStack(final Construct scope, final String id) {
        super(scope, id);

        this.dynamoTable = Table.Builder.create(this, "nava-log")
                .tableName("nava-log")
                .partitionKey(Attribute.builder()
                        .name("shipmentId")
                        .type(AttributeType.STRING)
                        .build())
                .sortKey(Attribute.builder()
                        .name("timestamp")
                        .type(AttributeType.STRING)
                        .build())
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .removalPolicy(RemovalPolicy.DESTROY)
                .build();
    }

    public Table getDynamoTable() {
        return dynamoTable;
    }
}
