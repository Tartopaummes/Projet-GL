#! /bin/sh

RED='\033[0;31m'
GREEN='\033[0;32m'
PURPLE='\033[0;35m'
LIGHT_BLUE='\033[1;34m'
LIGHT_PURPLE='\033[1;35m'
BROWN_ORANGE='\033[0;33m'
NC='\033[0m'

decac "-b"
read -r line # Stops the execution of the tests

echo "${BROWN_ORANGE}========================= Démonstration des prints =========================${NC}"
echo ""
echo "Execution de : decac 1_prints.deca"
decac "1_prints.deca"
ima "1_prints.ass"

read -r line # Stops the execution of the tests

echo "${BROWN_ORANGE}========================= Démonstration des variables =========================${NC}"
echo ""
echo "Execution de : decac 2_variables"
decac "2_variables.deca"
ima "2_variables.ass"

read -r line # Stops the execution of the tests

echo "${BROWN_ORANGE}========================= Démonstration des erreurs lexicales =========================${NC}"
echo ""
echo "Execution de : decac 3_lexical_error.deca"
decac "3_lexical_error.deca"

read -r line # Stops the execution of the tests

echo "${BROWN_ORANGE}========================= Démonstration des erreurs syntaxiques =========================${NC}"
echo ""
echo "Execution de : decac 4_syntax_error.deca"
decac "4_syntax_error.deca"

read -r line # Stops the execution of the tests

echo "${BROWN_ORANGE}========================= Démonstration des erreurs de contexte - assign =========================${NC}"
echo ""
echo "Execution de : decac 5_context_error_assign_int_boolean.deca"
decac "5_context_error_assign_int_boolean.deca"

read -r line # Stops the execution of the tests

echo "${BROWN_ORANGE}========================= Démonstration du calcul de PGCD =========================${NC}"
echo ""
echo "Execution de : decac 6_pgcd.deca"
decac "6_pgcd.deca"
ima "6_pgcd.ass"

read -r line # Stops the execution of the tests

echo "${BROWN_ORANGE}========================= Démonstration des cast et des méthodes =========================${NC}"
echo ""
echo "Execution de : decac 7_cast.deca"
decac "7_cast.deca"
ima "7_cast.ass"

read -r line # Stops the execution of the tests

echo "${BROWN_ORANGE}========================= Démonstration des erreurs de contexte - champs =========================${NC}"
echo ""
echo "Execution de : decac 8_context_error_double_field_declaration.deca"
decac "8_context_error_double_field_declaration.deca"

read -r line # Stops the execution of the tests

echo "${BROWN_ORANGE}========================= Démonstration des erreurs à l'execution =========================${NC}"
echo ""
echo "Execution de : decac 9_execution_error_no_return.deca"
decac "9_execution_error_no_return.deca"
ima "9_execution_error_no_return.ass"

read -r line # Stops the execution of the tests

echo "Utilisation de l'option -n."
echo "Execution de : decac -n 9_execution_error_no_return.deca"
decac "-n" "9_execution_error_no_return.deca"
ima "9_execution_error_no_return.ass"

read -r line # Stops the execution of the tests

echo "${BROWN_ORANGE}========================= Démonstration de l'optimisation =========================${NC}"
echo ""
echo "Comparaison entre avec et sans optimisation : "
echo "Execution de : decac 10_optim.deca"
decac "10_optim.deca"
ima "-s" "10_optim.ass"

echo "Execution de : decac -o 10_optim.deca"
decac "-o" "10_optim.deca"
ima "-s" "10_optim.ass"

read -r line # Stops the execution of the tests

echo "${BROWN_ORANGE}========================= Morpion =========================${NC}"
echo ""
echo "Execution de : decac 11_tic_tac_toe.deca"
decac "11_tic_tac_toe.deca"
ima "11_tic_tac_toe.ass"

read -r line # Stops the execution of the tests

echo "${BROWN_ORANGE}========================= Nim =========================${NC}"
echo ""
echo "Execution de : decac 12_nim.deca"
decac "12_nim.deca"
ima "12_nim.ass"
