#!/bin/bash

# Fetch all branches
git fetch --all

# Get a list of all branches
branches=$(git branch -r | grep -v '\->')

# Initialize an associative array to store character counts per author
declare -A char_counts

# Iterate over each branch
for branch in $branches; do
  # Get all commits from the current branch and count characters per author
  while read -r author message; do
    ((char_counts["$author"]+=${#message}))
  done < <(git log "$branch" --pretty=format:'%an %s')
done

# Print the title for the columns
echo "Character Count    |   Author"

# Print the character counts per author
for author in "${!char_counts[@]}"; do
  echo "${char_counts[$author]}                   $author"
done | sort -nr