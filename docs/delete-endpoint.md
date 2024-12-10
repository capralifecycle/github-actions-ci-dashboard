# Delete Endpoint

Sometimes you get unwanted CI Statuses:
1. A repo you no longer use/need.
2. Changes to a workflow ID causing duplicates.

There is an endpoint at:

```http request
DELETE localhost/admin/delete?secret=do-harm&id=123-master
Authorization: Bearer very-very-secret-admin-token
```

To get the ID (e.g. `123-master`), use the [developer endpoint](./Xbar-plugin.md) and find the top level `id` field in the json object.
