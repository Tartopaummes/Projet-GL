#! /bin/sh

# Author : Mael
# Created on  : 07/01/2025

# Test for the decompilation (option -p of compiler).
# Principle:
# - Decompile a initial .deca file --> save the output in a .deca file
# - Decompile this new file --> save the output in a .deca file.
# - Compare the The two generated .deca files (must be equals)
#
#
#Args :
# use arg --keep_files to don't remove generated files at the end of the script

cd "$(dirname "$0")"/../../.. || exit 1

PATH=./src/test/script/launchers:"$PATH"

DIRECTORY=src/test/deca/decompile
GENERATED_DIRECTORY=$DIRECTORY/generated

KEEP_FILES=false
# Check if the second argument is "keep_files"
if [ "$1" = "--keep_files" ]; then
  KEEP_FILES=true
fi

RED='\033[0;31m'
GREEN='\033[0;32m'
PURPLE='\033[0;35m'
LIGHT_BLUE='\033[1;34m'
LIGHT_PURPLE='\033[1;35m'
BROWN_ORANGE='\033[0;33m'
NC='\033[0m'

err=0
nb_tests=0

# Function to test if the given file is contextually invalid.
test_decompile () {
    INITIAL_DECA_FILE=$1
    INITIAL_DECA_FILE_NAME=$(basename $INITIAL_DECA_FILE ".deca")

    #First decompilation
    GENERATED_DECA_FILE="${GENERATED_DIRECTORY}/${INITIAL_DECA_FILE_NAME}_bis.deca"
    ERR_MSG=$(decac -p "$INITIAL_DECA_FILE" > "${GENERATED_DECA_FILE}" 2>&1)
    # Test is the output is a build error.
    test_build_error "$INITIAL_DECA_FILE" "$ERR_MSG"
    # Test is the output is an error.
    test_error "$INITIAL_DECA_FILE" "$GENERATED_DECA_FILE"

    # Second decompilation
    GENERATED_DECA_FILE_2="${GENERATED_DIRECTORY}/${INITIAL_DECA_FILE_NAME}_ter.deca"
    ERR_MSG=$(decac -p "$GENERATED_DECA_FILE" > "${GENERATED_DECA_FILE_2}" 2>&1)
    # Test is the output is a build error.
    test_build_error "$GENERATED_DECA_FILE" "$ERR_MSG"
        # Test is the output is an error.
    test_error "$GENERATED_DECA_FILE" "$GENERATED_DECA_FILE_2"

    #compare the two file
    if diff -q "$GENERATED_DECA_FILE" "$GENERATED_DECA_FILE_2" >/dev/null; then
         echo "${GREEN}Intended success${NC} of decac -p on ${PURPLE}$INITIAL_DECA_FILE${NC}."
    else
        FILE_CONTENT_1=$(cat "$GENERATED_DECA_FILE")
        FILE_CONTENT_2=$(cat "$GENERATED_DECA_FILE_2")
        err=$((err + 1))
        echo "${RED}Unintended failure${NC} of decac -p on ${PURPLE}$INITIAL_DECA_FILE${NC}. The two decompiled files are different got first ${LIGHT_BLUE}$FILE_CONTENT_1${NC}  and ${LIGHT_BLUE}$FILE_CONTENT_2${NC}"
    fi
}



# Function to test if the build failed.
test_build_error() {
    # Scan the error message for build failure.
    if echo "$2" | grep -q -e "^Exception in thread"
    then
        echo ""
        echo "${RED}Unintended failure${NC} of decac -p on ${PURPLE}$1${NC}."
        echo "${LIGHT_PURPLE}Build failed: ${RED}$2${NC}."
        err=$((err + 1))
    fi
}

# Function to test if the output is an error
test_error(){
  if grep -q "$1" "$2";
  then
      FILE_CONTENT=$(cat "$2")
      echo ""
      echo "${RED}Unintended failure${NC} of decac -p on ${PURPLE}$1${NC}."
      echo "${LIGHT_PURPLE}Got error: ${RED}$FILE_CONTENT${NC}."
      err=$((err + 1))
  fi
}

# rm generated directory before exit
safe_exit(){
  EXIT_CODE=$1
  if [ "$KEEP_FILES" != true ]; then
      rm -f "${GENERATED_DIRECTORY}"/*
      rmdir "$GENERATED_DIRECTORY"
  fi
  exit $EXIT_CODE
}

mkdir -p $GENERATED_DIRECTORY
for test_case in $(find $DIRECTORY -name '*.deca' -not -path "*/generated/*" -not -path "*/invalid/*" -not -path "*/perf/*")
do
    nb_tests=$((nb_tests + 1))
    test_decompile "$test_case"
done

success_rate=$((100 - err * 100 / nb_tests))
# Save the number of errors and tests
src/test/script/tools/save_to_file.sh $err $nb_tests

echo ""
echo "${BROWN_ORANGE}Context tests finished with ${LIGHT_BLUE}${success_rate}%${BROWN_ORANGE} success. ${NC}"
if [ $err -eq 0 ]
then
    echo "${GREEN}All tests passed${NC}"
    safe_exit 0
else
    echo "${RED}$err tests failed${NC} out of ${LIGHT_BLUE}$nb_tests${NC}"
    safe_exit 1
fi