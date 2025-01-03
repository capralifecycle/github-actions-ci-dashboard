# Delete Endpoint

Sometimes you get unwanted CI Statuses:
1. A repo you no longer use/need.
2. Changes to a workflow ID causing duplicates.

There is an endpoint at:

```http request
DELETE http://localhost:8080/admin/delete?secret=do-harm&id=123-master
Authorization: Bearer very-very-secret-admin-token
```

## Example

To get the ID (e.g. `123-master`), use the [developer endpoint](./Xbar-plugin.md) and find the top level `id` field in the json object.
For example:
```shell
curl --silent 'http://localhost:8080/api/statuses?repo_name=my-repo-prefix.*' \
  -H "Authorization: Bearer change-me-to-something-secret" \
  | jq '.[] | {id: .id, name: .repo.name}'
```

then
```shell
curl --silent -X DELETE 'http://localhost:8080/admin/delete?secret=do-harm&id=1234-master' \
  -H 'Authorization: Bearer very-very-secret-admin-token' -w "%{http_code}"
```
