set -euo pipefail

versions=(
  "1.21.11-fabric"
  "1.21.11-forge"
  "1.21.1-fabric"
  "1.21.1-forge"
  "1.20.6-fabric"
  "1.20.6-forge"
)

for ver in "${versions[@]}"; do
  ./gradlew "stonecutterSwitchTo$ver"
  ./gradlew -Ppublish.skipConfirmation=true ":$ver:publishMods"
done