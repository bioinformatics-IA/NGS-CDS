#!/bin/sh
FASTP_PATH="${1}"
MULTIQC_PATH="${2}"
readMode="${3}" # SINGLE/ PAIRED
inputPaths="${4}" # Single Comma separated, Pair: pair1#pair2,...
OUTPUT_DIR="${5}"
count="${6}"
addParameters="${7}"



OUT_SUBDIR="${OUTPUT_DIR}FastP${count}"

mkdir -p "$OUT_SUBDIR" || {
    echo "Error: Failed to create directory $OUT_SUBDIR" >&2
    exit 1
}


#echo "Inputpaths: $inputPaths"
#echo "OutputDir: $OUTPUT_DIR"
#echo "count: $count"
#echo "addParameters: $addParameters"

#Removing starting and ending double qoutes
addParameters=$(echo "$addParameters" | sed 's/^"//; s/"$//')


cd $FASTP_PATH
if [ $readMode = "SINGLE" ]; then
    for filePath in $(echo $inputPaths | sed "s/,/ /g")
    do

    filename=$(basename "$filePath") 
    #Extracting filename without extension
    case "$filename" in
        *.fastq.gz)
            FILE_NAME="${filename%.fastq.gz}"

            ;;
        *.fq.gz)
            FILE_NAME="${filename%.fq.gz}"

            ;;
            
        *)
            FILE_NAME="${filename%.*}"
    
            ;;
    esac

    fastp_cmd="./fastp -i "${filePath}" -o "${OUT_SUBDIR}/${filename}" --html "${OUT_SUBDIR}/${FILE_NAME}.html" --json "${OUT_SUBDIR}/${FILE_NAME}.json""

    if [ -n "$addParameters" ]; then
        fastp_cmd="$fastp_cmd $addParameters"
    fi

    #Check If files are already present/ Checking .html and .json files
    htmlFile="${OUT_SUBDIR}/${FILE_NAME}.html"
    jsonFile="${OUT_SUBDIR}/${FILE_NAME}.json"
    outputFile="${OUT_SUBDIR}/${filename}"

    if [ -s "$htmlFile" ] && [ -s "$jsonFile" ] && [ -s "$outputFile" ]; then
        echo "All output files ($outputFile, $htmlFile, $jsonFile) already exist. Skipping FastP."
    else
        echo "Proceeding with FastP"
        echo "Fastp Command: $fastp_cmd"
        eval $fastp_cmd 
            if [ $? -eq 0 ]; then
                echo "Fastp completed successfully for ${filename}"
            else
                echo "Fastp failed! Recheck ${filename}"
                exit 1
            fi

    fi

    done  
  
elif [ $readMode = "PAIR" ];   then
    
    
    for filePath in $(echo $inputPaths | sed "s/,/ /g")
    do

        PAIR1=$(echo "$filePath" | cut -d'#' -f1)
        PAIR2=$(echo "$filePath" | cut -d'#' -f2)
                
        filename1=$(basename "$PAIR1") 
        filename2=$(basename "$PAIR2") 

        #Extracting filename without extension
        case "$filename1" in
            *.fastq.gz)
                FILE_NAME1="${filename1%.fastq.gz}"
                ;;
            *.fq.gz)
                FILE_NAME1="${filename1%.fq.gz}"
                ;;
                
            *)
                FILE_NAME1="${filename1%.*}"
                ;;
        esac

        # Check if FILE_NAME1 ends with '_1' and remove it
        if expr "$FILE_NAME1" : ".*_1$" > /dev/null; then
            FILE_NAME1="${FILE_NAME1%_1}"
        fi


        fastp_cmd="    ./fastp -i "${PAIR1}" -I "${PAIR2}" -o "${OUT_SUBDIR}/${filename1}" -O "${OUT_SUBDIR}/${filename2}"  --detect_adapter_for_pe --html "${OUT_SUBDIR}/${FILE_NAME1}.html" --json "${OUT_SUBDIR}/${FILE_NAME1}.json""
        if [ -n "$addParameters" ]; then
            fastp_cmd="$fastp_cmd $addParameters"
        fi

        #Check If files are already present/ Checking .html and .json files
        htmlFile="${OUT_SUBDIR}/${FILE_NAME1}.html"
        jsonFile="${OUT_SUBDIR}/${FILE_NAME1}.json"
        outputFile1="${OUT_SUBDIR}/${filename1}"
        outputFile2="${OUT_SUBDIR}/${filename2}"

        if [ -f "$htmlFile" ] && [ -f "$jsonFile" ] && [ -f "$outputFile1" ] && [ -f "$outputFile2" ]; then
            echo "All output files ($outputFile1, $outputFile2, $htmlFile, $jsonFile) already exist. Skipping FastP."
        else
            
            echo "Fastp Command: $fastp_cmd"
            eval $fastp_cmd 
            # Check exit status
            if [ $? -eq 0 ]; then
                echo "Fastp completed successfully for ${FILE_NAME1}"
            else
                echo "Fastp failed! Recheck ${FILE_NAME1}"
                exit 1
            fi

        fi

    done 
        echo "Running MultiQC: $MULTIQC_PATH..."
        echo "Debug: $OUT_SUBDIR"
        #chmod +x "$MULTIQC_PATH"

        if ! "$MULTIQC_PATH" "$OUT_SUBDIR" -o "$OUT_SUBDIR" 2>/dev/null; then
   	 echo "Direct execution failed, trying with python -m multiqc..."
   	 python3 -m multiqc "$OUT_SUBDIR" -o "$OUT_SUBDIR"
	fi

        if [ $? -eq 0 ]; then
            echo "MultiQC completed successfully."
        else
            echo "MultiQC failed!"
            exit 1
        fi   
  
fi
