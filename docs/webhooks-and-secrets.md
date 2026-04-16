# Webhooks and Secrets

When configuring a Webhook in GitHub, you select:

- **Payload URL**: `https://ci-dashboard.liflig.io/webhook`
- **Content type**: `application/json`
- **Secret**: `a long and high entropy random string`
- **Events**: `workflow_run`
- **Active**: `true`

The **Secret** should be kept in e.g. a Password manager (ours is in 1Password
in `liflig-team-infra/GitHub Actions CI Dashboard Webhook Secret`).
The secret should be set via the infrastructure
and [liflig-properties](https://github.com/capralifecycle/liflig-properties) as `webhook.secret` (we did
it [here](https://github.com/capralifecycle/liflig-experiments-infra/blob/2103c2b276fe54f0ce74c55d70e139c486dd16f3/load-secrets-github-actions-ci-dashboard.ts#L5)).
This overwrites the sample value in `application.properties` during runtime.

## GitHub and Signing Webhook POST Requests

Every Webhook POST request from GitHub will sign its request Body with this **Secret** and HMAC SHA256,
and send the signature as a HTTP Header `X-Hub-Signature-256`.
The value of the header is `sha256=<lowercase-hex-digest>`, like:

```http request
POST /webhook
X-Hub-Signature-256: sha256=5c5134a624883d7df34eae110bf37f78a0620b159fd884760c40a66a3903293f

{ "requestBody": "goes here" }
```

## Per-client secrets (recommended)

Instead of a single shared secret, each GitHub organization can have its own webhook secret
via a dedicated endpoint: `POST /webhook/{clientId}`.

The `clientId` is the GitHub organization name (e.g. `capralifecycle`). Each client has its
own secret stored in AWS Secrets Manager.

### Adding a new client

1. Generate a secret: `openssl rand -base64 32`
2. Add the secret to `load-secrets-github-actions-ci-dashboard.ts` in liflig-experiments-infra
   with name `webhook.client.<org-name>.secret`
3. Add the parameter to `ci-dashboard-stack.ts`
4. Run the load-secrets script to provision the secret in AWS Secrets Manager
5. Deploy the dashboard
6. Configure the GitHub webhook URL to `https://ci-dashboard.liflig.io/webhook/<org-name>`

### Rotating a per-client secret

1. Generate a new secret
2. Update it in AWS Secrets Manager (via the load-secrets script)
3. Redeploy the dashboard to pick up the new secret
4. Update the secret in the GitHub organization webhook config
5. Done

## Rotating the legacy shared secret

> **Note:** This applies to the legacy `/webhook` endpoint. Prefer migrating to per-client
> secrets above.

This process will result in approximately 10 minutes of downtime for github-actions-ci-dashboards.

### When to Use
If the GitHub signing secret is compromised, it is necessary to rotate the secret. All GitHub organizations and repositories using this secret must be updated simultaneously, as this project does not support staggered secret updates (expand and contract).
Generate a New Secret: Use a reliable password manager or tool to generate a 32-character random password.

1. Update in 1Password: Save the new secret in 1Password under liflig-team-infra/GitHub Actions CI Dashboard Webhook Secret.
2. Notify the Team: Inform #dev-utvikling at least 30 minutes before rotating the secret. Use a message template like:
   1. "The GitHub Actions CI Dashboard Webhook Secret will be rotated at [time]. Downtime is expected for approximately 10 minutes. The new secret is available in 1Password under liflig-team-infra/GitHub Actions CI Dashboard Webhook Secret."
3. Update the Secret trough CLI: Run the load-secrets-script in the liflig-experiments repository to update the secret.
4. Update the secret in capralifecycle organization (you will need admin access)
5. Redeploy: Force a new deployment of liflig-ci-dashboards.
6. Validate and Notify: Confirm that GitHub Actions are reporting correctly. Notify #dev-utvikling that the rotation was successful.


## References

- [HMAC signature header (GitHub Docs)](https://docs.github.com/en/webhooks/webhook-events-and-payloads#delivery-headers)
- [Validating webhook deliveries (GitHub Docs)](https://docs.github.com/en/webhooks/using-webhooks/validating-webhook-deliveries)


