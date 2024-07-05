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

## References

- [HMAC signature header (GitHub Docs)](https://docs.github.com/en/webhooks/webhook-events-and-payloads#delivery-headers)
- [Validating webhook deliveries (GitHub Docs)](https://docs.github.com/en/webhooks/using-webhooks/validating-webhook-deliveries)


