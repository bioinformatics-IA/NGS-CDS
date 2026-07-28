##################################
###### INPUT PARAMETERS###########
##################################
myargs = commandArgs(trailingOnly=TRUE)


#myargs[1]="/home/iffy/NetBeansProjects/NGSProjectRUN/R_Libraries"
#myargs[2]="/home/iffy/NetBeansProjects/NGSProjectRUN/bin/R_Package/CranPkgs"
#myargs[3]= "/home/iffy/NetBeansProjects/NGSProjectRUN/bin/R_Package/BioPkgs" 
#myargs[4]= "archive,backports,bench,BiocManager,bslib,cachem,crayon,Cairo,decor,digest,farver,ggbeeswarm,ggfortify,ggplot2,ggpp,ggraph,ggrepel,ggridges,ggupset,googleAuthR,googleCloudStorageR,gplots,igraph,igraphdata,labeling,markdown,matrixStats,memoise,mockery,mockr,multcomp,pheatmap,prettydoc,R.devices,R.rsp,raster,RColorBrewer,rgl,s3,scales,sf,shiny,simplermarkdown,spelling,statmod,stringr,tidyverse,tzdb,vroom,withr"
#myargs[5]= "limma,vsn,PCAtools,BiocGenerics,S4Vectors,IRanges,GenomeInfoDb,GenomicRanges,MatrixGenerics,Biobase,SummarizedExperiment,genefilter"
#myargs[6]= "offline" 
#myargs[7]= "SINGLE"
#myargs[8]= "magick"

binaries_folder= myargs[1]

required_version <- "4.5.1"
##################################
###### FUNCTIONS    ###########
##################################

# FUNCTION 2: To create PACKAGE file for Binaries
checkBinaries<-function(folder_name){
   # Flag to track if any file is corrupted
  all_files_valid <- TRUE
  # Get the list of package files in the directory
  pkg_files <- list.files(folder_name, pattern = "\\.tar\\.gz$", full.names = TRUE)
  for (file in pkg_files) {
    cat("Checking file:", file, "\n")
    if (system(paste("gzip -t", shQuote(file))) != 0) {
      cat("File is corrupted:", file, "\n")
      all_files_valid <- FALSE
    }
  }
  # If all files are valid, create the PACKAGES file
  if (all_files_valid) {
    library(tools)
    print("All files are valid. Creating PACKAGES file.")
    write_PACKAGES(pkg_files, dir = folder_name)
  } else {
    cat("Some files are corrupted. Please re-download the corrupted files and rerun the script.\n")
  }
  
  }#End: CheckBinaries(Creating PACKAGE FILE)

# Clean dependencies list
clean_dependencies <- function(deps) {
  deps <- deps[!is.na(deps)]                 # Remove NA values
  deps <- trimws(deps)                       # Remove leading/trailing whitespace
  deps <- deps[deps != "R"]                  # Remove "R" (base language)
  deps <- deps[nzchar(deps)]                 # Remove empty strings (if any)
  return(unique(deps))                       # Ensure unique entries
}

# Function to get dependencies manually
get_dependencies <- function(pkg_list, available_pkgs) {
  deps <- character(0)
  for (pkg in pkg_list) {
    if (pkg %in% rownames(available_pkgs)) {
      pkg_deps <- available_pkgs[pkg, c( "Depends", "Imports","LinkingTo")] #"Suggests", 
      pkg_deps <- unlist(strsplit(pkg_deps, ","))
      pkg_deps <- gsub("\\s*\\(.*\\)", "", pkg_deps)  # Remove version info
      pkg_deps <- pkg_deps[nzchar(pkg_deps)]  # Remove empty strings
      deps <- c(deps, pkg_deps)
    }
  }
  return(unique(deps))
}

# FUNCTION 2: To download packages from internet in folder
downloadCRANBinaries <- function(pkgs, destCRANDir){
  
  # Get the list of available packages from CRAN
  available_pkgs <- available.packages(repos = "https://cran.r-project.org")
  
  # Get recursive dependencies
  all_pkgs <- pkgs
  
  while (TRUE) {
    new_deps <- get_dependencies(all_pkgs, available_pkgs)
    new_deps <- clean_dependencies(new_deps)
    if (all(new_deps %in% all_pkgs)) break
    all_pkgs <- unique(c(all_pkgs, new_deps))
  }
  
  
  for(package_name in all_pkgs){
    download.packages(pkgs= package_name, 
                      destdir = destCRANDir , 
                      repos = "https://cran.r-project.org", 
                      dependencies = TRUE
    ) 
    
  }
  
}#END- downloadCRANBinaries
updateLocalCRANMirror <- function(local_repo) {
  # Load required package
  if (!requireNamespace("tools", quietly = TRUE)) install.packages("tools")
  
  # Step 1: Read local PACKAGES file
  packages_path <- file.path(local_repo, "PACKAGES")
  if (!file.exists(packages_path)) {
    stop("PACKAGES file does not exist. Run checkBinaries() first.")
  }
  
  local_pkgs <- as.data.frame(read.dcf(packages_path), stringsAsFactors = FALSE)
  rownames(local_pkgs) <- local_pkgs$Package
  
  # Step 2: Get list of available packages on CRAN
  cran_pkgs <- available.packages()
  
  # Step 3: Compare versions and download newer ones
  for (pkg in rownames(local_pkgs)) {
    local_version <- local_pkgs[pkg, "Version"]
    
    if (pkg %in% rownames(cran_pkgs)) {
      cran_version <- cran_pkgs[pkg, "Version"]
      
      if (utils::compareVersion(cran_version, local_version) > 0) {
        message("Updating package: ", pkg, " (", local_version, " → ", cran_version, ")")
        
        # Download newer version into local repo folder
        download.packages(pkg, destdir = local_repo, type = "source")
      } else {
        message("Keeping existing package: ", pkg, " (up-to-date)")
      }
    } else {
      warning("Package ", pkg, " not found on CRAN.")
    }
  }
  
  # Step 4: Regenerate PACKAGES file
  message("Rebuilding PACKAGES metadata...")
  tools::write_PACKAGES(dir = local_repo)
}


##################################
############ CODE  ###############
##################################
