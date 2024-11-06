# Admin config

The API `/admin/config` should be used to upload dashboard configurations.
One config object per dashboard/TV. Use the `id` in the object to correlate to an id in the dashboard url.

Example request:

```http request
POST http://localhost:8080/admin/config
Content-Type: application/json
Authorization: Bearer very-very-secret-admin-token

[{
  "id": "a3",
  "orgMatchers": [
    {
      "matcher": ".*",
      "repoMatchers": [
        {
          "matcher": "my-team-.*",
          "branchMatchers": [
            {
              "matcher": ".*"
            }
          ]
        },
        {
          "matcher": "(TEAM|team)-.*"
        }
      ]
    }
  ]
}]
```

This makes the dashboard for `a3` only show CI builds for repositories starting with `my-team-`, `TEAM-` and `team-`.
The `orgMatchers[].matcher` is used for organization matching, like `"matcher": "capralifecycle"`.

To view this dashboard, use the url `http://localhost:8080/?token=change-me-to-something-secret&dashboardId=a3` on your
TV, where `dashboardId=a3` links it to this config.

Since the POST payload is a list, you can specify multiple configurations at once.

## Automatic Reconfiguration

Keep your configs in a json file in a repo. Then use the GitHub Actions
workflow [update-ci-dashboard.yaml](update-ci-dashboard.yaml) to automatically POST the config.

