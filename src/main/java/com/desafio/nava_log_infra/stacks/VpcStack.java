package com.desafio.nava_log_infra.stacks;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.services.ec2.Vpc;
import software.constructs.Construct;

public class VpcStack extends Stack {

    private final Vpc vpc;

    public VpcStack(final Construct scope, final String id) {
        super(scope, id);

        this.vpc = Vpc.Builder.create(this, "nava-log-vpc")
                .maxAzs(2)
                .natGateways(1) // Necessário para comunicação privada
                .build();
    }

    public Vpc getVpc() {
        return vpc;
    }

}
