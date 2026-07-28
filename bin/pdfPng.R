
myargs = commandArgs(trailingOnly=TRUE)
#### ARGUMENTS
#myargs[1]="/home/iffy/TestFiles/FINALOUTPUTS/"
#myargs[2]="/home/iffy/NetBeansProjects/NGSGradle/app/R_Libraries"

setwd(file.path(myargs[1]))

# Install magick if not already installed
if (!requireNamespace("magick", quietly = TRUE)) {
  install.packages("magick")
}

library(magick,lib.loc=myargs[2])

# Define the folder containing PNG files
folder_path <- myargs[3]
analysis_mode<-myargs[4]
# Get the list of PNG files
png_files <- list.files(folder_path, pattern = "\\.png$", full.names = TRUE)

# Read all PNG files as images
images <- image_read(png_files)

# Combine images into a single PDF
output_pdf <- paste(analysis_mode,"_CombinedPlots.pdf")
image_write(images, path = output_pdf, format = "pdf")

cat("PDF created at:", output_pdf, "\n")
