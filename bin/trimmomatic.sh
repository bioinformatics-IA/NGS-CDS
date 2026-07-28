#!/usr/bin/sh
#################################### PARAMETERS
TRIMMOMATIC_PATH="${1}"
MODE="${2}" #SINGLE, PAIR
INPUT_PATHS="${3}"  #Arrays of comma seperated paths and Pair seperated by #
OUTPUT_DIR="${4}" 
PHRED="${5}" #-phred33 or -phred64
LOG="${6}"

ADAPTER_FILE="${7}"
ILLUM_CLIP="${8}"

SLIDING="${9}"  
LEADING="${10}" 
TRAILING="${11}"
MINLEN="${12}" 

THREAD="${13}" 

MAXINFO="${14}" 
CROP="${15}"  
HEADCROP="${16}"  

echo "Creating Directory: $OUTPUT_DIR"

mkdir -p "$OUTPUT_DIR" || {
    echo "Error: Failed to create directory $OUTPUT_DIR" >&2
    exit 1
}

# Check if it was really created
if [ ! -d "$OUTPUT_DIR" ]; then
    echo "Output directory $OUTPUT_DIR not found!"
    exit 1
fi

###RUN TRIMMOMATIC

echo "================ RUNNING TRIMMOMATIC ..."

echo "Mode is : $MODE"
if [ "$MODE" = "SINGLE" ]; then

for inputpath in $(echo $INPUT_PATHS | sed "s/,/ /g") #Comma separated single paths
do

#Removing Extension to get File Name
filename=$(basename "$inputpath") 
# Extract the base name and extension
    case "$filename" in
        *.fastq.gz)
            FILE_NAME="${filename%.fastq.gz}"
            EXTENSION=".fastq.gz"
            ;;
        *.fq.gz)
            FILE_NAME="${filename%.fq.gz}"
            EXTENSION=".fq.gz"
            ;;
        *.fastq.bz2)
            FILE_NAME="${filename%.fastq.bz2}"
            EXTENSION=".fastq.bz2"
            ;;
        *.fq.bz2)
            FILE_NAME="${filename%.fq.bz2}"
            EXTENSION=".fq.bz2"
            ;;
        *.fastq)
            FILE_NAME="${filename%.fastq}"
            EXTENSION=".fastq"
            ;;
        *.fq)
            FILE_NAME="${filename%.fq}"
            EXTENSION=".fq"
            ;;
        *)
            echo "Unsupported file format: $filename"
            continue
            ;;
    esac


#######
trim_cmd="java -jar $TRIMMOMATIC_PATH SE $PHRED"

	
trim_cmd="$trim_cmd "${inputpath}" "${OUTPUT_DIR}${FILE_NAME}${EXTENSION}" ILLUMINACLIP:"$ADAPTER_FILE":"$ILLUM_CLIP""
	
#### ADDITIONAL PARAMETERS
	 	if [ -n "$THREAD" ]; then
  	trim_cmd="$trim_cmd -thread "${THREAD}""
  	
	fi
	
	if [ "$LOG" = "YES" ]; then
  	trim_cmd="$trim_cmd -trimlog "${FILE_NAME}.log""
  	
	fi


if [ -n "$LEADING" ]; then
 	trim_cmd="$trim_cmd LEADING:"${LEADING}""
 	fi
 	
 	if [ -n "$TRAILING" ]; then
 	trim_cmd="$trim_cmd TRAILING:"${TRAILING}""
 	fi
	
 	if [ -n "$SLIDING" ]; then
 	trim_cmd="$trim_cmd SLIDINGWINDOW:"${SLIDING}""
 	fi
 		
 	
 	if [ -n "$MINLEN" ]; then
 	trim_cmd="$trim_cmd MINLEN:"${MINLEN}""
 	fi
 	
 	if [ -n "$CROP" ]; then
 	trim_cmd="$trim_cmd CROP:"${CROP}""
 	fi
 	
 	if [ -n "$HEADCROP" ]; then
 	trim_cmd="$trim_cmd HEADCROP:"${HEADCROP}""
 	fi
 	
 	if [ -n "$MAXINFO" ]; then
 	trim_cmd="$trim_cmd MAXINFO:"${MAXINFO}""
 	fi

 	
####
 	echo "Trimmomatic command: $trim_cmd" 	

 	eval "$trim_cmd"
   
done
elif [ "$MODE" = "PAIR" ]; then

echo "Debug: I am inside PAIR"
echo "Debug: INPUT_PATHS: $INPUT_PATHS"
for inputpath in $(echo $INPUT_PATHS | sed "s/,/ /g")
do

#############################
#Seperate Input pair
# Read the first pair
	    
	    # Split the inputpath into PATH1 and PATH2
	    PATH1=$(echo "$inputpath" | cut -d'#' -f1)
	    PATH2=$(echo "$inputpath" | cut -d'#' -f2)
echo "Debug: Paths are: $PATH1 and $PATH2"	    
#############################
filename1=$(basename "$PATH1") 

#Removing Extension to get File Name
case "$filename1" in
    *.fastq.gz)
        FILE_NAME1="${filename1%.fastq.gz}"
        EXTENSION1=".fastq.gz"
         
        ;;
    *.fq.gz)
        FILE_NAME1="${filename1%.fq.gz}"
           EXTENSION1=".fq.gz"
         
        ;;
    *.fastq.bz2)
        FILE_NAME1="${filename1%.fastq.bz2}"
           EXTENSION1=".fastq.bz2"
         
        ;;
    *.fq.bz2)
        FILE_NAME1="${filename1%.fq.bz2}"
   EXTENSION1=".fq.bz2"
         
        ;;        
    *.fastq)
            FILE_NAME1="${filename1%.fastq}"
            EXTENSION1=".fastq"
            ;;
        *.fq)
            FILE_NAME1="${filename1%.fq}"
            EXTENSION1=".fq"
            ;;
        *)
            echo "Unsupported file format: $filename1"
            continue
            ;;
esac



filename2=$(basename "$PATH2")

#Removing Extension to get File Name
case "$filename2" in
    *.fastq.gz)
        FILE_NAME2="${filename2%.fastq.gz}"
        EXTENSION2=".fastq.gz"
        ;;
    *.fq.gz)
        FILE_NAME2="${filename2%.fq.gz}"
        EXTENSION2=".fq.gz"
        ;;
    *.fastq.bz2)
        FILE_NAME2="${filename2%.fastq.bz2}"
        EXTENSION2=".fastq.bz2"
        ;;
    *.fq.bz2)
        FILE_NAME2="${filename2%.fq.bz2}"
        EXTENSION2=".fq.bz2"
        ;;        
    *.fastq)
            FILE_NAME2="${filename2%.fastq}"
            EXTENSION2=".fastq"
            ;;
        *.fq)
            FILE_NAME2="${filename2%.fq}"
            EXTENSION2=".fq"
            ;;
        *)
            echo "Unsupported file format: $filename2"
            continue
            ;;
esac



trim_cmd="java -jar $TRIMMOMATIC_PATH PE $PHRED"

 	if [ -n "$THREAD" ]; then
  	trim_cmd="$trim_cmd -thread "${THREAD}""
  	
	fi
	
	if [ "$LOG" = "YES" ]; then
  	trim_cmd="$trim_cmd -trimlog "${FILE_NAME}.log""
  	
	fi
	
trim_cmd="$trim_cmd "${PATH1}" "${PATH2}" "${OUTPUT_DIR}${FILE_NAME1}${EXTENSION1}" "${OUTPUT_DIR}${FILE_NAME1}_U${EXTENSION1}" "${OUTPUT_DIR}${FILE_NAME2}${EXTENSION2}" "${OUTPUT_DIR}${FILE_NAME2}_U${EXTENSION2}" ILLUMINACLIP:"$ADAPTER_FILE":"$ILLUM_CLIP""
	
	
	if [ -n "$LEADING" ]; then
 	trim_cmd="$trim_cmd LEADING:"${LEADING}""
 	fi
 	
 	if [ -n "$TRAILING" ]; then
 	trim_cmd="$trim_cmd TRAILING:"${TRAILING}""
 	fi
	
 	if [ -n "$SLIDING" ]; then
 	trim_cmd="$trim_cmd SLIDINGWINDOW:"${SLIDING}""
 	fi
 		
 	
 	if [ -n "$MINLEN" ]; then
 	trim_cmd="$trim_cmd MINLEN:"${MINLEN}""
 	fi
 	
 	if [ -n "$CROP" ]; then
 	trim_cmd="$trim_cmd CROP:"${CROP}""
 	fi
 	
 	if [ -n "$HEADCROP" ]; then
 	trim_cmd="$trim_cmd HEADCROP:"${HEADCROP}""
 	fi
 	
 	if [ -n "$MAXINFO" ]; then
 	trim_cmd="$trim_cmd MAXINFO:"${MAXINFO}""
 	fi
 	
echo "Trimmomatic command: $trim_cmd" 	
 	eval "$trim_cmd"
done




fi


