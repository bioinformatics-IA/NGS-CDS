
myargs = commandArgs(trailingOnly=TRUE)
#### ARGUMENTS
# myargs[1]="/home/iffy/Project_Information/RNASeq/Zainab/ToCheck/Output/cohrot1_Results"
# myargs[2]="/home/iffy/NetBeansProjects/NGSGradle/app/R_Libraries"
# myargs[3]="FILE"
# myargs[4]="/home/iffy/Project_Information/RNASeq/Zainab/ToCheck/InputFiles/Cohort1_counts.csv"
###########################

setwd(file.path(myargs[1]))
cat("\nLoading Libraries...\n")

#suppressPackageStartupMessages(library(crayon,lib.loc=myargs[2]))
#suppressPackageStartupMessages(library(withr,lib.loc=myargs[2]))
#suppressPackageStartupMessages(library(tzdb,lib.loc=myargs[2]))
#suppressPackageStartupMessages(library(backports,lib.loc=myargs[2]))
suppressPackageStartupMessages(library(tidyverse,lib.loc=myargs[2]))
#suppressPackageStartupMessages(library(vroom, lib.loc = myargs[2]))

runMode<-myargs[3] # FOLDER or FILE
inputPath <- myargs[4]
###
read_in_feature_counts<- function(file){
  print("Reading Comments line........")
  cnt<- read_tsv(file, col_names =T, comment = "#")
  print("Reading columns......")
  #Removing columns Chr etc using Select and passing remaining to cnt
  cnt<- cnt %>% dplyr::select(-Chr, -Start, -End, -Strand, -Length)
  print(cnt)
  return(cnt)
}
cat("\n========== Preparing Read Counts ==========\n")

if(runMode == "FOLDER"){
  f_files<- list.files(inputPath, pattern = "*\\.featureCounts$", full.names = T)
  print(sprintf("Reading all featureCounts files from %s :",inputPath))
  print(f_files) #Read all files
  #MAPPING COUNTS FROM FILES
  # A list of data frames
  raw_counts<- map(f_files, read_in_feature_counts)
  print("Initial Read counts:")
  print(raw_counts)
  #Combined raw counts file (creates tibble)
  readcounts<- purrr::reduce(raw_counts, inner_join) 
  print("Combined rawcounts:")
  print(readcounts)
  readcounts <- as.data.frame(readcounts) #Converting tibble into data.frame
  names(readcounts)  <- gsub(".star.Aligned.sortedByCoord.out.bam.*", "", names(readcounts) ) #removing bam extension
  names(readcounts)  <-basename( names(readcounts))
  cat("Summary of the combined counts data frame:\n")
  print(str(readcounts))
  #Saving readcounts as tsv file
  cat(sprintf("Total Genes: %d | Total Samples: %d\n", nrow(readcounts), ncol(readcounts)))
  cat("\nCombined read counts have been successfully saved as 'CombinedCounts.tsv'.\n")
  write.table(readcounts, file = file.path(myargs[1], "CombinedCounts.tsv"), sep = "\t", quote = FALSE, row.names = FALSE)
  
  
  
  
}else if (runMode == "FILE"){
  # Check the file extension
  if (grepl("\\.csv$", inputPath, ignore.case = TRUE)) {
    # For CSV files, use comma as the delimiter
    readcounts <- read.table(inputPath, header = TRUE, sep = ",")
  } else if (grepl("\\.tsv$", inputPath, ignore.case = TRUE)) {
    # For TSV files, use tab as the delimiter
    readcounts <- read.table(inputPath, header = TRUE, sep = "\t")
  } else {
    stop("Error: Unsupported file format. Please provide a .csv or .tsv file.")
  }
  cat("Summary of the combined counts data frame:\n")
  print(str(readcounts))
  #Saving readcounts as tsv file
  cat("\nCombined read counts have been successfully saved as 'CombinedCounts.tsv'.\n")
  write.table(readcounts, file = file.path(myargs[1], "CombinedCounts.tsv"), sep = "\t", quote = FALSE, row.names = FALSE)
}

#SETTING GENEID AS ROW NAMES
if (tolower(colnames(readcounts)[1]) %in% c("geneid", "gene.id")) {
row.names(readcounts) <- readcounts[[1]] # c(readcounts$Geneid) #Setting row names from 1 2 3 to Geneid
} else {
  
  stop(paste0(
    "ERROR: First column is '", colnames(readcounts)[1], 
    "' but expected 'Geneid' or 'gene.id'. Please check your count file."
  ))
}
head(readcounts)
readcounts <- readcounts[,-c(1)] #removing extra gene column

#REMOVING ROWS WITH 0
cat("\nRemoving rows where all counts are 0 ... ")
before_rows <- nrow(readcounts)
readcounts <- readcounts[!rowSums(readcounts == 0) == ncol(readcounts), , drop = FALSE]
after_rows <- nrow(readcounts)
if (after_rows < before_rows) {
  message("Removed ", before_rows - after_rows, 
          " rows with all zero counts (", before_rows, " → ", after_rows, ").")
} else {
  message("No rows removed — no gene had all zero counts.")
}



#names(readcounts)  <- gsub(paste0(".*",p,sep=""), "", names(readcounts) ) #removing path from sample name
print("Initial Counts Dataframe (First 10 rows):")
print(head(readcounts, 10))

readcounts <- round(as.matrix(readcounts))  # Round any floating-point values

mode(readcounts) <- "integer"               # Ensure the data is stored as integers
cat("Summary of the combined counts data frame:\n")
print(str(readcounts))
cat(sprintf("Total Genes: %d | Total Samples: %d\n", nrow(readcounts), ncol(readcounts)))

cat("\nEnsuring all sample counts are numeric and converting to integers where needed...\n")

# Save the row names (gene names) before conversion
gene_names <- rownames(readcounts)
# Round and convert the matrix to integers, preserving row names
readcounts <- as.matrix(round(readcounts))  # Round first
# Apply the integer conversion while preserving row names
readcounts <- apply(readcounts, 2, function(x) as.integer(x))
# Restore the row names (gene names)
rownames(readcounts) <- gene_names
# Check the structure to ensure row names are retained and data is integers
str(readcounts)
# Confirm data type for the first few elements
#typeof(readcounts[1, 1])  # Should return "integer"
# Optional integrity check
if (!all(sapply(readcounts, is.integer))) {
  warning("⚠ Not all values are stored as integers. Check conversion.")
} else {
  cat("All values have been successfully converted to integers.\n")
}

################################################################################
#SAVING readcounts Dataframe

saveRDS(readcounts, file = "readcounts.RDS") 
cat("\n========== Finished Preparing Read Counts ==========\n")

