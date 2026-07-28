
myargs = commandArgs(trailingOnly=TRUE)
#### ARGUMENTS
#myargs[1]="/home/iffy/TestFiles/FINALOUTPUTS/"
#myargs[2]="/home/iffy/NetBeansProjects/NGSGradle/app/R_Libraries"
###########################


setwd(file.path(myargs[1]))

cat("\nLoading Libraries...\n")

suppressPackageStartupMessages(library(tidyverse,lib.loc=myargs[2]))
suppressPackageStartupMessages(library(jsonlite,lib.loc=myargs[2]))

cat("\n========== Preparing Read Counts ==========\n")

###########     STEP 1: READING ALL FEATURE COUNTS FILES
f_files<- list.files(myargs[1], pattern = "*\\.featureCounts$", full.names = T)
print(sprintf("Reading all featureCounts files from %s",myargs[1]))
print(f_files) #Read all files

#suppressPackageStartupMessages(library(vroom, lib.loc = myargs[2]))

####
read_in_feature_counts<- function(file){
  print("Reading Comments line........")
  cnt<- read_tsv(file, col_names =T, comment = "#")
  print("Reading columns......")
  #Removing columns Chr etc using Select and passing remaining to cnt
  cnt<- cnt %>% dplyr::select(-Chr, -Start, -End, -Strand, -Length)
  print(cnt)
  return(cnt)
}

#MAPPING COUNTS FROM FILES
# A list of data frames
raw_counts<- map(f_files, read_in_feature_counts)
print("Initial Read counts")
print(raw_counts)
#Combined raw counts file (creates tibble)
readcounts<- purrr::reduce(raw_counts, inner_join) 
print("Combined rawcounts:")
print(readcounts)
readcounts <- as.data.frame(readcounts) #Converting tibble into data.frame

#===========================================================
###########     STEP 2: PREPARING readcount dataframe
#GIVING MEANINGFUL NAMES TO SAMPLE COLUMN
names(readcounts)  <- gsub(".star.Aligned.sortedByCoord.out.bam.*", "", names(readcounts) ) #removing bam extension
names(readcounts)  <-basename( names(readcounts))

print("Initial Counts Dataframe (First 10 rows):")
print(head(readcounts, 10))

#p <- gsub("\\/", "\\.", myargs[1] ) 
#Saving readcounts as tsv file
cat(sprintf("Total Genes: %d | Total Samples: %d\n", nrow(readcounts), ncol(readcounts)))
cat("\nCombined read counts have been successfully saved as 'CombinedCounts.tsv'.\n")
write.table(readcounts, file = file.path(myargs[1], "CombinedCounts.tsv"), sep = "\t", quote = FALSE, row.names = FALSE)


#SETTING GENEID AS ROW NAMES
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

#row.names(readcounts) <- c(readcounts$Geneid) #Setting row names from 1 2 3 to Geneid
readcounts <- readcounts[,-c(1)] #removing extra gene column

#REMOVING ROWS WITH 0
readcounts <- readcounts[!rowSums(readcounts == 0) == ncol(readcounts), , drop = FALSE]


#names(readcounts)  <- gsub(paste0(".*",p,sep=""), "", names(readcounts) ) #removing path from sample name
print("Initial Counts Dataframe:")
print(readcounts)

print("Dataframe details:")

readcounts <- round(as.matrix(readcounts))  # Round any floating-point values
mode(readcounts) <- "integer"               # Ensure the data is stored as integers
str(readcounts)
print("NOTE: ALL SAMPLES COUNTS MUST BE NUMERIC")

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
typeof(readcounts[1, 1])  # Should return "integer"


################################################################################
print("Reading \'sample_groups.tsv\' file...")

#Prepaing Sample Groups data frame
df=read.csv("sample_groups.tsv",sep = "\t", header = TRUE)

#Set row names to the 1st 'SampleName' column
row.names(df) <- c(df$SAMPLENAME) 

print("Displaying details of data read from sample_group.tsv...")
print(str(df))

if (all(colnames(readcounts) %in% rownames(df)) && all(colnames(readcounts) == rownames(df))) {
  # If all colnames of readcounts are in and exactly match rownames of df
  print("All column names in readcounts are present and exactly match row names of df.")
  
} else {
  # If both conditions are false, change rownames in df to match colnames of readcounts
  mesagge("Mismatch detected between column names of 'readcounts' and row names of 'df'.")
  message("\nColumn names in 'readcounts':")
  print(colnames(readcounts))
  
  message("\nRow names in 'df':")
  print(rownames(df))
  stop("\nPlease fix the name mismatch before proceeding.")
}

# Ensure the columns except the first are correctly processed and assigned back to df
if(ncol(df)==2){
df[2] <- lapply(df[2], function(x) {
  if (is.character(x)) {
    
    # Trim whitespace and convert to lowercase for consistency
    x <- trimws(tolower(x))
    as.factor(x)
    
  } else {
    x
  }
})
}else{
df[, 2:ncol(df)] <- lapply(df[, 2:ncol(df)], function(x) {
  if (is.character(x)) {
   
    # Trim whitespace and convert to lowercase for consistency
    x <- trimws(tolower(x))
    as.factor(x)
 
    } else {
    x
  }
})
}


# Check the structure of the data to ensure columns are now factors
print(str(df))
# Get all columns except SampleName
columns_to_check <- names(df)[-1]  # Assuming first column is SampleName

# Dynamically create a cross-tabulation (table) for all remaining columns
print("Cross Table of Sample Data Frame:")
table(df[, columns_to_check])


#SAVING readcounts Dataframe

# Get the levels for each factor column (excluding SampleName)
#factor_levels <- lapply(df[, -1], levels)
factor_levels <- setNames(
  lapply(df[, -1, drop = FALSE], levels),
  colnames(df[, -1, drop = FALSE])
)

print("Factor Levels:")
print(factor_levels)

# Write to JSON
write_json(factor_levels, "factor_levels.json", pretty = TRUE)


saveRDS(readcounts, file = "readcounts.RDS") 
saveRDS(df,file="df.RDS")
cat("\n========== Finished Preparing Read Counts ==========\n")
