#! /bin/sh

# Author: gl34
# Initial version: 17/12/2024

# More advanced script to test the lexer.
# Goes through all the files in src/test/syntax and tests whether or not the expected results are given.
# The invalid tests are the ones in src/test/syntax/invalid/lexically_invalid.
# All the rest are valid.

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

# Function to test if the given file is lexically invalid.
test_lex_invalid () {
    ERR_LINE=$(echo "$1" | sed -e s/[^0-9]//g)
    ERR_MSG=$(test_synt "$1" 2>&1)

    # Test is the error message is a build error.
    if ! test_build_error "$DECA_FILE" "$ERR_MSG"
    then
        err=$((err+1))
        return 1
    fi

    # Test if the error message contains the file name and a line number.
    if echo "$ERR_MSG" | grep -q -e "$1:[0-9][0-9]*:"
    then
        if echo "$ERR_MSG" | grep -q -e "$1:$ERR_LINE"
        then
            echo "${GREEN}Intended failure${NC} of test_lex on ${PURPLE}$1${NC} at ${LIGHT_BLUE}line $ERR_LINE${NC}."
            return 0
        else
            err=$((err+1))
            echo "${RED}Unintended failure${NC} of test_synt on ${PURPLE}$1${NC}."
            echo "Should have failed at line ${LIGHT_BLUE}$ERR_LINE${NC}, got ${RED}$ERR_MSG${NC}."
            return 1
        fi
    else
        err=$((err+1))
        echo "${RED}Unintended success${NC} of test_lex on ${PURPLE}$1${NC}."
        echo "Should have failed at ${LIGHT_BLUE}line $ERR_LINE${NC}."
        return 1
    fi
}

# Function to test if the given file is lexically valid.
test_lex_valid () {
    ERR_MSG=$(test_lex "$1" 2>&1)

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
        echo "${RED}Unintended failure${NC} of test_lex on ${PURPLE}$1${NC}."
        echo "Got: ${RED}$ERR_MSG${NC}"
        return 1
    else
        echo "${GREEN}Intended success${NC} of test_lex on ${PURPLE}$1${NC}."
        return 0
    fi
}

# Function to test if the build failed.
test_build_error() {
    # Scan the error message for build failure.
    if echo "$2" | grep -q -e "^Exception in thread"
    then
        echo ""
        echo "${RED}Unintended failure${NC} of test_lex on ${PURPLE}$1${NC}."
        echo "${LIGHT_PURPLE}Build failed: ${RED}$2${NC}."
        return 1
    fi
    return 0
}

for test_case in $(find src/test/deca/syntax/invalid/lexically_invalid -name '*.deca' -not -path "*/provided/*")
do
    nb_tests=$((nb_tests+1))
    test_lex_invalid "$test_case"
done

for test_case in $(find src/test/deca/syntax/ -name '*.deca' -not -path "*/lexically_invalid/*" -not -path "*/provided/*")
do
    nb_tests=$((nb_tests+1))
    test_lex_valid "$test_case"
done

success_rate=$((100 - err * 100 / nb_tests))
# Save the number of errors and tests
src/test/script/tools/save_to_file.sh $err $nb_tests

echo ""
echo "${BROWN_ORANGE}Lexical tests finished with ${LIGHT_BLUE}${success_rate}%${BROWN_ORANGE} success. ${NC}"
if [ $err -eq 0 ]
then
    echo "${GREEN}All tests passed${NC}"
    exit 0
else
    echo "${RED}$err tests failed${NC} out of ${LIGHT_BLUE}$nb_tests${NC}"
    exit 1
fi