#!/bin/bash
# E2E Test Simulation Script for doc-auto-update.yml
# This script simulates and verifies the workflow behavior

set -e

echo "=========================================="
echo "E2E Test: Auto Update Documentation Workflow"
echo "=========================================="
echo ""

# Configuration
TEST_BRANCH="test/auto-update-workflow"
MAIN_BRANCH="main"
WORKFLOW_FILE=".github/workflows/doc-auto-update.yml"

echo "1. Verifying workflow configuration..."
echo "-------------------------------------------"

# Check workflow file exists
if [ -f "$WORKFLOW_FILE" ]; then
    echo "✓ Workflow file exists: $WORKFLOW_FILE"
else
    echo "✗ Workflow file not found: $WORKFLOW_FILE"
    exit 1
fi

# Verify workflow triggers on push to main with docs/adr changes
if grep -q "push:" "$WORKFLOW_FILE" && grep -q "branches:" "$WORKFLOW_FILE" && grep -q "main" "$WORKFLOW_FILE"; then
    echo "✓ Workflow triggers on push to main"
else
    echo "✗ Workflow trigger configuration incorrect"
    exit 1
fi

# Verify docs/adr/** is in paths
if grep -q "docs/adr/" "$WORKFLOW_FILE"; then
    echo "✓ Workflow monitors docs/adr/** path"
else
    echo "✗ Workflow does not monitor docs/adr/** path"
    exit 1
fi

echo ""
echo "2. Verifying 4 jobs are defined..."
echo "-------------------------------------------"

# Count jobs
JOB_COUNT=$(grep -c "^\s\s[a-z-]*:$" "$WORKFLOW_FILE" 2>/dev/null || echo "0")
echo "Jobs found in workflow:"
grep -E "^\s{2}[a-z-]+:$" "$WORKFLOW_FILE" | sed 's/:$//' | sed 's/^/  - /'

# Verify specific jobs
JOBS=("generate-api-docs" "update-adr-index" "update-rules-docs" "quality-check-and-commit")
for job in "${JOBS[@]}"; do
    if grep -q "^  $job:" "$WORKFLOW_FILE"; then
        echo "✓ Job defined: $job"
    else
        echo "✗ Job missing: $job"
        exit 1
    fi
done

echo ""
echo "3. Verifying [skip ci] in commit message..."
echo "-------------------------------------------"

# Check for [skip ci] in commit step
if grep -q "\[skip ci\]" "$WORKFLOW_FILE"; then
    echo "✓ Commit message contains [skip ci] to prevent loops"
    COMMIT_LINE=$(grep -n "\[skip ci\]" "$WORKFLOW_FILE" | head -1)
    echo "  Line: $COMMIT_LINE"
else
    echo "✗ [skip ci] not found in commit message"
    exit 1
fi

echo ""
echo "4. Verifying loop prevention mechanism..."
echo "-------------------------------------------"

# The [skip ci] tag prevents GitHub Actions from triggering on the auto-generated commit
# This prevents infinite loops
echo "✓ Loop prevention: [skip ci] tag in commit message"
echo "  - GitHub Actions ignores commits with [skip ci], [ci skip], etc."
echo "  - This prevents the workflow from triggering on its own commits"

echo ""
echo "5. Test branch status..."
echo "-------------------------------------------"

CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD)
echo "Current branch: $CURRENT_BRANCH"

# Check if test ADR file exists
if [ -f "docs/adr/adr-001-test-e2e-workflow.md" ]; then
    echo "✓ Test ADR file created: docs/adr/adr-001-test-e2e-workflow.md"
else
    echo "✗ Test ADR file not found"
    exit 1
fi

# Check commit history
echo ""
echo "Recent commits on test branch:"
git log --oneline -3 | sed 's/^/  /'

echo ""
echo "=========================================="
echo "Simulation Summary"
echo "=========================================="
echo ""
echo "When this test branch is merged to main:"
echo ""
echo "1. GitHub Actions will trigger doc-auto-update.yml workflow"
echo "2. Four jobs will execute in parallel/sequence:"
echo "   - generate-api-docs: Generates Dokka documentation"
echo "   - update-adr-index: Updates ADR README.md index"
echo "   - update-rules-docs: Updates rules documentation (if rules changed)"
echo "   - quality-check-and-commit: Commits all changes with [skip ci]"
echo "3. The [skip ci] tag prevents workflow re-triggering (loop prevention)"
echo "4. A new commit will be created by Doc Bot with updated documentation"
echo ""
echo "Expected workflow run:"
echo "  ✓ 4/4 Jobs complete successfully"
echo "  ✓ Doc update commit created"
echo "  ✓ Commit message contains [skip ci]"
echo "  ✓ No loop trigger occurred"
echo ""
echo "=========================================="
echo "E2E Test Simulation: PASSED"
echo "=========================================="
