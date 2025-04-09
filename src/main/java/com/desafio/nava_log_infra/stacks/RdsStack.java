package com.desafio.nava_log_infra.stacks;

import software.amazon.awscdk.*;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.rds.*;
import software.amazon.awscdk.services.ec2.*;
//import software.amazon.awscdk.services.secretsmanager.Secret;

import software.amazon.awscdk.services.secretsmanager.ISecret;
import software.amazon.awscdk.services.secretsmanager.Secret;
import software.amazon.awscdk.services.secretsmanager.SecretStringGenerator;
import software.constructs.Construct;

import java.util.List;

//public class RdsStack extends Stack {
//
//    private final DatabaseInstance dbInstance;
//    private final ISecret dbSecret;
//
//    public RdsStack(final Construct scope, final String id, Vpc vpc, SecurityGroup rdsSecurityGroup) {
//        super(scope, id);
//
//        this.dbSecret = Secret.Builder.create(this, "NavaLogDbSecret")
//                .generateSecretString(SecretStringGenerator.builder()
//                        .secretStringTemplate("{\"username\": \"postgres\"}")
//                        .generateStringKey("password")
//                        .excludePunctuation(true)
//                        .build())
//                .build();
//
//        this.dbInstance = DatabaseInstance.Builder.create(this, "NavaLogDbInstance")
//                .engine(DatabaseInstanceEngine.postgres(
//                        PostgresInstanceEngineProps.builder()
//                                .version(PostgresEngineVersion.VER_15_3)
//                                .build()))
//                .instanceType(InstanceType.of(InstanceClass.T3, InstanceSize.MICRO))
//                .vpc(vpc)
//                .multiAz(false)
//                .allocatedStorage(20)
//                .storageType(StorageType.GP2)
//                .credentials(Credentials.fromSecret(this.dbSecret))
//                .vpcSubnets(SubnetSelection.builder()
//                        .subnetType(SubnetType.PRIVATE_WITH_EGRESS)
//                        .build())
//                .securityGroups(List.of(rdsSecurityGroup)) // <-- SG vindo da SecurityGroupStack
//                .databaseName("nava_log")
//                .build();
//
//        CfnOutput.Builder.create(this, "RdsEndpoint")
//                .value(getDatabaseEndpoint())
//                .exportName("NavaLogRdsEndpoint")
//                .build();
//
//        CfnOutput.Builder.create(this, "RdsDatabaseName")
//                .value("nava_log")
//                .exportName("NavaLogDbName")
//                .build();
//    }
//
//    public String getDatabaseEndpoint() {
//        return dbInstance.getDbInstanceEndpointAddress();
//    }
//
//    public ISecret getDatabaseSecret() {
//        return dbSecret;
//    }
//}
public class RdsStack extends Stack {

    private final DatabaseInstance dbInstance;
    private final ISecret dbSecret;
    private final SecurityGroup rdsSG;

    public RdsStack(final Construct scope, final String id, Vpc vpc, SecurityGroup rdsSG) {
        super(scope, id);

        this.rdsSG = rdsSG;


        this.dbSecret = Secret.Builder.create(this, "NavaLogDbSecret")
                .generateSecretString(SecretStringGenerator.builder()
                        .secretStringTemplate("{\"username\": \"postgres\"}")
                        .generateStringKey("password")
                        .excludePunctuation(true)
                        .build())
                .build();

        this.dbInstance = DatabaseInstance.Builder.create(this, "NavaLogDbInstance")
                .engine(DatabaseInstanceEngine.postgres(
                        PostgresInstanceEngineProps.builder()
                                .version(PostgresEngineVersion.VER_15_3)
                                .build()))
                .instanceType(InstanceType.of(InstanceClass.T3, InstanceSize.MICRO))
                .vpc(vpc)
                .multiAz(false)
                .allocatedStorage(20)
                .storageType(StorageType.GP2)
                .credentials(Credentials.fromSecret(this.dbSecret))
                .vpcSubnets(SubnetSelection.builder()
                        .subnetType(SubnetType.PRIVATE_WITH_EGRESS)
                        .build())
                .securityGroups(List.of(this.rdsSG))
                .databaseName("nava_log")
                .build();

        CfnOutput.Builder.create(this, "RdsEndpoint")
                .value(getDatabaseEndpoint())
                .exportName("NavaLogRdsEndpoint")
                .build();

        CfnOutput.Builder.create(this, "DbSecretName")
                .value(dbSecret.getSecretArn())
                .exportName("DbSecretName")
                .build();

        CfnOutput.Builder.create(this, "DbSecretPassword")
                .value(dbSecret.getSecretName())
                .exportName("DbSecretPassword")
                .build();

    }

    public String getDatabaseEndpoint() {
        return dbInstance.getDbInstanceEndpointAddress();
    }

    public ISecret getDatabaseSecret() {
        return dbSecret;
    }

    public SecurityGroup getSecurityGroup() {
        return rdsSG;
    }
}
