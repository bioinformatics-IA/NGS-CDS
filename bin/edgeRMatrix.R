
myargs = commandArgs(trailingOnly=TRUE)
# Expected Parameter Maps:
# myargs[1] -> Working Directory Path
# myargs[2] -> R custom package directory
# myargs[3] -> "SINGLE" or "MULTIPLE" factor mode
# myargs[4] -> Design intercept option ("NO" / "YES")

# PARAMETERS
# myargs[1]="/mnt/ntfs/Output_RNASeq1/"#
# myargs[2]="/home/iffy/NetBeansProjects/NGSGradle/app/R_Libraries/"
# myargs[3]="SINGLE"
# myargs[4]="NO"
##################################################################

setwd(myargs[1])#set current working directory
dir.create(file.path(myargs[1], "EdgeRResults"))
output_dir <- file.path(myargs[1], "EdgeRResults")
print("LOADING LIBRARIES..")
################################################################################

required_libs <- c("limma","edgeR", "RColorBrewer" ,"Cairo")

for (pkg in required_libs) {
  tryCatch({
    suppressPackageStartupMessages(library(pkg, lib.loc = myargs[2], character.only = TRUE))
    cat("Loaded", pkg, "\n")
  }, error = function(e) {
    cat("ERROR: Failed to load", pkg, "--", conditionMessage(e), "\n")
    quit(status = 1)
  })
}
################################################################################

sink.reset <- function(){
  for(i in seq_len(sink.number())){
    sink(NULL)
  }
}
###############################################################################
#Variables

readcounts <- readRDS("readcounts.RDS")
df<-readRDS("df.RDS")

factMode = myargs[3] # "MULTIPLE"
designMode=myargs[4] # NO(with 0+) / YES(without 0+)

initial_genes   <- nrow(readcounts)
initial_samples <- ncol(readcounts)

print("#=======================================================================#")
print("#                       EDGER ANALYSIS STARTED                         #")
print("#======================================================================#")

cat(sprintf("Dataset Input Baseline: %d Genes across %d Samples.\n\n", initial_genes, initial_samples))
#Groups SINGLEFACTOR or MULTIFACTOR
if(factMode=="SINGLE")
{
  sampleGroups <- factor(df[, 2])
  print("Sample Groups created:")
  print(sampleGroups)
  dgList <-DGEList(counts=readcounts[,1:ncol(readcounts)], genes = row.names(readcounts), group = sampleGroups)

} else if ((factMode =="MULTIPLE") && (ncol(df)>2) ){
  # Create sampleGroups by pasting columns 2 to the rest programmatically
  sampleGroups <- factor(apply(df[, 2:ncol(df)], 1, paste, collapse="."))
  print("Sample Groups created:")
  print(sampleGroups)
    dgList <-DGEList(counts=readcounts[,1:ncol(readcounts)], genes = row.names(readcounts), group=sampleGroups)
}else {
  stop("Critical Failure: Factor mode configuration conflicts with sample design metadata structure dimensions.")
}

print("DGEList object created. Displaying summary of read counts and sample grouping:")
print(dgList)
#Create design for multiple factors
#sampleGroups <- factor(apply(df[, 2:ncol(df)], 1, paste, collapse="."))
if(designMode == "NO"){
  print("Design mode is set to 'NO': Creating design matrix with no intercept (~0 + group).")
  design <- model.matrix(~0+sampleGroups)
}else if (designMode == "YES"){
  print("Design mode is set to 'YES': Creating design matrix with intercept (~group).")
    design <- model.matrix(~sampleGroups)
  }

# Rename the columns without the 'sampleGroups' prefix
colnames(design) <- levels(sampleGroups)
print("Final design matrix for model fitting:")
print(design)

#Setting column names of design
setwd(file.path(myargs[1], "EdgeRResults"))

#Counts per million of each sample
print("Calculating counts per million (CPM) for each gene across all samples...\n")
myCPM <- cpm(dgList)
if (nrow(myCPM) >= 10 ) {
  cat("Preview of CPM matrix (first 10 genes across all samples):\n")
  print(myCPM[1:10, ])
} else {
  cat(sprintf("CPM matrix is too small (%d rows x %d columns), showing full matrix:\n", nrow(myCPM), ncol(myCPM)))
  print(myCPM)
}

write.table(myCPM, file = "CPM_Matrix.tsv", sep = "\t", quote = FALSE, row.names = TRUE)
cat("\nFull CPM matrix saved to 'CPM_Matrix.tsv'")

print("Checking how many genes have CPM > 0.5 in each sample...")
thresh <- myCPM > 0.5 # Which values in myCPM are greater than 0.5?
# This produces a logical matrix with TRUEs and FALSEs

print("Summarizing the number of samples where each gene has CPM > 0.5:")
cat("This summary shows how many genes are expressed (CPM > 0.5) in a specific number of samples.\n")
cat("For example: '5  → 2844' means 2844 genes are expressed in exactly 5 samples.\n")

print(table(rowSums(thresh))) # Summary of how many TRUEs there are in each row true/false value// no. of rows

cat("Generating CPM_Gene_Expression_Distribution.png ...\n")
CairoPNG(file ="CPM_Gene_Expression_Distribution.png",width = 11, height = 7, units = "in", res = 300 )
par(mar = c(6, 6, 5, 2) + 0.1)
hist(rowSums(thresh), breaks = 40, main = "Distribution of Gene Expression",
     xlab = "Number of Samples with CPM > 0.5", ylab = "Number of Genes", col = "steelblue",
     cex.main = 1.5,   # Makes the main title 200% larger
     cex.lab = 1.2,    # Makes X and Y axis labels 160% larger
     cex.axis = 1.1
     )
invisible(dev.off())

print("Total gene counts per sample before filtering:")
print(apply(dgList$counts, 2, sum)) # total gene counts per sample

print("Dimensions (genes x samples) of dgList before filtering:")
print(dim(dgList)) #Before filtering
print("Filtering low-expressed genes using filterByExpr()...")
keep <- filterByExpr(dgList,design = design) #Filtering for multiple factors
dgList_filtered <- dgList[keep, , keep.lib.sizes = FALSE]
final_genes   <- nrow(dgList_filtered)
final_samples <- ncol(dgList_filtered)
dropped_genes <- initial_genes - final_genes
cat("\n======================================================\n")
cat("                DATASET FILTERING SUMMARY             \n")
cat("======================================================\n")
cat(sprintf(" Initial Matrix Dimensions           : %d Genes x %d Samples\n", initial_genes, initial_samples))
cat(sprintf(" Low-Expression Genes Removed        : %d\n", dropped_genes))
cat(sprintf(" Remaining Genes Retained            : %d\n", final_genes))
cat(sprintf(" Final Post-Filtering Dimensions     : %d Genes x %d Samples\n", final_genes, final_samples))
cat(sprintf(" Percentage of Total Dataset Kept    : %.2f%%\n", (final_genes / initial_genes) * 100))
cat("======================================================\n\n")

# #FILTERING
# print("Summary of genes filtering:")
# cat("Total genes before filtering:", nrow(dgList), "\n")
# cat("Number of genes retained after filtering:", sum(keep), "\n")
# cat("Number of genes removed:", sum(!keep), "\n")
# cat("\nBreakdown of filtering result (TRUE = kept, FALSE = removed):\n")
# print(table(keep))
# cat("This shows how many times each unique value (TRUE/FALSE) occurs:\n")
# print(table(table(keep)))


dgList <- dgList[keep, , keep.lib.sizes=FALSE]
print("Dimensions (genes x samples) of dgList after filtering:")
dim(dgList) #After filtering

#####################################
# LIBRARY SIZE: BARPLOT
####################################
# The names argument tells the barplot to use the sample names on the x-axis
# The las argument rotates the axis names
cat("\nGenerating LibrarySize_BarPlot.png...\n ")
CairoPNG(file ="LibrarySize_BarPlot.png",width = 11, height = 7, units = "in", res = 300)
par(mar = c(8, 6.5, 4, 2) + 0.1)
barplot(dgList$samples$lib.size,names=colnames(dgList),col=brewer.pal(8, "Dark2"),las=2,
        cex.axis = 1.0,               # Slightly shrinks Y-axis numbers for clean spacing
        cex.names = 0.8
        )
title(main="Barplot of library sizes",cex.main = 1.3)
invisible(dev.off())

#####################################
# COUNTS PLOTS: BARPLOT, DENSITYPLOT
####################################

# Get raw log2 counts per million before normalization
logcounts <- cpm(dgList,log=TRUE)

#NORMALIZATION
print("Performing TMM Normalization Adjustment Factors....")
dgList <- calcNormFactors(dgList)
print("Normalization factors and updated sample-level information: ")
print (dgList$samples)

# Get log2 counts per million
lognormalized_counts <- cpm(dgList,log=TRUE)


# ==============================================================================
# SECTION 1: SEPARATE UNNORMALIZED BOXPLOT
# ==============================================================================
cat("\nGenerating Counts_Boxplot_Unnormalized.png...\n")

CairoPNG(file = "Counts_Boxplot_Unnormalized.png", width = 11, height = 7, units = "in", res = 300)
# Set a very large bottom margin (15 lines) for 100 sample text tracks
par(mar = c(8, 6, 4, 2) + 0.1)

# Generate a continuous color palette long enough for 100 samples
colors_vector <- colorRampPalette(brewer.pal(8, "Dark2"))(ncol(logcounts))

boxplot(logcounts, 
        xlab = "", 
        ylab = "Log2 counts per million", 
        col = colors_vector, 
        las = 2, 
        outline = FALSE,          # Hides outliers so 100 samples stay clean
        cex.axis = 0.8,           # Shrinks sample names text size so they don't overlap
        main = "Boxplots of logCPMs (unnormalised)")

# Use the column medians array instead of a single global matrix median

abline(h = median(apply(logcounts, 2, median)), col = "blue", lwd = 2, lty = 2)
invisible(dev.off())


# ==============================================================================
# SECTION 2: SEPARATE NORMALIZED BOXPLOT
# ==============================================================================
cat("Generating Counts_Boxplot_Normalized.png...\n")

CairoPNG(file = "Counts_Boxplot_Normalized.png", width = 11, height = 7, units = "in", res = 300)
par(mar = c(8, 6, 4, 2) + 0.1)

boxplot(lognormalized_counts, 
        xlab = "", 
        ylab = "Log2 counts per million", 
        col = colors_vector, 
        las = 2, 
        outline = FALSE, 
        cex.axis = 0.8, 
        main = "Boxplots of logCPMs (normalised)")

abline(h = median(apply(lognormalized_counts, 2, median)), col = "blue", lwd = 2, lty = 2)
invisible(dev.off())


# ==============================================================================
# SECTION 3: COMBINED BOXPLOTS LAYOUT (Side-by-Side Comparison)
# ==============================================================================
cat("Generating Counts_Boxplots_Combined.png...\n")

CairoPNG(file = "Counts_Boxplots_Combined.png", width = 14, height = 7, units = "in", res = 300)
# Split canvas into 1 row, 2 columns side-by-side
par(mfrow = c(2, 1))
# Balanced margins across both internal panels
#par(mar = c(8, 6, 4, 2) + 0.1)
par(mar = c(4, 4, 2.5, 0.5), mgp = c(2.5, 0.7, 0))

y_min <- min(c(logcounts, lognormalized_counts))
y_max <- max(c(logcounts, lognormalized_counts))
y_buffer <- (y_max - y_min) * 0.05
common_ylim <- c(y_min - y_buffer, y_max + y_buffer)

# Left Panel: Unnormalized
boxplot(logcounts, 
        xlab = "", 
        ylab = "Log2 counts per million", 
        col = colors_vector, 
        las = 2, 
        outline = FALSE,
        ylim = common_ylim,
        cex.main = 1.1,
        cex.axis = 0.55, # Slightly smaller font since panels are narrower here
        main = "Boxplots of logCPMs (unnormalised)")
abline(h = median(apply(logcounts, 2, median)), col = "blue", lwd = 2, lty = 2)

# Right Panel: Normalized
boxplot(lognormalized_counts, 
        xlab = "", 
        ylab = "Log2 counts per million", 
        col = colors_vector, 
        las = 2, 
        ylim = common_ylim,
        outline = FALSE, 
        cex.axis = 0.55, 
        cex.main = 1.1,
        main = "Boxplots of logCPMs (normalised)")
abline(h = median(apply(lognormalized_counts, 2, median)), col = "blue", lwd = 2, lty = 2)
invisible(dev.off())

# Define a robust color palette globally so it stays consistent across all plots
palette_colors <- rainbow(initial_samples)


# ==============================================================================
# SECTION 1: SEPARATE UNNORMALIZED DENSITY PLOT
# ==============================================================================
cat("\nGenerating Counts_Densityplot_Unnormalized.png...\n")

CairoPNG(file = "Counts_Densityplot_Unnormalized.png", width = 11, height = 7, units = "in", res = 300)
par(mar = c(6, 6, 4, 2) + 0.1) # Balanced margins for clean axis display

# Calculate the maximum y-axis limit for unnormalized counts
unnorm_max <- max(vapply(1:ncol(logcounts), function(i) max(density(logcounts[, i])$y), FUN.VALUE = numeric(1)))

# Plot the baseline first sample channel
plot(density(logcounts[, 1]), 
     main = "Unnormalized Gene Expression Distribution", 
     xlab = "Log2 Counts Per Million (Log2CPM)", 
     ylab = "Density",
     col = palette_colors[1], 
     lwd = 2, 
     ylim = c(0, unnorm_max))

# Overlay the remaining sample paths
for(i in 2:ncol(logcounts)) {
  lines(density(logcounts[, i]), col = palette_colors[i], lwd = 2)
}

invisible(dev.off())


# ==============================================================================
# SECTION 2: SEPARATE NORMALIZED DENSITY PLOT
# ==============================================================================
cat("Generating Counts_Densityplot_Normalized.png...\n")

CairoPNG(file = "Counts_Densityplot_Normalized.png", width = 14, height = 7, units = "in", res = 300)
par(mar = c(6, 6, 4, 2) + 0.1)

# Calculate the maximum y-axis limit for normalized counts
norm_max <- max(vapply(1:ncol(lognormalized_counts), function(i) max(density(lognormalized_counts[, i])$y), FUN.VALUE = numeric(1)))

# Plot the baseline first sample channel
plot(density(lognormalized_counts[, 1]), 
     main = "Normalized Gene Expression Distribution", 
     xlab = "Log2 Counts Per Million (Log2CPM)", 
     ylab = "Density",
     col = palette_colors[1], 
     lwd = 2, 
     ylim = c(0, norm_max))

# Overlay the remaining sample paths
for(i in 2:ncol(lognormalized_counts)) {
  lines(density(lognormalized_counts[, i]), col = palette_colors[i], lwd = 2)
}

invisible(dev.off())


# ==============================================================================
# SECTION 3: COMBINED DENSITY PLOTS LAYOUT (Side-by-Side Comparison)
# ==============================================================================
cat("Generating Counts_Densityplots_Combined.png...\n")

CairoPNG(file = "Counts_Densityplots_Combined.png", width = 14, height = 7, units = "in", res = 300)

# Set up the plotting area with 1 row and 2 columns side-by-side
par(mfrow = c(1, 2))
par(mar = c(6, 6, 4, 2) + 0.1)

# --- Left Panel: Unnormalized ---
plot(density(logcounts[, 1]), 
     main = "Unnormalized Counts Distribution", 
     xlab = "Log2 Counts Per Million (Log2CPM)", 
     ylab = "Density",
     col = palette_colors[1], 
     lwd = 2, 
     ylim = c(0, unnorm_max))

for(i in 2:ncol(logcounts)) {
  lines(density(logcounts[, i]), col = palette_colors[i], lwd = 2)
}

# --- Right Panel: Normalized ---
plot(density(lognormalized_counts[, 1]), 
     main = "Normalized Counts Distribution", 
     xlab = "Log2 Counts Per Million (Log2CPM)", 
     ylab = "Density",
     col = palette_colors[1], 
     lwd = 2, 
     ylim = c(0, norm_max))

for(i in 2:ncol(lognormalized_counts)) {
  lines(density(lognormalized_counts[, i]), col = palette_colors[i], lwd = 2)
}

invisible(dev.off())
#=====================================================
#Designing Model GLM Approach

#design <- model.matrix(~0+sampleGroups,data = dgList$samples)
#colnames(design) <- levels(dgList$samples$group)

#print("DESIGN:")
#print(design)

#Estimating the predicted log fold changes (logFC) for each gene based on the provided design matrix
print("Estimating preliminary log fold-changes (logFC) using predFC...")
logFC <- predFC(dgList,design,prior.count=1,dispersion=0.05)

if (nrow(logFC) >= 10 ) {
  cat("Log fold-change matrix (first 10 genes across all comparisons):\n")
  print(logFC[1:10, ])
} else {
  cat(sprintf("logFC matrix is too small (%d rows x %d columns), showing full matrix:\n", nrow(logFC), ncol(logFC)))
  print(logFC)
}

write.table(logFC, file = "Preliminary_LogFC.tsv", sep = "\t", quote = FALSE, row.names = TRUE)
cat("\nFull logFC matrix saved to 'Preliminary_LogFC.tsv'\n")

print("Correlation matrix of the log fold changes between the different conditions or groups:")
print(cor(logFC[,1:ncol(logFC)]))

  
#ESTIMATING DISPERSION
cat("\nEstimating Dispersion ...\n")
y <- estimateDisp(dgList, design, robust=TRUE)

print(sprintf("Common dispersion: %f", y$common.dispersion))
cat(sprintf("Tagwise dispersion (showing first %d):\n", min(10, length(y$tagwise.dispersion))))
print(head(y$tagwise.dispersion, 10))
write.table(y$tagwise.dispersion, file = "Tagwise_Dispersion.tsv", sep = "\t", quote = FALSE, col.names = "tagwise_dispersion")
cat("Full tagwise dispersion saved to 'Tagwise_Dispersion.tsv'\n")

cat(sprintf("Trended dispersion (showing first %d):\n", min(10, length(y$trended.dispersion))))
print(head(y$trended.dispersion, 10))
write.table(y$trended.dispersion, file = "Trended_Dispersion.tsv", sep = "\t", quote = FALSE, col.names = "trended_dispersion")
cat("Full Trended dispersion saved to 'Trended_Dispersion.tsv'\n")

cat(sprintf("Average Log CPM (showing first %d):\n", min(10, length(y$AveLogCPM))))
print(head(y$AveLogCPM, 10))
write.table(y$AveLogCPM, file = "Average_LogCPM.tsv", sep = "\t", quote = FALSE, col.names = "AveLogCPM")
cat("Full average log CPM saved to 'Average_LogCPM.tsv'\n")

#####################################
# MA PLOTS:  either by exacttest or glm
#  log ratios (A) against the mean expression levels (M) 
####################################
#For simple
# Perform exact test for differential expression

# et <- exactTest(y)
# 
# # Get the top tags (results) including logFC and average expression
# topTags <- topTags(et, n=Inf)
# results <- topTags$table

# cat("\nGenerating MAPlot(edgeR-exactTest).png...")
# CairoPNG(file ="MAPlot(edgeR-exactTest).png",width=1000, height=800)
# # Plot the MA plot using base R plot function
# plot(results$logCPM, results$logFC,
#      xlab = "Average Log CPM (A)", ylab = "Log Fold Change (M)",
#      main = "MA Plot of Differential Gene Expression: Log Fold Change vs Mean Expression", pch = 16, col = ifelse(results$FDR < 0.05, "red", "black"))
# 
# # Optionally, add a horizontal line at logFC = 0
# abline(h=0, col="blue")
# invisible(dev.off())
# 
# 
# # Fit the model and perform likelihood ratio test (LRT)
# fit <- glmFit(y, design)
# lrt <- glmLRT(fit)
# 
# # Get the top tags (results) including logFC and average expression (logCPM)
# topTags <- topTags(lrt, n=Inf)
# results <- topTags$table
# 
# # Extract log-fold changes (M) and average expression (A)
# #logFCPlot <- lrt$table$logFC  # M-values (log-fold change)
# #AveExpr <- rowMeans(cpm(dgList, log=TRUE))  # A-values (average log expression)
# cat("\nGenerating MAPlot(edgeR).png...")
# 
# CairoPNG(file ="MAPlot(edgeR).png",width=1000, height=800) 
# 
# # Base R MA plot
# plot(results$logCPM, results$logFC, xlab = "Average Log CPM (A)", ylab = "Log Fold Change (M)", pch = 16, col = ifelse(results$FDR < 0.05, "blue", "black"))
# abline(h = 0, col = "red")  # Add horizontal line at 0 for reference
# title("MA Plot of Differential Gene Expression: Log Fold Change vs Mean Expression")
# invisible(dev.off())
# 


cat("\nGenerating MDSPlot.png...")
#Data Exploration
CairoPNG(file ="MDSPlot.png",width = 11, height = 7, units = "in", res = 300)
par(mar = c(5, 5.5, 4, 2), mgp = c(2.8, 0.8, 0), cex = 1.0)
plotMDS(y,gene.selection="common",main="MDS PLOT of edgeR Object(dgList)",
        cex = 0.65,col=brewer.pal(8, "Dark2")) #An MDS plot shows the relative similarities of the ______samples.
grid(lty = "dotted", col = "gray85")
invisible(dev.off())
cat("\nGenerating Adaptive_Pipeline_MDSPlot.png....")
# Keep a wide right margin (12) to dynamically accommodate 1 or 2 legends smoothly
CairoPNG(file = "Enhanced_MDSPlot.png", width = 11, height = 7, units = "in", res = 300)
par(mar = c(5, 5, 4, 12), mgp = c(2.8, 0.8, 0), cex = 1.0)

# ==============================================================================
# 1. DYNAMIC METADATA SCANNING & FILTERING
# ==============================================================================
all_cols <- colnames(y$samples)
ignored_cols <- c("lib.size", "norm.factors", "group")
user_cols <- all_cols[!all_cols %in% ignored_cols]

# Keep only categorical grouping columns (ignore unique sample IDs/numbers)
metadata_factors <- c()
for (col in user_cols) {
  y$samples[[col]] <- as.factor(y$samples[[col]])
  if (length(levels(y$samples[[col]])) < nrow(y$samples)) {
    metadata_factors <- c(metadata_factors, col)
  }
}

# ==============================================================================
# 2. ADAPTIVE FACTOR ASSIGNMENT LOGIC
# ==============================================================================
# Base Defaults
sample_colors <- "black"
sample_shapes <- 16
legend_1_title <- "Group"
legend_1_levels <- levels(as.factor(y$samples$group))
has_second_legend <- FALSE

if (length(metadata_factors) == 0) {
  # --- FALLBACK: Strict Single-Factor Mode (Uses 'group' column) ---
  f1_vector       <- as.factor(y$samples$group)
  legend_1_levels <- levels(f1_vector)
  legend_1_colors <- brewer.pal(max(3, length(legend_1_levels)), "Set1")[1:length(legend_1_levels)]
  sample_colors   <- legend_1_colors[f1_vector]
  
} else {
  # --- MULTI-FACTOR MODE: Handle 1 or more custom metadata columns ---
  
  # Factor 1 (Controls Color)
  factor_1_name   <- metadata_factors[1]
  legend_1_title  <- factor_1_name
  f1_vector       <- y$samples[[factor_1_name]]
  legend_1_levels <- levels(f1_vector)
  legend_1_colors <- brewer.pal(max(3, length(legend_1_levels)), "Set1")[1:length(legend_1_levels)]
  sample_colors   <- legend_1_colors[f1_vector]
  
  # Factor 2 (Controls Shape - Activated only if a 2nd metadata column exists)
  if (length(metadata_factors) >= 2) {
    has_second_legend <- TRUE
    factor_2_name   <- metadata_factors[2]
    f2_vector       <- y$samples[[factor_2_name]]
    legend_2_levels <- levels(f2_vector)
    
    # Circle, Triangle, Square, Diamond, Plus-Cross
    available_shapes <- c(16, 17, 15, 18, 19, 3) 
    legend_2_shapes  <- available_shapes[1:length(legend_2_levels)]
    sample_shapes    <- legend_2_shapes[f2_vector]
  }
}

# ==============================================================================
# 3. GRAPH PLOTTING & LEGEND GENERATION
# ==============================================================================
plotMDS(y, 
        gene.selection = "common",
        col = sample_colors,         # Dynamically updated color mapping vector
        pch = sample_shapes,         # Dynamically updated shape vector (defaults to 16)
        cex = 1.8,                   
        main = "MDS Plot of Samples",
        xlab = "Leading LogFC Dim 1",
        ylab = "Leading LogFC Dim 2",
        cex.main = 1.3, cex.lab = 1.1, cex.axis = 0.9
)

grid(lty = "dotted", col = "gray80", lwd = 1)

# Primary Legend (Color) - Dynamic for both single-factor and multi-factor
legend("topright", 
       legend = legend_1_levels, 
       col = legend_1_colors, 
       pch = 16, 
       pt.cex = 1.5, cex = 0.9, bty = "n", 
       title = legend_1_title)

# Secondary Legend (Shape) - Automatically loads only if 2+ factors exist
if (has_second_legend) {
  legend("bottomright", 
         legend = legend_2_levels, 
         col = "gray40", 
         pch = legend_2_shapes, 
         pt.cex = 1.5, cex = 0.9, bty = "n", 
         title = factor_2_name)
}

invisible(dev.off())

cat("\nGenerating Dispersion(edgeR)_Plot.png...\n")
CairoPNG(file ="Dispersion(edgeR)_Plot.png",width = 11, height = 7, units = "in", res = 300)
par(mar = c(6, 6, 5, 2) + 0.1)
plotBCV(y,cex.lab = 1.6,cex.axis = 1.3) #Plotting Dispersion

title(main="BCV Plot for edgeR Data", cex.main = 2.0)
invisible(dev.off())


print("DesignNames:")
print(colnames(design))

setwd(file.path(myargs[1]))
save(y,design, file = "edgeRDisp.RData") #Save objects
print("********************************************************************************")

