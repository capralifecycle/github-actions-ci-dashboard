# Webhooks and Secrets

Each GitHub organization (a "client") has its own webhook secret and posts to a dedicated
endpoint: `POST /webhook/{clientId}`, where `clientId` is the GitHub organization name
(e.g. `capralifecycle`). Secrets are stored in AWS Secrets Manager and injected via the
infrastructure stack.

When configuring a Webhook in GitHub, you select:

- **Payload URL**: `https://ci-dashboard.liflig.io/webhook/<org-name>`
- **Content type**: `application/json`
- **Secret**: `a long and high entropy random string`
- **Events**: `workflow_run`
- **Active**: `true`

## GitHub and Signing Webhook POST Requests

Every Webhook POST request from GitHub will sign its request Body with the **Secret** and HMAC SHA256,
and send the signature as a HTTP Header `X-Hub-Signature-256`.
The value of the header is `sha256=<lowercase-hex-digest>`, like:

```http request
POST /webhook/<org-name>
X-Hub-Signature-256: sha256=5c5134a624883d7df34eae110bf37f78a0620b159fd884760c40a66a3903293f

{ "requestBody": "goes here" }
```

## Adding a new client

1. Generate a secret: `openssl rand -base64 32`
2. Add the secret to `load-secrets-github-actions-ci-dashboard.ts` in liflig-experiments-infra
   with name `webhook.client.<org-name>.secret`
3. Add the parameter to `ci-dashboard-stack.ts`
4. Run the load-secrets script to provision the secret in AWS Secrets Manager
5. Deploy the dashboard
6. Configure the GitHub webhook URL to `https://ci-dashboard.liflig.io/webhook/<org-name>`

## Rotating a per-client secret

1. Generate a new secret
2. Update it in AWS Secrets Manager (via the load-secrets script)
3. Redeploy the dashboard to pick up the new secret
4. Update the secret in the GitHub organization webhook config
5. Done

## References

- [HMAC signature header (GitHub Docs)](https://docs.github.com/en/webhooks/webhook-events-and-payloads#delivery-headers)
- [Validating webhook deliveries (GitHub Docs)](https://docs.github.com/en/webhooks/using-webhooks/validating-webhook-deliveries)
