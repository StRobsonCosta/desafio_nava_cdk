package com.desafio.nava_log_infra.stacks;

import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.services.ecr.Repository;
import software.constructs.Construct;

public class EcrStack extends Stack {

    private final Repository repository;

    public EcrStack(final Construct scope, final String id) {
        super(scope, id);

        this.repository = Repository.Builder.create(this, "nava-log-ecr")
                .repositoryName("nava-log-app")
                .removalPolicy(RemovalPolicy.DESTROY)
                .build();
    }

    public Repository getRepository() {
        return repository;
    }
}
