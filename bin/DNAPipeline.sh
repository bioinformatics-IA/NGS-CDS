#!/bin/sh
SAMTOOLS_PATH="${1}"
PICARD_PATH="${2}"
GATK_PATH="${3}"
INPUT_BAM="${4}"
OUTPUT_BAM="${5}"

#STEP 1: REMOVE DUPLICATES (Samtools or Picard)
OPT="${6}"
case "$OPT" in
   "SamOnly") cd $SAMTOOLS_PATH
#OPTIONS:
#-s Remove duplicate for single-end reads. By default, the command works for paired-end reads only.
#-S Treat paired-end reads as single-end reads.
   ./samtools fixmate $INPUT_BAM "$OUTPUT_BAM.samtools.bam"
   ./samtools rmdup -S $INPUT_BAM "$OUTPUT_BAM.samtools.bam"
   ;;
   "PicardOnly") java -jar $PICARD_PATH MarkDuplicates -I $INPUT_BAM -O "$OUTPUT_BAM.picard.bam" -M "$OUTPUT_BAM.metrices" --CREATE_INDEX true --VALIDATION_STRINGENCY SILENT --REMOVE_DUPLICATES true
   ;;
   "SamPicard") cd $SAMTOOLS_PATH
   
   ./samtools fixmate $INPUT_BAM "$OUTPUT_BAM.samtools.bam"
   ./samtools rmdup -S $INPUT_BAM "$OUTPUT_BAM.samtools.bam"
   
   java -jar $PICARD_PATH MarkDuplicates -I $INPUT_BAM -O "$OUTPUT_BAM.picard.bam" -M "$OUTPUT_BAM.metrices" --CREATE_INDEX true --VALIDATION_STRINGENCY SILENT --REMOVE_DUPLICATES true
   ;;
   
esac







#java -jar $PICARD_PATH RevertSam -I "${4}" -O "${5}.reverted.bam" --VALIDATION_STRINGENCY SILENT --ATTRIBUTE_TO_CLEAR FT --ATTRIBUTE_TO_CLEAR CO -SO "${6}"

#STEP 2: Samtofastq

#java -jar $PICARD_PATH SamToFastq -I "${5}.reverted.bam" --VALIDATION_STRINGENCY SILENT -F "${5}.1.fastq.gz" -F2 "${5}.2.fastq.gz"

#STEP 3: STAR
#cd $STAR_PATH
# YES
#./STAR --runThreadN ${7} --runMode "${8}" --genomeDir "${9}" --readFilesIn "${5}.1.fastq.gz" "${5}.2.fastq.gz" --readFilesCommand "${10}" --sjdbOverhang ${11} --outSAMtype ${12} --twopassMode ${13} --outFileNamePrefix "${9}/""${14}.star." --limitBAMsortRAM 1000000 --limitOutSJcollapsed 1000000

#STEP 4: MergeBAMAlignment

#java -jar $PICARD_PATH MergeBamAlignment -R "${15}" -UNMAPPED "${5}.reverted.bam" -ALIGNED "${9}/""${14}.star.Aligned.sortedByCoord.out.bam"  -O "${5}.bam" --INCLUDE_SECONDARY_ALIGNMENTS false --VALIDATION_STRINGENCY SILENT

#printf "\nMergeBamAlignment file created successfully: %s\n" "${5}.bam"

#STEP 5: Mark Duplicates
#java -jar $PICARD_PATH MarkDuplicates -I "${5}.bam" -O "${5}.dedupped.bam" -M "${5}.metrices" --CREATE_INDEX true --VALIDATION_STRINGENCY SILENT

#printf "\nMarkDuplicates file created successfully: %s\n" "${5}.dedupped.bam"
#STEP 6: GATK
#cd $GATK_PATH
#./gatk --java-options "-Xmx4G" SplitNCigarReads -R "${15}" -I "${5}.dedupped.bam" -O "${5}.split.bam"

#printf "\nSplitNCigarReads file created successfully: %s\n" "${5}.split.bam"
# Done AddOrReplaceReadGroups to solve error

#echo "./gatk --java-options "-Xmx4G" BaseRecalibrator -I "${5}.split.bam" -R "${15}" --known-sites "${17}" --known-sites "${18}" -O "${5}.recal_data.csv""

#./gatk --java-options "-Xmx4G" BaseRecalibrator -I "${5}.split.bam" -R "${15}" --known-sites "${17}" --known-sites "${18}" -O "${5}.recal_data.csv"

#printf "\nBaseRecalibrator file created successfully: %s\n" "${5}.recal_data.csv"

#echo "./gatk --java-options "-Xmx4G" ApplyBQSR -R "${16}" -I "${5}.split.bam" --bqsr-recal-file "${5}.recal_data.csv" -O "${5}.aligned.duplicates_marked.recalibrated.bam""
#./gatk --java-options "-Xmx4G" ApplyBQSR -R "${15}" -I "${5}.split.bam" --bqsr-recal-file "${5}.recal_data.csv" -O "${5}.aligned.duplicates_marked.recalibrated.bam"

#printf "\nApplyBQSR file created successfully: %s\n" "${5}.aligned.duplicates_marked.recalibrated.bam"

# SKIPPING INTERVAL LIST TOOLS

#./gatk --java-options "-Xmx4G" HaplotypeCaller -R "${15}" -I "${5}.aligned.duplicates_marked.recalibrated.bam" -O "${5}.hc.vcf.gz" --dont-use-soft-clipped-bases --dbsnp "${17}" -ERC GVCF

#printf "\nHaplotypeCaller file created successfully: %s\n" "${5}.hc.vcf.gz"
# SKIPPING MERGE VCF

#VARIATION FILTERATION
#./gatk --java-options "-Xmx4G" VariantFiltration -R "${15}" -V "${5}.hc.vcf.gz" -O "${5}.variant_filtered.vcf.gz" --filter-name "FS" --filter-expression "FS > 30.0" --filter-name "QD" --filter-expression "QD < 2.0"

#printf "\nVariantFiltration file created successfully: %s\n" "${5}.variant_filtered.vcf.gz"
	 



