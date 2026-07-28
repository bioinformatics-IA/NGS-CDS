#!/usr/bin/sh
#set -x 
#################################### PARAMETERS
BWA_PATH="${1}"
Samtools_Path="${2}"
GATK_PATH="${3}"

inputFilesPath="${4}" #Array of paths comma seperated
readMode="${5}"
thread="${6}"
mTag="${7}"
REFERENCE="${8}"
OUTPUT_DIR="${9}"

#################################### PARAMETERS
PIPELINE_STATUS_FILE="${OUTPUT_DIR}/VariantPipeline_status.txt"

# Create the file if it doesn't exist
if [ ! -f "$PIPELINE_STATUS_FILE" ]; then
# Try to create the file
    if ! touch "$PIPELINE_STATUS_FILE"; then
        echo "Error: Unable to create $PIPELINE_STATUS_FILE. Check directory permissions." 
        exit 1
    fi
fi


###1st check indexed genome:
# Check if all required BWA index files exist
if [ ! -f "${REFERENCE}.bwt" ] || \
   [ ! -f "${REFERENCE}.pac" ] || \
   [ ! -f "${REFERENCE}.ann" ] || \
   [ ! -f "${REFERENCE}.amb" ] || \
   [ ! -f "${REFERENCE}.sa" ]; then
    echo "Reference genome not indexed. Indexing now..."
    bwa index "$REFERENCE"
else
    echo "Reference genome is already indexed. Proceeding with Alignment..."
fi

# Check if FASTA index (.fai) exists
if [ ! -f "${REFERENCE}.fai" ]; then
cd $Samtools_Path	   
   
    echo "FASTA index (.fai) not found. Creating now..."
    samtools faidx "$REFERENCE"
else
    echo "FASTA index (.fai) already exists."
fi

# Check if sequence dictionary (.dict) exists
DICT_FILE="${REFERENCE%.*}.dict"
if [ ! -f "$DICT_FILE" ]; then
cd $GATK_PATH
    echo "Sequence dictionary (.dict) not found. Creating now..."
    ./gatk CreateSequenceDictionary -R "$REFERENCE"
else
    echo "Sequence dictionary (.dict) already exists."
fi



#Step 1: BWA-MEM
# Replace commas with newlines to split inputFilesPath by commas

echo "$inputFilesPath" | sed 's/,/\n/g' | while read -r fileInfo; do
  

	# Split the fileInfo into PATH , Name, AddString
	    filePath=$(echo "$fileInfo" | cut -d'#' -f1)
	    fileName=$(echo "$fileInfo" | cut -d'#' -f2)
	    addString=$(echo "$fileInfo" | cut -d'#' -f3)


# Replace literal tab characters with escaped \t
    addString=$(echo "$addString" | sed 's/[\t]/\\t/g' | tr -d "'")

#Defining file to check 

BAM_FILE="${OUTPUT_DIR}${fileName}.bam"
ADDBAM_FILE="${OUTPUT_DIR}${fileName}.add.bam"


stage="BWA|Samtools"
# Check if the pipeline_status.txt file contains the sample AND the stage is already recorded
if grep -q "^${fileName}|" "$PIPELINE_STATUS_FILE" && grep "^${fileName}|" "$PIPELINE_STATUS_FILE" | grep -Fq "|${stage}"; then
    # If both sample and stage exist in the file, that means this stage is already done for the sample
     echo "$fileName: $stage already done. Skipping Alignment..."
    
else
	 echo "Proceeding with BWA alignment of $filePath."  
	 

cd $BWA_PATH	    
    if [ $readMode = "SINGLE" ];
    then


       if [ $mTag = "yes" ];
       then
	bwa_cmd="./bwa mem -t $thread -M -R \"$addString\" \"$REFERENCE\" $filePath > \"${OUTPUT_DIR}${fileName}.sam\""
    	echo "Running BWA: $bwa_cmd"
	eval "$bwa_cmd"
       
       else
    	bwa_cmd="./bwa mem -t $thread -R \"$addString\" \"$REFERENCE\" $filePath > \"${OUTPUT_DIR}${fileName}.sam\""
    	echo "Running BWA: $bwa_cmd"
	eval "$bwa_cmd"
	fi
    elif [ $readMode = "PAIR" ];
    then
    filePath=$(echo "$filePath" | sed "s/*/ /g")
      if [ $mTag = "yes" ];
      then
      
       # Run BWA with correctly formatted -R argument
    	bwa_cmd="./bwa mem -t $thread -M -R \"$addString\" \"$REFERENCE\" $filePath > \"${OUTPUT_DIR}${fileName}.sam\""
 	# Debugging: Print the final command before execution
    	echo "Running BWA: $bwa_cmd"
    	# Execute the command
    	eval "$bwa_cmd"
      
       else
    	bwa_cmd="./bwa mem -t $thread -R \"$addString\" \"$REFERENCE\" $filePath > \"${OUTPUT_DIR}${fileName}.sam\""
    	echo "Running BWA: $bwa_cmd"
	eval "$bwa_cmd"
      
       fi
    fi

    
    
#START SAMTOOLS SORTING AND INDEXING
cd $Samtools_Path	   
echo "samtools view -Sb "${OUTPUT_DIR}""${fileName}.sam" | samtools sort -o "${OUTPUT_DIR}""${fileName}.bam""
echo "samtools index "${OUTPUT_DIR}""${fileName}.bam""

# Run conversion and sort
samtools view -Sb "${OUTPUT_DIR}${fileName}.sam" 2> "${OUTPUT_DIR}${fileName}.view.log" \
| samtools sort -o "${OUTPUT_DIR}${fileName}.bam" 2> "${OUTPUT_DIR}${fileName}.sort.log"

# Delete the log file if it's empty
if [ ! -s "${OUTPUT_DIR}${fileName}.view.log" ]; then
    rm -f "${OUTPUT_DIR}${fileName}.view.log"
    rm -f "${OUTPUT_DIR}${fileName}.sort.log"
   
fi
#DeleteLog

       if [ ! -f "${OUTPUT_DIR}${fileName}.bam" ]; then
    	echo "Error: BAM(${fileName}.bam) file was not created!"
    	exit 1
	fi
	
	# Check if BAM file exists and is non-empty
	if [ -s "${OUTPUT_DIR}${fileName}.bam" ]; then
	# Run samtools quickcheck to validate the BAM file
	
		if samtools quickcheck -v "${OUTPUT_DIR}${fileName}.bam"; then
        		
        		samtools index "${OUTPUT_DIR}""${fileName}.bam"
			# Check if the sample already has a line in the status file
			    if grep -q "^${fileName}|" "$PIPELINE_STATUS_FILE"; then
				# If the sample is already in the file, append the new stage to its existing line
				sed -i "s/^${fileName}.*/&|${stage}/" "$PIPELINE_STATUS_FILE"
			    else
				# If the sample is not in the file, create a new line with the sample and current stage
				echo "${fileName}|${stage}" >> "$PIPELINE_STATUS_FILE"
			    fi
			    
        		rm -f "${OUTPUT_DIR}${fileName}.sam"
        		echo "Deleted intermediate SAM file: ${OUTPUT_DIR}${fileName}.sam"
        		
        		 
    		else
        		echo "Warning: BAM (${fileName}.bam) failed quickcheck. SAM file NOT deleted."
    		fi
	    
	else
	    echo "Warning: BAM (${fileName}.bam) file is missing or empty. SAM (${fileName}.sam) file not deleted."
		exit 1
	fi       
       
fi

done
echo "++++++++++++++++++++++++++++++++++++ BWA MEM & Samtool COMPLETED +++++++++++++++++++++++++++++++++++++++++"





