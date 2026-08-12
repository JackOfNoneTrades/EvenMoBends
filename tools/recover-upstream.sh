#!/usr/bin/env bash
set -euo pipefail

expected_sha256="b1cab36f73e334d02153ec179460e0c8a21add1de90137bd86b32c74485cc3a3"
input_jar="${1:-}"
output_dir="${2:-}"

if [[ -z "$input_jar" || -z "$output_dir" ]]; then
    echo "usage: $0 <official-0.20.1-jar> <new-output-directory>" >&2
    exit 2
fi

if [[ ! -f "$input_jar" ]]; then
    echo "input jar does not exist: $input_jar" >&2
    exit 2
fi

if [[ -e "$output_dir" ]]; then
    echo "output path already exists; refusing to overwrite it: $output_dir" >&2
    exit 2
fi

actual_sha256="$(sha256sum "$input_jar" | awk '{print $1}')"
if [[ "$actual_sha256" != "$expected_sha256" ]]; then
    echo "unexpected input SHA-256: $actual_sha256" >&2
    exit 1
fi

gradle_cache_root="${GRADLE_USER_HOME:-$HOME/.gradle}/caches"
forge_version_dir="$gradle_cache_root/minecraft/net/minecraftforge/forge/1.7.10-10.13.4.1614-1.7.10"
forge_srg_jar="$forge_version_dir/forge-1.7.10-10.13.4.1614-1.7.10-srg.jar"
forge_src_jar="$forge_version_dir/forgeSrc-1.7.10-10.13.4.1614-1.7.10.jar"
srg_mapping="$forge_version_dir/srgs/srg-mcp.srg"
mcp_mapping_dir="$gradle_cache_root/minecraft/de/oceanlabs/mcp/mcp_stable/12"
cfr_jar="${CFR_JAR:-/usr/share/java/cfr/cfr.jar}"

if [[ -n "${SPECIAL_SOURCE_JAR:-}" ]]; then
    special_source_jar="$SPECIAL_SOURCE_JAR"
else
    special_source_jar="$(find "$gradle_cache_root" -path '*/net/md-5/SpecialSource/1.11.0/*SpecialSource-1.11.0-shaded.jar' -print -quit)"
fi

for required_file in "$forge_srg_jar" "$forge_src_jar" "$srg_mapping" \
    "$mcp_mapping_dir/fields.csv" "$mcp_mapping_dir/methods.csv" "$cfr_jar" "$special_source_jar"; do
    if [[ ! -f "$required_file" ]]; then
        echo "required recovery input is missing: $required_file" >&2
        exit 1
    fi
done

work_dir="$(mktemp -d)"
trap 'rm -rf -- "$work_dir"' EXIT

mkdir -p "$output_dir/src/main/java" "$output_dir/src/main/resources"

java -jar "$special_source_jar" \
    -i "$input_jar" "$forge_srg_jar" \
    -o "$work_dir/mobends-mcp.jar" \
    -m "$srg_mapping" \
    --only net/gobbob \
    --stable

java -jar "$cfr_jar" "$work_dir/mobends-mcp.jar" \
    --outputdir "$output_dir/src/main/java" \
    --extraclasspath "$forge_src_jar" \
    --silent true \
    --caseinsensitivefs true

mapping_script="$work_dir/mcp-names.sed"
awk -F, 'NR > 1 { printf "s/\\<%s\\>/%s/g\n", $1, $2 }' \
    "$mcp_mapping_dir/fields.csv" "$mcp_mapping_dir/methods.csv" > "$mapping_script"
find "$output_dir/src/main/java" -name '*.java' -print0 \
    | xargs -0 sed -i -f "$mapping_script"

# Strip only CFR's leading provenance comment; UPSTREAM.md records the tool and version centrally.
find "$output_dir/src/main/java" -name '*.java' -print0 \
    | xargs -0 perl -0pi -e 's{\A/\*.*?\*/\n}{}s'

(
    cd "$output_dir/src/main/resources"
    unzip -q "$input_jar" 'assets/*' 'logo.png' 'mcmod.info'
)

echo "Recovered source and resources into $output_dir"
