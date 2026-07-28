#!/bin/bash
set -euo pipefail

# Usage: ./extract_ann_snpeff.sh input.vcf output.tsv
INPUT_VCF="$1"
OUTPUT_TSV="$2"

if [[ -z "${INPUT_VCF:-}" || -z "${OUTPUT_TSV:-}" ]]; then
  echo "Usage: $0 input.vcf output.tsv"
  exit 1
fi

# Check file exists
if [[ ! -r "$INPUT_VCF" ]]; then
  echo "Cannot read input file: $INPUT_VCF" >&2
  exit 1
fi

# 🧬 Extract sample name (column after FORMAT in the #CHROM header line)
SAMPLE_NAME=$(grep -m1 '^#CHROM' "$INPUT_VCF" | awk '{print $10}')
if [[ -z "${SAMPLE_NAME:-}" ]]; then
  echo "⚠️  Could not detect sample name (no FORMAT/sample column found)"
  SAMPLE_NAME="."
fi

# Get the ANN header line (if any)
ANN_LINE=$(grep -m1 '^##INFO=<ID=ANN' "$INPUT_VCF" || true)
if [[ -z "$ANN_LINE" ]]; then
  echo "No ANN header found in $INPUT_VCF" >&2
  exit 1
fi

# Extract the description text inside Description="...".
DESC=$(echo "$ANN_LINE" | sed -n 's/.*Description="\([^"]*\)".*/\1/p')

# Try to get the list after "Format:" (preferred), otherwise after the first ':'
if echo "$DESC" | grep -q 'Format:'; then
  FIELDS_PART=$(echo "$DESC" | sed -E 's/.*Format: *//')
else
  FIELDS_PART=$(echo "$DESC" | sed -E 's/.*: *//')
fi

# Normalize separators and whitespace, then split into array
FIELDS_PART=$(echo "$FIELDS_PART" | sed "s/^'//; s/'$//; s/ *| */|/g")

IFS='|' read -r -a ANN_NAMES <<< "$FIELDS_PART"
N_FIELDS=${#ANN_NAMES[@]}

# 🧾 Write header (add Sample column at the end)
printf 'CHROM\tPOS\tREF\tALT' > "$OUTPUT_TSV"
for name in "${ANN_NAMES[@]}"; do
  name=$(echo "$name" | sed -E 's/[ \/]+/_/g; s/[^A-Za-z0-9_.-]/_/g; s/^_+|_+$//g')
  printf '\t%s' "$name" >> "$OUTPUT_TSV"
done
#printf '\tSample\n' >> "$OUTPUT_TSV"
printf '\tZygosity\tSample\n' >> "$OUTPUT_TSV"

# 🧩 Extract and write data rows
awk -v nfields="$N_FIELDS" -v sample="$SAMPLE_NAME" 'BEGIN{OFS="\t"}
  /^#/ { next }
  {
    info=$8

    # --- extract GT position from FORMAT ---
    nfmt = split($9, fmt, ":")
    gt_idx = 0
    for (k=1; k<=nfmt; k++) {
      if (fmt[k] == "GT") {
        gt_idx = k
        break
      }
    }

    # --- extract GT value from sample ---
    zyg = "."
    if (gt_idx > 0) {
      ns = split($10, samp, ":")
      gt = samp[gt_idx]

      if (gt ~ /^[0-9]+\/[0-9]+$/) {
        split(gt, g, "/")
        if (g[1] == g[2])
          zyg = "Hom"
        else
          zyg = "Het"
      }
    }

    # --- process ANN field ---
    if (match(info, /ANN=[^;]*/)) {
      ann_block = substr(info, RSTART+4, RLENGTH-4)
      n = split(ann_block, anns, ",")
      for(i=1;i<=n;i++){
        split(anns[i], f, "|")
        printf("%s\t%s\t%s\t%s", $1, $2, $4, $5)
        for(j=1;j<=nfields;j++){
          v = (j in f && f[j] != "") ? f[j] : "."
          printf("\t%s", v)
        }
        printf("\t%s\t%s\n",  zyg, sample)
      }
    }
  }' "$INPUT_VCF" >> "$OUTPUT_TSV"

echo "Output written to: $OUTPUT_TSV"


