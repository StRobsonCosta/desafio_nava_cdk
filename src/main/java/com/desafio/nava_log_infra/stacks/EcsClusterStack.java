package com.desafio.nava_log_infra.stacks;

import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.Fn;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.services.ec2.SecurityGroup;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ecr.Repository;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.iam.ManagedPolicy;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.amazon.awscdk.services.logs.LogGroup;
import software.amazon.awscdk.services.secretsmanager.ISecret;
import software.amazon.awscdk.services.secretsmanager.SecretAttributes;
import software.constructs.Construct;
import software.amazon.awscdk.services.secretsmanager.Secret;


import java.util.List;
import java.util.Map;

public class EcsClusterStack extends Stack {

    private final SecurityGroup ecsSG;

    public EcsClusterStack(final Construct scope, final String id, Vpc vpc, String tableName, Repository ecrRepository, SecurityGroup ecsSG) {
        super(scope, id);

        this.ecsSG = ecsSG;

        Cluster cluster = Cluster.Builder.create(this, "EcsCluster")
                .clusterName("nava-log-cluster")
                .vpc(vpc)
                .build();

        ContainerImage appImage = ContainerImage.fromEcrRepository(ecrRepository, "latest");

        Role executionRole = Role.Builder.create(this, "EcsExecutionRole")
                .assumedBy(new ServicePrincipal("ecs-tasks.amazonaws.com"))
                .managedPolicies(List.of(
                        ManagedPolicy.fromAwsManagedPolicyName("AmazonEC2ContainerRegistryReadOnly"),
                        ManagedPolicy.fromAwsManagedPolicyName("CloudWatchLogsFullAccess")
                ))
                .build();

        Role taskRole = Role.Builder.create(this, "EcsTaskRole")
                .assumedBy(new ServicePrincipal("ecs-tasks.amazonaws.com"))
                .managedPolicies(List.of(
                        ManagedPolicy.fromAwsManagedPolicyName("AmazonDynamoDBFullAccess")
                ))
                .build();

        FargateTaskDefinition taskDefinition = FargateTaskDefinition.Builder.create(this, "TaskDef")
                .cpu(1024)
                .memoryLimitMiB(2048)
                .executionRole(executionRole)
                .taskRole(taskRole)
                .build();

        ISecret dbSecret = Secret.fromSecretAttributes(this, "ImportedDbSecret",
                SecretAttributes.builder()
                        .secretCompleteArn(Fn.importValue("DbSecretArn"))
                        .build()
        );



        ContainerDefinition springContainer = taskDefinition.addContainer("SpringBootContainer",
                ContainerDefinitionOptions.builder()
                        .image(appImage)
                        .memoryLimitMiB(1024)
                        .cpu(512)
                        .environment(Map.of(
                                "DYNAMODB_TABLE_NAME", tableName,
//                                "DB_URL", "jdbc:postgresql://" + dbEndpoint + ":5432/nava_log"
                                    "DB_PASSWORD", Fn.importValue(dbSecret.toString()),
                                "DB_USERNAME", Fn.importValue("postgres"),
                                "DB_URL", "jdbc:postgresql://" + Fn.importValue("RdsEndpoint") + ":5432/nava_log"
                        ))
//                        .secrets(Map.of(
//                                "DB_PASSWORD", Secret.secretsManager(dbSecret, "password"),
//                                "DB_USERNAME", Secret.secretsManager(dbSecret, "username")
//                        ))
                        .build());

        springContainer.addPortMappings(PortMapping.builder()
                .containerPort(8081)
                .build());

        ContainerDefinition wireMockContainer = taskDefinition.addContainer("nava-log-wiremock",
                ContainerDefinitionOptions.builder()
                        .image(ContainerImage.fromRegistry("wiremock/wiremock:latest"))
                        .memoryLimitMiB(256)
                        .cpu(128)
                        .command(List.of("--verbose"))
                        .build());

        wireMockContainer.addPortMappings(PortMapping.builder()
                .containerPort(9090)
                .build());

        ApplicationLoadBalancedFargateService fargateService =
                ApplicationLoadBalancedFargateService.Builder.create(this, "SpringBootFargateService")
                        .serviceName("nava-log-service")
                        .cluster(cluster)
                        .cpu(512)
                        .memoryLimitMiB(1024)
                        .desiredCount(2)
                        .taskDefinition(taskDefinition)
                        .publicLoadBalancer(true)
                        .build(); // Removido: .securityGroups(...)

        // Associar ECS Security Group à Task (recomendado fora do builder)
        fargateService.getService().getConnections().addSecurityGroup(this.ecsSG);

        // Logs
        LogGroup.Builder.create(this, "EcsLogGroup")
                .logGroupName("SpringBootEcsLogs")
                .removalPolicy(RemovalPolicy.DESTROY)
                .build();

        CfnOutput.Builder.create(this, "LoadBalancerDNS")
                .value(fargateService.getLoadBalancer().getLoadBalancerDnsName())
                .build();
    }

    public SecurityGroup getSecurityGroup() {
        return ecsSG;
    }
}

