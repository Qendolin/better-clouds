gh issue list \
  --label fixed-in-next-release \
  --state open \
  --limit 20 \
  --json number \
  --jq '.[].number' |
while read -r issue; do
  gh issue close "$issue" --comment "The next release is out on [Modrinth](https://modrinth.com/mod/better-clouds/versions)"
done