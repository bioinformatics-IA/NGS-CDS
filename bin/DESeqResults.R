#===============================================================================
#                         COMMAND LINE ARGUMENTS
#===============================================================================

myargs = commandArgs(trailingOnly=TRUE)
# ######################################
# myargs[1]="/home/iffy/PhD_Data/CASESTUDY/1-Hepatocellular_carcinoma_100/RNASeq_PRJNA867011/TestingDESeq2/"
# myargs[2]="/home/iffy/NetBeansProjects/NGSGradle/app/R_Libraries/"
# myargs[3]="0.1"
# myargs[4]="0.05"
# myargs[5]="2" # For HCC 1st ran with 2 and found 390 gene
# myargs[6]="LIST" # TYPE/LIST
# myargs[7]="GENDER_male_vs_female"
# myargs[8]=""
# 
######################################
dir.create(file.path(myargs[1], "DeSeqResults"))
setwd(file.path(myargs[1]))

# Set lib path first to isolate the environment

detach_if_loaded <- function(pkg) {
  if (paste0("package:", pkg) %in% search()) {
    try(detach(paste0("package:", pkg), unload = TRUE, character.only = TRUE), silent = TRUE)
  }
}

annotate_genes_dynamic <- function(res_df, db = org.Hs.eg.db) {
  
  raw_ids <- rownames(res_df)
  clean_ids <- gsub("\\..*$", "", raw_ids) # Remove version extensions if present
  
  # 1. Detect ID Type using pattern matching
  # - Ensembl starts with ENS (e.g. ENSG..., ENST...)
  # - Entrez IDs are purely numeric digits
  # - Otherwise assume standard Gene Symbols
  
  if (all(grepl("^ENS", clean_ids[!is.na(clean_ids)]))) {
    input_keytype <- "ENSEMBL"
  } else if (all(grepl("^[0-9]+$", clean_ids[!is.na(clean_ids)]))) {
    input_keytype <- "ENTREZID"
  } else {
    input_keytype <- "SYMBOL"
  }
  
  cat(sprintf("[ANNOTATION] Detected gene keytype: %s\n", input_keytype))
  
  # 2. Target columns to fetch
  target_cols <- c("ENSEMBL", "SYMBOL", "ENTREZID")
  
  # 3. Map missing columns dynamically
  for (col in target_cols) {
    col_name <- tolower(col) # Column names in dataframe: ensembl, symbol, entrezid
    
    if (col == input_keytype) {
      # Keep cleaned original input as its corresponding column
      res_df[[col_name]] <- clean_ids
    } else {
      # Map missing column from database
      res_df[[col_name]] <- tryCatch({
        mapIds(
          db,
          keys = clean_ids,
          column = col,
          keytype = input_keytype,
          multiVals = "first"
        )
      }, error = function(e) {
        warning(sprintf("Could not map %s to %s: %s", input_keytype, col, e$message))
        return(NA)
      })
    }
  }
  
  # 4. Reorder dataframe: Place gene annotations upfront
  annot_cols <- c("symbol", "ensembl", "entrezid")
  other_cols <- setdiff(names(res_df), annot_cols)
  res_df <- res_df[, c(annot_cols, other_cols)]
  
  return(res_df)
}


cat("\nLoading Libraries...\n")

#INCLUDING ALL REQUIRED LIBRARIES
################################################################################

library(methods)
cat("methods in search():", "package:methods" %in% search(), "\n")
required_libs <- c("BiocGenerics","S4Vectors","IRanges","GenomicRanges","SummarizedExperiment", 
                   "DESeq2","gplots","ggplot2", "scales", "pheatmap", "AnnotationDbi",
                   "RColorBrewer", "Cairo", "gridExtra", "dplyr", "tidyr")

for (pkg in required_libs) {
  tryCatch({
    suppressPackageStartupMessages(library(pkg, lib.loc = myargs[2], character.only = TRUE))
    cat("Loaded", pkg, "\n")
  }, error = function(e) {
    cat("ERROR: Failed to load", pkg, "--", conditionMessage(e), "\n")
    quit(status = 1)
  })
}

organism<-"org.Hs.eg.db" #"org.Mm.eg.db"#
#Checking whether organism is already installed or not
installed_packages <- organism %in% rownames(installed.packages(lib = myargs[2]))

if(installed_packages == TRUE){
  cat(sprintf("[INFO] Organism annotation package '%s' already available in:\n%s\n",
              organism, myargs[2]))
}else{
  cat(sprintf("[INSTALL] Installing missing organism package '%s' into:\n%s\n",
              organism, myargs[2]))
  BiocManager::install(organism,lib=myargs[2])
}
suppressPackageStartupMessages(library(organism,lib.loc = myargs[2],character.only = TRUE))



################################################################################

## Results VARIABLES

resAlpha =as.numeric(myargs[3])
padj = as.numeric(myargs[4])
log2fold =as.numeric(myargs[5])

#RESULTS OPTIONS
#FACTORS
selectedFactor=myargs[6] #Factor Column Name
value1 = myargs[7] #
value2 = myargs[8]
#LIST

readcounts <- readRDS("readcounts.RDS")
dds <- readRDS("dds.RDS")

####      SETTING CURRENT WORKING DIRECTORY

###     FUNCTIONS

sink.reset <- function(){
  for(i in seq_len(sink.number())){
    sink(NULL)
  }
}

#===============================================================================
#                   DESeq RESULTS
#===============================================================================


setwd(file.path(myargs[1], "DeSeqResults"))
.libPaths()
cat("\n========== DESEQ RESULT ANALYSIS STARTED ==========\n")

tryCatch({
  res <- resultsNames(dds)
  cat("Available coefficients / contrasts in DESeq2 object:\n")
  print(res)
}, error = function(e) {
  message("Error getting resultsNames(dds): ", e$message)
})

#STEP 4: Saving RESULTS

if(selectedFactor=="LIST")
{
  
  print("ANALYSING RESULTS BY LIST.....")
  
  #Removing any square brackets from value1
  value1 = gsub("\\[|\\]", "", value1)
  #Split value1 on comma and save as a list
  value1=trimws(strsplit(value1, ",")[[1]])
  
  #cat("\nSelected values by the user: ",value1)
  cat("\nSelected values by the user: ", paste(value1, collapse=", "), "\n")
  
  # Initialize a data frame to store all results combined
  combined_res_df <- data.frame()
  for(x in value1) {
    v <- make.names(x)
    
    if(v %in% resultsNames(dds)) {
  
          res <- results(dds, alpha=resAlpha, name=v)
      
      res <- res[order(res$padj), ]  # order by adjusted p-value
      
      # Convert to data frame and add contrast name
      temp_df <- as.data.frame(res)
      temp_df$contrast <- v
      
      combined_res_df <- rbind(combined_res_df, temp_df)
      
      cat("Processed contrast: ", v, "\n")
      
    } else {
      warning("Contrast ", v, " not found in DESeq2 object. Skipping.")
    }
  }
  # Use the combined data frame as 'res_df'
  res_df <- combined_res_df
  
}else{
  print("ANALYSING RESULTS BY CONTRAST.....")
  res <- results(dds,alpha =resAlpha,contrast = c(selectedFactor,value1,value2)) 
  res <- res[order(res$padj), ]
  res_df <- as.data.frame(res)
  
}
# STEP 3: Print summary
cat("\nDESeq Results ordred by padj\n")
print(head(res_df))
cat("\nDESeq Result Summary:\n")
if(selectedFactor == "LIST") {
  print("Summary is per contrast available in 'contrast' column of res_df")
} else {
  print(summary(res))
}

#print("DESeq Results ordred by padj")
#res <-res[order(res$padj),]
#print(res)
#print("DESeq Result Summary:")
#print(summary(res)) #Summary of results

#results of analysis c(selectedFactor,value1,value2)

#===========================================================================

##NORMALIZED COUNTS
normalized_counts <- as.data.frame( counts(dds, normalized=TRUE))

#Creating Results data frame
#res_df <- as.data.frame(res) 

# Suggest minimum values found in res_df
min_padj <- min(res_df$padj, na.rm = TRUE)
min_log2fold <- min(abs(res_df$log2FoldChange), na.rm = TRUE)

max_padj <- max(res_df$padj, na.rm = TRUE)
max_log2fold <- max(abs(res_df$log2FoldChange), na.rm = TRUE)
print("Suggested thresholds based on data:")
print(sprintf("padj limit = %f to %f", min_padj, max_padj))
print(sprintf("log2FoldChange limit = %f to %f", min_log2fold,max_log2fold))

# Set a boolean column for significance based on padj and log2FoldChange
res_df$significant <- ifelse(res_df$padj < padj & abs(res_df$log2FoldChange) > log2fold, "Significant", NA) #Adding Significant col to res_df
print(sprintf("Adding an extra column 'Significant' in result data frame where padj<%f and log2FoldChange>%f",padj,log2fold))
# Check if there are any significant genes
if (sum(res_df$significant == "Significant", na.rm = TRUE) == 0) {
  
  print(sprintf("ERROR: No significant genes found based on the given padj and log2FoldChange thresholds. Check padj or log2FoldChange range again."))
  
} else {
  print(sprintf("Significant genes found (%d):",nrow(res_df[!is.na(res_df$significant), ])))
  print(res_df[!is.na(res_df$significant), ])
}


print(head(res_df))

######################  
##DE GENES and ANALYSIS
#####################
# Adding Gene column to res_df
res_df$genes <- rownames(res_df) 

deGenes <- res_df[which(res_df$significant == "Significant"),] #Significant genes complete data
deGenes<-deGenes[order(deGenes$padj),] #Sorting in decreasing order
cat("Total number of DE Genes (",nrow(deGenes),") :")
print(deGenes)
setwd(file.path(myargs[1]))
saveRDS(deGenes, file = "degenes.RDS") #Save single object
# 1. Strip decimal version numbers from Ensembl IDs if they exist in rownames
gsea_genes <- gsub("\\..*$", "", rownames(res_df)) 
# 2. Extract log2FoldChange values and assign clean gene names
gsea_list <- res_df$log2FoldChange
names(gsea_list) <- gsea_genes
# 3. Clean missing values and sort descending (from highly upregulated to downregulated)
gsea_list <- gsea_list[!is.na(gsea_list) & !is.na(names(gsea_list))]
gsea_list <- sort(gsea_list, decreasing = TRUE)

saveRDS(gsea_list, file = "gsea_geneList.RDS")
cat(sprintf("[GSEA PREP] Successfully saved complete ranked profile of %d genes to 'gsea_geneList.RDS'\n", length(gsea_list)))

print("Saving results to file DESeq2_Results.txt")
setwd(file.path(myargs[1], "DeSeqResults"))
#RESULTS TO FILE
sink(file ="DESeq2_Results.txt",append = TRUE,type = "output")
print("*****************************************")
print("DESeq Results ordred by padj")
print(res)
print("DESeq Result Summary:")
print(summary(res)) #Summary of results
print(sprintf("Adding an extra column 'Significant' in result data frame where padj<%f and log2FoldChange>%f",padj,log2fold))
print(res_df)
print(sprintf("Total number of DE Genes Identified: %d",nrow(deGenes)))
print("DE Genes:")
print(deGenes)

print("*****************************************")
sink.reset()

# Dynamically annotate Gene Symbols, Ensembl IDs, and Entrez IDs
res_df <- annotate_genes_dynamic(res_df, db = org.Hs.eg.db)

# Save results to clean CSV
write.csv(res_df, "DESeq_results.csv", row.names = FALSE)
cat("Results successfully saved with dynamic gene annotations to DESeq_results.csv\n")

#write.table(res_df,"DESeq_results.csv", row.names = FALSE,col.names = TRUE,sep = "\t")



########################
# Hierarchical clustering Plot: dist() or hclust()
########################


deGenes_normCount = normalized_counts[rownames(deGenes),]

deGenes2 <- as.matrix(deGenes_normCount)
#Scaling for better visualization (For clustering and heatmaps)
scaledata <- t(scale(t(deGenes2)))

#hierarchical tree
hc <- hclust(as.dist(1-cor(scaledata, method="spearman")), method="complete") # Clusters columns by Spearman correlation
#The distance data is used to plot the dendrogram.
sampleTree = as.dendrogram(hc, method="average")

cat("\nGenerating SampleClustering(DESeq).png....")
CairoPNG(file ="SampleClustering(DESeq).png", width = 15, height = 7.5, units = "in", res = 300)

# Adjust margins and plot tree
par(mar = c(7.5, 4.5, 5.5, 1),   # bottom, left, top, right
    mgp = c(3, 0.7, 0),     # axis title spacing
    cex = 1.0)

plot(sampleTree, 
     main = "",
     ylab = "Height", xlab = "",leaflab = "none",  cex.main = 1.2, cex.lab = 1.0, 
     cex.axis = 0.80,cex = 0.45,  font.lab = 2,font.axis = 1,las = 2  )

sample_labels <- labels(sampleTree)
text(x = 1:length(sample_labels), 
     y = par("usr")[3] - (max(hc$height) * 0.02), # Positions text right below the baseline
     labels = sample_labels, 
     srt = 90,                   # Rotates text 90 degrees vertically (replaces las=2)
     adj = 1,                    # Right-justifies text so it aligns at the branch tip
     xpd = TRUE,                 # Allows drawing outside the core plot box area
     cex = 0.65,                 # <-- CHOOSE YOUR EXACT SIZE HERE (Super Small & Crisp)
     font = 1)

mtext("Hierarchical Clustering of Samples\nBased on DESeq2 Normalized Counts",
      side = 3, line = 3.2, cex = 1.3, font = 2, adj = 0.5)
mtext("Spearman Correlation Distance | Complete Linkage\nThreshold for clustering indicated by dashed line",
  side = 3,  line = 1.0,cex = 0.85,font = 3, adj = 0.5)

max_height <- max(hc$height)  # Get the maximum height of the dendrogram
threshold <- 0.7 * max_height  # Set the threshold at 70% of the max height
# Add horizontal line for clustering threshold

abline(h = threshold, col = "red", lty = 2)
# Annotate the line with "70% Threshold"
text(x = length(hc$order) * 0.9, y = threshold, 
     labels = "70% Threshold", col = "red", pos = 3, cex = 0.75, font = 2)

invisible(dev.off())

########################
# Hierarchical clustering Plot: 
# Hierarchical tree of the genes
########################

topGenes <- head(order(rowVars(scaledata), decreasing = TRUE), 100)
scaledata_sub <- scaledata[topGenes, ]
# Cluster rows by Pearson correlation.
hr <- hclust(as.dist(1-cor(t(scaledata_sub), method="pearson")), method="complete") 
geneTree = as.dendrogram(hr) #, method="average"
cat("\nGenerating GeneClustering(DESeq).png....")

CairoPNG(file ="GeneClustering(DESeq).png",width = 15, height = 7.5, units = "in", res = 300)
# Adjust margins (bottom, left, top, right)

par(mar = c(7.5, 4.5, 5.5, 1),   # bottom, left, top, right
    mgp = c(3, 0.7, 0),     # axis title spacing
    cex = 1.0)
plot(geneTree,
     main = "",  xlab = "", ylab = "Height", leaflab = "none", cex = 0.45,   #x labels
     cex.lab = 1.0, cex.axis = 0.80,
     font.lab = 2,
     font.axis = 1,
     las = 2   )



gene_labels <- labels(geneTree)
text(x = 1:length(gene_labels), 
     y = par("usr")[3] - (max(hr$height) * 0.02), # Positions text right below the baseline
     labels = gene_labels, 
     srt = 90,                   # Rotates text 90 degrees vertically (replaces las=2)
     adj = 1,                    # Right-justifies text so it aligns at the branch tip
     xpd = TRUE,                 # Allows drawing outside the core plot box area
     cex = 0.65,                 # <-- CHOOSE YOUR EXACT SIZE HERE (Super Small & Crisp)
     font = 1)

mtext("Hierarchical Clustering of Differentially Expressed Genes\n(Top 100 Genes)", 
      side = 3, line = 3.2, cex = 1.3, font = 2, adj = 0.5)
mtext("Gene clusters based on Pearson correlation (complete linkage)", 
      side = 3, line = 1.0, cex = 0.85, font = 3, adj = 0.5)

invisible(dev.off())
###############################
# Hierarichal clustering: Heatmap
###############################

cat("\nGenerating ClusteringHeatmap(DESeq).png....")

CairoPNG(file ="ClusteringHeatmap(DESeq).png",width = 12, height = 9.5, units = "in", res = 300)
# Define a new color palette for the heatmap
heat_colors <- colorRampPalette(c("navy", "firebrick3","darkslategray","green"))(100)

par(
  oma = c(1, 0, 4, 0)   # extra OUTER margin for title

)
heatmap.2(deGenes2,
          Rowv = as.dendrogram(hr),       # Cluster rows
          Colv = as.dendrogram(hc),       # Cluster columns
          col=heat_colors,#redgreen(100),
          scale = "row",                  # Scale by row
          margins = c(8, 8),            # Adjust margins for better label visibility
          cexCol = 0.65,                   # Increase column label size
          cexRow = 0.50,                   # Set smaller font for row labels
          #        labRow = TRUE,                     # Hide=F row labels to avoid clutter
          xlab = "Samples",               # X-axis label
          ylab = "Differentially Expressed Genes",  # Y-axis label
          cex.lab = 2.0,         # axis labels (xlab/ylab)
          trace = "none",                 # Remove unnecessary traces
          density.info = "density",       # Enable density plot inside heatmap
          denscol = "white",              # Set the color of the density trace to black
          key = TRUE,                     # Add a color key for interpretation
          key.title = "Expression",       # Title for the key
          key.xlab = "Scaled Expression Levels",  # X-label for key
          dendrogram = "both"
)
mtext("Hierarchical Clustering Heatmap of DESeq2 Genes", 
      outer = TRUE, side = 3, cex = 1.3, font = 2, line = 1)

invisible(dev.off())

######################################
#PLOT HISTOGRAM RESULTS PVALUE, BASEMEAN, LOG2FOLDCHANGE
#####################################
cat("\nGenerating Histograms(DESeq_res).png....")
CairoPNG(file ="Histograms(DESeq_res).png",width = 12, height = 10, units = "in", res = 300)
# Set the layout to display 3 histograms in one plot
par(mfrow=c(3,1), mar = c(4.2, 4.2, 2.5, 1.5),
    cex.main = 2.0,   # title size
    font.main = 2,    # bold title
    cex.lab = 1.5,    # x/y axis label size
    font.lab = 2,     # bold axis labels
    cex.axis = 1.2   # tick label size
    )
hist(res$pvalue,breaks=20,col=brewer.pal(11, "PRGn"),main = "Distribution of P-values", 
     xlab = "P-value",  ylab = "Frequency")
hist(res$baseMean,breaks=20,col=brewer.pal(11, "PRGn"),main = "Distribution of BaseMean (Gene Expression)", 
     xlab = "BaseMean",  ylab = "Frequency")
hist(res$log2FoldChange,breaks=20,col=brewer.pal(11, "PRGn"),main = "Distribution of Log2 Fold Change", 
     xlab = "Log2 Fold Change",  ylab = "Frequency")
# Reset the layout to default
par(mfrow=c(1,1))
invisible(dev.off())

######################################
#VOLCANO PLOT:  PVALUE VS LOG2FOLDCHANGE (DEGenes), MA PLOT
#####################################
cat("\nGenerating VolcanoPlots(res).png....\n")
  CairoPNG(file ="VolcanoPlots(res).png",width = 12, height = 6, units = "in", res = 300)

volcanoPlot1 <-ggplot(res_df, aes(log2FoldChange,-log10(pvalue),  colour=significant))  +
  geom_point(size = 1, alpha = 0.6) +  # Control point size and transparency for clarity
  scale_x_continuous(oob = squish) +   # Prevents data points from being squished outside limits
  scale_colour_manual(name = "Significance", values = c("Significant" = "red"), na.value = "grey50") + 
  # Set axis labels
  # Add a significance threshold line
  geom_hline(yintercept = -log10(0.05), colour = "blue", linetype = "dashed", linewidth = 0.5) +  
  
  labs(x = "Log2 Fold Change", y = "-Log10 P-value") + 
  # Add plot title
  ggtitle("Volcano Plot: Log2 Fold Change vs P-value\n(DESeq2 Analysis)") + 
  # Adjust theme for cleaner look
  theme_bw() +
  theme(plot.title = element_text(hjust = 0.5, size = 11, face = "bold"),
        axis.title = element_text(size = 10),
        axis.text = element_text(size = 9),
        legend.position = "top", 
        legend.title = element_text(face = "bold"))

volcanoPlot2 <- ggplot(res_df, aes(baseMean, log2FoldChange, colour=significant)) +
  geom_point(size = 1, alpha = 0.6) +  # Control point size and transparency
  scale_y_continuous(oob = squish) +  # Prevent points from squishing out of range
  scale_x_log10() +  # Log scale for x-axis (baseMean)
  
  # Add a horizontal line at y=0 (log fold change 0) to indicate no change
  geom_hline(yintercept = 0, colour = "tomato1",linetype = "dashed", linewidth = 0.5) +
  # Set axis labels
  labs(x = "Mean of Normalized Counts (log scale)", y = "Log2 Fold Change") +
  
  # Define color scale for significant points
  scale_colour_manual(name = "Significance",values = c("Significant" = "red"), na.value = "grey50") +
  
  # Add a descriptive plot title
  ggtitle("MA Plot: Mean Expression vs Log2 Fold Change\n(DESeq2 Analysis)") +
  
  # Improve the overall theme for clarity
  theme_bw() +
  theme(plot.title = element_text(hjust = 0.5, size = 11, face = "bold"),
        axis.title = element_text(size = 10),
        axis.text = element_text(size = 9),
        legend.position = "top", 
        legend.title = element_text(face = "bold"))
# Arrange both plots side by side
grid.arrange(volcanoPlot1, volcanoPlot2, ncol = 2)

invisible(dev.off())

######################################
# SIGNIFICANT GENES PLOT:
#####################################


#Heatmap of Euclidean sample distances after rlog transformation.
colours = colorRampPalette( rev(brewer.pal(9, "Blues")) )(255)

normalized_counts$genes <- rownames(normalized_counts)

#determine the gene names of our top 20 genes by ordering our results and extracting the top 20 genes (by padj values)

top20_sig_genes <- deGenes %>% 
  arrange(padj) %>% 	#Arrange rows by padj values
  pull(genes) %>% 		#Extract character vector of ordered genes
  head(n=25) 		#Extract the first 20 genes

# extract the normalized count values for these top 20 genes
## normalized counts for top 20 significant genes
top20_sig_norm <- normalized_counts %>% 
  filter(genes %in% top20_sig_genes)
# top20_sig_norm <- normalized_counts[normalized_counts$genes %in% top20_sig_genes, ]


# Gathering the columns to have normalized counts to a single column
gathered_top20_sig <- top20_sig_norm %>%
  gather(colnames(top20_sig_norm)[1:ncol(top20_sig_norm)-1], 
         key = "SampleName", value = "normalized_counts", -genes) 


## PLOT between Top 20 Significant Genes  & Normalized Count    #????
cat("\nGenerating SignificantGenes.png....")
CairoPNG(file ="SignificantGenes.png",width = 12, height = 8, units = "in", res = 300)
print(ggplot(gathered_top20_sig) +
        geom_point(aes(x = genes, y = normalized_counts, color = SampleName), 
                      size = 1.2,   #Point Size
                   alpha = 0.7) +  # Increase  transparency
        scale_y_log10() +         #Transforms y-axis to log10 scale.      
        guides(color = guide_legend(ncol = 10, byrow = TRUE)) +
        labs(
          x = "Genes",
          y = "Log10 Normalized Counts",
          title = "Top 25 Significant DE Genes Based on Adjusted P-value (padj)"
        ) +
        theme_bw() +    #Applies black-and-white theme.
        theme(
          axis.text.x = element_text(angle = 45, hjust = 1, size = 10),  # Adjust angle and size of x-axis labels
          axis.title = element_text(size = 12),  # Increase size of axis titles
          plot.title = element_text(hjust = 0.5, size = 12, face = "bold"),  # Center and bold the title
          legend.position = "top",  # Place legend at the top
          legend.title = element_text(size = 8,face = "bold"),  # Bold legend title
          legend.text = element_text(size = 6.5),  # Adjust legend text size
          legend.key.size = unit(0.2, "cm"),      # Shrinks the actual color boxes
          legend.spacing.x = unit(0.05, "cm"),    # Tightens space between box and text
          legend.spacing.y = unit(0.02, "cm"),    # Squeezes rows closer vertically
          legend.box.spacing = unit(0.1, "cm"),   # Pulls legend closer to the title
          plot.margin = margin(t = 10, r = 15, b = 10, l = 15)  # Increase margins (top, right, bottom, left)
          
        )
)

invisible(dev.off())

#############
print(sprintf("TOTAL No. of DE GENES IDENTIFIED: %d",nrow(deGenes)))
print(rownames(deGenes))

print("***********************   DESEQ RESULT ANALYSIS FINISED.")

