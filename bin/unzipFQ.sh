#!/bin/sh

FILES_PATH= "/home/iffy/TestFiles/MouseExample/INPUTS" #"${1}"
#FILES= "${2}"

#printf "File Path is: %s" $FILES_PATH
sudo -S su
cd $FILES_PATH
pwd


#for fastq in $FILES
#do echo $fastq
#data=$(echo "$fastq" | tr , " ")
#gunzip -c $fastq
#echo $data
#done
#filename=$(basename -- "$fastq")

#extension="${filename##*.}"
#filename="${filename%.*}"
