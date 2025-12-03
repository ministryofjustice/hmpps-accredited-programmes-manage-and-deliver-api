# Scripts to use with the HMPPS Kubernetes cluster
This directory contains scripts that interact with the API instances deployed to the HMPPS Kubernetes cluster.

Every script takes one argument, `-ns <env>` ,that defines the namespace that it is to use.  `<env>` must be one of
'dev', 'preprod' or 'prod'

## The scripts and their function:
- `get-token` Acquires a token from the hmpps-auth service for the named environment. Printed to stdout. 
   environment.  The script should be invoked from the directory containing the files
- `get-courses` Invoke the `GET /courses` end-point and output the response to stdout
- `start-db-portforward-pod` Starts a pod in the named environment that forwards the Postgresql port for the API 
   instance to the pod. 
- `setup-servic-pod.bash` starts a service pod in the named namespace with IRSA based authentication, and opens a bash session which then can be used to run authorised commands.

Regarding port forwarding for the database. Once a port forwarding pod has been started for an environment
the pod port can be forwarded on to the user using a kubectl command like.
```
kubectl port-forward db-port-forward-pod --namespace=hmpps-manage-and-deliver-accredited-programmes-dev 5432:5432
```
Change the namespace and local port number (the left-hand one of the pair) as needed.

### Using the Service Pod:

Execute the script for the appropriate k8s namespace: 
```
$ ./setup-service-pod.bash -ns preprod
```
The script will either spin up the service pod, or log you into the existing service pod if it already exists. This can take a few seconds.

The script sets up useful environment variables that are available to the bash session and can be used to invoke aws cli 
commands as if you were authorised with the IRSA such as:

#### Retrieve the instance type of an AWS RDS database:
```
$ aws rds describe-db-instances --db-instance-identifier $RDS_INSTANCE_IDENTIFIER --query 'DBInstances[0].DBInstanceClass' --output text
```

#### List the latest RDS snapshot of an AWS RDS database:
```
$ aws rds describe-db-snapshots --db-instance-identifier $RDS_INSTANCE_IDENTIFIER --query 'DBSnapshots | sort_by(@, &SnapshotCreateTime)[-1]'
```

#### Create a manual snapshot of an AWS RDS database:
```
$ aws rds create-db-snapshot --db-snapshot-identifier manual-snapshot-09-11-2024 --db-instance-identifier $RDS_INSTANCE_IDENTIFIER
```

#### Describe a specific snapshot of an AWS RDS database:
```
$ aws rds describe-db-snapshots --db-snapshot-identifier manual-snapshot-09-11-2024 --db-instance-identifier $RDS_INSTANCE_IDENTIFIER 
```
