#===============================================================================
#                         COMMAND LINE ARGUMENTS
#===============================================================================
myargs = commandArgs(trailingOnly=TRUE)

Sys.getenv("R_LIBS_USER") 
##############################################################################
 # myargs[1]="/mnt/ntfs/Output_RNASeq1/"
 # myargs[2]="/home/iffy/NetBeansProjects/NGSGradle/app/R_Libraries/"
 # myargs[3]="both"
 # myargs[4]=10  #count
 # myargs[5]=0.9 #percentage
 # myargs[6]="SINGLE"
 # 
 # myargs[7]="NO"
 # myargs[8]="CONDITION"
###########################################################################

dir.create(file.path(myargs[1], "DeSeqResults"))

 # Print R version and search path
cat("R version:", R.Version()$version.string, "\n")
#cat("Search path:\n"); print(search())
setwd(file.path(myargs[1]))
library(methods)
cat("methods in search():", "package:methods" %in% search(), "\n")

print("Setting libraries path...")
# Set the library path to your custom location
.libPaths(c(myargs[2], .libPaths())) 
.libPaths()

#===============================================================================
#                         LOADING LIBRARIES
#===============================================================================
cat("\nLoading Libraries...\n")
required_libs <- c("BiocGenerics","S4Vectors","IRanges","GenomicRanges", "SummarizedExperiment","DESeq2",
                   "RColorBrewer", "limma", "vsn","pheatmap", "PCAtools","Cairo", "gridExtra")

for (pkg in required_libs) {
  tryCatch({
    suppressPackageStartupMessages(library(pkg, lib.loc = myargs[2], character.only = TRUE))
    cat("Loaded", pkg, "\n")
  }, error = function(e) {
    cat("ERROR: Failed to load", pkg, "--", conditionMessage(e), "\n")
    quit(status = 1)
  })
}
#===============================================================================
#                         VARIABLES
#===============================================================================

option <- myargs[3]
counts <- as.numeric(myargs[4]) #counts Threshold
percentage <- as.numeric(myargs[5])
factMode= myargs[6] #Factors SINGLE or MULTIPLE
interaction= myargs[7]# YES or NO
# Load the count data
readcounts <- readRDS("readcounts.RDS")
df<-readRDS("df.RDS")

cat("\nPreview of Read Counts Matrix (readcounts):\n")
print(head(readcounts))
cat("\nPreview of Sample Metadata (df):\n")
print(df)

cat("\nDimensions of the Read Counts Matrix:\n")
cat(sprintf("Genes (rows): %d | Samples (columns): %d\n", nrow(readcounts), ncol(readcounts)))
cat("\n Dimensions of the Sample Metadata (df):\n")
cat(sprintf("Samples (rows): %d | Annotations (columns): %d\n", nrow(df), ncol(df)))

# Display names for matching check
cat("\n Checking that sample names in 'readcounts' match row names in 'df'...\n")
cat("\n Column names of readcounts:\n")
print(colnames(readcounts))

cat("\n Row names of sample metadata (df):\n")
print(rownames(df))


if (all(colnames(readcounts) == rownames(df))) {
  cat("\n Sample names are properly aligned: each column in 'readcounts' matches a row in 'df'.\n")
} else {
  cat("\n Mismatch detected!\n")
  cat("Each column name in 'readcounts' must exactly match a row name in 'df'.\n")
  stop("Sample name mismatch. Please fix the alignment between readcounts and sample metadata.")
}

#===============================================================================
#                         SINK RESET FUCTION
#===============================================================================
sink.reset <- function(){
  for(i in seq_len(sink.number())){
    sink(NULL)
  }
}

#===============================================================================
#                   DESeq FUNCTION
#===============================================================================
setwd(file.path(myargs[1],"DeSeqResults"))

# MASTER PDF FOR ALL FIGURES
# Cairo::CairoPDF(
#   file = "All_DESeq2_Figures.pdf",
#   width = 12,
#   height = 8
# )


cat("\n========== DESEQ MATRIX ANALYSIS STARTED ==========\n")

#===============================================================================
#                         1- LIBRARY SIZE BAR PLOT
#===============================================================================
cat("Generating LibrarySize_BarPlot.png\n")

CairoPNG(file ="LibrarySize_BarPlot.png", width = 11,  height = 6, units = "in", res = 300)
# Adjust margin sizes (bottom, left, top, right)
par(mar = c(10, 6, 6, 2) + 0.1)  # par(mar = c(bottom, left, top, right))
par(mgp = c(4, 1, 0)) # For  Labels mgp = c(title, axis labels, axis line)
par(cex.main = 1.6)            # title size
par(cex.lab = 1.5)             # axis label size
    librarySizes <- barplot(colSums(readcounts),  
                            names=colnames(readcounts),
                            col= rainbow(ncol(readcounts)),#  brewer.pal(8, "Dark2"), 
                            las=2, # makes labels vertical (90°)
                            cex.names = 0.6,                    # SMALL sample names
                            cex.axis = 0.8,
                            ylim = c(0, max(colSums(readcounts)) * 1.1),
                            main="Barplot of Library Sizes",
                            sub = paste("Total Samples:", ncol(readcounts)),
                            ylab = "Total Counts",
                            space = 0.2)
invisible(dev.off())

#STEP 1: Preparing dds Matrix

  if(factMode=="MULTIPLE"){
    if(interaction == "YES"){
      #List of interaction terms
      interaction1=as.list(strsplit(myargs[8], ",")[[1]])
     # interaction2=myargs[7]
      design_formula=formula(paste("~", paste(paste(names(df)[2:ncol(df)], collapse = " + "),paste(" + ", paste(c(interaction1), collapse = "+"))))) 
    }#yes interaction
    else if(interaction == "NO"){ #Selects all colums from Second to the Last e.g: ~Gender + Disease + Drug + Time
      design_formula <- formula(paste("~", paste(names(df)[2:ncol(df)], collapse = " + "))) #For all columns
    }#No interaction
    
    print("Creating dds Matrix for following design formula: ")
    print(design_formula)
    dds <- DESeqDataSetFromMatrix(countData = readcounts,colData = df ,design = design_formula)

    
    
  }else  if(factMode=="SINGLE"){
    
    # Ensure second column of df is a factor
    df[[2]] <- as.factor(df[[2]])
    # Create design formula based on the second column of df
    design_formula <- formula(paste("~", paste(names(df)[2])))
    
    print("Creating dds Matrix for following design formula: ")
    print(design_formula)
    
      dds <- DESeqDataSetFromMatrix(countData = readcounts, colData = df ,design = design_formula)
  }

  print("Initial DESeqDataSet (dds) Matrix:")
  print(head(dds))
  
  # Track initial dimensions
  dims_initial <- dim(assay(dds))
  genes_initial <- dims_initial[1]
  total_samples <- dims_initial[2]
  
  cat("Dimensions of assay matrix: (Genes x Samples): ", dims_initial[1], "x", dims_initial[2], "\n")
  
  print("Sample Metadata (colData):")
  print(colData(dds))
  
  genes_after_counts <- genes_initial
  genes_final <- genes_initial
  #OPTIONAL: FILTERING
  if (myargs[3] == "none") {
    cat("   - No filtering applied. All genes retained.\n")
  }
  if (myargs[3] == "countsonly" || myargs[3] == "both") {
    cat("\nPerforming gene-level filtering based on read counts...\n")
    cat("   - Retaining genes with total counts ≥", counts, "\n")
    keep <- rowSums(counts(dds)) >= counts
    dds <- dds[keep,]
    genes_after_counts <- nrow(dds)
  }
  if (myargs[3] == "percentageonly" || myargs[3] == "both") {
    cat("   - Filtering genes based on ≥",percentage * 100,"% zero counts across samples...\n")
    
    # Get count matrix after possible filtering
    count_mat <- counts(dds)
    
    # Determine presence/absence
    total_samples <- ncol(count_mat)
    row_zeros <- rowSums(count_mat == 0)
    present_flags <- row_zeros < (percentage * total_samples)
    
    # Filter genes marked as "Present"
    dds <- dds[present_flags,]
    
    # Optional: Add Absent/Present + Combined_Result back to counts matrix if needed
    counts_df <- as.data.frame(count_mat)
    counts_df$Absent_or_Present <- ifelse(row_zeros >= 0.9 * total_samples, "Absent", "Present")
    counts_df$Combined_Result <- rowSums(count_mat)
  }
  
  genes_final <- nrow(dds)
  cat("\nFiltering complete.\n")
  cat("\n======================================================\n")
  cat("                FILTERING SUMMARY                    \n")
  cat("======================================================\n")
  cat(sprintf(" Initial Gene Count                  : %d\n", genes_initial))
  
  if (myargs[3] == "countsonly" || myargs[3] == "both") {
    genes_dropped_counts <- genes_initial - genes_after_counts
    cat(sprintf(" - Dropped by Total Counts Filter    : %d\n", genes_dropped_counts))
  }
  
  if (myargs[3] == "percentageonly" || myargs[3] == "both") {
    # If 'both', baseline is genes_after_counts; if 'percentageonly', baseline is genes_initial
    baseline <- if(myargs[3] == "both") genes_after_counts else genes_initial
    genes_dropped_pct <- baseline - genes_final
    cat(sprintf(" - Dropped by Zero-Percentage Filter : %d\n", genes_dropped_pct))
  }
  
  total_dropped <- genes_initial - genes_final
  pct_retained <- (genes_final / genes_initial) * 100
  
  cat("------------------------------------------------------\n")
  cat(sprintf(" Total Genes Filtered Out            : %d\n", total_dropped))
  cat(sprintf(" Final Gene Count Remaining          : %d (%.2f%% retained)\n", genes_final, pct_retained))
  cat(sprintf(" Final Matrix Dimensions             : %d Genes x %d Samples\n", genes_final, total_samples))
  cat("======================================================\n\n")
  
  
  
  
  
  cat("DESeqDataSet (dds) Matrix after Filtering:")
  print(dds)
  
  cat("Dimensions after filtering: (Genes x Samples): ", dim(assay(dds)), "\n")
  
  print("Filtered Assay Matrix:")
  print(head(assay(dds)))

    #STEP 2: RUNING DESEQ PIPELINE
  
  print("Running DESeq Analysis....")
  dds <- DESeq(dds)
  # Get the results
  cat("\nExtracting results...\n")
  res <- results(dds)
  print(res)
  print("DESeqDataSet (dds) after DESeq2 Analysis:")
  print(dds)
  print("Coefficient Estimates (log2 fold changes for model terms):")
  print(head(coef(dds)))
  print("Cook's Distances (influence of individual samples on model fit):")
  print(head(assays(dds)[["cooks"]]))
  print("Hat Matrix Diagonal (leverage values per gene/sample):")
  print(head(assays(dds)[["H"]]))
  print("Estimated Mean Normalized Counts (fitted by the model):")
  print(head(assays(dds)[["mu"]]))
  
 #Log2 fold change estimates for each gene and each model coefficient.
  cat("Saved log2 fold change estimates to 'dds_coef.csv'.\n")
  write.table(coef(dds), "dds_coef.csv", sep = ",", quote = FALSE, row.names = FALSE, col.names = TRUE)
  #Cook’s distance for each gene/sample — a measure of outlier influence on model fit.
  cat("Saved Cook’s distance diagnostics to 'dds_cooks.csv'. Helps detect influential samples.\n")
  write.table(assays(dds)[["cooks"]], "dds_cooks.csv", sep = ",", quote = FALSE, row.names = FALSE, col.names = TRUE)
  #Diagonal of the hat matrix (leverage values) for each gene/sample — indicates how much each point influences the fitted values.
  cat("Saved leverage (hat matrix diagonal) values to 'dds_H.csv'.\n")
  write.table(assays(dds)[["H"]], "dds_H.csv", sep = ",", quote = FALSE, row.names = FALSE, col.names = TRUE)
  #Estimated mean normalized counts for each gene/sample under the model.
  cat("Saved model-estimated mean normalized counts to 'dds_mu.csv'.\n")
    write.table(assays(dds)[["mu"]], "dds_mu.csv" , sep = ",", quote = FALSE, row.names = FALSE, col.names = TRUE)
  
  
  
  
  print("Listing Available Results Names from DESeq2 Model:")
  resultName = paste(strsplit(resultsNames(dds),' '),collapse=',')
  cat("Results Names (compact view): ", resultName, "\n")
  cat("\nDetailed Results Names (structured list):\n")
  print(resultsNames(dds))
  
  
  #===============================================================================
  #                         NORMALIZED COUNTS
  #===============================================================================
  
  
  normalized_counts <- as.data.frame( counts(dds, normalized=TRUE))
  print("DESeq2 Normalized Counts:")
  print(head(normalized_counts))
  write.table(normalized_counts, file = "normalized_counts.tsv", sep = "\t", quote = FALSE, col.names = NA)
  cat("\nNormalized read counts have been saved to 'normalized_counts.tsv'.\n")
  
  cat("\nSaving dds object...")
  print(file.path(myargs[1]))
  setwd(file.path(myargs[1]))
  saveRDS(dds, file = "dds.RDS") #Save single object
  sink.reset()
  cat("\nSaved dds object as dds.RDS")
#-------------------------------------#
  cat("\nChecking DeSeqResults path...\n")
  start_time <- Sys.time()
  print(file.path(myargs[1], "DeSeqResults"))
  cat("Calling dir.exists()...\n")
  flush.console()
  
  result <- NA
  tryCatch({
    result <- dir.exists(file.path(myargs[1], "DeSeqResults"))
    cat("dir.exists() result:", result, "\n")
  }, error = function(e) {
    cat("Error in dir.exists():", e$message, "\n")
  })
  
  cat("Time taken: "); print(Sys.time() - start_time)
  
  if (isTRUE(result)) {
    cat("\nTrying to change directory...\n")
    setwd(file.path(myargs[1], "DeSeqResults"))
    cat(" Directory changed successfully.\n")
  } else if (isFALSE(result)) {
    stop("ERROR: 'DeSeqResults' folder not found.")
  } else {
    stop("ERROR: Unexpected result from dir.exists().")
  }
  #-------------------------------------#
    
  #Normalization factors calculated by estimateSizeFactors
  #A normalization factor below one indicates that the library size will be scaled down This is also equivalent to scaling the counts upwards in that sample
  # a factor above one scales up the library size and is equivalent to downscaling the counts.
  #dds@colData$sizeFactor
  #####################################
  # COUNTS PLOTS: BOXPLOT, DENSITYPLOT
  ####################################
  # Log-transform counts
  # Calculate cpm counts for unnormalized and normalized data
  readcounts_raw <- counts(dds, normalized = FALSE)
  logcounts <- log2(readcounts_raw + 1)        
  print("\nLogcounts:\n")
  print(head(logcounts))
    lognormalized_counts <- log2(normalized_counts + 1) 
    print("\nLognormalized Counts:\n")
    print(head(lognormalized_counts))
    
  cat("\nGenerating Counts_Boxplots (Boxplot_Unnormalized_LogCounts.png, Boxplot_Normalized_LogCounts.png)")
  
  # Save the boxplots in one file using CairoPNG
  CairoPNG(file = "Boxplot_Unnormalized_LogCounts.png",  width = 14,  height = 7, units = "in", res = 300)
  par(mar = c(10, 6, 6, 2) + 0.1) # bottom, left, top, right
  par(cex.main = 1.8)
  par(cex.lab = 1.4)
    boxplot(logcounts, 
            main="Unnormalized Log2 Counts", 
            col=rainbow(ncol(readcounts)), 
            xaxt = "n",
            ylab = "Log2 Counts",
    pars = list(boxwex = 0.6))
    
    # Add rotated labels
    axis(1, at = 1:ncol(logcounts), labels = FALSE, tick = TRUE) # Draw ticks only
    
    text(x = 1:ncol(logcounts), y = par("usr")[3] - 0.8, 
         labels = colnames(logcounts), srt = 90, adj = 1, xpd = TRUE, 
         cex = 0.65)
    invisible(dev.off())
    
    CairoPNG(file = "Boxplot_Normalized_LogCounts.png",  width = 14,  height = 7, units = "in", res = 300)
    par(mar = c(10, 6, 6, 2) + 0.1) # bottom, left, top, right
    par(cex.main = 1.8)
    par(cex.lab = 1.4)
    
    boxplot(lognormalized_counts, main="Normalized Log2 Counts", col=rainbow(ncol(normalized_counts)), xaxt = "n", ylab = "Log2 Counts")
    # Add rotated labels
    axis(1, at = 1:ncol(lognormalized_counts), labels = FALSE, tick = TRUE) # Draw ticks only
    text(x = 1:ncol(lognormalized_counts), y = par("usr")[3] - 0.8, 
         labels = colnames(lognormalized_counts), srt = 90, adj = 1, xpd = TRUE, cex = 0.65)
    
  # Close the graphical device
  invisible(dev.off())
  
  
  CairoPNG(file = "Counts_Boxplot_Combined.png",  width = 14,  height = 7, units = "in", res = 300)
  par(mfrow = c(2, 1))
  par(mar = c(5, 6, 3, 2) + 0.1) # bottom, left, top, right
  par(cex.main = 1.2)
  par(cex.lab = 1.1)
  boxplot(logcounts, 
          main="Unnormalized Log2 Counts", 
          col=rainbow(ncol(readcounts)), 
          xaxt = "n",
          ylab = "Log2 Counts")
  
  # Add rotated labels
  axis(1, at = 1:ncol(logcounts), labels = FALSE, tick = FALSE) # Draw ticks only
  
  text(x = 1:ncol(logcounts), y = par("usr")[3] - 0.4, labels = colnames(logcounts), srt = 90, adj = 1, xpd = TRUE, 
       cex = 0.65)
  
  boxplot(lognormalized_counts, main="Normalized Log2 Counts", col=rainbow(ncol(normalized_counts)), xaxt = "n", ylab = "Log2 Counts")
  # Add rotated labels
  axis(1, at = 1:ncol(lognormalized_counts), labels = FALSE, tick = FALSE) # Draw ticks only
  text(x = 1:ncol(lognormalized_counts), y = par("usr")[3] - 0.4, labels = colnames(lognormalized_counts), srt = 90, adj = 1, xpd = TRUE, cex = 0.65)
  
  # Close the graphical device
  invisible(dev.off())

  # ==============================================================================
  # SECTION 1: SEPARATE UNNORMALIZED PLOT
  # ==============================================================================
  cat("\nGenerating Counts_Density Plots (Counts_Densityplot_Unnormalized.png)...\n")
  
  CairoPNG(file = "Counts_Densityplot_Unnormalized.png",  width = 11,  height = 6, units = "in", res = 300)
  par(mar = c(8, 6, 3, 2) + 0.1)
  par(cex.main = 1.3)
  par(cex.lab = 1.2)
  
  # Pre-define color palette
  colors_unnorm <- rainbow(ncol(logcounts))
  
  unnorm_max <- max(vapply(1:ncol(logcounts), function(i) {
    max(density(logcounts[, i])$y)
  }, FUN.VALUE = numeric(1)))
  
  # Set explicit x boundaries based on global data range
  x_range_unnorm <- range(logcounts, na.rm = TRUE)
  # Calculate metrics for the unnormalized dataset
  avg_bw_unnorm <- mean(vapply(1:ncol(logcounts), function(i) density(logcounts[, i])$bw, numeric(1)))
  sample_size_n <- nrow(logcounts)
  
  d_first <- density(logcounts[, 1])
  plot(d_first, main="Unnormalized Counts Distribution", col=colors_unnorm[1], 
       sub = paste0("N = ", sample_size_n, "   Avg Bandwidth = ", round(avg_bw_unnorm, 4)), # <-- Added subtitle
       lwd=2, ylim=c(0, unnorm_max), xlim=x_range_unnorm, xlab="Log2 Counts", ylab="Density")
  
  for(i in 2:ncol(logcounts)) {
    lines(density(logcounts[, i]), col=colors_unnorm[i], lwd=2)
  }
  invisible(dev.off())
  
  # ==============================================================================
  # SECTION 2: SEPARATE NORMALIZED PLOT
  # ==============================================================================
  cat("Generating Counts_Densityplot_Normalized.png...\n")
  
  CairoPNG(file = "Counts_Densityplot_Normalized.png",  width = 11,  height = 6, units = "in", res = 300)
  par(mar = c(8, 6, 3, 2) + 0.1)
  par(cex.main = 1.3)
  par(cex.lab = 1.2)
  
  colors_norm <- rainbow(ncol(lognormalized_counts))
  
  norm_max <- max(vapply(1:ncol(lognormalized_counts), function(i) {
    max(density(lognormalized_counts[, i])$y)
  }, FUN.VALUE = numeric(1)))
  
  x_range_norm <- range(lognormalized_counts, na.rm = TRUE)
  
  # Calculate metrics for the normalized dataset
  avg_bw_norm <- mean(vapply(1:ncol(lognormalized_counts), function(i) density(lognormalized_counts[, i])$bw, numeric(1)))
  d_first_norm <- density(lognormalized_counts[, 1])
  plot(d_first_norm, main="Normalized Counts Distribution", 
       sub = paste0("N = ", sample_size_n, "   Avg Bandwidth = ", round(avg_bw_norm, 4)), # <-- Added subtitle
       col=colors_norm[1], lwd=2, ylim=c(0, norm_max), xlim=x_range_norm, xlab="Log2 Counts", ylab="Density")
  
  for(i in 2:ncol(lognormalized_counts)) {
    lines(density(lognormalized_counts[, i]), col=colors_norm[i], lwd=2)
  }
  invisible(dev.off())
  
  # ==============================================================================
  # SECTION 3: COMBINED PLOT
  # ==============================================================================
  cat("Generating Counts_Densityplot_Combined.png...\n")
  
  CairoPNG(file = "Counts_Densityplot_Combined.png",  width = 11,  height = 6, units = "in", res = 300)
  par(mfrow = c(2, 1))
  par(mar = c(5.5, 6, 3, 2) + 0.1)
  par(cex.main = 1.0)
  par(cex.lab = 0.8)
  
  # Top Plot: Unnormalized Combined
  d_comb_first <- density(logcounts[, 1])
  
  # Calculate the average bandwidth across all unnormalized samples
  avg_bw_unnorm <- mean(vapply(1:ncol(logcounts), function(i) density(logcounts[, i])$bw, numeric(1)))
  sample_size_n <- nrow(logcounts) # Number of genes/features
  
  plot(d_comb_first, main="Unnormalized Counts Distribution", col=colors_unnorm[1], 
       sub = paste0("N = ", sample_size_n, "   Avg Bandwidth = ", round(avg_bw_unnorm, 4)), # <-- Added subtitle
       lwd=2, ylim=c(0, unnorm_max), xlim=x_range_unnorm, xlab="Log2 Counts", ylab="Density")
  
  for(i in 2:ncol(logcounts)) {
    lines(density(logcounts[, i]), col=colors_unnorm[i], lwd=2)
  }
  
  # Bottom Plot: Normalized Combined
  d_comb_first_norm <- density(lognormalized_counts[, 1])
  # Calculate the average bandwidth across all normalized samples
  avg_bw_norm <- mean(vapply(1:ncol(lognormalized_counts), function(i) density(lognormalized_counts[, i])$bw, numeric(1)))
  
  plot(d_comb_first_norm, main="Normalized Counts Distribution", 
       sub = paste0("N = ", sample_size_n, "   Avg Bandwidth = ", round(avg_bw_norm, 4)), # <-- Added subtitle
       col=colors_norm[1], lwd=2, ylim=c(0, norm_max), xlim=x_range_norm, xlab="Log2 Counts", ylab="Density")
  for(i in 2:ncol(lognormalized_counts)) {
    lines(density(lognormalized_counts[, i]), col=colors_norm[i], lwd=2)
  }
  
  invisible(dev.off())

# Individual plots 
    n <- ncol(logcounts)
  # Better layout
  rows <- ceiling(sqrt(n))   # Number of rows
  cols <- ceiling(n / rows)  # Number of columns
  
  inch_width  <- cols * 3.5
  inch_height <- rows * 3.0
    cat("\nGenerating Unnormalized_vs_Normalized_Profiles.png\n")
  CairoPNG( file = "Unnormalized_vs_Normalized_Profiles.png",  width = inch_width,
    height = inch_height,   units = "in",  # Explicitly tell Cairo to use inches
    res = 130  )
  
  # Better spacing and larger text
  par(
    mfrow = c(rows, cols),
    mar = c(4, 4, 3, 1),
    oma = c(5, 5, 5, 1),
    cex.main = 2.0,   # sample title size
    cex.axis = 1.1,
    cex.lab = 1.4
  )
  
  for(g in 1:ncol(logcounts)) {
    
    # Unnormalized
    y1 <- logcounts[, g]
    # Normalized
    y2 <- lognormalized_counts[, g]
    
    x <- seq_along(y1)
    
    # Set common y-limits
    ylim_range <- range(c(y1, y2), na.rm = TRUE)
    
    # Base plot
    plot(x,y1,pch = 16, cex = 0.25, col = rgb(1, 0, 0, 0.12),  main = colnames(logcounts)[g],
      xlab = "",ylab = "",  xaxt = "n",  ylim = ylim_range)
    
    # Add normalized points
    points( x,y2,  pch = 16,cex = 0.25,  col = rgb(0, 0, 1, 0.12) )
    
    # Smooth trends
    lines(lowess(x, y1, f = 0.05),col = "red",  lwd = 2.5  )
    
    lines(  lowess(x, y2, f = 0.05),col = "blue",   lwd = 2.5 )
    
    # Median lines
    abline(  h = median(y1, na.rm = TRUE),col = "red",lty = 2,lwd = 1.5 )
    
    abline( h = median(y2, na.rm = TRUE), col = "blue", lty = 2,lwd = 1.5 )
  }
  
  # Main title
  mtext("Unnormalized vs Normalized Log2 Expression Profiles",side = 3,  outer = TRUE,cex = 2.0,
    font = 2, line = 2.0)
  
  # Axis labels
  mtext(  "Gene Index",  side = 1,outer = TRUE,  line = 2,cex = 1.5 )
  
  mtext("Log2 Counts",side = 2,   outer = TRUE, line = 2,  cex = 1.5)
  
  # Global legend
  legend(   "topright", legend = c("Unnormalized", "Normalized"),   col = c("red", "blue"),
    lwd = 3,bty = "n",cex = 1.4  )
  
  invisible(dev.off())
  
  
    #####################################
  # MA PLOTS: dds, res, shrunken_res
  #  log ratios (A) against the mean expression levels (M) 
  ####################################
  
  cat("Generating MA(DeSeq)_Plots.png\n")
   #1 MA PLOT Between logfoldchange & mean of normalized counts
  CairoPNG(file ="MA(DeSeq)_Plots.png", width = 11,  height = 6, units = "in", res = 300)
  # Set up the plotting area with 1 row and 2 columns
  par(mfrow = c(1, 2))
  
  DESeq2::plotMA(dds,main="MA PLOT of DESeq Object(dds)" , ylim = c(-5, 5), alpha = 0.4) 
  abline(h = 0, col = "red")  # Add blue horizontal line at y = 0
  DESeq2::plotMA(res,main="MA PLOT of DESeq Results Object(res)" , ylim = c(-5, 5), alpha = 0.4)
  abline(h = 0, col = "red")  # Add blue horizontal line at y = 0
  
  invisible(dev.off())
  ##############################################################################
  # Assume you already have a DESeqDataSet object (dds) and results have been generated
  # Get results names and filter out the Intercept
  cat("\nExtracting result coefficient names from DESeq2 object...\n")
  result_names <- resultsNames(dds)
  cat("Filtering out 'Intercept' to get coefficients for shrinkage...\n")
  coef_to_shrink <- result_names[!result_names %in% "Intercept"]
  print(coef_to_shrink)  # Show the coefficients that will be processed
  
  # Create a list to store shrunken results
  shrunken_results_list <- list()
  cat("Applying log fold change shrinkage using 'apeglm' for each contrast...\n")
  # Apply lfcShrink to each coefficient except Intercept
  for (coef in coef_to_shrink) {
    cat(paste0("  - Shrinking coefficient: ", coef, "\n"))
    shrunken_results <- lfcShrink(dds, coef=coef, type="apeglm")
    shrunken_results_list[[coef]] <- shrunken_results
  }
  cat("Shrinkage complete. Displaying summary of shrunken results list:\n")
  print(names(shrunken_results_list))
  
  cat("\nGenerating MA(DeSeq)_ShrunkenResultsPlot.png\n")
  # Set up a PNG file to save the plots
  CairoPNG(file = "MA(DeSeq)_ShrunkenResultsPlot.png", width = 11,  height = 6, units = "in", res = 300)
  
  # Set up the plotting area with 1 row and 2 columns
  par(mfrow = c(length(coef_to_shrink), 2))  # Adjust rows based on number of comparisons
  
  # Loop through each coefficient to generate MA plots
  for (coef in coef_to_shrink) {
    # Original results
    original_results <- results(dds, name = coef)
    DESeq2::plotMA(original_results, main = paste("MA Plot -", coef), ylim = c(-5, 5), alpha = 0.4)
    abline(h = 0, col = "blue", lwd=1)  # Add horizontal line at y=0
    
    # Shrunken results
    shrunken_results <- shrunken_results_list[[coef]]
    DESeq2::plotMA(shrunken_results, main = paste("Shrunken MA Plot -", coef), ylim = c(-5, 5))
   # abline(h = 0, col = "blue")  # Add horizontal line at y=0
  }
    # Close the graphical device
  invisible(dev.off())
  # ==============================================================================
  # Regularized Log Transformation (rld)
  # ==============================================================================
 
  if (file.exists("rld_data.rds")) {
    cat("\nLoading existing Regularized log transformation from file...\n")
    rld <- readRDS("rld_data.rds")
  } else {
    cat("\nCalculating Regularized log transformation...\n")
  rld <- rlog(dds, blind=FALSE)
  cat("Saving rld object to disk...\n")
  saveRDS(rld, file = "rld_data.rds")
  }
  print("\nRegularized log:")
  print(rld)
  print(rowData(rld))

  # ==============================================================================
  # Variance Stabilizing Transform (VST)
  # ==============================================================================
  # Transform count data using the variance stablilizing transform
  if (file.exists("vst_data.rds")) {
    cat("\nLoading existing Variance stabilizing transform from file...\n")
    VST <- readRDS("vst_data.rds")
  } else {
    cat("\nCalculating Variance stabilizing transform...\n")
   VST <- tryCatch({
    vst(dds,nsub=nrow(dds)) 
    
  },error=function(err){
    # If the first attempt fails, try with default settings
    tryCatch({
      vst(dds)
    }, error = function(err) {
      # If that also fails, use a subset of rows with mean count > 5
      return(vst(dds, nsub = sum(rowMeans(counts(dds, normalized = TRUE)) > 5)))
    })
  })
   cat("Saving VST object to disk...\n")
   saveRDS(VST, file = "vst_data.rds")
  }#else
  print("\nVariance stablilizing transform:")
  print(VST)
  
  #####################################
  # MDS PLOTS: rld, VST
  #  represent variance between samples 
  ####################################
 cat("Generating MDS(DeSeq)_Plots.png\n")
  
  CairoPNG(file ="MDS(DeSeq)_Plots.png",width = 12, height = 6, units = "in", res = 300)
  # Set up the plotting area with 1 row and 2 columns
  par(mfrow = c(1, 2))
  
  limma::plotMDS(assay(rld),gene.selection = "common",main="MDS PLOT of rlog Object(rld)",
                 col=brewer.pal(8, "Dark2"), cex = 0.65)
  limma::plotMDS(assay(VST),gene.selection = "common",main="MDS PLOT of VST Object(VST)",
                 col=brewer.pal(8, "Dark2"), cex = 0.65)
  
  invisible(dev.off())
################################  
  #3 Plot Dispersion Estimates
  cat("Generating Dispersion(DeSeq)_Plot.png\n")
  
  CairoPNG(file ="Dispersion(DeSeq)_Plot.png",width = 11, height = 6, units = "in", res = 300)
  plotDispEsts(dds ,main="Dispersion Plot of dds")
  invisible(dev.off())
  
  
    
  #SAMPLE DISTANCE for HEATMAP
  sampleDists <- dist( t( assay(rld) ) )
  print("\nSample Distance using rld:")
  print(sampleDists)
  sampleDistMatrix <- as.matrix( sampleDists ) #Will be used to draw heatmap
  #Checking out total number of groups to draw on heatmap
  t<- ncol(df)#ncol(colData(rld))-1
  a<-""
  for (num in 2:t){
    
    a<- paste(a,rld[[num]], sep="-" )
  }
  #print(a)
  rownames(sampleDistMatrix) <- paste(a)#Adding groups names to row names
  
  

  
  #####################################
  ## Cluster Plot (Dendrogram)
  ## VST, rld
  #####################################
  
  
  cat("Generating Clustering Plots (Cluster_rlog_Plot.png, Cluster_VST_Plot.png and Combined_Cluster_Plot.png)\n")
  
  CairoPNG(file = "Cluster_rlog_Plot.png", width = 11, height = 7, units = "in", res = 300)
  par(mar = c(5, 5, 6, 2), cex.main = 1.3,cex.lab = 1.0,cex.axis = 1.0)
  #Distance Matix
  tryCatch({
    d_rld <- dist(t(assay(rld)))
    cat("dist() on rld done.\n")
  }, error = function(e) {
    cat("ERROR during dist(rld):", conditionMessage(e), "\n")
  })
  cat("Running hclust() on rld...\n")
  # Hierarchical clustering
  
  tryCatch({
    hc_rld <- hclust(d_rld)
    cat("hclust(rld) done.\n")
    
    plot(hc_rld, labels = colnames(assay(rld)), 
         main = "Hierarchical Clustering of Samples (rlog)",
         sub = "", xlab = "", ylab = "Distance", hang = -1, cex = 0.55)
    cat("Plotting rld dendrogram done.\n")
  }, error = function(e) {
    cat("ERROR during hclust(rld):", conditionMessage(e), "\n")
  })
  
  invisible(dev.off())
  
  cat("Generating Cluster_VST_Plot.png\n")
  CairoPNG(
    file = "Cluster_VST_Plot.png",width = 11, height = 7, units = "in", res = 300 )
  
  par(mar = c(5, 4, 6, 2),  cex.main = 1.3,   cex.lab = 1.1,  cex.axis = 1.0  )
  
  cat("Running dist() on VST assay...\n")
  tryCatch({
    d_vst <- dist(t(assay(VST)))
    cat("dist() on VST done.\n")
  }, error = function(e) {
    cat("ERROR during dist(VST):", conditionMessage(e), "\n")
  })
  cat("Running hclust() on VST...\n")
  tryCatch({
    hc_vst <- hclust(d_vst)
    cat("hclust(VST) done.\n")
    plot(hc_vst, labels = colnames(assay(VST)), 
         main = "Hierarchical Clustering of Samples (VST)",
         sub = "", xlab = "", ylab = "Distance", hang = -1, cex = 0.55)
    cat("Plotting VST dendrogram done.\n")
  }, error = function(e) {
    cat("ERROR during hclust(VST):", conditionMessage(e), "\n")
  })
  
  
  invisible(dev.off())
  
  cat("Generating Combined_Cluster_Plot.png\n")
  
  CairoPNG( file = "Combined_Cluster_Plot.png",  width = 14, height = 6, units = "in", res = 300)
  
  par(mfrow = c(1, 2),    mar = c(3.5, 4, 2.5, 0.5)+0.1,    oma = c(0, 1.5, 3, 1.5),    cex.main = 1.0,
    cex.lab = 0.9,    cex.axis = 0.8  )
  
  # rlog plot
  plot(
    hc_rld,
    labels = colnames(assay(rld)),
    main = "rlog Clustering",
    sub = "",
    xlab = "",
    ylab = "Distance",
    hang = -1,
    cex = 0.35
  )
  
  # VST plot
  plot(
    hc_vst,
    labels = colnames(assay(VST)),
    main = "VST Clustering",
    sub = "",
    xlab = "",
    ylab = "Distance",
    hang = -1,
    cex = 0.35
  )
  
  mtext(
    "Hierarchical Clustering of RNA-seq Samples",
    side = 3,
    outer = TRUE,
    cex = 1.1,
    font = 2,
    line = 0.6
  )
  
  invisible(dev.off())
  
  
  #################################################################
  #####################################
  ## MeanSD PLOTS
  ## normalized counts, rlog,VST
  #####################################
  # Check rows with counts greater than 0
  notAllZero <- (rowSums(counts(dds)) > 0)
  
  # Generate meanSdPlot for each dataset, customize, and store them as ggplot objects
  p1 <- meanSdPlot(log2(counts(dds, normalized = TRUE)[notAllZero, ]+1), plot = FALSE)$gg +
    ggtitle("Mean-SD Plot for Normalized Counts") + 
    theme_minimal() +
    theme(plot.title = element_text(hjust = 0.5, face = "bold", size = 11)) 
  
  p2 <- meanSdPlot(assay(rld)[notAllZero, ], plot = FALSE)$gg +
    ggtitle("Mean-SD Plot for Regularized Log Transformation") +
    theme_minimal() +
    theme(plot.title = element_text(hjust = 0.5, face = "bold", size = 11)) 
  
  p3 <- meanSdPlot(assay(VST)[notAllZero, ], plot = FALSE)$gg +
    ggtitle("Mean-SD Plot for VST Transformation") +
    theme_minimal() +
    theme(plot.title = element_text(hjust = 0.5, face = "bold", size = 11)) 
  # Save the combined plots in a single file
  cat("Generating meanSD(DeSeq)_Plots.png \n")
  
  CairoPNG(file = "meanSD(DeSeq)_Plots.png",width = 8, height = 5.5, units = "in", res = 300)
  
  # Arrange the plots in one row with 3 columns
  grid.arrange(p1, p2, p3, nrow = 3)
  
  # Close the graphical device
  invisible(dev.off())
  
######################
# HEATMAPS: 
# Euclidean Sample Distances (RLOG)
# Top Variable Genes
######################  
  #Heatmap of Euclidean sample distances after rlog transformation.
  colours = colorRampPalette( rev(brewer.pal(9, "Blues")) )(255)
  #12
  cat("Generating Heatmap1(DeSeq)_Plot.png \n")
  CairoPNG(file ="Heatmap1(DeSeq)_Plot.png",width = 12, height = 8.5, units = "in", res = 300)
  print(pheatmap(sampleDistMatrix,trace = "none",
                 col=colours,
                 main="Heatmap of Euclidean sample distances after rlog transformation.",
                 fontsize = 11,
                 fontsize_row = 8,
                 fontsize_col = 8,
                 
                 border_color = NA,
                 
                 clustering_method = "complete",
                 
                 treeheight_row = 50,
                 treeheight_col = 50,
                 
                 angle_col = 90
                 )
        )
  invisible(dev.off())
 
  #Let us select the 35 genes with the highest variance across samples
  #===============================================================================
  #           HIGH-QUALITY HEATMAP 
  #===============================================================================
    # Select top variable genes
  
  #===============================================================================
  #           HIGH-QUALITY HEATMAP FOR ~100 SAMPLES
  #===============================================================================

  topVarGenes <- head( order( rowVars( assay(rld) ), decreasing=TRUE ), 35 )
  CairoPNG(file ="Heatmap2(DeSeq)_Plot.png",width = 16, height = 8.5, units = "in", res = 300)
  print(pheatmap( assay(rld)[ topVarGenes, ], 
                  scale="row",
                  trace="none", 
                  dendrogram="column",
                  col = colorRampPalette( rev(brewer.pal(9, "RdBu")) )(255),
                  fontsize_main = 20,
                  main="Top 35 Genes with Variance across Samples"))
  invisible(dev.off())
  
  ######################
  # PCA PLOTS: 
  # rlog, VST
  ######################  
  
  p1<- DESeq2::plotPCA(rld, intgroup=names(colData(rld))[2:t] ,ntop=500 ) + 
    ggtitle("DESeq2 PCA PLOT rlog")+
    theme(
      plot.title = element_text(size = 12, face = "bold", hjust = 0.5),
      axis.title = element_text(size = 11, face = "bold"),
      axis.text = element_text(size = 9)
    )
  p2<- DESeq2::plotPCA(VST, intgroup=names(colData(VST))[2:t] ,ntop=500 ) + 
    ggtitle("DESeq2 PCA PLOT VST")+
    theme(
      plot.title = element_text(size = 12, face = "bold", hjust = 0.5),
      axis.title = element_text(size = 11, face = "bold"),
      axis.text = element_text(size = 9)
    )
  #13 PLOT PCA of rlog
  CairoPNG(file ="PCA(DeSeq)_rlogPlot.png",width = 9, height = 6, units = "in", res = 300)
  # Arrange the plots in one row with 3 columns
  grid.arrange(p1)
  
  invisible(dev.off())
  
  CairoPNG(file ="PCA(DeSeq)_VSTPlot.png",width = 9, height = 6, units = "in", res = 300)
  
    grid.arrange(p2)
  
  invisible(dev.off())
  
  CairoPNG(file ="PCA(DeSeq)_Plots.png",width = 9, height = 6, units = "in", res = 300)
 
  grid.arrange(p1, p2, ncol = 2)
  
  invisible(dev.off())
  
  # ============================================================================
  # ADVANCED PCA & BIPLOTS (PCAtools)
  # ============================================================================
  # Perform PCA on the VST data (Preferable method For RNA seq Data)
  p <- PCAtools::pca(assay(VST), metadata = colData(VST), removeVar = 0.1)
  
  # Extract scores and loadings for dynamic axis limits
  scores <- p$rotated
  loadings <- p$loadings
  
  # Check for any missing values in the scores or loadings
  if (any(is.na(scores)) || any(is.na(loadings))) {
    warning("Missing values detected in scores or loadings. These will be removed.")
    # Filter out rows with missing values
    scores <- na.omit(scores)
    loadings <- na.omit(loadings)
  }
  
  # Calculate dynamic axis limits (using the first two principal components)
  x_limits <- range(c(scores[,1], loadings[,1]), na.rm = TRUE)
  y_limits <- range(c(scores[,2], loadings[,2]), na.rm = TRUE)
  
  # Add padding to the axis limits (10% of the range)
  padding_x <- 0.1 * diff(x_limits)
  padding_y <- 0.1 * diff(y_limits)
  
  x_limits <- x_limits + c(-padding_x, padding_x)
  y_limits <- y_limits + c(-padding_y, padding_y)
  
  ###############################
  #BIPLOTS: p, conditions wise plots 
  ##############################
  CairoPNG(file ="BiPlot(DeSeq)_Plot.png",width = 11, height = 7, units = "in", res = 300)
  # Plot the PCA biplot without replacing scales
  suppressMessages(print(
    PCAtools::biplot( p, showLoadings = TRUE,
      labSize = 3.5,     # Decrease sample label font size
      pointSize = 2.5,   # Keep the point size
      sizeLoadingsNames = 3.5,  # Decrease gene label font size
      title = "PCA Biplot",
      max.overlaps = 100
    ) +
      ggplot2::theme(
        plot.title = element_text(size = 14, face = "bold", hjust = 0.5)
      ) +
      ggplot2::coord_cartesian(xlim = x_limits, ylim = y_limits)  # Apply dynamic limits
  ))
  invisible(dev.off())

  CairoPNG(file ="BiPlot2(DeSeq)_Plot.png",width = 11, height = 7, units = "in", res = 300)
  # Plot the PCA biplot without replacing scales
  suppressMessages(print(
    PCAtools::biplot( p, showLoadings = TRUE,
                      lab = NULL,     # Decrease sample label font size
                      pointSize = 2.5,   # Keep the point size
                      sizeLoadingsNames = 3.5,  # Decrease gene label font size
                      title = "PCA Biplot(Clean Vector View)",
                      max.overlaps = 100
    ) +
      ggplot2::theme(
        plot.title = element_text(size = 14, face = "bold", hjust = 0.5)
      ) +
      ggplot2::coord_cartesian(xlim = x_limits, ylim = y_limits)  # Apply dynamic limits
  ))
  invisible(dev.off())
  
  ##########################
  #EACH FACTOR BIPLOT
  
  # Define a vector of colors to use for the plots
  colors <- c("red", "blue", "green", "orange", "cyan", "magenta", "yellow","aquamarine","blueviolet",
              "darkslategray", "firebrick4" ,"cadetblue")  # Add more colors if needed
  set.seed(123)  # Set seed for reproducibility
  # Initialize an empty plot list to store individual plots
  plot_list <- list()
  
  # Generate and store each plot in the list
  for (i in 2:ncol(df)) {
    
    #unique_levels <- length(unique(p$metadata[[i]]))  # Get the number of unique levels
    # Repeat colors if there are not enough colors
    # Randomly select colors from the color vector for the unique levels
    #selected_colors <- sample(colors, size = unique_levels, replace = TRUE)
    group_var <- names(p$metadata)[i]
      groups <- unique(as.character(p$metadata[[names(p$metadata)[i]]]))
    unique_levels <- length(unique(as.character(p$metadata[[names(p$metadata)[i]]])))
    
    # repeat colors if needed
    selected_colors <- rep(colors, length.out = unique_levels)
    
    # IMPORTANT: named mapping (THIS fixes your issue)
    color_map <- setNames(selected_colors, groups)
      plot <- PCAtools::biplot(p, 
                             lab = NULL, 
                             colby = names(p$metadata)[i], 
                             hline = 0, 
                             vline = 0, 
                             legendPosition = 'right',
                             max.overlaps = 100)+
        ggplot2::scale_color_manual(values = color_map)+
        ggplot2::ggtitle(paste("PCA Biplot -", group_var))+
        ggplot2::theme(plot.title = element_text(size = 11, face = "bold"))

      file_name <- paste0("BiPlot_", group_var, ".png")
      
      Cairo::CairoPNG(file = file_name,
                      width = 11, height = 6, units = "in", res = 300)
            print(plot)
      invisible(dev.off())
      
          plot_list[[i - 1]] <- plot  # Store the plot
  }
  
  # Dynamically stretches panel size based on the number of generated factor plots
  grid_rows <- ceiling(length(plot_list) / 2)
  
  # Combine all the plots into a single grid layout
  combined_plot <- patchwork::wrap_plots(plot_list, ncol = 2)  # Arrange in 2 columns
  
  # Save the combined plot as an image
  Cairo::CairoPNG(file = "BiPlot(Deseq)_Combined.png",width = 13, height = grid_rows * 4.5, units = "in", res = 300)
  print(combined_plot)
  invisible(dev.off())
  
  
#####################
# LOADINGS PLOT:   All components, 1st 4 components
####################
  cat("\nGenerating Loadings(DeSeq)_Plot1.png")
  #Loadings
  CairoPNG(file ="Loadings(DeSeq)_Plot1.png",width = 12, height = 7.5, units = "in", res = 300)
  print(PCAtools::plotloadings(p,
                     components = getComponents(p, c(1:10)),
                     rangeRetain = 0.1,
                     labSize = 3.0,
                     absolute = FALSE,
                     title = 'PCA Loadings plot ',
                     subtitle = 'Misc PCs',
                     caption = 'Top 10% most influential genes (variables)',
                     shape = 23, shapeSizeRange = c(1, 10),
                     col = c('white', 'blue',"red"),
                     drawConnectors = FALSE)+
              ggplot2::theme(
            plot.title = element_text(size = 14, face = "bold", hjust = 0.5),
            plot.subtitle = element_text(size = 11, face = "italic", hjust = 0.5),
            
            axis.title = element_text(size = 11, face = "bold"),
            axis.text = element_text(size = 9),
            axis.text.x = element_text(size = 9, angle = 90, vjust = 0.5, hjust = 1),
            legend.title = element_text(size = 11, face = "bold"),
            legend.text = element_text(size = 9)
          )
        )
  
  
  invisible(dev.off())
  
  cat("\nGenerating Loadings(DeSeq)_Plot2.png")
  
  CairoPNG(file ="Loadings(DeSeq)_Plot2.png",width = 12, height = 8, units = "in", res = 300)
  print(PCAtools::plotloadings(p,
                               components = getComponents(p, c(1:4)),
                               rangeRetain = 0.1,
                               labSize = 2.5,
                               absolute = FALSE,
                               title = 'PCA Loadings plot',
                               subtitle = 'Misc PCs - Top 10% variables',
                                shape = 23, 
                               shapeSizeRange = c(1, 10),
                               col = c('white', 'blue',"red"),
                               drawConnectors = TRUE)+
          ggplot2::theme(
            plot.title = element_text(size = 14, face = "bold", hjust = 0.5),
            plot.subtitle = element_text(size = 11, face = "italic", hjust = 0.5),
            
            axis.title = element_text(size = 11, face = "bold"),
            axis.text = element_text(size = 9),
            
            legend.title = element_text(size = 11, face = "bold"),
            legend.text = element_text(size = 9)
          )
        )
  invisible(dev.off())
  
  
  
  
  ###########################
  # Pair Plots: on 1st five Pc, Components wise plots
  ##########################
  
  
##################################333
  # Temporarily redirect output and warnings to a null connection
  #sink("/dev/null")
  cat("\nGenerating Pairs(DeSeq)_Plot.png")
  
  CairoPNG(file ="Pairs(DeSeq)_Plot.png",width = 10, height = 9.5, units = "in", res = 300) 
  suppressMessages({
    pairs_plot <- pairsplot(p, components = getComponents(p, c(1:5)), 
                            title = "PCA Pairs Plot of Components 1-5",
                           
                            )  
    print(pairs_plot)
  })
  invisible(dev.off())
  
  
  ########################################
  # Pair plots by components
  for (i in 2:ncol(df)) {
    Cairo::CairoPNG(file = paste0("Pairplot_",names(p$metadata)[i],".png"),
                    width = 9.5, height = 9, units = "in", res = 300)
       pairs_plot<-pairsplot(p,
            components = getComponents(p, c(1:5)),
            colby = names(p$metadata)[i],
            title = paste0("Pairplot of ",names(p$metadata)[i])#,margingaps = unit(c(-0.02, -0.02, -0.02, -0.02), 'cm')
            )
         print(pairs_plot)
       
    invisible(dev.off())
  }
  # ============================================================================
  # SCREE PLOTS: hornsVST
  # ============================================================================

    #Let’s perform Horn’s parallel analysis first:
  s<-as.matrix(assay(VST)) 
  
  max_components <- min(nrow(s), ncol(s))
  horn <- parallelPCA(s, max.rank = max_components)
  
  #Now the elbow method:
  elbow <- findElbowPoint(p$variance)
  cat("Generating Scree(DeSeq)_Plot.png \n")
  
  CairoPNG(file ="Scree(DeSeq)_Plot.png",width = 11, height = 7, units = "in", res = 300)
  print(screeplot(p,
                  components = getComponents(p, 1:length(p$components)),
                  vline = c(horn$n, elbow)) +
          annotate("text", x = horn$n + 1, y = 40,label = "Horn's", vjust = -2, size = 4.5) +
          annotate("text", x = elbow + 1, y = 30, label = "Elbow method", vjust = -1, size = 4.5)+
          
          ggplot2::theme(
            plot.title = element_text(size = 14, face = "bold", hjust = 0.5),
            
            axis.title.x = element_text(size = 11),   # x-axis label (smaller)
            axis.text.x  = element_text(size = 9),   # x-axis tick labels (smaller)
            
            axis.title.y = element_text(size = 11),
            axis.text.y  = element_text(size = 9)
          )
       
        )
  
  invisible(dev.off())
  #########################
  # EIGENCOR PLOT:  on all component, on horn selected components 
  #########################
  if(ncol(df)>2){
  # Prepare meta data for eigencor plots
  p <- PCAtools::pca(assay(VST), metadata = colData(VST), removeVar = 0.1)
  
  # Retrieve colData
  col_data <- colData(VST)
  
  # Identify factor columns programmatically
  factor_columns <- sapply(col_data, is.factor)
  
  # Convert factor columns to numeric
  if(ncol(df)==2){
    col_data[2] <- lapply(col_data[2], function(x) as.numeric(x))  # Convert to 0/1
    
  }else{
    col_data[, 2:ncol(df)] <- lapply(col_data[, 2:ncol(df)], function(x) as.numeric(x))  # Convert to 0/1
    
  }
  
  # Run PCA using PCAtools with updated metadata from colData
  p1 <- PCAtools::pca(assay(VST), metadata = col_data, removeVar = 0.1)
  
  ##############################################################
  cat("\nGenerating Eigencor1(DeSeq)_Plot.png")
  
  CairoPNG(file ="Eigencor1(DeSeq)_Plot.png",width = 9, height = 6, units = "in", res = 300)
  print(eigencorplot(p1,
               components = getComponents(p1, 1:length(p1$components)),
               metavars = c(names(df)[2:ncol(df)]),
               col = c('white', 'cornsilk1', 'gold', 'forestgreen', 'darkgreen'),
               main = "Eigenvalue Correlation Analysis: Principal Components vs. Metadata Variables",
               rotLabX = 45,  # Rotation of x-axis labels
               cexMain= 1
               
               ) )

  invisible(dev.off())
  cat("\nGenerating Eigencor2(DeSeq)_Plot.png")
  
 CairoPNG(file ="Eigencor2(DeSeq)_Plot.png",width = 9, height = 6, units = "in", res = 300)
  print(eigencorplot(p1,
               components = getComponents(p1, 1:horn$n),  # Specifies the components to include based on Horn's analysis
               metavars =  c(names(df)[2:ncol(df)]),  # Uses metadata variables from the data frame, excluding the first column
               col = c('white', 'cornsilk1', 'gold', 'forestgreen', 'darkgreen'),  # Color scheme for the plot
               rotLabX = 45,  # Rotation of x-axis labels
               cexMain= 1,
               main = paste0("Eigenvalue Correlation of Principal Components with Clinical Variables (Up to Horn's Suggested Components)", 
                             "\n", 
                             "Principal Component Pearson r^2 Clinical Correlates"),  # Title and subtitle
               plotRsquared = TRUE,  # Plot the R-squared values
               corMultipleTestCorrection = 'BH'  # Benjamini-Hochberg correction for multiple testing
               ))

  invisible(dev.off())
#Supress Warnings
  }#if
  
  
   print("\nDESeq Matrix and Plots generated")

 
 cat("\n========== DESEQ MATRIX ANALYSIS FINISED ==========\n")
 
 
