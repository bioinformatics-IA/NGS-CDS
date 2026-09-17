#!/bin/bash
set -euo pipefail
shopt -s extglob
# First Argument Directory else Current directory by default
INPUT_DIR="${1:-.}"
# If $2 missing use *.annh.vcf
PATTERN="${2:-*.annH.vcf}"

OUTPUT_DIR="${INPUT_DIR}/tsv_outputs"

mkdir -p "$OUTPUT_DIR"

#captures every substring that is between two dots
#MERGED_SUFFIX=$(echo "$PATTERN" | grep -oP '(?<=\.)[^.]+(?=\.)')
#Joining with _ 
#MERGED_SUFFIX=$(echo "$PATTERN" | grep -oP '(?<=\.)[^.]+' | paste -sd "_")
# 1. Remove leading * or *_ if present
#CLEAN_PATTERN="${PATTERN#*([*_])}" 
CLEAN_PATTERN=$(echo "$PATTERN" | sed -E 's/^[*_.]+//')
# 2. Strip the trailing .vcf extension
CLEAN_PATTERN="${CLEAN_PATTERN%.vcf}"
# 3. Replace remaining dots with underscores
MERGED_SUFFIX="${CLEAN_PATTERN//./_}"

#MERGED_SUFFIX=$(echo "$PATTERN" | grep -oP '(?<=\.)[^.]+(?=\.)' | paste -sd "_")
MERGED_FILE="${INPUT_DIR}/mergedLong_${MERGED_SUFFIX}_all.tsv"
PIVOT_FILE="${INPUT_DIR}/mergedWide_${MERGED_SUFFIX}_all.tsv"

# Get all .annH.vcf files (including .modified.annH.vcf)
mapfile -t VCF_FILES < <(find "$INPUT_DIR" -maxdepth 1 -type f -name "$PATTERN"  | sort)

if [[ ${#VCF_FILES[@]} -eq 0 ]]; then
  echo "No $PATTERN files found in $INPUT_DIR"
  exit 1
fi


for VCF_FILE in "${VCF_FILES[@]}"; do
# Extract suffix from pattern (remove leading '*')
  SUFFIX="${PATTERN#*}"
  
  BASENAME=$(basename "$VCF_FILE" "$SUFFIX")
 
  OUTPUT_TSV="${OUTPUT_DIR}/${BASENAME}.tsv"

  echo "Processing: $VCF_FILE"
DIR="$(cd "$(dirname "$0")" && pwd)"
  if "$DIR/extract_info_withSample.sh" "$VCF_FILE" "$OUTPUT_TSV"; then
    echo "Done: $OUTPUT_TSV"
  else
    echo "Skipping $VCF_FILE (failed to extract ANN)"
  fi

  echo
done

echo " All possible files processed! Outputs in: $OUTPUT_DIR"
# Create merged file with header only once
awk 'FNR==1 && NR!=1 {next} {print}' "$OUTPUT_DIR"/*.tsv  > "$MERGED_FILE"
echo "Merged TSV(Long Format) created at: $MERGED_FILE"
DIR="$(cd "$(dirname "$0")" && pwd)"
"$DIR/pivot_samples.sh" "$MERGED_FILE" "$PIVOT_FILE"


rm -rf "$OUTPUT_DIR"
echo "Deleted output directory: $OUTPUT_DIR"

