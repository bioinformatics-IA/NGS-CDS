#!/usr/bin/env Rscript

# ==========================================================
#   ARGUMENTS
# ==========================================================
args <- commandArgs(trailingOnly = TRUE)
# args[1]= "/mnt/ntfs/Output_RNASeq1/"
# args[2]="/home/iffy/NetBeansProjects/NGSGradle/app/R_Libraries" # 2: Lib Path

bam_dir <- normalizePath(args[1])
user_lib <- normalizePath(args[2])

cat(" Reading files from Directory:", bam_dir, "\n")
plots_dir <- file.path(bam_dir, "Plots")
dir.create(plots_dir, showWarnings = FALSE, recursive = TRUE)
cat("Plots will be saved in:", plots_dir, "\n")


# ==========================================================
#   LIBRARIES
# ==========================================================
old_libs <- .libPaths()
.libPaths(c(user_lib, old_libs))

required_libs <- c( "Rsamtools", "GenomicAlignments", "IRanges", "tidyverse",
                    "ggplot2",  "dplyr","tidyr","data.table", "RColorBrewer", "Cairo")

cat("Loading libraries...\n")
for (pkg in required_libs) {
  suppressPackageStartupMessages(
    library(pkg, character.only = TRUE)
  )
}
cat("Libraries loaded successfully\n\n")

#-------------------------------------------------------------------------------
#   INPUT BAM FILES
# ==========================================================
bam_files <- list.files(bam_dir, pattern="\\.bam$", full.names=TRUE)

if (length(bam_files) == 0) {
  stop("No BAM files found in: ", bam_dir)
}
cat("Total BAM files:", length(bam_files), "\n")


# # ==========================================================
# #   GLOBAL COLLECTORS (ALL BAMs)
# # ==========================================================
# global_cum_depth <- list()
# global_cum_coverage <- list()
# yield_size <- 1e6
# # ==========================================================
# #   PROCESS EACH BAM
# # ==========================================================
# for (bam_file in bam_files) {
#   bam_name <- tools::file_path_sans_ext(basename(bam_file))
#   cat("\nProcessing:", bam_name, "\n")
#   cov_file <- file.path(dirname(bam_file),paste0(bam_name, "_coverage.rds"))
#   if (file.exists(cov_file)) {
#     cat("Coverage file exists. Loading saved coverage...\n")
#     cov_total <- readRDS(cov_file)
#   } else {
#   
#     cat("Opening BAM file...\n")
#         bf <- BamFile(bam_file, yieldSize = yield_size)
#   open(bf)
#   cov_total <- NULL
#   chunk_counter <- 0
#   repeat {
#     chunk_counter <- chunk_counter + 1
#     cat("Reading chunk:", chunk_counter, "\n")
#     gal <- readGAlignments(bf)
#     if (length(gal) == 0) {
#       cat("No more alignments. Breaking loop.\n")
#       break
#     }
#     cat("Alignments read:", length(gal), "\n")
#       chunk_cov <- coverage(gal)
#     if (is.null(cov_total)) {
#       cov_total <- chunk_cov
#     } else {
#       common <- intersect(names(cov_total), names(chunk_cov))
#       cov_total[common] <- cov_total[common] + chunk_cov[common]
#       
#     }
#       cat("Chunk", chunk_counter, "processed successfully.\n")
#   }
#   close(bf)
#   cat("BAM closed successfully.\n")
#   
#   cat("Saving coverage to disk...\n")
#   saveRDS(cov_total, cov_file)
#   cat("Coverage saved as:", cov_file, "\n")
#   }#else
#   
#   cat("Calculating depth distribution using Rle runs...\n")
#   
#   depth_values <- c()
#   depth_counts <- c()
#   for (chr in names(cov_total)) {
#     r <- cov_total[[chr]]
#     depth_values <- c(depth_values, runValue(r))
#     depth_counts <- c(depth_counts, runLength(r))
#   }
#   dt <- data.table(depth = depth_values, count = depth_counts)
#   dt <- dt[depth > 0]   # remove zero coverage
#   
#   cat("Total unique depth levels:", nrow(dt), "\n")
#   # =====================================================
#   # 1 CUMULATIVE DEPTH CURVE
#   # =====================================================
#   
#   dt_depth <- dt[order(-depth)]
#   dt_depth[, cumulative_positions := cumsum(count)]
#   dt_depth[, fraction_positions := cumulative_positions / sum(count)]
#   dt_depth[, bam := bam_name]
#   
#   dt_depth <- dt_depth[, .(fraction_positions = max(fraction_positions)), by = depth]
#   dt_depth[, bam := bam_name]
#   
#   global_cum_depth[[bam_name]] <- dt_depth
#   
#   # =====================================================
#   # 2 CUMULATIVE COVERAGE CURVE
#   # =====================================================
#   dt_cov <- dt[order(depth)]
#   dt_cov[, cumulative_fraction := cumsum(depth * count) / sum(depth * count)]
#   dt_cov[, bam := bam_name]
#   
#   dt_cov <- dt_cov[, .(cumulative_fraction = max(cumulative_fraction)), by = depth]
#   dt_cov[, bam := bam_name]
#   
#   global_cum_coverage[[bam_name]] <- dt_cov
#   cat("Finished processing:", bam_name, "\n")
# }
# #end-for
# 
# cat("Combining all BAM results...\n")
# global_cum_depth <- rbindlist(global_cum_depth)
# global_cum_coverage <- rbindlist(global_cum_coverage)
# 
# cat("All BAM files processed successfully.\n")
# cat("Depth rows:", nrow(global_cum_depth), "\n")
# cat("Coverage rows:", nrow(global_cum_coverage), "\n")
# 
# 
# # ==========================================================
# # PLOT 1: CUMULATIVE DEPTH
# # ==========================================================
# cat("Generating cumulative_depth_all_bams.png \n")
# p_depth<-ggplot(global_cum_depth,
#                 aes(x = depth, y = fraction_positions, color = bam)) +
#   geom_line() +
#   scale_x_continuous(trans="log10") +
#   theme_minimal(base_size = 14) +
#   labs(title="Cumulative Depth Curve (All BAMs)",
#        x="Depth (log scale)",
#        y="Fraction of Positions ≥ Depth")
# 
# CairoPNG(file = file.path(plots_dir, "cumulative_depth_all_bams.png"),
#          width = 14, height = 8, units = "in", res = 300)
# 
# print(p_depth)
# 
# 
# dev.off()
# 
# # ==========================================================
# # PLOT 2: CUMULATIVE COVERAGE
# # ==========================================================
# cat("Generating cumulative_coverage_all_bams.png \n")
# p_cov<-ggplot(global_cum_coverage,
#               aes(x = depth, y = cumulative_fraction, color = bam)) +
#   geom_line() +
#   theme_minimal(base_size = 14) +
#   labs(title="Cumulative Coverage Curve (All BAMs)",
#        x="Genome Position Index",
#        y="Cumulative Coverage Fraction")
# 
# CairoPNG(file = file.path(plots_dir, "cumulative_coverage_all_bams.png"),
#          width = 14, height = 8, units = "in", res = 300)
# 
# print(p_cov)
# 
# dev.off()
# 
# cat("Done. Plots saved in:", plots_dir, "\n")  
# stop("Quiting ")  



#--- Bam coverage code Ends
#-------------------------------------------------------------------------------


# PLOT Theme
qc_theme <- theme_bw(base_size = 12) +
  theme(
    plot.title = element_text(face = "bold", size = 14),    plot.subtitle = element_text(size = 11),
    axis.text.y = element_text(size = 10),    legend.position = "top",
    legend.title = element_text(face = "bold")  )

qc_theme_dense <- qc_theme +
  theme(    axis.text.y = element_text(size = 9),
            axis.text.x = element_text(angle = 90, hjust = 1, vjust = 0.5, size = 9),
    panel.grid.major.x = element_blank()  ,
    plot.margin = margin(t = 15, r = 15, b = 20, l = 20, unit = "pt")
    
    )

read_star_log <- function(file) {
  
  x <- read.delim(
    file,
    header = FALSE,
    sep = "|",
    stringsAsFactors = FALSE
  )
  
  colnames(x) <- c("metric", "value")
  x$metric <- trimws(x$metric)
  x$value  <- trimws(x$value)
  
  get_val <- function(name) {
    x$value[x$metric == name]
  }
  
  tibble(
    sample = sub("\\.star\\.Log\\.final\\.out$", "", basename(file)),
    
    # ADDED THIS LINE: Pulls the raw read depth from your STAR log file
    input_reads = as.numeric(get_val("Number of input reads")),
    
    uniquely_mapped_pct = as.numeric(gsub("%", "", get_val("Uniquely mapped reads %"))),
    splices_total = as.numeric(get_val("Number of splices: Total")),
    splices_annotated = as.numeric(get_val("Number of splices: Annotated (sjdb)")),
    multi_mapped_pct = as.numeric(gsub("%", "", get_val("% of reads mapped to multiple loci"))),
    unmapped_short_pct = as.numeric(gsub("%", "", get_val("% of reads unmapped: too short"))),
    mismatch_rate = as.numeric(gsub("%", "", get_val("Mismatch rate per base, %")))
  )
}




#Reading all files
log_files <- list.files(
  bam_dir,
  pattern = "Log.final.out$",
  full.names = TRUE
)
cat("Number of STAR log files found:", length(log_files), "\n")


if (length(log_files) == 0) {
  stop("No STAR Log.final.out files found in bam_dir. Check path or filename pattern.")
}

star_qc <- map_dfr(log_files, read_star_log)


# =========================
# Data for multi-category plots
# =========================

# For Graph 2: Splice comparison
star_qc_long <- star_qc %>%
  pivot_longer(
    cols = c(splices_total, splices_annotated),
    names_to = "splice_type",
    values_to = "count"
  ) %>%
  mutate(
    splice_type = factor(
      splice_type,
      levels = c("splices_total", "splices_annotated"),
        labels = c("Total splices", "Annotated (sjdb)")
    )
  )

# For Graph 4: Mapping composition
mapping_comp <- star_qc %>%
  select(
    sample,    uniquely_mapped_pct,    multi_mapped_pct,    unmapped_short_pct
  ) %>%
  pivot_longer(
    -sample,    names_to = "category",    values_to = "percent"
  ) %>%
  mutate(
    category = factor(      category,
      levels = c( "uniquely_mapped_pct",  "multi_mapped_pct", "unmapped_short_pct"
      ),
      labels = c(
        "Uniquely mapped reads", "Multi-mapped reads","Unmapped (too short)"
      )
    )
  )



sample_order <- star_qc %>% 
  arrange(uniquely_mapped_pct) %>% 
  pull(sample)

star_qc <- star_qc %>% mutate(sample = factor(sample, levels = sample_order))
star_qc_long <- star_qc_long %>% mutate(sample = factor(sample, levels = sample_order))
mapping_comp <- mapping_comp %>% mutate(sample = factor(sample, levels = sample_order))

cat("STAR QC table preview:\n")
print(head(star_qc))

# Graph 1: Uniquely mapped reads %
n_samples <- nrow(star_qc)

p1 <- ggplot(star_qc,
             aes(x = reorder(sample, uniquely_mapped_pct),
                 y = uniquely_mapped_pct,
                 fill = uniquely_mapped_pct)) +
  geom_col(width = 0.7) +
  #coord_flip() +
  scale_fill_gradient(
    low = "#d9f0a3",
    high = "#1b7837",
    name = "Mapping %"
  ) +
  labs(
    title = "Uniquely Mapped Reads Across Samples",
    subtitle = paste0(
      "Percentage of reads uniquely aligned by STAR (n = ",
      n_samples, " samples)"
    ),
    x = "Sample",
    y = "Uniquely mapped reads (%)",
    caption = "Each bar represents one RNA-seq sample. Lower values may indicate poor library or reference mismatch."
  ) +
  qc_theme_dense +
  theme(legend.position = "right")

CairoPNG(file.path(plots_dir, "01_uniquely_mapped_reads.png"),
         width = 14, height = 8, units = "in", res = 300)
print(p1)
invisible(dev.off())


#Graph 2: Total vs Annotated splices (combined bar chart)

p2 <- ggplot(star_qc_long,
             aes(x = sample, y = count, fill = splice_type)) +
  geom_col(position = position_dodge(width = 0.75), width = 0.65) +
  #coord_flip() +
  scale_fill_manual(
    values = c(
      "Total splices" = "#a1b5cb",
      "Annotated (sjdb)" = "#3f51b5"
    )
  ) +
  labs(
    title = "Splice Junction Detection per Sample",
    subtitle = "Comparison of total detected vs annotation-supported splice junctions",
    x = "Sample",
    y = "Number of splice junctions",
    fill = "Splice type",
    caption = "A high proportion of annotated splice junctions indicates good genome annotation compatibility."
  ) +
  qc_theme_dense

CairoPNG(file.path(plots_dir, "02_splicing_total_vs_annotated.png"),
         width = 14, height = 8, units = "in", res = 300)
print(p2)
invisible(dev.off())

# Graph 3: Fraction of annotated splices
star_qc <- star_qc %>%
  mutate(annotated_fraction = splices_annotated / splices_total * 100)


p3 <- ggplot(star_qc,
             aes(x = reorder(sample, annotated_fraction),
                 y = annotated_fraction,
                 fill = annotated_fraction)) +
  geom_col(width = 0.7) +
 # coord_flip() +
  
  # Reference threshold
  geom_hline(yintercept = 90,
             linetype = "dashed",
             color = "#b2182b",
             linewidth = 0.8) +
  
  # BLUE ↔ LIGHT YELLOW gradient
   scale_fill_gradientn(
    #colours = c("#fff7bc", "#7fcdbb", "#2c7fb8"),
     colours = c("#d0d1e6", "#74a9cf", "#0570b0"),
#    colours = c("#ece7f2", "#8c96c6", "#54278f"),
    limits = c(80, 100),
    oob = scales::squish,
    name = "Annotated %"
  ) +
   labs(
    title = "Fraction of Annotated Splice Junctions",
    subtitle = "Proportion of splice junctions supported by gene annotation",
    x = "Sample",
    y = "Annotated splice junctions (%)",
    caption = paste(
      "Each bar represents one sample.",
      "Color intensity reflects annotation support.",
      "Dashed line marks 90% threshold."
    )
  ) +
  
  qc_theme_dense +
  theme(legend.position = "right")

CairoPNG(
  file.path(plots_dir, "03_annotated_splice_fraction.png"),
  width = 14, height = 8, units = "in", res = 300
)
print(p3)
invisible(dev.off())


# Graph 4: Mapping composition
p4 <- ggplot(mapping_comp,
             aes(x = sample, y = percent, fill = category)) +
  geom_col(width = 0.75) +
#  coord_flip() +
  scale_fill_manual(
    values = c(
      "Uniquely mapped reads" = "#4daf4a",
      "Multi-mapped reads" = "#377eb8",
      "Unmapped (too short)" = "#e41a1c"
    )
  ) +
  labs(
    title = "Read Mapping Composition per Sample",
    subtitle = "Proportion of uniquely mapped, multi-mapped, and unmapped reads",
    x = "Sample",
    y = "Percentage of reads",
    fill = "Read category",
    caption = "High uniquely mapped fraction indicates good RNA-seq alignment quality."
  ) +
  qc_theme_dense

CairoPNG(file.path(plots_dir, "04_mapping_composition.png"),
         width = 14, height = 8, units = "in", res = 300)
print(p4)
invisible(dev.off())

p5 <- ggplot(star_qc, aes(x = sample, y = input_reads / 1e6)) +
  geom_col(fill = "#41b6c4", width = 0.7) +
#  coord_flip() +
  labs(
    title = "Raw Sequencing Depth per Sample",
    subtitle = "Total raw read pairs delivered by the sequencer",
    x = "Sample", y = "Millions of Reads"
  ) + qc_theme_dense


CairoPNG(file.path(plots_dir, "05_raw_sequencing_depth.png"),
         width = 14, height = 8, units = "in", res = 300)
print(p5)
invisible(dev.off())

p6 <- ggplot(star_qc, aes(x = sample, y = mismatch_rate, fill = mismatch_rate)) +
  geom_col(width = 0.7) +
#  coord_flip() +
  scale_fill_gradient(low = "#fee0d2", high = "#de2d26", name = "Mismatch %") +
  geom_hline(yintercept = 1.0, linetype = "dashed", color = "darkred") + # Normal is < 1%
  labs(
    title = "Per-Base Alignment Mismatch Rate",
    subtitle = "Percentage of aligned bases that do not match the reference genome",
    x = "Sample", y = "Mismatch Rate (%)"
  ) + qc_theme_dense+
  theme(legend.position = "right")


CairoPNG(file.path(plots_dir, "06_per_base_mismatch_rate.png"),
         width = 14, height = 8, units = "in", res = 300)
print(p6)
invisible(dev.off())

  cat("Plots Generated:\n01_uniquely_mapped_reads.png\n02_splicing_total_vs_annotated.png
      \n03_annotated_splice_fraction.png\n04_mapping_composition.png
      \n05_raw_sequencing_depth.png, \n06_per_base_mismatch_rate.png,
      ")

