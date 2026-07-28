myargs = commandArgs(trailingOnly = TRUE)
# #Expected formats:
# myargs[1] = "/mnt/ntfs/Output_RNASeq1/DeSeqResults,/mnt/ntfs/Output_RNASeq1/DeSeqResults/GeneEnrich_Results"
# myargs[2] = "/home/iffy/NetBeansProjects/NGSGradle/app/R_Libraries/"
# myargs[3] = "DESEQ2,GSEA"

# Ensure libraries path is loaded first
if (length(myargs) >= 2) {
  .libPaths(c(myargs[2], .libPaths()))
}

cat("\nLoading Libraries...\n")
required_libs <- c("grid", "png") 

for (pkg in required_libs) {
  tryCatch({
    suppressPackageStartupMessages(library(pkg, lib.loc = myargs[2], character.only = TRUE))
    cat("Loaded", pkg, "\n")
  }, error = function(e) {
    cat("ERROR: Failed to load", pkg, "--", conditionMessage(e), "\n")
    quit(status = 1)
  })
}

# Parse paths and modes
folder_paths   <- strsplit(myargs[1], ",")[[1]]
analysis_modes <- strsplit(myargs[3], ",")[[1]]

# --- MAIN LOOP CHANGED: PDF CREATION IS NOW INSIDE THE LOOP ---
for (i in seq_along(folder_paths)) {
  folder_path <- folder_paths[i]
  
  # Safeguard against mismatched argument lengths
  mode_name <- if (i <= length(analysis_modes)) analysis_modes[i] else paste0("Folder_", i)
  
  # Create a clean, unique PDF filename for this specific folder
  pdf_filename <- sprintf("%s_Analysis_Report.pdf", mode_name)
  
  cat(sprintf("\n==================================================\n"))
  cat(sprintf("--> Starting Folder %d/%d: %s (%s)\n", i, length(folder_paths), basename(folder_path), mode_name))
  
  # Fetch all PNG images inside this directory
  image_files <- list.files(folder_path, pattern = "\\.png$", full.names = TRUE, recursive = FALSE)
  
  if (length(image_files) == 0) {
    cat("    ! No PNG images found in this directory. Skipping PDF creation.\n")
    next
  }
  
  cat(sprintf("    Found %d PNG files. Opening dedicated PDF: %s\n", length(image_files), pdf_filename))
  
  # Set the working directory to this folder so the PDF drops directly inside it
  if (dir.exists(folder_path)) {
    setwd(folder_path)
  }
  
  # Open a dedicated PDF graphics device just for this folder
  pdf(file = pdf_filename, width = 11, height = 8.5) # Landscape orientation
  
  for (img_path in image_files) {
    img_name <- tools::file_path_sans_ext(basename(img_path))
    cat(sprintf("    + Adding image: %s\n", img_name))
    
    # Aggressive garbage collection before processing the next image matrix
    gc(verbose = FALSE)
    
    # Read the raw PNG file safely
    img_data <- tryCatch({
      png::readPNG(img_path)
    }, error = function(e) {
      cat(sprintf("    [ERROR] Could not read %s. Skipping.\n", img_name))
      return(NULL)
    })
    
    if (is.null(img_data)) next
    
    # Create a fresh new canvas page in the PDF
    grid.newpage()
    
    # Safe Title String
    title_text <- sprintf("%s | %s", mode_name, img_name)
    text_size <- if (nchar(title_text) > 50) 10 else 12
    
    # Title Header Viewport
    pushViewport(viewport(y = 0.95, height = 0.08, just = "top"))
    grid.text(title_text, gp = gpar(fontface = "bold", fontSize = text_size))
    popViewport()
    
    # Image Viewport with Aspect Ratio Auto-scaling
    tryCatch({
      img_height <- nrow(img_data)
      img_width  <- ncol(img_data)
      img_aspect <- img_width / img_height
      
      max_width  <- 0.95
      max_height <- 0.84
      pdf_aspect <- (11 * max_width) / (8.5 * max_height)
      
      if (img_aspect > pdf_aspect) {
        view_width  <- max_width
        view_height <- max_width / img_aspect * (11 / 8.5)
      } else {
        view_height <- max_height
        view_width  <- max_height * img_aspect * (8.5 / 11)
      }
      
      pushViewport(viewport(y = 0.04, height = view_height, width = view_width, just = "bottom"))
      grid.raster(img_data)
      popViewport()
      
    }, error = function(e) {
      cat(sprintf("    [ERROR] Failed to render raster for %s. Skipping page.\n", img_name))
      if (length(current.vp()) > 1) popViewport()
    })
    
    # Explicitly release memory right now
    rm(img_data)
  }
  
  # CRITICAL: Close the PDF device for THIS folder immediately.
  # This flushes data to disk and completely frees up R's graphical memory allocation.
  cat(sprintf("    Closing PDF device for %s...\n", mode_name))
  while (dev.cur() > 1) {
    invisible(dev.off())
  }
  
  if (file.exists(pdf_filename) && file.size(pdf_filename) > 0) {
    cat(sprintf("--> Success! %s generated safely (Size: %.2f MB).\n", pdf_filename, file.size(pdf_filename) / (1024^2)))
  } else {
    cat(sprintf("--> Error: %s was not created properly.\n", pdf_filename))
  }
  
  # Final loop cleanup pass
  gc(verbose = FALSE)
}

cat("\n==================================================\n")
cat("All folders processed completely.\n")
