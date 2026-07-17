gh issue list \
  --label fixed-in-next-release \
  --state open \
  --limit 5 \
  --json number \
  --jq '.[].number' |
while read -r issue; do
  gh issue close "$issue" --comment "The next release has been published, closing"
done