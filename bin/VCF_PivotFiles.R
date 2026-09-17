#!/usr/bin/env Rscript

myargs = commandArgs(trailingOnly=TRUE)
# #   
# myargs[1]="/home/iffy/PhD_Data/CASESTUDY/1-Hepatocellular_carcinoma_100/WES_PRJNA866195/VCF_WESsNew/"   # 1: Output Directory
# myargs[2]="/home/iffy/NetBeansProjects/NGSGradle/app/R_Libraries" # 2: Lib Path
# myargs[3]= "/home/iffy/PhD_Data/CASESTUDY/1-Hepatocellular_carcinoma_100/WES_PRJNA866195/VCF_WESsNew/mergedLong_annH_sift_all.tsv"
# myargs[4]="markdownfile path"
# Parse target TSV argument
target_tsv <- myargs[3]

# Extract filename (e.g., "mergedLong_annH_sift_all.tsv")
tsv_filename <- basename(target_tsv)

# Remove extension (e.g., "mergedLong_annH_sift_all")
tsv_name_no_ext <- tools::file_path_sans_ext(tsv_filename)

# Extract suffix after 'mergedLong_' (e.g., "annH_sift_all")
suffix <- sub("^mergedLong_", "", tsv_name_no_ext)

setwd(file.path(myargs[1]))
plots_dir_name <- paste0("plots_", suffix)
plots_dir <- file.path(getwd(), plots_dir_name )
dir.create(plots_dir, showWarnings = FALSE, recursive = TRUE)
# -------------------------------

#   Load Libraries
# -------------------------------
old_libraries <- .libPaths()
.libPaths(c(myargs[2], old_libraries))

required_libs <- c("tidyverse", "readr", "scales", "pheatmap", "RColorBrewer","Cairo",
                   "matrixStats","stringr","purrr","dplyr","ComplexHeatmap","circlize","grid",
                   "png", "ggrepel","tidyr","openxlsx",
                   "rmarkdown", "tinytex", "tools" #Packages for pdf report
                   )



for (pkg in required_libs) {
  suppressPackageStartupMessages(library(pkg, character.only = TRUE))
}

# =============================================================================
# Load Data
# =============================================================================
cat("Loading mutation annotation file...\n")
df <- read_tsv(myargs[3])

cat("Total variants loaded:", nrow(df), "\n")
str(df)

cat("Generating distinct genomic position tokens (Mutation_ID)...\n")
df <- df %>%
  mutate(
    Mutation_ID = paste(Gene_Name, CHROM, POS, REF, ALT, sep = ":")
  )

cat("Calculating cohort population parameters...\n")
total_patients <- n_distinct(df$Sample)
cat("Total unique baseline samples identified in cohort:", total_patients, "\n\n")

# =============================================================================
# DATA SUMMARIES
# =============================================================================

# ---- 1.1 Mutation count per sample ----
# Per Sample Mutation Total Count....Which samples are hypermutated?
cat("Calculating total mutation burden per sample...\n")

per_sample_count <- df %>%
  dplyr::count(Sample, name = "Mutation_Count")%>%
  dplyr::arrange(desc(Mutation_Count))

# ---- 1.2. Gene × Sample mutation matrix ----
cat("Building gene × sample mutation matrix...\n")
gene_sample_matrix <- df %>%
  distinct(Gene_Name, Sample) %>%
  dplyr::count(Gene_Name, Sample) %>%
  pivot_wider(
    names_from = Sample,
    values_from = n,
    values_fill = 0
  )



#-------------------------------------------------------------------------------
# 2.1: HEAVILY MUTATED GENES (Regardless of Impact)
# =============================================================================
# ---- 2.1 Overall Gene Penetrance ----
cat("Extracting Heavily Mutated Genes...\n")
gene_penetrance <- df %>%
  group_by(Gene_Name) %>%
  summarise(Mutated_Samples = n_distinct(Sample), Total_Variants = n(), .groups = 'drop') %>%
  select(Gene_Name, Mutated_Samples, Total_Variants) %>%
  arrange(desc(Mutated_Samples), desc(Total_Variants))

# ---- 2.2 Functional Annotation Counts Per Gene (Wide Format) ----
#Pivot-style table (genes × mutation types)
mutation_types_gene_wide <- df %>%
  dplyr::count(Gene_Name, Annotation) %>%
  pivot_wider(
    names_from = Annotation,
    values_from = n,
    values_fill = 0
  )

# Mutation types in each gene 
#Are mutations in this gene mostly missense, nonsense, splice, etc.?
# ---- 2.3 Mutation types per gene ----
cat("Summarizing mutation annotation per gene...\n")
mutation_types_gene <- df %>%
  dplyr::count(Gene_Name,Annotation_Impact,Annotation,name = "Variant_Count")

# ---- 2.4. High / moderate impact mutations ----
cat("Extracting high and moderate impact mutations...\n")
high_impact <- df %>%
  filter(Annotation_Impact %in% c("HIGH", "MODERATE")) %>%
  dplyr::count(Gene_Name, Annotation_Impact, Annotation, name = "Variant_Count")

#-------------------------------------------------------------------------------
# ---- 3.1 Unfiltered Master Registry (All Mutational Events) ----

cat("Creating a comprehensive registry of all individual mutational events...\n")

all_mutational_events <- df %>%
  # Group by the specific columns you want to keep
  group_by(Mutation_ID, Gene_Name, Annotation, Annotation_Impact) %>%
  # Collapse rows and calculate the total count
  summarise(Total_Records = n(),Unique_Samples = n_distinct(Sample),
            Avg_Occurrences_Per_Sample = round(n() / n_distinct(Sample), 2),
            .groups = 'drop') %>% 
  # Sort so that the most common mutational events are at the top
  arrange(desc(Total_Records), Gene_Name)

# ---- 3.2 Genomic Frequencies (Distinct Mutation Summary) ----
# 3.2: ALL GENOMIC MUTATIONS (Frequencies, Type, Impact)
# =============================================================================
# Which mutations are recurrent and are in how many samples(All mutations + All impacts) 
# Counting how many unique samples/patients have a specific mutation.
cat("Extracting genomic frequencies: Counting unique samples per mutation with annotation metadata...\n")
genomic_frequencies <- df %>%
  group_by(Mutation_ID, Gene_Name, Annotation, Annotation_Impact) %>%
  summarise(Sample_Count = n_distinct(Sample), .groups = 'drop') %>%
  arrange(desc(Sample_Count))


# ---- 3.3 Filtered High/Moderate Protein Prevalence (HGVS) ----

# STEP A: Generate the COMPLETE High/Moderate Impact Dataset
protein_prevalence_all <- df %>%
  # Handle case-sensitivity issues dynamically
  mutate(Impact_Clean = toupper(Annotation_Impact)) %>%
  filter(Impact_Clean %in% c("HIGH", "MODERATE")) %>%
    # Strip missing values or placeholder symbols from critical columns
  filter(!is.na(Gene_Name) & Gene_Name != "." & !is.na(HGVS.p) & HGVS.p != "") %>% 
    # Group and collapse to unique patients and capture all corresponding genomic coordinates
  group_by(Gene_Name, HGVS.p, Impact_Clean) %>%
  summarise(
    Sample_Count = n_distinct(Sample),
    Genomic_Coordinates = paste(unique(Mutation_ID), collapse = "; "),
    .groups = "drop"
  ) %>%
    # Sort the entire dataset from highest frequency down to lowest
  arrange(desc(Sample_Count))

# ---- 3.4 Unfiltered Protein Prevalence (All-Impact HGVS) ----
# UNFILTERED ALL IMPACTS HGVS
cat("Generating All-Impact HGVS Dataset...\n")
protein_prevalence_unfiltered_all <- df %>%
  filter(!is.na(Gene_Name) & Gene_Name != "." & !is.na(HGVS.p) & HGVS.p != "" & HGVS.p != ".") %>% 
  group_by(Gene_Name, HGVS.p, Annotation_Impact) %>%
  summarise(
    Sample_Count = n_distinct(Sample),
    Genomic_Coordinates = paste(unique(Mutation_ID), collapse = "; "),
    .groups = "drop"
  ) %>%
    arrange(desc(Sample_Count), desc(Gene_Name))

# =============================================================================
# CATEGORY 4: COMPREHENSIVE VARIANT METRICS (WITH GENE-LEVEL TOTALS)
# =============================================================================
cat("Building Comprehensive Variant-Level Summary Sheet with Gene Totals...\n")

comprehensive_variant_summary <- df %>%
  filter(!is.na(Gene_Name) & Gene_Name != ".") %>%
  # 1. First calculate gene-level total unique sample count across the whole cohort
  group_by(Gene_Name) %>%
  mutate(Gene_Mutated_Samples_Total = n_distinct(Sample)) %>%
  ungroup() %>%
  
  # 2. Now group by specific variant attributes
  group_by(
    Gene_Name, 
    Gene_Mutated_Samples_Total,
    Annotation, 
    Annotation_Impact, 
    Mutation_ID, 
    HGVS.p
  ) %>%
  # 3. Aggregate metrics at the specific variant level
  summarise(
    Variant_Mutated_Samples = n_distinct(Sample),
    Variant_Record_Count    = n(),
    .groups = 'drop'
  ) %>%
  rename(Genomic_Coordinates = Mutation_ID) %>%
  
  # 4. Order columns clearly for easy reading
  select(
    Gene_Name, 
    Gene_Mutated_Samples_Total,   # Gene-level cohort total (e.g., 8)
    Annotation, 
    Annotation_Impact, 
    Variant_Mutated_Samples,      # Variant-level cohort total (e.g., 2)
    Variant_Record_Count, 
    Genomic_Coordinates, 
    HGVS.p
  ) %>%
  # Sort by top mutated genes, then top variants within those genes
  arrange(desc(Gene_Mutated_Samples_Total), desc(Variant_Mutated_Samples), Gene_Name)

#filter_log_file <- file.path(myargs[1], "filtration_counts.csv")

output_dir   <- myargs[1]
target_tsv   <- myargs[3] # e.g., ".../mergedLong_vep_annH_sift_all.tsv"
filter_csv   <- file.path(output_dir, "filtration_counts.csv")

# Detect run type from TSV filename
active_stage <- NULL
if (grepl("_vep_annH_sift_all\\.tsv$", target_tsv)) {   active_stage <- "Germline_VEP" } else if (grepl("_annH_sift_all\\.tsv$", target_tsv)) {
  active_stage <- "Germline_SnpEff"
} else if (grepl("_vep_annM_sift_all\\.tsv$", target_tsv)) {   active_stage <- "Somatic_VEP" } else if (grepl("_annM_sift_all\\.tsv$", target_tsv)) {
  active_stage <- "Somatic_SnpEff"
}

# Extract relevant filtration counts for the RMarkdown report
filter_data_current <- NULL

if (file.exists(filter_csv) && !is.null(active_stage)) {
  raw_counts <- read.csv(filter_csv, stringsAsFactors = FALSE, check.names = FALSE)
  
  # Filter sample rows and the specific sub-total for THIS active stage
  filter_data_current <- raw_counts %>%
    filter(Pipeline_Stage == active_stage | Sample == paste0("TOTAL_", active_stage))
}

print ("Correction Done: ")
print(filter_data_current)
#print(active_stage)

# Save the required data structures for the Rmd report
save(
  df,total_patients, per_sample_count, gene_penetrance, genomic_frequencies,
   mutation_types_gene, high_impact, 
  gene_sample_matrix, protein_prevalence_all,protein_prevalence_unfiltered_all,
  filter_data_current, active_stage, plots_dir,
  file = file.path(myargs[1], "report_data.RData")
)

#-------------------------------------------------------------------------------
# =============================================================================
# EXCEL SHEET COMPILATION (Building the Master File)
# =============================================================================

cat("Compiling consolidated Excel workbook...\n")
wb <- createWorkbook()

# Define Professional Styles (Navy Theme)
header_style <- createStyle(fontName = "Segoe UI", fontSize = 11, fontColour = "#FFFFFF",
                            fgFill = "#1B365D", halign = "center", textDecoration = "bold")
body_style   <- createStyle(fontName = "Segoe UI", fontSize = 10, halign = "left")
pct_style    <- createStyle(fontName = "Segoe UI", fontSize = 10, numFmt = "0.00%", halign = "right")

# =============================================================================
# CATEGORY 1: SAMPLE & COHORT OVERVIEWS
# =============================================================================
# --- Tab 1: Mutation Burden ---
addWorksheet(wb, "1.Mutation Burden")
writeData(wb, "1.Mutation Burden", per_sample_count)
addStyle(wb, "1.Mutation Burden", style = header_style, rows = 1, cols = 1:ncol(per_sample_count))

# --- Tab 2: Gene x Sample Matrix (Perfect for OncoPrints) ---
addWorksheet(wb, "2.Gene-Sample Matrix")
writeData(wb, "2.Gene-Sample Matrix", gene_sample_matrix)
addStyle(wb, "2.Gene-Sample Matrix", style = header_style, rows = 1, cols = 1:ncol(gene_sample_matrix))

# CATEGORY 2: GENE-LEVEL ANALYSES
# =============================================================================

# --- Tab 3: Heavily Mutated Genes ---
addWorksheet(wb, "3. Heavily Mutated Genes")
writeData(wb, "3. Heavily Mutated Genes", gene_penetrance)
addStyle(wb, "3. Heavily Mutated Genes", style = header_style, rows = 1, cols = 1:ncol(gene_penetrance))

# --- Tab 4: Mutation Types Per Gene (Long Format) ---
addWorksheet(wb, "4. Mutation Types (Long)")
writeData(wb, "4. Mutation Types (Long)", mutation_types_gene)
addStyle(wb, "4. Mutation Types (Long)", style = header_style, rows = 1, cols = 1:ncol(mutation_types_gene))

# --- Tab 5: Mutation Types Per Gene (Wide Pivot Layout) ---
addWorksheet(wb, "5. Mutation Types (Wide)")
writeData(wb, "5. Mutation Types (Wide)", mutation_types_gene_wide)
addStyle(wb, "5. Mutation Types (Wide)", style = header_style, rows = 1, cols = 1:ncol(mutation_types_gene_wide))

# --- Tab 6: High and Moderate Structural Impacts ---
addWorksheet(wb, "6.Mutation Impact Summary")
writeData(wb, "6.Mutation Impact Summary", high_impact)
addStyle(wb, "6.Mutation Impact Summary", style = header_style, rows = 1, cols = 1:ncol(high_impact))

# CATEGORY 3: MUTATION & VARIANT-LEVEL ANALYSES
# =============================================================================

# --- Tab 7: Master Mutational Registry (All Individual Events) ---
addWorksheet(wb, "7. All Mutational Events")
writeData(wb, "7. All Mutational Events", all_mutational_events)
addStyle(wb, "7. All Mutational Events", style = header_style, rows = 1, cols = 1:ncol(all_mutational_events))

# --- Tab 8: Mutation Sample Frequencies ---
# --- Tab 8: Genomic Frequencies ---
addWorksheet(wb, "8. Genomic Frequencies")
writeData(wb, "8. Genomic Frequencies", genomic_frequencies)
addStyle(wb, "8. Genomic Frequencies", style = header_style, rows = 1, cols = 1:ncol(genomic_frequencies))

# --- Tab 9: Functional Alterations (High/Moderate HGVS Protein Changes) ---
addWorksheet(wb, "9. High-Mod Protein Alterations")
writeData(wb, "9. High-Mod Protein Alterations", protein_prevalence_all)
addStyle(wb, "9. High-Mod Protein Alterations", style = header_style, rows = 1, cols = 1:ncol(protein_prevalence_all))

# --- Tab 10: Unfiltered Protein Prevalence (All-Impact HGVS) ---
addWorksheet(wb, "10. All Protein Alterations")
writeData(wb, "10. All Protein Alterations", protein_prevalence_unfiltered_all)
addStyle(wb, "10. All Protein Alterations", style = header_style, rows = 1, cols = 1:ncol(protein_prevalence_unfiltered_all))

# --- Tab 11: Detailed Variant Summary (Requested Sheet) ---
addWorksheet(wb, "11. Detailed Variant Summary")
writeData(wb, "11. Detailed Variant Summary", comprehensive_variant_summary)
addStyle(wb, "11. Detailed Variant Summary", style = header_style, rows = 1, cols = 1:ncol(comprehensive_variant_summary))

# --- Final Layout Tuning ---
# Auto-adjust column tracking spacing across every tab cleanly
for(sheet in names(wb)){
  setColWidths(wb, sheet, cols = 1:ncol(df), widths = "auto")
}

#--------------------------------
# WRITTEN NARRATIVE PDF REPORT COMPILATION
# =============================================================================
cat("Compiling narrative written analysis into a PDF report...\n")
# Extract raw file basename (e.g., "mergedLong_annH_sift_all")
raw_filename <- tools::file_path_sans_ext(basename(myargs[3]))
# Clean name: remove "mergedLong_" prefix and "_sift_all" / "_all" suffix
clean_tag <- raw_filename %>%
  sub("^mergedLong_", "", .) %>%      # Remove prefix
  sub("_sift_all$", "", .) %>%        # Remove _sift_all suffix
  sub("_all$", "", .)                 # Fallback in case suffix is just _all


# Save consolidated workbook
excel_output_name <- paste0("Genomic_Cohort_Report_", clean_tag, ".xlsx")
saveWorkbook(wb, file.path(myargs[1], excel_output_name), overwrite = TRUE)

cat("Success: All tracking metrics exported into single unified workbook: Genomic_Cohort_Report_", clean_tag, ".xlsx\n", sep = "")


#-------------------------------------------------------------------------------
#----------------------------PLOTS----------------------------------------------
priority <- c(
  "stop_gained",
  "frameshift_variant",
  "splice_acceptor_variant",
  "splice_donor_variant",
  "start_lost",
  "missense_variant",
  "disruptive_inframe_insertion",
  "disruptive_inframe_deletion",
  "conservative_inframe_insertion",
  "conservative_inframe_deletion",
  "splice_region_variant",
  "start_retained_variant",
  "synonymous_variant",
  "5_prime_UTR_premature_start_codon_gain_variant",#
  "5_prime_UTR_variant",#
  "3_prime_UTR_variant",#
  "non_coding_transcript_exon_variant",
  "intron_variant",#
  "intragenic_variant",#
  "upstream_gene_variant",
  "downstream_gene_variant",
  "intergenic_region"#
)
drop_terms <- c(
  "3_prime_UTR_variant",
  "5_prime_UTR_premature_start_codon_gain_variant",
  "5_prime_UTR_variant",
  "intergenic_region",
  "intragenic_variant",
  "intron_variant",
  "synonymous_variant",
  "upstream_gene_variant",
  "."
)

get_top_consequence <- function(annotation) {
  terms <- str_split(annotation, "&")[[1]]

  ranked <- match(terms, priority)
  terms[which.min(ifelse(is.na(ranked), Inf, ranked))]
}

# -----------------------------------------------------------------------------
#                                   SAMPLE BURDEN
# -----------------------------------------------------------------------------
# -----------------------------------------------------------------------------
# Plot 1: Mutation burden per sample (BAR)
# -----------------------------------------------------------------------------

cat("Generating plot 01: Mutation burden per sample...\n")
# Sort by Mutation_Count (descending)
n_samples <- nrow(per_sample_count)
p_mutation_burden <- ggplot(per_sample_count,
                            aes(x = reorder(Sample, Mutation_Count),y = Mutation_Count,
                                fill = Mutation_Count)) +
  geom_col(width = 0.7) +
  coord_flip() +
  scale_fill_gradient(low = "#d9f0a3",high = "#1b7837",  name = "Total Variants") +
  labs(  title = "Mutational Burden Profile across Cohort",
    subtitle = paste0("Distribution of total variant counts quantified across ", n_samples, " samples"),
    x = "Sample ID",   y = "Number of Mutations",
    caption = "Color intensity indicates mutation burden per sample") +
  theme_bw(base_size = 14) +
  theme(  legend.position = "right", axis.text.y = element_text(size = 7, margin = margin(t = 5, b = 5)),
    plot.title = element_text(face = "bold"),
    plot.subtitle = element_text(size = 12)
  )

CairoPNG(file.path(plots_dir, "01-Per_sample_mutation_count.png"), width = 14, height = 8, units = "in", res = 300)
print(p_mutation_burden)
invisible(dev.off())

# -----------------------------------------------------------------------------
# Plot 2: Mutation burden per sample (LINE)
# -----------------------------------------------------------------------------
cat("Generating plot 02: Mutation burden trend line...\n")
#per_sample_count_sorted <- per_sample_count %>% arrange(desc(Mutation_Count))

p_trend <- ggplot(per_sample_count, aes(x = reorder(Sample, -Mutation_Count), y = Mutation_Count, group = 1)) +
  geom_line(color = "#1f78b4", linewidth = 1) +
  geom_point(color = "#1f78b4", size = 3) +
  labs(
    title = "Mutation Burden Stratification Spectrum",
    subtitle = "Rank-ordered exponential distribution plot highlighting hypermutated sample anomalies",
    x = "Cohort Samples (Ranked High to Low)",
    y = "Total Quantified Mutations",
    caption = "This curve illustrates the drop-off rate of mutations across the dataset, making it easy to isolate heavily mutated outlier samples on the far left."
  ) +
  theme_bw(base_size = 14) +
  theme(
    axis.text.x = element_text(angle = 90, hjust = 1, vjust = 0.5, size = 8),
    plot.title = element_text(face = "bold")
  )

CairoPNG(file.path(plots_dir, "02-Mutation_burden_per_sample_line.png"), width = 14, height = 7, units = "in", res = 300)
print(p_trend)
invisible(dev.off())

# -----------------------------------------------------------------------------
#                                   RECCURRENT MUTATIONS
# -----------------------------------------------------------------------------
# 3-top_recurrent_all_mutations.png
# -----------------------------------------------------------------------------
top_mutations <- df %>%
  dplyr::count(Mutation_ID) %>%
  arrange(desc(n)) %>%
  slice_head(n = 50) %>%
  select(Mutation_ID)

mutation_impacts <- df %>%
  semi_join(top_mutations, by = "Mutation_ID") %>%
  # Convert to uppercase to prevent case-sensitivity mismatches ("High" vs "HIGH")
  mutate(Impact_Clean = toupper(Annotation_Impact)) %>%
  mutate(Impact_Weight = case_when(
    Impact_Clean == "HIGH" ~ 2,
    Impact_Clean == "MODERATE" ~ 1,
    TRUE ~ 0
  )) %>%
  group_by(Mutation_ID) %>%
  # Isolate the most severe impact tier seen at this genomic coordinate
  filter(Impact_Weight == max(Impact_Weight)) %>%
  slice(1) %>%
  select(Mutation_ID, Max_Impact = Impact_Clean)

mutation_totals <- df %>%
  semi_join(top_mutations, by = "Mutation_ID") %>%
  dplyr::count(Mutation_ID, name = "total") %>%
  arrange(total) %>%
  mutate(
    Mutation_ID = factor(Mutation_ID, levels = Mutation_ID),
    total_label = as.character(total)
  )
# Attach the resolved impact data to your totals data frame
mutation_totals_outlined <- mutation_totals %>%
  left_join(mutation_impacts, by = "Mutation_ID")

cat("Generating Plot 03: top_recurrent_all_mutations.png ...\n")

plot_df <- df %>%
  semi_join(top_mutations, by = "Mutation_ID") %>%
  dplyr::count(Mutation_ID, Annotation) %>%
  group_by(Mutation_ID) %>%
  mutate(num_segments = n()) %>%
  ungroup() %>%

  # Sync factor levels to match the ordered leaderboard
  mutate(Mutation_ID = factor(Mutation_ID, levels = levels(mutation_totals$Mutation_ID))) %>%
  # Optional: Hide labels for tiny stacks (e.g., < 2) to prevent visual overlapping
  mutate(seg_label = ifelse(num_segments >= 2 & n >= 1, as.character(n), ""))
 # mutate(seg_label = ifelse(n >= 1, as.character(n), ""))

cols <- colorRampPalette(brewer.pal(12, "Paired"))(
  length(unique(plot_df$Annotation))
)

CairoPNG(  file = file.path(plots_dir, "03-top_recurrent_all_mutations.png"),
  width = 14, height = 8, units = "in", res = 300)

ggplot(plot_df, aes(x = Mutation_ID, y = n)) +
  # LAYER 1: Core stacked colors
  geom_col(aes(fill = Annotation), color = "white", width = 0.8) +

  # LAYER 2: Overlay a thick black DOT-DASH border for HIGH impact mutations
  geom_col(
    data = filter(mutation_totals_outlined, Max_Impact == "HIGH"),
    aes(y = total),
    fill = NA, color = "black", linetype = "dotdash", linewidth = 0.9, width = 0.8
  ) +

  # LAYER 3: Overlay a charcoal DASHED border for MODERATE impact mutations
  geom_col(
    data = filter(mutation_totals_outlined, Max_Impact == "MODERATE"),
    aes(y = total),
    fill = NA, color = "royalblue", linetype = "dashed", linewidth = 0.7, width = 0.8
  ) +

  # LAYER 4: Internal segment counts (Silent error handling built-in)
  geom_text(
    aes(label = seg_label, group = Annotation),
    position = position_stack(vjust = 0.5),
    size = 3.2, fontface = "bold", color = "black", na.rm = TRUE
  ) +

  # LAYER 5: Total counts placed cleanly outside the bar edges
  geom_text(
    data = mutation_totals_outlined,
    aes(x = Mutation_ID, y = total, label = total_label),
    inherit.aes = FALSE, hjust = -0.3, size = 3.5, fontface = "plain"
  ) +
  scale_x_discrete(labels = function(x) stringr::str_trunc(x, width = 40, side = "right")) +
  scale_fill_manual(values = cols) +
  guides(fill = guide_legend(nrow = 3, byrow = TRUE)) +
  labs(
    title = "Global Landscape of Top 50 Recurrent Genomic Mutations",
    subtitle = "Global cohort hotspots (coding & non-coding). Borders prioritize functional context:\nBlack Dot-Dash = HIGH Impact; Blue Dashed = MODERATE Impact.",
    x = "Mutation Locus (Gene : Chromosome : Position : Ref : Alt)",
    y = "Mutation Frequency (Sample Count)",
    fill = "Mutation Consequence"
  ) +
  theme_bw(base_size = 13) +
  theme(
    legend.position = "top",
    legend.title = element_text(size = 10, face = "bold"),
    legend.text = element_text(size = 9),
    legend.key.size = unit(0.4, "cm"),
    legend.spacing.x = unit(0.1, "cm"),                    # Bring items closer horizontally
    legend.spacing.y = unit(0.05, "cm"),                   # Bring rows closer vertically
    legend.box.spacing = unit(0.1, "cm"),
    legend.margin = margin(t = 2, r = 2, b = 4, l = 2, unit = "pt"),

    axis.text.y = element_text(size = 9, face="bold"),
    axis.text.x = element_text(size = 10),
    plot.title = element_text(face = "bold", size = 15),
    plot.subtitle = element_text(size = 11, color = "gray30"),

    plot.margin = margin(t = 10, r = 55, b = 10, l = 10, unit = "pt")
  ) +
  coord_flip(clip = "off")

invisible(dev.off())



cat("Generating plot 04: top_recurrent_coding_mutations.png...\n")
filtered_df_all_impacts <- df %>%
  filter(!Annotation %in% drop_terms)

# 3. Find the true top 50 coding mutations across all impact levels
top_coding_all_impacts <- filtered_df_all_impacts %>%
  group_by(Mutation_ID) %>%
  summarise(True_Sample_Count = n_distinct(Sample), .groups = "drop") %>%
  slice_max(True_Sample_Count, n = 50, with_ties = FALSE)

# 4. Build the primary stacking dataset for these top 50 loci
plot_df_7 <- filtered_df_all_impacts %>%
  semi_join(top_coding_all_impacts, by = "Mutation_ID") %>%
  mutate(
    Annotation = ifelse(
      Annotation %in% c(".", "intergenic_variant"),
      "Other / Unannotated",
      Annotation
    )
  ) %>%
  group_by(Mutation_ID, Annotation) %>%
  summarise(n = n_distinct(Sample), .groups = "drop")

# Identify mutations with multiple overlapping annotation consequences
multi_cons_7 <- plot_df_7 %>%
  dplyr::count(Mutation_ID) %>%
  filter(n > 1)

plot_df_7 <- plot_df_7 %>%
  mutate(
    seg_label = ifelse(
      Mutation_ID %in% multi_cons_7$Mutation_ID,
      paste0(n, "*"),
      NA_character_
    )
  )

# 5. Calculate cumulative lengths to position total labels flawlessly
mutation_totals_7 <- plot_df_7 %>%
  group_by(Mutation_ID) %>%
  summarise(physical_total = sum(n), .groups = "drop") %>%
  left_join(top_coding_all_impacts, by = "Mutation_ID") %>%
  mutate(
    total_label = ifelse(
      Mutation_ID %in% multi_cons_7$Mutation_ID,
      paste0("(", True_Sample_Count, "*)"),
      paste0("(", True_Sample_Count, ")")
    )
  )
mutation_impacts_7 <- filtered_df_all_impacts %>%
  semi_join(top_coding_all_impacts, by = "Mutation_ID") %>%
  mutate(Impact_Clean = toupper(Annotation_Impact)) %>%
  mutate(Impact_Weight = case_when(
    Impact_Clean == "HIGH" ~ 2,
    Impact_Clean == "MODERATE" ~ 1,
    TRUE ~ 0
  )) %>%
  group_by(Mutation_ID) %>%
  filter(Impact_Weight == max(Impact_Weight)) %>%
  slice(1) %>%
  select(Mutation_ID, Max_Impact = Impact_Clean)

mutation_totals_7 <- mutation_totals_7 %>%
  left_join(mutation_impacts_7, by = "Mutation_ID")
# 6. Synchronize factor levels to sort from lowest frequency to highest frequency
factor_levels_7 <- top_coding_all_impacts %>%
  arrange(True_Sample_Count) %>%
  pull(Mutation_ID)

plot_df_7$Mutation_ID <- factor(plot_df_7$Mutation_ID, levels = factor_levels_7)
mutation_totals_7$Mutation_ID <- factor(mutation_totals_7$Mutation_ID, levels = factor_levels_7)

# 7. Generate color palette matching the remaining variations
cols_7 <- colorRampPalette(brewer.pal(12, "Paired"))(
  length(unique(plot_df_7$Annotation))
)

# 8. Render the Chart (No coord_flip required)
cat("Generating Plot 04: top_recurrent_coding_mutations.png ...\n")

CairoPNG(
  file = file.path(plots_dir, "04-top_recurrent_coding_mutations.png"),
  width = 14, height = 8, units = "in", res = 300
)

ggplot(plot_df_7, aes(x = n, y = Mutation_ID)) +
  geom_col(aes(fill = Annotation), color = "white", width = 0.8) +
  # LAYER 2: Overlay black DOT-DASH border for HIGH impact
  geom_col(
    data = filter(mutation_totals_7, Max_Impact == "HIGH"),
    aes(x = physical_total, y = Mutation_ID),
    fill = NA, color = "black", linetype = "dotdash", linewidth = 0.9, width = 0.8,
    inherit.aes = FALSE
  ) +
  # LAYER 3: Overlay royalblue DASHED border for MODERATE impact
  geom_col(
    data = filter(mutation_totals_7, Max_Impact == "MODERATE"),
    aes(x = physical_total, y = Mutation_ID),
    fill = NA, color = "royalblue", linetype = "dashed", linewidth = 0.7, width = 0.8,
    inherit.aes = FALSE
  ) +

  # Internal segment text overlays
  geom_text(
    aes(label = seg_label, group = Annotation),
    position = position_stack(vjust = 0.5),
    size = 3.2,    fontface = "bold",
    color = "black",
    na.rm = TRUE
  ) +

  # External text totals right past the bar ends
  geom_text(
    data = mutation_totals_7,
    aes(x = physical_total, y = Mutation_ID, label = total_label),
    inherit.aes = FALSE,
    hjust = -0.1,
    size = 3.2,
    fontface = "plain"
  ) +
  scale_fill_manual(
    values = cols_7,
    guide = guide_legend(ncol = 3, byrow = TRUE)
  ) +
  labs(
    title = "Top 50 Recurrent Coding Mutations ",
    subtitle = "Ranked by unique sample frequency. Excludes non-coding/silent regions.\n
    Borders prioritize functional context: Black Dot-Dash = HIGH Impact Blue Dashed = MODERATE Impact",
    x = "Mutation Frequency (Sample Count)",
    y = "Mutation Locus (Gene : Chromosome : Position : Ref : Alt)",
    fill = "Mutation Consequence Type"
  ) +
  theme_bw(base_size = 13) +
  theme(
    legend.position = "top",
    legend.title = element_text(size = 10, face = "bold"),
    legend.text = element_text(size = 9),
    legend.key.size = unit(0.3, "cm"),
    legend.spacing.x = unit(0.1, "cm"),                    # Bring items closer horizontally
    legend.spacing.y = unit(0.05, "cm"),                   # Bring rows closer vertically
    legend.box.spacing = unit(0.1, "cm"),
    legend.margin = margin(t = 2, r = 2, b = 4, l = 2, unit = "pt"),

    axis.text.y = element_text(size = 8.5, face = "bold"),
    axis.text.x = element_text(size = 10),
    plot.title = element_text(face = "bold", size = 15),
    plot.subtitle = element_text(size = 11, color = "gray30"),
    plot.margin = margin(t = 10, r = 55, b = 10, l = 10, unit = "pt")

   # plot.margin = margin(t = 15, r = 70, b = 15, l = 15, unit = "pt")
  )

invisible(dev.off())

#------------------------------------------------------------------------------

high_impact_df <- df %>%
  filter(Annotation_Impact %in% c("HIGH", "MODERATE"))

# 2. Recalculate the mutation counts using ONLY high-impact mutations
high_impact_counts <- high_impact_df %>%
  group_by(Mutation_ID) %>%
  summarise(Sample_Count = n_distinct(Sample), .groups = "drop")

# 3. Slice the top 50 of THOSE high-impact mutations
top_high_impact_mutations <- high_impact_counts %>%
  slice_max(Sample_Count, n = 50, with_ties = FALSE)

# 4. Semi-join back to construct your plotting data frame
plot_df <- high_impact_df %>%
  semi_join(top_high_impact_mutations, by = "Mutation_ID") %>%
  mutate(
    Annotation = map_chr(Annotation, get_top_consequence)
  ) %>%
  group_by(Mutation_ID, Annotation) %>%
  summarise(n = n_distinct(Sample), .groups = "drop")

# Identify mutations with multiple overlapping annotation consequences
multi_cons <- plot_df %>%
  dplyr::count(Mutation_ID) %>%
  filter(n > 1)
# Add internal bar labels (*) for multi-consequence occurrences
plot_df <- plot_df %>%
  mutate(
    seg_label = ifelse(
      Mutation_ID %in% multi_cons$Mutation_ID,
      paste0(n, "*"),
      NA_character_
    )
  )

# Calculate totals per mutation for the outer bar text labels
mutation_totals <- plot_df %>%
  group_by(Mutation_ID) %>%
  summarise(total = sum(n), .groups = "drop") %>%
  mutate(
    total_label = ifelse(
      Mutation_ID %in% multi_cons$Mutation_ID,
      paste0("(", total, "*)"),
      paste0("(", total, ")")
    )
  )

mutation_impacts <- high_impact_df %>%
  semi_join(top_high_impact_mutations, by = "Mutation_ID") %>%
  mutate(Impact_Clean = toupper(Annotation_Impact)) %>%
  mutate(Impact_Weight = case_when(
    Impact_Clean == "HIGH" ~ 2,
    Impact_Clean == "MODERATE" ~ 1,
    TRUE ~ 0
  )) %>%
  group_by(Mutation_ID) %>%
  filter(Impact_Weight == max(Impact_Weight)) %>%
  slice(1) %>%
  select(Mutation_ID, Max_Impact = Impact_Clean)

mutation_totals <- mutation_totals %>%
  left_join(mutation_impacts, by = "Mutation_ID")

# Order the Y-axis factor levels by total frequency count

# Order the Y-axis factor levels by total frequency count
factor_levels <- mutation_totals %>%
  arrange(total) %>%
  pull(Mutation_ID)

plot_df$Mutation_ID <- factor(plot_df$Mutation_ID, levels = factor_levels)
mutation_totals$Mutation_ID <- factor(mutation_totals$Mutation_ID, levels = factor_levels)

cat("Generating Plot 05: top_recurrent_mutations_by_impact.png ...\n")

cols <- colorRampPalette(brewer.pal(12, "Paired"))(
  length(unique(plot_df$Annotation))
)

CairoPNG(
  file = file.path(plots_dir, "05-top_recurrent_mutations_by_impact.png"),
  width = 14, height = 8, units = "in", res = 300 )

# Mutations are mapped directly to Y, Counts to X. No coord_flip needed!
ggplot(plot_df, aes(x = n, y = Mutation_ID)) +
  # LAYER 1: Core stacked colors
  geom_col(aes(fill = Annotation), color = "white", width = 0.8) +
  geom_col(
    data = filter(mutation_totals, Max_Impact == "HIGH"),
    aes(x = total, y = Mutation_ID),
    fill = NA, color = "black", linetype = "dotdash", linewidth = 0.9, width = 0.8,
    inherit.aes = FALSE
  ) +
  # LAYER 3: Overlay royalblue DASHED border for MODERATE impact mutations
  geom_col(
    data = filter(mutation_totals, Max_Impact == "MODERATE"),
    aes(x = total, y = Mutation_ID),
    fill = NA, color = "royalblue", linetype = "dashed", linewidth = 0.7, width = 0.8,
    inherit.aes = FALSE
  ) +
  geom_text(aes(label = seg_label, group = Annotation),  position = position_stack(vjust = 0.5),
    size = 3.2, fontface = "bold",  color = "black",  na.rm = TRUE  ) +
  geom_text(  data = mutation_totals,    aes(x = total, y = Mutation_ID, label = total_label),
    inherit.aes = FALSE,    hjust = -0.1, # Leaves a clean space between the bar end and the plain text number
    size = 3.0,    fontface = "bold"  ) +
  scale_fill_manual(  values = cols,  guide = guide_legend(ncol = 3, byrow = TRUE)) +
  labs(
    title = "Top 50 Recurrent High-Impact Functional Mutations",
    subtitle = "Most frequent severe or moderate functional variants stacked by annotation type. Borders emphasize severity:\nBlack Dot-Dash = HIGH Impact; Blue Dashed = MODERATE Impact. (* indicates multiple types)",
    x = "Mutation Frequency (Sample Count)",
    y = "Mutation Locus (Gene : Chromosome : Position : Ref : Alt)",
    fill = "Functional Consequence Type"
  ) +
  theme_bw(base_size = 13) +
  theme(
    # --- Legend Fix ---
    legend.position = "top",
    legend.title = element_text(size = 10, face = "bold"),
    legend.text = element_text(size = 8.5),
    legend.key.size = unit(0.3, "cm"),         # Keeps the color boxes small and crisp
    legend.spacing.x = unit(0.1, "cm"),                    # Bring items closer horizontally
    legend.spacing.y = unit(0.05, "cm"),                   # Bring rows closer vertically
    legend.box.spacing = unit(0.1, "cm"),
    legend.margin = margin(t = 2, r = 2, b = 4, l = 2, unit = "pt"),

    # --- Axis Formatting ---
    axis.text.y = element_text(size = 8.5, face = "bold"),    # Shrinks text slightly so long labels fit on the Y-axis
    axis.text.x = element_text(size = 10),

    # --- Title & Subtitle Left Alignment ---
    plot.title = element_text(face = "bold", size = 16, hjust = 0),
    plot.subtitle = element_text(size = 11, color = "gray30", hjust = 0),
    # --- Safe Margins ---
    plot.margin = margin(t = 10, r = 55, b = 10, l = 10, unit = "pt")
    #plot.margin = margin(t = 15, r = 70, b = 15, l = 15, unit = "pt")
  ) +
  coord_cartesian(clip = "off") # Added to guarantee total_labels do not clip past the right margin

invisible(dev.off())

# -----------------------------------------------------------------------------
#                                   GENE PREVALENCE IN SAMPELS
# -----------------------------------------------------------------------------
# -----------------------------------------------------------------------------
#                                   BARPLOTS
# -----------------------------------------------------------------------------
#-------------------------------------------------------------------------------
#The bar plot shows the top 25 genes ranked by cumulative mutation burden,
#calculated as the sum of all mutations observed per gene across all samples.
#Bar length represents the total number of mutations detected for each gene.

cat("Generating plot 06: Unfiltered Variant Prevalence...\n")
# 1. Aggregate total row counts across the sample columns
total_patients <- length(unique(df$Sample))
top_genes <- gene_sample_matrix %>%
  filter(Gene_Name != ".") %>%
  mutate(Total = rowSums(select(., -Gene_Name), na.rm = TRUE)) %>%
  mutate(Percentage = (Total / total_patients) * 100) %>%
  arrange(desc(Total)) %>%
  slice_head(n = 50)

# Plot
p_gene_burden <- ggplot(top_genes, aes(x = reorder(Gene_Name, Total), y = Total, fill = Percentage)) + #Total
  geom_col(width = 0.7, color = "black", lwd = 0.15) +
  coord_flip() +
  scale_fill_gradient(low = "#d9f0a3", high = "#1F0994", name = "Cohort %") + #"Mutation Count"
  labs(
    title = "Top 50 Genes by Total Variant Cohort Prevalence (Unfiltered)",
    subtitle = "Counts unique patients with ANY sequence variation. Includes background non-coding elements, deep introns, and structural noise.",
    x = "Gene Name",
    y = "Number of Mutated Patients (Cohort Frequency)"#Total Mutations Across All Samples
  ) +
  theme_bw(base_size = 14) +
  theme(
    legend.position = "right",
    axis.text.y = element_text(size = 9, face="bold"),
    plot.title = element_text(face = "bold"),
    plot.subtitle = element_text(size = 12)
  )

# Save high-res PNG
CairoPNG(file.path(plots_dir, "06-gene_prevalence_unfiltered.png"),
         width = 14, height = 8, units = "in", res = 300)
print(p_gene_burden)
invisible(dev.off())


cat("Generating plot 07: Functional Coding Prevalence...\n")
# 1. Establish the total unique cohort size dynamically
total_patients <- length(unique(df$Sample))

# 2. Collapse data to unique Gene-Patient pairs to find true carrier distribution
patient_prevalence <- df %>%
  filter(Gene_Name != "." & !is.na(Gene_Name)) %>%
  filter(!Annotation %in% drop_terms) %>%
  distinct(Gene_Name, Sample) %>% # CRITICAL STEP: Eliminates multiple hits inside a single patient
  group_by(Gene_Name) %>%
  summarise(Sample_Count = n(), .groups = "drop") %>%
  mutate(Percentage = (Sample_Count / total_patients) * 100) %>%
  arrange(desc(Sample_Count)) %>%
  slice_head(n = 50)

# 3. Build Plot
p_prevalence <- ggplot(patient_prevalence, aes(x = reorder(Gene_Name, Sample_Count),
                                               y = Sample_Count,
                                               fill = Percentage)) +
  geom_col(width = 0.7, color = "black", lwd = 0.2) +
  coord_flip() +
  # Using an alternative high-contrast gradient map
  scale_fill_gradient(low = "#e5f5f9", high = "#2ca25f", name = "Cohort %") +
  labs(
    title = "Top 50 Genes by Functional Coding Prevalence (Filtered)",
    subtitle = paste0("Counts unique patients carrying true protein-altering mutations. Background introns and non-coding noise completely removed (n = ", total_patients, ")."),
    x = "Gene Name",
    y = "Number of Unique Patients Impacted"
  ) +
  theme_bw(base_size = 12) +
  theme(
    legend.position = "right",
    axis.text.y = element_text(size = 9, face = "bold"),
    plot.title = element_text(face = "bold", size = 13),
    plot.subtitle = element_text(size = 9, face = "italic")
  )

# 4. Save high-res PNG
CairoPNG(file.path(plots_dir, "07-gene_prevalence_filtered_coding.png"),
         width = 14, height = 8, units = "in", res = 300)
print(p_prevalence)
invisible(dev.off())


#-------------------------------------------------------------------------------
#                                    ONCOPLOTS & ONCOSTRIPS
#-------------------------------------------------------------------------------
# -----------------------------------------------------------------
# STEP 1: Define Clean, Comprehensive Base Color Palette
# -----------------------------------------------------------------
cols <- c(
  "missense_variant"                    = "#2ca02c",
  "stop_gained"                         = "#d62728",
  "frameshift_variant"                  = "#6a3d9a",
  "start_lost"                          = "#542788",
  "splice_donor_variant"                = "#756bb1",
  "splice_region_variant"               = "#ff7f00",
  "intron_variant"                      = "#d9d9d9",
  "conservative_inframe_insertion"      = "#e31a1c",
  "conservative_inframe_deletion"       = "#fb9a99",
  "disruptive_inframe_insertion"        = "#ff7f00",
  "disruptive_inframe_deletion"         = "#e6550d",
  "splice_acceptor_variant"             = "#e6550d",
  "synonymous_variant"                  = "#636363",
  "start_retained_variant"              = "#969696",
  "5_prime_UTR_variant"                 = "#3182bd",
  "5_prime_UTR_premature_start_codon_gain_variant" = "#08519c",
  "3_prime_UTR_variant"                 = "#6baed6",
  "non_coding_transcript_exon_variant"  = "#756bb1",
  "upstream_gene_variant"               = "#c7e9c0",
  "downstream_gene_variant"             = "#e5f5e0",
  "intragenic_variant"                  = "#cccccc",
  "intergenic_region"                   = "#f0f0f0"
)


# Define shared severity hierarchy
severity_rank <- c("stop_gained" = 1, "frameshift_variant" = 2,
                   "missense_variant" = 3, "splice_region_variant" = 4,
                   "5_prime_UTR_variant" = 5, "3_prime_UTR_variant" = 6,
                   "intron_variant" = 7)

# Establish uniform global cohort context
all_samples <- unique(df$Sample)
total_patients <- length(all_samples)


cat("Generating Oncoplot 08: unfiltered_genomic_burden_oncoplot.png ...\n")

############################################################
# 1. Select top mutated genes (Raw mass approach)
############################################################

top_genes <- df %>%
  distinct(Gene_Name, Sample) %>%
  dplyr::count(Gene_Name) %>%
  arrange(desc(n)) %>%
  slice_head(n = 50) %>%
  pull(Gene_Name)

# 2 Collapse mutation types per gene/sample
oncoplot_df <- df %>%
  filter(Gene_Name %in% top_genes) %>%
  group_by(Gene_Name, Sample) %>%
  summarise(Annotation = paste(unique(Annotation), collapse = ";"),
    .groups = "drop")

# 3 Create mutation matrix
oncoplot_matrix <- oncoplot_df %>%
  pivot_wider(
    names_from = Sample,
    values_from = Annotation,
    values_fill = list(Annotation = "")
  ) %>%
  tibble::column_to_rownames("Gene_Name") %>%
  as.matrix()

oncoplot_matrix[is.na(oncoplot_matrix)] <- ""
oncoplot_matrix <- oncoplot_matrix[top_genes, , drop = FALSE]

# 4 Ensure color vector contains ALL mutation types
mut_types <- sort(unique(df$Annotation))
#
# cat("Total mutation types:", length(mut_types), "\n")
# cat(mut_types, sep = "\n")
# table(df$Annotation)
missing_cols <- setdiff(mut_types, names(cols))

if(length(missing_cols) > 0){
  extra_cols <- setNames(
    grDevices::rainbow(length(missing_cols)),
    missing_cols
  )
  cols <- c(cols, extra_cols)
}

############################################################
# 5 Define how mutations are drawn inside cells
############################################################

alter_fun <- list(
  background = function(x, y, w, h) {
    grid.rect(
      x, y, w, h,
      gp = gpar(fill = "#f0f0f0", col = NA)
    )
  }
)

for(i in names(cols)){
  alter_fun[[i]] <- local({
    col <- cols[i]
    function(x, y, w, h) {
      grid.rect(
        x, y, w*0.9, h*0.9,
        gp = gpar(fill = col, col = NA)
      )
    }
  })
}

############################################################
# 6 Top bar = total mutations per sample
############################################################

sample_burden <- df %>%
  dplyr::count(Sample) %>%
  slice(match(colnames(oncoplot_matrix), Sample)) %>%
  pull(n)

top_bar <- HeatmapAnnotation(
  Mutations =
    anno_barplot(
      sample_burden,
      gp = gpar(fill = "#408f34"),
      border = FALSE,
      height = unit(2,"cm")
    )
)

############################################################
# 7 Right stacked bar = mutation composition per gene
############################################################

gene_mut_comp <- df %>%
  filter(Gene_Name %in% rownames(oncoplot_matrix)) %>%
  distinct(Gene_Name, Sample, Annotation) %>%
  dplyr::count(Gene_Name, Annotation) %>%
  pivot_wider(
    names_from = Annotation,
    values_from = n,
    values_fill = 0
  ) %>%
  tibble::column_to_rownames("Gene_Name") %>%
  as.matrix()

gene_mut_comp <- gene_mut_comp[rownames(oncoplot_matrix), , drop=FALSE]

right_bar <- rowAnnotation(
  Frequency =
    anno_barplot(
      gene_mut_comp,
      gp = gpar(
        fill = cols[colnames(gene_mut_comp)],
        col = "white"
      ),
      border = FALSE,
      width = unit(3, "cm")
    )
)

CairoPNG(file.path(plots_dir, "08-unfiltered_genomic_burden_oncoplot.png"),
         width = 15, height = 8, units = "in", res = 300)

ht <- oncoPrint(
  oncoplot_matrix,
  get_type = function(x) {
    if(x == "") return(NULL)
    strsplit(x, ";")[[1]]
  },
  alter_fun = alter_fun,col = cols, remove_empty_columns = FALSE,
  remove_empty_rows = FALSE,top_annotation = top_bar, right_annotation = right_bar,
  show_column_names = TRUE, show_row_names = TRUE,column_names_gp = gpar(fontsize = 7),
  row_names_gp = gpar(fontsize = 9), # Shrinks the gene names
  row_order = 1:nrow(oncoplot_matrix),


  pct_gp = gpar(fontsize = 9),               # Shrinks the mutation percentages
  column_title = "Landscape of Total Genomic Variation (Unfiltered Approach)\nTop 50 mutated genes selected by raw variant frequency across all regions. Highly captures large structural loci and background passenger mutations.",
  column_title_gp = gpar(fontsize = 11, fontface = "bold"),
  heatmap_legend_param = list(title = "Mutation Type (Combined)",   nrow = 4)
)

draw(
  ht,
  heatmap_legend_side = "bottom",
  annotation_legend_side = "bottom"
)
invisible(dev.off())
cat("Generating Plot 09: oncostrip_unfiltered.png\n")
# 1. Establish absolute master cohort context
total_patients <- length(unique(df$Sample))

master_samples_unfilt <- df %>%
  dplyr::count(Sample) %>%
  arrange(desc(n)) %>%
  pull(Sample)

# 2. Recalculate raw leaderboard top 20
top_unfiltered_genes <- df %>%
  distinct(Gene_Name, Sample) %>%
  dplyr::count(Gene_Name) %>%
  arrange(desc(n)) %>%
  slice_head(n = 50) %>%
  pull(Gene_Name)

# 3. Calculate manual patient mutation percentages for these genes
unfilt_pcts <- df %>%
  filter(Gene_Name %in% top_unfiltered_genes) %>%
  distinct(Gene_Name, Sample) %>%
  dplyr::count(Gene_Name) %>%
  mutate(Percent_Str = paste0(round((n / total_patients) * 100, 1), "%")) %>%
  slice(match(top_unfiltered_genes, Gene_Name))

pct_map_unfilt <- setNames(unfilt_pcts$Percent_Str, unfilt_pcts$Gene_Name)

strip_df_unfilt <- df %>%
  filter(Gene_Name %in% top_unfiltered_genes) %>%
  mutate(Priority = order(match(Annotation, names(severity_rank), nomatch = 99))) %>%
  arrange(Gene_Name, Sample, Priority) %>%
  group_by(Gene_Name, Sample) %>%
  summarise(Annotation = first(Annotation), .groups = "drop")

# 5. Pivot matrix and fill missing samples
matrix_unfilt <- strip_df_unfilt %>%
  pivot_wider(names_from = Sample, values_from = Annotation, values_fill = "Wild Type") %>%
  tibble::column_to_rownames("Gene_Name") %>%
  as.matrix()

missing_samples_u <- setdiff(master_samples_unfilt, colnames(matrix_unfilt))
if(length(missing_samples_u) > 0) {
  empty_mod <- matrix("Wild Type", nrow = nrow(matrix_unfilt), ncol = length(missing_samples_u),
                      dimnames = list(rownames(matrix_unfilt), missing_samples_u))
  matrix_unfilt <- cbind(matrix_unfilt, empty_mod)
}
matrix_unfilt <- matrix_unfilt[top_unfiltered_genes, master_samples_unfilt, drop = FALSE]
matrix_unfilt[is.na(matrix_unfilt) | matrix_unfilt == ""] <- "Wild Type"

# 6. Dynamically build a targeted, clean legend palette
unique_vals_unfilt <- unique(as.vector(matrix_unfilt))
local_cols_unfilt <- c("Wild Type" = "#F0F0F0")
for(val in unique_vals_unfilt) {
  if(val != "Wild Type") {
    local_cols_unfilt[val] <- if(val %in% names(cols)) cols[val] else grDevices::rainbow(10)[sample(1:10, 1)]
  }
}

# 7. Create text labels blending Gene Name + Frequency Percentages
row_labels_unfilt <- paste0(rownames(matrix_unfilt), "  ", pct_map_unfilt[rownames(matrix_unfilt)])

# 8. Plot Render with Explicit Titles and Compact Legend
CairoPNG(file.path(plots_dir, "09-oncostrip_unfiltered.png"), width = 14, height = 8, units = "in", res = 300)

ht_unfilt <- Heatmap(
  matrix_unfilt,
  name = "Mutation Status",
  col = local_cols_unfilt,
  cluster_rows = FALSE, cluster_columns = FALSE,
  rect_gp = gpar(col = "white", lwd = 0.5),
  column_names_gp = gpar(fontsize = 6),

  row_labels = row_labels_unfilt,
  row_names_gp = gpar(fontsize = 10, fontface = "plain"),

  # NEW: Explanatory Header Logic Context
  column_title = "Landscape of Total Genomic Variation (Unfiltered Oncostrip)\nGenes sorted strictly by raw variant volume. Grid cells display the single most severe mutation per patient sample.\nPercentages reflect the proportion of the entire cohort with at least one variant in that gene.",
  column_title_gp = gpar(fontsize = 11, fontface = "bold"),
  row_title = "Top 50 Unfiltered Genomic Loci",
  row_title_gp = gpar(fontsize = 12, fontface = "bold"),

  # FIX: Compact, Multi-Row Legend Boundaries
  heatmap_legend_param = list(
    title = "Mutation Class State",
    direction = "horizontal",
    nrow = 3,                      # Split across 3 rows to avoid clipping edges
    grid_height = unit(3.5, "mm"), # Smaller color boxes
    grid_width = unit(3.5, "mm"),  # Smaller color boxes
    labels_gp = gpar(fontsize = 7.5),
    title_gp = gpar(fontsize = 9, fontface = "bold")
  )
)

draw(ht_unfilt, heatmap_legend_side = "bottom")
invisible(dev.off())







cat("Generating plot 10: Functional Coding Alterations         \n")
# -----------------------------------------------------------------
# STEP 2: Filter Data for Top Genes (Excluding Background Noise)
# -----------------------------------------------------------------
top_genes <- df %>%
  filter(!Annotation %in% drop_terms) %>%
  distinct(Gene_Name, Sample) %>%
  dplyr::count(Gene_Name) %>%
  arrange(desc(n)) %>%
  slice_head(n = 50) %>%
  pull(Gene_Name)

# 2. Build Atomized Matrix Array (Normalizing & Split Combinations)
cleaned_df <- df %>%
  filter(Gene_Name %in% top_genes) %>%
  mutate(Clean_Annotation = gsub("&", ";", Annotation)) %>%
  separate_rows(Clean_Annotation, sep = ";") %>%
  distinct(Gene_Name, Sample, Clean_Annotation)

unique_genes <- top_genes
sample_mutations <- df %>% dplyr::count(Sample, name = "Mutation_Count")
unique_samples <- sample_mutations %>% arrange(desc(Mutation_Count)) %>% pull(Sample)

# Create standard 2D character matrix filled with empty strings
oncoplot_matrix <- matrix("", nrow = length(unique_genes), ncol = length(unique_samples),
                          dimnames = list(unique_genes, unique_samples))

for(i in 1:nrow(cleaned_df)) {
  g <- cleaned_df$Gene_Name[i]
  s <- cleaned_df$Sample[i]
  v <- cleaned_df$Clean_Annotation[i]
  if (s %in% unique_samples) {
    if(oncoplot_matrix[g, s] == "") {
      oncoplot_matrix[g, s] <- v
    } else {
      # Append multi-hits using a semicolon
      oncoplot_matrix[g, s] <- paste(oncoplot_matrix[g, s], v, sep = ";")
    }
  }
}
oncoplot_matrix <- oncoplot_matrix[unique_genes, , drop = FALSE]

# Determine all distinct mutation varieties found within our matrix
all_variants_present <- unique(cleaned_df$Clean_Annotation)

# -----------------------------------------------------------------
# STEP 4: Right Side Barplot Calculations
# -----------------------------------------------------------------
gene_mut_comp <- cleaned_df %>%
  dplyr::count(Gene_Name, Clean_Annotation) %>%
  pivot_wider(names_from = Clean_Annotation, values_from = n, values_fill = 0) %>%
  column_to_rownames("Gene_Name") %>%
  as.matrix()

gene_mut_comp <- gene_mut_comp[unique_genes, , drop = FALSE]

# Safety palette expansion
missing_elements <- setdiff(all_variants_present, names(cols))
if(length(missing_elements) > 0) {
  extra_cols <- setNames(grDevices::rainbow(length(missing_elements)), missing_elements)
  cols <- c(cols, extra_cols)
}

sample_burden <- df %>%
  dplyr::count(Sample) %>%
  slice(match(unique_samples, Sample)) %>%
  pull(n)

# -----------------------------------------------------------------
# STEP 5: Define the Layering Graphic Rules (alter_fun)
# -----------------------------------------------------------------
alter_fun <- list(
  background = function(x, y, w, h) {
    grid.rect(x, y, w - unit(0.5, "mm"), h - unit(0.5, "mm"),
              gp = gpar(fill = "#f5f5f5", col = NA))
  }
)

for (mut in all_variants_present) {
  local({
    m <- mut
    alter_fun[[m]] <<- function(x, y, w, h) {
      grid.rect(x, y, w - unit(0.5, "mm"), h - unit(0.5, "mm"),
                gp = gpar(fill = cols[m], col = NA))
    }
  })
}

# -----------------------------------------------------------------
# STEP 6: Creating Top and Right Annotations
# -----------------------------------------------------------------
right_bar <- rowAnnotation(
  "Mutation Count" = anno_barplot(
    gene_mut_comp,
    gp = gpar(fill = cols[colnames(gene_mut_comp)], col = "white", lwd = 0.4),
    border = FALSE, width = unit(2.5, "cm")
  ),
  annotation_name_side = "top",
  annotation_name_gp = gpar(fontsize = 9, fontface = "bold")
)

top_bar <- HeatmapAnnotation(
  "Mutation Burden" = anno_barplot(
    sample_burden,
    gp = gpar(fill = "#408f34", col = "white", lwd = 0.4),
    border = FALSE, height = unit(1.6, "cm")
  ),
  annotation_name_side = "left",
  annotation_name_gp = gpar(fontsize = 9, fontface = "bold")
)

# -----------------------------------------------------------------
# STEP 7: Build a Bulletproof Manual Legend
# -----------------------------------------------------------------
# This replaces the auto-legend entirely, neutralizing the length error.
final_grid_colors <- cols[all_variants_present]

custom_legend <- Legend(
  labels = names(final_grid_colors),
  legend_gp = gpar(fill = final_grid_colors),
  title = "Mutation Type",
  direction = "horizontal",
  nrow = 3,
  title_gp = gpar(fontsize = 10, fontface = "bold"),
  labels_gp = gpar(fontsize = 8)
)

# -----------------------------------------------------------------
# STEP 8: Render Plot via 2D Layout with Blocked Auto-Legends
# -----------------------------------------------------------------
CairoPNG(file.path(plots_dir, "10-functional_coding_variants_oncoplot.png"),
         width = 15, height = 8, units = "in", res = 300)

ht <- oncoPrint(
  oncoplot_matrix,
  alter_fun = alter_fun,
  col = final_grid_colors,
  name = "Mutation",

  # Turn off the built-in legend completely
  show_heatmap_legend = FALSE,
  row_order = 1:nrow(oncoplot_matrix),

  column_title = "Landscape of Functional Coding Alterations (Filtered Approach)\nTop 50 genes selected exclusively by high-impact, protein-altering mutations. Background non-coding mutations re-integrated post-selection.",
  column_title_gp = gpar(fontsize = 11, fontface = "bold"),
  remove_empty_columns = FALSE,
  remove_empty_rows = FALSE,
  pct_side = "left",
  pct_gp = gpar(fontsize = 8, fontface = "plain"),

  show_row_names = TRUE,
  show_column_names = TRUE,
  row_labels = paste0(unique_genes, " "),
  column_names_gp = gpar(fontsize = 6),
  row_names_gp = gpar(fontsize = 9, fontface = "plain"),

  top_annotation = top_bar,
  right_annotation = right_bar
)

# Draw the plot and pass our custom manual legend to the bottom margin
draw(ht,
     heatmap_legend_side = "bottom",
     annotation_legend_side = "bottom",
     annotation_legend_list = list(custom_legend))

invisible(dev.off())

cat("Generating plot 11: oncostrip_functional.png\n")

# 1. Recalculate top functional leaderboards
top_functional_genes <- df %>%
  filter(!Annotation %in% drop_terms) %>%
  distinct(Gene_Name, Sample) %>%
  dplyr::count(Gene_Name) %>%
  arrange(desc(n)) %>%
  slice_head(n = 50) %>%
  pull(Gene_Name)

# 2. Calculate coding patient percentages (FIXED: Synchronized filtering boundary to use drop_terms)
func_pcts <- df %>%
  filter(Gene_Name %in% top_functional_genes) %>%
  filter(!Annotation %in% drop_terms) %>%
  distinct(Gene_Name, Sample) %>%
  dplyr::count(Gene_Name) %>%
  mutate(Percent_Str = paste0(round((n / total_patients) * 100, 1), "%"))

pct_map_func <- setNames(func_pcts$Percent_Str, func_pcts$Gene_Name)

# 3. Flatten matrix using severe-first logic
strip_df_func <- df %>%
  filter(Gene_Name %in% top_functional_genes) %>%
  filter(!Annotation %in% drop_terms) %>%
  # FIX: Removed broken order() function wrapper to enable correct integer sorting
  mutate(Priority = match(Annotation, names(severity_rank), nomatch = 99)) %>%
  arrange(Gene_Name, Sample, Priority) %>%
  group_by(Gene_Name, Sample) %>%
  summarise(Annotation = first(Annotation), .groups = "drop")

# 4. Build grid matrix
matrix_func <- strip_df_func %>%
  pivot_wider(names_from = Sample, values_from = Annotation, values_fill = "Wild Type") %>%
  tibble::column_to_rownames("Gene_Name") %>%
  as.matrix()

# FIX: Order samples dynamically based on functional mutation density within THESE 50 functional genes
sample_order_func <- df %>%
  filter(Gene_Name %in% top_functional_genes) %>%
  filter(!Annotation %in% drop_terms) %>%
  distinct(Sample, Gene_Name) %>%
  dplyr::count(Sample) %>%
  arrange(desc(n)) %>%
  pull(Sample)

missing_samples_f <- setdiff(all_samples, sample_order_func)
final_sample_order_f <- c(sample_order_func, missing_samples_f)

# Backfill non-mutated samples into the functional matrix boundary
missing_cols_f <- setdiff(final_sample_order_f, colnames(matrix_func))
if(length(missing_cols_f) > 0) {
  empty_mod_f <- matrix("Wild Type", nrow = nrow(matrix_func), ncol = length(missing_cols_f),
                        dimnames = list(rownames(matrix_func), missing_cols_f))
  matrix_func <- cbind(matrix_func, empty_mod_f)
}
matrix_func <- matrix_func[top_functional_genes, final_sample_order_f, drop = FALSE]
matrix_func[is.na(matrix_func) | matrix_func == ""] <- "Wild Type"

# 5. Rebuild functional target legend palette
unique_vals_func <- unique(as.vector(matrix_func))
local_cols_func <- c("Wild Type" = "#F0F0F0")
for(val in unique_vals_func) {
  if(val != "Wild Type") {
    local_cols_func[val] <- if(val %in% names(cols)) cols[val] else grDevices::rainbow(10)[sample(1:10, 1)]
  }
}

# 6. Merge functional labels with calculated mutation percentages safely
row_labels_func <- paste0(rownames(matrix_func), "  ",
                          ifelse(is.na(pct_map_func[rownames(matrix_func)]), "0%", pct_map_func[rownames(matrix_func)]))

# 7. Render Plot 21 Canvas
CairoPNG(file.path(plots_dir, "11-oncostrip_functional.png"), width = 14, height = 8, units = "in", res = 300)

ht_func <- Heatmap(
  matrix_func,
  name = "Mutation Status",
  col = local_cols_func,
  cluster_rows = FALSE, cluster_columns = FALSE,
  rect_gp = gpar(col = "white", lwd = 0.5),
  column_names_gp = gpar(fontsize = 6),
  row_labels = row_labels_func,
  row_names_gp = gpar(fontsize = 10, fontface = "plain"),
  column_title = "Landscape of Functional Coding Alterations (Filtered Oncostrip)\nGenes sorted exclusively by protein-altering mutation density; background non-coding elements are completely stripped out.\nPercentages reflect the proportion of the entire cohort carrying active coding variations in that gene.",
  column_title_gp = gpar(fontsize = 11, fontface = "bold"),
  row_title = "Top 50 Functional Candidate Driver Genes",
  row_title_gp = gpar(fontsize = 12, fontface = "bold"),
  heatmap_legend_param = list(
    title = "Functional Mutation State", direction = "horizontal", nrow = 3,
    grid_height = unit(3.5, "mm"), grid_width = unit(3.5, "mm"),
    labels_gp = gpar(fontsize = 7.5), title_gp = gpar(fontsize = 9, fontface = "bold")
  )
)

draw(ht_func, heatmap_legend_side = "bottom")
invisible(dev.off())



cat("Generating Plot 12: High vs Moderate OncoPrint Matrix (Patient Prevalence)...\n")

# -----------------------------------------------------------------------------
# 1. Prepare the OncoPrint Matrix Data
# -----------------------------------------------------------------------------

# Filters out noncoding noise (MODIFIER) and low-impact variants automatically
matrix_df <- df %>%
  filter(Annotation_Impact %in% c("HIGH", "MODERATE")) %>%
  select(Sample, Gene_Name, Annotation_Impact) %>%
  distinct()

top_genes_matrix <- matrix_df %>%
  group_by(Gene_Name) %>%
  summarise(Num_Samples = n_distinct(Sample), .groups = "drop") %>%
  arrange(desc(Num_Samples)) %>% # Ensure strict descending order by frequency
  slice_max(Num_Samples, n = 50, with_ties = FALSE) %>%
  pull(Gene_Name)

matrix_df <- matrix_df %>%
  filter(Gene_Name %in% top_genes_matrix)

wide_matrix <- matrix_df %>%
  group_by(Gene_Name, Sample) %>%
  summarise(Impacts = paste(unique(Annotation_Impact), collapse = ";"), .groups = "drop") %>%
  pivot_wider(names_from = Sample, values_from = Impacts, values_fill = "")

mat <- as.matrix(wide_matrix[,-1])
rownames(mat) <- wide_matrix$Gene_Name

# FIX 1: Force matrix rows to stay in the exact top-to-bottom order of top_genes_matrix
mat <- mat[top_genes_matrix, , drop = FALSE]


# -----------------------------------------------------------------------------
# 2. Define the OncoPrint Visual Settings
# -----------------------------------------------------------------------------

col_palette <- c("HIGH" = "#1F78B4", "MODERATE" = "#F4A582")

background_style <- function(x, y, w, h) {
  grid.rect(x, y, w, h, gp = gpar(fill = "#F0F0F0", col = "white", lwd = 1))
}

alteration_graphics <- list(
  background = background_style,
  HIGH = function(x, y, w, h) {
    grid.rect(x, y, w*0.9, h*0.9, gp = gpar(fill = col_palette["HIGH"], col = NA))
  },
  MODERATE = function(x, y, w, h) {
    grid.rect(x, y, w*0.9, h*0.4, gp = gpar(fill = col_palette["MODERATE"], col = NA))
  }
)


# -----------------------------------------------------------------------------
# 3. Generate and Render the Canvas (With Legend Fix)
# -----------------------------------------------------------------------------
CairoPNG(file = file.path(plots_dir, "12-high_moderate_oncoprint.png"),
         width = 14, height = 8, units = "in", res = 300)

ht <- oncoPrint(
  mat,
  alter_fun = alteration_graphics,
  col = col_palette,
  remove_empty_columns = FALSE,
  remove_empty_rows = FALSE,

  # FIX 2: Deactivate default hierarchical row clustering to preserve frequency order
  row_order = 1:nrow(mat),

  column_title = "Cohort Mutation Landscape Matrix (Sample Prevalence Profile)\nVisualizes individual sample mutation tracking and co-occurrence patterns across the top 50 prioritized genes.",
  column_title_gp = gpar(fontsize = 13, fontface = "bold"),
  row_title = "Top Prioritized Genes (By Sample Frequency)",
  row_title_gp = gpar(fontsize = 12, fontface = "bold"),

  row_names_gp = gpar(fontsize = 8, fontface = "italic"), # Styled cleanly for gene symbols
  pct_gp = gpar(fontsize = 8, fontface = "plain"),
  column_names_gp = gpar(fontsize = 8),
  show_column_names = TRUE,

  right_annotation = rowAnnotation(
    row_barplot = anno_oncoprint_barplot(border = TRUE)
  ),
  top_annotation = HeatmapAnnotation(
    column_barplot = anno_oncoprint_barplot(border = TRUE),
    annotation_name_side = "left"
  ),

  heatmap_legend_param = list(
    title = "Mutation Severity",
    at = c("HIGH", "MODERATE"),
    labels = c("HIGH", "MODERATE"),
    title_gp = gpar(fontsize = 10, fontface = "bold"),
    labels_gp = gpar(fontsize = 9)
  )
)

# Render safely with padding settings
draw(ht, padding = unit(c(5, 5, 5, 10), "mm"))

invisible(dev.off())

cat("Generating Plot 13: oncostrip_high_moderate_annotation.png\n")

# 2. Extract Top 50 Genes using Plot 10 logic (HIGH & MODERATE impacts only)
top_genes_impact <- df %>%
  filter(Annotation_Impact %in% c("HIGH", "MODERATE")) %>%
  distinct(Gene_Name, Sample) %>%
  dplyr::count(Gene_Name) %>%
  arrange(desc(n)) %>%
  slice_head(n = 50) %>%
  pull(Gene_Name)

# 3. Calculate patient mutation percentages within this high/moderate boundary
impact_pcts <- df %>%
  filter(Gene_Name %in% top_genes_impact) %>%
  filter(Annotation_Impact %in% c("HIGH", "MODERATE")) %>%
  distinct(Gene_Name, Sample) %>%
  dplyr::count(Gene_Name) %>%
  mutate(Percent_Str = paste0(round((n / total_patients) * 100, 1), "%"))

pct_map_impact <- setNames(impact_pcts$Percent_Str, impact_pcts$Gene_Name)

# 4. Flatten multi-hits extracting specific Annotation types instead of broad Impact strings
strip_df_impact <- df %>%
  filter(Gene_Name %in% top_genes_impact) %>%
  filter(Annotation_Impact %in% c("HIGH", "MODERATE")) %>%
  # Sort multi-hits dynamically using the functional severity ranking scale
  mutate(Priority = match(Annotation, names(severity_rank), nomatch = 99)) %>%
  arrange(Gene_Name, Sample, Priority) %>%
  group_by(Gene_Name, Sample) %>%
  summarise(Annotation = first(Annotation), .groups = "drop")

# 5. Build Grid Matrix
matrix_impact <- strip_df_impact %>%
  pivot_wider(names_from = Sample, values_from = Annotation, values_fill = "Wild Type") %>%
  tibble::column_to_rownames("Gene_Name") %>%
  as.matrix()

# 6. Sort samples dynamically to establish a clean waterfall cascade based on high/mod mutation profiles
sample_order_impact <- df %>%
  filter(Gene_Name %in% top_genes_impact) %>%
  filter(Annotation_Impact %in% c("HIGH", "MODERATE")) %>%
  distinct(Sample, Gene_Name) %>%
  dplyr::count(Sample) %>%
  arrange(desc(n)) %>%
  pull(Sample)

missing_samples_i <- setdiff(all_samples, sample_order_impact)
final_sample_order_i <- c(sample_order_impact, missing_samples_i)

# Backfill non-mutated cohort samples into the matrix boundaries
missing_cols_i <- setdiff(final_sample_order_i, colnames(matrix_impact))
if(length(missing_cols_i) > 0) {
  empty_mod_i <- matrix("Wild Type", nrow = nrow(matrix_impact), ncol = length(missing_cols_i),
                        dimnames = list(rownames(matrix_impact), missing_cols_i))
  matrix_impact <- cbind(matrix_impact, empty_mod_i)
}
matrix_impact <- matrix_impact[top_genes_impact, final_sample_order_i, drop = FALSE]
matrix_impact[is.na(matrix_impact) | matrix_impact == ""] <- "Wild Type"

# 7. Dynamically map global colors to the pulled mutation classes
unique_vals_impact <- unique(as.vector(matrix_impact))
local_cols_impact <- c("Wild Type" = "#F0F0F0")
for(val in unique_vals_impact) {
  if(val != "Wild Type") {
    local_cols_impact[val] <- if(val %in% names(cols)) cols[val] else grDevices::rainbow(10)[sample(1:10, 1)]
  }
}

# 8. Create text labels blending Gene Name + Frequency Percentages safely
row_labels_impact <- paste0(rownames(matrix_impact), "  ",
                            ifelse(is.na(pct_map_impact[rownames(matrix_impact)]), "0%", pct_map_impact[rownames(matrix_impact)]))

# 9. Render Canvas with multi-row legend support
CairoPNG(file.path(plots_dir, "13-high_moderate_annotation_oncostrip.png"), width = 14, height = 9, units = "in", res = 300)

ht_impact <- Heatmap(
  matrix_impact,
  name = "Mutation Status",
  col = local_cols_impact,
  cluster_rows = FALSE, cluster_columns = FALSE,
  rect_gp = gpar(col = "white", lwd = 0.5),
  column_names_gp = gpar(fontsize = 6),
  row_labels = row_labels_impact,
  row_names_gp = gpar(fontsize = 10, fontface = "plain"),
  column_title = "Landscape of Prioritized Functional Variation (High & Moderate Impact Oncostrip)\nGenes sorted strictly by High/Moderate variant prevalence. Grid cells expose the specific functional mutation category.\nPercentages reflect the proportion of the entire cohort with at least one high or moderate impact variant in that gene.",
  column_title_gp = gpar(fontsize = 11, fontface = "bold"),
  row_title = "Top 50 Prioritized Functional Loci",
  row_title_gp = gpar(fontsize = 12, fontface = "bold"),
  heatmap_legend_param = list(
    title = "Mutation Class State", direction = "horizontal", nrow = 3,
    grid_height = unit(3.5, "mm"), grid_width = unit(3.5, "mm"),
    labels_gp = gpar(fontsize = 7.5), title_gp = gpar(fontsize = 9, fontface = "bold")
  )
)

draw(ht_impact, heatmap_legend_side = "bottom")
invisible(dev.off())


#------------------------------------------

cat("Generating Plot : 13.1-top_protein_alterations_impact.png ...\n")

# 1. Establish the total unique cohort size dynamically
total_patients <- length(unique(df$Sample))
# 3. STEP B: Create a distinct subset dedicated ONLY to the Top 50 Plot
protein_prevalence <- protein_prevalence_all %>%
  slice_head(n = 50) %>%
  mutate(
    # Create Variant_Label using existing variables from protein_prevalence_all
    Variant_Label = paste0(Gene_Name, " (", HGVS.p, ")"),
    Percentage = (Sample_Count / total_patients) * 100
  ) %>%
  # Synchronize factor levels so ggplot builds the chart from highest down to lowest smoothly
  mutate(Variant_Label = factor(Variant_Label, levels = rev(unique(Variant_Label))))

# 3. Render the Plot
p_protein_bars <- ggplot(protein_prevalence, aes(x = Sample_Count, y = Variant_Label, fill = Impact_Clean)) +
  # Core bars with thin borders
  geom_col(width = 0.7, color = "black", lwd = 0.25) +

  # TEXT OVERLAY: Prints the raw patient count and precise cohort % directly past the bar edge
  geom_text(
    aes(label = paste0(Sample_Count, " (", round(Percentage, 1), "%)")),
    hjust = -0.15,
    size = 3.2,
    fontface = "bold",
    color = "black"
  ) +

  # High-contrast palette: Red for HIGH impact (Nonsense/Frameshift), Blue for MODERATE (Missense)
  scale_fill_manual(
    values = c("HIGH" = "#d95f02", "MODERATE" = "#7570b3"),
    name = "Impact Tier"
  ) +

  labs(
    title = "Top 50 Specific Protein Alterations Ranked by Cohort Penetrance",
    subtitle = paste0("Counts unique patients carrying the exact protein alteration (Total Cohort n = ", total_patients, ").\nBackground non-coding variations completely excluded."),
    x = "Number of Unique Carrier Patients (Frequency)",
    y = "Protein-Level Variant (Gene Name & Amino Acid Change)"
  ) +

  theme_bw(base_size = 13) +
  theme(
    legend.position = "top",
    legend.title = element_text(size = 10, face = "bold"),
    legend.text = element_text(size = 9),

    axis.text.y = element_text(size = 9.5, face = "bold"),
    axis.text.x = element_text(size = 10),

    plot.title = element_text(face = "bold", size = 15),
    plot.subtitle = element_text(size = 10.5, color = "gray30"),

    # Extra right margin space protects your text labels from clipping off the canvas edge
    plot.margin = margin(t = 10, r = 65, b = 10, l = 10, unit = "pt")
  ) +
  # Allows text labels to print outside the standard graph panel boundaries smoothly
  coord_cartesian(clip = "off")

# 4. Save high-resolution PNG
CairoPNG(
  file = file.path(plots_dir, "13.1-top_protein_alterations_impact.png"),
  width = 14, height = 8, units = "in", res = 300
)
print(p_protein_bars)

invisible(dev.off())





#--------------------------------------------------------------------------------
#                           GENE--> COUNT
#-------------------------------------------------------------------------------


#-------------------------------------------------------------------------------

cat("Generating plot 14: High vs Moderate Absolute Gene Burden (All Events)...\n")

# Step A: Filter out any lower-tier variants and group counts strictly by Gene and Impact
gene_impact_counts <- high_impact %>%
  filter(Annotation_Impact %in% c("HIGH", "MODERATE")) %>%
  group_by(Gene_Name, Annotation_Impact) %>%
  summarise(n = sum(Variant_Count), .groups = "drop")

# Step B: Calculate total mutational burden per gene to isolate the top 40 drivers
top_burden_genes <- gene_impact_counts %>%
  group_by(Gene_Name) %>%
  summarise(Total_Mutations = sum(n), .groups = "drop") %>%
  slice_max(Total_Mutations, n = 50, with_ties = FALSE)

# Step C: Filter plotting dataframe and order factors by total mutation burden
plot_df_enhanced <- gene_impact_counts %>%
  filter(Gene_Name %in% top_burden_genes$Gene_Name)

# Reorder the Gene factor levels using our calculated total burdens
plot_df_enhanced$Gene_Name <- factor(
  plot_df_enhanced$Gene_Name,
  levels = top_burden_genes %>% arrange(Total_Mutations) %>% pull(Gene_Name)
)

# Create a clean data frame for the total burden text labels at the end of bars
total_labels <- top_burden_genes %>%
  filter(Gene_Name %in% top_burden_genes$Gene_Name)


# -----------------------------------------------------------------------------
# 2. Plot Generation: Structured Mutational Profile Matrix
# -----------------------------------------------------------------------------

CairoPNG(
  file = file.path(plots_dir, "14-high_moderate_impact_mutations.png"),
  width = 14, height = 8, units = "in", res = 300
)

ggplot(plot_df_enhanced, aes(x = n, y = Gene_Name, fill = Annotation_Impact)) +
  # Create a clean stacked structure
  geom_col(color = "white", width = 0.75, position = position_stack(reverse = TRUE)) +
  # Print total accumulated mutation counts cleanly at the tip of each bar
  geom_text(
    data = total_labels,
    aes(x = Total_Mutations, y = Gene_Name, label = Total_Mutations),
    inherit.aes = FALSE,
    hjust = -0.3,
    size = 3.2,
    fontface = "italic",
    color = "black"
  ) +

  # Professional, publication-grade color scheme (Deep Blue for High, Salmon/Coral for Moderate)
  scale_fill_manual(values = c("HIGH" = "#1F78B4", "MODERATE" = "#F4A582")) +

  labs(
    title = "Total Cumulative Mutational Burden (Top 50 High-Density Genes)",
    subtitle = "Absolute count of all detected alterations, highlighting hotspots prone to repetitive or multi-hit variants",
    x = "Total Mutation Events (Absolute Count)",
    y = "Prioritized Gene Name",
    fill = "Impact Severity",
    caption = "*Note: Unlike the OncoPrint which tracks binary patient presence, this chart displays total raw variant frequencies.\nHighly recurrent genes or multi-hit loci expand the bars beyond sample sizes."
  ) +
  theme_bw(base_size = 14) +
  theme(
    # --- Clean Legend Architecture ---
    legend.position = "top",
    legend.title = element_text(size = 10, face = "bold"),
    legend.text = element_text(size = 9),
    legend.key.size = unit(0.4, "cm"),

    axis.title.x = element_text(size = 10, face = "bold", margin = margin(t = 10)),
    axis.title.y = element_text(size = 10, face = "bold", margin = margin(r = 10)),
    # --- Precise Axis Text Handling ---
    axis.text.y = element_text(size = 9, face = "plain"),
    axis.text.x = element_text(size = 9),

    # --- Left Alignment to Match Professional Styling and Prevent Clipping ---
    plot.title = element_text(face = "bold", size = 15, hjust = 0),
    plot.subtitle = element_text(size = 11, color = "gray30", hjust = 0),
    plot.caption = element_text(size = 9, face = "italic", color = "gray40", hjust = 1),
    # Expand right padding so end-of-bar total labels remain completely inside borders
    plot.margin = margin(t = 15, r = 50, b = 15, l = 15, unit = "pt")
  ) +
  coord_cartesian(clip = "off") # Safety barrier protecting labels past plot edge

invisible(dev.off())

cat("Generating plot 15: Overall mutations per gene (mutation_types_per_gene.png)...\n")
top_genes <- mutation_types_gene %>%
  dplyr::count(Gene_Name, wt = Variant_Count) %>%
  slice_max(n, n = 50) %>%
  pull(Gene_Name)

plot_df2 <- mutation_types_gene %>%
  filter(Gene_Name %in% top_genes) %>%
  mutate(
     Annotation = ifelse(Annotation %in% c( "intergenic_variant"), #".",
                        "Other / Unannotated", Annotation)
  ) %>%
  filter(Annotation != ".") %>%   # OMIT "." values
  group_by(Gene_Name, Annotation) %>%
  summarise(n = sum(Variant_Count), .groups = "drop")

gene_totals <- plot_df2 %>%
  group_by(Gene_Name) %>%
  summarise(total = sum(n), .groups = "drop")

gene_order <- gene_totals %>%
  arrange(desc(total)) %>%
  pull(Gene_Name) %>%
  rev()

plot_df2 <- plot_df2 %>%
  mutate(Gene_Name = factor(Gene_Name, levels = gene_order))

cols <- colorRampPalette(RColorBrewer::brewer.pal(12, "Paired"))(length(unique(plot_df2$Annotation)))

p_mut_type <- ggplot(plot_df2, aes(x = Gene_Name, y = n, fill = Annotation)) + #reorder(Gene_Name, -n)
  geom_col(color = "white", width = 0.8) +
  # Smaller count labels at end of bars
  geom_text(data = gene_totals, aes(x = Gene_Name, y = total, label = total),
            inherit.aes = FALSE, hjust = -0.2, fontface = "bold", size = 3.2) +
  scale_fill_manual(values = cols) +
  coord_flip(clip = "off") +
  labs(
    title = "Total Mutational Burden and Composition per Gene (Top 50)",
    subtitle = "Absolute count of all detected variants across the entire cohort (unfiltered by patient or clinical impact)",
    x = "Gene Name",
    y = "Total Number of Mutation Events (Raw Counts)",
    fill = "Mutation Type",
    caption = "*Note: This plot tracks total variant counts, not patient prevalence. Large structural genes may exhibit high counts\ndue to natural background variation and passenger mutation accumulation rather than active disease drive."
  ) +
  theme_bw(base_size = 14) +
  theme(
    legend.position = "top",
    legend.title = element_text(size = 10, face = "bold"),
    legend.text = element_text(size = 8.5),
    legend.key.size = unit(0.3, "cm"),
    legend.spacing.x = unit(0.1, "cm"),                    # Bring items closer horizontally
    legend.spacing.y = unit(0.05, "cm"),                   # Bring rows closer vertically
    legend.box.spacing = unit(0.1, "cm"),
    legend.margin = margin(t = 2, r = 2, b = 4, l = 2, unit = "pt"),
    # To change the actual Gene Names and scale numbers:
    axis.text.y = element_text(size = 9, face="bold"),
    axis.text.x = element_text(size = 10),

    # To change the big axis titles:
    axis.title.x = element_text(size = 11, face = "bold"),
    axis.title.y = element_text(size = 11, face = "bold"),

    plot.title = element_text(face = "bold", size = 15),
    plot.subtitle = element_text(size = 11, color = "gray30"),

    plot.caption = element_text(size = 9, face = "italic", color = "gray40", hjust = 1)
  )

CairoPNG(file.path(plots_dir, "15-mutation_types_per_gene.png"),
         width = 14, height = 8, units = "in", res = 300)
print(p_mut_type)
invisible(dev.off())
#------------------------------------------------------------------------------


cat("Generating plot 16: Coding-only mutation burden per gene (coding_mutation_burden_per_gene.png)...\n")

# 1. Filter out the background noise using your drop_terms, then find top 50 coding genes
top_genes <- mutation_types_gene %>%
  filter(!Annotation %in% drop_terms) %>%
  dplyr::count(Gene_Name, wt = Variant_Count) %>%
  slice_max(n, n = 50, with_ties = FALSE) %>%
  pull(Gene_Name)

# 2. Build the plotting dataframe using only these top coding genes
plot_df2 <- mutation_types_gene %>%
  filter(Gene_Name %in% top_genes) %>%
  filter(!Annotation %in% drop_terms) %>%
  group_by(Gene_Name, Annotation) %>%
  summarise(n = sum(Variant_Count), .groups = "drop")

# 3. Calculate absolute coding variant totals for the end-of-bar labels
gene_totals <- plot_df2 %>%
  group_by(Gene_Name) %>%
  summarise(total = sum(n), .groups = "drop")

# 4. Sort the y-axis cleanly by total coding mutation density
gene_order <- gene_totals %>%
  arrange(desc(total)) %>%
  pull(Gene_Name) %>%
  rev()

plot_df2 <- plot_df2 %>%
  mutate(Gene_Name = factor(Gene_Name, levels = gene_order))

# Color palette setup
cols <- colorRampPalette(RColorBrewer::brewer.pal(12, "Paired"))(length(unique(plot_df2$Annotation)))

# 5. Generate the Plot
p_mut_type <- ggplot(plot_df2, aes(x = Gene_Name, y = n, fill = Annotation)) +
  geom_col(color = "white", width = 0.8) +

  # Absolute count numbers at the end of bars
  geom_text(data = gene_totals, aes(x = Gene_Name, y = total, label = total),
            inherit.aes = FALSE, hjust = -0.2, fontface = "bold", size = 3.2) +
  scale_fill_manual(values = cols) +
  coord_flip(clip = "off") +
  labs(
    title = "Coding Mutation Burden Profiling (Top 50 Genes)",
    subtitle = "Absolute count of exonic/functional variants, stacked by molecular consequence",
    x = "Gene Name",
    y = "Total Number of Coding Mutations",
    fill = "Mutation Type",
    caption = "*Note: This plot tracks total raw variant counts across the full cohort, not patient-level prevalence.\nMulti-hit occurrences inside a single individual's sample will increase the total height of the bar."
  ) +
  theme_bw(base_size = 14) +
  theme(
    legend.position = "top",
    legend.title = element_text(size = 10, face = "bold"),
    legend.text = element_text(size = 8.5),
    legend.key.size = unit(0.3, "cm"),
    legend.spacing.x = unit(0.1, "cm"),                    # Bring items closer horizontally
    legend.spacing.y = unit(0.05, "cm"),                   # Bring rows closer vertically
    legend.box.spacing = unit(0.1, "cm"),
    legend.margin = margin(t = 2, r = 2, b = 4, l = 2, unit = "pt"),
    # To change the actual Gene Names and scale numbers:
    axis.text.y = element_text(size = 9, face="bold"),
    axis.text.x = element_text(size = 10),
    # To change the big axis titles:
    axis.title.x = element_text(size = 11, face = "bold"),
    axis.title.y = element_text(size = 11, face = "bold"),


    plot.title = element_text(face = "bold", size = 15),
    plot.subtitle = element_text(size = 11, color = "gray30"),

    # This directly controls and shrinks the note font size below the plot
    plot.caption = element_text(size = 9, face = "italic", color = "gray40", hjust = 1)
  )

# 6. Save Plot 11 with the explicit coding descriptive name
CairoPNG(file.path(plots_dir, "16-coding_mutation_burden_per_gene.png"),
         width = 14, height = 8, units = "in", res = 300)
print(p_mut_type)
invisible(dev.off())





# -----------------------------------------------------------------------------
# Plot 17: Annotation type per gene Pie chart
# -----------------------------------------------------------------------------
# =============================================================================
# UNIFIED DATA & COLOR PREPARATION (Shared by both plots)
# =============================================================================

# 1. Process the main dataframe once
shared_df <- df %>%
  mutate(Annotation = purrr::map_chr(Annotation, get_top_consequence)) %>%
  group_by(Annotation) %>%
  summarise(total = n(), .groups = "drop") %>%
  mutate(
    percent = total / sum(total) * 100,
    label = paste0(round(percent, 1), "%"),
    display_label = paste0(round(percent, 1), "% (", format(total, big.mark = ","), ")")
  )

grand_total <- sum(shared_df$total)

# 2. Generate the color scheme based on Plot 17's largest-to-smallest ordering
color_ordering <- shared_df %>% arrange(desc(percent))
cols <- colorRampPalette(RColorBrewer::brewer.pal(12, "Paired"))(nrow(color_ordering))
names(cols) <- color_ordering$Annotation # This locks the colors to the exact text names


# =============================================================================
# GENERATE PLOT 17: Donut/Pie Chart
# =============================================================================
cat("Generating plot 17: mutation_type_distribution_pie.png...\n")

pie_df <- shared_df %>% arrange(desc(percent))

graphics.off()
CairoPNG(file = file.path(plots_dir, "17-mutation_type_distribution_pie.png"),
         width = 14, height = 8, units = "in", res = 300)

ggplot(pie_df, aes(x = 2, y = percent, fill = reorder(Annotation, -percent))) +
  geom_col(color = "white", linewidth = 0.6, width = 0.5) +
  coord_polar(theta = "y", start = 0) +

  geom_text(
    aes(label = ifelse(percent >= 2.0, label, "")),
    position = position_stack(vjust = 0.5),
    size = 2.8,
    fontface = "bold",
    color = "gray15"
  ) +

  annotate("text", x = 1.1, y = 0,
           label = paste0("Total Variants\n", format(grand_total, big.mark = ",")),
           size = 5.5, fontface = "bold", color = "gray15") +

  scale_fill_manual(values = cols, guide = guide_legend(ncol = 3, byrow = TRUE)) +
  scale_x_continuous(limits = c(1.1, 2.5)) +

  labs(
    title = "Overall Distribution of Variant Functional Consequences",
    subtitle = "Proportional distribution of highest-impact mutation consequences",
    fill = "Mutation Consequence"
  ) +
  theme_void() +
  theme(
    plot.title = element_text(hjust = 0.5, face = "bold", size = 16, margin = margin(b = 4)),
    plot.subtitle = element_text(hjust = 0.5, size = 11, color = "gray30", face = "italic", margin = margin(b = 15)),

    legend.position = "top",
    legend.title = element_text(size = 10, face = "bold"),
    legend.text = element_text(size = 10),
    legend.key.size = unit(0.4, "cm"),
    legend.spacing.x = unit(0.1, "cm"),
    legend.spacing.y = unit(0.05, "cm"),
    legend.box.spacing = unit(0.1, "cm"),

    plot.margin = margin(t = 15, r = 20, b = 15, l = 20, unit = "pt")
  )

invisible(dev.off())


# =============================================================================
# GENERATE PLOT 18: Standalone Horizontal Bar Chart (With Matching Colors)
# =============================================================================
cat("Generating plot 18: mutation_type_distribution_bar.png...\n")

bar_df <- shared_df %>% arrange(percent)

CairoPNG(file = file.path(plots_dir, "18-mutation_type_distribution_bar.png"),
         width = 13, height = 8, units = "in", res = 300)

ggplot(bar_df, aes(x = reorder(Annotation, percent), y = percent, fill = Annotation)) +
  geom_col(color = "black", linewidth = 0.2, width = 0.75) +

  geom_text(aes(label = display_label), hjust = -0.15, size = 3.8, fontface = "bold") +

  scale_fill_manual(values = cols) + # Uses the exact same color mapping vector
  scale_y_continuous(expand = expansion(mult = c(0, 0.15))) +
  coord_flip(clip = "off") +

  labs(
    title = "Overall Distribution of Variant Functional Consequences",
    subtitle = "Relative percentage and total instance counts prioritized by highest-impact tier",
    x = "Assigned Functional Annotation",
    y = "Percentage of Total Callset (%)"
  ) +
  theme_bw(base_size = 13) +
  theme(
    legend.position = "none",
    plot.title = element_text(face = "bold", size = 15, margin = margin(b = 4)),
    plot.subtitle = element_text(size = 11, color = "gray30", face = "italic", margin = margin(b = 15)),

    axis.text.y = element_text(face = "bold", color = "black", size = 10),
    axis.text.x = element_text(color = "black"),
    axis.title = element_text(face = "bold", size = 11),

    panel.grid.major.y = element_blank(),
    plot.margin = margin(t = 15, r = 70, b = 15, l = 15, unit = "pt")
  )

invisible(dev.off())
#-------------------------------------------------------------------------------
# Plot 19: Gene × Sample mutation heatmap
# -----------------------------------------------------------------------------

cat("\nFiltering for HIGH/MODERATE impacts...\n")
cat("Ranking genes strictly by TOTAL MUTATION VOLUME (Density Profile)...\n")
cat("Generating plot 19: gene_sample_heatmap_functional_mutation_density.png\n")

# Step 1: Filter raw data for high/moderate impacts and calculate unique events per sample per gene
filtered_events <- df %>%
  filter(Gene_Name != ".", Annotation_Impact %in% c("HIGH", "MODERATE")) %>%
  group_by(Gene_Name, Sample) %>%
  summarise(Mutation_Count = n(), .groups = "drop")

# Step 2: Pivot into a clean wide matrix format, filling empty entries with 0
wide_filtered_mat <- filtered_events %>%
  pivot_wider(names_from = Sample, values_from = Mutation_Count, values_fill = 0) %>%
  column_to_rownames("Gene_Name") %>%
  as.matrix()

mode(wide_filtered_mat) <- "numeric"

# Step 3: Select top 25 genes based strictly on HIGH/MODERATE mutation load
filtered_gene_totals <- rowSums(wide_filtered_mat)
top_filtered_genes <- names(sort(filtered_gene_totals, decreasing = TRUE))[1:min(25, length(filtered_gene_totals))]
heatmap_mat_filtered <- wide_filtered_mat[top_filtered_genes, , drop = FALSE]

# Step 4: Define context-specific color palette based on new maximum values
max_val_filt <- max(heatmap_mat_filtered)
col_fun_filt <- colorRamp2(
  c(0, max_val_filt*0.1, max_val_filt*0.25, max_val_filt*0.5, max_val_filt*0.75, max_val_filt),
  c("#FFFFFF", "#E0F3F8", "#A8DDB5", "#a3c2de", "#609bc8", "#0070a3")
)

# Step 5: Create Heatmap with functional impact descriptors
ht_filtered <- Heatmap(
  heatmap_mat_filtered,
  name = "Functional Count",
  col = col_fun_filt,
  cluster_rows = TRUE,
  cluster_columns = TRUE,
  show_row_names = TRUE,
  show_column_names = TRUE,
  row_names_gp = gpar(fontsize = 9, fontface = "bold"),
  column_names_gp = gpar(fontsize = 6),
  rect_gp = gpar(col = "grey90", lwd = 0.3),

  # NEW TITLES: Clearly shows this isolates functional disruptions
  column_title = "Targeted Functional Mutation Density Landscape (High & Moderate Impacts Only)",
  column_title_gp = gpar(fontsize = 14, fontface = "bold"),
  row_title = "Top 25 Genes Ranked by Total Mutation Volume (Susceptible to Single-Patient Hyper-mutation)",
  row_title_gp = gpar(fontsize = 11, fontface = "italic"),

  heatmap_legend_param = list(
    title = "Mutation Count",
    at = c(0, round(max_val_filt/4), round(max_val_filt/2), round(max_val_filt*0.75), max_val_filt),
    labels = c("0", "Low", "Medium", "High", "Max"),
    legend_height = unit(6, "cm"),
    title_position = "topcenter",
    title_gp = gpar(fontsize = 12, fontface = "bold"),
    labels_gp = gpar(fontsize = 10)
  )
)

# Save to PNG
CairoPNG(
  file = file.path(plots_dir, "19-gene_sample_heatmap_functional_mutation_density.png"),
  width = 14, height = 8, units = "in", res = 300
)
draw(ht_filtered, padding = unit(c(5, 5, 5, 10), "mm"))
invisible(dev.off())


cat("\nFiltering for HIGH/MODERATE impacts...\n")
cat("Ranking genes strictly by COHORT PATIENT PENETRANCE (% of unique samples mutated)...\n")
cat("Generating plot 20: quantitative_oncoprint_cohort_penetrance.png\n")

# -----------------------------------------------------------------------------
# 1. Prepare Data: Count mutations, but Sort Genes by Patient Penetrance
# -----------------------------------------------------------------------------

# Step A: Filter raw data for high/moderate impacts and get counts
filtered_events <- df %>%
  filter(Gene_Name != ".", Annotation_Impact %in% c("HIGH", "MODERATE")) %>%
  group_by(Gene_Name, Sample) %>%
  summarise(Mutation_Count = n(), .groups = "drop")

# Step B: Calculate true OncoPrint Patient Penetrance (Unique Samples per Gene)
gene_penetrance <- filtered_events %>%
  group_by(Gene_Name) %>%
  summarise(Num_Samples = n_distinct(Sample), .groups = "drop") %>%
  slice_max(Num_Samples, n = 25, with_ties = FALSE) %>% # Top 25 widespread genes
  arrange(Num_Samples) # Ordered ascending so the highest is at the top when plotted

# Step C: Pivot into a clean wide matrix format using only these top 25 genes
wide_hybrid_mat <- filtered_events %>%
  filter(Gene_Name %in% gene_penetrance$Gene_Name) %>%
  pivot_wider(names_from = Sample, values_from = Mutation_Count, values_fill = 0) %>%
  column_to_rownames("Gene_Name")

# Enforce the OncoPrint row ordering (By patient frequency, not raw rowSums)
wide_hybrid_mat <- wide_hybrid_mat[gene_penetrance$Gene_Name, , drop = FALSE]
mat_hybrid <- as.matrix(wide_hybrid_mat)
mode(mat_hybrid) <- "numeric"


# -----------------------------------------------------------------------------
# 2. Setup Quantitative Heatmap Styles
# -----------------------------------------------------------------------------

# Continuous color palette: White (0) -> Light Coral -> Dark Firebrick Red (Max counts)
max_val_hybrid <- max(mat_hybrid)
col_fun_hybrid <- colorRamp2(
  c(0, 1, max_val_hybrid * 0.5, max_val_hybrid),
  c("#F5F5F5", "#FADBD8", "#E74C3C", "#78281F") # Clean gray background for zeros
)


# -----------------------------------------------------------------------------
# 3. Create the Hybrid Quantitative OncoPrint
# -----------------------------------------------------------------------------

# Calculate sample-level total frequencies for the top bar annotation chart
sample_totals <- colSums(mat_hybrid)

ht_hybrid <- Heatmap(
  mat_hybrid,
  name = "Mutation Count",
  col = col_fun_hybrid,

  # Clustering preferences
  cluster_rows = FALSE,      # CRITICAL: Keeps our explicit patient prevalence sort order intact!
  cluster_columns = TRUE,    # Groups patients with similar overall mutational loads together

  show_row_names = TRUE,
  show_column_names = TRUE,
  row_names_gp = gpar(fontsize = 10, fontface = "bold"),
  column_names_gp = gpar(fontsize = 6),
  rect_gp = gpar(col = "white", lwd = 0.5), # Clean white grid borders

  # Factual, highly informative titles
  column_title = "Population-Level Cohort Penetrance Matrix (Hybrid OncoPrint)",
  column_title_gp = gpar(fontsize = 14, fontface = "bold"),
  row_title = "Top 25 Genes Sorted by Recurrence Frequency (% of Unique Patients Damaged)",
  row_title_gp = gpar(fontsize = 11, fontface = "italic"),

  # Add OncoPrint-style marginal bar plots to show raw burden trends
  top_annotation = HeatmapAnnotation(
    "Total Sample Burden" = anno_barplot(sample_totals, bar_width = 0.8, gp = gpar(fill = "#34495E", col = NA)),
    annotation_name_side = "left",
    annotation_name_gp = gpar(fontsize = 9, fontface = "bold")
  ),

  heatmap_legend_param = list(
  #  title = "High/Mod Mutations\n(Per Sample)",
    title = "High/Mod Hits\n(Cap: 1 vote/patient for Rank)",
    title_gp = gpar(fontsize = 10, fontface = "bold"),
    labels_gp = gpar(fontsize = 9),
    legend_height = unit(4, "cm")
  )
)

# Render and Save the Canvas
CairoPNG(
  file = file.path(plots_dir, "20-quantitative_oncoprint_cohort_penetrance.png"),
  width = 15, height = 9, units = "in", res = 300
)

draw(ht_hybrid, padding = unit(c(5, 5, 5, 10), "mm"))

invisible(dev.off())


# =============================================================================
# Plot 21: Functional Gene-Mutation Co-Occurrence Matrix (Pathway Interactions)
# =============================================================================

cat("\nFiltering for HIGH/MODERATE functional impacts...\n")
cat("Calculating pairwise gene co-occurrence matrix for top 25 recurrent genes...\n")
cat("Generating plot 21: gene_functional_cooccurrence_matrix.png\n")

# Step 1: Filter for functional changes and extract binary presence/absence
df_functional <- df %>%
  filter(Gene_Name != ".", Annotation_Impact %in% c("HIGH", "MODERATE"))

gene_sample <- df_functional %>%
  distinct(Gene_Name, Sample) %>%
  mutate(val = 1) %>%
  pivot_wider(names_from = Sample, values_from = val, values_fill = 0)

mat <- gene_sample %>%
  column_to_rownames("Gene_Name") %>%
  as.matrix()

# Step 2: Select the Top 25 genes based on unique patient penetrance
top_genes <- names(sort(rowSums(mat), decreasing = TRUE))[1:min(25, nrow(mat))]
mat <- mat[top_genes, , drop = FALSE]

# Step 3: Compute the square symmetric interaction matrix via linear algebra
interaction_matrix <- mat %*% t(mat)

# Step 4: Visual Optimization Trick (Scale color contrast by off-diagonal max)
mat_no_diag <- interaction_matrix
diag(mat_no_diag) <- 0
max_interaction <- max(mat_no_diag)
if(max_interaction == 0) max_interaction <- 1

# Step 5: Define a sophisticated, high-contrast Deep Amethyst color palette
col_fun_interact <- colorRamp2(
  c(0, max_interaction * 0.25, max_interaction * 0.5, max_interaction * 0.75, max(interaction_matrix)),
  c("#F8F7FF", "#D8B4F8", "#9D4EDD", "#5A189A", "#240046") # Sleek Off-White -> Lavender -> Deep Purple
)

# Step 6: Create Heatmap with inner-cell numeric overlays
CairoPNG(
  file = file.path(plots_dir, "21-gene_functional_cooccurrence_matrix.png"),
  width = 14, height = 8, units = "in", res = 300
)

ht_interaction <- Heatmap(
  interaction_matrix,
  name = "Shared Patients",
  col = col_fun_interact,
  cluster_rows = TRUE,
  cluster_columns = TRUE,
  show_row_names = TRUE,
  show_column_names = TRUE,
  row_names_gp = gpar(fontsize = 10, fontface = "bold"),
  column_names_gp = gpar(fontsize = 10, fontface = "bold"),
  rect_gp = gpar(col = "white", lwd = 0.5), # Crisp white borders

  column_title = "Functional Gene Mutation Co-Occurrence Network\n(High & Moderate Impacts Only)",
  column_title_gp = gpar(fontsize = 14, fontface = "bold"),
  row_title = "Top 25 Highly Recurrent Cohort Loci",
  row_title_gp = gpar(fontsize = 11, fontface = "italic"),

  # Map labels directly inside the cells for unparalleled clarity
  cell_fun = function(j, i, x, y, width, height, fill) {
    val <- interaction_matrix[i, j]
    if(val > 0) {
      # Use white text for dark backgrounds, dark text for light backgrounds
      text_col <- ifelse(val >= (max_interaction * 0.5) || i == j, "white", "#240046")
      grid.text(sprintf("%d", val), x, y, gp = gpar(fontsize = 9, fontface = "bold", col = text_col))
    }
  },

  heatmap_legend_param = list(
    title = "Shared Patients\n(Count)",
    title_gp = gpar(fontsize = 10, fontface = "bold"),
    labels_gp = gpar(fontsize = 9),
    legend_height = unit(4, "cm")
  )
)

draw(ht_interaction, padding = unit(c(5, 5, 5, 10), "mm"))
invisible(dev.off())

# -----------------------------------------------------------------------------
# Plot: Mutation spectrum
# -----------------------------------------------------------------------------
cat("\nIsolating single-nucleotide variants (SNVs)...\n")
cat("Profiling all 12 uncollapsed directional base substitutions across the cohort...\n")
cat("Generating plot 22: mutation_spectrum.png\n")
# 1. Gather all 12 raw base changes
mutation_spectrum <- df %>%
  separate_rows(ALT, sep = ",") %>%
  filter(nchar(REF) == 1 & nchar(ALT) == 1) %>%
  mutate(Substitution = paste0(REF, ">", ALT)) %>%
  dplyr::count(Substitution)
# Enforce your exact 12-class factor sequence
mutation_spectrum$Substitution <- factor(
  mutation_spectrum$Substitution,
  levels = c("C>A","C>G","C>T","T>A","T>C","T>G",
             "A>C","A>G","A>T","G>A","G>C","G>T")
)
# 2. Build the chart with borders and clear subtitles
p_spec <- ggplot(mutation_spectrum,
                 aes(Substitution, n, fill = Substitution)) +
  geom_col(color = "black", width = 0.7, lwd = 0.3) +
  labs(
    title = "Cohort-Wide Single Nucleotide Variant (SNV) Mutation Spectrum",
    subtitle = "Absolute frequency count of all 12 directional base-pair substitutions identified across the cohort.",
    x = "Nucleotide Substitution Type",
    y = "Total Variant Count"
  ) +
  theme_bw() +
  theme(legend.position="none",
        plot.title = element_text(face = "bold", size = 14),
        plot.subtitle = element_text(size = 10, face = "italic"),
        axis.text = element_text(color = "black", face = "bold"),
        axis.title = element_text(face = "bold")
        )

CairoPNG(file.path(plots_dir, "22-mutation_spectrum.png"),
         width = 11, height = 7, units = "in", res = 300)

print(p_spec)
invisible(dev.off())



# -----------------------------------------------------------------------------
# Plot 9 (NEW): Overall mutation impact composition
# -----------------------------------------------------------------------------
cat("\nProfiling overall variant functional severity tiers...\n")
cat("Summarizing high, moderate, low, and modifier mutation counts...\n")
cat("Generating plot 23: overall_mutation_impact.png\n")

impact_summary <- df %>% dplyr::count(Annotation_Impact)

p_impact <- ggplot(impact_summary, aes(x = Annotation_Impact, y = n, fill = Annotation_Impact)) +
  geom_col(color = "black", lwd = 0.2, width = 0.6) +
  #geom_text(aes(label = n), vjust = -0.5, size = 4, fontface = "bold") +
  geom_text(aes(label = format(n, big.mark = ",")), vjust = -0.5, size = 4.5, fontface = "bold") +
  scale_fill_manual(values = c("HIGH"="#D73027", "MODERATE"="#FC8D59", "LOW"="#91BFDB","MODIFIER" = "#E0E0E0")) +
  scale_y_continuous(expand = expansion(mult = c(0, 0.11))) +
  labs(
    title = "Overall Distribution of Mutation Impact",
    subtitle = "Functional severity profiling across the entire variant cohort. High-impact alterations represent potential loss-of-function events.",
    x = "Predicted Functional Impact Tier",
    y = "Total Variant Count",
    fill = "Impact Tier"
  ) +
  theme_bw(base_size = 14) +
  theme(
    legend.position = "none",
    plot.title = element_text(face = "bold", size = 16),
    plot.subtitle = element_text(size = 11, face = "italic", color = "gray30"),
    panel.grid.major.x = element_blank()
  )

CairoPNG(file.path(plots_dir, "23-overall_mutation_impact.png"),
         width = 14, height = 8, units = "in", res = 300)
print(p_impact)
invisible(dev.off())

# =============================================================================
cat("All analyses and plots generated successfully.\n")
# =============================================================================

#-------------------------------------------------------------------------------
# WRITING TO PDF
#-------------------------------------------------------------------------------
cat("Preparing VCF_Analysis.pdf ...\n")

# Hardcoded output name as requested
pdf_filename <- file.path(plots_dir, "VCF_Analysis.pdf")

# Fetch all PNG images strictly inside the current working directory
image_files <- list.files(path = plots_dir, pattern = "\\.png$", full.names = TRUE, recursive = FALSE)
if (length(image_files) == 0) {
  cat("\n[ERROR] No PNG images found in the current directory. Skipping PDF creation.\n")
} else {
  cat(sprintf("\nFound %d PNG files. Opening PDF: %s\n", length(image_files), pdf_filename))

  # Open a dedicated PDF graphics device (Landscape orientation)
  pdf(file = pdf_filename, width = 11, height = 8.5)

  for (img_path in image_files) {
    img_name <- tools::file_path_sans_ext(basename(img_path))
    cat(sprintf("  + Adding image: %s\n", img_name))

    # Aggressive garbage collection to keep memory clean
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

    # Title Header Viewport
    text_size <- if (nchar(img_name) > 50) 10 else 12
    pushViewport(viewport(y = 0.95, height = 0.08, just = "top"))
    grid.text(img_name, gp = gpar(fontface = "bold", fontSize = text_size))
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

  # Close the PDF graphics device completely
  cat("\nClosing PDF device...\n")
  while (dev.cur() > 1) {
    invisible(dev.off())
  }

  # Final verification check
  if (file.exists(pdf_filename) && file.size(pdf_filename) > 0) {
    cat(sprintf("--> Success! %s generated safely (Size: %.2f MB).\n", pdf_filename, file.size(pdf_filename) / (1024^2)))
  } else {
    cat(sprintf("--> Error: %s was not created properly.\n", pdf_filename))
  }

  # Final loop cleanup pass
  gc(verbose = FALSE)
}
#------------------------------------------------------------------------------
#                 MARKDOWNREPORT
#------------------------------------------------------------------------------
# Build dynamic output filenames
output_pdf_name <- paste0("Genomic_Cohort_Summary_", clean_tag, ".pdf")
output_pdf_path <- file.path(myargs[1], output_pdf_name)


my_data_file <- file.path(myargs[1], "report_data.RData")

rmarkdown::render(
  input = myargs[4],
   output_file = output_pdf_path,
  params      = list(rdata_path = my_data_file,
                     lib_path   = myargs[2])
)

cat("Success: Text-based narrative evaluation generated as '", output_pdf_name, "'\n", sep = "")
#-------------------------------

