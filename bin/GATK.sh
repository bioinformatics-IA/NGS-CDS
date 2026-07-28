#!/bin/sh

GATK_PATH="${1}"
cd $GATK_PATH

TOOL=$2
REFERENCE="${3}" #Refernce Genome

case "$TOOL" in
   "BaseRecalibrator") 
   if [ -z "$6" ]; then
    ./gatk --java-options "-Xmx4G -DGATK_STACKTRACE_ON_USER_EXCEPTION=true" BaseRecalibrator -I "$3" -R "$4" --known-sites "$5" -O "$6"
   else
    ./gatk --java-options "-Xmx4G -DGATK_STACKTRACE_ON_USER_EXCEPTION=true" BaseRecalibrator -I "$3" -R "$4" --known-sites "$5" --known-sites "$6" -O "$7"
   fi

   ;;
   "ApplyBQSR") ./gatk --java-options "-Xmx4G -DGATK_STACKTRACE_ON_USER_EXCEPTION=true" ApplyBQSR -R "$3" -I "$4" --bqsr-recal-file "$5" -O "$6"
   ;;
   "AnalyzeCovariates") java -jar $PICARD MergeBamAlignment -ALIGNED "$3" -UNMAPPED "$4" -O "$5" -R "$6"
   ;;
   "HaplotypeCaller") 
   BamFilesPath="${4}"
   OUTPUT_DIR="${5}"
   HaploOption="${6}"
    ERC="${7}" 
    G1="${8}"
    bamout="${9}"
   echo "Running HaplotypeCaller..."

#STEP 7: HaplotypeCaller: Call germline SNPs and indels via local re-assembly of haplotypes 
for bampath in $(echo $BamFilesPath | sed "s/,/ /g")

do
filename=$(basename "$bampath") 
BAMFILE_NAME="${filename%.*}" 

haplotype_cmd="./gatk --java-options "-Xmx4G -DGATK_STACKTRACE_ON_USER_EXCEPTION=true" HaplotypeCaller -I "${bampath}" -R "${REFERENCE}" -O "${OUTPUT_DIR}""${BAMFILE_NAME}_hap.vcf.gz" -ERC "$ERC""


#Single-sample GVCF calling with allele-specific annotations

if [ -n "$G1" ]; then
  haplotype_cmd="$haplotype_cmd $G1"
fi

#Variant calling with bamout to show realigned reads

if [ -n "$bamout" ]; then
  haplotype_cmd="$haplotype_cmd -bamout $bamout"
fi
#Single-sample GVCF calling (outputs intermediate GVCF)

echo "HaplotypeCaller Command: $haplotype_cmd"
  eval "$haplotype_cmd"

done


if [ $HaploOption = "JOINTMODE" ];
then
# List all _hap.vcf.gz files in the specified directory
echo "Running GenotypeGVCFs..."
VCF_FILES=$(find "$OUTPUT_DIR" -name "*_hap.vcf.gz")

echo "./gatk --java-options "-Xmx4G" GenotypeGVCFs -R "${REFERENCE}" -V "${VCF_FILES}"  -O "${OUTPUT_DIR}""joint_hap.vcf.gz" "
./gatk --java-options "-Xmx4G" GenotypeGVCFs -R "${REFERENCE}" -V "${VCF_FILES}"  -O "${OUTPUT_DIR}""joint_hap.vcf.gz" 

fi
   ./gatk --java-options "-Xmx4G -DGATK_STACKTRACE_ON_USER_EXCEPTION=true" HaplotypeCaller -R "$3" -I "$4" -O "$5" -ERC "$6" 
  
   ;;
   "SplitNCigarReads") ./gatk --java-options "-Xmx4G -DGATK_STACKTRACE_ON_USER_EXCEPTION=true" SplitNCigarReads -R "$3" -I "$4" -O "$5"
   ;;
   "VariantFiltration") 
   filterOptions="${6}"
   filterOptions=$(echo "$filterOptions" | sed 's/^"//; s/"$//')
   ./gatk --java-options "-Xmx4G -DGATK_STACKTRACE_ON_USER_EXCEPTION=true" VariantFiltration -R "$3" -V "$4" -O "$5" $filterOptions
   
   
   ;;
   "SelectVariants")
   selectVariantStr="${6}"
   selectVariantStr=$(echo "$selectVariantStr" | sed 's/^"//; s/"$//')
   ./gatk --java-options "-Xmx4G -DGATK_STACKTRACE_ON_USER_EXCEPTION=true" SelectVariants -R "$3" -V "$4" -O "$5" $selectVariantStr
   ;;
   "IndexFeatureFile") ./gatk --java-options "-Xmx4G -DGATK_STACKTRACE_ON_USER_EXCEPTION=true" IndexFeatureFile -I "$3"
   ;;
   "Mutect2") 
   ##########################################################################################  		    
   ###### SETTING PARAMETERS
   BamFilesPath="${4}" #Array of paths
   somaticOP="${5}"   #Somatic Options: TUMORN, TUMORM, TUMORO, TUMORMIT,TUMORFOR
   OUTPUT_DIR="${6}"
    outputS="${7}"
    NormalData="${8}" #Array of normal paths & name comma separated
    germlineResource="${9}" # Or GenomicInterval in Mitochondrial Mode
    PON="${10}"
    allele="${11}"
    f1r2="${12}"
    
    if [ -z "$outputS" ]; then
    
       # Convert comma-separated list to space-separated list
	bampath=$(echo "$BamFilesPath" | sed "s/,/ /g")
	filename=$(basename "$bampath") 
  	BAMFILE_NAME="${filename%.*}"
    else
      	 BAMFILE_NAME="$outputS"
      	
    fi
    
   ##########################################################################################  		    

		if [ "$somaticOP" = "TUMORN" ]; then  #TUMOR WITH MATCHED NORMAL (SINGLE)
		   
		    
		    values="$(echo "$NormalData" | cut -d',' -f1-2)"
		    # Split the values
		    NORMALPATH=$(echo "$values" | cut -d',' -f1)
	    	    NSAMPLE=$(echo "$values" | cut -d',' -f2)

		mutect_cmd="./gatk --java-options "-Xmx4G" Mutect2 -R "${REFERENCE}" -I "${BamFilesPath}" -I "${NORMALPATH}" -normal "${NSAMPLE}" --germline-resource "${germlineResource}" --panel-of-normals "${PON}" -O "${OUTPUT_DIR}""${BAMFILE_NAME}_mut.vcf.gz""

		elif [ "$somaticOP" = "TUMORM" ]; then   # TUMOR WITH MATCHED NORMAL (MULTIPLE)

			mutect_cmd="./gatk --java-options "-Xmx4G" Mutect2 -R "${REFERENCE}""
			for bampath in $(echo $BamFilesPath | sed "s/,/ /g")
			do
			filename=$(basename "$bampath") 
			BAMFILE_NAME="${filename%.*}" 
			mutect_cmd="$mutect_cmd -I "${bampath}""

			done

			while [ -n "$NormalData" ]; do
		       # Read the first pair
	    		values=$(echo "$NormalData" | cut -d',' -f1,2)
	    		
	    		# Split the values
	    		NORMALPATH=$(echo "$values" | cut -d',' -f1)
	    		NSAMPLE=$(echo "$values" | cut -d',' -f2)
			
			mutect_cmd="$mutect_cmd -I "${NORMALPATH}" -normal "${NSAMPLE}""
	    		
						    
	    		# Process the values
	    		# Remove the processed values from filterOptions
	    		NormalData=$(echo "$NormalData" | sed "s|^$values,||")
	    		
			done



			mutect_cmd="$mutect_cmd --germline-resource "${germlineResource}" --panel-of-normals "${PON}" -O "${OUTPUT_DIR}""${BAMFILE_NAME}_mut.vcf.gz""


		elif [ "$somaticOP" = "TUMORO" ]; then #TUMOR ONLY MODE

			mutect_cmd="./gatk --java-options "-Xmx4G" Mutect2 -R "${REFERENCE}" -I "${BamFilesPath}" -O "${OUTPUT_DIR}""${BAMFILE_NAME}_mut.vcf.gz""
		if [ -n "$germlineResource" ]; then
  			mutect_cmd="$mutect_cmd --germline-resource "${germlineResource}""
		fi

		if [ -n "$PON" ]; then
  			mutect_cmd="$mutect_cmd --PON "${PON}""
		fi


		elif [ "$somaticOP" = "TUMORMIT" ]; then # Mitochondrial mode
			
		mutect_cmd="./gatk --java-options "-Xmx4G" Mutect2 -R "${REFERENCE}" -L "${germlineResource}" --mitochondria-mode -I "${BamFilesPath}" -O "${OUTPUT_DIR}""${BAMFILE_NAME}_mut.vcf.gz""
	
		elif [ "$somaticOP" = "TUMORFOR" ]; then #Force calling mode

		if [ -n "$alleles" ]; then
		mutect_cmd="./gatk --java-options "-Xmx4G" Mutect2 -R "${REFERENCE}" -I "${BamFilesPath}" -alleles "${alleles}" -O "${OUTPUT_DIR}""${BAMFILE_NAME}_mut.vcf.gz""
		fi
		if [ -n "$f1r2" ]; then
		mutect_cmd="./gatk --java-options "-Xmx4G" Mutect2 -R "${REFERENCE}" -I "${BamFilesPath}" --f1r2-tar-gz "${f1r2}" -O "${OUTPUT_DIR}""${BAMFILE_NAME}_mut.vcf.gz""
		fi


		fi

#echo " Mutect2 command is:::: $mutect_cmd"
		eval "$mutect_cmd"

	   ;;
esac

