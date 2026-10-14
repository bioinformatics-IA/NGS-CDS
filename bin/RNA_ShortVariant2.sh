#!/usr/bin/bash
#set -x 
#==================================================================================================
#                                           PARAMETERS
#==================================================================================================
STAR_PATH="${1}"
PICARD_PATH="${2}"
GATK_PATH="${3}"
snpEff="${4}" #snpEff Path###
VEP_PATH="${5}"
R_LIBS="${6}"

BamFilesPath="${7}" #Array of paths
REFERENCE="${8}"
OUTPUT_DIR="${9}"

KNOWN_SITES="${10}"  #Variants in dbSNP database
addReplaceGroups="${11}" #Array of comma seperated values 
SplitOption="${12}"  #YES,NO
HaploOption="${13}"  #SINGLEMODE,JOINTMODE
HaploInterval="${14}"   #either file, or either mannual interval
Thread="${15}"
# 16-19 for Variant Annotation
annotationOPtion="${16}"	#snpeff,vep,both
 
if [ "$annotationOPtion" = "snpeff" ]; then
snpEff_db="${17}" # hg38 or hg19    GRCh37.75
elif [ "$annotationOPtion" = "vep" ]; then
vepAssembly="${17}"
vepSpecies="${18}"
elif [ "$annotationOPtion" = "both" ]; then
snpEff_db="${17}" # hg38 or hg19    GRCh37.75
vepAssembly="${18}"
vepSpecies="${19}"
fi

snpEff_path="${snpEff}/snpEff.jar"
snpSift="${snpEff}/SnpSift.jar"

# 20- 23 Variant Filteration
variantFilterationOp="${20}" #ALL,BA
selectVariantStr="${21}"
filterOptionsB="${22}" #String of  Filterexpression, filtername
filterOptionsA="${23}" #Sift Filtering (After)

germSomaticOP="${24}" #Option for GERMLINE or SOMATIC or BOTH
somaticOP="${25}"  #Somatic Options: TUMORN, TUMORM, TUMORO, TUMORMIT,TUMORFOR

ANNOT_GNOMAD="${26}"
ANNOT_DBSNP="${27}"
ANNOT_CLINVAR="${28}"

# 24-26 or 24-29 or 24-32--changed: 29-31 or
if [ "$germSomaticOP" = "GERMLINE" ]; then
    ERC="${29}" #ERC*
    G1="${30}"
    bamout="${31}"
    
elif [ "$germSomaticOP" = "SOMATIC" ]; then
    TumorNormalData="${29}" #Array of Tumor#Normal pairs separated by Comma
    germlineResource="${30}"
    PON="${31}"
    allele="${32}"
    f1r2="${33}"
    genomicInterval="${34}"
    
elif [ "$germSomaticOP" = "BOTH" ]; then
    ERC="${29}" #ERC*
    G1="${30}"
    bamout="${31}"
    
    TumorNormalData="${32}" #Array of normal paths & name comma separated
    germlineResource="${33}"
    PON="${34}"
    allele="${35}"
    f1r2="${36}"
    genomicInterval="${37}"
fi


TempOutputDir="/mnt/ntfs/Output_WES/"

GATK_MEM="-Xmx12G"
TMP_DIR="${OUTPUT_DIR}gatk_tmp"
mkdir -p "$TMP_DIR"

#==================================================================================================
#                                   END  PARAMETERS
#==================================================================================================

#==================================================================================================
#                                  HELPER FUNCTIONS
#==================================================================================================
# FUNCTION 01: to extract the active filter from $filterOptionsA
 
get_active_filter() {
    raw_input="$1"
    target_tag="$2" # "SNPEFF" or "VEP"

    # 1. Use Perl to extract everything after [$target_tag] up to the next [SECTION] or end of string
    extracted=$(echo "$raw_input" | perl -0777 -ne '
        if (/\[\s*'$target_tag'\s*\]\s*(.*?)(?=\[\s*[A-Za-z0-9_]+\s*\]|\z)/s) {
            print $1;
        }
    ')

    # 2. Fallback to raw_input if no section tag was matched
    if [ -z "$extracted" ]; then
        extracted="$raw_input"
    fi

    # 3. Convert newlines to spaces
    extracted=$(echo "$extracted" | tr '\n' ' ')

    # 4. Strip leading/trailing whitespaces, closing brackets, single quotes, double quotes, and backticks
    extracted=$(echo "$extracted" | sed -e 's/^[[:space:]]\]*//' -e 's/^[[:space:]"'\''`]*//' -e 's/[[:space:]"'\''`]*$//')

    echo "$extracted"
}

#==================================================================================================
# FUNCTION 02: 
 
append_total_summary() {
    local target_file="$1"
    if [ -f "$target_file" ] && [ $(wc -l < "$target_file") -gt 1 ]; then
        awk -F',' '
        NR > 1 {
            tool = $2
            count[tool]++
            raw[tool] += $4
            filt[tool] += $5
            rem[tool] += $6
            
            tot_count++
            tot_raw += $4
            tot_filt += $5
            tot_rem += $6
        }
        END {
            for (t in count) {
                pct = (raw[t] > 0) ? (filt[t] / raw[t]) * 100 : 0
                printf "TOTAL_%s (%d Samples),%s,SubTotal,%d,%d,%d,%.2f%%\n", t, count[t], t, raw[t], filt[t], rem[t], pct
            }
            
            if (length(count) > 1) {
                tot_pct = (tot_raw > 0) ? (tot_filt / tot_raw) * 100 : 0
                printf "TOTAL_COMBINED (%d Records),ALL,Combined,%d,%d,%d,%.2f%%\n", tot_count, tot_raw, tot_filt, tot_rem, tot_pct
            }
        }' "$target_file" >> "$target_file"
    fi
}
#==================================================================================================
# LOG FILE CREATION FOR VARIANT CALLING STEPS

PIPELINE_STATUS_FILE="${OUTPUT_DIR}/VariantPipeline_status.txt"

# Create the file if it doesn't exist
if [ ! -f "$PIPELINE_STATUS_FILE" ]; then
# Try to create the file
    if ! touch "$PIPELINE_STATUS_FILE"; then
        echo "Error: Unable to create $PIPELINE_STATUS_FILE. Check directory permissions." 
        exit 1
    fi
fi
#==================================================================================================
# FUNCTION 03: PIPELINE STATUS-KEEPS RECORD OF STEPS EXECUTED OR NOT

update_pipeline_status() {
    BAMFILE_NAME="$1"
    stage="$2"
    if grep -q "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE"; then
        # Sample already exists — append the new stage
        sed -i "s/^${BAMFILE_NAME}.*/&|${stage}/" "$PIPELINE_STATUS_FILE"
    else
        # Sample not in file — add a new line
        echo "${BAMFILE_NAME}|${stage}" >> "$PIPELINE_STATUS_FILE"
    fi
}

#==================================================================================================
# CHECKING DICTIONARY FILE FOR REFERENCE. IF NOT PRESENT CREAT IT CreateSequenceDictionary

DICT_FILE="${REFERENCE%.*}.dict"

if [ ! -f "$DICT_FILE" ]; then
cd $GATK_PATH
    echo "Sequence dictionary (.dict) not found. Creating now..."
	#CorrectionDone- Uncomment it later on
    ./gatk CreateSequenceDictionary -R "$REFERENCE"
else
    echo "Sequence dictionary (.dict) already exists."
fi

#==================================================================================================
# Step 1: Check Read Groups present or not (.bam--->add.bam)
#==================================================================================================

echo "Checking ReadGroups..."

for bampath in $(echo $BamFilesPath | sed "s/,/ /g")
do
filename=$(basename "$bampath") 
#BAMFILE_NAME="${filename%.*}"
BAMFILE_NAME=$(echo "$filename" | sed -E 's/(\.star\.Aligned\.sortedByCoord\.out)?\.bam$//')

#Loop to read 5 text fields and delete them
	while [ -n "$addReplaceGroups" ]; do
	    # Read the first 5 values
	    values="$(echo "$addReplaceGroups" | cut -d',' -f1-5)"
	    
	    # Split the values
	    ID=$(echo "$values" | cut -d',' -f1)
	    LB=$(echo "$values" | cut -d',' -f2)
	    PL=$(echo "$values" | cut -d',' -f3)
	    PU=$(echo "$values" | cut -d',' -f4)
	    SM=$(echo "$values" | cut -d',' -f5)
	   
	    # Remove the processed values from groupsData
	    addReplaceGroups=$(echo "$addReplaceGroups" | sed "s/^$values,//")
	    addReplaceGroups=$(echo "$addReplaceGroups" | sed "s/^$values$//")

	    break
	done


stage="ReadGroups"
if grep -q "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" && grep "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" | grep -q "|${stage}"; then
    # If both sample and stage exist in the file, that means this stage is already done for the sample
     echo "$BAMFILE_NAME: $stage already done. Skipping AddoReplaceReadGroup..."

else


 #Check if read group information is present in the input BAM file
 echo "java -jar $PICARD_PATH ViewSam -I "${bampath}" --ALIGNMENT_STATUS All --PF_STATUS All --HEADER_ONLY TRUE"
  if java -jar $PICARD_PATH ViewSam -I "${bampath}" --ALIGNMENT_STATUS All --PF_STATUS All --HEADER_ONLY TRUE | grep -q "^@RG.*ID:.*LB:.*PL:.*PU:.*SM:.*"; then

    printf "Read group information is already present in the input BAM file.\nSkipping the 'AddOrReplaceReadGroups' tool."
     mv "${bampath}" "${OUTPUT_DIR}""${BAMFILE_NAME}.add.bam" #renaming the file
    
   # Add your other steps here
  else
    echo "Read group information is not present in the input BAM file."
    echo "Adding or replacing read groups using Picard..."

		# Add or Replace Read Groups using Picard
echo "java -jar $PICARD_PATH AddOrReplaceReadGroups -I "${bampath}" -O "${OUTPUT_DIR}""${BAMFILE_NAME}.add.bam" --RGID $ID --RGLB $LB --RGPL $PL --RGPU $PU --RGSM $SM"
 
    	java -jar $PICARD_PATH AddOrReplaceReadGroups -I "${bampath}" -O "${OUTPUT_DIR}""${BAMFILE_NAME}.add.bam" --RGID $ID --RGLB $LB --RGPL $PL --RGPU $PU --RGSM $SM
	
	 echo "Read groups added or replaced successfully for $BAMFILE_NAME!"
	 
	
  fi #End of java if
   # Check if the sample already has a line in the status file
			    if grep -q "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE"; then
				# If the sample is already in the file, append the new stage to its existing line
				sed -i "s/^${BAMFILE_NAME}.*/&|${stage}/" "$PIPELINE_STATUS_FILE"
			    else
				# If the sample is not in the file, create a new line with the sample and current stage
				echo "${BAMFILE_NAME}|${stage}" >> "$PIPELINE_STATUS_FILE"
			    fi
  
fi #End FileCheck
done

#==================================================================================================
#	Step 2: MarkDuplicates (.add.bam ----> dedup.bam, .metrices)
#==================================================================================================

echo "Running MarkDuplicates..."
for bampath in $(echo $BamFilesPath | sed "s/,/ /g")
do
filename=$(basename "$bampath") 
BAMFILE_NAME=$(echo "$filename" | sed -E 's/(\.star\.Aligned\.sortedByCoord\.out)?\.bam$//') 

#Check for File already exist or Not (.dedup.bam, .metrices)
add_bam="${OUTPUT_DIR}""${BAMFILE_NAME}.add.bam"
dedup_bam="${OUTPUT_DIR}""${BAMFILE_NAME}.dedup.bam"
metrices="${OUTPUT_DIR}""${BAMFILE_NAME}.metrices"

stage="MarkDuplicates"

if grep -q "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" && grep "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" | grep -q "|${stage}"; then
    # If both sample and stage exist in the file, that means this stage is already done for the sample
     echo "$BAMFILE_NAME: $stage already done. Skipping MarkDuplicates..."

else

echo "java -jar $PICARD_PATH MarkDuplicates -I "${OUTPUT_DIR}""${BAMFILE_NAME}.add.bam" -O "${OUTPUT_DIR}""${BAMFILE_NAME}.dedup.bam" -M "${OUTPUT_DIR}""${BAMFILE_NAME}.metrices" --CREATE_INDEX true --VALIDATION_STRINGENCY SILENT --REMOVE_DUPLICATES true"

java -jar $PICARD_PATH MarkDuplicates -I "${OUTPUT_DIR}""${BAMFILE_NAME}.add.bam" -O "${OUTPUT_DIR}""${BAMFILE_NAME}.dedup.bam" -M "${OUTPUT_DIR}""${BAMFILE_NAME}.metrices" --CREATE_INDEX true --VALIDATION_STRINGENCY SILENT --REMOVE_DUPLICATES true

markdup_status=$?

	if [ $markdup_status -eq 0 ] && [ -s "$dedup_bam" ] && [ -s "$metrices" ] && samtools quickcheck -v "$dedup_bam"; then
     		   update_pipeline_status "$BAMFILE_NAME" "MarkDuplicates"

       	   rm -f "${add_bam}"
	           echo "MarkDuplicates completed successfully. Deleting intermediate file: $add_bam"
	else
  	           echo "Error: MarkDuplicates failed or output BAM is invalid. add.bam NOT deleted."
	   exit 1
       fi 
       #Check-end
fi      
done
echo "++++++++++++++++++++++++++++++++++++ MarkDuplicates COMPLETED +++++++++++++++++++++++++++++++++++++++++"

#==================================================================================================
# Step 3: Building BAM Index using Picard (.dedup.bam ---> .dedup.bai)
#==================================================================================================

echo "Running BuildBamIndex..."
for bampath in $(echo $BamFilesPath | sed "s/,/ /g")
do
filename=$(basename "$bampath") 
BAMFILE_NAME=$(echo "$filename" | sed -E 's/(\.star\.Aligned\.sortedByCoord\.out)?\.bam$//') 


#Check for File already exist or Not .add.bam
dedup_bai="${OUTPUT_DIR}""${BAMFILE_NAME}.dedup.bai"

#if [ -f "$dedup_bai" ]; then
stage="BuildBamIndex"
if grep -q "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" && grep "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" | grep -q "|${stage}"; then
    # If both sample and stage exist in the file
     echo "$BAMFILE_NAME: $stage already done. Skipping BuildBamIndex..."
 
else
echo "java -jar $PICARD_PATH BuildBamIndex -I "${OUTPUT_DIR}""${BAMFILE_NAME}.dedup.bam" -O "${OUTPUT_DIR}""${BAMFILE_NAME}.dedup.bai""
    	java -jar $PICARD_PATH BuildBamIndex -I "${OUTPUT_DIR}""${BAMFILE_NAME}.dedup.bam" -O "${OUTPUT_DIR}""${BAMFILE_NAME}.dedup.bai"
 index_status=$?
 
 if [ $index_status -eq 0 ] && [ -s "$dedup_bai" ]; then
    echo "BuildBamIndex completed successfully. Index file: $dedup_bai"

 		 update_pipeline_status "$BAMFILE_NAME" "BuildBamIndex"
else
    echo "Error: BuildBamIndex failed or index file ($bai_file) is missing/empty."
    exit 1
fi
     	
fi
done
echo "++++++++++++++++++++++++++++++++++++ BuildBamIndex COMPLETED +++++++++++++++++++++++++++++++++++++++++"

############################################################################################################################
#STEP 4: SPLITNCIGAR (dedup.bam--->split.bam, split.bai)		OPTIONAL
############################################################################################################################

cd $GATK_PATH

if [ $SplitOption = "YES" ];
then
echo "Running SplitNCigarReads..."
for bampath in $(echo $BamFilesPath | sed "s/,/ /g")
do
filename=$(basename "$bampath") 
BAMFILE_NAME=$(echo "$filename" | sed -E 's/(\.star\.Aligned\.sortedByCoord\.out)?\.bam$//') 

#Check for File already exist or Not 
dedup_bam="${OUTPUT_DIR}""${BAMFILE_NAME}.dedup.bam"
metrices="${OUTPUT_DIR}""${BAMFILE_NAME}.metrices"
dedup_bai="${OUTPUT_DIR}""${BAMFILE_NAME}.dedup.bai"
split_bam="${OUTPUT_DIR}""${BAMFILE_NAME}.split.bam"
stage="SplitNCigarReads"

if grep -q "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" && grep "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" | grep -q "|${stage}"; then
    # If both sample and stage exist in the file
     echo "$BAMFILE_NAME: $stage already done. Skipping SplitNCigarReads..."

else

echo "./gatk --java-options "-Xmx4G" SplitNCigarReads -R "${REFERENCE}" -I "${OUTPUT_DIR}""${BAMFILE_NAME}.dedup.bam" -O "${OUTPUT_DIR}""${BAMFILE_NAME}.split.bam""
./gatk --java-options "-Xmx4G" SplitNCigarReads -R "${REFERENCE}" -I "${OUTPUT_DIR}""${BAMFILE_NAME}.dedup.bam" -O "${OUTPUT_DIR}""${BAMFILE_NAME}.split.bam"

split_status=$?

# Check if SplitNCigarReads succeeded
        if [ $split_status -eq 0 ] && [ -s "$split_bam" ] && samtools quickcheck -v "$split_bam"; then
 
            echo "SplitNCigarReads completed successfully. Deleting intermediate files:"
            echo "  - $dedup_bam"
            echo "  - $dedup_bai"
            echo "  - $metrices"

        update_pipeline_status "$BAMFILE_NAME" "SplitNCigarReads"

            rm -f "$dedup_bam" "$dedup_bai" "$metrices"
            

        else
            echo "Warning: $split_bam not created or is empty. Intermediate files NOT deleted."
 		exit 1
        fi

fi
done
echo "++++++++++++++++++++++++++++++++++++ SplitNCigarReads COMPLETED +++++++++++++++++++++++++++++++++++++++++"
fi

############################################################################################################################
#STEP 5: BaseRecalibrator   & ApplyBQSR
###########################################################################################################################

#NOTE: Download vcf files from https://ftp.ncbi.nlm.nih.gov/snp/organisms/human_9606/VCF/

#./gatk --java-options "-Xmx4G -DGATK_STACKTRACE_ON_USER_EXCEPTION=true" ValidateVariants -R "${REFERENCE}" -V "${KNOWN_SITE1}" --validation-type-to-exclude ALL  

#Create an index for the VCF file using the "IndexFeatureFile" tool

#echo "./gatk --java-options "-Xmx4G -DGATK_STACKTRACE_ON_USER_EXCEPTION=true" IndexFeatureFile -I "${KNOWN_SITE1}""

#./gatk --java-options "-Xmx4G -DGATK_STACKTRACE_ON_USER_EXCEPTION=true" IndexFeatureFile -I "${KNOWN_SITE1}"

################## BaseRecalibrator  (*.split.bam,*.vcf --> *.recal_data.table )
################## BaseRecalibrator  (*.dedup.bam,*.dedup.bai,*.metrices,*.vcf --> *.recal_data.table )


################## ApplyBQSR (*.split.bam or *.dedup.bam, *.recal_data.table  --> *.recal_reads.bam,*.recal_reads.bai) 

echo "Running BaseRecalibrator & ApplyBQSR..."

for bampath in $(echo $BamFilesPath | sed "s/,/ /g")
do
filename=$(basename "$bampath") 
BAMFILE_NAME=$(echo "$filename" | sed -E 's/(\.star\.Aligned\.sortedByCoord\.out)?\.bam$//') 
#Check for File already exist or Not .add.bam

recalData_table="${OUTPUT_DIR}""${BAMFILE_NAME}.recal_data.table"
recalRead_bam="${OUTPUT_DIR}""${BAMFILE_NAME}.recal_reads.bam"

stage="BaseRecalibrator|ApplyBQSR"
if grep -q "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" && grep "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" | grep -q "|${stage}"; then
    # If both sample and stage exist in the file
     echo "$BAMFILE_NAME: $stage already done. Skipping BaseRecalibrator & ApplyBQSR..."
    
else

if [ $SplitOption = "YES" ];
then	
	split_bam="${OUTPUT_DIR}""${BAMFILE_NAME}.split.bam"

	echo "./gatk --java-options "-Xmx4G" BaseRecalibrator -I "${OUTPUT_DIR}""${BAMFILE_NAME}.split.bam" -R "${REFERENCE}" "${KNOWN_SITES}" -O "${OUTPUT_DIR}""${BAMFILE_NAME}.recal_data.table""
	./gatk --java-options "-Xmx4G" BaseRecalibrator -I "${OUTPUT_DIR}""${BAMFILE_NAME}.split.bam" -R "${REFERENCE}" ${KNOWN_SITES} -O "${OUTPUT_DIR}""${BAMFILE_NAME}.recal_data.table"

	base_recal_status=$?

	echo "./gatk --java-options "-Xmx4G" ApplyBQSR -R "${REFERENCE}" -I "${OUTPUT_DIR}""${BAMFILE_NAME}.split.bam"  --bqsr-recal-file "${OUTPUT_DIR}""${BAMFILE_NAME}.recal_data.table" -O "${OUTPUT_DIR}""${BAMFILE_NAME}.recal_reads.bam""

	./gatk --java-options "-Xmx4G" ApplyBQSR -R "${REFERENCE}" -I "${OUTPUT_DIR}""${BAMFILE_NAME}.split.bam"  --bqsr-recal-file "${OUTPUT_DIR}""${BAMFILE_NAME}.recal_data.table" -O "${OUTPUT_DIR}""${BAMFILE_NAME}.recal_reads.bam"

	apply_bqsr_status=$?

# Check if BaseRecalibrator succeeded
        if [ $base_recal_status -eq 0 ] && [ $apply_bqsr_status -eq 0 ] && [ -s "$recalData_table" ] && [ -s "$recalRead_bam" ];then
			samtools quickcheck -v "$recalRead_bam"
    		quick_status=$?
	    	if [ $quick_status -eq 0 ]; then
		    
		    	echo "BaseRecalibrator & ApplyBQSR completed successfully. Deleting intermediate files:"
		    	echo "  - $split_bam"
		    	update_pipeline_status "$BAMFILE_NAME" "BaseRecalibrator|ApplyBQSR"
		    
		    	rm -f "$split_bam"
	    	else
				echo "Error: $recal_bam failed validation (samtools quickcheck exit code $quick_status)."
				exit 1
	    	fi
        else
            echo "Warning: $recalData_table and $recalRead_bam not created or is empty. Intermediate files NOT deleted."
			exit 1
        fi



else	
	dedup_bam="${OUTPUT_DIR}""${BAMFILE_NAME}.dedup.bam"
	metrices="${OUTPUT_DIR}""${BAMFILE_NAME}.metrices"
	dedup_bai="${OUTPUT_DIR}""${BAMFILE_NAME}.dedup.bai"

	echo "./gatk --java-options "-Xmx4G" BaseRecalibrator -I "${OUTPUT_DIR}""${BAMFILE_NAME}.dedup.bam" -R "${REFERENCE}" ${KNOWN_SITES} -O "${OUTPUT_DIR}""${BAMFILE_NAME}.recal_data.table""

	./gatk --java-options "-Xmx4G" BaseRecalibrator -I "${OUTPUT_DIR}""${BAMFILE_NAME}.dedup.bam" -R "${REFERENCE}" ${KNOWN_SITES} -O "${OUTPUT_DIR}""${BAMFILE_NAME}.recal_data.table"
	baseRecal_status=$?
# Run ApplyBQSR only if BaseRecalibrator succeeded
if [ $baseRecal_status -eq 0 ]; then
echo "./gatk --java-options "-Xmx4G" ApplyBQSR -R "${REFERENCE}" -I "${OUTPUT_DIR}""${BAMFILE_NAME}.dedup.bam"  --bqsr-recal-file "${OUTPUT_DIR}""${BAMFILE_NAME}.recal_data.table" -O "${OUTPUT_DIR}""${BAMFILE_NAME}.recal_reads.bam""
./gatk --java-options "-Xmx4G" ApplyBQSR -R "${REFERENCE}" -I "${OUTPUT_DIR}""${BAMFILE_NAME}.dedup.bam"  --bqsr-recal-file "${OUTPUT_DIR}""${BAMFILE_NAME}.recal_data.table" -O "${OUTPUT_DIR}""${BAMFILE_NAME}.recal_reads.bam"

applyBqsr_status=$?
# Check ApplyBQSR exit code and validate final BAM
    if [ $applyBqsr_status -eq 0 ]; then
    samtools quickcheck -v "$recalRead_bam"
    quick_status=$?
    	if [ $quick_status -eq 0 ]; then
		echo "BaseRecalibrator & ApplyBQSR completed successfully."
		echo "Deleting intermediate files:"
		echo "  - $dedup_bam"
		echo "  - $dedup_bai"
		echo "  - $metrices"

		update_pipeline_status "$BAMFILE_NAME" "BaseRecalibrator|ApplyBQSR"

		rm -f "$dedup_bam" "$dedup_bai" "$metrices"
	else
        	echo "Error: $recal_bam failed validation (samtools quickcheck exit code $quick_status)."
        exit 1
    	fi
        # Update pipeline status
    else
        echo "Error: ApplyBQSR failed or output BAM failed validation. Intermediate files NOT deleted."
    	exit 1
    fi
else
    echo "Error: BaseRecalibrator failed. Skipping ApplyBQSR. Intermediate files NOT deleted."
	exit 1
fi
#Apply BQSR


# Check if BaseRecalibrator succeeded
      #  if [ -s "$recalData_table" ] && [ -s "$recalRead_bam" ]; then



fi

fi
done

echo "++++++++++++++++++++++++++++++++++++ BaseRecalibrator & ApplyBQSR COMPLETED +++++++++++++++++++++++++++++++++++++++++"



############################################################################################################################
# STEP 6: AnalyzeCovariates: Evaluate and compare base quality score recalibration (BQSR) tables
#	( *.recal_data.table --> *.analyzeC.pdf,*.analyzeC.csv)
############################################################################################################################

echo "Running AnalyzeCovariates..."
for bampath in $(echo $BamFilesPath | sed "s/,/ /g")
do
filename=$(basename "$bampath") 
BAMFILE_NAME=$(echo "$filename" | sed -E 's/(\.star\.Aligned\.sortedByCoord\.out)?\.bam$//') 

#Check for File already exist or Not .add.bam
analyzeC_pdf="${OUTPUT_DIR}""${BAMFILE_NAME}.analyzeC.pdf"
analyzeC_csv="${OUTPUT_DIR}""${BAMFILE_NAME}.analyzeC.csv"


if grep -q "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" && grep "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" | grep -q "|${stage}"; then
    # If both sample and stage exist in the file
     echo "$BAMFILE_NAME: $stage already done. Skipping AnalyzeCovariates..."
else

echo "./gatk --java-options "-Xmx4G" AnalyzeCovariates -bqsr "${OUTPUT_DIR}""${BAMFILE_NAME}.recal_data.table" -plots "${OUTPUT_DIR}""${BAMFILE_NAME}.analyzeC.pdf" -csv "${OUTPUT_DIR}""${BAMFILE_NAME}.analyzeC.csv""

export R_LIBS

./gatk --java-options "-Xmx4G" AnalyzeCovariates -bqsr "${OUTPUT_DIR}""${BAMFILE_NAME}.recal_data.table" -plots "${OUTPUT_DIR}""${BAMFILE_NAME}.analyzeC.pdf" -csv "${OUTPUT_DIR}""${BAMFILE_NAME}.analyzeC.csv"

update_pipeline_status "$BAMFILE_NAME" "AnalyzeCovariates"

fi
done

echo "++++++++++++++++++++++++++++++++++++ AnalyzeCovariates COMPLETED +++++++++++++++++++++++++++++++++++++++++"

#############################################################################################################################
#STEP 7: GERMLINE OR SOMATIC OR BOTH 
# Germline   .recal_reads.bam --> _ghap.vcf.gz
# Somatic                     --> _mut.vcf.gz
#############################################################################################################################


if [ "$germSomaticOP" = "GERMLINE" ]; then
	echo "========================================================================"
	echo " Running HaplotypeCaller Pipeline (Mode: $HaploOption)"
	echo " Interval Target: ${HaploInterval:-'Whole Genome (WGS - No Interval)'}"
	echo "========================================================================"
	#STEP 7: HaplotypeCaller: Call germline SNPs and indels via local re-assembly of haplotypes 
	for bampath in $(echo $BamFilesPath | sed "s/,/ /g")
	do
		
		filename=$(basename "$bampath") 
		BAMFILE_NAME=$(echo "$filename" | sed -E 's/(\.star\.Aligned\.sortedByCoord\.out)?\.bam$//') 
		haplotype_cmd="./gatk --java-options \"-Xms4G -Xmx12G\" HaplotypeCaller -I "${OUTPUT_DIR}${BAMFILE_NAME}.recal_reads.bam" -R "${REFERENCE}" -O "${OUTPUT_DIR}""${BAMFILE_NAME}_ghap.vcf.gz" -ERC "$ERC" -G StandardAnnotation -G AS_StandardAnnotation --native-pair-hmm-threads ${Thread}"
		
		# Add interval target if provided
		if [ -n "$HaploInterval" ]; then
			haplotype_cmd="$haplotype_cmd $HaploInterval --interval-padding 100"
		fi
		
		#Single-sample GVCF calling with allele-specific annotations
		if [ -n "$G1" ]; then
		haplotype_cmd="$haplotype_cmd $G1"
		fi

		#Variant calling with bamout to show realigned reads
		if [ -n "$bamout" ]; then
		haplotype_cmd="$haplotype_cmd -bamout $bamout"
		fi

		#Single-sample GVCF calling (outputs intermediate GVCF)
		happ_vcf="${OUTPUT_DIR}""${BAMFILE_NAME}_ghap.vcf.gz"
		hap_vcf="${OUTPUT_DIR}""${BAMFILE_NAME}_hap.vcf.gz"

		stage="HaplotypeCaller"
		if grep -q "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" && grep "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" | grep -q "|${stage}"; then
			# If both sample and stage exist in the file
			echo "$BAMFILE_NAME: $stage already done. Skipping HaplotypeCaller..."
		else

			echo "HaplotypeCaller Command: $haplotype_cmd"
			eval "$haplotype_cmd"
			hap_status=$?

			if [ $hap_status -eq 0 ] && [ -s "$happ_vcf" ]; then 
				update_pipeline_status "$BAMFILE_NAME" "HaplotypeCaller"
				
				if [ "$HaploOption" = "SINGLEMODE" ];
				then
					stage="GenotypeGVCFs"
					if grep -q "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" && grep "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" | grep -q "|${stage}"; 
					then
						# If both sample and stage exist in the file
						echo "$BAMFILE_NAME: $stage already done. Skipping GenotypeGVCFs..."
					
					else
						echo "Running GenotypeGVCFs (SINGLEMODE) for $BAMFILE_NAME : "
						genotype_cmd="./gatk --java-options "-Xms4G -Xmx18G" GenotypeGVCFs -R "$REFERENCE" -V "${OUTPUT_DIR}${BAMFILE_NAME}_ghap.vcf.gz" -G StandardAnnotation -G AS_StandardAnnotation -O "${OUTPUT_DIR}${BAMFILE_NAME}_hap.vcf.gz""
							
							if [ -n "$HaploInterval" ]; then
								genotype_cmd="$genotype_cmd $HaploInterval"
							fi
						echo "GenotypeGVCFs CMD: $genotype_cmd"
						eval "$genotype_cmd"	
						genotype_status=$?
						
							if [ $genotype_status -eq 0 ] && [ -s "$hap_vcf" ]; then 
								update_pipeline_status "$BAMFILE_NAME" "GenotypeGVCFs"
								echo "GenotypeGVCFs completed successfully."
								
								rm -f "$happ_vcf"
							else
								echo "Error: GenotypeGVCFs failed or output VCF ($hap_vcf) is invalid or not indexed properly."
								exit 1
							fi

					fi

				else
					continue			
				fi
			else
				echo "Error: HaplotypeCaller failed or output VCF ($hap_vcf) is invalid or not indexed properly."
				exit 1
			fi
		
		
		fi
	done

	if [ "$HaploOption" = "JOINTMODE" ];
	then
		BAMFILE_NAME="joint"
		# List all _hap.vcf.gz files in the specified directory
		# 1- Define GenomicsDB workspace directory
		DB_PATH="$OUTPUT_DIR/genomicsdb"

		# 2- Creating a map file	
		GVCF_LIST="$OUTPUT_DIR/gvcf_map.txt"
		echo "------------------------------------------------------------------------"
		echo " Starting Joint Variant Calling Pipeline"
		echo "------------------------------------------------------------------------"
	
		# Correction: Always clear/rebuild the map from current BAMs
		> "$GVCF_LIST"
		for bampath in $(echo $BamFilesPath | sed "s/,/ /g")
		do
    		filename=$(basename "$bampath")
    		sample=$(echo "$filename" | sed -E 's/(\.star\.Aligned\.sortedByCoord\.out)?\.bam$//')
    		gvcf_path="${OUTPUT_DIR}${sample}_ghap.vcf.gz"
    
    		if [ -s "$gvcf_path" ]; then
        		echo -e "${sample}\t${gvcf_path}" >> "$GVCF_LIST"
    		else
        		echo "Error: GVCF file for $sample ($gvcf_path) is missing or empty."
       			 exit 1
    		fi
		done
		
		
		#if [ ! -s "$GVCF_LIST" ]; then
		#	for f in "$OUTPUT_DIR"/*_ghap.vcf.gz; do
		#		sample=$(basename "$f" _ghap.vcf.gz)
		#		echo -e "$sample\t$f" >> "$GVCF_LIST"
		#	done
		#else
		#	echo "Map File $GVCF_LIST already exists. Skipping creation."
		#fi
	
		echo "Running GenomicsDBImport + GenotypeGVCFs..."
		#Changed:  Generate a unique hash based on the content of the current sample map
		MAP_HASH=$(md5sum "$GVCF_LIST" | cut -d' ' -f1)
		DB_PATH="$OUTPUT_DIR/genomicsdb_${MAP_HASH}"
		
		#3- Check GenomicsDBImport in Status or not
		stage="GenomicsDBImport"
		if grep -q "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" && grep "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" | grep -q "|${stage}"; then
	
			if [ -d "$DB_PATH" ] && [ -f "$DB_PATH/callset.json" ] && [ -f "$DB_PATH/vidmap.json" ] && [ -f "$DB_PATH/vcfheader.vcf" ] ; then
				echo "GenomicsDB workspace already exists at $DB_PATH"
				echo "Skipping GenomicsDBImport..."
			else
				echo "Error: GenomicsDBImport is recorded in the status file, but one or more required files are missing ($DB_PATH, $DB_PATH/callset.json, $DB_PATH/vidmap.json, $DB_PATH/vcfheader.vcf)."
				exit 1
			fi

		else
			echo ">>> Running GenomicsDBImport..."
			genomicsdb_cmd="./gatk --java-options "-Xms4g -Xmx18g" GenomicsDBImport --genomicsdb-workspace-path "$DB_PATH" --batch-size 50 --sample-name-map "${GVCF_LIST}" --reader-threads ${Thread} ${HaploInterval}"
			if [ -n "$HaploInterval" ]; then
				genomicsdb_cmd="$genomicsdb_cmd $HaploInterval"
			fi
			echo "GenomicsDBImport CMD $genomicsdb_cmd"
			eval "$genomicsdb_cmd"
			genomic_status=$?
				if [ $genomic_status -eq 0 ]; then 
					update_pipeline_status "$BAMFILE_NAME" "$stage"
					echo "GenomicsDBImport completed Successfully" 
				else
					echo "Error: GenomicsDBImport failed."
					exit 1		
				fi
		

		fi
		#GenomicsDBImport-Status

		JOINT_OUTPUT_FILE="$OUTPUT_DIR/joint_hap.vcf.gz"
		INDEX_FILE="$JOINT_OUTPUT_FILE.tbi"
	
		# 4 - Check GenotypeGVCFs status	
		stage="GenotypeGVCFs"
		if grep -q "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" && grep "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" | grep -q "|${stage}"; then
	
			if [ -s "$JOINT_OUTPUT_FILE" ] && [ -s "$INDEX_FILE" ]; then
    			echo "Skipping GenotypeGVCFs: Already recorded in pipeline status."
				echo " $JOINT_OUTPUT_FILE and its index already exist."
			else
				echo "Error: GenotypeGVCFs is written in status file but $JOINT_OUTPUT_FILE and its index does not exist."
				exit 1
			fi
		else
			echo "Running Joint GenotypeGVCFs..."
			joint_genotype_cmd="./gatk --java-options "-Xms4G -Xmx32G" GenotypeGVCFs -R "$REFERENCE" -V gendb://"$DB_PATH" -G StandardAnnotation -G AS_StandardAnnotation -O "$OUTPUT_DIR""joint_hap.vcf.gz""
			if [ -n "$HaploInterval" ]; then
				joint_genotype_cmd="$joint_genotype_cmd $HaploInterval"
			fi

			echo "Joint GenotypeGVCFs CMD: $joint_genotype_cmd"
			eval "$joint_genotype_cmd"
			genotype_status=$?
					
			if [ $genotype_status -eq 0 ] && [ -s "$JOINT_OUTPUT_FILE" ]  && [ -s "$INDEX_FILE" ]; then 
				update_pipeline_status "$BAMFILE_NAME" "$stage"
				echo "GenotypeGVCFs completed successfully."
				
			else
				echo "Error: GenotypeGVCFs failed or output VCF ($JOINT_OUTPUT_FILE, INDEX_FILE) is invalid or not indexed properly."
				exit 1
			fi
			#GVCF-IF

		fi
		#GenotypeGVCFs-Status check
		
	
	fi
	#Joint-if
	echo "========================================================================"
	echo " HaplotypeCaller Pipeline Phase Completed"
	echo "========================================================================"
	
##End haplotypecaller
elif [ "$germSomaticOP" = "SOMATIC" ]; then
	echo "========================================================================"
	echo " Running Mutect2 Pipeline (Mode: $somaticOP)"
	echo "========================================================================"
	
	
	stage="Mutect2"
	#=====================================================================================================
	# 					Mutect2: 01- TUMOR WITH MATCHED NORMAL (SINGLE)
	#=====================================================================================================

	if [ "$somaticOP" = "TUMORN" ]; then  #TUMOR WITH MATCHED NORMAL (SINGLE)

		for bampath in $(echo $TumorNormalData | sed "s/,/ /g")
		do
		
			TUMORNAME=$(echo "$bampath" | cut -d'#' -f1)
			NORMALNAME=$(echo "$bampath" | cut -d'#' -f2)

			mutect_cmd="./gatk --java-options "-Xmx4G" Mutect2 -R \"${REFERENCE}\" -I \"${OUTPUT_DIR}${TUMORNAME}.recal_reads.bam\" -I \"${OUTPUT_DIR}${NORMALNAME}.recal_reads.bam\" -normal \"${NORMALNAME}\" --germline-resource \"${germlineResource}\" --panel-of-normals \"${PON}\" -O \"${OUTPUT_DIR}${TUMORNAME}.${NORMALNAME}_mut.vcf.gz\""

			if grep -q "^${TUMORNAME}|" "$PIPELINE_STATUS_FILE" && grep "^${TUMORNAME}|" "$PIPELINE_STATUS_FILE" | grep -q "|${stage}"; then
				# If both sample and stage exist in the file
				echo "$TUMORNAME: $stage already done. Skipping Mutect2..."
			else
				echo "Mutect2 Command: $mutect_cmd"
				eval "$mutect_cmd"
				update_pipeline_status "$TUMORNAME" "$stage"
			fi
			#For pipelinecheck
		done
		#=====================================================================================================
		# 					Mutect2: 02- TUMOR WITH MATCHED NORMAL (MULTIPLE)
		#=====================================================================================================

	elif [ "$somaticOP" = "TUMORM" ]; then   # TUMOR WITH MATCHED NORMAL (MULTIPLE)
		#Changed
		JOINT_MUT_VCF="${OUTPUT_DIR}joint_mut.vcf.gz"
		mutect_cmd="./gatk --java-options "-Xmx4G" Mutect2 -R "${REFERENCE}" --tmp-dir \"${TMP_DIR}\""
		
		if [ -n "$INTERVALS" ]; then
        	mutect_cmd="$mutect_cmd -L \"${INTERVALS}\" --interval-padding 100"
    	fi
		
		for bampath in $(echo $TumorNormalData | sed "s/,/ /g")
		do

 	    	TUMORNAME=$(echo "$bampath" | cut -d'#' -f1)
	    	NORMALNAME=$(echo "$bampath" | cut -d'#' -f2)
	    
			mutect_cmd="$mutect_cmd -I "${OUTPUT_DIR}""${TUMORNAME}.recal_reads.bam" -I "${OUTPUT_DIR}""${NORMALNAME}.recal_reads.bam" -normal "${NORMALNAME}""
		
		done

			if [ -n "$germlineResource" ]; then
        		mutect_cmd="$mutect_cmd --germline-resource \"${germlineResource}\""
    		fi
    		if [ -n "$PON" ]; then
        		mutect_cmd="$mutect_cmd --panel-of-normals \"${PON}\""
    		fi
			
			mutect_cmd="$mutect_cmd -O \"$JOINT_MUT_VCF\""

			if grep -q "^${TUMORNAME}|" "$PIPELINE_STATUS_FILE" && grep "^${TUMORNAME}|" "$PIPELINE_STATUS_FILE" | grep -q "|${stage}"; then
				# If both sample and stage exist in the file
				echo "$TUMORNAME: $stage already done. Skipping Mutect2..."
			else

				echo "Mutect2 Command: $mutect_cmd"
				eval "$mutect_cmd"
				mutect_status=$?
				if [ $mutect_status -eq 0 ] && [ -s "$JOINT_MUT_VCF" ]; then
					if [ ! -f "${JOINT_MUT_VCF}.tbi" ]; then
            			./gatk IndexFeatureFile -I "$JOINT_MUT_VCF"
					fi
            		update_pipeline_status "$BAMFILE_NAME" "$stage"
        		else
            		echo "Error: Mutect2 failed or output VCF ($JOINT_MUT_VCF) is missing."
            		exit 1
        		fi
				
			fi
		#=====================================================================================================
		# 					Mutect2: 03- TUMOR ONLY MODE
		#=====================================================================================================

	elif [ "$somaticOP" = "TUMORO" ]; then #TUMOR ONLY MODE
		echo "Starting Tumor Only mode......."
		
		for bampath in $(echo $BamFilesPath | sed "s/,/ /g")
		do
			filename=$(basename "$bampath") 
			BAMFILE_NAME=$(echo "$filename" | sed -E 's/(\.star\.Aligned\.sortedByCoord\.out)?\.bam$//') 
			F1R2_FILE="${OUTPUT_DIR}${BAMFILE_NAME}_f1r2.tar.gz"
			mutect_cmd="./gatk --java-options "-Xmx4G" Mutect2 -R "${REFERENCE}" -I "${TempOutputDir}""${BAMFILE_NAME}.recal_reads.bam" -O "${OUTPUT_DIR}""${BAMFILE_NAME}_mut.vcf.gz" --f1r2-tar-gz "${F1R2_FILE}""

			if [ -n "$germlineResource" ]; then
				mutect_cmd="$mutect_cmd --germline-resource "${germlineResource}""
			fi

			if [ -n "$PON" ]; then
				mutect_cmd="$mutect_cmd --pon "${PON}""
			fi

			if grep -q "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" && grep "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" | grep -q "|${stage}"; then
				# If both sample and stage exist in the file
				echo "$BAMFILE_NAME: $stage already done. Skipping Mutect2..."
			else

				echo "Mutect2 Command: $mutect_cmd"
				eval "$mutect_cmd"
				update_pipeline_status "$BAMFILE_NAME" "$stage"
			fi

		done
		#=====================================================================================================
		# 					Mutect2: 04- TUMOR ONLY MODE with Normals
		#=====================================================================================================

	elif [ "$somaticOP" = "TUMORON" ]; then #TUMOR ONLY MODE with Normals

		for bampath in $(echo $BamFilesPath | sed "s/,/ /g")
		do
			filename=$(basename "$bampath") 
			BAMFILE_NAME=$(echo "$filename" | sed -E 's/(\.star\.Aligned\.sortedByCoord\.out)?\.bam$//') 
			mutect_cmd="./gatk --java-options "-Xmx4G" Mutect2 -R "${REFERENCE}" -I "${OUTPUT_DIR}""${BAMFILE_NAME}.recal_reads.bam" -max-mnp-distance 0 -O "${OUTPUT_DIR}""${BAMFILE_NAME}_pon.vcf.gz""
			if [ -n "$germlineResource" ]; then
			mutect_cmd="$mutect_cmd --germline-resource "${germlineResource}""
			fi


			if grep -q "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" && grep "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" | grep -q "|${stage}"; then
				# If both sample and stage exist in the file
				echo "$BAMFILE_NAME: $stage already done. Skipping Mutect2..."
			else

				echo "Mutect2 Command: $mutect_cmd"
				eval "$mutect_cmd"
				update_pipeline_status "$BAMFILE_NAME" "$stage"
			fi

		done
		# Build sample map for GenomicsDBImport
		sample_map="${OUTPUT_DIR}pon_sample_map.txt"
		rm -f "$sample_map"
		for vcf in $(find "$OUTPUT_DIR" -name "*_pon.vcf.gz"); do
			sample=$(basename "$vcf" | cut -d'_' -f1)
			echo "$sample $vcf" >> "$sample_map"
		done

		genomicsdb_dir="${OUTPUT_DIR}pon_db"
		echo "Running GenomicsDBImport..."

		./gatk --java-options "-Xmx4g -Xms4g" GenomicsDBImport --sample-name-map "$sample_map" --genomicsdb-workspace-path "$genomicsdb_dir" 

		pon_output="${OUTPUT_DIR}PON.vcf.gz"
		./gatk CreateSomaticPanelOfNormals -R "$REFERENCE" -V gendb://"$genomicsdb_dir" -O "$pon_output"
		
		#=====================================================================================================
		# 					Mutect2: 04- TUMOR ONLY MODE with PON
		#=====================================================================================================

	elif [ "$somaticOP" = "TUMOROPON" ]; then #TUMOR ONLY MODE with PON
		pon_output="${OUTPUT_DIR}PON.vcf.gz"

		#======= First creating PON from Normals
		for bampath in $(echo $TumorNormalData | sed "s/,/ /g")
		do
		
			TUMORNAME=$(echo "$bampath" | cut -d'#' -f1)
			NORMALNAME=$(echo "$bampath" | cut -d'#' -f2)

			mutect_cmd="./gatk --java-options "-Xmx4G" Mutect2 -R "${REFERENCE}" -I "${OUTPUT_DIR}""${NORMALNAME}.recal_reads.bam" -max-mnp-distance 0 -O "${OUTPUT_DIR}""${NORMALNAME}_pon.vcf.gz""

			if [ -n "$germlineResource" ]; then
			mutect_cmd="$mutect_cmd --germline-resource \"${germlineResource}\""
			fi


			if grep -q "^${NORMALNAME}|" "$PIPELINE_STATUS_FILE" && grep "^${NORMALNAME}|" "$PIPELINE_STATUS_FILE" | grep -q "|${stage}"; then
				# If both sample and stage exist in the file
				echo "$BAMFILE_NAME: $stage already done. Skipping Mutect2..."
			else
				echo "Running Mutect2 on normal sample: $NORMALNAME"
				echo "Mutect2 Command: $mutect_cmd"
				eval "$mutect_cmd"
				update_pipeline_status "$BAMFILE_NAME" "$stage"
			fi

		done
		#======= Build sample map for GenomicsDBImport
		sample_map="${OUTPUT_DIR}pon_sample_map.txt"
		rm -f "$sample_map"
		for vcf in $(find "$OUTPUT_DIR" -name "*_pon.vcf.gz"); do
			sample=$(basename "$vcf" | cut -d'_' -f1)
			echo "$sample $vcf" >> "$sample_map"
		done

		genomicsdb_dir="${OUTPUT_DIR}pon_db"
		echo "Running GenomicsDBImport..."

		./gatk --java-options "-Xmx4g -Xms4g" GenomicsDBImport --sample-name-map "$sample_map" --genomicsdb-workspace-path "$genomicsdb_dir" 

		./gatk CreateSomaticPanelOfNormals -R "$REFERENCE" -V gendb://"$genomicsdb_dir" -O "$pon_output"

		#========= Second running Tumor only on tumors with above PON
		for bampath in $(echo $TumorNormalData | sed "s/,/ /g")
		do
			
				TUMORNAME=$(echo "$bampath" | cut -d'#' -f1)
				NORMALNAME=$(echo "$bampath" | cut -d'#' -f2)


			mutect_cmd="./gatk --java-options "-Xmx4G" Mutect2 -R "${REFERENCE}" -I "${OUTPUT_DIR}""${TUMORNAME}.recal_reads.bam" -O "${OUTPUT_DIR}""${TUMORNAME}_mut.vcf.gz""

			if [ -n "$germlineResource" ]; then
			mutect_cmd="$mutect_cmd --germline-resource \"${germlineResource}\""
			fi

			# Use generated PON file
			if [ -f "$pon_output" ]; then
				mutect_cmd="$mutect_cmd --pon \"$pon_output\""
			else
					echo "Error: PON file not found at $pon_output"
					exit 1
			fi

			if grep -q "^${TUMORNAME}|" "$PIPELINE_STATUS_FILE" && grep "^${TUMORNAME}|" "$PIPELINE_STATUS_FILE" | grep -q "|${stage}"; then
				# If both sample and stage exist in the file
				echo "$BAMFILE_NAME: $stage already done. Skipping Mutect2..."
			else

				echo "Running Mutect2 on tumor sample: $TUMORNAME"
				echo "Mutect2 Command: $mutect_cmd"
				eval "$mutect_cmd"
				update_pipeline_status "$BAMFILE_NAME" "$TUMORNAME"
			fi

		done
		#=====================================================================================================
		# 					Mutect2: 05- Mitochondrial mode
		#=====================================================================================================

	elif [ "$somaticOP" = "TUMORMIT" ]; then # Mitochondrial mode

		for bampath in $(echo $BamFilesPath | sed "s/,/ /g")
		do
			filename=$(basename "$bampath") 
			BAMFILE_NAME=$(echo "$filename" | sed -E 's/(\.star\.Aligned\.sortedByCoord\.out)?\.bam$//') 

			mutect_cmd="./gatk --java-options "-Xmx4G" Mutect2 -R "${REFERENCE}" -L "${genomicInterval}" --mitochondria-mode -I "${OUTPUT_DIR}""${BAMFILE_NAME}.recal_reads.bam" -O "${OUTPUT_DIR}""${BAMFILE_NAME}_mut.vcf.gz""

			if grep -q "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" && grep "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" | grep -q "|${stage}"; then
				# If both sample and stage exist in the file
				echo "$BAMFILE_NAME: $stage already done. Skipping Mutect2..."
			else
				echo "Mutect2 Command: $mutect_cmd"
				eval "$mutect_cmd"
				update_pipeline_status "$BAMFILE_NAME" "$stage"
			fi


		done
			#=====================================================================================================
			# 					Mutect2: 06- Force calling mode
			#=====================================================================================================

	elif [ "$somaticOP" = "TUMORFOR" ]; then #Force calling mode

		for bampath in $(echo $BamFilesPath | sed "s/,/ /g")
		do
		filename=$(basename "$bampath") 
		BAMFILE_NAME=$(echo "$filename" | sed -E 's/(\.star\.Aligned\.sortedByCoord\.out)?\.bam$//') 

		if [ -n "$TumorNormalData" ]; then
		mutect_cmd="./gatk --java-options "-Xmx4G" Mutect2 -R \"${REFERENCE}\" -I \"${OUTPUT_DIR}${BAMFILE_NAME}.recal_reads.bam\" -alleles \"${TumorNormalData}\" -O \"${OUTPUT_DIR}${BAMFILE_NAME}_mut.vcf.gz\""
		fi
		if [ -n "$germlineResource" ]; then
		mutect_cmd="./gatk --java-options "-Xmx4G" Mutect2 -R \"${REFERENCE}\" -I \"${OUTPUT_DIR}${BAMFILE_NAME}.recal_reads.bam\" --f1r2-tar-gz \"${germlineResource}\" -O \"${OUTPUT_DIR}${BAMFILE_NAME}_mut.vcf.gz\""
		fi


		if grep -q "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" && grep "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" | grep -q "|${stage}"; then
			# If both sample and stage exist in the file
			echo "$BAMFILE_NAME: $stage already done. Skipping Mutect2..."
		else
		echo "Mutect2 Command: $mutect_cmd"
		eval "$mutect_cmd"
		update_pipeline_status "$BAMFILE_NAME" "$stage"
		fi

		done

	fi
	#Mutect2
	echo "++++++++++++++++++++++++++++++++++++ Mutect2 COMPLETED +++++++++++++++++++++++++++++++++++++++++"

elif [ "$germSomaticOP" = "BOTH" ]; then   #BOTH
		##############################
		#### RUNNING HAPLOTYPE CALLER 
		##############################
		echo "Running HaplotypeCaller 1st.....Please Wait!"

	echo "========================================================================"
	echo " Running HaplotypeCaller Pipeline (Mode: $HaploOption)"
	echo " Interval Target: ${HaploInterval:-'Whole Genome (WGS - No Interval)'}"
	echo "========================================================================"

	#STEP 7: HaplotypeCaller: Call germline SNPs and indels via local re-assembly of haplotypes 
	for bampath in $(echo $BamFilesPath | sed "s/,/ /g")
	do
		filename=$(basename "$bampath") 
		BAMFILE_NAME=$(echo "$filename" | sed -E 's/(\.star\.Aligned\.sortedByCoord\.out)?\.bam$//') 
		haplotype_cmd="./gatk --java-options \"-Xms4G -Xmx12G\" HaplotypeCaller -I "${OUTPUT_DIR}${BAMFILE_NAME}.recal_reads.bam" -R "${REFERENCE}" -O "${OUTPUT_DIR}""${BAMFILE_NAME}_ghap.vcf.gz" -ERC "$ERC" -G StandardAnnotation -G AS_StandardAnnotation --native-pair-hmm-threads ${Thread}"
		
		# Add interval target if provided
		if [ -n "$HaploInterval" ]; then
			haplotype_cmd="$haplotype_cmd $HaploInterval --interval-padding 100"
		fi
		
		#Single-sample GVCF calling with allele-specific annotations
		if [ -n "$G1" ]; then
		haplotype_cmd="$haplotype_cmd $G1"
		fi

		#Variant calling with bamout to show realigned reads
		if [ -n "$bamout" ]; then
		haplotype_cmd="$haplotype_cmd -bamout $bamout"
		fi

		#Single-sample GVCF calling (outputs intermediate GVCF)
		happ_vcf="${OUTPUT_DIR}""${BAMFILE_NAME}_ghap.vcf.gz"
		hap_vcf="${OUTPUT_DIR}""${BAMFILE_NAME}_hap.vcf.gz"

		stage="HaplotypeCaller"
		if grep -q "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" && grep "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" | grep -q "|${stage}"; then
			# If both sample and stage exist in the file
			echo "$BAMFILE_NAME: $stage already done. Skipping HaplotypeCaller..."
		else

			echo "HaplotypeCaller Command: $haplotype_cmd"
			eval "$haplotype_cmd"
			hap_status=$?

			if [ $hap_status -eq 0 ] && [ -s "$happ_vcf" ]; then 
				update_pipeline_status "$BAMFILE_NAME" "HaplotypeCaller"
				
				if [ "$HaploOption" = "SINGLEMODE" ];
				then
					stage="GenotypeGVCFs"
					if grep -q "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" && grep "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" | grep -q "|${stage}"; 
					then
						# If both sample and stage exist in the file
						echo "$BAMFILE_NAME: $stage already done. Skipping GenotypeGVCFs..."
					
					else
						echo "Running GenotypeGVCFs (SINGLEMODE) for $BAMFILE_NAME : "
						genotype_cmd="./gatk --java-options "-Xms4G -Xmx18G" GenotypeGVCFs -R "$REFERENCE" -V "${OUTPUT_DIR}${BAMFILE_NAME}_ghap.vcf.gz" -G StandardAnnotation -G AS_StandardAnnotation -O "${OUTPUT_DIR}${BAMFILE_NAME}_hap.vcf.gz""
							
							if [ -n "$HaploInterval" ]; then
								genotype_cmd="$genotype_cmd $HaploInterval"
							fi
						echo "GenotypeGVCFs CMD: $genotype_cmd"
						eval "$genotype_cmd"	
						genotype_status=$?
						
							if [ $genotype_status -eq 0 ] && [ -s "$hap_vcf" ]; then 
								update_pipeline_status "$BAMFILE_NAME" "GenotypeGVCFs"
								echo "GenotypeGVCFs completed successfully."
								
								rm -f "$happ_vcf"
							else
								echo "Error: GenotypeGVCFs failed or output VCF ($hap_vcf) is invalid or not indexed properly."
								exit 1
							fi

					fi

				else
					continue			
				fi
			else
				echo "Error: HaplotypeCaller failed or output VCF ($hap_vcf) is invalid or not indexed properly."
				exit 1
			fi
		
		
		fi
	done

	if [ "$HaploOption" = "JOINTMODE" ];
	then
		BAMFILE_NAME="joint"
		# List all _hap.vcf.gz files in the specified directory
		# 1- Define GenomicsDB workspace directory
		DB_PATH="$OUTPUT_DIR/genomicsdb"

		# 2- Creating a map file	
		GVCF_LIST="$OUTPUT_DIR/gvcf_map.txt"
		echo "------------------------------------------------------------------------"
		echo " Starting Joint Variant Calling Pipeline"
		echo "------------------------------------------------------------------------"
	
		# Correction: Always clear/rebuild the map from current BAMs
		> "$GVCF_LIST"
		for bampath in $(echo $BamFilesPath | sed "s/,/ /g")
		do
    		filename=$(basename "$bampath")
    		sample=$(echo "$filename" | sed -E 's/(\.star\.Aligned\.sortedByCoord\.out)?\.bam$//')
    		gvcf_path="${OUTPUT_DIR}${sample}_ghap.vcf.gz"
    
    		if [ -s "$gvcf_path" ]; then
        		echo -e "${sample}\t${gvcf_path}" >> "$GVCF_LIST"
    		else
        		echo "Error: GVCF file for $sample ($gvcf_path) is missing or empty."
       			 exit 1
    		fi
		done
		
		
		#if [ ! -s "$GVCF_LIST" ]; then
		#	for f in "$OUTPUT_DIR"/*_ghap.vcf.gz; do
		#		sample=$(basename "$f" _ghap.vcf.gz)
		#		echo -e "$sample\t$f" >> "$GVCF_LIST"
		#	done
		#else
		#	echo "Map File $GVCF_LIST already exists. Skipping creation."
		#fi
	
		echo "Running GenomicsDBImport + GenotypeGVCFs..."
		#Changed:  Generate a unique hash based on the content of the current sample map
		MAP_HASH=$(md5sum "$GVCF_LIST" | cut -d' ' -f1)
		DB_PATH="$OUTPUT_DIR/genomicsdb_${MAP_HASH}"
		
		#3- Check GenomicsDBImport in Status or not
		stage="GenomicsDBImport"
		if grep -q "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" && grep "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" | grep -q "|${stage}"; then
	
			if [ -d "$DB_PATH" ] && [ -f "$DB_PATH/callset.json" ] && [ -f "$DB_PATH/vidmap.json" ] && [ -f "$DB_PATH/vcfheader.vcf" ] ; then
				echo "GenomicsDB workspace already exists at $DB_PATH"
				echo "Skipping GenomicsDBImport..."
			else
				echo "Error: GenomicsDBImport is recorded in the status file, but one or more required files are missing ($DB_PATH, $DB_PATH/callset.json, $DB_PATH/vidmap.json, $DB_PATH/vcfheader.vcf)."
				exit 1
			fi

		else
			echo ">>> Running GenomicsDBImport..."
			genomicsdb_cmd="./gatk --java-options "-Xms4g -Xmx18g" GenomicsDBImport --genomicsdb-workspace-path "$DB_PATH" --batch-size 50 --sample-name-map "${GVCF_LIST}" --reader-threads ${Thread} ${HaploInterval}"
			if [ -n "$HaploInterval" ]; then
				genomicsdb_cmd="$genomicsdb_cmd $HaploInterval"
			fi
			echo "GenomicsDBImport CMD $genomicsdb_cmd"
			eval "$genomicsdb_cmd"
			genomic_status=$?
				if [ $genomic_status -eq 0 ]; then 
					update_pipeline_status "$BAMFILE_NAME" "$stage"
					echo "GenomicsDBImport completed Successfully" 
				else
					echo "Error: GenomicsDBImport failed."
					exit 1		
				fi
		

		fi
		#GenomicsDBImport-Status

		JOINT_OUTPUT_FILE="$OUTPUT_DIR/joint_hap.vcf.gz"
		INDEX_FILE="$JOINT_OUTPUT_FILE.tbi"
	
		# 4 - Check GenotypeGVCFs status	
		stage="GenotypeGVCFs"
		if grep -q "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" && grep "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" | grep -q "|${stage}"; then
	
			if [ -s "$JOINT_OUTPUT_FILE" ] && [ -s "$INDEX_FILE" ]; then
    			echo "Skipping GenotypeGVCFs: Already recorded in pipeline status."
				echo " $JOINT_OUTPUT_FILE and its index already exist."
			else
				echo "Error: GenotypeGVCFs is written in status file but $JOINT_OUTPUT_FILE and its index does not exist."
				exit 1
			fi
		else
			echo "Running Joint GenotypeGVCFs..."
			joint_genotype_cmd="./gatk --java-options "-Xms4G -Xmx32G" GenotypeGVCFs -R "$REFERENCE" -V gendb://"$DB_PATH" -G StandardAnnotation -G AS_StandardAnnotation -O "$OUTPUT_DIR""joint_hap.vcf.gz""
			if [ -n "$HaploInterval" ]; then
				joint_genotype_cmd="$joint_genotype_cmd $HaploInterval"
			fi

			echo "Joint GenotypeGVCFs CMD: $joint_genotype_cmd"
			eval "$joint_genotype_cmd"
			genotype_status=$?
					
			if [ $genotype_status -eq 0 ] && [ -s "$JOINT_OUTPUT_FILE" ]  && [ -s "$INDEX_FILE" ]; then 
				update_pipeline_status "$BAMFILE_NAME" "$stage"
				echo "GenotypeGVCFs completed successfully."
				
			else
				echo "Error: GenotypeGVCFs failed or output VCF ($JOINT_OUTPUT_FILE, INDEX_FILE) is invalid or not indexed properly."
				exit 1
			fi
			#GVCF-IF

		fi
		#GenotypeGVCFs-Status check
		
	
	fi
	#Joint-if
	echo "========================================================================"
	echo " HaplotypeCaller Pipeline Phase Completed"
	echo "========================================================================"
	
	#echo "++++++++++++++++++++++++++++++++++++ HaplotypeCaller COMPLETED +++++++++++++++++++++++++++++++++++++++++"


		echo "Running MUTECT2.....Please Wait!"

		stage="Mutect2"
		#=====================================================================================================
		# 	BOTH			Mutect2: 01- TUMOR WITH MATCHED NORMAL (SINGLE)
		#=====================================================================================================
	if [ "$somaticOP" = "TUMORN" ]; then  #TUMOR WITH MATCHED NORMAL (SINGLE)

		for bampath in $(echo $TumorNormalData | sed "s/,/ /g")
		do
		
			TUMORNAME=$(echo "$bampath" | cut -d'#' -f1)
			NORMALNAME=$(echo "$bampath" | cut -d'#' -f2)

			mutect_cmd="./gatk --java-options "-Xmx4G" Mutect2 -R \"${REFERENCE}\" -I \"${OUTPUT_DIR}${TUMORNAME}.recal_reads.bam\" -I \"${OUTPUT_DIR}${NORMALNAME}.recal_reads.bam\" -normal \"${NORMALNAME}\" --germline-resource \"${germlineResource}\" --panel-of-normals \"${PON}\" -O \"${OUTPUT_DIR}${TUMORNAME}.${NORMALNAME}_mut.vcf.gz\""

			if grep -q "^${TUMORNAME}|" "$PIPELINE_STATUS_FILE" && grep "^${TUMORNAME}|" "$PIPELINE_STATUS_FILE" | grep -q "|${stage}"; then
				# If both sample and stage exist in the file
				echo "$TUMORNAME: $stage already done. Skipping Mutect2..."
			else


				echo "Mutect2 Command: $mutect_cmd"
				eval "$mutect_cmd"
				update_pipeline_status "$TUMORNAME" "$stage"
			fi
			#For pipelinecheck
		done
		#=====================================================================================================
		# 	BOTH				Mutect2: 02- TUMOR WITH MATCHED NORMAL (MULTIPLE)
		#=====================================================================================================
	elif [ "$somaticOP" = "TUMORM" ]; then   # TUMOR WITH MATCHED NORMAL (MULTIPLE)
		
		#Changed
		JOINT_MUT_VCF="${OUTPUT_DIR}joint_mut.vcf.gz"
		mutect_cmd="./gatk --java-options "-Xmx4G" Mutect2 -R "${REFERENCE}" --tmp-dir \"${TMP_DIR}\""
		
		if [ -n "$INTERVALS" ]; then
        	mutect_cmd="$mutect_cmd -L \"${INTERVALS}\" --interval-padding 100"
    	fi
		
		for bampath in $(echo $TumorNormalData | sed "s/,/ /g")
		do

 	    	TUMORNAME=$(echo "$bampath" | cut -d'#' -f1)
	    	NORMALNAME=$(echo "$bampath" | cut -d'#' -f2)
	    
			mutect_cmd="$mutect_cmd -I "${OUTPUT_DIR}""${TUMORNAME}.recal_reads.bam" -I "${OUTPUT_DIR}""${NORMALNAME}.recal_reads.bam" -normal "${NORMALNAME}""
		
		done

			if [ -n "$germlineResource" ]; then
        		mutect_cmd="$mutect_cmd --germline-resource \"${germlineResource}\""
    		fi
    		if [ -n "$PON" ]; then
        		mutect_cmd="$mutect_cmd --panel-of-normals \"${PON}\""
    		fi
			#mutect_cmd="$mutect_cmd --germline-resource "${germlineResource}" --panel-of-normals "${PON}" -O "${OUTPUT_DIR}""joint_mut.vcf.gz""

			mutect_cmd="$mutect_cmd -O \"$JOINT_MUT_VCF\""

			if grep -q "^${TUMORNAME}|" "$PIPELINE_STATUS_FILE" && grep "^${TUMORNAME}|" "$PIPELINE_STATUS_FILE" | grep -q "|${stage}"; then
				# If both sample and stage exist in the file
				echo "$TUMORNAME: $stage already done. Skipping Mutect2..."
			else

				echo "Mutect2 Command: $mutect_cmd"
				eval "$mutect_cmd"
				mutect_status=$?
				if [ $mutect_status -eq 0 ] && [ -s "$JOINT_MUT_VCF" ]; then
					if [ ! -f "${JOINT_MUT_VCF}.tbi" ]; then
            		./gatk IndexFeatureFile -I "$JOINT_MUT_VCF"
					fi
            		update_pipeline_status "$BAMFILE_NAME" "$stage"
        		else
            		echo "Error: Mutect2 failed or output VCF ($JOINT_MUT_VCF) is missing."
            		exit 1
        		fi
				#update_pipeline_status "$TUMORNAME" "$stage"
			fi
		#=====================================================================================================
		# 		BOTH			Mutect2: 03- TUMOR ONLY MODE
		#=====================================================================================================

	elif [ "$somaticOP" = "TUMORO" ]; then #TUMOR ONLY MODE

		for bampath in $(echo $BamFilesPath | sed "s/,/ /g")
		do
			filename=$(basename "$bampath") 
			BAMFILE_NAME=$(echo "$filename" | sed -E 's/(\.star\.Aligned\.sortedByCoord\.out)?\.bam$//') 
			mutect_cmd="./gatk --java-options "-Xmx4G" Mutect2 -R "${REFERENCE}" -I "${OUTPUT_DIR}""${BAMFILE_NAME}.recal_reads.bam" -O "${OUTPUT_DIR}""${BAMFILE_NAME}_mut.vcf.gz""

			if [ -n "$germlineResource" ]; then
			mutect_cmd="$mutect_cmd --germline-resource "${germlineResource}""
			fi

			if [ -n "$PON" ]; then
			mutect_cmd="$mutect_cmd --pon "${PON}""
			fi

			if grep -q "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" && grep "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" | grep -q "|${stage}"; then
				# If both sample and stage exist in the file
				echo "$BAMFILE_NAME: $stage already done. Skipping Mutect2..."
			else

				echo "Mutect2 Command: $mutect_cmd"
				eval "$mutect_cmd"
				update_pipeline_status "$BAMFILE_NAME" "$stage"
			fi

		done
		#=====================================================================================================
		# 		BOTH			Mutect2: 04- TUMOR ONLY MODE with Normals
		#=====================================================================================================

	elif [ "$somaticOP" = "TUMORON" ]; then #TUMOR ONLY MODE with Normals

		for bampath in $(echo $BamFilesPath | sed "s/,/ /g")
		do
			filename=$(basename "$bampath") 
			BAMFILE_NAME=$(echo "$filename" | sed -E 's/(\.star\.Aligned\.sortedByCoord\.out)?\.bam$//') 
			mutect_cmd="./gatk --java-options "-Xmx4G" Mutect2 -R "${REFERENCE}" -I "${OUTPUT_DIR}""${BAMFILE_NAME}.recal_reads.bam" -max-mnp-distance 0 -O "${OUTPUT_DIR}""${BAMFILE_NAME}_pon.vcf.gz""

			if [ -n "$germlineResource" ]; then
			mutect_cmd="$mutect_cmd --germline-resource "${germlineResource}""
			fi

			if grep -q "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" && grep "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" | grep -q "|${stage}"; then
				# If both sample and stage exist in the file
				echo "$BAMFILE_NAME: $stage already done. Skipping Mutect2..."
			else

				echo "Mutect2 Command: $mutect_cmd"
				eval "$mutect_cmd"
				update_pipeline_status "$BAMFILE_NAME" "$stage"
			fi

		done
		# Build sample map for GenomicsDBImport
		sample_map="${OUTPUT_DIR}pon_sample_map.txt"
		rm -f "$sample_map"
		for vcf in $(find "$OUTPUT_DIR" -name "*_pon.vcf.gz"); do
			sample=$(basename "$vcf" | cut -d'_' -f1)
			echo "$sample $vcf" >> "$sample_map"
		done

		genomicsdb_dir="${OUTPUT_DIR}pon_db"
		echo "Running GenomicsDBImport..."

			./gatk --java-options "-Xmx4g -Xms4g" GenomicsDBImport --sample-name-map "$sample_map" --genomicsdb-workspace-path "$genomicsdb_dir" 

		pon_output="${OUTPUT_DIR}PON.vcf.gz"
		./gatk CreateSomaticPanelOfNormals -R "$REFERENCE" -V gendb://"$genomicsdb_dir" -O "$pon_output"

		#=====================================================================================================
		# 		BOTH			Mutect2: 05- TUMOR ONLY MODE with PON
		#=====================================================================================================

	elif [ "$somaticOP" = "TUMOROPON" ]; then #TUMOR ONLY MODE with PON
		pon_output="${OUTPUT_DIR}PON.vcf.gz"

		#======= First creating PON from Normals
		for bampath in $(echo $TumorNormalData | sed "s/,/ /g")
		do
			
				TUMORNAME=$(echo "$bampath" | cut -d'#' -f1)
				NORMALNAME=$(echo "$bampath" | cut -d'#' -f2)

			mutect_cmd="./gatk --java-options "-Xmx4G" Mutect2 -R "${REFERENCE}" -I "${OUTPUT_DIR}""${NORMALNAME}.recal_reads.bam" -max-mnp-distance 0 -O "${OUTPUT_DIR}""${NORMALNAME}_pon.vcf.gz""

			if [ -n "$germlineResource" ]; then
			mutect_cmd="$mutect_cmd --germline-resource \"${germlineResource}\""
			fi


			if grep -q "^${NORMALNAME}|" "$PIPELINE_STATUS_FILE" && grep "^${NORMALNAME}|" "$PIPELINE_STATUS_FILE" | grep -q "|${stage}"; then
				# If both sample and stage exist in the file
				echo "$BAMFILE_NAME: $stage already done. Skipping Mutect2..."
			else
				echo "Running Mutect2 on normal sample: $NORMALNAME"
				echo "Mutect2 Command: $mutect_cmd"
				eval "$mutect_cmd"
				update_pipeline_status "$BAMFILE_NAME" "$stage"
			fi

		done
		#======= Build sample map for GenomicsDBImport
		sample_map="${OUTPUT_DIR}pon_sample_map.txt"
		rm -f "$sample_map"
		for vcf in $(find "$OUTPUT_DIR" -name "*_pon.vcf.gz"); do
			sample=$(basename "$vcf" | cut -d'_' -f1)
			echo "$sample $vcf" >> "$sample_map"
		done

		genomicsdb_dir="${OUTPUT_DIR}pon_db"
		echo "Running GenomicsDBImport..."

			./gatk --java-options "-Xmx4g -Xms4g" GenomicsDBImport --sample-name-map "$sample_map" --genomicsdb-workspace-path "$genomicsdb_dir" 

			./gatk CreateSomaticPanelOfNormals -R "$REFERENCE" -V gendb://"$genomicsdb_dir" -O "$pon_output"

		#========= Second running Tumor only on tumors with above PON
		for bampath in $(echo $TumorNormalData | sed "s/,/ /g")
		do
			
				TUMORNAME=$(echo "$bampath" | cut -d'#' -f1)
				NORMALNAME=$(echo "$bampath" | cut -d'#' -f2)


		mutect_cmd="./gatk --java-options "-Xmx4G" Mutect2 -R "${REFERENCE}" -I "${OUTPUT_DIR}""${TUMORNAME}.recal_reads.bam" -O "${OUTPUT_DIR}""${TUMORNAME}_mut.vcf.gz""

		if [ -n "$germlineResource" ]; then
		mutect_cmd="$mutect_cmd --germline-resource \"${germlineResource}\""
		fi

		# Use generated PON file
		if [ -f "$pon_output" ]; then
			mutect_cmd="$mutect_cmd --pon \"$pon_output\""
		else
				echo "Error: PON file not found at $pon_output"
				exit 1
		fi

		if grep -q "^${TUMORNAME}|" "$PIPELINE_STATUS_FILE" && grep "^${TUMORNAME}|" "$PIPELINE_STATUS_FILE" | grep -q "|${stage}"; then
			# If both sample and stage exist in the file
			echo "$BAMFILE_NAME: $stage already done. Skipping Mutect2..."
		else

			echo "Running Mutect2 on tumor sample: $TUMORNAME"
			echo "Mutect2 Command: $mutect_cmd"
			eval "$mutect_cmd"
			update_pipeline_status "$BAMFILE_NAME" "$TUMORNAME"
		fi

		done
		#=====================================================================================================
		# 		BOTH			Mutect2: 06- Mitochondrial mode
		#=====================================================================================================

	elif [ "$somaticOP" = "TUMORMIT" ]; then # Mitochondrial mode

		for bampath in $(echo $BamFilesPath | sed "s/,/ /g")
		do
			filename=$(basename "$bampath") 
			BAMFILE_NAME=$(echo "$filename" | sed -E 's/(\.star\.Aligned\.sortedByCoord\.out)?\.bam$//') 

			mutect_cmd="./gatk --java-options "-Xmx4G" Mutect2 -R "${REFERENCE}" -L "${genomicInterval}" --mitochondria-mode -I "${OUTPUT_DIR}""${BAMFILE_NAME}.recal_reads.bam" -O "${OUTPUT_DIR}""${BAMFILE_NAME}_mut.vcf.gz""

			if grep -q "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" && grep "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" | grep -q "|${stage}"; then
				# If both sample and stage exist in the file
				echo "$BAMFILE_NAME: $stage already done. Skipping Mutect2..."
			else
				echo "Mutect2 Command: $mutect_cmd"
				eval "$mutect_cmd"
				update_pipeline_status "$BAMFILE_NAME" "$stage"
			fi
		done
		#=====================================================================================================
		# 		BOTH			Mutect2: 07- Force calling mode
		#=====================================================================================================

	elif [ "$somaticOP" = "TUMORFOR" ]; then #Force calling mode

		for bampath in $(echo $BamFilesPath | sed "s/,/ /g")
		do
			filename=$(basename "$bampath") 
			BAMFILE_NAME=$(echo "$filename" | sed -E 's/(\.star\.Aligned\.sortedByCoord\.out)?\.bam$//') 

			if [ -n "$TumorNormalData" ]; then
			mutect_cmd="./gatk --java-options "-Xmx4G" Mutect2 -R \"${REFERENCE}\" -I \"${OUTPUT_DIR}${BAMFILE_NAME}.recal_reads.bam\" -alleles \"${TumorNormalData}\" -O \"${OUTPUT_DIR}${BAMFILE_NAME}_mut.vcf.gz\""
			fi
			if [ -n "$germlineResource" ]; then
			mutect_cmd="./gatk --java-options "-Xmx4G" Mutect2 -R \"${REFERENCE}\" -I \"${OUTPUT_DIR}${BAMFILE_NAME}.recal_reads.bam\" --f1r2-tar-gz \"${germlineResource}\" -O \"${OUTPUT_DIR}${BAMFILE_NAME}_mut.vcf.gz\""
			fi


			if grep -q "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" && grep "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" | grep -q "|${stage}"; then
				# If both sample and stage exist in the file
				echo "$BAMFILE_NAME: $stage already done. Skipping Mutect2..."
			else
			echo "Mutect2 Command: $mutect_cmd"
			eval "$mutect_cmd"
			update_pipeline_status "$BAMFILE_NAME" "$stage"
			fi

		done

	fi

	echo "++++++++++++++++++++++++++++++++++++ Mutect2 COMPLETED +++++++++++++++++++++++++++++++++++++++++"

fi #end BOTH


#############################################################################################################################
# STEP 8: VARIANT FILTRATION OPTIONS
#############################################################################################################################
# ALL, BA

echo "VARIANT FILTRATION OPTION SELECTED IS: $variantFilterationOp"

if [ "$variantFilterationOp" = "ALL" ]; then
	##################################
	# ALL Start: 
	##################################
	# ALL:STEP 9.1: VariantFiltration BEFORE: Filter variant calls based on INFO and/or FORMAT annotations
	# ------------------------------------
	# Germline _hap.vcf.gz --> _HVFB.vcf
	# Mutect2  _mut.vcf.gz --> _MVFB.vcf

	echo "Running VariantFilteration ..."

	#Removing starting and ending double qoutes
	filterOptionsB=$(echo "$filterOptionsB" | sed 's/^"//; s/"$//')

	for vcfFile in "$OUTPUT_DIR"*_mut.vcf.gz "$OUTPUT_DIR"*_hap.vcf.gz; do

		if [ -f "$vcfFile" ]; then 	
			filename=$(basename "$vcfFile") 
			BAMFILE_NAME=$(echo "$filename" | sed -E 's/(_mut|_hap)\.vcf\.gz$//')

			case "$vcfFile" in
				*_mut.vcf.gz)
					if grep -q "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" && grep "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" | grep -q "|VariantFiltration_M"; then
						echo "$BAMFILE_NAME: FilterMutectCalls (VariantFiltration_M) already done. Skipping Variant Filtration ..."
										
					elif [ "$germSomaticOP" = "SOMATIC" ] || [ "$germSomaticOP" = "BOTH" ]; then

						F1R2_FILE="${OUTPUT_DIR}${BAMFILE_NAME}_f1r2.tar.gz"
                    	ORIENT_MODEL="${OUTPUT_DIR}${BAMFILE_NAME}_read_orientation_model.tar.gz"
                    	PILEUP_TABLE="${OUTPUT_DIR}${BAMFILE_NAME}_getpileupsummaries.table"
        				CONTAM_TABLE="${OUTPUT_DIR}${BAMFILE_NAME}_contamination.table"
						OUT_MVFB="${OUTPUT_DIR}${BAMFILE_NAME}_MVFB.vcf"

						echo "Step 1: LearnReadOrientationModel for $BAMFILE_NAME "
                    	orient_cmd="./gatk --java-options \"-Xms4G -Xmx16G\" LearnReadOrientationModel -I \"${F1R2_FILE}\" -O \"${ORIENT_MODEL}\""
                    	echo "Command: $orient_cmd"
                    	eval $orient_cmd
                    
						if [ $? -ne 0 ]; then
							echo "Error: LearnReadOrientationModel failed for $BAMFILE_NAME"
							exit 1
						fi

                    	echo "Step 2: FilterMutectCalls for $BAMFILE_NAME "
                    	filterVariant_cmd="./gatk --java-options \"-Xms4G -Xmx16G\" FilterMutectCalls -R \"${REFERENCE}\" -V \"${vcfFile}\" --ob-priors \"${ORIENT_MODEL}\" -O \"${OUT_MVFB}\""
						
						echo "FilterMutectCalls Command is : $filterVariant_cmd"
						eval $filterVariant_cmd
						if [ $? -ne 0 ]; then
							echo "Error: FilterMutectCalls failed for $BAMFILE_NAME"
							exit 1
						else
							update_pipeline_status "$BAMFILE_NAME" "VariantFiltration_M"
						fi
						

					fi
					
					;;
				*_hap.vcf.gz)
				
					if grep -q "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" && grep "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" | grep -q "|VariantFiltration_H"; then
						# If both sample and stage exist in the file
						echo "$BAMFILE_NAME: VariantFiltration_H already done. Skipping VariantFiltration..."
				
					elif [ "$germSomaticOP" = "GERMLINE" ] || [ "$germSomaticOP" = "BOTH" ]; then
						#\"-Xms2G -Xmx4G\"
						filterVariant_cmd="./gatk --java-options \"-Djava.awt.headless=true -Xms1G -Xmx3G -XX:+UseG1GC\"  VariantFiltration -R "${REFERENCE}" -V "${vcfFile}" -O "${OUTPUT_DIR}""${BAMFILE_NAME}_HVFB.vcf""

						if [ -z "$filterOptionsB" ]; then
							
							filterVariant_cmd="$filterVariant_cmd --filter-expression \"vc.getAttribute('BaseQRankSum') < -2.0 || vc.getAttribute('MQRankSum') < -2.0\" --filter-name "LowBaseQRankSum" "
						else
							filterVariant_cmd="$filterVariant_cmd $filterOptionsB\"" #ADDED QUOTE AT END
						fi

						echo "VariantFiltration Command is : $filterVariant_cmd"
						eval $filterVariant_cmd
						if [ $? -ne 0 ]; then
							echo "Error: VariantFiltration failed for $BAMFILE_NAME"
							exit 1
						else
							update_pipeline_status "$BAMFILE_NAME" "VariantFiltration_H"
							sleep 2
						fi
						
					fi
					
			;;
			esac

		fi
	done
	echo "++++++++++++++++++++++++++++++++++++ VariantFiltration Before COMPLETED +++++++++++++++++++++++++++++++++++++++++"


	##################################
	# ALL: STEP 9.2: SelectVariants: Select a subset of variants from a VCF file
	# ------------------------------------
	#  germline ( _HVFB.vcf ---> _hselvar.vcf )
	#  somatic  ( _MVFB.vcf ---> _mselvar.vcf )

	echo "Running SelectVariants..."

	#Removing starting and ending double qoutes
	selectVariantStr=$(echo "$selectVariantStr" | sed 's/^"//; s/"$//')
	for vcfFile in "$OUTPUT_DIR"*_MVFB.vcf "$OUTPUT_DIR"*_HVFB.vcf; do
		selectVariant_cmd=""

		if [ -s "$vcfFile" ]; then 	
			filename=$(basename "$vcfFile") 
			BAMFILE_NAME=$(echo "$filename" | sed -E 's/(_MVFB|_HVFB)\.vcf$//')

			case "$vcfFile" in
				*_MVFB.vcf)
					
						if grep -q "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" && grep "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" | grep -q "|SelectVariant_M"; then
							# If both sample and stage exist in the file
							echo "$BAMFILE_NAME: SelectVariant_M already done. Skipping SelectVariant..."
						
						elif [ "$germSomaticOP" = "SOMATIC" ] || [ "$germSomaticOP" = "BOTH" ]; then
							selectVariant_cmd="./gatk --java-options "-Xmx4G"  SelectVariants -R "${REFERENCE}" -V "${vcfFile}" -O "${OUTPUT_DIR}""${BAMFILE_NAME}_mselvar.vcf""

							selectVariant_cmd="$selectVariant_cmd $selectVariantStr  --exclude-filtered true"
							echo "SelectVariants Command: $selectVariant_cmd"
							eval $selectVariant_cmd
							if [ $? -ne 0 ]; then
								echo "Error: SelectVariants failed for $BAMFILE_NAME"
								exit 1
							else
								update_pipeline_status "$BAMFILE_NAME" "SelectVariant_M"
							fi

						fi
					
					;;
				*_HVFB.vcf)
				
					# Skip if joint haplotype file exists and this is not the joint file
					if [ -f "$joint_file" ] && [ "$vcfFile" != "$joint_file" ]; then
						echo "Skipping $vcfFile because $joint_file exists."
						continue
					fi

					# For all vcf one by one
					if grep -q "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" && grep "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" | grep -q "|SelectVariant_H"; then
						# If both sample and stage exist in the file
						echo "$BAMFILE_NAME: SelectVariant_H already done. Skipping SelectVariant..."
					
					elif [ "$germSomaticOP" = "GERMLINE" ] || [ "$germSomaticOP" = "BOTH" ]; then
					
						selectVariant_cmd="./gatk --java-options \"-Xms2G -Xmx4G\"  SelectVariants -R "${REFERENCE}" -V "${vcfFile}" -O "${OUTPUT_DIR}${BAMFILE_NAME}_hselvar.vcf""
						selectVariant_cmd="$selectVariant_cmd $selectVariantStr  --exclude-filtered true"
						echo "SelectVariant Command: $selectVariant_cmd"

						eval $selectVariant_cmd
						if [ $? -ne 0 ]; then
							echo "Error: SelectVariants failed for $BAMFILE_NAME"
							exit 1
						else
							update_pipeline_status "$BAMFILE_NAME" "SelectVariant_H"
						fi

					fi
					
					;;
			esac

		fi
	done
	echo "++++++++++++++++++++++++++++++++++++ SelectVariants COMPLETED +++++++++++++++++++++++++++++++++++++++++"
	
	##################################
	# ALL: STEP 9.3: Annotation: snpEff, VEP, Both

	# snpEFF ONLY
	if [ "$annotationOPtion" = "snpeff" ]; then
		# ------------------------------------
		#  germline ( _hselvar.vcf ---> .annH.vcf ), VEP _vep_annH.vcf
		#  somatic  ( _mselvar.vcf ---> .annM.vcf ), VEP _vep_annM.vcf
		
		#First check database available or not and then install
		# Check if the database is already installed
		echo "Checking snpEff database..."
		if java -Xmx8g -jar $snpEff_path databases -v | grep -wq "$snpEff_db"; then
			echo "$snpEff_db is already installed"
		else
			# If the database is not installed, download it
			echo "Installing $snpEff_db ..."
			java -Xmx8g -jar $snpEff_path download -v "$snpEff_db"
			if [ $? -ne 0 ]; then
				echo "Error: snpEff database $snpEff_db download failed"
				exit 1
			else
				echo "$snpEff_db download completed"
			fi
			
		fi
		echo "Running snpEff..."

		for vcfFile in "$OUTPUT_DIR"*_mselvar.vcf "$OUTPUT_DIR"*_hselvar.vcf; do

			if [ -f "$vcfFile" ]; then 	
				filename=$(basename "$vcfFile") 
				BAMFILE_NAME=$(echo "$filename" | sed -E 's/(_mselvar|_hselvar)\.vcf$//')
				case "$vcfFile" in
					*_mselvar.vcf) 
					if grep -q "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" && grep "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" | grep -q "|snpEff_M"; then
						# If both sample and stage exist in the file
						echo "$BAMFILE_NAME: snpEff_M already done. Skipping snpEff..."
					
					elif [ "$germSomaticOP" = "SOMATIC" ] || [ "$germSomaticOP" = "BOTH" ]; then
						echo "Annotating SOMATIC variant: $filename"
					
						java -Xmx8g -jar "$snpEff_path" -q -canon -csvStats "${OUTPUT_DIR}${BAMFILE_NAME}.annM.csv" -stats "${OUTPUT_DIR}${BAMFILE_NAME}.annM.html" "$snpEff_db" "$vcfFile" > "${OUTPUT_DIR}${BAMFILE_NAME}.annM.vcf"
						if [ $? -ne 0 ]; then
							echo "Error: snpEff failed for $BAMFILE_NAME"
							exit 1
						else
							update_pipeline_status "$BAMFILE_NAME" "snpEff_M"
						fi
							
					fi
					;;
					*_hselvar.vcf) 
					if grep -q "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" && grep "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" | grep -q "|snpEff_H"; then
						# If both sample and stage exist in the file
						echo "$BAMFILE_NAME: snpEff_H already done. Skipping snpEff..."
					elif [ "$germSomaticOP" = "GERMLINE" ] || [ "$germSomaticOP" = "BOTH" ]; then
						echo "Annotating GERMLINE variant: $filename"
					
						java -Xmx4g -jar "$snpEff_path" -q -canon -csvStats "${OUTPUT_DIR}${BAMFILE_NAME}.annH.csv" -stats "${OUTPUT_DIR}${BAMFILE_NAME}.annH.html" "$snpEff_db" "$vcfFile" > "${OUTPUT_DIR}${BAMFILE_NAME}.annH.vcf"
						if [ $? -ne 0 ]; then
							echo "Error: snpEff failed for $BAMFILE_NAME"
							exit 1
						else
							update_pipeline_status "$BAMFILE_NAME" "snpEff_H"
						fi
						
					fi
				;;
				esac
			fi
		done

		echo "snpEff finished. All ann.vcf files generated."
		vcfFiles="$OUTPUT_DIR*.annM.vcf $OUTPUT_DIR*.annH.vcf" # For snpSIFT
	elif [ "$annotationOPtion" = "vep" ]; then
		echo "Running VEP..."
		cd $VEP_PATH
		for vcfFile in "$OUTPUT_DIR"*_mselvar.vcf "$OUTPUT_DIR"*_hselvar.vcf; do
			if [ -f "$vcfFile" ]; then 	
				filename=$(basename "$vcfFile") 
				BAMFILE_NAME=$(echo "$filename" | sed -E 's/(_mselvar|_hselvar)\.vcf$//')

				export PATH="/usr/bin:$PATH"

				case "$vcfFile" in
					*_mselvar.vcf)
					#Check for File already exist or Not
					if grep -q "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" && grep "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" | grep -q "|VEP_M"; then
						# If both sample and stage exist in the file
						echo "$BAMFILE_NAME: VEP_M already done. Skipping VEP..."
					elif [ "$germSomaticOP" = "SOMATIC" ] || [ "$germSomaticOP" = "BOTH" ]; then
							# Run VEP after variant filtering
							vep_cmd="perl vep --input_file \"${vcfFile}\" --output_file \"${OUTPUT_DIR}${BAMFILE_NAME}_vep_annM.vcf\" \
								--cache --dir_cache \"$VEP_PATH/.vep\" --fasta \"$REFERENCE\" --vcf --offline \
								--assembly $vepAssembly --species $vepSpecies --sift b --polyphen b \
								--canonical --symbol --af --af_gnomad --af_1kg \
								--gene_phenotype --regulatory --numbers --biotype --verbose --force_overwrite"
							
							echo "VEP Annotation Command: $vep_cmd"
							eval $vep_cmd
							if [ $? -ne 0 ]; then
									echo "Error: VEP failed for $BAMFILE_NAME"
									exit 1
							else 
								update_pipeline_status "$BAMFILE_NAME" "VEP_M"
							fi
							
						
					fi
						;;
					*_hselvar.vcf)
					#Check for File already exist or Not
					if grep -q "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" && grep "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" | grep -q "|VEP_H"; then
						# If both sample and stage exist in the file
						echo "$BAMFILE_NAME: VEP_H already done. Skipping VEP..."
					elif [ "$germSomaticOP" = "GERMLINE" ] || [ "$germSomaticOP" = "BOTH" ]; then
				
							# Run VEP after variant filtering
							vep_cmd="perl vep --input_file \"${vcfFile}\" --output_file \"${OUTPUT_DIR}${BAMFILE_NAME}_vep_annH.vcf\" \
							--cache --dir_cache \"$VEP_PATH/.vep\" --fasta \"$REFERENCE\" --vcf --offline \
							--assembly $vepAssembly --species $vepSpecies --sift b --polyphen b \
							--canonical --symbol --af --af_gnomad --af_1kg  \
							--gene_phenotype --regulatory --numbers --biotype --verbose --force_overwrite"
						
							echo "VEP Annotation Command: $vep_cmd"
							eval $vep_cmd
							if [ $? -ne 0 ]; then
									echo "Error: VEP failed for $BAMFILE_NAME"
									exit 1
							else 
								update_pipeline_status "$BAMFILE_NAME" "VEP_H"
							fi
						
					fi
					;;
				esac

			fi
		done
		vcfFiles="$OUTPUT_DIR*_vep_annM.vcf $OUTPUT_DIR*_vep_annH.vcf"
		echo "VEP finished. All *_vep_ann.vcf files generated." 

	elif [ "$annotationOPtion" = "both" ]; then

		# ------------------------------------
		#  germline ( _hselvar.vcf ---> .annH.vcf )
		#  somatic  ( _mselvar.vcf ---> .annM.vcf )
		
		#First check database available or not and then install
		# Check if the database is already installed
	
		echo "Checking snpEff database..."
		if java -Xmx8g -jar $snpEff_path databases -v | grep -wq "$snpEff_db"; then
			echo "$snpEff_db is already installed"
		else
			# If the database is not installed, download it
			echo "Installing $snpEff_db ..."
			java -Xmx8g -jar $snpEff_path download -v "$snpEff_db"
			if [ $? -ne 0 ]; then
				echo "Error: snpEff database $snpEff_db download failed"
				exit 1
			else
				echo "$snpEff_db download completed"
			fi
			
		fi
		echo "Running snpEff..."


		for vcfFile in "$OUTPUT_DIR"*_mselvar.vcf "$OUTPUT_DIR"*_hselvar.vcf; do
		
			if [ -f "$vcfFile" ]; then 	
			
				filename=$(basename "$vcfFile") 
				BAMFILE_NAME=$(echo "$filename" | sed -E 's/(_mselvar|_hselvar)\.vcf$//')

				case "$vcfFile" in
					*_mselvar.vcf) 
					if grep -q "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" && grep "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" | grep -q "|snpEff_M"; then
						# If both sample and stage exist in the file
						echo "$BAMFILE_NAME: snpEff_M already done. Skipping snpEff..."
					elif [ "$germSomaticOP" = "SOMATIC" ] || [ "$germSomaticOP" = "BOTH" ]; then
						echo "Annotating SOMATIC variant: $filename"
						java -Xmx8g -jar "$snpEff_path" -q -canon -csvStats "${OUTPUT_DIR}${BAMFILE_NAME}.annM.csv" -stats "${OUTPUT_DIR}${BAMFILE_NAME}.annM.html" "$snpEff_db" "$vcfFile" > "${OUTPUT_DIR}${BAMFILE_NAME}.annM.vcf"
						if [ $? -ne 0 ]; then
							echo "Error: snpEff failed for $BAMFILE_NAME"
							exit 1
						else
							update_pipeline_status "$BAMFILE_NAME" "snpEff_M"
						fi
							
					fi
					;;
					*_hselvar.vcf) 

					if grep -q "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" && grep "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" | grep -q "|snpEff_H"; then
						# If both sample and stage exist in the file
						echo "$BAMFILE_NAME: snpEff_H already done. Skipping snpEff..."
					elif [ "$germSomaticOP" = "GERMLINE" ] || [ "$germSomaticOP" = "BOTH" ]; then
						echo "Annotating GERMLINE variant: $filename"
						java -Xmx8g -jar "$snpEff_path" -q -canon -csvStats "${OUTPUT_DIR}${BAMFILE_NAME}.annH.csv" -stats "${OUTPUT_DIR}${BAMFILE_NAME}.annH.html" "$snpEff_db" "$vcfFile" > "${OUTPUT_DIR}${BAMFILE_NAME}.annH.vcf"
						if [ $? -ne 0 ]; then
							echo "Error: snpEff failed for $BAMFILE_NAME"
							exit 1
						else
							update_pipeline_status "$BAMFILE_NAME" "snpEff_H"
						fi
					fi
					;;
				esac
			fi
		done
	
		echo "snpEff finished. All ann.vcf files generated."

		echo "Running VEP..."
		cd $VEP_PATH

		for vcfFile in "$OUTPUT_DIR"*.annH.vcf "$OUTPUT_DIR"*.annM.vcf; do

			if [ -f "$vcfFile" ]; then 	
				filename=$(basename "$vcfFile") 
				BAMFILE_NAME=$(echo "$filename" | sed -E 's/(.annH|.annM)\.vcf$//')
				export PATH="/usr/bin:$PATH"

				case "$vcfFile" in
					*.annM.vcf)
						
					if grep -q "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" && grep "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" | grep -q "|VEP_M"; then
							# If both sample and stage exist in the file
							echo "$BAMFILE_NAME: VEP_M already done. Skipping VEP..."
					else
						if [ "$germSomaticOP" = "SOMATIC" ] || [ "$germSomaticOP" = "BOTH" ]; then
					
							# Run VEP after variant filtering
							vep_cmd="perl vep --input_file \"${vcfFile}\" --output_file \"${OUTPUT_DIR}${BAMFILE_NAME}_vep_annM.vcf\" --cache --dir_cache \"$VEP_PATH/.vep\" --fasta \"$REFERENCE\" --vcf --offline --assembly $vepAssembly --species $vepSpecies --sift b --polyphen b --canonical --symbol --af --af_gnomad --af_1kg  --gene_phenotype --regulatory --numbers --biotype --verbose --force_overwrite"
							echo "VEP Annotation Command: $vep_cmd"
							eval $vep_cmd
							if [ $? -ne 0 ]; then
								echo "Error: VEP failed for $BAMFILE_NAME"
								exit 1
							else
								update_pipeline_status "$BAMFILE_NAME" "VEP_M"
							fi
							
						fi
					fi
						;;
					*.annH.vcf)
					if grep -q "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" && grep "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" | grep -q "|VEP_H"; then
							# If both sample and stage exist in the file
							echo "$BAMFILE_NAME: VEP_H already done. Skipping VEP..."
					else
						if [ "$germSomaticOP" = "GERMLINE" ] || [ "$germSomaticOP" = "BOTH" ]; then
						
							# Run VEP after variant filtering
							vep_cmd="perl vep --input_file \"${vcfFile}\" --output_file \"${OUTPUT_DIR}${BAMFILE_NAME}_vep_annH.vcf\" --cache --dir_cache \"$VEP_PATH/.vep\" --fasta \"$REFERENCE\" --vcf --offline --assembly $vepAssembly --species $vepSpecies --sift b --polyphen b --canonical --symbol --af --af_gnomad --af_1kg  --gene_phenotype --regulatory --numbers --biotype --verbose --force_overwrite"
								
							echo "VEP Annotation Command: $vep_cmd"
							eval $vep_cmd
							if [ $? -ne 0 ]; then
								echo "Error: VEP failed for $BAMFILE_NAME"
								exit 1
							else
								update_pipeline_status "$BAMFILE_NAME" "VEP_H"
							fi
						fi
					fi
						;;
				esac

			fi
		done
		echo "VEP finished. All *_vep_ann.vcf files generated." 
		vcfFiles="$OUTPUT_DIR*.annM.vcf $OUTPUT_DIR*.annH.vcf $OUTPUT_DIR*_vep_annM.vcf $OUTPUT_DIR*_vep_annH.vcf"
	fi
	#annotationOption-END

	##################################
	# ALL: STEP 9.: Filteration: snpSIFT

	echo "Performing SnpSIFT annotation & filtering ..."
	#CorrectionDone
	FILTER_LOG_FILE="${OUTPUT_DIR}/filtration_counts.csv"
	echo "Sample,Pipeline_Stage,Raw_Variants,Filtered_Variants,Removed_Variants,Retention_Pct" > "$FILTER_LOG_FILE"

	RESOLVED_GNOMAD=""
	if [ -n "$ANNOT_GNOMAD" ]; then
		if [ -d "$ANNOT_GNOMAD" ]; then
			echo "gnomAD directory detected. Building file list..."
			RESOLVED_GNOMAD=$(find "$ANNOT_GNOMAD" -maxdepth 1 -name "*.vcf.gz" | paste -sd, -)
		elif [ -f "$ANNOT_GNOMAD" ]; then
			RESOLVED_GNOMAD="$ANNOT_GNOMAD"
		fi
	fi

	# 1B. Construct space-separated database argument for SnpSift
	# 1B. Build the -tabix options string
	SNPSIFT_DBS=""
	[ -n "$RESOLVED_GNOMAD" ] && SNPSIFT_DBS="$SNPSIFT_DBS $RESOLVED_GNOMAD"
	[ -f "$ANNOT_DBSNP" ]     && SNPSIFT_DBS="$SNPSIFT_DBS $ANNOT_DBSNP"
	[ -f "$ANNOT_CLINVAR" ]   && SNPSIFT_DBS="$SNPSIFT_DBS $ANNOT_CLINVAR"

	# Trim leading spaces
	SNPSIFT_DBS=$(echo "$SNPSIFT_DBS" | xargs)

	# Log detected databases
	echo "SnpSift Database Targets: ${SNPSIFT_DBS:-None specified}"

	for vcfFile in $vcfFiles; do
		if [ -f "$vcfFile" ]; then 
			filename=$(basename "$vcfFile") 

			case "$filename" in
				*_vep_annM.vcf)
					BAMFILE_NAME=$(echo "$filename" | sed 's/_vep_annM\.vcf$//')
					if grep -q "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" && grep "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" | grep -q "|SnpSIFT(VEP)_M"; then
						# If both sample and stage exist in the file
						echo "$BAMFILE_NAME: SnpSIFT(VEP)_M already done. Skipping SNPSIFT..."
				
					elif [ "$germSomaticOP" = "SOMATIC" ] || [ "$germSomaticOP" = "BOTH" ]; then
						echo "Annotating SOMATIC variant (EXON): $filename"
						
						ACTIVE_FILTER=$(get_active_filter "$filterOptionsA" "VEP")
						# 1. Count raw variants before filtering
                    	RAW_COUNT=$(grep -vc "^#" "$vcfFile")
						
						# --- STEP A: INLINE ANNOTATION ---
                    	ANNOT_VCF="${OUTPUT_DIR}${BAMFILE_NAME}_vep_annM.annot.vcf"
						# --- STEP A: INLINE ANNOTATION ---
						if [ -n "$SNPSIFT_DBS" ]; then
							CURRENT_INPUT="$vcfFile"
							STEP_NUM=1

							for db in $SNPSIFT_DBS; do
								echo "Running SnpSift annotate with DB: $db"
							
								# Unique temporary output for each database pass
								TMP_OUT="${OUTPUT_DIR}${BAMFILE_NAME}_tmp_pass_${STEP_NUM}.vcf"
								echo "Running command: java -Xmx4g -jar $snpSift annotate -tabix $db $CURRENT_INPUT > $TMP_OUT"
								java -Xmx4g -jar "$snpSift" annotate -tabix "$db" "$CURRENT_INPUT" > "$TMP_OUT"
							
								# Check if SnpSift succeeded AND created a non-empty output
								if [ $? -ne 0 ] || [ ! -s "$TMP_OUT" ]; then
									echo "Error: SnpSIFT Annotation failed or produced empty file for $BAMFILE_NAME on DB $db"
									rm -f "$TMP_OUT"
									exit 1
								fi

								# Remove previous pass temporary file (unless it was the original input)
								if [ "$CURRENT_INPUT" != "$vcfFile" ]; then
									rm -f "$CURRENT_INPUT"
								fi

								# Advance input to the newly created VCF pass
								CURRENT_INPUT="$TMP_OUT"
								STEP_NUM=$((STEP_NUM + 1))
							done
						
							# Move the final non-empty pass to your target annotated VCF name
							mv "$CURRENT_INPUT" "$ANNOT_VCF"
						else
							echo "No annotation databases provided. Skipping SnpSift annotate stage..."
							cp "$vcfFile" "$ANNOT_VCF"
						fi
						# --- STEP B: FILTER ANNOTATED VCF ---
						echo "java -Xmx8g -jar $snpSift filter $ACTIVE_FILTER $ANNOT_VCF >  ${OUTPUT_DIR}${BAMFILE_NAME}_vep_annM.sift.vcf"
						java -Xmx8g -jar "$snpSift" filter "$ACTIVE_FILTER" "$ANNOT_VCF" >  "${OUTPUT_DIR}${BAMFILE_NAME}_vep_annM.sift.vcf"
												
						if [ $? -ne 0 ]; then
							echo "Error: SnpSIFT failed for $BAMFILE_NAME"
							exit 1
						else
							# 2. Count variants after SnpSift filtering
                        	OUT_VCF="${OUTPUT_DIR}${BAMFILE_NAME}_vep_annM.sift.vcf"
                        	FILTERED_COUNT=$(grep -vc "^#" "$OUT_VCF")
                        	REMOVED_COUNT=$((RAW_COUNT - FILTERED_COUNT))
                            RETENTION_PCT="0.00"
                        	if [ "$RAW_COUNT" -gt 0 ]; then
                            	RETENTION_PCT=$(awk -v f="$FILTERED_COUNT" -v r="$RAW_COUNT" 'BEGIN {printf "%.2f", (f/r)*100}')
                        	fi
	                        # 3. Append counts to CSV log
                        	echo "${BAMFILE_NAME},Somatic_VEP,${RAW_COUNT},${FILTERED_COUNT},${REMOVED_COUNT},${RETENTION_PCT}\%" >> "$FILTER_LOG_FILE"
							update_pipeline_status "$BAMFILE_NAME" "SnpSIFT(VEP)_M"
							rm -f "$ANNOT_VCF" # Optional: remove intermediate annotated file to save disk space
						fi
						
					fi
					
							;;
				*_vep_annH.vcf)

					BAMFILE_NAME=$(echo "$filename" | sed 's/_vep_annH\.vcf$//')
					if grep -q "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" && grep "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" | grep -q "|SnpSIFT(VEP)_H"; then
						# If both sample and stage exist in the file
						echo "$BAMFILE_NAME: SnpSIFT(VEP)_H already done. Skipping SNPSIFT..."
					elif [ "$germSomaticOP" = "GERMLINE" ] || [ "$germSomaticOP" = "BOTH" ]; then
						echo "Annotating GERMLINE variant: $filename"
						
						ACTIVE_FILTER=$(get_active_filter "$filterOptionsA" "VEP")
						# 1. Count raw variants before filtering
                    	RAW_COUNT=$(grep -vc "^#" "$vcfFile")
						
						# --- STEP A: INLINE ANNOTATION ---
						ANNOT_VCF="${OUTPUT_DIR}${BAMFILE_NAME}_vep_annH.annot.vcf"
						if [ -n "$SNPSIFT_DBS" ]; then
							CURRENT_INPUT="$vcfFile"
							STEP_NUM=1

							for db in $SNPSIFT_DBS; do
								echo "Running SnpSift annotate with DB: $db"
							
								# Unique temporary output for each database pass
								TMP_OUT="${OUTPUT_DIR}${BAMFILE_NAME}_tmp_pass_${STEP_NUM}.vcf"
								echo "Running command: java -Xmx4g -jar $snpSift annotate -tabix $db $CURRENT_INPUT > $TMP_OUT"
								java -Xmx4g -jar "$snpSift" annotate -tabix "$db" "$CURRENT_INPUT" > "$TMP_OUT"
							
								# Check if SnpSift succeeded AND created a non-empty output
								if [ $? -ne 0 ] || [ ! -s "$TMP_OUT" ]; then
									echo "Error: SnpSIFT Annotation failed or produced empty file for $BAMFILE_NAME on DB $db"
									rm -f "$TMP_OUT"
									exit 1
								fi

								# Remove previous pass temporary file (unless it was the original input)
								if [ "$CURRENT_INPUT" != "$vcfFile" ]; then
									rm -f "$CURRENT_INPUT"
								fi

								# Advance input to the newly created VCF pass
								CURRENT_INPUT="$TMP_OUT"
								STEP_NUM=$((STEP_NUM + 1))
							done
						
							# Move the final non-empty pass to your target annotated VCF name
							mv "$CURRENT_INPUT" "$ANNOT_VCF"
						else
							echo "No annotation databases provided. Skipping SnpSift annotate stage..."
							cp "$vcfFile" "$ANNOT_VCF"
						fi

						# --- STEP B: FILTER ANNOTATED VCF ---
						echo "java -Xmx4g -jar $snpSift filter $ACTIVE_FILTER $ANNOT_VCF > ${OUTPUT_DIR}${BAMFILE_NAME}_vep_annH.sift.vcf"
						java -Xmx4g -jar "$snpSift" filter "$ACTIVE_FILTER" "$ANNOT_VCF" > "${OUTPUT_DIR}${BAMFILE_NAME}_vep_annH.sift.vcf"
						if [ $? -ne 0 ]; then
							echo "Error: SnpSIFT failed for $BAMFILE_NAME"
							exit 1
						else
							# 2. Count variants after filtering
                        	OUT_VCF="${OUTPUT_DIR}${BAMFILE_NAME}_vep_annH.sift.vcf"
                        	FILTERED_COUNT=$(grep -vc "^#" "$OUT_VCF")
                        	REMOVED_COUNT=$((RAW_COUNT - FILTERED_COUNT))
                        	RETENTION_PCT="0.00"
                        	if [ "$RAW_COUNT" -gt 0 ]; then
                            	RETENTION_PCT=$(awk -v f="$FILTERED_COUNT" -v r="$RAW_COUNT" 'BEGIN {printf "%.2f", (f/r)*100}')
                       	 	fi

                        	# 3. Append counts to CSV log
                        	echo "${BAMFILE_NAME},Germline_VEP,${RAW_COUNT},${FILTERED_COUNT},${REMOVED_COUNT},${RETENTION_PCT}\%" >> "$FILTER_LOG_FILE"
							update_pipeline_status "$BAMFILE_NAME" "SnpSIFT(VEP)_H"
							rm -f "$ANNOT_VCF"
						fi
						
					fi
							
							;;
				*.annM.vcf)
					BAMFILE_NAME=$(echo "$filename" | sed 's/\.annM\.vcf$//')
					if grep -q "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" && grep "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" | grep -q "|SnpSIFT(snpEff)_M"; then
						# If both sample and stage exist in the file
						echo "$BAMFILE_NAME: SnpSIFT(snpEff)_M already done. Skipping SNPSIFT..."
					elif [ "$germSomaticOP" = "SOMATIC" ] || [ "$germSomaticOP" = "BOTH" ]; then
						echo "Annotating SOMATIC variant (EXON): $filename"
						
						ACTIVE_FILTER=$(get_active_filter "$filterOptionsA" "SNPEFF")
						# 1. Count raw variants before filtering
                    	RAW_COUNT=$(grep -vc "^#" "$vcfFile")
						
						# --- STEP A: INLINE ANNOTATION ---
						ANNOT_VCF="${OUTPUT_DIR}${BAMFILE_NAME}.annM.annot.vcf"
						
						if [ -n "$SNPSIFT_DBS" ]; then
							CURRENT_INPUT="$vcfFile"
							STEP_NUM=1
							for db in $SNPSIFT_DBS; do
							echo "Running SnpSift annotate with DB: $db"
							
							# Unique temporary output for each database pass
							TMP_OUT="${OUTPUT_DIR}${BAMFILE_NAME}_tmp_pass_${STEP_NUM}.vcf"
							echo "Running command: java -Xmx4g -jar $snpSift annotate -tabix $db $CURRENT_INPUT > $TMP_OUT"
							java -Xmx4g -jar "$snpSift" annotate -tabix "$db" "$CURRENT_INPUT" > "$TMP_OUT"
							
							# Check if SnpSift succeeded AND created a non-empty output
							if [ $? -ne 0 ] || [ ! -s "$TMP_OUT" ]; then
								echo "Error: SnpSIFT Annotation failed or produced empty file for $BAMFILE_NAME on DB $db"
								rm -f "$TMP_OUT"
								exit 1
							fi

							# Remove previous pass temporary file (unless it was the original input)
							if [ "$CURRENT_INPUT" != "$vcfFile" ]; then
								rm -f "$CURRENT_INPUT"
							fi

							# Advance input to the newly created VCF pass
							CURRENT_INPUT="$TMP_OUT"
							STEP_NUM=$((STEP_NUM + 1))
						done
						
						# Move the final non-empty pass to your target annotated VCF name
						mv "$CURRENT_INPUT" "$ANNOT_VCF"
					else
						echo "No annotation databases provided. Skipping SnpSift annotate stage..."
						cp "$vcfFile" "$ANNOT_VCF"
					fi
							
						# --- STEP B: FILTER ANNOTATED VCF ---
						echo "java -Xmx8g -jar $snpSift filter $ACTIVE_FILTER $ANNOT_VCF > ${OUTPUT_DIR}${BAMFILE_NAME}.annM.sift.vcf"
						java -Xmx8g -jar "$snpSift" filter "$ACTIVE_FILTER" "$ANNOT_VCF" > "${OUTPUT_DIR}${BAMFILE_NAME}.annM.sift.vcf"
						
						if [ $? -ne 0 ]; then
							echo "Error: SnpSIFT failed for $BAMFILE_NAME"
							exit 1
						else
							# 2. Count variants after filtering
                        	OUT_VCF="${OUTPUT_DIR}${BAMFILE_NAME}.annM.sift.vcf"
                        	FILTERED_COUNT=$(grep -vc "^#" "$OUT_VCF")
                        	REMOVED_COUNT=$((RAW_COUNT - FILTERED_COUNT))
                        
                        	RETENTION_PCT="0.00"
                        	if [ "$RAW_COUNT" -gt 0 ]; then
                            	RETENTION_PCT=$(awk -v f="$FILTERED_COUNT" -v r="$RAW_COUNT" 'BEGIN {printf "%.2f", (f/r)*100}')
                        	fi

                        	# 3. Append counts to CSV log
                        	echo "${BAMFILE_NAME},Somatic_SnpEff,${RAW_COUNT},${FILTERED_COUNT},${REMOVED_COUNT},${RETENTION_PCT}\%" >> "$FILTER_LOG_FILE"
							update_pipeline_status "$BAMFILE_NAME" "SnpSIFT(snpEff)_M"
							rm -f "$ANNOT_VCF"
						fi
						
					fi
							;;
				*.annH.vcf)
					BAMFILE_NAME=$(echo "$filename" | sed 's/\.annH\.vcf$//')
					if grep -q "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" && grep "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" | grep -q "|SnpSIFT(snpEff)_H"; then
						# If both sample and stage exist in the file
						echo "$BAMFILE_NAME: SnpSIFT(snpEff)_H already done. Skipping SNPSIFT..."
					elif [ "$germSomaticOP" = "GERMLINE" ] || [ "$germSomaticOP" = "BOTH" ]; then
						echo "Annotating GERMLINE variant: $filename"
						
						ACTIVE_FILTER=$(get_active_filter "$filterOptionsA" "SNPEFF")
						
						# 1. Count raw variants before filtering
                    	RAW_COUNT=$(grep -vc "^#" "$vcfFile")
						
						# --- STEP A: INLINE ANNOTATION ---
						ANNOT_VCF="${OUTPUT_DIR}${BAMFILE_NAME}.annH.annot.vcf"
				
						if [ -n "$SNPSIFT_DBS" ]; then
							CURRENT_INPUT="$vcfFile"
							STEP_NUM=1

							for db in $SNPSIFT_DBS; do
								echo "Running SnpSift annotate with DB: $db"
							
								# Unique temporary output for each database pass
								TMP_OUT="${OUTPUT_DIR}${BAMFILE_NAME}_tmp_pass_${STEP_NUM}.vcf"
								echo "Running command: java -Xmx4g -jar $snpSift annotate -tabix $db $CURRENT_INPUT > $TMP_OUT"
								java -Xmx4g -jar "$snpSift" annotate -tabix "$db" "$CURRENT_INPUT" > "$TMP_OUT"
							
								# Check if SnpSift succeeded AND created a non-empty output
								if [ $? -ne 0 ] || [ ! -s "$TMP_OUT" ]; then
									echo "Error: SnpSIFT Annotation failed or produced empty file for $BAMFILE_NAME on DB $db"
									rm -f "$TMP_OUT"
									exit 1
								fi

								# Remove previous pass temporary file (unless it was the original input)
								if [ "$CURRENT_INPUT" != "$vcfFile" ]; then
									rm -f "$CURRENT_INPUT"
								fi

								# Advance input to the newly created VCF pass
								CURRENT_INPUT="$TMP_OUT"
								STEP_NUM=$((STEP_NUM + 1))
							done
						
						# Move the final non-empty pass to your target annotated VCF name
						mv "$CURRENT_INPUT" "$ANNOT_VCF"
					else
						echo "No annotation databases provided. Skipping SnpSift annotate stage..."
						cp "$vcfFile" "$ANNOT_VCF"
					fi
						
						# --- STEP B: FILTER ANNOTATED VCF ---
						echo "java -Xmx4g -jar $snpSift filter $ACTIVE_FILTER $ANNOT_VCF > ${OUTPUT_DIR}${BAMFILE_NAME}.annH.sift.vcf"
						java -Xmx4g -jar "$snpSift" filter "$ACTIVE_FILTER" "$ANNOT_VCF" > "${OUTPUT_DIR}${BAMFILE_NAME}.annH.sift.vcf"
						if [ $? -ne 0 ]; then
							echo "Error: SnpSIFT failed for $BAMFILE_NAME"
							exit 1
						else
							# 2. Count variants after filtering
	                        OUT_VCF="${OUTPUT_DIR}${BAMFILE_NAME}.annH.sift.vcf"
    	                    FILTERED_COUNT=$(grep -vc "^#" "$OUT_VCF")
        	                REMOVED_COUNT=$((RAW_COUNT - FILTERED_COUNT))
            	            RETENTION_PCT="0.00"
                        	if [ "$RAW_COUNT" -gt 0 ]; then
                            	RETENTION_PCT=$(awk -v f="$FILTERED_COUNT" -v r="$RAW_COUNT" 'BEGIN {printf "%.2f", (f/r)*100}')
                        	fi
                	        # 3. Append counts to CSV log
                    	    echo "${BAMFILE_NAME},Germline_SnpEff,${RAW_COUNT},${FILTERED_COUNT},${REMOVED_COUNT},${RETENTION_PCT}\%" >> "$FILTER_LOG_FILE"
							update_pipeline_status "$BAMFILE_NAME" "SnpSIFT(snpEff)_H"
							echo "Removing temporary intermediate annotated file $ANNOT_VCF"
							rm -f "$ANNOT_VCF"
						fi
						
					fi
							;;
						
			esac

		fi
	done

	echo "Appending total summary to the counts file...."

	if [ -f "$FILTER_LOG_FILE" ] && [ $(wc -l < "$FILTER_LOG_FILE") -gt 1 ]; then
		awk -F',' '
		NR > 1 {
			stage = $2
			count[stage]++
			raw[stage] += $3
			filt[stage] += $4
			rem[stage] += $5
			
			tot_count++
			tot_raw += $3
			tot_filt += $4
			tot_rem += $5
		}
		END {
			# Print sub-totals per stage tag (e.g. TOTAL_Germline_SnpEff)
			for (st in count) {
				pct = (raw[st] > 0) ? (filt[st] / raw[st]) * 100 : 0
				printf "TOTAL_%s (%d Samples),%s,%d,%d,%d,%.2f%%\n", st, count[st], st, raw[st], filt[st], rem[st], pct
			}
			
			# Print overall combined summary if multiple stages were run
			if (length(count) > 1) {
				tot_pct = (tot_raw > 0) ? (tot_filt / tot_raw) * 100 : 0
				printf "TOTAL_COMBINED (%d Records),Combined,%d,%d,%d,%.2f%%\n", tot_count, tot_raw, tot_filt, tot_rem, tot_pct
			}
		}' "$FILTER_LOG_FILE" >> "$FILTER_LOG_FILE"
	fi
	echo "++++++++++++++++++++++++++++++++++++ SnpSIFT Annotation & Filtration  COMPLETED +++++++++++++++++++++++++++++++++++++++++"

	##################################
	# ALL END: 
	##################################
elif [ "$variantFilterationOp" = "BA" ]; then
	##################################
	# BA Start: VariantFiltration+ Annotation (SNP,VEP)+SNPSIFT(Annotation+Filteration) 
	##################################
	# ALL:STEP 9.1: VariantFiltration BEFORE: Filter variant calls based on INFO and/or FORMAT annotations
	# ------------------------------------
	# Germline _hap.vcf.gz --> _HVFB.vcf
	# Mutect2  _mut.vcf.gz --> _MVFB.vcf

	echo "Running VariantFilteration ..."

	#Removing starting and ending double qoutes
	filterOptionsB=$(echo "$filterOptionsB" | sed 's/^"//; s/"$//')

	for vcfFile in "$OUTPUT_DIR"*_mut.vcf.gz "$OUTPUT_DIR"*_hap.vcf.gz; do

		if [ -f "$vcfFile" ]; then 	
			filename=$(basename "$vcfFile") 
			BAMFILE_NAME=$(echo "$filename" | sed -E 's/(_mut|_hap)\.vcf\.gz$//')

			case "$vcfFile" in
				*_mut.vcf.gz)
					if grep -q "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" && grep "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" | grep -q "|VariantFiltration_M"; then
						echo "$BAMFILE_NAME: FilterMutectCalls (VariantFiltration_M) already done. Skipping Variant Filtration ..."
										
					elif [ "$germSomaticOP" = "SOMATIC" ] || [ "$germSomaticOP" = "BOTH" ]; then

						F1R2_FILE="${OUTPUT_DIR}${BAMFILE_NAME}_f1r2.tar.gz"
                    	ORIENT_MODEL="${OUTPUT_DIR}${BAMFILE_NAME}_read_orientation_model.tar.gz"
                    	PILEUP_TABLE="${OUTPUT_DIR}${BAMFILE_NAME}_getpileupsummaries.table"
        				CONTAM_TABLE="${OUTPUT_DIR}${BAMFILE_NAME}_contamination.table"
						OUT_MVFB="${OUTPUT_DIR}${BAMFILE_NAME}_MVFB.vcf"

						echo "Step 1: LearnReadOrientationModel for $BAMFILE_NAME "
                    	orient_cmd="./gatk --java-options \"-Xms4G -Xmx16G\" LearnReadOrientationModel -I \"${F1R2_FILE}\" -O \"${ORIENT_MODEL}\""
                    	echo "Command: $orient_cmd"
                    	eval $orient_cmd
                    
						if [ $? -ne 0 ]; then
							echo "Error: LearnReadOrientationModel failed for $BAMFILE_NAME"
							exit 1
						fi

                    	echo "Step 2: FilterMutectCalls for $BAMFILE_NAME "
                    	filterVariant_cmd="./gatk --java-options \"-Xms4G -Xmx16G\" FilterMutectCalls -R \"${REFERENCE}\" -V \"${vcfFile}\" --ob-priors \"${ORIENT_MODEL}\" -O \"${OUT_MVFB}\""
						
						echo "FilterMutectCalls Command is : $filterVariant_cmd"
						eval $filterVariant_cmd
						if [ $? -ne 0 ]; then
							echo "Error: FilterMutectCalls failed for $BAMFILE_NAME"
							exit 1
						else
							update_pipeline_status "$BAMFILE_NAME" "VariantFiltration_M"
						fi
						

					fi
					
					;;
				*_hap.vcf.gz)
				
					if grep -q "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" && grep "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" | grep -q "|VariantFiltration_H"; then
						# If both sample and stage exist in the file
						echo "$BAMFILE_NAME: VariantFiltration_H already done. Skipping VariantFiltration..."
				
					elif [ "$germSomaticOP" = "GERMLINE" ] || [ "$germSomaticOP" = "BOTH" ]; then
					
						filterVariant_cmd="./gatk --java-options \"-Xms4G -Xmx16G\"  VariantFiltration -R "${REFERENCE}" -V "${vcfFile}" -O "${OUTPUT_DIR}""${BAMFILE_NAME}_HVFB.vcf""

						if [ -z "$filterOptionsB" ]; then
							
							filterVariant_cmd="$filterVariant_cmd --filter-expression \"vc.getAttribute('BaseQRankSum') < -2.0 || vc.getAttribute('MQRankSum') < -2.0\" --filter-name "LowBaseQRankSum" "
						else
							filterVariant_cmd="$filterVariant_cmd $filterOptionsB\"" #ADDED QUOTE AT END
						fi

						echo "VariantFiltration Command is : $filterVariant_cmd"
						eval $filterVariant_cmd
						if [ $? -ne 0 ]; then
							echo "Error: VariantFiltration failed for $BAMFILE_NAME"
							exit 1
						else
							update_pipeline_status "$BAMFILE_NAME" "VariantFiltration_H"
						fi
					fi
					
			;;
			esac

		fi
	done
	echo "++++++++++++++++++++++++++++++++++++ VariantFiltration Before COMPLETED +++++++++++++++++++++++++++++++++++++++++"
		##################################
	# ALL: STEP 9.3: Annotation: snpEff, VEP, Both

	if [ "$annotationOPtion" = "snpeff" ]; then
		# ------------------------------------
		#  germline ( _HVFB.vcf ---> .annH.vcf )
		#  somatic  ( _MVFB.vcf ---> .annM.vcf )
		
		#First check database available or not and then install
		# Check if the database is already installed
		echo "Checking snpEff database..."
		if java -Xmx8g -jar $snpEff_path databases -v | grep -wq "$snpEff_db"; then
			echo "$snpEff_db is already installed"
		else
			# If the database is not installed, download it
			echo "Installing $snpEff_db ..."
			java -Xmx8g -jar $snpEff_path download -v "$snpEff_db"
			if [ $? -ne 0 ]; then
				echo "Error: snpEff database $snpEff_db download failed"
				exit 1
			else
				echo "$snpEff_db download completed"
			fi
			
		fi
		echo "Running snpEff..."

		for vcfFile in "$OUTPUT_DIR"*_MVFB.vcf "$OUTPUT_DIR"*_HVFB.vcf; do

			if [ -f "$vcfFile" ]; then 	
				filename=$(basename "$vcfFile") 
				BAMFILE_NAME=$(echo "$filename" | sed -E 's/(_MVFB|_HVFB)\.vcf$//')
				case "$vcfFile" in
					*_MVFB.vcf) 
					if grep -q "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" && grep "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" | grep -q "|snpEff_M"; then
						# If both sample and stage exist in the file
						echo "$BAMFILE_NAME: snpEff_M already done. Skipping snpEff..."
					elif [ "$germSomaticOP" = "SOMATIC" ] || [ "$germSomaticOP" = "BOTH" ]; then
						echo "Annotating SOMATIC variant: $filename"
						java -Xmx8g -jar "$snpEff_path" -q -canon -csvStats "${OUTPUT_DIR}${BAMFILE_NAME}.annM.csv" -stats "${OUTPUT_DIR}${BAMFILE_NAME}.annM.html" "$snpEff_db" "$vcfFile" > "${OUTPUT_DIR}${BAMFILE_NAME}.annM.vcf"
						if [ $? -ne 0 ]; then
							echo "Error: snpEff failed for $BAMFILE_NAME"
							exit 1
						else
							update_pipeline_status "$BAMFILE_NAME" "snpEff_M"
						fi
							
					fi
					;;
					*_HVFB.vcf) 
					if grep -q "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" && grep "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" | grep -q "|snpEff_H"; then
						# If both sample and stage exist in the file
						echo "$BAMFILE_NAME: snpEff_H already done. Skipping snpEff..."
					elif [ "$germSomaticOP" = "GERMLINE" ] || [ "$germSomaticOP" = "BOTH" ]; then
						echo "Annotating GERMLINE variant: $filename"
						java -Xmx8g -jar "$snpEff_path" -q -canon -csvStats "${OUTPUT_DIR}${BAMFILE_NAME}.annH.csv" -stats "${OUTPUT_DIR}${BAMFILE_NAME}.annH.html" "$snpEff_db" "$vcfFile" > "${OUTPUT_DIR}${BAMFILE_NAME}.annH.vcf"
						if [ $? -ne 0 ]; then
							echo "Error: snpEff failed for $BAMFILE_NAME"
							exit 1
						else
							update_pipeline_status "$BAMFILE_NAME" "snpEff_H"
						fi
					fi
					;;
				#stage
				esac
			fi
		done
	
		echo "snpEff finished. All ann.vcf files generated."
		vcfFiles="$OUTPUT_DIR*.annM.vcf $OUTPUT_DIR*.annH.vcf" # For snpSIFT
	elif [ "$annotationOPtion" = "vep" ]; then
		echo "Running VEP..."
		cd $VEP_PATH
		for vcfFile in "$OUTPUT_DIR"*_MVFB.vcf "$OUTPUT_DIR"*_HVFB.vcf; do
			if [ -f "$vcfFile" ]; then 	
				filename=$(basename "$vcfFile") 
				BAMFILE_NAME=$(echo "$filename" | sed -E 's/(_MVFB|_HVFB)\.vcf$//')

				export PATH="/usr/bin:$PATH"

				case "$vcfFile" in
					*_MVFB.vcf)
					#Check for File already exist or Not
					if grep -q "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" && grep "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" | grep -q "|VEP_M"; then
						# If both sample and stage exist in the file
						echo "$BAMFILE_NAME: VEP_M already done. Skipping VEP..."
					else 
						if [ "$germSomaticOP" = "SOMATIC" ] || [ "$germSomaticOP" = "BOTH" ]; then
							# Run VEP after variant filtering
							vep_cmd="perl vep --input_file \"${vcfFile}\" --output_file \"${OUTPUT_DIR}${BAMFILE_NAME}_vep_annM.vcf\" \
								--cache --dir_cache \"$VEP_PATH/.vep\" --fasta \"$REFERENCE\" --vcf --offline \
								--assembly $vepAssembly --species $vepSpecies --sift b --polyphen b \
								--canonical --symbol --af --af_gnomad --af_1kg \
								--gene_phenotype --regulatory --numbers --biotype --verbose --force_overwrite"
							
							echo "VEP Annotation Command: $vep_cmd"
							eval $vep_cmd
							if [ $? -ne 0 ]; then
									echo "Error: VEP failed for $BAMFILE_NAME"
									exit 1
							else 
								update_pipeline_status "$BAMFILE_NAME" "VEP_M"
							fi
							
						fi
					fi
					;;
					*_HVFB.vcf)
					#Check for File already exist or Not
					if grep -q "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" && grep "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" | grep -q "|VEP_H"; then
						# If both sample and stage exist in the file
						echo "$BAMFILE_NAME: VEP_H already done. Skipping VEP..."
					elif [ "$germSomaticOP" = "GERMLINE" ] || [ "$germSomaticOP" = "BOTH" ]; then
				
							# Run VEP after variant filtering
							vep_cmd="perl vep --input_file \"${vcfFile}\" --output_file \"${OUTPUT_DIR}${BAMFILE_NAME}_vep_annH.vcf\" \
							--cache --dir_cache \"$VEP_PATH/.vep\" --fasta \"$REFERENCE\" --vcf --offline \
							--assembly $vepAssembly --species $vepSpecies --sift b --polyphen b \
							--canonical --symbol --af --af_gnomad --af_1kg  \
							--gene_phenotype --regulatory --numbers --biotype --verbose --force_overwrite"
						
							echo "VEP Annotation Command: $vep_cmd"
							eval $vep_cmd
							if [ $? -ne 0 ]; then
									echo "Error: VEP failed for $BAMFILE_NAME"
									exit 1
							else 
								update_pipeline_status "$BAMFILE_NAME" "VEP_H"
							fi
						
					fi
					;;
				esac

			fi
		done
		vcfFiles="$OUTPUT_DIR*_vep_annM.vcf $OUTPUT_DIR*_vep_annH.vcf"
		echo "VEP finished. All *_vep_ann.vcf files generated." 

	elif [ "$annotationOPtion" = "both" ]; then

		# ------------------------------------
		#  germline ( _HVFB.vcf ---> .annH.vcf )
		#  somatic  ( _MVFB.vcf ---> .annM.vcf )
		
		#First check database available or not and then install
		# Check if the database is already installed
		echo "Checking snpEff database..."
		if java -Xmx8g -jar $snpEff_path databases -v | grep -wq "$snpEff_db"; then
			echo "$snpEff_db is already installed"
		else
			# If the database is not installed, download it
			echo "Installing $snpEff_db ..."
			java -Xmx8g -jar $snpEff_path download -v "$snpEff_db"
			if [ $? -ne 0 ]; then
				echo "Error: snpEff database $snpEff_db download failed"
				exit 1
			else
				echo "$snpEff_db download completed"
			fi
			
		fi
		echo "Running snpEff..."

		for vcfFile in "$OUTPUT_DIR"*_MVFB.vcf "$OUTPUT_DIR"*_HVFB.vcf; do
			if [ -f "$vcfFile" ]; then 	
				filename=$(basename "$vcfFile") 
				BAMFILE_NAME=$(echo "$filename" | sed -E 's/(_MVFB|_HVFB)\.vcf$//')

				case "$vcfFile" in 
					*_MVFB.vcf) 
					if grep -q "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" && grep "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" | grep -q "|snpEff_M"; then
						# If both sample and stage exist in the file
						echo "$BAMFILE_NAME: snpEff_M already done. Skipping snpEff..."
					elif [ "$germSomaticOP" = "SOMATIC" ] || [ "$germSomaticOP" = "BOTH" ]; then
						echo "Annotating SOMATIC variant: $filename"
						java -Xmx8g -jar "$snpEff_path" -q -canon -csvStats "${OUTPUT_DIR}${BAMFILE_NAME}.annM.csv" -stats "${OUTPUT_DIR}${BAMFILE_NAME}.annM.html" "$snpEff_db" "$vcfFile" > "${OUTPUT_DIR}${BAMFILE_NAME}.annM.vcf"
						if [ $? -ne 0 ]; then
							echo "Error: snpEff failed for $BAMFILE_NAME"
							exit 1
						else
							update_pipeline_status "$BAMFILE_NAME" "snpEff_M"
						fi
							
					fi
					;;
					*_HVFB.vcf) 
					if grep -q "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" && grep "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" | grep -q "|snpEff_H"; then
						# If both sample and stage exist in the file
						echo "$BAMFILE_NAME: snpEff_H already done. Skipping snpEff..."
					elif [ "$germSomaticOP" = "GERMLINE" ] || [ "$germSomaticOP" = "BOTH" ]; then
						echo "Annotating GERMLINE variant: $filename"
						java -Xmx8g -jar "$snpEff_path" -q -canon -csvStats "${OUTPUT_DIR}${BAMFILE_NAME}.annH.csv" -stats "${OUTPUT_DIR}${BAMFILE_NAME}.annH.html" "$snpEff_db" "$vcfFile" > "${OUTPUT_DIR}${BAMFILE_NAME}.annH.vcf"
						if [ $? -ne 0 ]; then
							echo "Error: snpEff failed for $BAMFILE_NAME"
							exit 1
						else
							update_pipeline_status "$BAMFILE_NAME" "snpEff_H"
						fi
					fi
					;;
				#stage
				esac
			fi
		done
	
		echo "snpEff finished. All ann.vcf files generated."

		echo "Running VEP..."
		cd $VEP_PATH

		for vcfFile in "$OUTPUT_DIR"*.annH.vcf "$OUTPUT_DIR"*.annM.vcf; do

			if [ -f "$vcfFile" ]; then 	
				filename=$(basename "$vcfFile") 
				BAMFILE_NAME=$(echo "$filename" | sed -E 's/(.annH|.annM)\.vcf$//')
				export PATH="/usr/bin:$PATH"
				case "$vcfFile" in
					*.annM.vcf)
						
					if grep -q "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" && grep "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" | grep -q "|VEP_M"; then
							# If both sample and stage exist in the file
							echo "$BAMFILE_NAME: VEP_M already done. Skipping VEP..."
					elif [ "$germSomaticOP" = "SOMATIC" ] || [ "$germSomaticOP" = "BOTH" ]; then
					
							# Run VEP after variant filtering
							vep_cmd="perl vep --input_file \"${vcfFile}\" --output_file \"${OUTPUT_DIR}${BAMFILE_NAME}_vep_annM.vcf\" --cache --dir_cache \"$VEP_PATH/.vep\" --fasta \"$REFERENCE\" --vcf --offline --assembly $vepAssembly --species $vepSpecies --sift b --polyphen b --canonical --symbol --af --af_gnomad --af_1kg  --gene_phenotype --regulatory --numbers --biotype --verbose --force_overwrite"
							echo "VEP Annotation Command: $vep_cmd"
							eval $vep_cmd
							if [ $? -ne 0 ]; then
								echo "Error: VEP failed for $BAMFILE_NAME"
								exit 1
							else
								update_pipeline_status "$BAMFILE_NAME" "VEP_M"
							fi
							
						
					fi
						;;
					*.annH.vcf)
	
					if grep -q "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" && grep "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" | grep -q "|VEP_H"; then
							# If both sample and stage exist in the file
							echo "$BAMFILE_NAME: VEP_H already done. Skipping VEP..."
					elif [ "$germSomaticOP" = "GERMLINE" ] || [ "$germSomaticOP" = "BOTH" ]; then
						
							# Run VEP after variant filtering
							vep_cmd="perl vep --input_file \"${vcfFile}\" --output_file \"${OUTPUT_DIR}${BAMFILE_NAME}_vep_annH.vcf\" --cache --dir_cache \"$VEP_PATH/.vep\" --fasta \"$REFERENCE\" --vcf --offline --assembly $vepAssembly --species $vepSpecies --sift b --polyphen b --canonical --symbol --af --af_gnomad --af_1kg  --gene_phenotype --regulatory --numbers --biotype --verbose --force_overwrite"
								
							echo "VEP Annotation Command: $vep_cmd"
							eval $vep_cmd
							if [ $? -ne 0 ]; then
								echo "Error: VEP failed for $BAMFILE_NAME"
								exit 1
							else
								update_pipeline_status "$BAMFILE_NAME" "VEP_H"
							fi
						
					fi
						;;
				esac

			fi
		done
		echo "VEP finished. All *_vep_ann.vcf files generated." 
		vcfFiles="$OUTPUT_DIR*.annM.vcf $OUTPUT_DIR*.annH.vcf $OUTPUT_DIR*_vep_annM.vcf $OUTPUT_DIR*_vep_annH.vcf"
	fi
	##################################
	# ALL: STEP 9.4: Filteration: snpSIFT
		echo "Performing SnpSIFT annotation & filtering ..."


	RESOLVED_GNOMAD=""
	if [ -n "$ANNOT_GNOMAD" ]; then
		if [ -d "$ANNOT_GNOMAD" ]; then
			echo "gnomAD directory detected. Building file list..."
			RESOLVED_GNOMAD=$(find "$ANNOT_GNOMAD" -maxdepth 1 -name "*.vcf.gz" | paste -sd, -)
		elif [ -f "$ANNOT_GNOMAD" ]; then
			RESOLVED_GNOMAD="$ANNOT_GNOMAD"
		fi
	fi

	# 1B. Construct space-separated database argument for SnpSift
	# 1B. Build the -tabix options string
	SNPSIFT_DBS=""
	[ -n "$RESOLVED_GNOMAD" ] && SNPSIFT_DBS="$SNPSIFT_DBS $RESOLVED_GNOMAD"
	[ -f "$ANNOT_DBSNP" ]     && SNPSIFT_DBS="$SNPSIFT_DBS $ANNOT_DBSNP"
	[ -f "$ANNOT_CLINVAR" ]   && SNPSIFT_DBS="$SNPSIFT_DBS $ANNOT_CLINVAR"

	# Trim leading spaces
	SNPSIFT_DBS=$(echo "$SNPSIFT_DBS" | xargs)

	# Log detected databases
	echo "SnpSift Database Targets: ${SNPSIFT_DBS:-None specified}"

	for vcfFile in $vcfFiles; do
		if [ -f "$vcfFile" ]; then 
			filename=$(basename "$vcfFile") 

			case "$filename" in
				*_vep_annM.vcf)
					BAMFILE_NAME=$(echo "$filename" | sed 's/_vep_annM\.vcf$//')
					if grep -q "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" && grep "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" | grep -q "|SnpSIFT(VEP)_M"; then
						# If both sample and stage exist in the file
						echo "$BAMFILE_NAME: SnpSIFT(VEP)_M already done. Skipping SNPSIFT..."
				
					elif [ "$germSomaticOP" = "SOMATIC" ] || [ "$germSomaticOP" = "BOTH" ]; then
						echo "Annotating SOMATIC variant (EXON): $filename"
						
						ACTIVE_FILTER=$(get_active_filter "$filterOptionsA" "VEP")
						# 1. Count raw variants before filtering
                    	RAW_COUNT=$(grep -vc "^#" "$vcfFile")
						
						# --- STEP A: INLINE ANNOTATION ---
                    	ANNOT_VCF="${OUTPUT_DIR}${BAMFILE_NAME}_vep_annM.annot.vcf"
						# --- STEP A: INLINE ANNOTATION ---
						if [ -n "$SNPSIFT_DBS" ]; then
							CURRENT_INPUT="$vcfFile"
							STEP_NUM=1

							for db in $SNPSIFT_DBS; do
								echo "Running SnpSift annotate with DB: $db"
							
								# Unique temporary output for each database pass
								TMP_OUT="${OUTPUT_DIR}${BAMFILE_NAME}_tmp_pass_${STEP_NUM}.vcf"
								echo "Running command: java -Xmx4g -jar $snpSift annotate -tabix $db $CURRENT_INPUT > $TMP_OUT"
								java -Xmx4g -jar "$snpSift" annotate -tabix "$db" "$CURRENT_INPUT" > "$TMP_OUT"
							
								# Check if SnpSift succeeded AND created a non-empty output
								if [ $? -ne 0 ] || [ ! -s "$TMP_OUT" ]; then
									echo "Error: SnpSIFT Annotation failed or produced empty file for $BAMFILE_NAME on DB $db"
									rm -f "$TMP_OUT"
									exit 1
								fi

								# Remove previous pass temporary file (unless it was the original input)
								if [ "$CURRENT_INPUT" != "$vcfFile" ]; then
									rm -f "$CURRENT_INPUT"
								fi

								# Advance input to the newly created VCF pass
								CURRENT_INPUT="$TMP_OUT"
								STEP_NUM=$((STEP_NUM + 1))
							done
						
							# Move the final non-empty pass to your target annotated VCF name
							mv "$CURRENT_INPUT" "$ANNOT_VCF"
						else
							echo "No annotation databases provided. Skipping SnpSift annotate stage..."
							cp "$vcfFile" "$ANNOT_VCF"
						fi
						# --- STEP B: FILTER ANNOTATED VCF ---
						echo "java -Xmx8g -jar $snpSift filter $ACTIVE_FILTER $ANNOT_VCF >  ${OUTPUT_DIR}${BAMFILE_NAME}_vep_annM.sift.vcf"
						java -Xmx8g -jar "$snpSift" filter "$ACTIVE_FILTER" "$ANNOT_VCF" >  "${OUTPUT_DIR}${BAMFILE_NAME}_vep_annM.sift.vcf"
												
						if [ $? -ne 0 ]; then
							echo "Error: SnpSIFT failed for $BAMFILE_NAME"
							exit 1
						else
							# 2. Count variants after SnpSift filtering
                        	OUT_VCF="${OUTPUT_DIR}${BAMFILE_NAME}_vep_annM.sift.vcf"
                        	FILTERED_COUNT=$(grep -vc "^#" "$OUT_VCF")
                        	REMOVED_COUNT=$((RAW_COUNT - FILTERED_COUNT))
                            RETENTION_PCT="0.00"
                        	if [ "$RAW_COUNT" -gt 0 ]; then
                            	RETENTION_PCT=$(awk -v f="$FILTERED_COUNT" -v r="$RAW_COUNT" 'BEGIN {printf "%.2f", (f/r)*100}')
                        	fi
	                        # 3. Append counts to CSV log
                        	echo "${BAMFILE_NAME},Somatic_VEP,${RAW_COUNT},${FILTERED_COUNT},${REMOVED_COUNT},${RETENTION_PCT}\%" >> "$FILTER_LOG_FILE"
							update_pipeline_status "$BAMFILE_NAME" "SnpSIFT(VEP)_M"
							rm -f "$ANNOT_VCF" # Optional: remove intermediate annotated file to save disk space
						fi
						
					fi
					
							;;
				*_vep_annH.vcf)

					BAMFILE_NAME=$(echo "$filename" | sed 's/_vep_annH\.vcf$//')
					if grep -q "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" && grep "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" | grep -q "|SnpSIFT(VEP)_H"; then
						# If both sample and stage exist in the file
						echo "$BAMFILE_NAME: SnpSIFT(VEP)_H already done. Skipping SNPSIFT..."
					elif [ "$germSomaticOP" = "GERMLINE" ] || [ "$germSomaticOP" = "BOTH" ]; then
						echo "Annotating GERMLINE variant: $filename"
						
						ACTIVE_FILTER=$(get_active_filter "$filterOptionsA" "VEP")
						# 1. Count raw variants before filtering
                    	RAW_COUNT=$(grep -vc "^#" "$vcfFile")
						
						# --- STEP A: INLINE ANNOTATION ---
						ANNOT_VCF="${OUTPUT_DIR}${BAMFILE_NAME}_vep_annH.annot.vcf"
						if [ -n "$SNPSIFT_DBS" ]; then
							CURRENT_INPUT="$vcfFile"
							STEP_NUM=1

							for db in $SNPSIFT_DBS; do
								echo "Running SnpSift annotate with DB: $db"
							
								# Unique temporary output for each database pass
								TMP_OUT="${OUTPUT_DIR}${BAMFILE_NAME}_tmp_pass_${STEP_NUM}.vcf"
								echo "Running command: java -Xmx4g -jar $snpSift annotate -tabix $db $CURRENT_INPUT > $TMP_OUT"
								java -Xmx4g -jar "$snpSift" annotate -tabix "$db" "$CURRENT_INPUT" > "$TMP_OUT"
							
								# Check if SnpSift succeeded AND created a non-empty output
								if [ $? -ne 0 ] || [ ! -s "$TMP_OUT" ]; then
									echo "Error: SnpSIFT Annotation failed or produced empty file for $BAMFILE_NAME on DB $db"
									rm -f "$TMP_OUT"
									exit 1
								fi

								# Remove previous pass temporary file (unless it was the original input)
								if [ "$CURRENT_INPUT" != "$vcfFile" ]; then
									rm -f "$CURRENT_INPUT"
								fi

								# Advance input to the newly created VCF pass
								CURRENT_INPUT="$TMP_OUT"
								STEP_NUM=$((STEP_NUM + 1))
							done
						
							# Move the final non-empty pass to your target annotated VCF name
							mv "$CURRENT_INPUT" "$ANNOT_VCF"
						else
							echo "No annotation databases provided. Skipping SnpSift annotate stage..."
							cp "$vcfFile" "$ANNOT_VCF"
						fi

						# --- STEP B: FILTER ANNOTATED VCF ---
						echo "java -Xmx4g -jar $snpSift filter $ACTIVE_FILTER $ANNOT_VCF > ${OUTPUT_DIR}${BAMFILE_NAME}_vep_annH.sift.vcf"
						java -Xmx4g -jar "$snpSift" filter "$ACTIVE_FILTER" "$ANNOT_VCF" > "${OUTPUT_DIR}${BAMFILE_NAME}_vep_annH.sift.vcf"
						if [ $? -ne 0 ]; then
							echo "Error: SnpSIFT failed for $BAMFILE_NAME"
							exit 1
						else
							# 2. Count variants after filtering
                        	OUT_VCF="${OUTPUT_DIR}${BAMFILE_NAME}_vep_annH.sift.vcf"
                        	FILTERED_COUNT=$(grep -vc "^#" "$OUT_VCF")
                        	REMOVED_COUNT=$((RAW_COUNT - FILTERED_COUNT))
                        	RETENTION_PCT="0.00"
                        	if [ "$RAW_COUNT" -gt 0 ]; then
                            	RETENTION_PCT=$(awk -v f="$FILTERED_COUNT" -v r="$RAW_COUNT" 'BEGIN {printf "%.2f", (f/r)*100}')
                       	 	fi

                        	# 3. Append counts to CSV log
                        	echo "${BAMFILE_NAME},Germline_VEP,${RAW_COUNT},${FILTERED_COUNT},${REMOVED_COUNT},${RETENTION_PCT}\%" >> "$FILTER_LOG_FILE"
							update_pipeline_status "$BAMFILE_NAME" "SnpSIFT(VEP)_H"
							rm -f "$ANNOT_VCF"
						fi
						
					fi
							
							;;
				*.annM.vcf)
					BAMFILE_NAME=$(echo "$filename" | sed 's/\.annM\.vcf$//')
					if grep -q "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" && grep "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" | grep -q "|SnpSIFT(snpEff)_M"; then
						# If both sample and stage exist in the file
						echo "$BAMFILE_NAME: SnpSIFT(snpEff)_M already done. Skipping SNPSIFT..."
					elif [ "$germSomaticOP" = "SOMATIC" ] || [ "$germSomaticOP" = "BOTH" ]; then
						echo "Annotating SOMATIC variant (EXON): $filename"
						
						ACTIVE_FILTER=$(get_active_filter "$filterOptionsA" "SNPEFF")
						# 1. Count raw variants before filtering
                    	RAW_COUNT=$(grep -vc "^#" "$vcfFile")
						
						# --- STEP A: INLINE ANNOTATION ---
						ANNOT_VCF="${OUTPUT_DIR}${BAMFILE_NAME}.annM.annot.vcf"
						
						if [ -n "$SNPSIFT_DBS" ]; then
							CURRENT_INPUT="$vcfFile"
							STEP_NUM=1
							for db in $SNPSIFT_DBS; do
							echo "Running SnpSift annotate with DB: $db"
							
							# Unique temporary output for each database pass
							TMP_OUT="${OUTPUT_DIR}${BAMFILE_NAME}_tmp_pass_${STEP_NUM}.vcf"
							echo "Running command: java -Xmx4g -jar $snpSift annotate -tabix $db $CURRENT_INPUT > $TMP_OUT"
							java -Xmx4g -jar "$snpSift" annotate -tabix "$db" "$CURRENT_INPUT" > "$TMP_OUT"
							
							# Check if SnpSift succeeded AND created a non-empty output
							if [ $? -ne 0 ] || [ ! -s "$TMP_OUT" ]; then
								echo "Error: SnpSIFT Annotation failed or produced empty file for $BAMFILE_NAME on DB $db"
								rm -f "$TMP_OUT"
								exit 1
							fi

							# Remove previous pass temporary file (unless it was the original input)
							if [ "$CURRENT_INPUT" != "$vcfFile" ]; then
								rm -f "$CURRENT_INPUT"
							fi

							# Advance input to the newly created VCF pass
							CURRENT_INPUT="$TMP_OUT"
							STEP_NUM=$((STEP_NUM + 1))
						done
						
						# Move the final non-empty pass to your target annotated VCF name
						mv "$CURRENT_INPUT" "$ANNOT_VCF"
					else
						echo "No annotation databases provided. Skipping SnpSift annotate stage..."
						cp "$vcfFile" "$ANNOT_VCF"
					fi
							
						# --- STEP B: FILTER ANNOTATED VCF ---
						echo "java -Xmx8g -jar $snpSift filter $ACTIVE_FILTER $ANNOT_VCF > ${OUTPUT_DIR}${BAMFILE_NAME}.annM.sift.vcf"
						java -Xmx8g -jar "$snpSift" filter "$ACTIVE_FILTER" "$ANNOT_VCF" > "${OUTPUT_DIR}${BAMFILE_NAME}.annM.sift.vcf"
						
						if [ $? -ne 0 ]; then
							echo "Error: SnpSIFT failed for $BAMFILE_NAME"
							exit 1
						else
							# 2. Count variants after filtering
                        	OUT_VCF="${OUTPUT_DIR}${BAMFILE_NAME}.annM.sift.vcf"
                        	FILTERED_COUNT=$(grep -vc "^#" "$OUT_VCF")
                        	REMOVED_COUNT=$((RAW_COUNT - FILTERED_COUNT))
                        
                        	RETENTION_PCT="0.00"
                        	if [ "$RAW_COUNT" -gt 0 ]; then
                            	RETENTION_PCT=$(awk -v f="$FILTERED_COUNT" -v r="$RAW_COUNT" 'BEGIN {printf "%.2f", (f/r)*100}')
                        	fi

                        	# 3. Append counts to CSV log
                        	echo "${BAMFILE_NAME},Somatic_SnpEff,${RAW_COUNT},${FILTERED_COUNT},${REMOVED_COUNT},${RETENTION_PCT}\%" >> "$FILTER_LOG_FILE"
							update_pipeline_status "$BAMFILE_NAME" "SnpSIFT(snpEff)_M"
							rm -f "$ANNOT_VCF"
						fi
						
					fi
							;;
				*.annH.vcf)
					BAMFILE_NAME=$(echo "$filename" | sed 's/\.annH\.vcf$//')
					if grep -q "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" && grep "^${BAMFILE_NAME}|" "$PIPELINE_STATUS_FILE" | grep -q "|SnpSIFT(snpEff)_H"; then
						# If both sample and stage exist in the file
						echo "$BAMFILE_NAME: SnpSIFT(snpEff)_H already done. Skipping SNPSIFT..."
					elif [ "$germSomaticOP" = "GERMLINE" ] || [ "$germSomaticOP" = "BOTH" ]; then
						echo "Annotating GERMLINE variant: $filename"
						
						ACTIVE_FILTER=$(get_active_filter "$filterOptionsA" "SNPEFF")
						
						# 1. Count raw variants before filtering
                    	RAW_COUNT=$(grep -vc "^#" "$vcfFile")
						
						# --- STEP A: INLINE ANNOTATION ---
						ANNOT_VCF="${OUTPUT_DIR}${BAMFILE_NAME}.annH.annot.vcf"
				
						if [ -n "$SNPSIFT_DBS" ]; then
							CURRENT_INPUT="$vcfFile"
							STEP_NUM=1

							for db in $SNPSIFT_DBS; do
								echo "Running SnpSift annotate with DB: $db"
							
								# Unique temporary output for each database pass
								TMP_OUT="${OUTPUT_DIR}${BAMFILE_NAME}_tmp_pass_${STEP_NUM}.vcf"
								echo "Running command: java -Xmx4g -jar $snpSift annotate -tabix $db $CURRENT_INPUT > $TMP_OUT"
								java -Xmx4g -jar "$snpSift" annotate -tabix "$db" "$CURRENT_INPUT" > "$TMP_OUT"
							
								# Check if SnpSift succeeded AND created a non-empty output
								if [ $? -ne 0 ] || [ ! -s "$TMP_OUT" ]; then
									echo "Error: SnpSIFT Annotation failed or produced empty file for $BAMFILE_NAME on DB $db"
									rm -f "$TMP_OUT"
									exit 1
								fi

								# Remove previous pass temporary file (unless it was the original input)
								if [ "$CURRENT_INPUT" != "$vcfFile" ]; then
									rm -f "$CURRENT_INPUT"
								fi

								# Advance input to the newly created VCF pass
								CURRENT_INPUT="$TMP_OUT"
								STEP_NUM=$((STEP_NUM + 1))
							done
						
						# Move the final non-empty pass to your target annotated VCF name
						mv "$CURRENT_INPUT" "$ANNOT_VCF"
					else
						echo "No annotation databases provided. Skipping SnpSift annotate stage..."
						cp "$vcfFile" "$ANNOT_VCF"
					fi
						
						# --- STEP B: FILTER ANNOTATED VCF ---
						echo "java -Xmx4g -jar $snpSift filter $ACTIVE_FILTER $ANNOT_VCF > ${OUTPUT_DIR}${BAMFILE_NAME}.annH.sift.vcf"
						java -Xmx4g -jar "$snpSift" filter "$ACTIVE_FILTER" "$ANNOT_VCF" > "${OUTPUT_DIR}${BAMFILE_NAME}.annH.sift.vcf"
						if [ $? -ne 0 ]; then
							echo "Error: SnpSIFT failed for $BAMFILE_NAME"
							exit 1
						else
							# 2. Count variants after filtering
	                        OUT_VCF="${OUTPUT_DIR}${BAMFILE_NAME}.annH.sift.vcf"
    	                    FILTERED_COUNT=$(grep -vc "^#" "$OUT_VCF")
        	                REMOVED_COUNT=$((RAW_COUNT - FILTERED_COUNT))
            	            RETENTION_PCT="0.00"
                        	if [ "$RAW_COUNT" -gt 0 ]; then
                            	RETENTION_PCT=$(awk -v f="$FILTERED_COUNT" -v r="$RAW_COUNT" 'BEGIN {printf "%.2f", (f/r)*100}')
                        	fi
                	        # 3. Append counts to CSV log
                    	    echo "${BAMFILE_NAME},Germline_SnpEff,${RAW_COUNT},${FILTERED_COUNT},${REMOVED_COUNT},${RETENTION_PCT}\%" >> "$FILTER_LOG_FILE"
							update_pipeline_status "$BAMFILE_NAME" "SnpSIFT(snpEff)_H"
							echo "Removing temporary intermediate annotated file $ANNOT_VCF"
							rm -f "$ANNOT_VCF"
						fi
						
					fi
							;;
						
			esac

		fi
	done
	
	echo "++++++++++++++++++++++++++++++++++++ SnpSIFT Annotation & Filtration  COMPLETED +++++++++++++++++++++++++++++++++++++++++"


	##################################
	# BA END: 
	##################################

fi
#variantFilterationOp-END






#Create VCF 
#SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
# Create VCF 
#echo "Generating combined annotated vcf files"
#echo "$SCRIPT_DIR/extract_all_vcfs.sh \"$OUTPUT_DIR\" \"*.annH.sift.vcf\""
#echo "$SCRIPT_DIR/extract_all_vcfs.sh \"$OUTPUT_DIR\" \"*_vep_annH.sift.vcf\""

#"$SCRIPT_DIR/extract_all_vcfs.sh" "$OUTPUT_DIR" "*.annH.sift.vcf"
#"$SCRIPT_DIR/extract_all_vcfs.sh" "$OUTPUT_DIR" "*_vep_annH.sift.vcf"

#"$SCRIPT_DIR/extract_all_vcfs.sh" "$OUTPUT_DIR" "*.annM.sift.vcf"
#"$SCRIPT_DIR/extract_all_vcfs.sh" "$OUTPUT_DIR" "*_vep_annM.sift.vcf"

#echo "Generating combined annotated vcf files"
#echo "./extract_all_vcfs.sh "$OUTPUT_DIR" "*.annH.sift.vcf""
#echo "./extract_all_vcfs.sh "$OUTPUT_DIR" "*_vep_annH.sift.vcf""
#./extract_all_vcfs.sh "$OUTPUT_DIR" "*.annH.sift.vcf"
#./extract_all_vcfs.sh "$OUTPUT_DIR" "*_vep_annH.sift.vcf"












