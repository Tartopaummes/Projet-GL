#! /bin/sh

# Auteur : gl34
# Version initiale : 01/01/2025

# This script compiles all .deca files in the valid folder, runs ima on the generated .ass files,
# and compares the result with the expected value.

cd "$(dirname "$0")"/../../.. || exit 1

PATH=./src/test/script/launchers:./src/main/bin:"$PATH"

RED='\033[0;31m'
GREEN='\033[0;32m'
PURPLE='\033[0;35m'
LIGHT_BLUE='\033[1;34m'
LIGHT_PURPLE='\033[1;35m'
BROWN_ORANGE='\033[0;33m'
NC='\033[0m'

err=0
nb_tests=0

test_folder(){
  folder=$1
  must_fail=$2

  # Iterate over all .deca files in the valid folder
  for deca_file in $(find "$folder"  -name "*.deca" -not -path "*/provided/*")
  do
      nb_tests=$((nb_tests+1))
      # Remove the .ass file if it exists
      ass_file="${deca_file%.deca}.ass"
      rm -f "$ass_file" 2>/dev/null

      compile_msg=$(decac "$deca_file" 2>&1)
      # Compile the .deca file
      if ! ( echo $? | grep 0 > /dev/null ); then
          echo "${RED}Compilation failed${NC} for ${PURPLE}$deca_file${NC}"
          echo "${LIGHT_PURPLE}Got: ${RED}$compile_msg${NC}"
          err=$((err+1))
          continue
      fi

      # Check if the .ass file was generated
      if [ ! -f "$ass_file" ]; then
          echo "File $(basename "$ass_file") not generated."
          err=$((err+1))
          continue
      fi

      # Extract the expected value from the .deca file, ignoring lines starting with //
      expected=$(sed -n '/\[Begin Result\]/,/\[End Result\]/p' "$deca_file" | sed 's/^\/\///' | sed '1d;$d')


      # Extract the input values from the .deca file, ignoring lines starting with //.  If it exist
      input=$(sed -n '/\[Begin Input\]/,/\[End Input\]/p' "$deca_file" | sed 's/^\/\///' | sed '1d;$d')

      # Check if the input is empty
      if [ -n "$input" ]; then
        decainput_file="${deca_file%.deca}.decainput"
        #put the input into a .decainput file
        echo "$input" > "$decainput_file"
        # Run ima on the .ass file with the .decainput file and capture the result
        result=$(ima "$ass_file" < "$decainput_file")
        # Remove the .decainput file
        rm -f "$decainput_file" 2>/dev/null

      else
          # Run ima on the .ass file and capture the result
          result=$(ima "$ass_file")
      fi

      # verifiy if there is no execution vail (only if the programme mustn't failed
      if [ "$must_fail" = 0 ]; then
        if [ $? -ne 0 ]; then
            echo "${RED}Execution failed${NC} for ${PURPLE}$ass_file${NC}"
            echo "${LIGHT_PURPLE}Got: ${RED}$result${NC}"
            err=$((err+1))
            continue
        fi
      fi
      rm -f "$ass_file"

      # Compare the result with the expected value
      if [ "$result" = "$expected" ]; then
          echo "${GREEN}Test passed${NC} for codegen on${PURPLE} $deca_file${NC}"
      else
          err=$((err+1))
          echo "${RED}Unexpected result ${NC} for ${PURPLE} $deca_file${NC}"
          echo "${RED}$result ${NC}"
          echo "${LIGHT_PURPLE}Expected :${NC}"
          echo "${LIGHT_BLUE}$expected${NC}"
      fi
  done
}


test_folder "src/test/deca/codegen/valid/" 0
test_folder "src/test/deca/codegen/invalid/" 1

success_rate=$((100 - err * 100 / nb_tests))
# Save the number of errors and tests
src/test/script/tools/save_to_file.sh $err $nb_tests

echo ""
echo "${BROWN_ORANGE}Codegen tests finished with ${LIGHT_BLUE}${success_rate}%${BROWN_ORANGE} success. ${NC}"
if [ $err -eq 0 ]
then
    echo "${GREEN}All tests passed${NC}"
    exit 0
else
    echo "${RED}$err tests failed${NC} out of ${LIGHT_BLUE}$nb_tests${NC}"
    exit 1
fi