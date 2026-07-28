

# Default Directory
myargs = commandArgs(trailingOnly=TRUE)
################################################################################
# myargs[1]="/home/iffy/PhD_Data/CASESTUDY/1-Hepatocellular_carcinoma_100/RNASeq_PRJNA867011/TestingDESeq2/"   # 1: Output Directory
# myargs[2]="/home/iffy/NetBeansProjects/NGSGradle/app/R_Libraries" # 2: Lib Path
# myargs[3]="ENSEMBL" # 3: Form Type
# myargs[4]="org.Hs.eg.db"#"org.Mm.eg.db" # 4: Organism DB
# 
# myargs[5]="MF"  # 5: GO Sub ontology
# myargs[6]=1     # 6: GO Level
# 
# myargs[7]="ALL" # 7: enrichGO sub
# myargs[8]="BH"  # 8: enrichGO p-adjust method
# myargs[9]=0.05  # 9: enrichGO p-value cutoff
# myargs[10]=0.2  # 10: enrichGO q-value cutoff
# 
# myargs[11]="ALL"  # 11: gseGO sub
# myargs[12]="none" # 12: gseGO p-adjust method
# myargs[13]=0.05 # 13: gseGO p-value cutoff
# myargs[14] <- "fgsea" # 14: gseBy method
# myargs[15]<- as.numeric(1e-10)  # 15: GSEA epsilon value
# #KEGG
# myargs[16]="hsa"#"mmu"   # 16: KEGG Organism ID
# 
# myargs[17]="BH" # 17: FIXED: KEGG enrich p-adjust (Changed from 'none' to 'BH')
# myargs[18]=0.05 # 18: KEGG enrich p-value cutoff
# 
# myargs[19]="BH" # 19: FIXED: KEGG gse p-adjust (Changed from 'none' to 'BH')
# myargs[20]=0.05 # 20: KEGG gse p-value cutoff
# 
# myargs[21]=1  # 21: mkeggPcutoff
# myargs[22]=1  # 22: mkeggQcutoff
# myargs[23]=1  # 23: gmkeggPcutoff
# 
# myargs[24]="deSeq2" #"edgeR" # # 24: Analysis type
# filepath="/home/iffy/ssh_folder/Output/EdgeRResults/exactTest.csv"


################################################################################
print("LOADING LIBRARIES FOR GSEA ANALYSIS....")
#Setting Library Path
old_libraries <- .libPaths()
.libPaths(c(myargs[2], old_libraries))
.libPaths()
required_libs <- c("BiocManager","biomaRt","ggplot2","ggridges", "cowplot",
                   "Cairo","ggupset", "gridExtra","clusterProfiler"
                   ,"KEGGREST","pathview","ReactomePA","DOSE","GO.db") 

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
#STEP1: INSTALLING/LOADING ORGANISM 
organism<-myargs[4] #"org.Mm.eg.db"#
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
#Arguments
# Global Variable Extractors
groupSub <- myargs[5]
groupLevel <- as.numeric(myargs[6])

enrichSub <- myargs[7]
enrichP <- myargs[8]
enrichPcutoff <- as.numeric(myargs[9])
enrichQcutoff <- as.numeric(myargs[10])

gseSub <- myargs[11]
gseP <- myargs[12]
gsePcutoff <- as.numeric(myargs[13])
gseBy <- myargs[14]
eps<- as.numeric(myargs[15])

# KEGG Variable Extractors
org = myargs[16]

enrichP1 <-myargs[17]
enrichPcutoff1 <- as.numeric(myargs[18])

gseP1 <- myargs[19]
gsePcutoff1 <- as.numeric(myargs[20])


mkeggPcutoff <-as.numeric(myargs[21])
mkeggQcutoff <- as.numeric(myargs[22])
gmkeggPcutoff <- as.numeric(myargs[23])


############################################################################
frmType <-myargs[3]
analysis <- myargs[24]
#filepath <- myargs[25]


sink.reset <- function(){
  for(i in seq_len(sink.number())){
    sink(NULL)
  }
}

################################################################################
#GENE SET ENRICHMENT FUNCTION

geneSetAnalysis<-function(deGenes,folderN,analysis_type){ 

  cat("\n====================================================================\n")
  cat(sprintf("[PROCESS STARTED] INITIALIZING GSEA WORKFLOW FOR: %s\n", analysis_type))
  cat("====================================================================\n")
  cat(sprintf("Input Gene Identifier Type: %s\n", frmType))
   
    frmType_map <- list(
    "ENSEMBL"  = list(biomart = "ensembl_gene_id", dbi = "ENSEMBL"),
    "SYMBOL"   = list(biomart = "hgnc_symbol",      dbi = "SYMBOL"),
    "ENTREZID" = list(biomart = "entrezgene_id",    dbi = "ENTREZID"),
    "UNIPROT"  = list(biomart = "uniprotswissprot",  dbi = "UNIPROT")
  )
  
  if(!frmType %in% names(frmType_map)) {
    stop(sprintf("[ERROR] Selected format type '%s' is not supported by mapping tables.", frmType))
  }
 
  biomart_fromAttr <- frmType_map[[frmType]]$biomart
  dbi_keytype      <- frmType_map[[frmType]]$dbi
  frmType <- biomart_fromAttr
  cat(sprintf("Incoming feature identifier profile set to: %s\n", frmType))
  
  
  # ============================================================================
  # GLOBAL BACKGROUND GENE PROCESSING METRICS
  # ============================================================================
  
  # Track the initial number of raw input features:
  initial_bg_count <- length(unique(names(global_raw_list)))
  cat(sprintf("Total raw background genes loaded from dataset: %d\n", initial_bg_count))
  cat(" Querying AnnotationDbi for local background database mapping....\n")
  global_mapping <- suppressMessages(AnnotationDbi::select(
    get(organism), 
    keys    = names(global_raw_list), 
    columns = "ENTREZID", 
    keytype = dbi_keytype
  ))
  # Count how many total rows were returned before filtering
  raw_mapped_rows <- nrow(global_mapping)
  
  cat(sprintf("Total Reference Rows returned by AnnotationDbi: %d\n", raw_mapped_rows))
  cat(sprintf("Duplicated rows generated due to 1-to-many mappings: %d\n", raw_mapped_rows - initial_bg_count))
  global_mapping <- global_mapping[!is.na(global_mapping$ENTREZID) & global_mapping$ENTREZID != "", ]
  
  # Track the final counts after cleaning
  final_mapped_count <- length(unique(global_mapping[[dbi_keytype]]))
  removed_count <- initial_bg_count - final_mapped_count
  
  cat("\n========= MAPPING EFFICIENCY SUMMARY =========\n")
  cat(sprintf("Total unique keys successfully mapped to active Entrez IDs: %d\n", final_mapped_count))
  cat(sprintf("Total unmapped records dropped (NA or Empty string values): %d\n", removed_count))
  cat("============================================================\n\n")
  
  # Create background dataframe, sort by absolute LogFC, and remove duplicate Entrez IDs
  prep_df <- data.frame(
    Key = names(global_raw_list),
    LogFC = as.numeric(global_raw_list),
    AbsLogFC = abs(as.numeric(global_raw_list))
  )
  prep_df <- merge(prep_df, global_mapping, by.x = "Key", by.y = dbi_keytype)
  prep_df <- prep_df[order(prep_df$AbsLogFC, decreasing = TRUE), ]
  
  dedup_gsea_df <- prep_df[!duplicated(prep_df$ENTREZID), ]
  
  cat(sprintf("Combined data frame rows after merge processing           : %d\n", nrow(prep_df)))
  cat(sprintf("Redundant shared-destination Entrez rows removed          : %d\n", nrow(prep_df) - nrow(dedup_gsea_df)))
  cat(sprintf("Final un-duplicated background genes saved into rank list : %d\n", nrow(dedup_gsea_df)))
  cat("============================================================\n\n")
   # Create the final ordered vector clusterProfiler's GSEA functions demand
  geneList <- dedup_gsea_df$LogFC
  names(geneList) <- dedup_gsea_df$ENTREZID
  geneList <- sort(geneList, decreasing = TRUE)

    #-----------------------------------------------------------------------------
  #                     Global Gene List Completed
  #-----------------------------------------------------------------------------
  
  cat("-----------------------------------------------------------------------------\n")
  cat("                        PREPARING DE GENES LIST FOR GSEA                     \n")
  cat("-----------------------------------------------------------------------------\n")
  
  
  # CALCULATING UNIPROT, ENTERZID, SYMBOL, ENSEMBL ACCORDING TO frmType  
  toTypeOptions <- list(
    ensembl_gene_id = c("uniprotswissprot", "hgnc_symbol", "entrezgene_id"),
    hgnc_symbol = c("ensembl_gene_id", "uniprotswissprot", "entrezgene_id"),
    entrezgene_id = c("uniprotswissprot", "hgnc_symbol", "ensembl_gene_id"),
    uniprotswissprot = c("ensembl_gene_id", "hgnc_symbol", "entrezgene_id")
  )

  # Remove version suffix from Ensembl IDs
  if (frmType == "ensembl_gene_id") {
    cat("\nTruncating Ensembl version decimals for bioMart compatibility...\n")
    deGenes$genes <- sub("\\..*", "", deGenes$genes)
    cat("Cleaned IDs:\n")
    print(head(deGenes$genes)) 
  }
 
  if (frmType %in% names(toTypeOptions)) {
    
    # Build attribute list for getBM()
    fromAttr <- frmType
    toAttrs <- toTypeOptions[[frmType]]
    
    bm_res <- NULL
    
    cat("[BIOMART] Connecting to Ensembl Live Database...\n")
    mirrors <- c("www", "useast", "asia")
    mart <- NULL
    
    for (m in mirrors) {
      cat(sprintf("Attempting connection via mirror: '%s'...\n", m))
      tryCatch({
        if (m == "www") {
          mart <- useMart("ensembl", dataset = "hsapiens_gene_ensembl")
        } else {
          mart <- useEnsembl(biomart = "genes", dataset = "hsapiens_gene_ensembl", mirror = m)
        }
        if (!is.null(mart)) {
          cat(sprintf("Success! Connected using the '%s' server.\n", m))
          break
        }
      }, error = function(e) {
        cat(sprintf("Warning: Mirror '%s' failed or redirected. Trying next available...\n", m))
      })
    }
    
    # If web servers responded, run your original biomaRt query
    if (!is.null(mart)) {
      cat("[BIOMART] Querying conversion cross-references...\n")
      tryCatch({
        bm_res <- getBM(
          attributes = c(fromAttr, toAttrs),
          filters    = fromAttr,
          values     = deGenes$genes,
          mart       = mart
        )
      }, error = function(e) {
        cat("Warning: getBM query failed on live server. Tripping local fallback...\n")
        bm_res <- NULL
      })
    }else {
      # LOCAL DATABASE FALLBACK VIA AN OR CONDITION EFFECT
      cat("\n[NOTICE] Ensembl web services failed! Swapping immediately to local database mapping via AnnotationDbi...\n")
      
      # Mirror the exact biomaRt system naming convention to match your code downstream
      dbi_map <- c(ensembl_gene_id = "ENSEMBL", hgnc_symbol = "SYMBOL", entrezgene_id = "ENTREZID", uniprotswissprot = "UNIPROT")
      
      # Select locally using your preset variables
      local_res <- suppressMessages(AnnotationDbi::select(
        get(organism), 
        keys    = deGenes$genes, 
        columns = dbi_map[toAttrs], 
        keytype = dbi_map[[fromAttr]]
      ))
      
      # Invert map to translate standard uppercase DBI columns back into your specific lowercase strings
      inv_map <- names(dbi_map); names(inv_map) <- dbi_map
      colnames(local_res) <- inv_map[colnames(local_res)]
      
      # Force columns into the exact layout sequence your code handles next
      bm_res <- local_res[, c(fromAttr, toAttrs), drop = FALSE]
    }# else
    
    # Handle empty or NA values safely
    bm_res[bm_res == "" | is.na(bm_res)] <- "N/A"
    
    print(head(bm_res)) 
    
    # ---- Mapping Status ----
    mapped_genes <- unique(bm_res[[fromAttr]])
   unmapped_genes <- setdiff(deGenes$genes, mapped_genes)
  
    cat("\n======= SIGNIFICANT DE GENES DATABASE ANNOTATION =======\n")
    cat(sprintf("-> Total input DE genes submitted for processing: %d\n", length(deGenes$genes)))
    cat(sprintf("-> Unique input DE genes verified in submission  : %d\n", length(unique(deGenes$genes))))
    cat(sprintf("-> Total database structural query rows returned : %d\n", nrow(bm_res)))
    cat(sprintf("-> DE genes successfully localized in database  : %d\n", length(mapped_genes)))
    cat(sprintf("-> DE genes totally absent from database record   : %d\n", length(unmapped_genes)))
    cat("=======================================================\n")
    
    cat(sprintf("Mapped DE genes (%d):\n", length(mapped_genes)))
    for (i in seq_along(mapped_genes)) {
      # Print the gene and add a comma unless it's the last gene in the row
      cat(sprintf("%-10s", mapped_genes[i]))
      if (i %% 10 == 0 || i == length(mapped_genes)) {
        cat("\n")  # Move to the next line after every 6 genes or at the end
      } else {
        cat(", ")
      }
    }#for
    cat(sprintf("\nUnmapped DE genes (%d):\n", length(unmapped_genes)))
    for (i in seq_along(unmapped_genes)) {
      # Print the gene and add a comma unless it's the last gene in the row
      cat(sprintf("%-10s", unmapped_genes[i]))
      if (i %% 10 == 0 || i == length(unmapped_genes)) {
        cat("\n")  # Move to the next line after every 6 genes or at the end
      } else {
        cat(", ")
      }
    }#for
    
    # ---- Investigate One-to-Many Splits BEFORE deduplication ----
    # Count how many rows each input gene has in the result
    gene_counts <- table(bm_res[[fromAttr]])
    multi_mapped <- gene_counts[gene_counts > 1]
    
    if(length(multi_mapped) > 0) {
      cat("\n[ALERT] The following genes mapped to MULTIPLE target attributes (causing row inflation):\n")
      print(multi_mapped)
    } else {
      cat("\n[INFO] Every mapped gene has exactly one target attribute row.\n")
    }
    
    
  } else {
    stop("[ERROR] Invalid 'frmType' provided.")
  }
  
  cat("\nSaving DEGenes before Deduplication to 'DEGenes_Duplicate.csv'...\n")
  write.table(bm_res,"DEGenes_Duplicate.csv", row.names = FALSE,col.names = TRUE,sep = "\t")
  
  
  cat("Performing cross-reference unique structural deduplication ---\n")
  cat("Removing repeated Entrez Gene IDs; retaining first mapped entry per gene...\n")
  cat("Original rows:", nrow(bm_res), "\n")
  
  cat("\nGenes causing 1-to-many splits:\n")
  print(bm_res[bm_res[[fromAttr]] %in% names(which(table(bm_res[[fromAttr]]) > 1)), ])
  
  # Remove duplicates based on the ENTREZID column
    #duplicate_ids <- bm_res[!duplicated(bm_res$entrezgene_id), ]
  
  # 3. CORRECT DEDUPLICATION: Deduplicate by your input gene column
   deduplicated_res <- bm_res[!duplicated(bm_res[[fromAttr]]), ]
   
   cat("Rows after input gene deduplication:", nrow(deduplicated_res), "\n")
  print(deduplicated_res)
  
  #Creating a new dataframe along with all the columns as per mapped genes
  # Subset deGenes based on matching  frmType IDs in duplicate_ids
  res_df2 = deGenes[deGenes$genes %in% deduplicated_res[[frmType]], ]
  
  print(sprintf("Total mapped Genes: %d",nrow(res_df2)))
  
  # Merge res_df2 with duplicate_ids based on the frmType column (e.g., ENSEMBL)
  res_df2 <- merge(res_df2, deduplicated_res, by.x = "genes", by.y = frmType, all.x = TRUE)
 
   cat("Removing rows with missing (NA), blank, or 'N/A' Entrez Gene IDs...")
  res_df2 <- res_df2[!is.na(res_df2$entrezgene_id) & 
                       res_df2$entrezgene_id != "N/A" & 
                       res_df2$entrezgene_id != "", ]
  cat(sprintf("Total verified Entrez-mapped Genes retained for plotting: %d\n", nrow(res_df2)))
  
  cat("\nSaving DEGenes Details to 'DEGenes_Details.csv'...\n")
  write.table(res_df2,"DEGenes_Details.csv", row.names = FALSE,col.names = TRUE,sep = "\t")
  
  if(analysis_type == "deSeq2")
  {
    cat("\nExtracting log2FoldChange values (DESeq2 standard)...\n")
    de_vector <- res_df2$log2FoldChange
  }else if (analysis_type == "edgeR"){
    cat("\nExtracting logFC values (edgeR standard)...\n")
    de_vector <- res_df2$logFC
  }
  
  #NAMING GENELIST AS PER ENTERZID ALONG WITH LOG2FOLD VALUES
    names(de_vector) <- res_df2$entrezgene_id
  
  # omit any NA values 
  initial_len <- length(de_vector)
  
  # Filter out real NA values AND literal "N/A" strings
  de_vector <- de_vector[!is.na(names(de_vector)) & names(de_vector) != "N/A" & names(de_vector) != ""]
  
  na_dropped <- initial_len - length(de_vector)
  cat(sprintf("Dropped %d genes due to missing (N/A) Entrez IDs.\n", na_dropped))
  
  # ============================================================================
  # FINAL DE GENES GSEA SELECTION SUMMARY
  # ============================================================================
  cat("\n============================================================\n")
  cat("FINAL DE GENES GSEA SELECTION SUMMARY\n")
  cat(sprintf("-> Unique mapped genes containing expression logs: %d\n", initial_len))
  cat(sprintf("-> Invalid annotations dropped (missing or 'N/A' Entrez IDs): %d\n", na_dropped))
  cat(sprintf("-> Total processed DE genes passing to clusterProfiler GSEA : %d\n", length(de_vector)))
  cat("============================================================\n\n")
  
  
  # sort the list in decreasing order (required for clusterProfiler)
  #Gene Names along with log2foldchange (EntrezID)
  cat("Sorting gene list in decreasing order for clusterProfiler...\n")
  de_geneList = sort(de_vector, decreasing = TRUE)
  
  cat("Final DE geneList vector length:", length(de_geneList), "\n")
  cat("Structure of the DE geneList vector:\n")
  print(str(de_geneList))
  
  #Gene Names (Enterz ID)
  de_genes_only<- names(de_geneList) 
  
  if(length(de_genes_only) == 0) {
    stop("[CRITICAL FAILURE] Zero functional Entrez IDs remaining. Analysis cannot execute.")
  }
  
  cat(sprintf("Final functional Entrez ID array ready for pathway analysis: %d\n", length(de_genes_only)))
  cat(paste(de_genes_only, collapse = ", "), "\n")

  # PIPELINE AUTODETECTION: HANDLING TIES DYNAMICALLY
  # ----------------------------------------------------------------------------
  # 1. Check if there are duplicate ranking statistics in the incoming dataset
  total_elements <- length(geneList)
  unique_elements <- length(unique(geneList))
  
  if (unique_elements < total_elements) {
    tie_percentage <- ((total_elements - unique_elements) / total_elements) * 100
    cat(sprintf("Duplicate values detected (%0.2f%% of list).\nApplying microscopic tie-breaker amount = 1e-7...\n", tie_percentage))
    
    # Apply deterministic jitter only because ties exist
    set.seed(42) # Safe reproducible seed for pipeline environments
    gene_names_backup <- names(geneList) # Backup names
    geneList <- jitter(geneList, amount = 1e-7)
    names(geneList) <- gene_names_backup # Reassign names
    cat("Re-sorting background gene list after tie-breaking adjustments...\n")
    geneList <- sort(geneList, decreasing = TRUE)
  } else {
    cat("[PIPELINE INFO] Perfect rank sequence verified. No ties found, skipping jitter.\n")
  }
  
  
  
  cat(sprintf("Final background geneList prepared successfully. Length: %d\n\n", length(geneList)))
  
  #########################################################  
  # 1) GO classification: GO profile at a specific level
  ########################################################
  
  cat("\n1: Starting  groupGO Functional Classification Analysis\n")
  # Define a mapping between ontology types and levels
  ontology_levels <- list("BP" = 1, "MF" = 2, "CC" = 3)
  
  # Function for groupGO based on user's choice
  if (groupSub == "ALL") {
    # If user chooses "ALL", run groupGO for BP, MF, and CC
    ontologies <- c("BP", "MF", "CC")
    groupGO_results <- list()
    barplots_list <- list()
    
    # Loop over each ontology and run groupGO
    for (ont in ontologies) {
      groupGO_results[[ont]] <- groupGO(
        gene     = de_genes_only,     # Your vector of gene IDs
        OrgDb    = organism,     # OrgDb object (e.g., org.Mm.eg.db for mouse)
        keyType  = "ENTREZID",   # Key type of the input gene list
        ont      = ont,          # Ontology type (BP, MF, CC)
        level    = ontology_levels[[ont]]  # Level of GO hierarchy
      )
    }
    # Visualize or work with the results for all ontologies
    for (ont in ontologies) {
      print(paste("Results for:", ont))
      print(groupGO_results[[ont]])
      groupGO_results_df <- as.data.frame(groupGO_results[[ont]]@result)
      print(groupGO_results_df)
      cat("\nSaving groupGO results to ",paste0("groupGO_results_", ont, ".csv"),"...\n")
      write.csv(groupGO_results_df, paste0("groupGO_results_", ont, ".csv"), row.names = FALSE)
      sink.reset()
      
    
    CairoPNG(file = paste("GroupGO_BarPlots.png"), width = 15, height = 7, units = "in", res = 300)
    
    
    p_temp <- barplot(groupGO_results[[ont]], drop = TRUE,  showCategory = min(nrow(groupGO_results_df), 20))
    p_temp$data <- p_temp$data[order(p_temp$data$Count, decreasing = FALSE), ]
    p_temp$data$Description <- factor(p_temp$data$Description, levels = p_temp$data$Description)
    
    barplots_list[[ont]] <- p_temp +
      # Main Title pointing to your current subontology loop variable (ont)
      ggtitle(sprintf("GO Classification: %s", ont)) +
      # Explicit X/Y axis titles matching your target logic
      labs(x = "Number of Genes (Count)", y = "Gene Ontology (GO) Terms") +
      # Fine-tuned theme parameters for structural neatness
      theme_bw() + # Highly recommended base layer for clean pipeline figures
      theme(
        legend.position = "none",
        plot.title = element_text(face = "bold", size = 12, hjust = 0.5, margin = margin(b = 15)),
        # X and Y Axis Titles formatting
        axis.title.x = element_text(face = "bold", size = 11, margin = margin(t = 12)),
        axis.title.y = element_text(face = "bold", size = 11, margin = margin(r = 12)),
        
        # Axis text marks (ensures long GO terms on Y stay perfectly visible)
        axis.text.y = element_text(size = 10),
        axis.text.x = element_text(size = 10)
      )      
    }#for
    
    # Arrange both plots in a 1x2 grid
    grid.arrange(barplots_list[["CC"]],barplots_list[["MF"]],barplots_list[["BP"]], ncol = 3)
    
    invisible(dev.off())
    
    
    
  } else {
    cat("Selected Ontology Subclass:", groupSub, "\n")
    cat("GO Hierarchy Level:", ontology_levels[[groupSub]], "\n")
    cat("Total Input Genes:", length(de_genes_only), "\n\n")
    # If user selects a single ontology, analyze it directly
    groupGO_results <- groupGO(
      gene     = de_genes_only,    # Your vector of gene IDs
      OrgDb    = organism,    # OrgDb object (e.g., org.Mm.eg.db for mouse)
      keyType  = "ENTREZID",  # Key type of the input gene list
      ont      = groupSub,    # Ontology type chosen by the user (BP, MF, CC)
      level    = ontology_levels[[groupSub]]  # Level of GO hierarchy
    )
    cat("Summary of groupGO Results\n")
    print(groupGO_results)
    
    groupGO_results_df <- as.data.frame(groupGO_results@result)
    cat("\nTotal GO Categories Identified:",
        nrow(groupGO_results_df), "\n\n")
    cat("Top GO Classification Results (Top 10):\n")
    print(head(groupGO_results_df, 10))
    
    write.csv(groupGO_results_df, paste0("groupGO_results_", groupSub, ".csv"), row.names = FALSE)
    sink.reset()
    cat("\nResults saved to:", paste0("groupGO_results_", groupSub, ".csv"), "\n")
    
      cat("Generating GroupGO_BarPlot.png\n")
      CairoPNG(file = paste("GroupGO_BarPlot.png"), width = 11, height = 7, units = "in", res = 300)
      # Set margins to ensure the title is displayed
      par(mar = c(10, 4, 4, 2) + 0.1)  # Bottom, Left, Top, Right
      p <- barplot(groupGO_results, drop = TRUE, showCategory = min(nrow(groupGO_results_df), 20))
      p$data <- p$data[order(p$data$Count, decreasing = FALSE), ]
      p$data$Description <- factor(p$data$Description, levels = p$data$Description)
      p <- p +
        # Set the main title and explicit X/Y labels
        ggtitle(sprintf("Gene Ontology Functional Classification Mapping - Subclass: %s", groupSub)) +
        labs(x = "Number of Genes (Count)", y = "Gene Ontology (GO) Terms") +

        # Fine-tune the text sizes to match your huge image dimensions
        theme(
          # Main Title formatting
          plot.title = element_text(face = "bold", size = 16, hjust = 0.5, margin = margin(b = 20)),

          # X and Y Axis titles (the labels themselves)
          axis.title.x = element_text(face = "bold", size = 12, margin = margin(t = 15)),
          axis.title.y = element_text(face = "bold", size = 12, margin = margin(r = 15)),

          # Axis tick labels (the actual GO terms and number marks)
          axis.text.y = element_text(size = 11),
          axis.text.x = element_text(size = 11)
        )

      print(p)
      invisible(dev.off())
      
      
  }
  
  
  ##############################################################################  
  # 2) enrichGO: For GO over-representation analysis
  ##############################################################################
  print("2: STARTING GO OVER-REPRESENTATION ANALYSIS (enrichGO)")
  
  ego <- enrichGO(gene= de_genes_only, #a vector of enterz gene id
                  #universe      = geneUniverse,
                  OrgDb         = organism,
                  ont           = enrichSub,#enrichSubenrichSub
                  pAdjustMethod = enrichP,#enrichP
                  pvalueCutoff  = enrichPcutoff,#enrichPcutoff
                  qvalueCutoff  = enrichQcutoff,#enrichQcutoff
                  readable      = TRUE)
  if (nrow(as.data.frame(ego)) != 0){
     cat("enrichGO Analysis complete! Significant terms identified: ",nrow(as.data.frame(ego))," \n")
    print("enrichGO over-representation analysis summary:")
    print(head(ego,n=dim(ego)))
    # Convert enrichGO results to a data frame
    ego_df <- as.data.frame(ego)
    
    cat("\nSaving enrichGO results to ",paste0("enrichGO_results_", enrichSub, ".csv"),"...\n")
    write.csv(ego_df, paste0("enrichGO_results_", enrichSub, ".csv"), row.names = FALSE)
    sink.reset()
    
    ###################################################################################
    #enrichGO: Plots
    cat("\nGenerating enrichGO_BarPlot.png (Combined View)...\n")
    CairoPNG(file ="enrichGO_BarPlot.png",width = 12, height = 7, units = "in", res = 300) 
    # First plot: Top 10 GO Enrichment Terms (using enrichplot::barplot)
    p1 <- barplot(ego, drop = TRUE, showCategory = min(nrow(ego_df), 20)) +
      ggtitle("Enriched GO Categories\n(Ranked by Target Gene Count)") +
      labs(x = "Gene Count", y = "Gene Ontology (GO) Terms") +
      theme(
        plot.title   = element_text(face = "bold", size = 12, hjust = 0.5, margin = margin(b = 15)),
        axis.title.x = element_text(face = "bold", size = 10),
        axis.title.y = element_text(face = "bold", size = 10),
        axis.text.y  = element_text(size = 9),
        axis.text.x  = element_text(size = 10),
        plot.margin  = unit(c(1, 1, 1, 1), "cm")
      )
    
    # Second plot: Bar plot of qscore (-log10(p.adjust)) using ggplot2
    p2 <- mutate(ego, qscore = -log(p.adjust, base=10)) %>% 
      barplot(x = "qscore", showCategory = min(nrow(ego_df), 20)) +
      ggtitle("Pathway Distribution Statistical Significance\nCalculated via -log10(p.adjust)") +
      labs(x = "-log10(p.adjust Value)", y = "Gene Ontology (GO) Terms") +
      theme(
        plot.title   = element_text(face = "bold", size = 12, hjust = 0.5, margin = margin(b = 15)),
        axis.title.x = element_text(face = "bold", size = 10),
        axis.title.y  = element_text(face = "bold", size = 10),
        axis.text.y  = element_text(size = 9),
        axis.text.x  = element_text(size = 10),
        plot.margin  = unit(c(1, 1, 1, 1), "cm")
      )
    
    # Arrange both plots in a 1x2 grid
    grid.arrange(p1, p2, ncol = 2, 
                 top = grid::textGrob(sprintf("GO Over-Representation Profiling Matrix (Ontology System: %s)", enrichSub), 
                                      gp = grid::gpar(fontsize = 14, fontface = "bold")))
    invisible(dev.off())
    
    cat("\nGenerating enrichGO_DotPlot.png...\n")
    CairoPNG(file ="enrichGO_DotPlot.png",width = 11, height = 7, units = "in", res = 300)
    print(dotplot(ego,showCategory = min(nrow(ego_df), 20))+ 
            ggtitle("GO Over-Representation Analysis: Significantly Enriched Biological Terms") +
            labs(x = "Gene Ratio (Enriched/Total Mapping Array)", y = "Gene Ontology (GO) Terms") +
            theme(
              plot.title   = element_text(face = "bold", size = 14, hjust = 0.5, margin = margin(b = 15)),
              axis.title.x = element_text(face = "bold", size = 12),
              axis.title.y = element_text(face = "bold", size = 12),
              axis.text.y  = element_text(size = 11),
              axis.text.x  = element_text(size = 12),
              plot.margin  = unit(c(1, 1, 1, 1), "cm")
            ))
    invisible(dev.off())
    
    cat("Generating enrichGO_UpsetPlot.png...\n")
    CairoPNG(file ="enrichGO_UpsetPlot.png",width = 11, height = 7, units = "in", res = 300)
    # Generate the upset plot
    print(enrichplot::upsetplot(ego, n = min(nrow(ego_df), 10))+ 
            ggtitle("GO Over-Representation UpSet Intersection Matrix:\n Gene Membership Overlaps")+
            labs(y = "Intersection Size (Gene Count)", x = "GO Term Composition Intersections") +
            theme(
              plot.title  = element_text(face = "bold", size = 12, hjust = 0.5, margin = margin(b = 20)),
              # INCREASE GO TERM NAMES: Scale up the text size for the row names
              axis.text.y  = element_text(size = 9),
              # Increase numeric text sizes on axes
              axis.text.x  = element_text(size = 9),
              axis.title.x = element_text(size = 10, face = "bold"),
              axis.title.y = element_text(size = 10, face = "bold"),
              plot.margin = unit(c(1, 1, 1, 1), "cm")
            ))
    
 
    invisible(dev.off())
    
    x2 <- enrichplot::pairwise_termsim(ego) 
    
    cat("Generating enrichGO_EMAPlot.png...\n")
    CairoPNG(file ="enrichGO_EMAPlot.png",width = 11, height = 7, units = "in", res = 300)
    # INCREASE TEXT: Using the 'cex.params' argument to scale up node labels directly inside the function
    print(enrichplot::emapplot(x2, showCategory = min(nrow(ego_df), 20),
                               node_label_size = 3.0,
                               label_format    = 25
                               
                               )+ 
            ggtitle("GO Semantic Functional Similarity Network of Enriched Biological Pathways")+
      theme(
        plot.title  = element_text(face = "bold", size = 14, hjust = 0.5, margin = margin(b = 20)),
        plot.margin = unit(c(1, 1, 1, 1), "cm"),
        legend.text = element_text(size = 10),
        legend.title = element_text(size = 12, face = "bold")
      ))
    
    invisible(dev.off())
    
    cat("Generating enrichGO_CnetPlot.png...\n")
    CairoPNG(file ="enrichGO_CnetPlot.png",width = 11, height = 7, units = "in", res = 300)
    print(cnetplot(ego,
                    categorySizeBy= ~pvalue, 
                   foldChange = de_vector  ,
                   showCategory = min(nrow(ego), 10) 
                   
                   )+  #de_genes_only
      ggtitle("GO Linkage Category Network (Cnetplot): Gene Targets to Enriched Annotation Nodes")+
      theme(plot.title  = element_text(face = "bold", size = 14, hjust = 0.5, margin = margin(b = 20)),
            plot.margin = unit(c(1, 2, 1, 1), "cm"),  # Adjust the top, right, bottom, and left margins
            legend.text = element_text(size = 10),
            legend.title = element_text(size = 12, face = "bold")
             ))
    
    invisible(dev.off())
    
  } else {
    cat("[NOTICE] No enriched pathways found above the specified cutoffs in enrichGO analysis.\n")
  }
  ###############################################################################
  # 3) gseGO: GO Gene Set Enrichment Analysis
  ###############################################################################

  cat("============================================================\n\n")

  # print("3: STARTING GO GENE SET ENRICHMENT ANALYSIS (gseGO)")
  # gse <- gseGO(geneList=geneList,
  #              ont =gseSub, # BP,MF,CC,ALL
  #              pvalueCutoff = gsePcutoff,
  #              verbose = TRUE, 
  #              OrgDb = organism,
  #              pAdjustMethod = gseP,
  #              by=gseBy)
  
  go_mappings <- AnnotationDbi::select(get(organism), 
                                       keys = names(geneList), 
                                       columns = c("GO", "ONTOLOGY"), 
                                       keytype = "ENTREZID")
  go_mappings <- go_mappings[!is.na(go_mappings$GO) & !is.na(go_mappings$ONTOLOGY), ]
  if (trimws(gseSub) != "ALL") {
    go_mappings$ONTOLOGY <- trimws(go_mappings$ONTOLOGY)
    go_mappings <- go_mappings[go_mappings$ONTOLOGY == trimws(gseSub), ]
  } else {
    cat("[INFO] 'ALL' subcategory selected. Retaining BP, MF, and CC categories together.\n")
  }
  if (nrow(go_mappings) == 0) {
    stop(sprintf("ERROR: No mappings found for ontology subcategory '%s'.", gseSub))
  }
  go_term2gene <- data.frame(
    term = go_mappings$GO,
    gene = go_mappings$ENTREZID
  )
  go_term2gene <- unique(go_term2gene)
  
  cat(sprintf("Successfully mapped %d unique GO-to-Gene interactions for GSEA.\n", nrow(go_term2gene)))
  
  gse <- GSEA(
    geneList      = geneList,
    TERM2GENE     = go_term2gene,
    pvalueCutoff  = gsePcutoff,
    pAdjustMethod = gseP,
    verbose       = TRUE,
    by            = gseBy
  )
  
  gse_df <- as.data.frame(gse)
  if (nrow(gse_df) > 0) {
    cat("\nTranslating GO IDs to human-readable term descriptions...\n")
    go_descriptions <- AnnotationDbi::select(GO.db, 
                                             keys = gse@result$ID, 
                                             columns = "TERM", 
                                             keytype = "GOID")
    
    matched_terms <- go_descriptions$TERM[match(gse@result$ID, go_descriptions$GOID)]
    gse@result$Description <- matched_terms
    gse_df$Description <- matched_terms
  }
  cat("Converting internal gene IDs to readable symbols for CnetPlot...\n")
  gse <- DOSE::setReadable(gse, 
                                            OrgDb = get(organism), 
                                            keyType = "ENTREZID")
  
  
  if (nrow(as.data.frame(gse)) != 0){

    cat("Number of gseGO results: ",nrow(as.data.frame(gse)),"\n")
    print("gseGO Gene Set Enrichment Analysis summary:")
    print(head(gse,n=dim(gse)))
    gse_df <- as.data.frame(gse)

    cat("\nSaving gseGO results to ",paste0("gseGO_Results(", gseSub, ").csv"),"...\n")
    write.csv(gse_df, paste0("gseGO_Results(", gseSub, ").csv"), row.names = FALSE)
    sink.reset()



    ##############################################################################################################
    # gseGO: PLOTS
    cat("\nGenerating gseGO_DotPlot.png...")

    cat(sprintf("\n[PLOT INFO] Total pathways found: %d. Displaying top: %d terms in DotPlot.\n",
                nrow(as.data.frame(gse)), min(nrow(as.data.frame(gse)), 20)))
    CairoPNG(file ="gseGO_DotPlot.png", width = 11, height = 7, units = "in", res = 300)
    print( dotplot(gse, showCategory = min(nrow(as.data.frame(gse)), 20),
                   title = "Gene Set Enrichment Analysis: GO Categories",
                   split=".sign")+ facet_grid(.~.sign) +
      scale_y_discrete(labels = function(x) stringr::str_wrap(x, width = 60),
                       expand = expansion(add = c(1, 1))) +
        scale_size_continuous(range = c(2, 5)) +
      theme(plot.title   = element_text(face = "bold", size = 13, hjust = 0.5, margin = margin(b = 20)),
            axis.text.y  = element_text(size = 8.5, lineheight = 0.75, vjust = 0.5),
            # X-Axis and Legend layout text scaling
            axis.text.x  = element_text(size = 9),
            axis.title.x = element_text(size = 10),
            strip.text   = element_text(size = 11, face = "bold"), # Facet label headers (Activated/Suppressed)
            plot.margin = unit(c(0.5, 0.5, 0.5, 0.5), "cm")))
    invisible(dev.off())

    #Enrichment map: organizes enriched terms into a network with edges connecting overlapping gene sets.
    cat("\nGenerating gseGO_EMAPlot.png...")
    x2 <- enrichplot::pairwise_termsim(gse)
    CairoPNG(file ="gseGO_EMAPlot.png",width = 11, height = 7, units = "in", res = 300)
    print(enrichplot::emapplot(x2, min(nrow(as.data.frame(gse)), 20),
                               node_label_size = 3.0,
                               label_format    = 25

                               )+
      ggtitle("GO Term Enrichment Network: Gene Set Enrichment Analysis")+
      theme(plot.title   = element_text(face = "bold", size = 14, hjust = 0.5, margin = margin(b = 20)),
            plot.margin = unit(c(1, 2, 1, 1), "cm")))
    invisible(dev.off())
    #CATEGORY NET PLOT :depicts the linkages of genes and biological concepts

    # categorySize can be either 'pvalue' or 'geneNum'

    cat("\nGenerating gseGO_CnetPlot.png...")
    CairoPNG(file ="gseGO_CnetPlot.png",width = 11, height = 7, units = "in", res = 300)
    print(enrichplot::cnetplot(clusterProfiler::setReadable(gse,    OrgDb = organism,    keyType = "ENTREZID"),
                               categorySizeBy= ~pvalue,
                               foldChange = geneList,
                               showCategory = min(nrow(gse), 10) ,
                   node_label = "all"
                   )+
      ggtitle("Gene-Concept Network: GO Term Enrichment Analysis")+
      theme(plot.title   = element_text(face = "bold", size = 12, hjust = 0.5, margin = margin(b = 15)),

            # Clean legend integration panel settings
            legend.title = element_text(size = 8, face = "bold"),
            legend.text = element_text(size = 7),
            legend.key.size = unit(0.3, "cm"),
            legend.position = "right",

            plot.margin = unit(c(1, 1, 1, 1), "cm")  # Adjust the top, right, bottom, and left margins
      ))

    invisible(dev.off())

    cat("\nGenerating gseGO_RidgePlot.png...")
    CairoPNG(file ="gseGO_RidgePlot.png",width = 11, height = 7, units = "in", res = 300)
    #Ridge PLOT: Grouped by gene set, density plots are generated
    print(ridgeplot(gse, min(nrow(as.data.frame(gse)), 20)) + labs(x = "Enrichment Distribution")+
      scale_y_discrete(labels = function(x) stringr::str_wrap(x, width = 50),
                       expand = expansion(add = c(1, 1))) +
      ggtitle("GO Term Enrichment Distribution Across Gene Sets")+
      theme(plot.title   = element_text(face = "bold", size = 14, hjust = 0.5, margin = margin(b = 20)),
            axis.text.y  = element_text(size = 10, lineheight = 0.75, vjust = 0.5),
            plot.margin = unit(c(1, 1, 1, 1), "cm")
      ))

    invisible(dev.off())

    cat("\nPreparing individual GSEA pathway charts...\n")

        plotList <- list()
        num_plots <- min(6, nrow(as.data.frame(gse)))

        for(i in 1:num_plots){

          # 1. Enclose gseaplot in print() so it executes and draws properly
          p_base <- print(gseaplot(x = gse, by = "all", geneSetID = i))

          # 2. Build your clean title string
          title_str <- stringr::str_wrap(paste("GSEA: GO Term - ", gse$Description[i]), width = 55)

          # 3. Wrap p_base inside wrap_elements()
          # This turns the uncooperative gseaplot into a standard, fully-behaved layout block
          plotList[[i]] <- patchwork::wrap_elements(panel = p_base) +
            labs(title = title_str) +
            theme(
              # Style and push the title down from the top edge
              plot.title = element_text(
                face = "bold",
                size = 11,
                hjust = 0.5,
                margin = margin(t = 5, b = -3, unit = "pt")
              ),
              # Add clean page margins on the left, right, and bottom edges
              plot.margin = margin(t = 0, r = 5, b = 2, l = 5, unit = "pt")
            )

        } #endfor

        cat("\nGenerating gseGO_GseaPlot.png...\n")

        if (length(plotList) > 0) {
          dynamic_cols <- if (length(plotList) == 1) 1 else 2
          dynamic_rows <- ceiling(length(plotList) / dynamic_cols)

          # 4. Use patchwork's wrap_plots instead of grid.arrange
          # This safely preserves the titles and layout configurations
          final_grid <- patchwork::wrap_plots(plotList, ncol = dynamic_cols, nrow = dynamic_rows)+
            patchwork::plot_layout(guides = "collect") &
            theme(plot.margin = margin(2, 2, 2, 2, unit = "pt")) # Forces tight nesting

          inch_width <- if (dynamic_cols == 1) 6.5 else 13
          inch_height <- dynamic_rows * 4.2  # ~5.3 inches per row of plots

          # Set the canvas device
          CairoPNG(file = "gseGO_GseaPlot.png", width = inch_width, height = inch_height, units = "in", res = 300)

          # Print the final patchwork object directly
          print(final_grid)

          invisible(dev.off())
        }
  #############################TESTTING ABOVE
  }#endif
  else {
    cat("[NOTICE] No enriched groups found above the specified cutoffs in gseGO analysis.\n")
  }

  
  
   # ###############################################################################
  # 4) enrichKEGG: KEGG pathway over-representation analysis
  print("4: STARTING KEGG OVER-REPRESENTATION ANALYSIS (enrichKEGG)")
  ekegg <- NULL
  tryCatch({
  ekegg <- enrichKEGG(gene = de_genes_only,
                      organism = org,
                      pvalueCutoff=enrichPcutoff1,
                      pAdjustMethod=enrichP1) #,use_internal_data = TRUE  removed as it triggers kegg.db
  }, error = function(e) {
   
    cat("[NETWORK EXCEPTION] KEGG Server dropped the connection or is offline!\n")
    cat("Error details:", conditionMessage(e), "\n")
    cat("Bypassing server crash. Tripping safety logic paths...\n")
    
    ekegg <<- NULL # Secure container safely as empty across parent environments
  })
  
  if (!is.null(ekegg) && nrow(as.data.frame(ekegg)) != 0){
    
    cat(sprintf("Success! Identified %d significant KEGG pathways.\n", nrow(as.data.frame(ekegg))))
    
     print("KEGG pathway over-representation analysis summary:")
    print(head(ekegg))
    
    ekegg_df<-as.data.frame(ekegg)
    cat("\nSaving KEGG enrichment results to 'enrichKEGG_Results.csv'...\n")
    write.csv(ekegg_df, "enrichKEGG_Results.csv", row.names = FALSE)
    sink.reset()
    
    cat("\nGenerating enrichKEGG_DotPlot.png\n")
    # PLOTS:
    CairoPNG(file ="enrichKEGG_DotPlot.png",width = 11, height = 7, units = "in", res = 300)
    print( dotplot(ekegg, showCategory = min(nrow(ekegg_df), 20), 
                   title = "KEGG Pathway Over-Representation Analysis: Significantly Enriched Functions") + 
      scale_y_discrete(labels = function(x) stringr::str_wrap(x, width = 45)) +
      theme(plot.title   = element_text(face = "bold", size = 14, hjust = 0.5, margin = margin(b = 10)),
            plot.margin = unit(c(1, 2, 1, 1), "cm"),
            legend.text = element_text(size = 12),
            legend.title = element_text(size = 14, face = "bold")
            
            ))
    invisible(dev.off())
    
    cat("\nGenerating enrichKEGG_BarPlot.png\n")
    CairoPNG(file ="enrichKEGG_BarPlot.png",width = 11, height = 7, units = "in", res = 300)
    
    print(barplot(ekegg, drop = TRUE, showCategory = min(nrow(ekegg_df), 20)) +
            ggtitle("Top Over-Represented KEGG Pathways (Ranked by Transferred Gene Count)")+
            theme(plot.title   = element_text(face = "bold", size = 14, hjust = 0.5, margin = margin(b = 10)),
                  plot.margin = unit(c(1, 1, 1, 1), "cm")))
    invisible(dev.off())
    
    cat("\nCalculating pairwise similarity between enriched terms...\n")
    x2 <- enrichplot::pairwise_termsim(ekegg) 
    print(x2)
    cat("\nDimensions of termsim matrix: ", paste(dim(x2@termsim), collapse = " x "), "\n")
    
    off_diag_sim <- x2@termsim[upper.tri(x2@termsim)]
    unique_vals <- unique(off_diag_sim)
    cat("Unique off-diagonal values in termsim:", unique_vals, "\n")
   
     # Safer: check it's a real matrix and has off-diagonal similarities
    if (!is.null(x2@termsim) &&
        is.matrix(x2@termsim) &&
        nrow(x2@termsim) > 1 &&
        sum(x2@termsim[upper.tri(x2@termsim)]) > 0 &&
        length(unique(x2@termsim[upper.tri(x2@termsim)])) > 1) {
      
    cat("\nGenerating enrichKEGG_EMAPlot.png\n")
    CairoPNG(file ="enrichKEGG_EMAPlot.png",width = 11, height = 7, units = "in", res = 300)
    
    print(enrichplot::emapplot(x2, showCategory = min(nrow(ekegg_df), 20),
                               node_label_size = 3.0,
                               label_format    = 25
                               )+ 
            ggtitle("KEGG Pathway Over-Representation Similarity Map: Functional Topology Networks")+
            theme(plot.title   = element_text(face = "bold", size = 14, hjust = 0.5, margin = margin(b = 10)),
                  plot.margin = unit(c(0.5, 1, 0.5, 1), "cm")))
    invisible(dev.off()) 
    } else {
      cat("Not enough pathway similarity for emapplot. Skipping plot.\n")
    }
    
    
    cat("\nGenerating enrichKEGG_UpsetPlot.png\n")
    # Clean, wide canvas layout to handle the text naturally
    CairoPNG(file = "enrichKEGG_UpsetPlot.png", width = 13, height = 7, units = "in", res = 300)
    
    # Generate the base upset plot from your standard KEGG object (max 10 terms)
    p_upset <- enrichplot::upsetplot(ekegg, n = min(nrow(ekegg_df), 10))
    
    # Safely shrink the inner label sizes with the downstream operator
    p_upset_tuned <- p_upset & 
      theme(
        axis.text.y = element_text(size = 9.5),
        axis.text.x = element_text(size = 9.5)
      )
    
    # Add the main title on the outer structural canvas
    final_upset <- ggplotify::as.ggplot(p_upset_tuned) + 
      labs(title = "KEGG Pathways: Set Intersections of Enriched Genes") +
      theme(
        plot.title = element_text(
          face = "bold", 
          size = 14, 
          hjust = 0.5, 
          margin = margin(t = 10, b = 5)
        ),
        plot.margin = margin(t = 5, r = 10, b = 5, l = 10, unit = "pt")
      )
    
    print(final_upset)
    invisible(dev.off())
    cat("\nGenerating enrichKEGG_CNETPlot.png\n")
    # Networks require extra breathing space, so we expand to 13x9 inches
    CairoPNG(file = "enrichKEGG_CNETPlot.png", width = 13, height = 9, units = "in", res = 300)
    
    print(
      enrichplot::cnetplot(clusterProfiler::setReadable(ekegg,    OrgDb = organism,    keyType = "ENTREZID"), 
                           showCategory = min(nrow(ekegg_df), 10), 
                           foldChange = geneList
                           ) +           
        ggtitle("KEGG Pathway Frameworks: Category-Gene Networks") +
        theme(
          plot.title  = element_text(face = "bold", size = 14, hjust = 0.5, margin = margin(b = 10)),
          plot.margin = unit(c(1, 1, 1, 1), "cm")
        )
    )
    
    invisible(dev.off())
    
    #  pathwayID<-ekegg@result$ID[1]
    
    #Pathway Diagram
    # browseKEGG(ekegg,pathwayID )
  }#endif
  else {
   
    cat("[WARNING] enrichKEGG returned no significant pathways.\n")
    cat("Possible causes include:\n")
    cat(" - Strict statistical thresholds\n")
    cat(" - Insufficient Entrez ID mapping\n")
    cat(" - KEGG server/network instability\n")  
  }#else
  
  ###############################################################################
  # 5) gseKEGG: KEGG pathway gene set enrichment analysis
  print("5: STARTING KEGG PATHWAY GENE SET ENRICHMENT ANALYSIS (gseKEGG)")
    
  tryCatch({
    
    gsekegg <- gseKEGG(geneList     = geneList,
                       organism     = org,#keggOrg,
                       pvalueCutoff = gsePcutoff1,
                       pAdjustMethod = gseP1,
                       keyType = "ncbi-geneid"
    )
    
    if (!is.null(gsekegg) && nrow(as.data.frame(gsekegg)) != 0){
      cat(sprintf("Success! gseKEGG calculated %d enriched tracks.\n", nrow(as.data.frame(gsekegg))))
      # View the top results and p-values
      head(gsekegg@result[, c("ID", "Description", "pvalue", "p.adjust")])
      
      print("KEGG pathway gene set enrichment analysis")
      head(gsekegg)
      
      gsekegg_df<-as.data.frame(gsekegg)
      print(gsekegg_df)
      write.csv(gsekegg_df, "gseKEGG_Results.csv", row.names = FALSE)
      sink.reset()
      
      # gseKEGG: PLOTS
      cat("\nGenerating gseKEGG_DotPlot.png\n")
      CairoPNG(file ="gseKEGG_DotPlot.png",width = 11, height = 7, units = "in", res = 300)
      print(dotplot(gsekegg, showCategory = min(nrow(gsekegg_df), 20), 
                    title = "KEGG Pathway Gene Set Enrichment Analysis (GSEA): Regulation Profiles" , split=".sign") + 
              facet_grid(.~.sign) + 
              scale_y_discrete(labels = function(x) stringr::str_wrap(x, width = 50),
                               expand = expansion(add = c(1, 1))) +
              theme(plot.title   = element_text(face = "bold", size = 16, hjust = 0.5, margin = margin(b = 20)),
                    axis.text.y  = element_text(size = 10, lineheight = 0.75, vjust = 0.5),
                    axis.text.x  = element_text(size = 11),
                    axis.title.x = element_text(size = 13, face = "bold"),
                    strip.text   = element_text(size = 13, face = "bold"), # Facet label headers (Activated/Suppressed)
                    plot.margin = unit(c(0.5, 0.5, 0.5, 0.5), "cm"))
            )
      invisible(dev.off())
      # #Enrichment map organizes enriched terms into a network with edges connecting overlapping gene sets. 
      
      x2 <- enrichplot::pairwise_termsim(gsekegg) 
      cat("\nGenerating gseKEGG_EMAPPlot.png\n")
      CairoPNG(file ="gseKEGG_EMAPPlot.png",width = 11, height = 7, units = "in", res = 300)
      print(enrichplot::emapplot(x2, showCategory = min(nrow(gsekegg_df), 20))+ 
              ggtitle("KEGG GSEA Semantic Similarity Map: Enriched Pathway Functional Connections")+
        theme(plot.title   = element_text(face = "bold", size = 14, hjust = 0.5, margin = margin(b = 10)),
              plot.margin = unit(c(1, 1, 1, 1), "cm")))
      invisible(dev.off())
      
      # #CATEGORY NET PLOT :depicts the linkages of genes and biological concepts
      # # categorySize can be either 'pvalue' or 'geneNum'
      cat("\nGenerating gseKEGG_CnetPlot.png\n")
      CairoPNG(file ="gseKEGG_CnetPlot.png",width = 11, height = 7, units = "in", res = 300)
      print(cnetplot(clusterProfiler::setReadable(gsekegg,    OrgDb = organism,    keyType = "ENTREZID"), 
                     categorySizeBy= ~pvalue,  foldChange = geneList,
                     showCategory = min(nrow(gsekegg), 10) 
                     )+ 
        ggtitle("KEGG Linkage Network Graph (Cnetplot): Core Expression Links to Enriched Pathways")+
        theme(plot.title   = element_text(face = "bold", size = 14, hjust = 0.5, margin = margin(b = 10)),
              plot.margin = unit(c(1, 1, 1, 1), "cm")  # Adjust the top, right, bottom, and left margins
        ))
      
      invisible(dev.off())
      cat("\nGenerating gseKEGG_RidgePlot.png\n")
      CairoPNG(file ="gseKEGG_RidgePlot.png",width = 11, height = 7, units = "in", res = 300)
      #Ridge PLOT: Grouped by gene set, density plots are generated
      print(ridgeplot(gsekegg, showCategory = min(nrow(gsekegg_df), 20)) + 
              labs(x = "Core Enrichment Score Density Profile Distributions") +
              ggtitle("Expression Shift Density Scales Across Significantly Enriched KEGG Pathways") +
              theme(plot.title   = element_text(face = "bold", size = 14, hjust = 0.5, margin = margin(b = 10)),
              plot.margin = unit(c(1, 1, 1, 1), "cm") ,
              axis.text.y = element_text(size = 8)
        ))
      
      invisible(dev.off())
      
      
      cat("\nGenerating gseKEGG_GseaPlot.png\n")
      CairoPNG(file ="gseKEGG_GseaPlot.png",width = 12, height = 8, units = "in", res = 300)
      
      plotList<-list()
      for(i in 1:min(10, length(gsekegg$Description))){
        plotList[[i]] <- print(gseaplot(gsekegg, by = "all", title = gsekegg$Description[i], geneSetID = i))+
          ggtitle(paste("GSEA: KEGG Pathway - " , gsekegg$Description[i]))+
          theme(plot.title = element_text(size = 9, face = "bold",hjust = 0.5),
                axis.title.x = element_text(size = 8),
                axis.text.y = element_text(size = 7),
                axis.text.x  = element_text(size = 7),
                axis.title.y = element_text(size = 8),
                plot.margin = unit(c(0.5, 0.5, 0.5, 0.5), "cm")  
          )
      }
      
      # Dynamically arrange the plots (handles less than 10 elements)
      if (length(plotList) > 0) {
        dynamic_cols <- if (length(plotList) == 1) 1 else 2
        dynamic_rows <- ceiling(length(plotList) / dynamic_cols)
        do.call(grid.arrange, c(plotList, nrow = dynamic_rows, ncol = dynamic_cols))
      }
      invisible(dev.off())
      
      
    }#endif
    else {
      cat("[WARNING] No significant gseKEGG pathways detected under current filtering thresholds.\n")
    }
  }, error = function(e) {
    cat("[RUNTIME ERROR] KEGG GSEA structural calculation dropped out:", conditionMessage(e), "\n")
  
  })
  
  
  ###############################################################################
  
  ###############################################################################
  #6) enrichMKEGG: KEGG module over-representation analysis
  print("6: STARTING KEGG MODULE OVER-REPRESENTATION ANALYSIS (enrichMKEGG)")
  mkk <- NULL
  tryCatch({
  mkk <- enrichMKEGG(gene = de_genes_only,
                     organism = org,
                     pvalueCutoff = mkeggPcutoff,
                     qvalueCutoff = mkeggPcutoff,
                     pAdjustMethod = enrichP1
  )
  }, error = function(e) {
    cat("[NETWORK EXCEPTION] KEGG Module Server connection dropped during ORA!\n")
    emkegg <<- NULL
  })
  
  if (!is.null(mkk) && nrow(as.data.frame(mkk)) != 0){
    cat("Success! Number of enrichMKEGG results: ",nrow(as.data.frame(mkk)),"\n")
    
    print("KEGG module over-representation analysis summary:")
    head(mkk)
    
    mkk_df<-as.data.frame(mkk)
    
    write.csv(mkk_df, "enrichMKEGG_Results.csv", row.names = FALSE)
    sink.reset()
    
    #####################
    #PLOTS
    cat("\nGenerating enrichMKEGG_DotPlot.png\n")
    CairoPNG(file ="enrichMKEGG_DotPlot.png",width = 11, height = 7, units = "in", res = 300)
    print(dotplot(mkk, showCategory = min(nrow(mkk_df), 20), 
                  title = "KEGG Functional Modules Over-Representation Analysis: Enriched Structs") + 
            scale_y_discrete(labels = function(x) stringr::str_wrap(x, width = 70),
                             expand = expansion(add = c(1, 1))) +
            theme(plot.title   = element_text(face = "bold", size = 14, hjust = 0.5, margin = margin(b = 10)),
                  axis.text.y  = element_text(size = 8, lineheight = 0.75, vjust = 0.5),
                  plot.margin = unit(c(1, 2, 1, 1), "cm")))
    invisible(dev.off())
    
    x2 <- enrichplot::pairwise_termsim(mkk) 
    cat("\nGenerating enrichMKEGG_EMAPPlot.png\n")
    CairoPNG(file ="enrichMKEGG_EMAPPlot.png",width = 11, height = 7, units = "in", res = 300)
    print(enrichplot::emapplot(x2, showCategory = min(nrow(mkk_df), 20),
                               node_label_size = 2.5,label_format    = 25
                               )+
           
      ggtitle("Enriched KEGG Structural Modules: Network Association Topology Maps")+
      theme(plot.title   = element_text(face = "bold", size = 14, hjust = 0.5, margin = margin(b = 10)),
            plot.margin = unit(c(1, 2, 1, 1), "cm")))
    invisible(dev.off())
    
    cat("\nGenerating enrichMKEGG_BarPlot.png\n")
    CairoPNG(file = "enrichMKEGG_BarPlot.png", width = 11, height = 7, units = "in", res = 300)
    
    print(
      barplot(mkk, showCategory = min(nrow(mkk_df), 20),
              title = "KEGG Functional Modules: Pathway Enrichment Significance") + 
        scale_y_discrete(labels = function(x) stringr::str_wrap(x, width = 70),
                         expand = expansion(add = c(1, 1))) +
        theme(plot.title   = element_text(face = "bold", size = 14, hjust = 0.5, margin = margin(b = 10)),
              axis.text.y  = element_text(size = 8, lineheight = 0.75, vjust = 0.5),
              plot.margin  = unit(c(1, 2, 1, 1), "cm"))
    )
    
    invisible(dev.off())
    
    cat("\nGenerating enrichMKEGG_CNETPlot.png\n")
    # Networks require a bit more breathing room, so we use 13x9 inches
    CairoPNG(file = "enrichMKEGG_CNETPlot.png", width = 13, height = 9, units = "in", res = 300)
    
    print(
      enrichplot::cnetplot(clusterProfiler::setReadable(mkk,    OrgDb = organism,    keyType = "ENTREZID"), 
                           showCategory = min(nrow(mkk_df), 10), 
                          foldChange = geneList) +  # <-- Pass your named numeric vector here!
        ggtitle("KEGG Functional Modules: Category-Gene Networks") +
        theme(plot.title   = element_text(face = "bold", size = 14, hjust = 0.5, margin = margin(b = 10)),
              plot.margin  = unit(c(1, 1, 1, 1), "cm"))
    )
    
    invisible(dev.off())
    
    cat("\nGenerating enrichMKEGG_UpsetPlot.png\n")
    # Clean, wide canvas layout to handle the text naturally
    CairoPNG(file = "enrichMKEGG_UpsetPlot.png", width = 13, height = 7, units = "in", res = 300)
    
    # 1. Generate the base upset plot from your original object
    p_upset <- enrichplot::upsetplot(mkk, n = min(nrow(mkk_df), 10))
    
    # 2. Directly shrink the inner label sizes with zero extra layout hacks
    p_upset_tuned <- p_upset & 
      theme(
        axis.text.y = element_text(size = 9.5),
        axis.text.x = element_text(size = 9.5)
      )
    
    # 3. Apply the centered title to the main canvas
    final_upset <- ggplotify::as.ggplot(p_upset_tuned) + 
      labs(title = "KEGG Functional Modules: Set Intersections of Enriched Pathways") +
      theme(
        plot.title = element_text(
          face = "bold", 
          size = 14, 
          hjust = 0.5, 
          margin = margin(t = 10, b = 5)
        ),
        plot.margin = margin(t = 5, r = 10, b = 5, l = 10, unit = "pt")
      )
    
    # Print the final object to the file device
    print(final_upset)
    
    invisible(dev.off())
    #  browseKEGG(mkk_result, pathwayID = "M00123")  # M00123 is an example module
  } else{
    cat("None of the input genes are annotated in KEGG modules. Skipping enrichMKEGG.\n")
  }
  ##############################################################################
  # 7) gseMKEGG
  print("7: STARTING KEGG MODULE GENE SET ENRICHMENT ANALYSIS (gseMKEGG)")
  
  
  # Fetch all valid KEGG module genes for your organism via KEGGREST API
  kegg_ids <- KEGGREST::keggLink(org, "module")
  # Crucial Change: Use bg_geneList (the 20,000 background genes) for GSEA validation!
  formatted_query_ids <- paste0(org, ":", names(geneList))
  all_kegg_genes      <- unname(kegg_ids)
  # Validate your bg_geneList entries against the official database
  valid_ids <- formatted_query_ids %in% all_kegg_genes
  cat("Number of background geneList entries mapping to KEGG Modules: ", sum(valid_ids), "out of", length(geneList), "\n")
  
  
  
  # kegg_ids <- KEGGREST::keggLink(org, "module")  # or pathway if you're doing enrichKEGG
  # formatted_query_ids <- paste0("hsa:", names(geneList))
  # all_kegg_genes      <- unname(kegg_ids)
  # 
  # # Validate your geneList
  # valid_ids <- formatted_query_ids %in% all_kegg_genes #paste0("hsa:", names(geneList)) %in% names(kegg_ids)
  # cat("Number of geneList entries mapping to KEGG Modules: ", sum(valid_ids), "out of", length(geneList), "\n")
  # 
  mkk2 <- NULL
  if (sum(valid_ids) > 0) {
    cat("Running gseMKEGG with valid gene IDs...\n")
    mkk2 <- tryCatch({
      gseMKEGG(
        geneList = geneList,
        organism = org,
        keyType = "ncbi-geneid",
        pvalueCutoff = gmkeggPcutoff,
        pAdjustMethod = gseP1,
        eps           = eps
      )
    }, error = function(e) {
      message("gseMKEGG failed: ", e$message)
      mkk2 <- NULL
    })
  } else {
    cat("No valid gseMKEGG mappable gene IDs found in geneList. Skipping gseMKEGG.\n")
    mkk2 <- NULL
  }
  
  
  if (!is.null(mkk2) && nrow(as.data.frame(mkk2)) != 0){
    
    cat("Success! Number of gseMKEGG results: ",nrow(as.data.frame(mkk2)),"\n")
    
    print("gseMKEGG module gene set enrichment analysis summary:")
    head(mkk2)
    
    mkk2_df<-as.data.frame(mkk2)
    
    write.csv(mkk2_df, "gseMKEGG_Results.csv", row.names = FALSE)
    sink.reset()
    
    #PLOTS
    cat("\nGenerating gseMKEGG_DotPlot.png\n")
    CairoPNG(file ="gseMKEGG_DotPlot.png",width = 11, height = 7, units = "in", res = 300)
    print(dotplot(mkk2, showCategory = min(nrow(mkk2_df), 20), 
                  title = "KEGG Module Gene Set Enrichment Analysis (GSEA): Dotplot Mapping Profiles") + 
            scale_y_discrete(labels = function(x) stringr::str_wrap(x, width = 50),
                             expand = expansion(add = c(1, 1))) +
            theme(plot.title   = element_text(face = "bold", size = 14, hjust = 0.5, margin = margin(b = 10)),
                  axis.text.y  = element_text(size = 8.5),
                  plot.margin = unit(c(0.5, 0.5, 0.5, 0.5), "cm")))
    invisible(dev.off())
    
    x2 <- enrichplot::pairwise_termsim(mkk2) 
    cat("\nGenerating gseMKEGG_EMAPPlot.png\n")
    CairoPNG(file = "gseMKEGG_EMAPPlot.png", width = 14, height = 9, units = "in", res = 300)
    
    # Generate your exact original plot cleanly—no hidden layer modifications
    p_emap <- enrichplot::emapplot(x2, showCategory = min(nrow(mkk2_df), 20))
    
    print(
      p_emap +  
        ggtitle("KEGG Module GSEA Network Topology: Similarity Array Interactions Maps") +
        theme(
          # The title size can stay at 14 because the canvas is wider now
          plot.title   = element_text(face = "bold", size = 14, hjust = 0.5, margin = margin(b = 15)),
          plot.margin  = unit(c(0.75, 1, 0.75, 1), "cm")
        )
    )
    
    invisible(dev.off())
    
    
      
    cat("\nGenerating gseMKEGG_GseaPlot.png\n")
    CairoPNG(file ="gseMKEGG_GseaPlot.png",width = 12, height = 8, units = "in", res = 300)
    
    plotList<-list()
    for(i in 1:min(10, length(mkk2$Description))){
      plotList[[i]] <- print(gseaplot(mkk2, by = "all", title = mkk2$Description[i], geneSetID = i))+
        ggtitle(stringr::str_wrap(paste("GSEA: KEGG Module - " , mkk2$Description[i]), 
                                  width = 45))+
        theme(plot.title   = element_text(face = "bold", size = 9, 
                                          hjust = 0.5, margin = margin(b = 10)),
              plot.margin = unit(c(0.4, 0.4, 0.4, 0.4), "cm"),
              axis.title.x = element_text(size = 8),
              axis.text.y = element_text(size = 7),
              axis.text.x  = element_text(size = 7),
              axis.title.y = element_text(size = 8)
        )
    }
    # Dynamically arrange the plots (handles less than 10 elements)
    if (length(plotList) > 0) {
      dynamic_cols <- if (length(plotList) == 1) 1 else 2
      dynamic_rows <- ceiling(length(plotList) / dynamic_cols)
      do.call(grid.arrange, c(plotList, nrow = dynamic_rows, ncol = dynamic_cols))
    }
    invisible(dev.off())
  
    cat("\nGenerating gseMKEGG_RidgePlot.png\n")
    # Ridge plots need horizontal space for density waves and clear vertical row separation
    CairoPNG(file = "gseMKEGG_RidgePlot.png", width = 12, height = 8, units = "in", res = 300)
    
    print(
      enrichplot::ridgeplot(mkk2, showCategory = min(nrow(mkk2_df), 20)) + 
        scale_y_discrete(labels = function(x) stringr::str_wrap(x, width = 50)) +
        ggtitle("KEGG Module GSEA: Core Enrichment Expression Distributions") +
        theme(
          plot.title  = element_text(face = "bold", size = 14, hjust = 0.5, margin = margin(b = 10)),
          axis.text.y = element_text(size = 8.5),
          plot.margin = unit(c(0.5, 0.5, 0.5, 0.5), "cm")
        )
    )
    
    invisible(dev.off())
    
    cat("\nGenerating gseMKEGG_CNETPlot.png\n")
    # Networks require an open grid canvas to spread out complex module connections
    CairoPNG(file = "gseMKEGG_CNETPlot.png", width = 13, height = 9, units = "in", res = 300)
    
    print(
      enrichplot::cnetplot(clusterProfiler::setReadable(mkk2,    OrgDb = organism,    keyType = "ENTREZID"), 
                           showCategory = min(nrow(mkk2_df), 10), 
                           foldChange = geneList) +              # Colors gene nodes dynamically by expression values
        ggtitle("KEGG Module Frameworks: Functional Category-Gene Networks") +
        theme(
          plot.title  = element_text(face = "bold", size = 14, hjust = 0.5, margin = margin(b = 10)),
          plot.margin = unit(c(1, 1, 1, 1), "cm")
        )
    )
    
    invisible(dev.off())
    
    
    
    }else {
    cat("No significant KEGG module enrichment or mapping failed.\n")
  }#endif
  
  
  cat("\n Initiating Molecular Pathway Visualization Layout...\n")
  
  # 1. Automatically grab the top enriched pathway ID from your KEGG data frame
  # (Works with either ORA 'ekegg_df' or GSEA 'keggGSEA_df')
  if (exists("ekegg_df") && nrow(ekegg_df) > 0) {
    top_pathway_id <- ekegg_df$ID[1] 
  } else if (exists("gsekegg_df") && nrow(gsekegg_df) > 0) {
    top_pathway_id <- gsekegg_df$ID[1]
  } else {
    top_pathway_id <- NULL
    cat("[WARNING] No enriched KEGG pathways found to map with Pathview.\n")
  }
  
  if (!is.null(top_pathway_id)) {
    # Strip away any letters from the ID to make it clean for pathview (e.g., "hsa04110" -> "04110")
    clean_pathway_id <- gsub("[a-zA-Z]", "", top_pathway_id)
    
    cat(sprintf("[PATHVIEW] Downloading and rendering structural overlay map for ID: %s\n", clean_pathway_id))
    
    # 2. Render the pathway layout map
    pathview(
      gene.data  = geneList,           # Uses your full background ranked list with Entrez IDs
      pathway.id = clean_pathway_id,   # The top pathway ID
      species    = org,                # Dynamically maps your three-letter code variable (e.g., "hsa")
      limit      = list(gene = max(abs(geneList)), cpd = 1), 
      bins       = list(gene = 20, cpd = 20)
    )
    cat(sprintf("[PATHVIEW] Success! Check your working folder for '%s%s.pathview.png'.\n", org, clean_pathway_id))
  }
  
  #-------------------------------------------------------------------------------
  #                         PATHWAY ANALYSIS
  #-------------------------------------------------------------------------------
  cat("\nInitiating Molecular Pathway Visualization Layout...\n")
  
  # ==========================================
  # BLOCK 1: Process ORA (ekegg_df)
  # ==========================================
  if (exists("ekegg_df") && nrow(ekegg_df) > 0) {
    # 1. Sort by adjusted p-value to ensure top is actually most significant
    ekegg_df <- ekegg_df[order(ekegg_df$p.adjust), ]
    top_ora_id <- ekegg_df$ID[1]
    
    # Clean the ID (e.g., "hsa04110" -> "04110")
    clean_ora_id <- gsub("[a-zA-Z]", "", top_ora_id)
    
    cat(sprintf("[PATHVIEW - ORA] Rendering map for top ORA ID: %s\n", clean_ora_id))
    
    # Render the ORA pathway map
    pathview(
      gene.data  = geneList,           
      pathway.id = clean_ora_id,       
      species    = org,                
      limit      = list(gene = max(abs(geneList)), cpd = 1), 
      bins       = list(gene = 20, cpd = 20)
    )
    # Appending '_ORA' to let you know which analysis generated this image
    cat(sprintf("[PATHVIEW] Success! Saved ORA map as '%s%s.pathview.png'.\n", org, clean_ora_id))
  } else {
    cat("[INFO] No ORA (ekegg_df) data found for Pathview.\n")
  }
  
  # --- Visual Separator ---
  cat(paste0("\n", paste(rep("-", 60), collapse=""), "\n"))
  
  # ==========================================
  # BLOCK 2: Process GSEA (keggGSEA_df) gsekegg_df
  # ==========================================
  if (exists("gsekegg_df") && nrow(gsekegg_df) > 0) {
    # 1. Sort by adjusted p-value
    gsekegg_df <- gsekegg_df[order(gsekegg_df$p.adjust), ]
    top_gsea_id <- gsekegg_df$ID[1]
    
    # Clean the ID
    clean_gsea_id <- gsub("[a-zA-Z]", "", top_gsea_id)
    
    cat(sprintf("[PATHVIEW - GSEA] Rendering map for top GSEA ID: %s\n", clean_gsea_id))
    
    # Render the GSEA pathway map
    pathview(
      gene.data  = geneList,           
      pathway.id = clean_gsea_id,       
      species    = org,                
      limit      = list(gene = max(abs(geneList)), cpd = 1), 
      bins       = list(gene = 20, cpd = 20)
    )
    cat(sprintf("[PATHVIEW] Success! Saved GSEA map as '%s%s.pathview.png'.\n", org, clean_gsea_id))
  } else {
    cat("[INFO] No GSEA (gsekegg_df) data found for Pathview.\n")
  }
  
  #-------------------------------------------------------------------------------
  #                         REACTOME PATHWAY INTEGRATION
  #-------------------------------------------------------------------------------
 
  # Map your 3-letter 'org' variable to Reactome's full-name requirements
 
  if (org == "mmu") reactome_organism <- "mouse"
  if (org == "rno") reactome_organism <- "rat"
  if (org == "hsa") reactome_organism <- "human"
  cat(sprintf("\n[REACTOME] Executing Profiling Step on '%s' Space...\n", reactome_organism))
  
  # ---- 1. Over-Representation Analysis (ORA) ----
  reactome_enrich <- enrichPathway(
    gene         = de_genes_only,      # Character vector of significant Entrez IDs
    organism     = reactome_organism, 
    pvalueCutoff = 0.05,
    readable     = TRUE                # Flips Entrez IDs back to reader-friendly Gene Symbols
  )
  
  # ---- 2. Gene Set Enrichment Analysis (GSEA) ----
  reactome_gsea <- gsePathway(
    geneList     = geneList,           # Your globally ranked numeric vector
    organism     = reactome_organism,
    pvalueCutoff = 0.05,
    verbose      = FALSE
  )
  
  # Convert objects to data frames for safe downstream evaluations
  reactome_enrich_df <- as.data.frame(reactome_enrich)
  reactome_gsea_df   = as.data.frame(reactome_gsea)
  
  # ---- 3. Generating Visualization Assays ----
  if (nrow(reactome_enrich_df) > 0) {
    cat("Generating Reactome_DotPlot.png\n")
    CairoPNG(file = "Reactome_DotPlot.png", width = 11, height = 7, units = "in", res = 300)
    print(
      dotplot(reactome_enrich, showCategory = min(nrow(reactome_enrich_df), 20), 
              title = "Reactome Frameworks: Pathway Over-Representation Mapping Profiles") +
        scale_y_discrete(labels = function(x) stringr::str_wrap(x, width = 50),
                         expand = expansion(add = c(1, 1))) +
        theme(
          plot.title  = element_text(face = "bold", size = 14, hjust = 0.5, margin = margin(b = 10)),
          axis.text.y = element_text(size = 8.5),
          plot.margin = unit(c(0.5, 0.5, 0.5, 0.5), "cm")
        )
    )
    invisible(dev.off())
  }#end-if
  
  # ---- 3. Generating GSEA Visualization Assays ----
  if (nrow(reactome_gsea_df) > 0) {
    cat("Generating Reactome_GSEA_DotPlot.png\n")
    # 1. Increase the height of the canvas slightly to stretch out the y-axis
    CairoPNG(file = "Reactome_GSEA_DotPlot.png", width = 13, height = 9, units = "in", res = 300)
    print(
      dotplot(reactome_gsea, showCategory = min(nrow(reactome_gsea_df), 20), split = ".sign",
              title = "Reactome Frameworks: Pathway GSEA Mapping Profiles") +
        # 2. Lower the wrap width to 45 so long phrases split more cleanly 
        scale_y_discrete(labels = function(x) stringr::str_wrap(x, width = 70)) +
        theme(plot.title  = element_text(face = "bold", size = 14, hjust = 0.5, margin = margin(b = 10)),
          # 3. Reduce the y-axis font size slightly (from 8.5 to 7.5)
          axis.text.y = element_text(size = 8.5, lineheight = 0.8), 
          # 4. Add subtle spacing between the facets if needed
          panel.spacing = unit(1.5, "lines"),
          
          plot.margin = unit(c(0.5, 0.5, 0.5, 0.5), "cm")
        )
    )
    invisible(dev.off())
  }
  
  #-------------------------------------------------------------------------------
  #                         DISEASE ONTOLOGY INTEGRATION
  #-------------------------------------------------------------------------------
  if (org == "hsa") {
    cat("\n[DISEASE ONTOLOGY] Initializing Human Clinical Phenotype Cross-References...\n")
    
    # ---- 1. Over-Representation Analysis (ORA) ----
    # FIX: Changed ont = "DO" to ont = "HDO"
    do_enrich <- enrichDO(
      gene         = de_genes_only,    # Significant Entrez IDs
      ont          = "HDO",            
      pvalueCutoff = 0.05,
      readable     = TRUE
    )
    
    # ---- 2. Gene Set Enrichment Analysis (GSEA) ----
    # FIX: Added ont = "HDO" explicitly to keep it synced
    do_gsea <- gseDO(
      geneList     = geneList,         # Ranked expression genome vector
      ont          = "HDO",
      pvalueCutoff = 0.05,
      verbose      = FALSE
    )
    
    # Ensure readable symbols are available for GSEA plotting if supported
    if (nrow(as.data.frame(do_gsea)) > 0) {
      do_gsea <- clusterProfiler::setReadable(do_gsea, OrgDb = "org.Hs.eg.db", keyType = "ENTREZID")
    }
    
    do_enrich_df <- as.data.frame(do_enrich)
    do_gsea_df   <- as.data.frame(do_gsea)
    
    # ---- 3a. Generating CNET Plot for ORA Results ----
    if (nrow(do_enrich_df) > 0) {
      cat("Generating DiseaseOntology_ORA_CNETPlot.png\n")
      CairoPNG(file = "DiseaseOntology_ORA_CNETPlot.png", width = 13, height = 9, units = "in", res = 300)
      print(
        enrichplot::cnetplot(
          do_enrich, 
          showCategory = min(nrow(do_enrich_df), 10),
          foldChange   = geneList
        ) + 
          ggtitle("Disease Ontology Networks (ORA): Genetic Intersections of Clinical Pathologies") +
          theme(
            plot.title  = element_text(face = "bold", size = 14, hjust = 0.5, margin = margin(b = 10)),
            plot.margin = unit(c(1, 1, 1, 1), "cm")
          )
      )
      invisible(dev.off())
    }
    
    # ---- 3b. Generating CNET Plot for GSEA Results ----
    if (nrow(do_gsea_df) > 0) {
      cat("Generating DiseaseOntology_GSEA_CNETPlot.png\n")
      CairoPNG(file = "DiseaseOntology_GSEA_CNETPlot.png", width = 11, height = 7, units = "in", res = 300)
      print(
        enrichplot::cnetplot(
          do_gsea, 
          showCategory = min(nrow(do_gsea_df), 10), 
          foldChange   = geneList
          ) + 
          ggtitle("Disease Ontology Networks (GSEA): Functional Disease Framework Profiles") +
          theme(
            plot.title  = element_text(face = "bold", size = 14, hjust = 0.5, margin = margin(b = 10)),
            plot.margin = unit(c(1, 1, 1, 1), "cm")
          )
      )
      invisible(dev.off())
    }
    
  } else {
    cat("\n[DISEASE ONTOLOGY] Skipped: Disease Ontology profiling is exclusively reserved for human datasets ('hsa').\n")
  }
  
  
  
  
  
  
  
  
  dev.off()
  cat("\n====================================================================\n")
  cat("[PROCESS COMPLETE] GSEA ANALYSIS FINISHED SUCCESSFULLY\n")
  cat("All enrichment tables, pathway statistics, and publication-quality plots were generated.\n")
  cat("====================================================================\n")
  
  
  
  
  
    
}# End GSEA Function
  


#===============================================================================
#                           MAIN FUNCTION
#===============================================================================

# CHECKING THE TYPE OF ANALYSIS deseq2 OR edgeR
# LOAD 'DEGENES' OBJECT ACCORDINGLY
if(analysis == "deSeq2")
{
  setwd(file.path(myargs[1])) 
  deGenes <-  readRDS("degenes.RDS") 
  cat("\n[GSEA INITIALIZATION] Loading global background landscape profile...\n")
  global_raw_list <- readRDS("gsea_geneList.RDS") 
  
  folderN<-"DeSeqResults"
  dir.create(file.path(myargs[1], "DeSeqResults/GeneEnrich_Results"))
  setwd(file.path(myargs[1], folderN, "GeneEnrich_Results"))
  geneSetAnalysis(deGenes,folderN,"deSeq2")
}else if (analysis == "edgeR"){
  #deGenes <-read.csv(filepath,sep = "\t", header = TRUE)
  setwd(file.path(myargs[1])) 
  folderN<-"EdgeRResults"
  deGenes <-  readRDS("degenesE.RDS")
  gsea_matches <- list.files(path = file.path(myargs[1]), pattern = "^gsea_geneList_.*\\.RDS$", full.names = TRUE)
  if (length(gsea_matches) == 0) {
    stop("CRITICAL ERROR: No edgeR GSEA gene list file matching pattern 'gsea_geneList_*.RDS' found in EdgeRResults directory.")
  }
  
  cat(sprintf("\n[GSEA INITIALIZATION] Loading edgeR global background landscape profile from: %s\n", basename(gsea_matches[1])))
  global_raw_list <- readRDS(gsea_matches[1])
  
  dir.create(file.path(myargs[1], "EdgeRResults/GeneEnrich_Results"))
  
  setwd(file.path(myargs[1], folderN, "GeneEnrich_Results"))
  geneSetAnalysis(deGenes,folderN,"edgeR")
}else if (analysis == "both"){
  
  for(i in 1:2){
    setwd(file.path(myargs[1])) 
    if(i==1){
      deGenes <-  readRDS("degenes.RDS")
      folderN<-"DeSeqResults"
      cat("\n[GSEA INITIALIZATION] Loading global background landscape profile...\n")
      global_raw_list <- readRDS("gsea_geneList.RDS") 
      dir.create(file.path(myargs[1], "DeSeqResults/GeneEnrich_Results"))
      setwd(file.path(myargs[1], folderN, "GeneEnrich_Results"))
      geneSetAnalysis(deGenes,folderN,"deSeq2")
    }else if (i==2){
      deGenes <-  readRDS("degenesE.RDS")
      folderN<-"EdgeRResults"
      gsea_matches <- list.files(path = file.path(myargs[1]), pattern = "^gsea_geneList_.*\\.RDS$", full.names = TRUE)
      if (length(gsea_matches) == 0) {
        stop("CRITICAL ERROR: No edgeR GSEA gene list file matching pattern 'gsea_geneList_*.RDS' found in EdgeRResults directory.")
      }
      
      cat(sprintf("\n[GSEA INITIALIZATION] Loading edgeR global background landscape profile from: %s\n", basename(gsea_matches[1])))
      global_raw_list <- readRDS(gsea_matches[1])
      dir.create(file.path(myargs[1], "EdgeRResults/GeneEnrich_Results"))
      setwd(file.path(myargs[1], folderN, "GeneEnrich_Results"))
      geneSetAnalysis(deGenes,folderN,"edgeR")
    }
    
  }
  
}
  

