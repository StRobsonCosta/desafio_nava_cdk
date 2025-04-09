package com.desafio.nava_log_infra.stacks;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.services.ec2.SecurityGroup;
import software.amazon.awscdk.services.ec2.Vpc;
import software.constructs.Construct;

public class SecurityGroupStack extends Stack {
    private final SecurityGroup rdsSecurityGroup;
    private final SecurityGroup ecsSecurityGroup;
    private final SecurityGroup lbSecurityGroup;

    public SecurityGroupStack(final Construct scope, final String id, Vpc vpc) {
        super(scope, id);

        this.rdsSecurityGroup = SecurityGroup.Builder.create(this, "RdsSG")
                .vpc(vpc)
                .allowAllOutbound(true)
                .build();

        this.ecsSecurityGroup = SecurityGroup.Builder.create(this, "EcsSG")
                .vpc(vpc)
                .allowAllOutbound(true)
                .build();

        this.lbSecurityGroup = SecurityGroup.Builder.create(this, "LbSG")
                .vpc(vpc)
                .allowAllOutbound(true)
                .build();

    }

    public SecurityGroup getRdsSecurityGroup() { return rdsSecurityGroup; }
    public SecurityGroup getEcsSecurityGroup() { return ecsSecurityGroup; }
    public SecurityGroup getLbSecurityGroup() { return lbSecurityGroup; }
}