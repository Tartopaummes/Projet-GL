#! /bin/sh

# Author: gl34
# Initial version: 17/12/2024

# More advanced script to test the part B (contextual verification).
# Goes through all the files in src/test/deca/context and tests whether or not the expected results are given.
# The invalid tests are the ones in src/test/deca/context/invalid/.
# The valid tests are the ones in src/test/deca/context/valid/.

cd "$(dirname "$0")"/../../.. || exit 1

PATH=./src/test/script/launchers:"$PATH"

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
test_context_invalid () {
    DECA_FILE=$1
    ERR_MSG=$(test_context "$DECA_FILE" 2>&1)

    # Test is the error message is a build error.
    if ! test_build_error "$DECA_FILE" "$ERR_MSG"
    then
        err=$((err+1))
        return 1
    fi

    # Extract the expected value from the .deca file, ignoring lines starting with //
    EXPECTED_ERR_MSG=$(sed -n '/\[Begin Result\]/,/\[End Result\]/p' "$DECA_FILE" | sed 's/^\/\///' | sed '1d;$d')
    #escape each * : replace each * by \* (	otherwise there is a problem with grep)
    EXPECTED_ERR_MSG=$(echo "$EXPECTED_ERR_MSG" | sed 's/\*/\\*/g')
    #Extract the line number of the expected error
    ERR_LOCATION_EXPECTED=$(echo "$EXPECTED_ERR_MSG" | sed -n 's/^.*\.deca:\([0-9]*:[0-9]*\):.*/\1/p')


    # Test if the error message contains the file name and a line number.
    if  echo "$ERR_MSG" | grep -q -e "$DECA_FILE:[0-9][0-9]*:"
    then
        # Test if the error message is the expected message
        if echo "$ERR_MSG" | grep -q -e "$EXPECTED_ERR_MSG"
        then
            echo "${GREEN}Intended failure${NC} of test_context on ${PURPLE}$DECA_FILE${NC} at ${LIGHT_BLUE}character $ERR_LOCATION_EXPECTED${NC}."
        else
            err=$((err+1))
            echo "${RED}Unintended failure${NC} of test_context on ${PURPLE}$DECA_FILE${NC}."
            echo "Should have failed with error ${LIGHT_BLUE}$EXPECTED_ERR_MSG${NC}, got: ${RED}$ERR_MSG${NC}."
        fi
    else
        err=$((err+1))
        echo "${RED}Unintended succes${NC} of test_context on ${PURPLE}$DECA_FILE${NC}. "
        echo "Should have failed with error ${LIGHT_BLUE}$EXPECTED_ERR_MSG${NC}."
    fi
}

# Function to test if the given file is syntactically valid.
test_context_valid () {
    ERR_MSG=$(test_context "$1" 2>&1)

    # Test is the error message is a build error.
    if ! test_build_error "$DECA_FILE" "$ERR_MSG"
    then
        err=$((err+1))
        return 1
    fi

    # Test if the error message contains the file name and a line number.
    if echo "$ERR_MSG" | grep -q -e "$1:[0-9][0-9]*:"
    then
        err=$((err+1))
        echo "${RED}Unintended failure${NC} of test_context on ${PURPLE}$1${NC}."
        echo "Got: ${RED}$ERR_MSG${NC}."
    else
        echo "${GREEN}Intended success${NC} of test_context on ${PURPLE}$1${NC}."
    fi
}

# Function to test if the build failed.
test_build_error() {
    # Scan the error message for build failure.
    if echo "$2" | grep -q -e "^Exception in thread"
    then
        echo "${RED}Unintended failure${NC} of test_context on ${PURPLE}$1${NC}."
        echo "${LIGHT_PURPLE}Build failed: ${RED}$2${NC}."
        return 1
    fi
    return 0
}

for test_case in $(find src/test/deca/context/invalid -name '*.deca' -not -path "*/provided/*")
do
    nb_tests=$((nb_tests+1))
    test_context_invalid "$test_case"
done

for test_case in $(find src/test/deca/context/valid -name '*.deca' -not -path "*/provided/*")
do
    nb_tests=$((nb_tests+1))
    test_context_valid "$test_case"
done

success_rate=$((100 - err * 100 / nb_tests))
# Save the number of errors and tests
src/test/script/tools/save_to_file.sh $err $nb_tests

echo ""
echo "${BROWN_ORANGE}Context tests finished with ${LIGHT_BLUE}${success_rate}%${BROWN_ORANGE} success. ${NC}"
if [ $err -eq 0 ]
then
    echo "${GREEN}All tests passed${NC}"
    exit 0
else
    echo "${RED}$err tests failed${NC} out of ${LIGHT_BLUE}$nb_tests${NC}"
    exit 1
fi
