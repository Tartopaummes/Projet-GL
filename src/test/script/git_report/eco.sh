#!/bin/bash

# Fetch all branches
git fetch --all

# Get a list of all branches
branches=$(git branch -r | grep -v '\->')

# Initialize an associative array to store commit counts per author
declare -A commit_counts

# Iterate over each branch
for branch in $branches; do
  # Get all commits from the current branch and count commits per author
  while read -r author; do
    ((commit_counts["$author"]++))
  done < <(git log "$branch" --pretty=format:'%an')
done

# Print the title for the columns
echo "Commit Count | Author"

# Print the commit counts per author
for author in "${!commit_counts[@]}"; do
  echo "${commit_counts[$author]}       |      $author"
done | sort -nr