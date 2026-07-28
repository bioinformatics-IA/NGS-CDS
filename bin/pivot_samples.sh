#!/usr/bin/env bash
set -euo pipefail

# Usage:
#   ./pivot_samples.sh merged_annH_all.tsv pivoted_by_sample.tsv
# Output: one row per unique annotation (all columns except Sample),
#         plus one column per sample with "1" if that sample contains the row, "." otherwise.

INPUT="${1:-merged_annH_all.tsv}"
OUTPUT="${2:-pivoted_annH.tsv}"

if [[ ! -f "$INPUT" ]]; then
  echo "Error: input file not found: $INPUT" >&2
  exit 1
fi

# determine number of columns in header
ncols=$(awk -F'\t' 'NR==1{print NF; exit}' "$INPUT")
if (( ncols < 2 )); then
  echo "Error: input must have at least 2 columns" >&2
  exit 1
fi
base_cols=$(( ncols - 1 ))   # all columns except last (Sample)

awk -F'\t' -v OFS='\t' -v base_cols="$base_cols" '
NR==1 {
  # store header names (columns 1..base_cols)
  for(i=1;i<=base_cols;i++) hdr[i]=$i
  next
}
{
  # build a stable key from the base columns using SUBSEP
  key = $1
  for(i=2;i<=base_cols;i++) key = key SUBSEP $i

  # sample name is the last column
  sample = $(base_cols+1)

  # record sample order (first appearance)
  if(!(sample in samp_seen)){
    samp_seen[sample]=1
    samples[++ns]=sample
  }

  # mark presence
  present[key SUBSEP sample] = 1

  # record the first encountered base-column string for this key (to print later)
  if(!(key in seen)){
    seen[key]=1
    stored[key] = $1
    for(i=2;i<=base_cols;i++) stored[key] = stored[key] OFS $i
    order[++kcount] = key
  }
}
END {
  # print header: base headers then sample names (in order of appearance)
  for(i=1;i<=base_cols;i++){
    printf("%s", hdr[i])
    if(i<base_cols) printf(OFS)
  }
  for(j=1;j<=ns;j++) printf("%s%s", OFS, samples[j])
  printf("\n")

  # print rows in the order they were first seen
  for(k=1;k<=kcount;k++){
    key = order[k]
    printf("%s", stored[key])
    for(j=1;j<=ns;j++){
      s = samples[j]
      if( (key SUBSEP s) in present ) v = "1"; else v = "."
      printf("%s%s", OFS, v)
    }
    printf("\n")
  }
}
' "$INPUT" > "$OUTPUT"

#echo "Wrote pivoted file: $OUTPUT"
echo "Merged TSV (Wide Format) created at: $OUTPUT"
