#!/bin/sh
#PATHS to Binaries
STAR_PATH="${1}"
PICARD_PATH="${2}"
GATK_PATH="${3}"
SUBREAD_PATH="${4}"
# 19 SINGLEREAD/PAIRREAD

#RUNNING MODE
RUN_MODE="${20}" # MULTIPLEPERRUN OR SINGLEPERRUN
#21 featureCounts EACH /COMBINED
FILE_MODE="${22}" #exFAT --readFilesCommand "${9}"
PIPELINE_STATUS_FILE="${18}RNASeqPipeline_status.txt"


#13 Sample name is not used in Single per run
set -e
echo "==================  RUNNING STAR..."

echo "Status File: $PIPELINE_STATUS_FILE"
# Create the file if it doesn't exist
if [ ! -f "$PIPELINE_STATUS_FILE" ]; then
# Try to create the file

    if ! touch "$PIPELINE_STATUS_FILE"; then
        echo "Error: Unable to create $PIPELINE_STATUS_FILE. Check directory permissions." 
        exit 1
        
    fi
fi

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

#STAR 
cd $STAR_PATH
#===========================
###FOR MULTIPLE SAMPLES
#===========================

echo "File mode: $FILE_MODE and RUN MODE $RUN_MODE"

if [ $RUN_MODE = "MULTIPLE" ];
then

STAR_FILE="${18}""${13}.star.Aligned.sortedByCoord.out.bam"
stage="STAR"
SAMPLE_NAME=$(echo "${13}" | sed 's/_[12]$//')

# Check if the pipeline_status.txt file contains the sample AND the stage is already recorded
if grep -q "^${SAMPLE_NAME}|" "$PIPELINE_STATUS_FILE" && grep "^${SAMPLE_NAME}|" "$PIPELINE_STATUS_FILE" | grep -q "|${stage}"; then
    # If both sample and stage exist in the file, that means this stage is already done for the sample
     echo "$SAMPLE_NAME: $stage already done. Skipping STAR Alignment..."
else
	if [ $FILE_MODE = "zipped" ];
	then
	##RUN STAR 
echo "	./STAR --runThreadN ${5} --runMode "${6}" --genomeDir "${7}" --readFilesIn ${8} --readFilesCommand "${9}" --sjdbOverhang ${10} --outSAMtype ${11} --twopassMode ${12} --outFileNamePrefix "${18}""${13}.star." --limitBAMsortRAM ${14} --limitOutSJcollapsed ${15} --quantMode "${16}" --sjdbGTFfile "${17}" --outBAMsortingBinsN ${23} --outSAMmapqUnique ${24} --outSAMunmapped ${25}

"
	./STAR --runThreadN ${5} --runMode "${6}" --genomeDir "${7}" --readFilesIn ${8} --readFilesCommand "${9}" --sjdbOverhang ${10} --outSAMtype ${11} --twopassMode ${12} --outFileNamePrefix "${18}${13}.star." --limitBAMsortRAM ${14} --limitOutSJcollapsed ${15} --quantMode "${16}" --sjdbGTFfile "${17}" --outBAMsortingBinsN ${23} --outSAMmapqUnique ${24} --outSAMunmapped ${25}
	else #NTFS

echo "	./STAR --runThreadN ${5} --runMode "${6}" --genomeDir "${7}" --readFilesIn ${8} --sjdbOverhang ${10} --outSAMtype ${11} --twopassMode ${12} --outFileNamePrefix "${18}$SAMPLE_NAME.star." --limitBAMsortRAM ${14} --limitOutSJcollapsed ${15} --quantMode "${16}" --sjdbGTFfile "${17}" --outBAMsortingBinsN ${23} --outSAMmapqUnique ${24} --outSAMunmapped ${25}
"
	./STAR --runThreadN ${5} --runMode "${6}" --genomeDir "${7}" --readFilesIn ${8} --sjdbOverhang ${10} --outSAMtype ${11} --twopassMode ${12} --outFileNamePrefix "${18}$SAMPLE_NAME.star." --limitBAMsortRAM ${14} --limitOutSJcollapsed ${15} --quantMode "${16}" --sjdbGTFfile "${17}" --outBAMsortingBinsN ${23} --outSAMmapqUnique ${24} --outSAMunmapped ${25}
#	 --outTmpDir "${18}tmp"
	fi
	
update_pipeline_status "$SAMPLE_NAME" "STAR"


fi
stage="featureCounts"
if grep -q "^${SAMPLE_NAME}|" "$PIPELINE_STATUS_FILE" && grep "^${SAMPLE_NAME}|" "$PIPELINE_STATUS_FILE" | grep -q "|${stage}"; then
     echo "$SAMPLE_NAME: $stage already done. Skipping featureCounts..."
else
	###FEATURECOUNT SINGLE READ
	#===========================
	cd $SUBREAD_PATH
	if [ "${19}" = "SINGLE" ];
	then
		
	./featureCounts -T ${5} -s 0 -a  "${17}" -o "${18}${13}.featureCounts" "${18}${13}.star.Aligned.sortedByCoord.out.bam" 
	###FEATURECOUNT PAIRED READ
	#===========================
	elif [ "${19}" = "PAIR" ];
	then

	./featureCounts -p -T ${5} -s 0 -a  "${17}" -o "${18}${SAMPLE_NAME}.featureCounts" "${18}${SAMPLE_NAME}.star.Aligned.sortedByCoord.out.bam" 

	fi
	
update_pipeline_status "$SAMPLE_NAME" "featureCounts"

fi
#===========================
###FOR SINGLE SAMPLES
#===========================
elif [ $RUN_MODE = "SINGLE" ];
then

for fastq in ${8}
do 
#echo $fastq

data=$(echo "$fastq" | tr , " ") #No affect in SINGLE, But nessesary for PAIR


filename=$(basename -- "$fastq" | tr -d '\"')  # Remove trailing double quotes
filename=$(echo "$filename" | tr -d '[:space:]')  # Remove any trailing whitespace or newlines


#echo "$filename" | od -c   #Command to check hidden characters in or around filename
case "$filename" in
    *.fastq.gz)
        FILE_NAME="${filename%.fastq.gz}"
        ;;
    *.fq.gz)
        FILE_NAME="${filename%.fq.gz}"
        ;;
    *.fastq.bz2)
        FILE_NAME="${filename%.fastq.bz2}"
        ;;
    *.fq.bz2)
        FILE_NAME="${filename%.fq.bz2}"
        ;;        
    *)
        FILE_NAME="${filename%.*}"
        ;;
esac

cd $STAR_PATH

STAR_FILE="$FILE_NAME.star.Aligned.sortedByCoord.out.bam"
stage="STAR"
SAMPLE_NAME=$(echo "$FILE_NAME" | sed 's/_[12]$//')

# Check if the pipeline_status.txt file contains the sample AND the stage is already recorded
if grep -q "^${SAMPLE_NAME}|" "$PIPELINE_STATUS_FILE" && grep "^${SAMPLE_NAME}|" "$PIPELINE_STATUS_FILE" | grep -q "|${stage}"; then
    # If both sample and stage exist in the file, that means this stage is already done for the sample
     echo "$SAMPLE_NAME: $stage already done. Skipping STAR Alignment..."
else

#RUN STAR
if [ $FILE_MODE = "zipped" ];
then
echo "./STAR --runThreadN ${5} --runMode "${6}" --genomeDir "${7}" --readFilesIn $data --readFilesCommand "${9}" --sjdbOverhang ${10} --outSAMtype ${11} --twopassMode ${12} --outFileNamePrefix "${18}$SAMPLE_NAME.star." --limitBAMsortRAM ${14} --limitOutSJcollapsed ${15} --quantMode "${16}" --sjdbGTFfile "${17}" --outBAMsortingBinsN ${23} --outSAMmapqUnique ${24} --outSAMunmapped ${25}"

./STAR --runThreadN ${5} --runMode "${6}" --genomeDir "${7}" --readFilesIn $data --readFilesCommand "${9}" --sjdbOverhang ${10} --outSAMtype ${11} --twopassMode ${12} --outFileNamePrefix "${18}$SAMPLE_NAME.star." --limitBAMsortRAM ${14} --limitOutSJcollapsed ${15} --quantMode "${16}" --sjdbGTFfile "${17}" --outBAMsortingBinsN ${23} --outSAMmapqUnique ${24} --outSAMunmapped ${25}

else

echo "./STAR --runThreadN ${5} --runMode "${6}" --genomeDir "${7}" --readFilesIn $data --sjdbOverhang ${10} --outSAMtype ${11} --twopassMode ${12} --outFileNamePrefix "${18}$SAMPLE_NAME.star." --limitBAMsortRAM ${14} --limitOutSJcollapsed ${15} --quantMode "${16}" --sjdbGTFfile "${17}" --outBAMsortingBinsN ${23} --outSAMmapqUnique ${24} --outSAMunmapped ${25}"

./STAR --runThreadN ${5} --runMode "${6}" --genomeDir "${7}" --readFilesIn $data --sjdbOverhang ${10} --outSAMtype ${11} --twopassMode ${12} --outFileNamePrefix "${18}$SAMPLE_NAME.star." --limitBAMsortRAM ${14} --limitOutSJcollapsed ${15} --quantMode "${16}" --sjdbGTFfile "${17}" --outBAMsortingBinsN ${23} --outSAMmapqUnique ${24} --outSAMunmapped ${25}

fi
##END STAR
	        		 # Check if the sample already has a line in the status file
			    if grep -q "^${SAMPLE_NAME}|" "$PIPELINE_STATUS_FILE"; then
				# If the sample is already in the file, append the new stage to its existing line
				sed -i "s/^${SAMPLE_NAME}.*/&|${stage}/" "$PIPELINE_STATUS_FILE"
			    else
				# If the sample is not in the file, create a new line with the sample and current stage
				echo "${SAMPLE_NAME}|${stage}" >> "$PIPELINE_STATUS_FILE"
			    fi


fi

# Clean up STAR intermediate directories
#rm -rf "${18}${SAMPLE_NAME}.star._STARpass1"
#rm -rf "${18}${SAMPLE_NAME}.star._STARgenome"
#rm -rf "${18}${SAMPLE_NAME}.star._STARtmp"
find "${18}" -maxdepth 1 -type d -name "*.star._STAR*" -exec rm -rf {} +


#Only Running featurecounts for EACH file if EACH option selected
if [ "${21}" = "EACH" ];
then
###FEATURECOUNT SINGLE READ
#===========================

stage="featureCounts"
if grep -q "^${SAMPLE_NAME}|" "$PIPELINE_STATUS_FILE" && grep "^${SAMPLE_NAME}|" "$PIPELINE_STATUS_FILE" | grep -q "|${stage}"; then
     echo "$SAMPLE_NAME: $stage already done. Skipping featureCounts..."
else

cd $SUBREAD_PATH
if [ "${19}" = "SINGLE" ];
then


./featureCounts -T ${5} -s 0 -a  "${17}" -o "${18}$SAMPLE_NAME.featureCounts" "${18}$SAMPLE_NAME.star.Aligned.sortedByCoord.out.bam" 




###FEATURECOUNT PAIRED READ
#===========================
elif [ "${19}" = "PAIR" ];
then
	
./featureCounts -p -T ${5} -s 0 -a  "${17}" -o "${18}$SAMPLE_NAME.featureCounts"  "${18}$SAMPLE_NAME.star.Aligned.sortedByCoord.out.bam" 


fi
update_pipeline_status "$SAMPLE_NAME" "featureCounts"

fi

fi
### Combined feature count file is generated in the end of Java code

done


fi


