detect_libraries <- function(file) {
  # Initialize the used_libraries variable in the global environment
  assign("used_libraries", character(0), envir = .GlobalEnv)
  
  # Function to log library calls for namespace operators (::)
  log_namespace_calls <- function() {
    calls <- sys.calls()
    for (call in calls) {
      if (is.call(call) && as.character(call[[1]]) == "::") {
        lib_name <- as.character(call[[2]])
        if (!lib_name %in% used_libraries) {
          used_libraries <<- c(used_libraries, lib_name)
        }
      }
    }
  }
  
  # Trace `getFromNamespace` for namespace calls (::)
  trace(utils::getFromNamespace, quote(log_namespace_calls()), print = FALSE)
  
  # Trace `library()` and `require()` calls to detect loaded packages
  trace(base::library, 
        quote(used_libraries <<- unique(c(used_libraries, as.character(sys.call()[[2]])))), 
        print = FALSE)
  trace(base::require, 
        quote(used_libraries <<- unique(c(used_libraries, as.character(sys.call()[[2]])))), 
        print = FALSE)
  
  # Save the original working directory and set the file's directory
  original_wd <- getwd()
  file_dir <- dirname(file)
  setwd(file_dir)
  
  # Source the R script and handle errors gracefully
  tryCatch({
    source(basename(file))  # Use only the filename, not the path
  }, error = function(e) {
    message("Error during sourcing: ", e$message)
  }, finally = {
    # Untrace all traced functions and restore the original working directory
    untrace(utils::getFromNamespace)
    untrace(base::library)
    untrace(base::require)
    setwd(original_wd)
  })
  
  # Return unique detected libraries
  result <- unique(get("used_libraries", envir = .GlobalEnv))
  
  # Clean up by removing the global variable
  rm("used_libraries", envir = .GlobalEnv)
  
  result
}

# Run the function on your script
result <- detect_libraries("/home/iffy/NetBeansProjects/NGSGradle/app/bin/counts.R")
print(result)
