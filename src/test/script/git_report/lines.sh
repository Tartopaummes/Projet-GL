#!/bin/bash

# Fetch all branches
git fetch --all

# Get a list of all branches
branches=$(git branch -r | grep -v '\->')

# Initialize an associative array to store line counts per author
declare -A line_counts

# Iterate over each branch
for branch in $branches; do
  # Get all commits from the current branch and count lines per author
  while read -r author lines; do
    ((line_counts["$author"]+=lines))
  done < <(git log "$branch" --pretty=format:'%an %b' | awk '{print $1, gsub(/\n/, "&") + 1}')
done

# Print the title for the columns
echo "Line Count    |   Author"

# Print the line counts per author
for author in "${!line_counts[@]}"; do
  echo "${line_counts[$author]}                   $author"
done | sort -nr