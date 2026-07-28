#!/bin/sh
STEP="${1}" # 1 for fastqc and 2 for Reading data
MULTIQC_PATH="${2}"

OUT_SUBDIR="${5}"

case "$STEP" in
   "Step1")
   	mkdir -p "$OUT_SUBDIR" || {
    echo "Error: Failed to create directory $OUT_SUBDIR" >&2
    exit 1
}
   	FASTQC_PATH="${3}"
	cd $FASTQC_PATH
	echo "================ RUNNING FASTQC ..."
	printf "Input file(s) for FastQC are: %s %s " "${4}" $OUT_SUBDIR
	./fastqc ${3} -o "$OUT_SUBDIR"
    
   ;;
   "Step2") 
   
   unzip -p "${2}" "*/summary.txt" | less ;

   ;;
   
esac



#   	for f in "${2}*_fastqc.zip"; 
#	do 
#	unzip -p "${2}*_fastqc.zip" "*/summary.txt" | less ;

#	done
   
