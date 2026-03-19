#!/bin/bash

set -e

ADR_DIR="docs/architecture/adr"
INDEX_FILE="$ADR_DIR/README.md"

if [ ! -d "$ADR_DIR" ]; then
    echo "Error: ADR directory $ADR_DIR does not exist"
    exit 1
fi

extract_adr_metadata() {
    local file="$1"
    local number=""
    local title=""
    local date=""
    local status=""
    
    number=$(basename "$file" | grep -oE '^[0-9]+' | head -1)
    title=$(head -1 "$file" | sed -E 's/^# ADR [0-9]+: ?//' | sed 's/`/\\`/g')
    
    if command -v stat >/dev/null 2>&1; then
        if stat -f "%Sm" -t "%Y-%m-%d" "$file" >/dev/null 2>&1; then
            date=$(stat -f "%Sm" -t "%Y-%m-%d" "$file")
        else
            date=$(stat -c "%y" "$file" 2>/dev/null | cut -d' ' -f1 || echo "")
        fi
    fi
    
    if grep -qi "## 决策" "$file" 2>/dev/null || grep -qi "## decision" "$file" 2>/dev/null; then
        status="accepted"
    elif grep -qi "template" "$file" 2>/dev/null; then
        status="template"
    elif grep -qi "proposed" "$file" 2>/dev/null; then
        status="proposed"
    elif grep -qi "deprecated" "$file" 2>/dev/null; then
        status="deprecated"
    elif grep -qi "superseded" "$file" 2>/dev/null; then
        status="superseded"
    else
        status="unknown"
    fi
    
    if [ "$number" = "000" ]; then
        return
    fi
    
    echo "$number|$title|$date|$status"
}

ADR_FILES=$(find "$ADR_DIR" -maxdepth 1 -name '[0-9]*-*.md' -type f | sort)

if [ -z "$ADR_FILES" ]; then
    echo "Warning: No ADR files found matching pattern [0-9]*-*.md"
    exit 0
fi

TABLE_ROWS=""
for file in $ADR_FILES; do
    metadata=$(extract_adr_metadata "$file")
    if [ -n "$metadata" ]; then
        TABLE_ROWS="$TABLE_ROWS$metadata
"
    fi
done

EXISTING_INDEX=""
if [ -f "$INDEX_FILE" ]; then
    EXISTING_INDEX=$(sed -n '/^| 编号/,/^$/p' "$INDEX_FILE" | tail -n +3 | sed '$d' || echo "")
fi

MERGED_ROWS=""
SEEN_NUMBERS=""

if [ -n "$EXISTING_INDEX" ]; then
    while IFS= read -r line; do
        if [ -n "$line" ]; then
            num=$(echo "$line" | cut -d'|' -f1 | tr -d ' ')
            if [ -n "$num" ] && ! echo "$SEEN_NUMBERS" | grep -q "|$num|"; then
                MERGED_ROWS="$MERGED_ROWS$line
"
                SEEN_NUMBERS="$SEEN_NUMBERS|$num|"
            fi
        fi
    done <<< "$EXISTING_INDEX"
fi

while IFS= read -r line; do
    if [ -n "$line" ]; then
        num=$(echo "$line" | cut -d'|' -f1 | tr -d ' ')
        if [ -n "$num" ] && ! echo "$SEEN_NUMBERS" | grep -q "|$num|"; then
            MERGED_ROWS="$MERGED_ROWS$line
"
            SEEN_NUMBERS="$SEEN_NUMBERS|$num|"
        fi
    fi
done <<< "$TABLE_ROWS"

SORTED_ROWS=$(echo "$MERGED_ROWS" | grep -v '^$' | sort -t'|' -k1,1n)

cat > "$INDEX_FILE" << 'HEADER'
# 架构决策记录 (ADR)

本目录包含项目的架构决策记录。

## ADR 列表

| 编号 | 标题 | 日期 | 状态 |
|------|------|------|------|
HEADER

if [ -n "$SORTED_ROWS" ]; then
    while IFS= read -r line; do
        if [ -n "$line" ]; then
            num=$(echo "$line" | cut -d'|' -f1)
            title=$(echo "$line" | cut -d'|' -f2)
            date=$(echo "$line" | cut -d'|' -f3)
            status=$(echo "$line" | cut -d'|' -f4)
            
            filename=$(printf "%03d" "$num")-*.md
            actual_file=$(ls "$ADR_DIR"/$filename 2>/dev/null | head -1 | xargs basename 2>/dev/null || echo "")
            
            if [ -n "$actual_file" ]; then
                echo "| [$num]($actual_file) | $title | $date | $status |" >> "$INDEX_FILE"
            fi
        fi
    done <<< "$SORTED_ROWS"
fi

cat >> "$INDEX_FILE" << 'FOOTER'

## 什么是 ADR

架构决策记录 (Architecture Decision Record, ADR) 是一种记录重要架构决策及其背景的文档。每个 ADR 包含：

- **背景**：为什么需要做出这个决策
- **选项**：考虑过的备选方案
- **决策**：最终选择的方案
- **后果**：决策带来的影响

## 创建新的 ADR

1. 复制 `000-template.md` 作为新 ADR 的基础
2. 使用下一个连续的编号（如 002、003...）
3. 填写所有四个必需部分
4. 运行 `.github/scripts/update-adr-index.sh` 自动更新索引

## 参考

- [Michael Nygard 的 ADR 格式](https://cognitect.com/blog/2011/11/15/documenting-architecture-decisions)
FOOTER

COUNT=$(echo "$SORTED_ROWS" | grep -c . 2>/dev/null || echo 0)
echo "✓ ADR index updated: $INDEX_FILE"
echo "  Found $COUNT ADR(s)"
