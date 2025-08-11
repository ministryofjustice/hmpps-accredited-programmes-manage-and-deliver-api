# Access the development database remotely

## Prerequisites

Install jq (brew install jq)

- [Follow the Cloud Platform guidance to connect to the Kubernetes
  cluster](https://user-guide.cloud-platform.service.justice.gov.uk/documentation/getting-started/kubectl-config.html#connecting-to-the-cloud-platform-39-s-kubernetes-cluster).

## Get database secrets

```bash
kubectl -n hmpps-manage-and-deliver-accredited-programmes-dev get secret rds-postgresql-instance-output -o json | jq '.data | map_values(@base64d)'
```

This will output a list of Base64-decoded secrets for the development database like this:

```
"database_name": "XXXX",
"database_password": "XXXX",
"database_username": "XXXX",
"rds_instance_address": "XXXX",
"rds_instance_endpoint": "XXXX"
```

## Run port-forwarding pod

You'll need to run the port-forwarding pod to point your local environment to the dev database.

To check if the port-forwarding pod already exists, run:

```bash
kubectl -n hmpps-manage-and-deliver-accredited-programmes-dev get pods
```

If there is no pod called `port-forward-pod` then create it with the following:

```bash
kubectl -n hmpps-manage-and-deliver-accredited-programmes-dev run dev-port-forward-pod --image=ministryofjustice/port-forward --env="REMOTE_HOST=<rds_instance_address>" --env="REMOTE_PORT=5432" --env="LOCAL_PORT=5432"
```

where `rds_instance_address` is the `rds_instance_address` value from the secrets above.

### Set up port forwarding

The following command will forward port `5433`, but you can change that to your desired port.

```bash
kubectl -n hmpps-manage-and-deliver-accredited-programmes-dev port-forward dev-port-forward-pod 5433:5432
```

Now you should be able to log into the database using your favourite database client.

## Entering secrets in your database client (e.g. IntelliJ)

- Host: `localhost`
- User: `database_username` secret value
- Password `database_password` secret value
- Port: `5433` (or whichever port you specified above)
