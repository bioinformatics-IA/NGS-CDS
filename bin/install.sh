#!/bin/bash

# Directory where the compressed files are located
FASTP_PATH="${1}"
STAR_PATH="${2}"
BWA_PATH="${3}"
SAMTOOLS_PATH="${4}"
BCFTOOLS_PATH="${5}"
VEP_PATH="${6}"
HTSLIB_PATH="${7}"
#vcf2maf_PATH="${8}"
PASS="${8}"
LOG_PATH="${9}"
R_LIB="${10}"

echo "Paths are: $FASTP_PATH,$STAR_PATH,$BWA_PATH,$SAMTOOLS_PATH,$BCFTOOLS_PATH,$VEP_PATH,$HTSLIB_PATH"
echo "Log File: $LOG_PATH"

if ! echo "$PASS" | sudo -S true > /dev/null 2>&1; then
    echo "Error: Invalid sudo password or no sudo access."
    exit 1
fi

echo "============================================"
echo "INSTALLING REQUIRED LIBRARIES..."
echo "============================================"

# Function to install a package
#install_package() {
#    PACKAGE=$1

#   echo "Installing $PACKAGE..."
   
   # Check if installable with a dry-run first
#    if ! apt-get install -s "$PACKAGE" > /dev/null 2>&1; then
#        echo "Error: $PACKAGE cannot be installed due to missing or broken dependencies."
#        exit 1
#    fi
    
#    if echo "$PASS" | sudo -S DEBIAN_FRONTEND=noninteractive apt-get install -y "$PACKAGE"; then
#        echo "$PACKAGE installed successfully."
#    else
#        echo "Error installing $PACKAGE. Exiting." 
#        exit 1
#    fi
#}
##########################################################################
###############################################################################
# install_if_missing <apt‑package> [<binary‑to‑verify>]
# • Skips the package if it is already “install ok installed”.
# • Performs an apt dry‑run first; exits on broken deps.
# • Optionally verifies that a specific binary was created.
###############################################################################
install_package () {
    local PKG="$1"
    local CHECK_BIN="$2"

    # 1.  Is the package already installed AND healthy?
    if dpkg -s "$PKG" 2>/dev/null | grep -q "^Status: install ok installed"; then
        echo "$PKG already installed — skipping."
        # Extra safety: if a binary was requested, make sure it really exists
        if [[ -n "$CHECK_BIN" && ! -x "$CHECK_BIN" ]]; then
            echo "$PKG is flagged installed but $CHECK_BIN is missing."
            echo "    Will reinstall to repair."
        else
            return 0
        fi
    fi

    # 2.  Dry‑run so we fail fast on unresolved dependencies
    if ! apt-get -s install "$PKG" >/dev/null 2>&1; then
        echo "$PKG cannot be installed (broken dependencies)."
        exit 1
    fi

    # 3.  Real installation
    echo "➜ Installing $PKG ..."
    if echo "$PASS" | sudo -S DEBIAN_FRONTEND=noninteractive apt-get install -y "$PKG"; then
        echo "$PKG installed."
        # 4.  If the caller asked for a binary, verify it
        if [[ -n "$CHECK_BIN" && ! -x "$CHECK_BIN" ]]; then
            echo "$PKG installed but $CHECK_BIN not found — aborting."
            exit 1
        fi
    else
        echo "Failed to install $PKG."
        exit 1
    fi
}



###########################################################################

export DEBIAN_FRONTEND=noninteractive  # Prevents interactive prompts

# ----- 1. Update package lists
echo "Updating system packages..."
echo "$PASS" | sudo -S apt update || echo "Warning: Failed to update package lists. Continuing..."

# ----- 2. Pin R to version 4.5.1 -----
echo "Pinning R version to 4.5.1..."
echo "$PASS" | sudo tee /etc/apt/preferences.d/pin-r-version <<EOF
Package: r-base r-base-core r-base-dev r-recommended r-cran-*
Pin: version 4.5.1-*
Pin-Priority: 1001
EOF

# ----- 3. Lock R packages to prevent future upgrades -----
echo "Marking R packages as held..."
echo "$PASS" | sudo -S apt-mark hold r-base r-base-core r-recommended



echo "$PASS" | sudo -S DEBIAN_FRONTEND=noninteractive apt-get \
  -o Dpkg::Options::="--force-confdef" \
  -o Dpkg::Options::="--force-confold" \
  upgrade -y


echo "Upgrading system packages..."
echo "$PASS" | sudo -S apt-get upgrade -y \
  -o Dpkg::Options::="--force-confdef" \
  -o Dpkg::Options::="--force-confold"



# Ensure partially installed packages are configured
echo "Configuring partially installed packages..."
echo "$PASS" | sudo -S dpkg --configure -a

# Ensure any broken packages are fixed before proceeding
echo "Fixing broken packages if any..."
echo "$PASS" | sudo -S apt-get install -f -y
#echo "$PASS" | sudo -S apt --fix-broken install -y
#echo "$PASS" | sudo -S apt autoremove -y
#echo "$PASS" | sudo -S apt clean


# ----- 6. Install specific R 4.5.1 version -----
echo "Installing R version 4.5.1..."
echo "$PASS" | sudo -S apt-get install -y \
  r-base-core=4.5.1-1.2004.0 \
  r-base=4.5.1-1.2004.0 \
  r-recommended=4.5.1-1.2004.0
# ----- 7. Verify installation -----
echo "Verifying R installation..."
R --version


echo "Checking R installation..."
R --version && echo "R installed successfully!" || echo "Error: R installation failed."

#echo "Installing missing dependencies..."
#echo "$PASS" | sudo -S apt install -f -y
# ----- 8. Final cleanup -----
echo "$PASS" | sudo -S apt-get autoremove -y
echo "$PASS" | sudo -S apt-get clean

echo "Installing build-essential and required libraries..."
echo "$PASS" | sudo -S apt install -y build-essential pkg-config autoconf automake libtool cmake libssl-dev

echo "Installing critical libraries (libmariadb-dev: libgeotiff-dev, default-libmysqlclient-dev, libspatialite-dev)..."

#unhold packages if pinned
echo "$PASS" | sudo -S apt-mark unhold libmariadb-dev libmariadb-dev-compat || true

# remove MariaDB headers
echo "$PASS" | sudo -S apt purge -y libmariadb-dev libmariadb-dev-compat

# install correct MySQL headers
echo "$PASS" | sudo -S apt install -y libmysqlclient-dev default-libmysqlclient-dev

#install_package libmariadb-dev 
#install_package libmariadb-dev-compat

#echo "$PASS" | sudo -S apt install -y libmysqlclient-dev default-libmysqlclient-dev || true #error was coming on VEp if not installed  


#/usr/bin/mariadb_config
#echo "$PASS" | sudo -S apt-mark manual libmariadb-dev
#echo "$PASS" | sudo -S apt-mark hold libmariadb-dev


#echo "Installing critical spatial libraries (for sf, geospatial)..."
#install_package gdal-bin 
#install_package libgdal-dev /usr/bin/gdal-config

#install_package libgeotiff-dev
#install_package libspatialite-dev


# 🔒 Lock GDAL-related packages to avoid removal
#echo "$PASS" | sudo -S apt-mark manual gdal-bin libgdal-dev
#echo "$PASS" | sudo -S apt-mark hold gdal-bin libgdal-dev

#echo "Verifying gdal-config is available..."
#if ! command -v gdal-config >/dev/null 2>&1; then
#    echo "ERROR: gdal-config not found after installing libgdal-dev."
#    echo "Trying to find manually..."
#    sudo find / -name gdal-config 2>/dev/null || echo "Not found anywhere on system!"
#    exit 1
#else
#    echo "gdal-config found at $(which gdal-config)"
#fi

echo "All critical libraries installed successfully."

echo "Installing required system libraries..."

# Install required packages
install_package libisal-dev   # Required for FastP
install_package libdeflate-dev
install_package libz-dev    # Required for Bowtie2
install_package zlib1g-dev
install_package libc6-dev
install_package libc6-dev-i386
install_package libc6-dev-amd64
install_package libbz2-dev #For samtools
install_package libproj-dev   # For R Package
install_package libudunits2-dev
install_package libgit2-dev  #For gert
install_package libpq-dev  #For RPostgreSQL
#install_package gdal-bin	# Required for sf 
#install_package libgdal-dev	# Includes gdal-config needed by R packages

install_package libcairo2-dev # For Cario 
install_package libfreetype6-dev   #For freetype2 
install_package libpng-dev
install_package libtiff-dev
install_package libtiff5-dev
install_package libjpeg-dev
install_package libcurl4-openssl-dev #For curl

# Add missing dependencies for edgeR
install_package libblas-dev  # Required for LAPACK & BLAS
install_package liblapack-dev  # Required for LAPACK & BLAS
install_package gfortran # For Package dotCall64, sf, classInt, ash
install_package libgfortran5
install_package libgfortran-10-dev

install_package libxml2-dev #For xml2
install_package libharfbuzz-dev #For textshaping
install_package libfribidi-dev 
install_package cpanminus #For Perl
install_package libhts-dev  #For VEP Bio::DB::HTS
install_package liblzma-dev
#install_package libmariadb-dev # For RMySQL RPackage 
install_package libeigen3-dev   # `tatami` uses Eigen
install_package libboost-dev    # Often required for advanced C++ packages
install_package libv8-dev      # Required for V8 Package 
#install_package libfswatch-dev # For watcher
echo "============================================"
echo "DEACTIVATING CONDA..."
echo "============================================"


# Check if Conda is active and deactivate it
if command -v conda &>/dev/null && type -t conda | grep -q function; then
echo "Conda is active. Deactivating..."
    conda deactivate
    else
    echo "Conda is not initialized in this shell. Skipping deactivation."
fi

# Unset Conda-specific environment variables
unset CONDA_DEFAULT_ENV
unset CONDA_PREFIX
unset CONDA_SHLVL
unset _CE_CONDA
unset _CONDA_ROOT
unset _CONDA_EXE
# Remove Conda paths from PATH and LD_LIBRARY_PATH
export PATH=$(echo "$PATH" | sed -e 's/:\/home\/.*\/anaconda3\/bin//g')
export LD_LIBRARY_PATH=$(echo "$LD_LIBRARY_PATH" | sed -e 's/:\/home\/.*\/anaconda3\/lib//g')


# Ensure correct compiler is used
export PATH=/usr/bin:/usr/local/bin:$PATH
export CC=/usr/bin/gcc
export CXX=/usr/bin/g++

export CXXFLAGS="-Wno-format-security"
export CFLAGS="-Wno-format-security"
export R_CXXFLAGS="-Wno-format-security"
export R_CFLAGS="-Wno-format-security"

export R_MAKEVARS_USER="~/.R/Makevars"

mkdir -p ~/.R
echo "CXXFLAGS+=-Wno-format-security" >> ~/.R/Makevars
echo "CFLAGS+=-Wno-format-security" >> ~/.R/Makevars
echo "PKG_CFLAGS+=-Wno-format-security" >> ~/.R/Makevars
echo "PKG_LIBS+=-Wno-format-security" >> ~/.R/Makevars


# Verify compiler versions
echo "Using GCC: $(gcc --version | head -n 1)"
echo "Using G++: $(g++ --version | head -n 1)"

#
echo "F77 = gfortran" | sudo -S tee -a /usr/lib/R/etc/Makeconf
echo "FC = gfortran" | sudo -S tee -a /usr/lib/R/etc/Makeconf

#Setting up RLibraries
# Ensure the file exists
RENV="$HOME/.Renviron"
touch "$RENV"
# If R_LIBS_USER is already set, replace it; otherwise append it
if grep -q '^R_LIBS_USER=' "$RENV"; then
  # update the existing line
  sed -i "s|^R_LIBS_USER=.*|R_LIBS_USER=$R_LIB|" "$RENV"
else
  # append a new setting
  echo "R_LIBS_USER=$R_LIB" >> "$RENV"
fi
echo "Updated $RENV:"
grep '^R_LIBS_USER=' "$RENV"
#----------------------

# Ensure pkg-config is installed
if ! command -v pkg-config &> /dev/null; then
    echo "Error: pkg-config not installed. Installing..."
    sudo apt install -y pkg-config
fi


# Find the correct pkg-config path

PKG_CONFIG_DIR=$(pkg-config --variable=pc_path pkg-config | tr ':' '\n' | while read dir; do
    if [ -d "$dir" ]; then
        echo "$dir"
        break
    fi
done)

echo "========================================================"
echo "PKG_CONFIG_DIR: $PKG_CONFIG_DIR"
echo "========================================================"

# Set PKG_CONFIG_PATH

if [ -z "$PKG_CONFIG_PATH" ]; then
    export PKG_CONFIG_PATH=$PKG_CONFIG_DIR
else
    export PKG_CONFIG_PATH=$PKG_CONFIG_DIR:$PKG_CONFIG_PATH
fi

if [ -z "$LD_LIBRARY_PATH" ]; then
    export LD_LIBRARY_PATH=/usr/lib/x86_64-linux-gnu
else
    export LD_LIBRARY_PATH=/usr/lib/x86_64-linux-gnu:$LD_LIBRARY_PATH
fi

# Function to clean pkg-config output
clean_pkg_config() {
    pkg_output=$(pkg-config "$1" "$2" 2>/dev/null | tr ' ' '\n' | grep -v '^$' | sort -u)
    clean_output=""
    for path in $pkg_output; do
        if [[ $path != -I* && $path != -L* ]]; then
            path="$3$path"  # Add -I or -L only if missing
        fi
        clean_output="$clean_output $path"
    done
    echo "$clean_output" | tr -s ' '  # Remove extra spaces
}



# Get includes and libs, ensuring they are properly formatted
ZLIB_INCLUDE=$(clean_pkg_config --cflags-only-I zlib -I)
ZLIB_LIB=$(clean_pkg_config --libs-only-L zlib -L)

FREETYPE_INCLUDE=$(clean_pkg_config --cflags-only-I freetype2 -I)
FREETYPE_LIB=$(clean_pkg_config --libs-only-L freetype2 -L)

LIBXML2_INCLUDE=$(clean_pkg_config --cflags-only-I libxml-2.0 -I)
LIBXML2_LIB=$(clean_pkg_config --libs-only-L libxml-2.0 -L)

# If pkg-config fails for zlib, set default paths manually
if [ -z "$ZLIB_INCLUDE" ]; then ZLIB_INCLUDE="-I/usr/include"; fi
if [ -z "$ZLIB_LIB" ]; then ZLIB_LIB="-L/usr/lib/x86_64-linux-gnu"; fi

if [ -z "$FREETYPE_INCLUDE" ]; then FREETYPE_INCLUDE="-I/usr/include/freetype2"; fi
if [ -z "$FREETYPE_LIB" ]; then FREETYPE_LIB="-L/usr/lib/x86_64-linux-gnu"; fi

remove_duplicates() {
    local seen=() unique=()
    for path in $1; do
        if [[ ! " ${seen[@]} " =~ " $path " ]]; then
            seen+=("$path")
            unique+=("$path")
        fi
    done
    echo "${unique[*]}"
}



INCLUDE_DIR=$(remove_duplicates "$ZLIB_INCLUDE $FREETYPE_INCLUDE -I/usr/include -I/usr/include/x86_64-linux-gnu")
LIB_DIR=$(remove_duplicates "$ZLIB_LIB $FREETYPE_LIB -L/usr/lib/x86_64-linux-gnu")
PKG_CFLAGS=$(remove_duplicates "$ZLIB_INCLUDE $FREETYPE_INCLUDE $LIBXML2_INCLUDE -I/usr/include/x86_64-linux-gnu -DIS_LITTLE_ENDIAN")
PKG_LIBS=$(remove_duplicates "$ZLIB_LIB $FREETYPE_LIB $LIBXML2_LIB -L/usr/lib/x86_64-linux-gnu -lz -lfreetype -lssl -lcrypto  -lxml2 ")

# Add MariaDB flags

INCLUDE_DIR="$INCLUDE_DIR -I/usr/include/mariadb"
LIB_DIR="$LIB_DIR -L/usr/lib/x86_64-linux-gnu -L/usr/lib"
PKG_CFLAGS="$PKG_CFLAGS -I/usr/include/mariadb"
PKG_LIBS="$PKG_LIBS -lmysqlclient"

# Export final values
export INCLUDE_DIR="$INCLUDE_DIR"
export LIB_DIR="$LIB_DIR"
export PKG_CFLAGS="$PKG_CFLAGS -Wno-format-security"
export PKG_LIBS="$PKG_LIBS -Wno-format-security"

export LAPACK_LIBS="-L/usr/lib/x86_64-linux-gnu -llapack"
export BLAS_LIBS="-L/usr/lib/x86_64-linux-gnu -lblas"

# STEP 5: Also append to Makevars for R to pick up
echo "PKG_CFLAGS+=-I/usr/include/mariadb" >> ~/.R/Makevars
echo "PKG_LIBS+=-L/usr/lib/x86_64-linux-gnu -lmysqlclient" >> ~/.R/Makevars

echo "========================================================"
echo "CXXFLAGS: $CXXFLAGS"
echo "CFLAGS: $CFLAGS"
echo "PKG_CONFIG_PATH: $PKG_CONFIG_PATH"
echo "INCLUDE_DIR: $INCLUDE_DIR"
echo "LIB_DIR: $LIB_DIR"
echo "PKG_CFLAGS: $PKG_CFLAGS"
echo "PKG_LIBS: $PKG_LIBS"
echo "LAPACK_LIBS: $LAPACK_LIBS"
echo "BLAS_LIBS= $BLAS_LIBS"
echo "========================================================"

# Check if zlib.h exists
# Check if zlib.h exists in any of the INCLUDE_DIR paths
FOUND_ZLIB_H=false
#for dir in $INCLUDE_DIR; do
for dir in $(echo "$INCLUDE_DIR" | tr ' ' '\n' | grep -oP '(?<=-I).+'); do
    if [ -f "$dir/zlib.h" ]; then
        FOUND_ZLIB_H=true
        break
    fi
done

if [ "$FOUND_ZLIB_H" = false ]; then
    echo "Error: zlib.h not found in any include directories ($INCLUDE_DIR). Installation failed."
    exit 1
fi


if [ ! -f "/usr/include/bits/libc-header-start.h" ]; then
    echo "Error: Missing libc development headers. Installing..."
    sudo -S apt install -y libc-dev
fi


# Verify presence of required headers
FOUND_ALL_HEADERS=true
for header in freetype/freetype.h png.h tiff.h jpeglib.h; do
    FOUND=false
 #   for dir in $INCLUDE_DIR; do
 for dir in $(echo "$INCLUDE_DIR" | tr ' ' '\n' | grep -oP '(?<=-I).+'); do
          if [ -f "$dir/$header" ]; then
            FOUND=true
            break
        fi
    done
    if [ "$FOUND" = false ]; then
        echo "Error: Missing $header in $INCLUDE_DIR. Ensure required dev packages are installed."
        FOUND_ALL_HEADERS=false
    fi
done

if [ "$FOUND_ALL_HEADERS" = false ]; then
    exit 1
fi


sudo ldconfig


echo "============================================"
echo "System dependencies successfully installed."
echo "============================================"

# Function to check for errors and stop if one occurs
check_error() {
    if [ $? -ne 0 ]; then
        echo "Error occurred during: $1" | tee -a "$LOG_PATH"
        exit 1
    fi
}
#CHECKING TO INSTALL PERL OR NOT
#!/bin/bash

# Check if Perl is installed
if command -v perl &>/dev/null; then
    # Get installed Perl version
    PERL_VERSION=$(perl -e 'print $];')
    
    # Ensure `bc` is available
    command -v bc >/dev/null || { echo "Warning: 'bc' is not installed. Skipping Perl version check."; }
if [[ -n "$PERL_VERSION" && $(echo "$PERL_VERSION >= 5.010000" | bc -l) -eq 1 ]]; then
        echo "Perl version $PERL_VERSION is already installed (>=5.10). Skipping installation."
    else
        echo "Perl version is outdated ($PERL_VERSION). Installing newer version..."
        INSTALL_PERL=true
    fi
else 
echo "Perl is not installed. Installing..."
    INSTALL_PERL=true    
fi

# Install Perl only if needed
if [[ $INSTALL_PERL == true ]]; then
    INSTALL_PATH="$HOME/perl"

    # Extract Perl source
    tar -xvzf perl-5.40.1.tar.gz || { echo "Error extracting Perl source."; return 1; }
    cd perl-5.40.1 || { echo "Error: Failed to enter Perl source directory."; return 1; }

    # Configure, compile, and install in home directory
    ./Configure -des -Dprefix="$INSTALL_PATH" || { echo "Configure failed."; return 1; }
    make -j$(nproc) || { echo "Make failed."; return 1; }
    make install || { echo "Make install failed."; return 1; }

    # Add Perl to PATH
    if ! echo "$PATH" | grep -q "$INSTALL_PATH/bin"; then
        echo "export PATH=$INSTALL_PATH/bin:\$PATH" >> ~/.bashrc
    fi
    export PATH="$INSTALL_PATH/bin:$PATH"  # Update PATH immediately

    # Verify installation
    "$INSTALL_PATH/bin/perl" -v || { echo "Perl installation verification failed."; return 1; }

    echo "✅ Perl installed successfully in $INSTALL_PATH"
fi

# Set local perl lib path
#export PERL5LIB=~/perl5/lib/perl5:$PERL5LIB
#export PATH=~/perl5/bin:$PATH
# Set local perl lib path (add only if missing)
if [ ":$PATH:" != *":$HOME/perl5/bin:"* ]; then
    export PATH="$HOME/perl5/bin:$PATH"
fi

if [ ":$PERL5LIB:" != *":$HOME/perl5/lib/perl5:"* ]; then
    export PERL5LIB="$HOME/perl5/lib/perl5:$PERL5LIB"
fi


#echo 'export PERL5LIB=~/perl5/lib/perl5:$PERL5LIB' >> ~/.bashrc
#echo 'export PATH=~/perl5/bin:$PATH' >> ~/.bashrc


# Function to log and install software if not already installed
# Function to install software and log output in real time
install_software() {
    SOFTWARE=$1
    INSTALL_DIR=$2
    BUILD_COMMANDS=$3

    echo "Installing $SOFTWARE..." | tee -a "$LOG_PATH"

    # Run build commands with real-time logging
    eval "$BUILD_COMMANDS" 2>&1 | stdbuf -oL -eL tee -a "$LOG_PATH"

    # Check for errors
    if [[ ${PIPESTATUS[0]} -ne 0 ]]; then
        echo "Error: Installation of $SOFTWARE failed! Check $LOG_PATH for details." | tee -a "$LOG_PATH"
        exit 1  # Stop script on error
    fi

    echo "$SOFTWARE installed successfully." | tee -a "$LOG_PATH"
}
# Example installation commands for each software

install_software "fastp" "$FASTP_PATH" "cd $FASTP_PATH && make clean && make"
install_software "STAR" "$STAR_PATH" "cd $STAR_PATH && make STAR"
install_software "bwa" "$BWA_PATH" "cd $BWA_PATH && CPATH=/usr/include LIBRARY_PATH=/lib/x86_64-linux-gnu LD_LIBRARY_PATH=/lib/x86_64-linux-gnu make"
install_software "samtools" "$SAMTOOLS_PATH" "cd $SAMTOOLS_PATH && make"
export PATH=${SAMTOOLS_PATH}:$PATH
install_software "bcftools" "$BCFTOOLS_PATH" "cd $BCFTOOLS_PATH && make clean && ./configure --enable-plugins --prefix=$BCFTOOLS_PATH && make && make install"
export PATH=${BCFTOOLS_PATH}:$PATH
export BCFTOOLS_PLUGINS=$BCFTOOLS_PATH/plugins
source ~/.bashrc

install_software "HTS" "$HTSLIB_PATH" "cd \"$HTSLIB_PATH\" && make && echo \"$PASS\" | sudo -S make install"
install_software "ensembl-vep" "$VEP_PATH" "echo $PASS | sudo -S cpanm -n List::MoreUtils DBD::mysql Test::Warnings XML::LibXML XML::LibXML::Reader Bio::SeqFeature::Lite && cd $VEP_PATH && echo $PASS | sudo -S perl INSTALL.pl --AUTO a --NO_HTSLIB --NO_TEST --NO_UPDATE --CACHEDIR $VEP_PATH/cache --SPECIES homo_sapiens --CACHE_VERSION 114 && export HTSLIB_DIR=$HTSLIB_PATH && echo $PASS | sudo -S cpanm -n Bio::DB::HTS" #--local-lib=~/perl5 
CACHE_VERSION=114
SPECIES=homo_sapiens
if [ ! -d "$VEP_PATH/cache/homo_sapiens" ]; then
    echo "Downloading VEP cache..."
    mkdir -p $VEP_PATH/cache
    echo 'Downloading VEP cache for $SPECIES (version $CACHE_VERSION)...'
    wget -c ftp://ftp.ensembl.org/pub/release-\${CACHE_VERSION}/variation/VEP/\${SPECIES}_vep_\${CACHE_VERSION}_CACHE.tar.gz \
        -O \${SPECIES}_vep_\${CACHE_VERSION}_CACHE.tar.gz
    echo 'Extracting VEP cache...'
    tar -xzf \${SPECIES}_vep_\${CACHE_VERSION}_CACHE.tar.gz -C $VEP_PATH/cache --strip-components=1 &&

     # tell VEP to register the cache (without re-downloading)
    perl INSTALL.pl --AUTO c --SPECIES homo_sapiens --CACHE_VERSION 114 --CACHEDIR $VEP_PATH/cache

fi
#install_software "vcf2maf" "$vcf2maf_PATH" "cd \"$vcf2maf_PATH\" && echo \"$PASS\" | sudo -S cpanm -n DBI DBD::SQLite JSON Try::Tiny File::Which List::Util List::MoreUtils && export PATH=$vcf2maf_PATH:\$PATH"

echo "Installation completed successfully." | tee -a "$LOG_PATH"

