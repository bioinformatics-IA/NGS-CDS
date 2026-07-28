#!/bin/bash
STARPath="${1}"
FILE_MODE="${14}"
cd $STARPath
make STAR
STEP="${2}"

case "$STEP" in
  "Step1")
    ARGS=""
    [ -n "$3" ] && ARGS+=" --runThreadN $3"
    [ -n "$4" ] && ARGS+=" --runMode $4"
    [ -n "$5" ] && ARGS+=" --genomeDir $5"
    [ -n "$6" ] && ARGS+=" --genomeFastaFiles $6"
    [ -n "$7" ] && ARGS+=" --sjdbGTFfile $7"
    [ -n "$8" ] && ARGS+=" --sjdbOverhang $8"
    [ -n "$9" ] && ARGS+=" --sjdbGTFtagExonParentTranscript $9"
    [ -n "${10}" ] && ARGS+=" --sjdbGTFfeatureExon ${10}"
    [ -n "${11}" ] && ARGS+=" --sjdbFileChrStartEnd ${11}"
    [ -n "${12}" ] && ARGS+=" --genomeSAindexNbases ${12}"
    [ -n "${13}" ] && ARGS+=" --genomeChrBinNbits ${13}"
    [ -n "${15}" ] && ARGS+=" --outBAMsortingBinsN ${15}"
    [ -n "${16}" ] && ARGS+=" --outSAMmapqUnique ${16}"

    ./STAR $ARGS
    ;;
    
  "Step2")
    ARGS=""
    [ -n "$3" ] && ARGS+=" --runThreadN $3"
    [ -n "$4" ] && ARGS+=" --runMode $4"
    [ -n "$5" ] && ARGS+=" --genomeDir $5"
    [ -n "$6" ] && ARGS+=" --readFilesIn $6"
    if [ "$FILE_MODE" = "exFAT" ]; then
      [ -n "$7" ] && ARGS+=" --readFilesCommand $7"
    fi
    [ -n "$8" ] && ARGS+=" --outSAMunmapped $8"
    [ -n "$9" ] && ARGS+=" --outFileNamePrefix $9"
    [ -n "${10}" ] && ARGS+=" --outSAMtype ${10}"
    [ -n "${11}" ] && ARGS+=" --twopassMode ${11}"
    [ -n "${12}" ] && ARGS+=" --limitBAMsortRAM ${12}"
    [ -n "${13}" ] && ARGS+=" --limitOutSJcollapsed ${13}"
    [ -n "${15}" ] && ARGS+=" --outBAMsortingBinsN ${15}"
    [ -n "${16}" ] && ARGS+=" --outSAMmapqUnique ${16}"

    ./STAR $ARGS
    ;;
esac



 #  "Step2") ./STAR --runThreadN ${3} --runMode "${4}" --genomeDir "${5}" --readFilesIn ${6} --readFilesCommand ${7} --outSAMunmapped ${8} --outFileNamePrefix "${9}" --sjdbOverhang 100 --outSAMtype BAM SortedByCoordinate --twopassMode Basic --limitBAMsortRAM 1000000 --limitOutSJcollapsed 1000000
#Basic STAR Options:

#Some advanced STAR options
#STAR --runThreadN $1 --runMode $2 --genomeDir $3 --genomeFastaFiles $4 --sjdbGTFfile $5 --sjdbOverhang $6

