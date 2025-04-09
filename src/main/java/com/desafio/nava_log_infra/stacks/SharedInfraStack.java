package com.desafio.nava_log_infra.stacks;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.services.ec2.SecurityGroup;
import software.amazon.awscdk.services.ec2.SubnetConfiguration;
import software.amazon.awscdk.services.ec2.SubnetType;
import software.amazon.awscdk.services.ec2.Vpc;
import software.constructs.Construct;

import java.util.List;

public class SharedInfraStack extends Stack {

    private final Vpc vpc;
    private final SecurityGroup rdsSecurityGroup;
    private final SecurityGroup springServiceSecurityGroup;

    public SharedInfraStack(final Construct scope, final String id) {
        super(scope, id);

        this.vpc = Vpc.Builder.create(this, "NavaLogVpc")
                .maxAzs(2)
                .subnetConfiguration(List.of(
                        SubnetConfiguration.builder()
                                .name("public-subnet")
                                .subnetType(SubnetType.PUBLIC)
                                .cidrMask(24)
                                .build(),
                        SubnetConfiguration.builder()
                                .name("private-subnet")
                                .subnetType(SubnetType.PRIVATE_WITH_EGRESS)
                                .cidrMask(24)
                                .build()
                ))
                .build();

        this.rdsSecurityGroup = SecurityGroup.Builder.create(this, "RdsSG")
                .vpc(vpc)
                .allowAllOutbound(true)
                .description("Security Group for RDS")
                .build();

        this.springServiceSecurityGroup = SecurityGroup.Builder.create(this, "SpringServiceSG")
                .vpc(vpc)
                .allowAllOutbound(true)
                .description("Security Group for Spring Boot ECS Service")
                .build();
    }

    public Vpc getVpc() {
        return vpc;
    }

    public SecurityGroup getRdsSecurityGroup() {
        return rdsSecurityGroup;
    }

    public SecurityGroup getSpringServiceSecurityGroup() {
        return springServiceSecurityGroup;
    }
}