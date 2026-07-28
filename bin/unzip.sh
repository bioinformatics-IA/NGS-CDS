#!/bin/sh

# Directory where the compressed files are located 
# /zipbin 
SOURCE_DIR="${1}"

# Directory where the files will be extracted
# /bin
BINARIES_DIR="${2}"


# Loop through all zip, bz2, and gz files in the source folder
echo "Source Dir is: $SOURCE_DIR"
echo "Bin Dir is: $BINARIES_DIR"

for file in "$SOURCE_DIR"/*; do
    # Ensure the comparison is done with proper quoting
    if [ "${file##*.}" = "zip" ]; then
        echo "Unzipping $file to $BINARIES_DIR"
        unzip -o "$file" -d "$BINARIES_DIR"
    elif [ "${file##*.}" = "bz2" ]; then
        echo "Extracting $file to $BINARIES_DIR"
        tar -xjf "$file" -C "$BINARIES_DIR"
    elif [ "${file##*.}" = "gz" ]; then
        echo "Extracting $file to $BINARIES_DIR"
        tar -xzf "$file" -C "$BINARIES_DIR"
    else
        echo "Skipping $file (not a supported archive type)"
    fi
done


############################
# Extract fastqc_v0.12.1.zip
#unzip -o "$SOURCE_DIR/fastqc_v0.12.1.zip" -d "$BINARIES_DIR"

# Extract Trimmomatic-0.39.zip
#unzip -o "$SOURCE_DIR/Trimmomatic-0.39.zip" -d "$BINARIES_DIR"

############################
# Extract STAR_2.7.11b.zip
#unzip -o "$SOURCE_DIR/STAR_2.7.11b.zip" -d "$BINARIES_DIR"

############################
# Extract bowtie2-2.5.4-source.zip
#unzip -o "$SOURCE_DIR/bowtie2-2.5.4-source.zip" -d "$BINARIES_DIR"

#########################
# Extract gatk-4.6.0.0.zip
#unzip -o "$SOURCE_DIR/gatk-4.6.0.0.zip" -d "$BINARIES_DIR"

############################
# Extract samtools-1.20.tar.bz2
#tar -xjf "$SOURCE_DIR/samtools-1.20.tar.bz2" -C "$BINARIES_DIR"

############################
# Extract bcftools-1.20.tar.bz2
#tar -xjf "$SOURCE_DIR/bcftools-1.20.tar.bz2" -C "$BINARIES_DIR"

# Extract subread-2.0.7-Linux-x86_64.tar.gz
#tar -xzf "$SOURCE_DIR/subread-2.0.7-Linux-x86_64.tar.gz" -C "$BINARIES_DIR"


############################
# Extract snpEff_latest_core.zip
#unzip -o "$SOURCE_DIR/snpEff_latest_core.zip" -d "$BINARIES_DIR"


echo "####################################################"
echo "####      ALL FILES UNZIPPED SUCCESSFULLY      ####"
echo "####################################################"
