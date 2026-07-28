#!/bin/sh
Samtools_Path="${1}"
cd $Samtools_Path
#./configure --prefix="${1}"
#make
#make install
export PATH="${1}/bin":$PATH 

TOOL_NAME="${2}"
case "$TOOL_NAME" in
   "view") samtools view -Sb "$3" > "$4"
   ;;
   "sort") samtools sort -O "$3" "$4" > "$5" 
   ;;
   "index") samtools index "$3" 
   ;;
   
esac

