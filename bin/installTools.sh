#!/bin/bash

# Directory where the compressed files are located
SOFTWARE_NAME="${1}"
ZIPPED_FILE="${2}"
BINARIES_DIR="${3}"
SOFTWARE_PATH="${4}"


if ! echo "$PASS" | sudo -S true > /dev/null 2>&1; then
    echo "Error: Invalid sudo password or no sudo access."
    exit 1
fi

# Function to install software and log output in real time
install_software() {
    SOFTWARE=$1
    INSTALL_DIR=$2
    BUILD_COMMANDS=$3

    echo "Installing $SOFTWARE..." | tee -a "$LOG_PATH"

    # Run build commands with real-time logging
    eval "$BUILD_COMMANDS" 2>&1 | stdbuf -oL -eL tee -a "$LOG_PATH"

    # Check for errors
    if [[ ${PIPESTATUS[0]} -ne 0 ]]; then
        echo "Error: Installation of $SOFTWARE failed! Check $LOG_PATH for details." | tee -a "$LOG_PATH"
        exit 1  # Stop script on error
    fi

    echo "$SOFTWARE installed successfully." | tee -a "$LOG_PATH"
}



case "$SOFTWARE_NAME" in
    fastp)
        echo "Unzipping $ZIPPED_FILE to $BINARIES_DIR"
        unzip -o "$ZIPPED_FILE" -d "$BINARIES_DIR"
        install_software "fastp" "$SOFTWARE_PATH" "cd $SOFTWARE_PATH && make clean && make"
        ;;
    STAR)
        echo "Unzipping $ZIPPED_FILE to $BINARIES_DIR"
        unzip -o "$ZIPPED_FILE" -d "$BINARIES_DIR"
        install_software "STAR" "$SOFTWARE_PATH" "cd $SOFTWARE_PATH && make STAR"
        ;;
    bwa)
        echo "Unzipping $ZIPPED_FILE to $BINARIES_DIR"
        unzip -o "$ZIPPED_FILE" -d "$BINARIES_DIR"
        install_software "bwa" "$SOFTWARE_PATH" "cd $SOFTWARE_PATH && CPATH=/usr/include LIBRARY_PATH=/lib/x86_64-linux-gnu LD_LIBRARY_PATH=/lib/x86_64-linux-gnu make"
        ;;
    samtools)    
        echo "Unzipping $ZIPPED_FILE to $BINARIES_DIR"
        tar -xjf "$ZIPPED_FILE" -C "$BINARIES_DIR"
        install_software "samtools" "$SOFTWARE_PATH" "cd $SOFTWARE_PATH && make"
        ;;
    bcftools)    
        echo "Unzipping $ZIPPED_FILE to $BINARIES_DIR"
        tar -xjf "$ZIPPED_FILE" -C "$BINARIES_DIR"
        install_software "bcftools" "$SOFTWARE_PATH" "cd $SOFTWARE_PATH && make"
        ;;
    HTS)    
        echo "Unzipping $ZIPPED_FILE to $BINARIES_DIR"
        tar -xjf "$ZIPPED_FILE" -C "$BINARIES_DIR"
        install_software "HTS" "$SOFTWARE_PATH" "cd \"$SOFTWARE_PATH\" && make && echo \"$PASS\" | sudo -S make install"
        ;;    
    ensembl-vep)
        echo "Unzipping $ZIPPED_FILE to $BINARIES_DIR"
        unzip -o "$ZIPPED_FILE" -d "$BINARIES_DIR"
        install_software "ensembl-vep" "$SOFTWARE_PATH" "echo $PASS | sudo -S cpanm -n List::MoreUtils DBD::mysql Test::Warnings XML::LibXML XML::LibXML::Reader Bio::SeqFeature::Lite && cd $SOFTWARE_PATH && echo $PASS | sudo -S perl INSTALL.pl --AUTO a --NO_HTSLIB --NO_TEST --NO_UPDATE --CACHEDIR $VEP_PATH/cache --SPECIES homo_sapiens --CACHE_VERSION 114 && export HTSLIB_DIR=$HTSLIB_PATH && echo $PASS | -S cpanm -n Bio::DB::HTS" #--local-lib=~/perl5 

        ;;
    *)
        echo "Error: Unknown software '$SOFTWARE_NAME'. Supported: fastp, STAR, bwa, samtools, bctools, HTS, ensembl-vep"
        exit 1
        ;;
esac











