#!/bin/sh
TOOL=${1}
PICARD="${2}"

case "$TOOL" in
   "SamToFastq") java -jar $PICARD SamToFastq -I "${3}" -F "${4}" 
   ;;
   "MarkDuplicates") java -jar $PICARD MarkDuplicates -I "${3}" -O "${4}" -M "${5}"
   ;;
   "ReorderSam") java -jar $PICARD ReorderSam -I "${3}" -SD "${4}" -O "${5}"
   ;;
   "MergeBamAlignment") java -jar $PICARD MergeBamAlignment -ALIGNED "${3}" -UNMAPPED "${4}" -O "${5}" -R "${6}"
   ;;
   "MergeSamFiles") java -jar $PICARD MergeSamFiles -I "${3}" -I "${4}" -O "${5}"
   ;;
   "NormalizeFasta") java -jar $PICARD NormalizeFasta -I "${3}" -O "${4}"
   ;;
   "FastqToSam") java -jar $PICARD FastqToSam -F1 "${3}" -F2 "${4}" -O "${5}" -SM "${6}"
   #RG=rg0013
   ;;
   "RevertSam") java -jar $PICARD RevertSam -I "${3}" -O "${4}"
   ;;
   "CreateSequenceDictionary") java -jar $PICARD CreateSequenceDictionary -R "${3}" -O "${4}"
   ;;
   "AddOrReplaceReadGroups") java -jar $PICARD AddOrReplaceReadGroups -I "${3}" -O "${4}" -ID "${5}" -LB "${6}" -PL "${7}" -PU "${8}" -SM "${9}"
   ;;
esac


