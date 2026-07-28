myargs = commandArgs(trailingOnly=TRUE)
# myargs[1] -> Base Working Directory Path
# myargs[2] -> R Custom Library Path
# myargs[3] -> Alpha P-Value cutoff (FDR)
# myargs[4] -> Absolute LogFC Threshold
# myargs[5] -> Statistical Method string
# myargs[6] -> Intercept Mode
# myargs[7] -> Group Parameter 1 / Contrast Key-Pairs
# myargs[8] -> Group Parameter 2
# myargs[9] -> Explicit Contrast Layout
# 
# myargs[1]="/mnt/ntfs/Output_RNASeq1/"
# myargs[2]="/home/iffy/NetBeansProjects/NGSGradle/app/R_Libraries/"
# myargs[3]="0.05"    #pvalue
# myargs[4]="1.5"       #logfc
# myargs[5]="exactTest"  #type
# myargs[6]="NO"    #DesignMode
# myargs[7]="male"     #Value1
# myargs[8]="female"    #value2
# myargs[9]="0,-1,1"   #contrast

setwd(myargs[1])#set current working directory
dir.create(file.path(myargs[1], "EdgeRResults"))
print("LOADING LIBRARIES FOR EDGER RESULTS ANALYSIS")
################################################################################
required_libs <- c("limma","edgeR", "RColorBrewer" ,"Cairo", "tidyverse")

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

#Variables
load(file = "edgeRDisp.RData")
pvalue=as.numeric(myargs[3])
logFC =as.numeric(myargs[4])
type = myargs[5]#"exactTest", "QLTest", "likelihoodTest"
designMode = myargs[6]
value1= myargs[7]#
value2= myargs[8]#

################################################################################
coef_names <- colnames(design)

cat("\n========== EDGER RESULT ANALYSIS STARTED ==========\n")
print("Displaying sample metadata including group assignments and normalization factors:")
print ( y$samples)

cat("Checking group levels in edgeR DGEList object:\n")
print(levels(y$samples$group))
cat("Number of samples in each group:\n")
print(table(y$samples$group))
#cat("Number of genes tested: ", nrow(dgeTest$table), "\n")

print("Design Matrix used for model fitting:")
print(design)

print(sprintf("Starting to run %s analysis",type))
setwd(file.path(myargs[1], "EdgeRResults"))


if(type == "exactTest"){

  #Classical approach to make pairwise comparison between groups
  #Will find DE gene expressed in value2 vs value1
  cat("\nRunning exactTest(",value1,value2,") to find genes DE in ",value2," vs ",value1," ...")
  dgeTest <- exactTest(y,pair = c(value1,value2)) #Names
  #fileName<-"exactTest.csv"
fileName <- paste0("exactTest_", value2, "_vs_", value1, ".csv")
plotFile <- paste0("MAPlot_exactTest_", value2, "_vs_", value1, ".png")
gseaFile <- paste0("gsea_geneList_exactTest_", value2, "_vs_", value1, ".RDS")
}else if(type =="glmQLFit+glmQLFTest(coef)"){
  #coef
cat("\n Running glmQLFit+glmQLFTest(coef)...\n ")
  cat("\nRunning glmQLFit with robust=TRUE...\n")
  fit <- glmQLFit(y, design, robust=TRUE)
  print("Displaying fit coefficients after running glmQLFit:")
  print(head(fit$coefficients))
  cat("Generating QL Dispersion Plot: PlotQLDisp(edgeR)1.png\n")
  
  CairoPNG(file ="PlotQLDisp(edgeR)1.png", width = 9000, height = 6000, res = 600)
  par(mar = c(5, 5, 4, 2)) 
  plotQLDisp(fit)
  invisible(dev.off())
  
  cat("\nDesign: ",coef_names)
      if(value2 == ""){
  
        value1=which(colnames(design) == paste0( myargs[7]))
      cat("\nRunning glmQLFTest on coef", value1, "(", coef_names[value1], ") ...\n")
      
    dgeTest <- glmQLFTest(fit, coef=value1)
    comp_name <- coef_names[value1]
    cat("This will test the null hypothesis that the **coefficient for this group is equal to 0**.\n")
    cat("In other words, it checks whether the group '", colnames(design)[value1], "' shows significantly non-zero expression relative to a baseline.\n")
    cat("Use this only if your design includes an intercept (e.g., ~group), and this coefficient represents a log-fold change.\n")
    
    
    }else{
    
      value1=which(colnames(design) == paste0( myargs[7]))
      value2=which(colnames(design) == paste0(myargs[8]))
      
      cat("\nRunning glmQLFTest on coefs", value1, "to", value2, "(", paste(coef_names[value1:value2], collapse = ", "), ") ...\n")
    
    dgeTest <- glmQLFTest(fit, coef=value1:value2)
    comp_name <- paste0(coef_names[value2], "_vs_", coef_names[value1])
    
    cat("This will perform a **joint test** to determine whether **any** of these coefficients are significantly different from 0.\n")
    cat("This is known as an **omnibus test** (or global test).\n")
    cat("In simple terms, it answers: 'Is there a significant effect in at least one of these groups?'\n")
    cat("NOTE: This does *not* tell you which specific group is significant or the direction of the effect.\n")
    cat("        It does *not* compare these groups to each other.\n")
    cat("        If you want to compare two specific groups (e.g., tb vs control), use a contrast vector instead.\n")
    
  }
  
  fileName <- paste0("glmQLFTest_", comp_name, ".csv")
  plotFile <- paste0("glmQLFTest_", comp_name,  ".png")
  gseaFile <- paste0("gsea_geneList_QLFcoef_", comp_name, ".RDS")
    #fileName<-"glmQLFTest_coef.csv"
  #  write.table(res_df,"glmQLFTest_coef.csv", row.names = FALSE,col.names = TRUE, sep = "\t")
}else if(type =="glmQLFit+glmQLFTest(contrast)"){ 
  #with contrast
  cat("\n Running glmQLFit+glmQLFTest(contrast)...\n ")
    cat("\nRunning glmQLFit with robust=TRUE...\n")
  fit <- glmQLFit(y, design, robust=TRUE)
  cat("Model coefficients:\n")
  print(head(fit$coefficients))
  
  cat("Generating QL Dispersion Plot: PlotQLDisp(edgeR)2.png\n")
  CairoPNG(file ="PlotQLDisp(edgeR)2.png", width=9000, height=6000, res=600)
  par(mar = c(5, 5, 4, 2)) 
  plotQLDisp(fit)
  invisible(dev.off())
  
#################
  # Step 1: Split into key:value pairs
  split_parts <- strsplit(unlist(strsplit(value1, ",")), ":")
  
  # Step 2: Create named vector
  contrast_named <- sapply(split_parts, function(x) as.numeric(x[2]))
  names(contrast_named) <- sapply(split_parts, function(x) x[1])
  
  # Step 3: Match order to design matrix columns
  design_cols <- colnames(design)
  contrast <- numeric(length(design_cols))
  names(contrast) <- design_cols
  
  # Fill in values from user input
  matching <- intersect(names(contrast_named), names(contrast))
  contrast[matching] <- contrast_named[matching]
  
  # Print and test
  cat("Using contrast vector:\n")
  print(contrast)
  
  
  #########
    # Convert the comma-separated string into a numeric vector
  #contrast <- as.numeric(unlist(strsplit(value1, ",")))
  #cat("Using contrast vector: ", paste(contrast, collapse = ", "), "\n")
  # Validate contrast length
  if (length(contrast) != ncol(design)) {
    stop(sprintf(
      "Contrast vector length (%d) does not match number of model coefficients (%d).\nDesign matrix columns: %s",
      length(contrast), ncol(design), paste(colnames(design), collapse = ", ")
    ))
  }
  cat("Running glmQLFTest with contrast...\n")
    dgeTest <- glmQLFTest(fit, contrast = contrast)
    
    cat("This method tests a **user-defined linear combination** of the model coefficients.\n")
    cat("The contrast vector defines the exact comparison to make between groups in the design matrix.\n")
    cat("Each value in the contrast vector corresponds to one coefficient (column) in the design matrix:\n")
    cat(" - A value of +1 or -1 means the group is included in the comparison.\n")
    cat(" - A value of 0 means the group is ignored.\n")
    cat(" - The test compares the weighted difference between groups defined by this vector.\n")
    cat(sprintf("Design matrix columns (%d): %s\n", ncol(design), paste(colnames(design), collapse = ", ")))
    cat(sprintf("Contrast vector used (%d):   %s\n", length(contrast), paste(contrast, collapse = ", ")))
    cat("   Contrast testing is most commonly used with designs like `~0 + group` (no intercept).\n")
    cat("   In that case, each group has its own column in the design matrix, making group-to-group comparisons straightforward.\n")
    cat("   If your design is `~group` (with intercept), be cautious — you may need to adjust contrast interpretation accordingly.\n")

  fileName<-"glmQLFTest_contrast.csv"
  plotFile <- "glmQLFTest_contrast.png"
  gseaFile <- "gsea_geneList_QLFcontrast.RDS"
  #write.table(res_df,"glmQLFTest_contrast.csv", row.names = FALSE,col.names = TRUE, sep = "\t")
}else if(type =="glmFit+glmLRT(coef)"){
  cat("\n Running glmQLFit+glmFit+glmLRT(coef)...\n ")
  cat("\nRunning glmQLFit with robust=TRUE...\n")
  
  fit <- glmFit(y, design, robust=TRUE)
  cat("Model coefficients:\n")
  print(head(fit$coefficients))
  cat("\nDesign: ",coef_names)
  if(value2 == ""){
    value1=which(colnames(design) == paste0(myargs[7]))
    cat("\nRunning glmLRT with coef =", value1, "(", coef_names[value1], ")\n")
    dgeTest <- glmLRT(fit, coef = value1)
    comp_name <- coef_names[value1]
    cat(sprintf("This will test whether the expression in group '%s' is significantly different from 0 (baseline).\n", coef_names[value1]))
    cat("Note: This is not a group-to-group comparison. It simply checks whether this coefficient (group mean) is statistically non-zero.\n")
    
  
}else{
  value1=which(colnames(design) == paste0(myargs[7]))
  value2=which(colnames(design) == paste0(myargs[8]))
  cat("\nRunning glmLRT with coef range =", value1, "to", value2, "\n")
  dgeTest <- glmLRT(fit, coef = value1:value2)
  comp_name <- paste0(coef_names[value2], "_vs_", coef_names[value1])
  
  cat(sprintf("This will jointly test whether any of the coefficients from group '%s' to '%s' are significantly different from 0.\n", coef_names[value1], coef_names[value2]))
  cat("This is a combined (omnibus) test: it checks if any of the selected groups have non-zero expression, not comparing them directly to each other.\n")
  cat("If you want to compare two specific groups (e.g., groupA vs groupB), consider using a contrast vector instead.\n")
}
  
  fileName <- paste0("glmLRT_", comp_name, ".csv")
  plotFile <- paste0("glmLRT_", comp_name, ".png")
  gseaFile <- paste0("gsea_geneList_LRTcoef_", comp_name, ".RDS")
  
}else if(type =="glmFit+glmLRT(contrast)"){
  cat("\nRunning glmFit for design matrix...\n")
  fit <- glmFit(y, design)
  cat("Model coefficients:\n")
  print(head(fit$coefficients))
  coef_names <- colnames(design)

  # Convert the comma-separated string into a numeric vector
  #contrast <- as.numeric(unlist(strsplit(value1, ",")))
  # Step 1: Split into key:value pairs
  split_parts <- strsplit(unlist(strsplit(value1, ",")), ":")
  
  # Step 2: Create named vector
  contrast_named <- sapply(split_parts, function(x) as.numeric(x[2]))
  names(contrast_named) <- sapply(split_parts, function(x) x[1])
  
  # Step 3: Match order to design matrix columns
  design_cols <- colnames(design)
  contrast <- numeric(length(design_cols))
  names(contrast) <- design_cols
  
  # Fill in values from user input
  matching <- intersect(names(contrast_named), names(contrast))
  contrast[matching] <- contrast_named[matching]
  
  # Print and test
  cat("Using contrast vector:\n")
  print(contrast)
  
  
  # Validate contrast length
  if (length(contrast) != ncol(design)) {
    stop(sprintf(
      "Contrast vector length (%d) does not match number of model coefficients (%d).\nDesign matrix columns: %s",
      length(contrast), ncol(design), paste(colnames(design), collapse = ", ")
    ))
  }
  cat("Running glmLRT with contrast...\n")
  dgeTest <- glmLRT(fit, contrast = contrast)  
  cat("This method uses a likelihood ratio test (LRT) to compare nested models.\n")
  cat("The contrast vector defines a linear combination of coefficients (groups) to test.\n")
  cat(" - Each number in the contrast corresponds to a column in the design matrix.\n")
  cat(" - A +1 or -1 indicates inclusion in the contrast; 0 means ignore that group.\n")
  cat(" - The contrast tests whether the **weighted difference** in log-expression between these groups is significantly different from 0.\n")
  cat(sprintf("Design matrix columns (%d): %s\n", ncol(design), paste(colnames(design), collapse = ", ")))
  cat(sprintf("Contrast vector (%d):       %s\n", length(contrast), paste(contrast, collapse = ", ")))
  

  fileName <- "glmLRT_contrast.csv"
  plotFile <- "glmLRT_contrast.png"
  gseaFile <- "gsea_geneList_LRTcontrast.RDS"
  
}else if(type =="glmTreat"){
  cat("\n Running glmTreat...\n ")
  
  cat("\nRunning glmQLFit with robust=TRUE...\n")
  fit <- glmQLFit(y, design, robust=TRUE)
  cat("Model coefficients:\n")
  print(fit$coefficients)
  
  if (designMode == "NO") {
  #  value1=myargs[7]  #values must be string
   # value2=myargs[8]
    
    # Design without intercept, use contrast
    cat(sprintf("\nUsing contrast mode with design '~0 + group': Testing %s vs %s (lfc > %.2f)", value2, value1, logFC))
    contrast_str <- paste0(value2, " - ", value1)
    contrast <- makeContrasts(contrasts = contrast_str, levels = design)
    dgeTest <- glmTreat(fit, contrast = contrast, lfc = logFC)
    comp_name    <- paste0(value2, "_vs_", value1)
  }else{
   
    # Design with intercept (~group), use coef
    cat(sprintf("\nUsing coefficient mode with design '~group': Testing coef for %s vs baseline (lfc > %.2f)", value1, logFC))
    # Determine coefficient index
    coef_idx <- which(colnames(design) == value1)
    if (length(coef_idx) == 0) {
      stop(sprintf("ERROR: Coefficient for group %s not found in design matrix.", value1))
    }
    
    dgeTest <- glmTreat(fit, coef = coef_idx, lfc = logFC)
    comp_name <- colnames(design)[coef_idx]
    
  }
  
  print(sprintf("glmQLFit produced coefficient matrix of dimensions: %d genes x %d conditions",
                nrow(fit$coefficients), ncol(fit$coefficients)))
  
  write.csv(fit$coefficients, file = "glmQLFit_coefficients.csv", row.names = TRUE)

 #   fileName <- paste0("glmTreat_", colnames(design)[value1], "_lfc", logFC, ".csv")
#  plotFile<-paste0("glmTreat_", colnames(design)[value1], "_lfc", logFC, ".png")
  
  fileName <- paste0("glmTreat_", comp_name, "_lfc", logFC, ".csv")
  plotFile <- paste0("glmTreat_", comp_name, "_lfc", logFC, ".png")
  gseaFile <- paste0("gsea_geneList_glmTreat_", comp_name, ".RDS")
 
}

################################################################################
#RESULT OF DE
################################################################################
res <- topTags(dgeTest, n=nrow(dgeTest$table))
res_df = as.data.frame(res)
res_df<-res_df[order(res_df$FDR),]
cat("Preview of DE results ( Check ",fileName," to explore full list):\n")
head(res_df)

cat("\nGenerating background dataset-wide GSEA logFC ranking array spectrum...\n")
gsea_vector <- res_df$logFC
clean_gene_names <- gsub("\\..*$", "", rownames(res_df))
names(gsea_vector) <- clean_gene_names
gsea_vector <- gsea_vector[!is.na(gsea_vector) & !is.na(names(gsea_vector))]
gsea_vector <- sort(gsea_vector, decreasing = TRUE)


cat("\nGenerating ", plotFile, "...\n")

CairoPNG(file =plotFile,width = 11, height = 7, units = "in", res = 300) #"MAPlot(edgeR-exactTest).png"
par(mar = c(6, 6, 5, 2) + 0.1)
# Plot the MA plot using base R plot function
plot(res_df$logCPM, res_df$logFC,
     xlab = "Average Log CPM (A)", ylab = "Log Fold Change (M)",
     main = "MA Plot of Differential Gene Expression: Log Fold Change vs Mean Expression", 
     pch = 16, col = ifelse(res_df$FDR < 0.05, "red", "black"),
     cex.main = 2.0,   # Makes the main title 200% larger
     cex.lab = 1.6,    # Makes X and Y axis labels 160% larger
     cex.axis = 1.3
     
     )

# Optionally, add a horizontal line at logFC = 0
abline(h=0, col="blue")
invisible(dev.off())


##### SIGNIFICANT GENES:
# Suggest minimum values found in res_df
min_padj <- min(res_df$FDR, na.rm = TRUE)
min_log2fold <- min(abs(res_df$logFC), na.rm = TRUE)
max_padj <- max(res_df$FDR, na.rm = TRUE)
max_log2fold <- max(abs(res_df$logFC), na.rm = TRUE)
print("Suggested thresholds based on exactTest results data:")
print(sprintf("PValue(FDR) limit = %f to %f", min_padj, max_padj))
print(sprintf("LogFC limit = %f to %f", min_log2fold,max_log2fold))
summary(res_df$logFC)

print(sprintf("Finding Significant genes based on the criteria: FDR < %.3g and |logFC| > %.2f", pvalue, logFC))
res_df$significant <- ifelse(res_df$FDR < pvalue & abs(res_df$logFC) > logFC, "Significant", NA) #Adding Significant col to res_df

# Count significant genes
num_sig <- sum(res_df$significant == "Significant", na.rm = TRUE)

if (num_sig == 0) {
  
  stop(sprintf("No significant genes found based on the given padj and log2FoldChange thresholds. Check padj or log2FoldChange range again."))
  
} else {
  
  print(sprintf("Significant genes found: %d",num_sig))
  # Extract significant genes
  deGenes <- res_df[which(res_df$significant == "Significant"), ]
  cat("\nTop significant genes:\n")
  print(head(deGenes))
  # Create individual unique output filenames based on comparison method to prevent accidental overwrites
  de_output_name <- paste0(tools::file_path_sans_ext(fileName), "_Significant_DE_Genes.csv")
  write.table(deGenes, file = de_output_name, row.names = FALSE, col.names = TRUE, sep = "\t", quote = FALSE)
  cat(sprintf("\nIdentified %d differential targets. Saved subset data frame to: %s\n", nrow(deGenes), de_output_name))
  
}

write.table(res_df,fileName, row.names = FALSE,col.names = TRUE, sep = "\t")
cat(sprintf("Full results saved to: %s\n", fileName))

print("Results Table ordered by FDR:")
print(head(res_df[order(res_df$FDR), ]))

deGenes <- res_df[which(res_df$significant == "Significant"),]
print(sprintf("Total number of DE Genes Identified: %d",nrow(deGenes)))

print("DE Genes:")
print(deGenes)
#write.table(deGenes, file = "DE_Genes.csv", row.names = FALSE, col.names = TRUE, sep = "\t", quote = FALSE)
#cat(sprintf("Saved significant genes to: %s\n", "DE_Genes.csv"))

setwd(file.path(myargs[1]))
saveRDS(deGenes, file = "degenesE.RDS")

# Export the GSEA list to the execution folder path cleanly
saveRDS(gsea_vector, file = gseaFile)
cat(sprintf("--> Success! GSEA list compiled cleanly with %d features saved to: %s\n", length(gsea_vector), gseaFile))

print("TOP Tags:")
print(topTags(dgeTest))
print("Result Summary:")
print(summary(decideTests(dgeTest)))
setwd(file.path(myargs[1], "EdgeRResults"))
# ------------------------------------------------------------------------------
# PLOT 1: VOLCANO PLOT (LogFC vs -Log10 FDR)
# ------------------------------------------------------------------------------
cat("\nGenerating VolcanoPlot(edgeR).png....")
CairoPNG(file = "VolcanoPlot(edgeR).png", width = 11, height = 7, units = "in", res = 300)

# Create a temporary significance mapping variable for plotting colors
res_df$volc_sig <- "Not Significant"
res_df$volc_sig[res_df$FDR < pvalue & res_df$logFC > logFC] <- "Up-regulated"
res_df$volc_sig[res_df$FDR < pvalue & res_df$logFC < -logFC] <- "Down-regulated"
res_df$volc_sig <- as.factor(res_df$volc_sig)

volc_plot <- ggplot(res_df, aes(x = logFC, y = -log10(FDR), color = volc_sig)) +
  geom_point(size = 1.2, alpha = 0.7) +
  scale_x_continuous(oob = scales::squish) +
  scale_color_manual(name = "Expression", 
                     values = c("Not Significant" = "grey60", "Up-regulated" = "firebrick3", "Down-regulated" = "navy")) +
  geom_hline(yintercept = -log10(pvalue), colour = "black", linetype = "dashed", linewidth = 0.4) +
  geom_vline(xintercept = c(-logFC, logFC), colour = "black", linetype = "dashed", linewidth = 0.4) +
  labs(x = "Log2 Fold Change (logFC)", y = "-Log10 FDR (Adjusted P-value)",
       title = "Volcano Plot of Differential Gene Expression",
       subtitle = paste0("Thresholds: FDR < ", pvalue, " | |logFC| > ", logFC)) +
  theme_bw() +
  theme(plot.title = element_text(hjust = 0.5, size = 12, face = "bold"),
        plot.subtitle = element_text(hjust = 0.5, size = 10, face = "italic"),
        axis.title = element_text(size = 11, face = "bold"),
        legend.position = "top")

print(volc_plot)
invisible(dev.off())

# ------------------------------------------------------------------------------
# PLOT 2: TOP 20 SIGNIFICANT GENES DOT PLOT (Dynamic Pipeline Scaling)
# ------------------------------------------------------------------------------
cat("\nGenerating SignificantGenes(edgeR).png....")
CairoPNG(file = "SignificantGenes(edgeR).png", width = 12, height = 8, units = "in", res = 300)

# 1. Pull the top 20 genes from your pre-filtered 'deGenes' data frame sorted by FDR
top20_sig_genes <- deGenes %>%  
  arrange(FDR) %>% 	
  pull(genes) %>% # Uses the 'genes' character vector column created in your pipeline 		
  head(n = 25) 		

normalized_counts <- cpm(y, normalized.lib.sizes = TRUE)
# 2. Extract and reshape normalized count coordinates
# Assumes 'normalized_counts' exists in your global environment from CPM calculation: e.g., cpm(dgList)
top20_sig_norm <- as.data.frame(normalized_counts) %>% 
  filter(rownames(.) %in% top20_sig_genes)
top20_sig_norm$genes <- rownames(top20_sig_norm)

gathered_top20_sig <- top20_sig_norm %>%
  gather(key = "SampleName", value = "counts", -genes)

# 3. Plot the Expression Distributions
print(
  ggplot(gathered_top20_sig) +
    geom_point(aes(x = genes, y = counts, color = SampleName), 
               size = 1.5, alpha = 0.8) +  
    scale_y_log10() +         
    guides(color = guide_legend(ncol = 10, byrow = TRUE)) + 
    labs(
      x = "Top 25 Genes (Ordered by FDR)",
      y = "Log10 Normalized Counts (CPM)",
      title = "Top 25 Significant DE Genes Across Individual Samples (edgeR)"
    ) +
    theme_bw() +    
    theme(
      axis.text.x = element_text(angle = 45, hjust = 1, size = 10, face = "bold"),  
      axis.title = element_text(size = 12, face = "bold"),  
      plot.title = element_text(hjust = 0.5, size = 12, face = "bold"),  
      
      # Clean ultra-compact legend adjustments to fit complex sample numbers safely
      legend.position = "top",  
      legend.title = element_text(size = 8, face = "bold"),  
      legend.text = element_text(size = 6), 
      legend.key.size = unit(0.2, "cm"),      
      legend.spacing.x = unit(0.05, "cm"),    
      legend.spacing.y = unit(0.02, "cm"),    
      legend.box.spacing = unit(0.1, "cm"),   
      plot.margin = margin(t = 10, r = 15, b = 10, l = 15)  
    )
)
invisible(dev.off())

cat("\nGenerating PValue_Histogram(edgeR).png....")
CairoPNG(file = "PValue_Histogram(edgeR).png", width = 7, height = 5, units = "in", res = 300)

par(mar = c(5, 5, 4, 2), mgp = c(2.8, 0.8, 0))
hist(res_df$PValue, 
     breaks = 40, 
     col = "cadetblue3", 
     border = "white",
     main = "P-value Distribution Frequency", 
     xlab = "Raw P-value", 
     ylab = "Frequency",
     cex.main = 1.1, 
     cex.lab = 1.0, 
     cex.axis = 0.8)

invisible(dev.off())

cat("\nGenerating SampleClustering(edgeR).png....")
CairoPNG(file = "SampleClustering(edgeR).png", width = 15, height = 7.5, units = "in", res = 300)

logcpm_matrix <- cpm(y, log = TRUE, prior.count = 2)
sample_dist <- as.dist(1 - cor(logcpm_matrix, method = "spearman"))
sample_hc   <- hclust(sample_dist, method = "average")
sample_tree <- as.dendrogram(sample_hc)

# Roomy bottom margin for your labels
par(mar = c(9, 4.5, 5.5, 1), mgp = c(3, 0.7, 0), cex = 1.0)

# Hide native labels completely
plot(sample_tree, main = "", ylab = "Height", xlab = "", leaflab = "none",
     cex.lab = 1.0, cex.axis = 0.80, font.lab = 2)

# Apply your exact manual text placement logic
sample_labels <- labels(sample_tree)
text(x = 1:length(sample_labels), 
     y = par("usr")[3] - (max(sample_hc$height) * 0.02), # Dynamic baseline positioning
     labels = sample_labels, 
     srt = 90,           # Vertical rotation
     adj = 1,            # Align right to branch tips
     xpd = TRUE,         # Allow drawing in margin space
     cex = 0.65,         # Your precise label scale
     font = 1)

mtext("Hierarchical Clustering of Samples\nBased on edgeR Log2 CPM Counts",
      side = 3, line = 3.2, cex = 1.3, font = 2, adj = 0.5)
mtext("Spearman Correlation Distance | Average Linkage",
      side = 3, line = 1.0, cex = 0.85, font = 3, adj = 0.5)

invisible(dev.off())


cat("\nGenerating GeneClustering(edgeR).png....")
CairoPNG(file = "GeneClustering(edgeR).png", width = 15, height = 7.5, units = "in", res = 300)

# 1. Calculate variance per gene row and extract index trackers for the top 100
gene_variances <- apply(logcpm_matrix, 1, var)
top100_genes   <- head(order(gene_variances, decreasing = TRUE), 100)
top100_matrix  <- logcpm_matrix[top100_genes, ]

# 2. Compute gene distance matrix using Pearson correlation
gene_dist <- as.dist(1 - cor(t(top100_matrix), method = "pearson"))
gene_hc   <- hclust(gene_dist, method = "complete")
gene_tree <- as.dendrogram(gene_hc)

# 3. Setup canvas parameters - added a bit more bottom margin for safely rotated text
par(mar = c(8.5, 4.5, 5.5, 1), mgp = c(3, 0.7, 0), cex = 1.0)

# --- THE FIX (Part 1): Turn off standard labels via leaflab = "none" ---
plot(gene_tree, main = "", ylab = "Height", xlab = "", 
     cex.lab = 1.0, cex.axis = 0.80, font.lab = 2,
     leaflab = "none") 

# --- THE FIX (Part 2): Extract, Truncate, and Manually Plot Gene Labels ---
raw_gene_labels <- labels(gene_tree)

# Draw the shortened text right below the leaf tips
text(x = 1:length(raw_gene_labels), 
     y = par("usr")[3] - (max(gene_hc$height) * 0.02), # Anchored just below 0 baseline
     labels = raw_gene_labels, 
     srt = 90,           # Rotate vertical
     adj = 1,            # Align right to the tree tips
     xpd = TRUE,         # Allow drawing inside the margin space
     cex = 0.55,         # Keep font crisp and small
     font = 1)

# Custom stacked titles
mtext("Hierarchical Clustering of Expression Profiles\n(Top 100 Most Variable Genes)", 
      side = 3, line = 3.2, cex = 1.3, font = 2, adj = 0.5)
mtext("Pearson Correlation Distance | Complete Linkage", 
      side = 3, line = 1.0, cex = 0.85, font = 3, adj = 0.5)

invisible(dev.off())

cat("\n======================================================\n")
cat("           DGER RESULTS ANALYSIS COMPLETED       \n")
cat("======================================================\n")