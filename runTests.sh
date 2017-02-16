#!/bin/bash

BLUE='\033[1;34m'
NC='\033[0m'

function highlight() {
  printf "${BLUE}%s${NC}\n" "$1"
}

fail=0
for dir in */; do
  dir=$(basename "$dir")
  cd "$dir"
  if [[ -e gradlew ]]; then
    echo ""
    highlight "$dir : testing"
    echo ""

    ./gradlew check
    result=$?
    if [[ $result != 0 && $fail == 0 ]]; then
      fail=$result
    fi

    echo ""
    printf "${BLUE}%s${NC}\n" "$dir : done"
    echo ""

  fi
  cd ..
done

# returns the first nonzero exit code
exit $fail
