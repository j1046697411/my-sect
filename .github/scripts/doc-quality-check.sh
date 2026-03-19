#!/bin/bash
set -e

# Documentation Quality Check Script
# Runs markdownlint and lychee for internal link validation

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(dirname "$(dirname "$SCRIPT_DIR")")"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "🔍 Starting documentation quality check..."

# Check if markdownlint is installed
if ! command -v markdownlint &> /dev/null; then
    echo -e "${YELLOW}markdownlint not found. Installing...${NC}"
    npm install -g markdownlint-cli
fi

# Check if lychee is installed
if ! command -v lychee &> /dev/null; then
    echo -e "${YELLOW}lychee not found. Installing...${NC}"
    if command -v cargo &> /dev/null; then
        cargo install lychee
    else
        echo -e "${RED}Error: cargo not found. Please install lychee manually.${NC}"
        exit 1
    fi
fi

# Default to docs directory if no arguments provided
FILES_TO_CHECK="${@:-$ROOT_DIR/docs}"

echo "📁 Checking files in: $FILES_TO_CHECK"

# Run markdownlint
echo -e "\n${GREEN}Running markdownlint...${NC}"
markdownlint --config "$ROOT_DIR/.markdownlint.json" "$FILES_TO_CHECK" || {
    echo -e "${YELLOW}⚠️  markdownlint found issues (non-blocking)${NC}"
}

# Run lychee for internal links only
echo -e "\n${GREEN}Running lychee (internal links only)...${NC}"
lychee \
    --exclude '^https?://' \
    --exclude '^ftp://' \
    --exclude '^mailto:' \
    --exclude '^tel:' \
    --verbose \
    --no-progress \
    "$FILES_TO_CHECK" || {
    echo -e "${YELLOW}⚠️  lychee found broken links (non-blocking)${NC}"
}

echo -e "\n${GREEN}✅ Documentation quality check completed${NC}"
echo -e "${YELLOW}Note: Issues found are warnings only and do not block the workflow${NC}"
