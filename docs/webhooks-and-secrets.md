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

## Rotating secret
Note: This process will result in approximately 10 minutes of downtime for github-actions-ci-dashboards.

### When to Use
If the GitHub signing secret is compromised, it is necessary to rotate the secret. All GitHub organizations and repositories using this secret must be updated simultaneously, as this project does not support staggered secret updates (expand and contract).
Generate a New Secret: Use a reliable password manager or tool to generate a 32-character random password.

1. Update in 1Password: Save the new secret in 1Password under liflig-team-infra/GitHub Actions CI Dashboard Webhook Secret.
2. Notify the Team: Inform #dev-utvikling at least 30 minutes before rotating the secret. Use a message template like:
   1. "The GitHub Actions CI Dashboard Webhook Secret will be rotated at [time]. Downtime is expected for approximately 10 minutes. The new secret is available in 1Password under liflig-team-infra/GitHub Actions CI Dashboard Webhook Secret."
3. Update the Secret in Code: Run the load-secrets-script in the liflig-experiments repository to update the secret.
4. Redeploy: Force a new deployment of liflig-ci-dashboards.
5. Validate and Notify: Confirm that GitHub Actions are reporting correctly. Notify #dev-utvikling that the rotation was successful.


## References

- [HMAC signature header (GitHub Docs)](https://docs.github.com/en/webhooks/webhook-events-and-payloads#delivery-headers)
- [Validating webhook deliveries (GitHub Docs)](https://docs.github.com/en/webhooks/using-webhooks/validating-webhook-deliveries)


