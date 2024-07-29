# Xbar Plugin

If you use MacOs and have [Xbar](https://xbarapp.com/) installed (`brew install --cask xbar`),
you can copy the code at the bottom of this file, and run this command in a
terminal: `pbpaste > ~/Library/Application\ Support/xbar/plugins/ci.5m.sh`.
Requires `jq` installed via homebrew to work.

It will add a top-bar widget to your mac, which polls the repos and notifies you if a build has failed. The 5m means it
polls every 5 minutes. You can set this to any duration you want, such as 10s.
![Xbar preview](xbar-preview.png)


**~/Library/Application\ Support/xbar/plugins/ci.5m.sh:**
```shell
#!/bin/bash

# <xbar.title>Liflig CI</xbar.title>
# <xbar.version>1.0</xbar.version>
# <xbar.author>Truls Stenrud</xbar.author>

set -eu -o pipefail
export PATH='/usr/local/bin:/usr/bin:/opt/homebrew/bin:$PATH'

# Change the host and token to match your setup.
#data=$(curl https://ci-dashboard.liflig.io/api/statuses?repo_name=my-prefix-.* -H "Authorization: Bearer change-me-to-something-secret")
data=$(curl https://ci-dashboard.liflig.io/api/statuses -H "Authorization: Bearer change-me-to-something-secret")

if [[ $data =~ "FAILED" ]]; then
  echo "Something failed"
else
  echo "All good"
fi

echo "---"

echo "$data" | jq '.[]' -c | while read -r line; do
  
  name=$(jq '.repo.name' -r <<< $line)
  org=$(jq '.repo.owner' -r <<< $line)
  status=$(jq '.lastStatus' -r <<< $line)

  if [[ "$status" = "SUCCEEDED" ]]; then
    icon=✅
  elif [[ "$status" = "IN_PROGRESS" ]]; then
    icon=🔄
  elif [[ "$status" = "FAILED" ]]; then
    icon=❌
  else
    icon=$status
  fi

  echo "$icon $name | href=https://github.com/$org/$name"
done
```
