#!/bin/bash

# Name of the status file to create
STATUS_FILE="VariantPipeline_status.txt"

# Define the default stage to assume these BAMs are completed up to
DEFAULT_STAGE="BAM|Samtools"


# Empty or create the status file
> "$STATUS_FILE"

# Loop through all .bam files in the current directory
for bam in *.bam; do
    # Extract the sample name (removing .bam extension)
    sample="${bam%.bam}"

    # Add entry to the status file
    echo "${sample}|${DEFAULT_STAGE}" >> "$STATUS_FILE"
done

echo "Initialized $STATUS_FILE with all .bam files at stage '$DEFAULT_STAGE'."

