#!/usr/bin/env bash
# =============================================================================
# run-from-terminal.sh — Push local changes (diagrams, Medium drafts, Volume 5)
#
#   cd ~/github-personal/architect-notes
#   bash run-from-terminal.sh
# =============================================================================

set -euo pipefail

cd ~/github-personal/architect-notes
export GIT_SSH_COMMAND="ssh -i $HOME/.ssh/id_ed25519_github_pramod0s007 -o IdentitiesOnly=yes"

check_no_cursor_coauthor() {
  if git log -1 --format=%B | grep -qi 'Co-authored-by: Cursor'; then
    echo "ERROR: Cursor co-author detected."
    exit 1
  fi
}

git pull origin main

# Ignore macOS junk
if [[ ! -f .gitignore ]] || ! grep -q '.DS_Store' .gitignore 2>/dev/null; then
  printf '\n.DS_Store\n**/.DS_Store\n' >> .gitignore
fi

# Commit 1 — Diagrams (Mermaid source + PNG)
git add diagrams/*.mmd diagrams/*.png .gitignore
git commit -m "Add professional Mermaid diagrams and cover images for Papers 01–19"
echo "=== Commit 1 ===" && git log -1 --oneline
check_no_cursor_coauthor

# Commit 2 — Medium drafts (Papers 01–15)
git add \
  volume-1-thinking-like-an-architect/paper-01-why-memorizing-design-patterns-is-holding-you-back/MEDIUM-DRAFT.md \
  volume-1-thinking-like-an-architect/paper-01-why-memorizing-design-patterns-is-holding-you-back/MEDIUM-PASTE.html \
  volume-1-thinking-like-an-architect/paper-02-the-four-architectural-buckets/MEDIUM-DRAFT.md \
  volume-1-thinking-like-an-architect/paper-02-the-four-architectural-buckets/MEDIUM-PASTE.html \
  volume-1-thinking-like-an-architect/paper-03-the-death-of-if-else/MEDIUM-DRAFT.md \
  volume-1-thinking-like-an-architect/paper-03-the-death-of-if-else/MEDIUM-PASTE.html \
  volume-2-behavioral-design/paper-04-strategy-pattern-through-real-refactoring/MEDIUM-DRAFT.md \
  volume-2-behavioral-design/paper-04-strategy-pattern-through-real-refactoring/MEDIUM-PASTE.html \
  volume-2-behavioral-design/paper-05-state-pattern-through-a-stopwatch/MEDIUM-DRAFT.md \
  volume-2-behavioral-design/paper-05-state-pattern-through-a-stopwatch/MEDIUM-PASTE.html \
  volume-2-behavioral-design/paper-06-command-pattern-through-banking-systems/MEDIUM-DRAFT.md \
  volume-2-behavioral-design/paper-06-command-pattern-through-banking-systems/MEDIUM-PASTE.html \
  volume-2-behavioral-design/paper-07-visitor-pattern-without-uml/MEDIUM-DRAFT.md \
  volume-2-behavioral-design/paper-07-visitor-pattern-without-uml/MEDIUM-PASTE.html \
  volume-2-behavioral-design/paper-08-lookup-tables-vs-polymorphism/MEDIUM-DRAFT.md \
  volume-2-behavioral-design/paper-08-lookup-tables-vs-polymorphism/MEDIUM-PASTE.html \
  volume-3-enterprise-patterns/paper-09-specification-pattern/MEDIUM-DRAFT.md \
  volume-3-enterprise-patterns/paper-09-specification-pattern/MEDIUM-PASTE.html \
  volume-3-enterprise-patterns/paper-10-chain-of-responsibility/MEDIUM-DRAFT.md \
  volume-3-enterprise-patterns/paper-10-chain-of-responsibility/MEDIUM-PASTE.html \
  volume-4-architect-level-thinking/paper-11-builder-pattern/MEDIUM-DRAFT.md \
  volume-4-architect-level-thinking/paper-11-builder-pattern/MEDIUM-PASTE.html \
  volume-4-architect-level-thinking/paper-12-factory-pattern/MEDIUM-DRAFT.md \
  volume-4-architect-level-thinking/paper-12-factory-pattern/MEDIUM-PASTE.html \
  volume-4-architect-level-thinking/paper-13-when-patterns-become-anti-patterns/MEDIUM-DRAFT.md \
  volume-4-architect-level-thinking/paper-13-when-patterns-become-anti-patterns/MEDIUM-PASTE.html \
  volume-4-architect-level-thinking/paper-14-pattern-selection-decision-tree/MEDIUM-DRAFT.md \
  volume-4-architect-level-thinking/paper-14-pattern-selection-decision-tree/MEDIUM-PASTE.html \
  volume-4-architect-level-thinking/paper-15-which-patterns-still-matter-in-2026/MEDIUM-DRAFT.md \
  volume-4-architect-level-thinking/paper-15-which-patterns-still-matter-in-2026/MEDIUM-PASTE.html
git commit -m "Add Medium publishing drafts for Papers 01–15"
echo "=== Commit 2 ===" && git log -1 --oneline
check_no_cursor_coauthor

# Commit 3 — Volume 5 structural (Medium drafts 16–19)
git add volume-5-structural-patterns/
git commit -m "Add Volume 5 structural pattern Medium drafts (Papers 16–19)"
echo "=== Commit 3 ===" && git log -1 --oneline
check_no_cursor_coauthor

# Commit 4 — Structural code samples
git add \
  code-samples/.gitignore \
  code-samples/observer/ \
  code-samples/decorator/ \
  code-samples/proxy/ \
  code-samples/adapter/
git commit -m "Add Observer, Decorator, Proxy, and Adapter code samples"
echo "=== Commit 4 ===" && git log -1 --oneline
check_no_cursor_coauthor

# Commit 5 — Publishing docs and scripts
git add \
  docs/PUBLISHING-ORDER.md \
  docs/SERIES-ROADMAP.md \
  scripts/
git commit -m "Add publishing order, series roadmap, and wiki publish scripts"
echo "=== Commit 5 ===" && git log -1 --oneline
check_no_cursor_coauthor

git push origin main

echo ""
git log -6 --oneline
echo ""
echo "Done. https://github.com/pramod0s007/architect-notes"
