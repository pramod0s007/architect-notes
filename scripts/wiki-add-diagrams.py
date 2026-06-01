#!/usr/bin/env python3
"""Append diagram code blocks to Architect Notes wiki hub and key papers."""

import sys
from pathlib import Path

REPO = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(Path.home() / ".agentic-devx/scripts/wiki"))
from wiki_manager import WikiManager  # noqa: E402

HUB_ID = "3907887571"

DIAGRAM_FILES = [
    ("Core: Pressure to Pattern", REPO / "volume-1-thinking-like-an-architect/paper-01-why-memorizing-design-patterns-is-holding-you-back/diagrams/pressure-to-pattern.md"),
    ("Four Architectural Buckets", REPO / "volume-1-thinking-like-an-architect/paper-02-the-four-architectural-buckets/diagrams/four-buckets.md"),
    ("Pattern Selection Tree", REPO / "docs/pattern-selection-decision-tree.md"),
    ("Pattern Maturity Curve", None),
    ("2026 Pattern Relevance", REPO / "volume-4-architect-level-thinking/paper-15-which-patterns-still-matter-in-2026/diagrams/pattern-relevance-2026.md"),
]

MATURITY = """flowchart LR
  P1[Phase 1 - Discovery] --> P2[Phase 2 - Overuse]
  P2 --> P3[Phase 3 - Architectural Judgment]"""


def code_macro(body: str, lang: str = "text") -> str:
    escaped = body.replace("]]>", "]]&gt;")
    return (
        '<ac:structured-macro ac:name="code">'
        f'<ac:parameter ac:name="language">{lang}</ac:parameter>'
        f"<ac:plain-text-body><![CDATA[{escaped}]]></ac:plain-text-body>"
        "</ac:structured-macro>"
    )


def main() -> None:
    wm = WikiManager()
    block = "<h2>Diagrams</h2><p>Visual reference (also in GitHub). Render with Mermaid locally or use Draw.io.</p>"
    for title, path in DIAGRAM_FILES:
        block += f"<h3>{title}</h3>"
        if path and path.exists():
            text = path.read_text(encoding="utf-8")
            # extract fenced blocks or use whole file
            if "```" in text:
                parts = text.split("```")
                for i, part in enumerate(parts):
                    if i % 2 == 1:
                        lines = part.strip().split("\n", 1)
                        lang = lines[0] if lines[0] in ("text", "mermaid") else "text"
                        body = lines[1] if len(lines) > 1 else lines[0]
                        block += code_macro(body.strip(), lang if lang != "text" else "text")
            else:
                block += code_macro(text, "text")
        elif title.startswith("Pattern Maturity"):
            block += code_macro(MATURITY, "mermaid")
    wm.append_content(block, page_id=HUB_ID)
    print("Appended diagrams to hub", HUB_ID)


if __name__ == "__main__":
    main()
