#!/bin/sh
#PATHS to Binaries for featureCounts
#---------------------------------------------------------
SUBREAD_PATH="${1}"
cd $SUBREAD_PATH
echo "PATH: $SUBREAD_PATH"
#---------------------------------------------------------

#featureCounts -p -t exon -g gene_id -a annotation.gtf -o 

# Checking Pipeline Status
#---------------------------------------------------------
PIPELINE_STATUS_FILE="${06}RNASeqPipeline_status.txt"
echo "Status Track File: $PIPELINE_STATUS_FILE"

# Create the file if it doesn't exist
if [ ! -f "$PIPELINE_STATUS_FILE" ]; then
# Try to create the file

    if ! touch "$PIPELINE_STATUS_FILE"; then
        echo "Error: Unable to create $PIPELINE_STATUS_FILE. Check directory permissions." 
        exit 1
        
    fi
fi
#---------------------------------------------------------

stage="featureCounts"

# Extract sample name safely from the first file in $8

#first_path=$(echo "$8" | awk '{print $1}')
#filename=$(basename "$first_path")
#sample_name=$(echo "$filename" | cut -d '.' -f1)

#echo "Sample name is $sample_name"


# Check if featureCounts already done for this sample
#if grep -q "^$sample_name|" "$PIPELINE_STATUS_FILE" && grep "^$sample_name|" "$PIPELINE_STATUS_FILE" | grep -q "|$stage"; then
#    echo "$sample_name: $stage already done. Skipping featureCounts..."
#else

# -------------------------------
# Pre-check: are ALL samples already processed?
# -------------------------------
all_done=true

for bam in $8; do
    sample=$(basename "$bam" | cut -d '.' -f1)

    if ! grep -q "^$sample|" "$PIPELINE_STATUS_FILE" || \
       ! grep "^$sample|" "$PIPELINE_STATUS_FILE" | grep -q "|$stage"; then
        all_done=false
        break
    fi
done

if [ "$all_done" = true ]; then
    echo "All samples already have featureCounts. Skipping this stage."
    exit 0
fi


    if [ "${2}" = "SINGLE" ];
    then
        echo "./featureCounts -T ${3} -s ${4} -a "${5}" -o "${6}""${7}.featureCounts" $(echo ${8})"
        ./featureCounts -T ${3} -s ${4} -a "${5}" -o "${6}""${7}.featureCounts" $(echo ${8})
    ###FEATURECOUNT PAIRED READ
    #===========================
    elif [ "${2}" = "PAIR" ];
    then
        echo "./featureCounts -p -T ${3} -s ${4} -a "${5}" -o "${6}""${7}.featureCounts"  $(echo ${8})"	
        ./featureCounts -p -T ${3} -s ${4} -a "${5}" -o "${6}""${7}.featureCounts"  $(echo ${8})
    fi

    # EXIT STATUS CHECK (CRITICAL)
    # -------------------------------
    if [ $? -ne 0 ]; then
        echo "featureCounts failed. Not updating status."
        exit 1
    fi
    echo "featureCounts completed successfully."

    # Update pipeline status
    # (STAR-style: log ALL samples)
    # -------------------------------
    for bam in $8; do
        sample=$(basename "$bam" | cut -d '.' -f1)

        if grep -q "^$sample|" "$PIPELINE_STATUS_FILE"; then
            # Append featureCounts only if not already present
            if ! grep "^$sample|" "$PIPELINE_STATUS_FILE" | grep -q "|$stage"; then
                sed -i "/^$sample|/ s/\$/|$stage/" "$PIPELINE_STATUS_FILE"
            fi
        else
            echo "$sample|$stage" >> "$PIPELINE_STATUS_FILE"
        fi
    done

    # Update pipeline status
    #sed -i "/^$sample_name|/ s/\$/|featureCounts/" "$PIPELINE_STATUS_FILE"

    # Clean up temporary files
    find "${6}" -type f -name "*.tmp" -delete

#fi

###FEATURECOUNT SINGLE READ
#/featureCounts" "-t" "exon" "-g" "gene_id" "--primary" "-p" "-C" "-a" "/mnt/d/Zeeshan/Genomes_indexed/Homo_sapiens.GRCh38.99.gtf" "-o" "/mnt/d/Aqeel/SRR19649955.featureCounts" "/mnt/d/Aqeel/SRR19649955Aligned.sortedByCoord.out.bam" 
#===========================
