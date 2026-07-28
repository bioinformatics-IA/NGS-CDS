# myargs[4]= "archive,backports,bench,BiocManager,bslib,cachem,crayon,Cairo,decor,digest,farver,ggbeeswarm,ggfortify,ggplot2,ggpp,ggraph,ggrepel,ggridges,ggupset,googleAuthR,googleCloudStorageR,gplots,igraph,igraphdata,labeling,markdown,matrixStats,memoise,mockery,mockr,multcomp,pheatmap,prettydoc,R.devices,R.rsp,raster,RColorBrewer,rgl,s3,scales,sf,shiny,simplermarkdown,spelling,statmod,stringr,tidyverse,tzdb,vroom,withr"
# myargs[5]= "limma,vsn,PCAtools,BiocGenerics,S4Vectors,IRanges,GenomeInfoDb,GenomicRanges,MatrixGenerics,Biobase,SummarizedExperiment,genefilter"

##################################
###### INPUT PARAMETERS###########
##################################
myargs = commandArgs(trailingOnly=TRUE)

# 
# myargs[1]="/home/iffy/NetBeansProjects/NGSGradle/app/R_Libraries"
# myargs[2]="/home/iffy/NetBeansProjects/NGSGradle/app/bin/R_Packages"
# myargs[3]= "INSTALL"
# myargs[4]= "4.5.1"
# myargs[5]= "YES"
# myargs[6]= "NO"
# myargs[7]= "offline"
# myargs[8]= "SINGLE"
# myargs[9]= "codetools"

installIN=  myargs[1] 
binaries_folder= myargs[2]
code_mode=myargs[3] #INSTALL,DOWNLOAD,CHECK
required_version <- myargs[4] #"4.5.1"
cranPkgs=    myargs[5] 
biocPkgs=   myargs[6] 
mode=   myargs[7]  
functionValue= myargs[8] 
pkgName=myargs[9]

cat(
  "========================================\n",
  "        PASSED PARAMETERS LOG          \n",
  "========================================\n",
  "installIN:        ", installIN, "\n",
  "binaries_folder:  ", binaries_folder, "\n",
  "code_mode:        ", code_mode, "\n",
  "required_version: ", required_version, "\n",
  "cranPkgs:         ", cranPkgs, "\n",
  "biocPkgs:         ", biocPkgs, "\n",
  "mode:             ", mode, "\n",
  "functionValue:    ", functionValue, "\n",
  "pkgName:          ", pkgName, "\n",
  "========================================\n",
  sep = ""
)


##################################
###### FUNCTIONS    ###########
##################################

# FUNCTION 1: To install Packages from Folder
installPkgs<- function(my_Pkgs,installIN,installFROM){
  for (package_name in my_Pkgs){
    if (package_name %in% c("curl", "ragg", "haven", "vroom","feather","sparseMatrixStats")) {
      install.packages(package_name,
                       lib = installIN , 
                       contriburl = paste("file://",installFROM,sep = ""),
                       type = "source", 
                       configure.vars = paste0("INCLUDE_DIR=", Sys.getenv("INCLUDE_DIR"), 
                                               " LIB_DIR=", Sys.getenv("LIB_DIR"),
                                               " CXXFLAGS=-Wno-error=format-security"),
                       dependencies = TRUE,
                       ask = FALSE
      )  
      
    }
    else if (package_name=="s2"){
      install.packages(package_name,
                       lib = installIN , 
                       contriburl = paste("file://",installFROM,sep = ""),
                       type = "source", 
                       configure.vars = paste(
                         "PKG_CFLAGS='", Sys.getenv("INCLUDE_DIR"), "' ",
                         "PKG_LIBS='", Sys.getenv("LIB_DIR"), " -lfreetype -lpng16 -lz -llapack -lblas'"
                       ),
                       dependencies = TRUE,
                       ask = FALSE
      )  
      
    }
    else if (package_name=="edgeR"){
      install.packages(package_name,
                       lib = installIN , 
                       contriburl = paste("file://",installFROM,sep = ""),
                       type = "source", 
                       configure.vars = "--with-lapack=/usr/lib/x86_64-linux-gnu/liblapack.so",
                       dependencies = TRUE,
                       ask = FALSE
      )  
      
    }  else if (package_name=="RMySQL"){
      install.packages(package_name,
                       lib = installIN , 
                       contriburl = paste("file://",installFROM,sep = ""),
                       type = "source", 
                    #   configure.vars = "INCLUDE_DIR=/usr/include/mariadb LIB_DIR=/usr/lib/x86_64-linux-gnu",
                       configure.args = "--with-mysql-config=/usr/bin/mysql_config",
                       dependencies = TRUE,
                       ask = FALSE
      )  
      
    } 
    else if (package_name == "sf") {
      install.packages(package_name,
                       lib = installIN,
                       contriburl = paste("file://", installFROM, sep = ""),
                       type = "source",
                       configure.args = "--with-gdal-config=/usr/local/bin/gdal-config",
                       dependencies = TRUE,
                       ask = FALSE
      )
    }
    else{
      tryCatch({
        Sys.setenv(CXXFLAGS="-Wno-format-security", CFLAGS="-Wno-format-security")
      install.packages(package_name,
                       lib = installIN , 
                       contriburl = paste("file://",installFROM,sep = ""),
                       type = "source", 
                       dependencies = TRUE,
                       ask = FALSE
      )
       
      }, error = function(e) {
        stop(paste("Error installing package:", package_name, "\nError message:", e$message))
      })
    }#else
    
  }
}#END- installPkgs

# FUNCTION 2: To create PACKAGE file for Binaries
checkBinaries<-function(folder_name){
   # Flag to track if any file is corrupted
  all_files_valid <- TRUE
  # Get the list of package files in the directory
  pkg_files <- list.files(folder_name, pattern = "\\.tar\\.gz$", full.names = TRUE)
  
  cat("Total packages found:", length(pkg_files), "\n\n")
  
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
    cat("\nAll", length(pkg_files), "files are valid. Creating PACKAGES file.\n")
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

### FUNCTION TO UPDATE BINARIES
updateLocalCRANMirror <- function(local_repo) {
  options(timeout = 600)  # Extend timeout
  .libPaths(c(installIN, .libPaths()))  # Prioritize installIN
  
  # Load required packages
  if (!requireNamespace("tools", quietly = TRUE)) install.packages("tools", lib = installIN)
  if (!requireNamespace("BiocManager", quietly = TRUE)) install.packages("BiocManager", lib = installIN)
  
  # Read local PACKAGES file
  packages_path <- file.path(local_repo, "PACKAGES")
  if (!file.exists(packages_path)) {
    stop("PACKAGES file does not exist. Run checkBinaries() first.")
  }
  
  local_pkgs <- as.data.frame(read.dcf(packages_path), stringsAsFactors = FALSE)
  rownames(local_pkgs) <- local_pkgs$Package
  
  # Detect R version
  r_ver <- paste0(R.version$major, ".", strsplit(R.version$minor, "\\.")[[1]][1])
  
  version_map <- BiocManager:::.version_map()
  
  # Get the matching Bioconductor version from the version map
  version_map <- BiocManager:::.version_map()
  colnames(version_map) <- c("Bioc", "RVer", "Status")  # assign column names
  
  # Find the Bioconductor version where RVer matches your R version
  bioc_match_df <- subset(version_map, RVer == r_ver & Status == "release")
  
  if (nrow(bioc_match_df) > 0) {
    bioc_version <- as.character(bioc_match_df$Bioc[1])
  } else {
    warning("No exact Bioconductor release match found for R ", r_ver, ". Using latest known Bioc version.")
    release_versions <- na.omit(version_map$Bioc[version_map$Status == "release"])
    if (length(release_versions) == 0) stop("No valid Bioconductor versions found in map.")
    bioc_version <- as.character(tail(release_versions, 1))
  }
  
  message("Detected compatible Bioconductor version for ", r_ver, ": ", bioc_version)
  
  # Set Bioconductor version
  BiocManager::install(version = bioc_version, ask = FALSE, update = FALSE, lib = installIN)
  
  # Fetch available packages
  message("Fetching CRAN package list...")
  cran_pkgs <- available.packages()
  
  message("Fetching Bioconductor package list...")
  bioc_pkgs <- BiocManager::available()
  
  # Update loop
  for (pkg in rownames(local_pkgs)) {
    local_version <- local_pkgs[pkg, "Version"]
    updated <- FALSE
    
    if (pkg %in% rownames(cran_pkgs)) {
      cran_version <- cran_pkgs[pkg, "Version"]
      if (utils::compareVersion(cran_version, local_version) > 0) {
        message("Updating CRAN package: ", pkg, " (", local_version, " → ", cran_version, ")")
        updated <- TRUE
      }
    } else if (pkg %in% bioc_pkgs) {
      message("Updating Bioconductor package: ", pkg)
      updated <- TRUE
    } else {
      warning("Package ", pkg, " not found on CRAN or Bioconductor.")
    }
    
    if (updated) {
      old_file_pattern <- paste0("^", pkg, "_", local_version, "\\.tar\\.gz$")
      old_file <- list.files(local_repo, pattern = old_file_pattern, full.names = TRUE)
      if (length(old_file)) file.remove(old_file)
      
      # Download new version
      download.packages(pkg, destdir = local_repo, type = "source", repos = BiocManager::repositories())
    } else {
      message("Keeping existing package: ", pkg, " (up-to-date)")
    }
  }
  
  # Rebuild PACKAGES metadata
  pkg_count <- length(list.files(local_repo, pattern = "\\.tar\\.gz$"))
  message("Rebuilding PACKAGES metadata... Total packages: ", pkg_count)
  tools::write_PACKAGES(dir = local_repo)
}


##################################
############ CODE  ###############
##################################

if(code_mode == "INSTALL")
{
  
  #Check if install folder exists
  if (!dir.exists(installIN)) {
    dir.create(installIN)
  }
  
  # Read exported variables from shell
  Sys.setenv(
    PATH = Sys.getenv("PATH"),
    INCLUDE_DIR = Sys.getenv("INCLUDE_DIR"),
    LIB_DIR = Sys.getenv("LIB_DIR"),
    PKG_CONFIG_PATH = Sys.getenv("PKG_CONFIG_PATH"),
    LAPACK_LIBS = Sys.getenv("LAPACK_LIBS"),
    BLAS_LIBS = Sys.getenv("BLAS_LIBS"),
    CXXFLAGS = "-Wno-format-security",
    CFLAGS = "-Wno-format-security"
  )
  
  # Verify if they are correctly set
  print("TESTING PATHS IN R:")
  system("ls /usr/include/mariadb/mysql.h")
  Sys.which("mariadb_config")
  
  print(Sys.getenv("PATH"))
  print(Sys.getenv("INCLUDE_DIR"))
  print(Sys.getenv("LIB_DIR"))
  print(Sys.getenv("PKG_CONFIG_PATH"))
  print(Sys.getenv("CXXFLAGS"))
  print(Sys.getenv("CFLAGS"))
  
  
  #Setting Library Path
  .libPaths(installIN)
  print(.libPaths())  # Verify that only your project folder is used
  
  
  #Convert list to factors from comma seperated strings
  my_cranPkgs <- unlist(strsplit(cranPkgs,","))
  my_cranPkgs <- trimws(my_cranPkgs)
  my_biocPkgs <- unlist(strsplit(biocPkgs,","))
  my_biocPkgs <- trimws(my_biocPkgs)
  #Update CRAN FOLDER
  
  #STEP 1: PREPARE PACKAGES FILE
  print("Checking Packages Binaries...")
  checkBinaries(binaries_folder)
  
  
  if(functionValue == "ALL") #ALL Packages 
  {
    
      if(mode == "offline"){
        #STEP 2: CHECK R VERSION
        if (getRversion() == required_version) {
          print("============================================")
          print("----------INSTALLING CRAN PACKAGES----------")
          print("============================================")
          
          #Install CRAN Packages (1st time) from local folder
          installPkgs(my_cranPkgs,installIN,binaries_folder)
          # Get compatible Bioconductor version for current R
        
            BiocManager::install(version = "3.22",lib=installIN, ask = FALSE, update=FALSE)
          print("============================================")
          print("----------INSTALLING BIOC PACKAGES----------")
          print("============================================")
          
          installPkgs(my_biocPkgs,installIN,binaries_folder)
          
        } else {
          stop(paste("R version", required_version, " is required."))
        }
        
        
      }
      else if(mode == "online"){
        
        print ("Online Installation: ")
        options(timeout = 600)
        options(install.packages.check.source = "no")
        Sys.setenv(R_INSTALL_STAGED = FALSE)
        
        print("============================================")
        print("----------INSTALLING CRAN PACKAGES----------")
        print("============================================")
        
        
        # Install BiocManager first if not already installed in installIN
        if (!"BiocManager" %in% rownames(installed.packages(lib.loc = installIN))) {
          install.packages("BiocManager", lib = installIN)
        }
        # Set library path so that BiocManager and Bioc packages go to installIN
        .libPaths(c(installIN, .libPaths()))
        BiocManager::install(version = "3.22", lib=installIN, ask= FALSE, update = TRUE) #Only downloads BiocManger
        
        # Get compatible Bioconductor version for current R
        bioc_version <- BiocManager::version()
        print(bioc_version)
        install.packages(my_cranPkgs, lib = installIN , dependencies = NA)
        
        
        print("============================================")
        print("----------INSTALLING BIOC PACKAGES----------")
        print("============================================")
        BiocManager::install(my_biocPkgs, lib = installIN ,dependencies =c("Depends", "Imports"), ask= FALSE , force = TRUE )
        
      }
      
  }else if(functionValue =="SINGLE"){
    if(mode == "offline"){
      
      # Get the names of .tar.gz files with version numbers
      #pattern <- "_(\\d+\\.)*\\d+\\.tar\\.gz$"
      # This pattern matches an underscore, followed by any numbers, dots, or dashes, ending in .tar.gz
      pattern <- "_[0-9.-]+\\.tar\\.gz$"
      
      if(cranPkgs == "YES"){
        
        file_names <- list.files(binaries_folder, pattern = pattern)
        # Extract just the names without version and extension
        pkg_names <- gsub(pattern, "", file_names)
        
        
        # Split the pkgName string by commas, remove extra spaces, and trim it
        pkg_list <- strsplit(pkgName, ",")[[1]]
        pkg_list <- trimws(pkg_list)  # Remove leading/trailing whitespace
        for (pkg in pkg_list) { 
          if(pkg %in% pkg_names){
            
            install.packages(pkg,lib = installIN , 
                             contriburl = paste("file://",binaries_folder,sep = ""),
                             dependencies = TRUE) 
            print(sprintf("Package %s has been installed.", pkg))
          }else {
            print(sprintf("Package  %s binary is not available in %s",pkg,binaries_folder))
          }
        }#for
      } else if (biocPkgs == "YES"){
        
        file_names <- list.files(binaries_folder, pattern = pattern)
        # Extract just the names without version and extension
        pkg_names <- gsub(pattern, "", file_names)
        
        # Split the pkgName string by commas, remove extra spaces, and trim it
        pkg_list <- strsplit(pkgName, ",")[[1]]
        pkg_list <- trimws(pkg_list)  # Remove leading/trailing whitespace
        for (pkg in pkg_list) { 
          
          if(pkg %in% pkg_names){
            
            install.packages(pkg,lib = installIN , 
                             contriburl = paste("file://",binaries_folder,sep = ""),
                             dependencies = TRUE) 
          }else {
            print(sprintf("Package  %s binary is not available in %s",pkg,binaries_folder))
          }
        }#for
        
      } #end-else
      
    }else if(mode == "online"){
      # Split the pkgName string by commas, remove extra spaces, and trim it
      pkg_list <- strsplit(pkgName, ",")[[1]]
      pkg_list <- trimws(pkg_list)  # Remove leading/trailing whitespace
      
      if(cranPkgs == "YES"){
        install.packages(pkg_list, lib = installIN , repos = "http://cran.us.r-project.org",dependencies = TRUE)
        
      }else if(biocPkgs =="YES"){
        BiocManager::install(pkg_list, lib = installIN ,dependencies =TRUE, ask= FALSE , force = TRUE , verbose= TRUE )
        
        
      }
    }#end-else
  }#elseSINGLE
  
}  else if (code_mode == "DOWNLOAD"){
  #Check if install folder exists
  if (!dir.exists(binaries_folder)) {
    dir.create(binaries_folder)
  }
  # First validate existing binaries and create PACKAGES
 # checkBinaries(binaries_folder)
  cat("\nChecking for updates to existing packages. This may take a moment...\n\n")
  # Then update the mirror
  updateLocalCRANMirror(binaries_folder)
  
} else if (code_mode == "CHECK"){
  #Check if install folder exists
  if (!dir.exists(binaries_folder)) {
    dir.create(binaries_folder)
  }
  # First validate existing binaries and create PACKAGES
  checkBinaries(binaries_folder)
  
}

warnings()