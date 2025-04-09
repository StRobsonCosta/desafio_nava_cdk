package com.desafio.nava_log_infra;

import com.desafio.nava_log_infra.stacks.*;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.ec2.Peer;
import software.amazon.awscdk.services.ec2.Port;
import software.amazon.awscdk.services.ec2.SecurityGroup;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.secretsmanager.ISecret;

public class App {

	public static void main(String[] args) {
		software.amazon.awscdk.App app = new software.amazon.awscdk.App();

		// Ambiente AWS
		Environment awsEnv = Environment.builder()
				.account(System.getenv("CDK_DEFAULT_ACCOUNT"))
				.region(System.getenv("CDK_DEFAULT_REGION"))
				.build();

		// 1️⃣ VPC
		VpcStack vpcStack = new VpcStack(app, "VpcStack");
		Vpc vpc = vpcStack.getVpc();

		// 2️⃣ Security Groups
		SecurityGroupStack sgStack = new SecurityGroupStack(app, "SecurityGroupStack", vpc);
		SecurityGroup rdsSG = sgStack.getRdsSecurityGroup();
		SecurityGroup ecsSG = sgStack.getEcsSecurityGroup();

		// 3️⃣ RDS (recebe SG da stack de SGs)
		RdsStack rdsStack = new RdsStack(app, "RdsStack", vpc, rdsSG);
		String dbEndpoint = rdsStack.getDatabaseEndpoint();
		ISecret dbSecret = rdsStack.getDatabaseSecret();

		// 4️⃣ DynamoDB
		DynamoDbStack dynamoStack = new DynamoDbStack(app, "DynamoDbStack");

		// 5️⃣ ECR
		EcrStack ecrStack = new EcrStack(app, "EcrStack");

		// 6️⃣ ECS Cluster e Service (recebe SG da stack de SGs)
		EcsClusterStack ecsStack = new EcsClusterStack(
				app,
				"EcsClusterStack",
				vpc,
				dynamoStack.getDynamoTable().getTableName(),
				ecrStack.getRepository(),
				ecsSG // agora passa SG como dependência
		);

		// 7️⃣ Adiciona regra no SG da RDS sem criar dependência inversa
		rdsSG.addIngressRule(
				ecsSG,
				Port.tcp(5432),
				"Permitir acesso do ECS ao RDS"
		);

		app.synth();
	}
}
