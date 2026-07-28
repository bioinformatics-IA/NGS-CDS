#!/bin/sh
STARPath="${1}"
FILE_MODE="${14}"
cd $STARPath
make STAR
STEP="${2}"
case "$STEP" in
   "Step1") ./STAR --runThreadN ${3} --runMode "${4}" --genomeDir "${5}" --genomeFastaFiles ${6} --sjdbGTFfile "${7}" --sjdbOverhang ${8} --sjdbGTFtagExonParentTranscript ${9} --sjdbGTFfeatureExon ${10} --sjdbFileChrStartEnd ${11} --genomeSAindexNbases ${12} --genomeChrBinNbits ${13} --outBAMsortingBinsN ${15} --outSAMmapqUnique ${16}
   ;;
   "Step2") if [ $FILE_MODE = "exFAT" ];
   then
   ./STAR --runThreadN ${3} --runMode "${4}" --genomeDir "${5}" --readFilesIn ${6} --readFilesCommand ${7} --outSAMunmapped ${8} --outFileNamePrefix "${9}" --outSAMtype ${10} --twopassMode ${11} --limitBAMsortRAM ${12} --limitOutSJcollapsed ${13} --outBAMsortingBinsN ${15} --outSAMmapqUnique ${16}
   else
   
   ./STAR --runThreadN ${3} --runMode "${4}" --genomeDir "${5}" --readFilesIn ${6} --outSAMunmapped ${8} --outFileNamePrefix "${9}" --outSAMtype ${10} --twopassMode ${11} --limitBAMsortRAM ${12} --limitOutSJcollapsed ${13} --outBAMsortingBinsN ${15} --outSAMmapqUnique ${16}
   fi

#  "Step2") ./STAR --runThreadN ${3} --runMode "${4}" --genomeDir "${5}" --readFilesIn ${6} --readFilesCommand ${7} --outSAMunmapped ${8} --outFileNamePrefix "${9}" --sjdbOverhang 100 --outSAMtype BAM SortedByCoordinate --twopassMode Basic --limitBAMsortRAM 1000000 --limitOutSJcollapsed 1000000
   ;;
   
esac
#Basic STAR Options:

#Some advanced STAR options
#STAR --runThreadN $1 --runMode $2 --genomeDir $3 --genomeFastaFiles $4 --sjdbGTFfile $5 --sjdbOverhang $6

