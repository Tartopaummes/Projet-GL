#!/bin/bash

# Fetch all branches
git fetch --all

# Get a list of all branches
branches=$(git branch -r | grep -v '\->')

# Initialize an associative array to store commit counts per day
declare -A commit_counts

# Iterate over each branch
for branch in $branches; do
  # Get all commits from the current branch and count commits per day
  while read -r date; do
    ((commit_counts["$date"]++))
  done < <(git log "$branch" --date=short --pretty=format:'%ad')
done

# Print the title for the columns
echo "Date       | Commit Count"

# Print the commit counts per day
for date in "${!commit_counts[@]}"; do
  echo "$date | ${commit_counts[$date]}"
done | sort