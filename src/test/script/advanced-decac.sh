#! /bin/sh

# Test the options

PATH=./src/main/bin:"$PATH"
cd "$(dirname "$0")"/../../.. || exit 1

RED='\033[0;31m'
GREEN='\033[0;32m'
PURPLE='\033[0;35m'
LIGHT_BLUE='\033[1;34m'
LIGHT_PURPLE='\033[1;35m'
BROWN_ORANGE='\033[0;33m'
NC='\033[0m'

err=0
nb_tests=0
test_decac_b(){
  nb_tests=$((nb_tests+1))
  decac_moins_b=$(decac -b)

  if [ "$?" -ne 0 ]; then
      echo "ERREUR: decac -b a termine avec un status different de zero."
      err=$((err+1))
      exit 1
  fi

  if [ "$decac_moins_b" = "" ]; then
      echo "ERREUR: decac -b n'a produit aucune sortie"
      err=$((err+1))
      exit 1
  fi

  if echo "$decac_moins_b" | grep -i -e "erreur" -e "error"; then
      echo "ERREUR: La sortie de decac -b contient erreur ou error"
      err=$((err+1))
      exit 1
  fi

  echo "${GREEN}Test passed${NC} No problem with decac -b."
  return 0
}



remove_ass(){
  # Remove the .ass files if it exists
  rm -f "${deca_file1%.deca}.ass" 2>/dev/null
  rm -f "${deca_file2%.deca}.ass" 2>/dev/null
}

compile_and_check_good_compilation(){
  #compile the .deca file and verify if the compilation has failed
  if [ "$option" = "" ]; then
    if [ "$deca_file2" = "" ]; then
      compile_msg=$(decac "$deca_file1" 2>&1)
    else
      compile_msg=$(decac "$deca_file1" "$deca_file2" 2>&1)
    fi
  else
    if [ "$option" = "-r" ]; then
      if [ "$deca_file2" = "" ]; then
        compile_msg=$(decac "$option" "$check_register" "$deca_file1" 2>&1)
      else
        compile_msg=$(decac "$option" "$check_register" "$deca_file1" "$deca_file2" 2>&1)
      fi
    else
      if [ "$deca_file2" = "" ]; then
        compile_msg=$(decac "$option" "$deca_file1" 2>&1)
      else
        compile_msg=$(decac "$option" "$deca_file1" "$deca_file2" 2>&1)
      fi
    fi
  fi


  #check
  if ! ( echo $? | grep 0 > /dev/null ); then
      echo "${RED}Compilation failed${NC} for ${PURPLE}$command $deca_file1 $deca_file1${NC}"
      echo "${LIGHT_PURPLE}Got: ${RED}$compile_msg${NC}"
      err=$((err+1))
      return 1
  fi
  return 0
}
check_ass_generated(){
  # Check if the .ass file has been generated
  if [ ! -f "$ass_file" ]; then
      echo "${RED}Execution failed${NC} for ${PURPLE}$ass_file${NC} with ${PURPLE}$command $deca_file1 $deca_file2${NC}. File $(basename "$ass_file") not generated."
      err=$((err+1))
      return 1
  fi
  return 0
}

check_ima_execution(){
  # check if there is an execution error during executing the .ass file with ima
  if [ $? -ne 0 ]; then
    echo "${RED}Execution failed${NC} for ${PURPLE}$ass_file${NC} with ${PURPLE}$command $deca_file1 $deca_file2${NC}"
    echo "${LIGHT_PURPLE}Got: ${RED}$result${NC}"
    err=$((err+1))
    return 1
  fi
  return 0
}
check_registers_in_ass(){
  # Construct the dynamic regular expression
  regex="R("
  i=$check_register
  while [ $i -le 16 ]; do
    regex="$regex$i|"
    i=$((i + 1))
  done
  regex="${regex%|})"  # Remove the trailing '|'

  grep_output="$(grep -E "$regex" "$ass_file")"
  if [ -n "$grep_output" ] ; then
    err=$((err+1))
    echo "${RED}Unexpected result ${NC} for ${PURPLE}$command $deca_file${NC}"
    echo "${LIGHT_PURPLE}Expected : no use of a register greater than or equal to R$check_register ${NC}"
    echo "${RED}But find : $grep_output${NC}"
    return 1
  else
    #Test passed
    return 0
  fi
}


check_result(){
  # Compare the result with the expected value
  if [ "$result" = "$expected" ]; then
    if [ "$check_register" -gt 3 ]; then
      if ! check_registers_in_ass; then
        return 1
      fi
    fi
    #Test passed
    return 0
  else
      err=$((err+1))
      echo "${RED}Unexpected result ${NC} for ${PURPLE}$command $deca_file${NC}"
      echo "${RED}$result ${NC}"
      echo "${LIGHT_PURPLE}Expected :${NC}"
      echo "${LIGHT_BLUE}$expected${NC}"
      return 1
  fi
}

check_not_contains(){
  # check if the result contain the string result_must_not_contain
  echo "$result" | grep -q "$result_must_not_contain"

  if [ $? -eq 0 ]; then
    echo "The result contains the string '$result_must_not_contain'"
    echo "${RED}Unexpected result ${NC} for ${PURPLE}$command $deca_file${NC}"
    echo "${RED}$result ${NC}"
    echo "${LIGHT_PURPLE}Expected : does not contain${NC}"
    echo "${LIGHT_BLUE}$result_must_not_contain${NC}"
    return 1
  else
    #Test passed
    return 0
  fi
}

test_file(){
  option="$1"
  deca_file1="$2"
  deca_file2="$3"
  parse_expected_result=$4 #=1 to parse expected_result from deca_file ($5 is override). If 0, expected result is $5
  expected="$5"
  must_generate_ass=$6 #=1 if the command must generate a ass file. =0 if not
  check_not_contain=$7 #=1 to to check if the result do not conatain result_must_not_contain. Don't check expected result.
  result_must_not_contain=$8
  check_register=$9 #if 0 do not check; if >0 (between 4 and 16) check in the .ass if there are no registers grater than check_register
  no_check_ima_execution="${10}" #if 1 don't check if the execution of the .ass file failed. If 0 check it

  nb_tests=$((nb_tests+1))
  command="decac $option"
  #remove ass files
  remove_ass

  # Call compile_and_check_good_compilation and check its return value
  if ! compile_and_check_good_compilation; then
    return 1
  fi

  for deca_file in "$deca_file1" "$deca_file2"; do
    if [ "$deca_file" = "" ]; then
      continue
    fi
    ass_file="${deca_file%.deca}.ass"

    if [ "$parse_expected_result" = 1 ]; then
      # Extract the expected value from the .deca file, ignoring lines starting with //
      expected=$(sed -n '/\[Begin Result\]/,/\[End Result\]/p' "$deca_file" | sed 's/^\/\///' | sed '1d;$d')
    fi


    if [ "$must_generate_ass" = 1 ]; then
      # Call check_ass_generated and check its return value
      if ! check_ass_generated; then
        return 1
      fi

      # Run ima on the .ass file and capture the result
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

        # Call check_ima_execution and check its return value
        if [ "$no_check_ima_execution" = 0 ]; then
          if ! check_ima_execution; then
            return 1
          fi
        fi
    fi

    if [ "$check_not_contain" = 1 ]; then
      if ! check_not_contains; then
        return 1
      fi
    else
      # Call check_result and check its return value
      if ! check_result; then
        return 1
      fi
    fi
  done

  remove_ass

  echo "${GREEN}Test passed${NC} for $command on ${PURPLE}$deca_file1 $deca_file2${NC}"

  return 0
}

test_decac_b
test_file "-v" "src/test/deca/codegen/valid/syracuse42.deca" "" 0 "" 0 0 "" 0 0
test_file "-v" "src/test/deca/codegen/valid/class/ln2_fct.deca" "" 0 "" 0 0 "" 0 0
test_file "" "src/test/deca/codegen/valid/syracuse42.deca" "src/test/deca/codegen/valid/ln2.deca" 1 "" 1 0 "" 0 0
test_file "-P" "src/test/deca/codegen/valid/syracuse42.deca" "src/test/deca/codegen/valid/ln2.deca" 1 "" 1 0 "" 0 0
for test_case in $(find src/test/deca/codegen/invalid -name '*.deca' -not -path "*/provided/*" -not -path "*/execution_error_null_dereferencing.deca"); do
  test_file "-n" "$test_case" "" 0 "" 1 1 "Error" 0 0
done
test_file "-n" "src/test/deca/codegen/invalid/execution_error_null_dereferencing.deca" "" 0 "" 1 1 "Error" 0 1
test_file "-r" "src/test/deca/codegen/valid/ln2.deca" "" 1 "" 1 0 "" 4 0
test_file "-r" "src/test/deca/codegen/valid/class/ln2_fct.deca" "" 1 "" 1 0 "" 4 0



success_rate=$((100 - err * 100 / nb_tests))
# Save the number of errors and tests
src/test/script/tools/save_to_file.sh $err $nb_tests

echo ""
echo "${BROWN_ORANGE}Deca options tests finished with ${LIGHT_BLUE}${success_rate}%${BROWN_ORANGE} success. ${NC}"
if [ $err -eq 0 ]
then
    echo "${GREEN}All tests passed${NC}"
    exit 0
else
    echo "${RED}$err tests failed${NC} out of ${LIGHT_BLUE}$nb_tests${NC}"
    exit 1
fi







