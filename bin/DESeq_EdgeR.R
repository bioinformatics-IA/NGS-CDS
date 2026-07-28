#===============================================================================
#                         COMMAND LINE ARGUMENTS
#===============================================================================
####      SETTING CURRENT WORKING DIRECTORY
myargs = commandArgs(trailingOnly=TRUE)
 
##############################################################################
#myargs[1]="/home/iffy/TestFiles/FINALOUTPUTS/"
#myargs[2]="/home/iffy/NetBeansProjects/NGSGradle/app/R_Libraries/"
#myargs[3]=10
#myargs[4]="MULTIPLE"
#myargs[5]="NO"
###########################################################################
dir.create(file.path(myargs[1], "DeSeqResults"))

setwd(file.path(myargs[1]))

print("Setting libraries path....")
################################################################################
# Set the library path to your custom location
.libPaths(c(myargs[2], .libPaths())) 
.libPaths()
#search()  #To check whic packages are loaded and from where
print("Loading libraries....")
#LOAD LIBRARIES
library(dplyr, lib.loc = myargs[2])
library(BiocGenerics, lib.loc = myargs[2])
library(S4Vectors, lib.loc = myargs[2])
library(IRanges, lib.loc = myargs[2])
library(GenomeInfoDb, lib.loc = myargs[2])
library(GenomicRanges, lib.loc = myargs[2])
library(matrixStats, lib.loc = myargs[2])
library(MatrixGenerics, lib.loc = myargs[2])
library(Biobase, lib.loc = myargs[2])
library(SummarizedExperiment, lib.loc = myargs[2])
library(memoise, lib.loc = myargs[2])
library(withr, lib.loc = myargs[2])
library(ggplot2, lib.loc = myargs[2])
library(genefilter, lib.loc = myargs[2])
library(DESeq2, lib.loc = myargs[2])
library( "RColorBrewer" , lib.loc = myargs[2] )
library("limma", lib.loc = myargs[2]) #plotMDS
library("vsn", lib.loc = myargs[2]) #For meanSDPlots
library("pheatmap", lib.loc = myargs[2])
library(ggrepel, lib.loc = myargs[2])
library(PCAtools, lib.loc = myargs[2])
library(Cairo, lib.loc = myargs[2])

library(farver, lib.loc = myargs[2])
library(labeling, lib.loc = myargs[2])
library(digest, lib.loc = myargs[2])
library(gridExtra,lib.loc = myargs[2])  # For arranging multiple ggplot objects
library(patchwork,lib.loc = myargs[2])
library(GGally,lib.loc = myargs[2])
library(rlang,lib.loc = myargs[2])
################################################################################

## VARIABLES
counts <- as.numeric(myargs[3]) #counts Threshold
factMode= myargs[4] #Factors SINGLE or MULTIPLE

# Load the count data
readcounts <- readRDS("count.RDS")

# Check the structure of readcounts
str(readcounts)

# Ensure the count data is in integer mode
readcounts <- round(as.matrix(readcounts))  # Round any floating-point values
mode(readcounts) <- "integer"               # Ensure the data is stored as integers

print(str(readcounts))

###     FUNCTIONS

sink.reset <- function(){
  for(i in seq_len(sink.number())){
    sink(NULL)
  }
}


#===============================================================================
#                    ANALYSIS OF FEATURECOUNTS
#===============================================================================
#ANALYSIS

print("READING sample_groups.tsv FILE......")

#Prepaing Sample Groups data frame
df=read.csv("sample_groups.tsv",sep = "\t", header = TRUE)#Reading modified csv file and creating samplegroups

#Set row names to the 1st 'SampleName' column
row.names(df) <- c(df$SampleName) 


print("Displaying details of data read from sample_group.tsv...")
print(str(df))


#check
all(colnames(readcounts) %in% rownames(df))
all(colnames(readcounts) == rownames(df)) #Are they in the same order

# Convert all character columns (except SampleName) to factors
df[, -1] <- lapply(df[, -1], function(x) if (is.character(x)) as.factor(x) else x)
# Check the structure of the data to ensure columns are now factors
print(str(df))
# Get all columns except SampleName
columns_to_check <- names(df)[-1]  # Assuming first column is SampleName

# Dynamically create a cross-tabulation (table) for all remaining columns
table(df[, columns_to_check])

#===============================================================================
#                   DESeq FUNCTION
#===============================================================================
setwd(file.path(myargs[1],"DeSeqResults"))
  print("STARTING DESeq2 ANALYSIS......")
  
CairoPNG(file ="LibrarySize_BarPlot.png",width=1000, height=800)
# Adjust margin sizes (bottom, left, top, right)
par(mar = c(10, 4, 4, 2) + 0.1)  # Increase the bottom margin to accommodate long labels
    librarySizes <- barplot(colSums(readcounts),  names=names(readcounts),col=brewer.pal(8, "Dark2"), las=2, main="Barplot of library sizes")
invisible(dev.off())

    #STEP 1: Preparing dds Matrix
  if(factMode=="MULTIPLE"){
    interaction= myargs[5]# YES or NO

    if(interaction == "YES"){
      interaction1=myargs[6]
      interaction2=myargs[7]
      design_formula=formula(paste("~", paste(paste(names(df)[2:ncol(df)], collapse = " + "),paste(" + ", paste(c(interaction1,interaction2), collapse = ":"))))) 
      #formula(paste("~", p(Second to Last column with +,Two columns with :)))   ~Gender + Disease + Drug + Time + Drug:Time
    }
    else if(interaction == "NO"){ #Selects all colums from Second to the Last e.g: ~Gender + Disease + Drug + Time
      design_formula <- formula(paste("~", paste(names(df)[2:ncol(df)], collapse = " + "))) #For all columns
  
      
    }
    
    
    
    print("Creating dds Matrix for following design: ")
    print(design_formula)
    dds <- DESeqDataSetFromMatrix(countData = readcounts,colData = df ,design = design_formula)

  }else  if(factMode=="SINGLE"){
    
    # Ensure second column of df is a factor
    df[[2]] <- as.factor(df[[2]])
    # Create design formula based on the second column of df
    design_formula <- formula(paste("~", paste(names(df)[2])))
    
    print("Creating dds Matrix for following design formula: ")
    print(design_formula)
    
    # Check the structure of readcounts
    str(readcounts)
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
    
    
      dds <- DESeqDataSetFromMatrix(countData = readcounts, colData = df ,design = design_formula)
  }

 
  print("Initial dds Matrix:")
  print(dds)
  print("dds colData")
  print(colData(dds))
  
  #OPTIONAL: FILTERING
  print("Performing Filtering..... ")
  keep <- rowSums(counts(dds))>=counts #reads having counts >=10 are selected
  
  dds <- dds[keep,] #Modifying dds with filtered rows
  print("dds Matrix after Filtering")
  print(dds)
  print("dss assay")
  print(assay(dds))

    #STEP 2: RUNING DESEQ PIPELINE
  
  print("Running DESeq Analysis....")
  
  dds <- DESeq(dds)
  # Get the results
  res <- results(dds)
  
  
  print("dds after DESeq Analysis")
  print(dds)
  print("coef(dds)")
  print(coef(dds))
  print("Cooks Distance")
  print(assays(dds)[["cooks"]])
  print("H")
  print(assays(dds)[["H"]])
  print("mu")
  print(assays(dds)[["mu"]])
  print("Displaying results names....")
  resultName = paste(strsplit(resultsNames(dds),' '),collapse=',')
  print(resultName)
  print(resultsNames(dds))
  
  
  ####################
  ##NORMALIZED COUNTS
  ###################
  
  normalized_counts <- as.data.frame( counts(dds, normalized=TRUE))
  print("Normalized Counts")
  print(normalized_counts)
  setwd(file.path(myargs[1]))
  saveRDS(dds, file = "dds.RDS") #Save single object
  setwd(file.path(myargs[1],"DeSeqResults"))
 
  sink.reset()
  
  #Normalization factors calculated by estimateSizeFactors
  #A normalization factor below one indicates that the library size will be scaled down This is also equivalent to scaling the counts upwards in that sample
  # a factor above one scales up the library size and is equivalent to downscaling the counts.
  #dds@colData$sizeFactor
  #####################################
  # COUNTS PLOTS: BOXPLOT, DENSITYPLOT
  ####################################
  # Log-transform counts
  # Calculate cpm counts for unnormalized and normalized data
  logcounts <- cpm(readcounts,log=TRUE)#log2(readcounts + 1)        
  lognormalized_counts <- cpm(normalized_counts,log=TRUE)#log2(normalized_counts + 1) 
  
  # Save the boxplots in one file using CairoPNG
  CairoPNG(file = "Counts_Boxplots.png", width = 1440, height = 720)
  # Set up the plotting area with 1 row and 2 columns
  par(mfrow = c(1, 2))
  # Increase the bottom margin to accommodate long labels
  par(mar = c(10, 4, 4, 2) + 0.1)
  
    boxplot(logcounts, main="Unnormalized Log2 Counts", col=rainbow(ncol(readcounts)), las=2)
    boxplot(lognormalized_counts, main="Normalized Log2 Counts", col=rainbow(ncol(normalized_counts)), las=2)
  # Close the graphical device
  invisible(dev.off())
  
  # Save the densityplots in one file using CairoPNG
  CairoPNG(file = "Counts_Densityplots.png", width = 1440, height = 720)
  # Set up the plotting area with 1 row and 2 columns
  par(mfrow = c(1, 2))
  # Increase the bottom margin to accommodate long labels
  par(mar = c(10, 4, 4, 2) + 0.1)
  
  # Calculate the maximum y-axis limit for unnormalized counts
  unnorm_max <- max(sapply(1:ncol(readcounts), function(i) max(density(log2(readcounts[,i] + 1))$y)))
  
  plot(density(log2(readcounts[,1] + 1)), main="Unnormalized Counts Distribution", col="blue", lwd=2, ylim=c(0, unnorm_max))
  for(i in 2:ncol(readcounts)) {
    lines(density(log2(readcounts[,i] + 1)), col=rainbow(ncol(readcounts))[i], lwd=2)
  }
  # Calculate the maximum y-axis limit for normalized counts
  norm_max <- max(sapply(1:ncol(normalized_counts), function(i) max(density(log2(normalized_counts[,i] + 1))$y)))
  
  plot(density(log2(normalized_counts[,1] + 1)), main="Normalized Counts Distribution", col="blue", lwd=2, ylim=c(0, norm_max))
  for(i in 2:ncol(normalized_counts)) {
    lines(density(log2(normalized_counts[,i] + 1)), col=rainbow(ncol(normalized_counts))[i], lwd=2)
  }
  
  # Close the graphical device
  invisible(dev.off())
  
  ##############################################################################
  
  # Calculate the appropriate layout for the plots
  n <- ncol(readcounts)
  rows <- ceiling(sqrt(n))   # Number of rows
  cols <- ceiling(n / rows)  # Number of columns
  
  # Open the PNG device for output
  CairoPNG(file = "LogUnNormalized_Plot.png", width = 1000, height = 800)
  
  # Set up the plot layout
  par(mfrow = c(rows, cols))
  
  # Loop through each sample (column) in readcounts
  for (g in 1:ncol(readcounts)) {
    plot(logcounts[, g], main = colnames(readcounts)[g], xlab = "Gene Index", ylab = "Log2 Count")
    abline(h = 0, col = "blue")  # Horizontal line at 0
  }
  
  # Add a global title
  title("Log2 of Unnormalized Counts Data", line = -1, outer = TRUE)
  
  # Close the PNG device
  invisible(dev.off())
  
  #####################################
  # MA PLOTS: dds, res, shrunken_res
  #  log ratios (A) against the mean expression levels (M) 
  ####################################
   #1 MA PLOT Between logfoldchange & mean of normalized counts
  CairoPNG(file ="MA(DeSeq)_Plots.png",width=1440, height=720) 
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
  result_names <- resultsNames(dds)
  coef_to_shrink <- result_names[!result_names %in% "Intercept"]
  
  # Create a list to store shrunken results
  shrunken_results_list <- list()
  
  # Apply lfcShrink to each coefficient except Intercept
  for (coef in coef_to_shrink) {
    shrunken_results <- lfcShrink(dds, coef=coef, type="apeglm")
    shrunken_results_list[[coef]] <- shrunken_results
  }
  
  # Set up a PNG file to save the plots
  CairoPNG(file = "MA(DeSeq)_ShrunkenResultsPlot.png", width = 1440, height = 720)
  
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
  
  #############################################################################
  
  #Regularized log transformation
  rld <- rlog(dds, blind=FALSE)
  print("Regularized log:")
  print(rld)
  print(rowData(rld))

  # Transform count data using the variance stablilizing transform
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
  print("Variance stablilizing transform:")
  print(VST)
  
  
  #####################################
  # MDS PLOTS: rld, VST
  #  represent variance between samples 
  ####################################
  CairoPNG(file ="MDS(DeSeq)_Plots.png",width = 1440, height = 720) 
  # Set up the plotting area with 1 row and 2 columns
  par(mfrow = c(1, 2))
  
  limma::plotMDS(assay(rld),gene.selection = "common",main="MDS PLOT of rlog Object(rld)", col=brewer.pal(8, "Dark2"))
  limma::plotMDS(assay(VST),gene.selection = "common",main="MDS PLOT of VST Object(VST)", col=brewer.pal(8, "Dark2"))
  
  invisible(dev.off())
################################  
  #3 Plot Dispersion Estimates
  CairoPNG(file ="Dispersion(DeSeq)_Plot.png",width=1000, height=720) 
  plotDispEsts(dds ,main="Dispersion Plot of dds")
  invisible(dev.off())
  
  
    
  #SAMPLE DISTANCE for HEATMAP
  sampleDists <- dist( t( assay(rld) ) )
  print("Sample Distance using rld")
  print(sampleDists)
  sampleDistMatrix <- as.matrix( sampleDists ) #Will be used to draw heatmap
  #Checking out total number of groups to draw on heatmap
  t<- ncol(df)#ncol(colData(rld))-1
  a<-""
  for (num in 2:t){
    
    a<- paste(a,rld[[num]], sep="-" )
  }
  print(a)
  rownames(sampleDistMatrix) <- paste(a)#Adding groups names to row names
  
  

  
  #####################################
  ## Cluster Plot (Dendrogram)
  ## VST, rld
  #####################################
  
  
  CairoPNG(file ="Cluster(DeSeq)_Plot.png",width = 1440, height = 720) 
  # Set up the plotting area with 1 row and 2 columns
  par(mfrow = c(1, 2)) 
  # Perform hierarchical clustering
  d <- dist(t(assay(rld))) 
  hc <- hclust(d)
 
  # Plot the dendrogram
  plot(hc, labels = colnames(assay(rld)), main = "Hierarchical Clustering of Samples(rlog)", sub = "", 
       xlab = "", ylab = "Height", 
       hang = -1,  # Adjusts the position of labels
       cex = 0.7)
  
  # Perform hierarchical clustering
  d <- dist(t(assay(VST))) 
  hc <- hclust(d)
  # Plot the dendrogram
  plot(hc, labels = colnames(assay(VST)), main = "Hierarchical Clustering of Samples(VST)", sub = "", 
       xlab = "", ylab = "Height", 
       hang = -1,  # Adjusts the position of labels
       cex = 0.7)
  invisible(dev.off())
  
  #################################################################
  #####################################
  ## MeanSD PLOTS
  ## normalized counts, rlog,VST
  #####################################
  # Check rows with counts greater than 0
  notAllZero <- (rowSums(counts(dds)) > 0)
  
  # Generate meanSdPlot for each dataset, customize, and store them as ggplot objects
  p1 <- meanSdPlot(log2(counts(dds, normalized = TRUE)[notAllZero, ]), plot = FALSE)$gg +
    ggtitle("Mean-SD Plot for Normalized Counts") + 
    theme(plot.title = element_text(hjust = 0.5)) 
  
  p2 <- meanSdPlot(assay(rld[notAllZero, ]), plot = FALSE)$gg +
    ggtitle("Mean-SD Plot for Regularized Log Transformation") +
    theme(plot.title = element_text(hjust = 0.5)) 
  
  p3 <- meanSdPlot(assay(VST[notAllZero, ]), plot = FALSE)$gg +
    ggtitle("Mean-SD Plot for VST Transformation") +
    theme(plot.title = element_text(hjust = 0.5)) 
  # Save the combined plots in a single file
  CairoPNG(file = "meanSD(DeSeq)_Plots.png", width = 1440, height = 1440)
  
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
  CairoPNG(file ="Heatmap1(DeSeq)_Plot.png",width=720, height=540) 
  print(pheatmap(sampleDistMatrix,trace = "none",col=colours,main="Heatmap of Euclidean sample distances after rlog transformation."))
  invisible(dev.off())
 
  #Let us select the 35 genes with the highest variance across samples
  topVarGenes <- head( order( rowVars( assay(rld) ), decreasing=TRUE ), 35 )
  CairoPNG(file ="Heatmap2(DeSeq)_Plot.png",width=720, height=540) 
  print(pheatmap( assay(rld)[ topVarGenes, ], scale="row",
                  trace="none", dendrogram="column",
                  col = colorRampPalette( rev(brewer.pal(9, "RdBu")) )(255),main="Top 35 Genes with Variance across Samples"))
  invisible(dev.off())
  
  ######################
  # PCA PLOTS: 
  # rlog, VST
  ######################  
  
  p1<- DESeq2::plotPCA(rld, intgroup=names(colData(rld))[2:t] ,ntop=500 ) + 
    ggtitle("DESeq2 PCA PLOT rlog")
  p2<- DESeq2::plotPCA(VST, intgroup=names(colData(VST))[2:t] ,ntop=500 ) + 
    ggtitle("DESeq2 PCA PLOT VST")
  #13 PLOT PCA of rlog
  CairoPNG(file ="PCA(DeSeq)_Plots.png",width = 1440, height = 720) 
  # Arrange the plots in one row with 3 columns
  grid.arrange(p1, p2, ncol = 2)
  
  invisible(dev.off())
  
  
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
  CairoPNG(file ="BiPlot(DeSeq)_Plot.png",width=800, height=700) 
  # Plot the PCA biplot without replacing scales
  print(
    PCAtools::biplot(
      p,
      showLoadings = TRUE,
      labSize = 3,     # Decrease sample label font size
      pointSize = 3,   # Keep the point size
      sizeLoadingsNames = 3,  # Decrease gene label font size
      title = "PCA Biplot"
    ) +
      ggplot2::coord_cartesian(xlim = x_limits, ylim = y_limits)  # Apply dynamic limits
  )
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
    unique_levels <- length(unique(p$metadata[[i]]))  # Get the number of unique levels
    # Repeat colors if there are not enough colors
    # Randomly select colors from the color vector for the unique levels
    selected_colors <- sample(colors, size = unique_levels, replace = TRUE)
    
    
      plot <- PCAtools::biplot(p, 
                             lab = names(p$metadata)[i], 
                             colby = names(p$metadata)[i], 
                             hline = 0, 
                             vline = 0, 
                             legendPosition = 'right')+
        ggplot2::scale_color_manual(values = selected_colors)  # Use the repeated colors
      
    
    
    plot_list[[i - 1]] <- plot  # Store the plot
  }
  
  # Combine all the plots into a single grid layout
  combined_plot <- patchwork::wrap_plots(plot_list, ncol = 2)  # Arrange in 2 columns
  
  # Save the combined plot as an image
  Cairo::CairoPNG(file = "BiPlot(Deseq)_Combined.png", width = 1440, height = 1080)
  print(combined_plot)
  invisible(dev.off())
  
  
#####################
# LOADINGS PLOT:   All components, 1st 4 components
####################

  #Loadings
  CairoPNG(file ="Loadings(DeSeq)_Plot1.png",width=1500, height=900) 
  print(PCAtools::plotloadings(p,
                     components = getComponents(p, c(1:length(p$components))),
                     rangeRetain = 0.1,
                     labSize = 3.0,
                     absolute = FALSE,
                     title = 'PCA Loadings plot',
                     subtitle = 'Misc PCs',
                     caption = 'Top 10% variables',
                     shape = 23, shapeSizeRange = c(1, 16),
                     col = c('white', 'blue',"red"),
                     drawConnectors = FALSE))
  
  
  invisible(dev.off())
  
  CairoPNG(file ="Loadings(DeSeq)_Plot2.png",width=1500, height=900) 
  print(PCAtools::plotloadings(p,
                               components = getComponents(p, c(1:4)),
                               rangeRetain = 0.1,
                               labSize = 4.0,
                               absolute = FALSE,
                               title = 'PCA Loadings plot',
                               subtitle = 'Misc PCs',
                               caption = 'Top 10% variables',
                               shape = 23, shapeSizeRange = c(1, 16),
                               col = c('white', 'blue',"red"),
                               drawConnectors = TRUE))
  invisible(dev.off())
  
  
  
  
  ###########################
  # Pair Plots: on 1st five Pc, Components wise plots
  ##########################
  
  
##################################333
  # Temporarily redirect output and warnings to a null connection
  #sink("/dev/null")
  CairoPNG(file ="Pairs(DeSeq)_Plot.png",width=1000, height=800) 
  # Suppress warnings during plot creation
 
  suppressWarnings({
    pairs_plot <- pairsplot(p, components = getComponents(p, c(1:5)), 
                            title = "PCA Pairs Plot of Components 1-5")  
                            
    print(pairs_plot)
 
  })
  invisible(dev.off())
  #sink()  # Reset the sink
  
  ########################################
  # Pair plots by components
  for (i in 2:ncol(df)) {
    Cairo::CairoPNG(file = paste0("Pairplot_",names(p$metadata)[i],".png"), width = 1200, height = 800)
       pairs_plot<-pairsplot(p,
            components = getComponents(p, c(1:5)),
            colby = names(p$metadata)[i],
            title = paste0("Pairplot of ",names(p$metadata)[i])#,margingaps = unit(c(-0.02, -0.02, -0.02, -0.02), 'cm')
            )
      print(pairs_plot)
    invisible(dev.off())
  }
 ###########################################

##############################
# Scree Plot: hornsVST
##############################
  
  #Let’s perform Horn’s parallel analysis first:
  s<-as.matrix(assay(VST)) 
  num_samples <- nrow(s)
  num_features <- ncol(s)
  max_components <- min(num_samples, num_features)
  horn <- parallelPCA(s, max.rank = max_components)
  
  #Now the elbow method:
  elbow <- findElbowPoint(p$variance)
  
  CairoPNG(file ="Scree(DeSeq)_Plot.png",width=720, height=540) 
  print(screeplot(p,
                  components = getComponents(p, 1:length(p$components)),
                  vline = c(horn$n, elbow)) +
          annotate("text", x = horn$n + 1, y = 50,label = "Horn's", vjust = -2, size = 8) +
          annotate("text", x = elbow + 1, y = 50, label = "Elbow method", vjust = -1, size = 8)
       
        )
  
  invisible(dev.off())
  #########################
  # EIGENCOR PLOT:  on all component, on horn selected components 
  #########################
  # Prepare meta data for eigencor plots
  p <- PCAtools::pca(assay(VST), metadata = colData(VST), removeVar = 0.1)
  
  # Retrieve colData
  col_data <- colData(VST)
  
  # Identify factor columns programmatically
  factor_columns <- sapply(col_data, is.factor)
  
  # Convert factor columns to numeric
  col_data[, factor_columns] <- lapply(col_data[, factor_columns], function(x) as.numeric(x) - 1)  # Convert to 0/1
  
  # Run PCA using PCAtools with updated metadata from colData
  p1 <- PCAtools::pca(assay(VST), metadata = col_data, removeVar = 0.1)
  
  ##############################################################
  
  
  CairoPNG(file ="Eigencor1(DeSeq)_Plot.png",width=1000, height=800) 
  eigencorplot(p1,
               components = getComponents(p1, 1:length(p1$components)),
               metavars = c(names(df)[2:ncol(df)]),
               col = c('white', 'cornsilk1', 'gold', 'forestgreen', 'darkgreen'),
               main = "Eigenvalue Correlation Analysis: Principal Components vs. Metadata Variables",
               rotLabX = 45,  # Rotation of x-axis labels
               cexMain= 1,
               
               ) 

  invisible(dev.off())
  
 CairoPNG(file ="Eigencor2(DeSeq)_Plot.png",width=1000, height=800) 
  eigencorplot(p1,
               components = getComponents(p1, 1:horn$n),  # Specifies the components to include based on Horn's analysis
               metavars = c(names(df)[2:ncol(df)]),  # Uses metadata variables from the data frame, excluding the first column
               col = c('white', 'cornsilk1', 'gold', 'forestgreen', 'darkgreen'),  # Color scheme for the plot
               rotLabX = 45,  # Rotation of x-axis labels
               cexMain= 1,
               main = paste0("Eigenvalue Correlation of Principal Components with Clinical Variables (Up to Horn's Suggested Components)", 
                             "\n", 
                             "Principal Component Pearson r^2 Clinical Correlates"),  # Title and subtitle
               plotRsquared = TRUE,  # Plot the R-squared values
               corMultipleTestCorrection = 'BH',  # Benjamini-Hochberg correction for multiple testing
               )

  invisible(dev.off())
#Supress Warnings

 print("DESeq Matrix and Plots generated")

 