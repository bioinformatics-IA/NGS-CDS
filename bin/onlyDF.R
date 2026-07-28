
myargs = commandArgs(trailingOnly=TRUE)
#### ARGUMENTS
#myargs[1]="/home/iffy/ssh_folder/Output"
#myargs[2]="/home/iffy/NetBeansProjects/NGSGradle/app/R_Libraries"
#myargs[3]="/home/iffy/ssh_folder/Output/CombinedCounts.tsv"
###########################
setwd(file.path(myargs[1]))
cat("\nLoading Libraries...\n")

suppressPackageStartupMessages(library(tidyverse,lib.loc=myargs[2]))
suppressPackageStartupMessages(library(jsonlite,lib.loc=myargs[2]))

readcounts <- readRDS("readcounts.RDS")
cat("\n========== Step1: Preparing Dataframe ==========\n")

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
  message("Mismatch detected between column names of 'readcounts' and row names of 'df'.")
  message("\nColumn names in 'readcounts':")
  print(colnames(readcounts))
  
  message("\nRow names in 'df':")
  print(rownames(df))
  stop("\nPlease fix the name mismatch before proceeding.")
}

# Convert all character columns (except SampleName) to factors
#df[, -1] <- lapply(df[, -1], function(x) if (is.character(x)) as.factor(x) else x)

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
cat("------------------------------------------\n")
print(table(df[, columns_to_check]))
cat("------------------------------------------\n")

# Get the levels for each factor column (excluding SampleName)
factor_levels <- lapply(df[, -1], levels)
# Write to JSON
write_json(factor_levels, "factor_levels.json", pretty = TRUE, auto_unbox = TRUE)


saveRDS(df,file="df.RDS")


print("***********************   FINISED PREPARING DATAFRAME.")
