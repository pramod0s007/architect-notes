#!/usr/bin/env python3
"""Publish Architect Notes v2.0 to Confluence space ~pramods."""

from __future__ import annotations

import html
import json
import os
import re
import subprocess
import sys
from pathlib import Path

REPO = Path(__file__).resolve().parents[1]
WIKI_SCRIPT = Path.home() / ".agentic-devx/scripts/wiki/wiki_manager.py"
SPACE = "~pramods"
HOME_PAGE_ID = "3214893944"
GITHUB = "https://github.com/pramod0s007/architect-notes"

PAPERS = [
    ("01", "volume-1-thinking-like-an-architect/paper-01-why-memorizing-design-patterns-is-holding-you-back", "Why Memorizing Design Patterns Is Holding You Back"),
    ("02", "volume-1-thinking-like-an-architect/paper-02-the-four-architectural-buckets", "The Four Architectural Buckets"),
    ("03", "volume-1-thinking-like-an-architect/paper-03-the-death-of-if-else", "The Death of if-else"),
    ("04", "volume-2-behavioral-design/paper-04-strategy-pattern-through-real-refactoring", "Strategy Pattern Through Real Refactoring"),
    ("05", "volume-2-behavioral-design/paper-05-state-pattern-through-a-stopwatch", "State Pattern Through a StopWatch"),
    ("06", "volume-2-behavioral-design/paper-06-command-pattern-through-banking-systems", "Command Pattern Through Banking Systems"),
    ("07", "volume-2-behavioral-design/paper-07-visitor-pattern-without-uml", "Visitor Pattern Without UML"),
    ("08", "volume-2-behavioral-design/paper-08-lookup-tables-vs-polymorphism", "Lookup Tables vs Polymorphism"),
    ("09", "volume-3-enterprise-patterns/paper-09-specification-pattern", "Specification Pattern"),
    ("10", "volume-3-enterprise-patterns/paper-10-chain-of-responsibility", "Chain of Responsibility"),
    ("11", "volume-4-architect-level-thinking/paper-11-builder-pattern", "Builder Pattern"),
    ("12", "volume-4-architect-level-thinking/paper-12-factory-pattern", "Factory Pattern"),
    ("13", "volume-4-architect-level-thinking/paper-13-when-patterns-become-anti-patterns", "When Patterns Become Anti-Patterns"),
    ("14", "volume-4-architect-level-thinking/paper-14-pattern-selection-decision-tree", "Pattern Selection Decision Tree"),
    ("15", "volume-4-architect-level-thinking/paper-15-which-patterns-still-matter-in-2026", "Which Patterns Still Matter in 2026"),
]

DOCS = [
    ("Architect Notes — Learning Path", "docs/learning-path.md"),
    ("Architect Notes — Architecture Map", "docs/architecture-map.md"),
    ("Architect Notes — Pattern Selection Tree", "docs/pattern-selection-decision-tree.md"),
    ("Architect Notes — Interview Roadmap", "docs/interview-roadmap.md"),
    ("Architect Notes — Staff Engineer Roadmap", "docs/staff-engineer-roadmap.md"),
]

MERMAID = {
    "pressure-to-pattern.png": """flowchart TD
  A[Code Smell] --> B[Design Pressure]
  B --> C[Refactoring]
  C --> D[Abstraction]
  D --> E[Pattern]""",
    "four-buckets.png": """flowchart TB
  P[Software Design Problem] --> D[Data Variation]
  P --> O[Object Variation]
  P --> B[Behavior Variation]
  P --> R[Rules Variation]""",
    "pattern-selection-tree.png": """flowchart TB
  Q[What is changing?] --> D[Data]
  Q --> O[Object]
  Q --> B[Behavior]
  Q --> R[Rules]
  D --> DT[Templates / Configuration]
  O --> OT[DI / Composition]
  B --> BT[Strategy / Command / State]
  R --> RT[Specification / Rule Engine]""",
    "strategy-evolution.png": """flowchart TD
  A[Behavior Variation] --> B[Large Conditional]
  B --> C[Repeated Changes]
  C --> D[Interface Extraction]
  D --> E[Composition]
  E --> F[Strategy Pattern]""",
    "maturity-curve.png": """flowchart LR
  P1[Phase 1 Discovery] --> P2[Phase 2 Overuse]
  P2 --> P3[Phase 3 Architectural Judgment]""",
    "pattern-relevance-2026.png": """flowchart TB
  subgraph Strong[Still Strong]
    S1[Strategy]
    S2[State]
    S3[Command]
    S4[Specification]
    S5[Builder]
  end
  subgraph Sit[Situational]
    T1[Visitor]
    T2[Factory]
    T3[Chain]
  end
  subgraph Rare[Less Common]
    R1[Abstract Factory]
    R2[Prototype]
    R3[Mediator]
  end""",
    "cor-pipeline.png": """flowchart TD
  R[Request] --> A[Authentication]
  A --> Z[Authorization]
  Z --> V[Validation]
  V --> L[Rate Limiting]
  L --> OK[Success]""",
}

PAPER_DIAGRAM = {
    "01": "pressure-to-pattern.png",
    "02": "four-buckets.png",
    "04": "strategy-evolution.png",
    "10": "cor-pipeline.png",
    "13": "maturity-curve.png",
    "14": "pattern-selection-tree.png",
    "15": "pattern-relevance-2026.png",
}


def load_wiki_manager():
    sys.path.insert(0, str(WIKI_SCRIPT.parent))
    from wiki_manager import WikiManager  # noqa: E402

    return WikiManager()


def image_macro(filename: str, alt: str) -> str:
    return (
        f'<p><ac:image ac:align="center" ac:alt="{html.escape(alt)}">'
        f'<ri:attachment ri:filename="{html.escape(filename)}" /></ac:image></p>'
    )


def code_macro(body: str, language: str = "text") -> str:
    escaped = body.replace("]]>", "]]&gt;")
    return (
        '<ac:structured-macro ac:name="code">'
        f'<ac:parameter ac:name="language">{language}</ac:parameter>'
        f"<ac:plain-text-body><![CDATA[{escaped}]]></ac:plain-text-body>"
        "</ac:structured-macro>"
    )


def md_to_storage(md: str) -> str:
    lines = md.splitlines()
    out: list[str] = []
    i = 0
    in_code = False
    code_buf: list[str] = []
    code_lang = "text"
    list_buf: list[str] = []

    def flush_list() -> None:
        nonlocal list_buf
        if list_buf:
            out.append("<ul>" + "".join(f"<li>{html.escape(x)}</li>" for x in list_buf) + "</ul>")
            list_buf = []

    while i < len(lines):
        line = lines[i]
        if line.strip().startswith("```"):
            flush_list()
            if not in_code:
                in_code = True
                code_buf = []
                lang = line.strip()[3:].strip()
                code_lang = lang or "text"
            else:
                out.append(code_macro("\n".join(code_buf), code_lang))
                in_code = False
                code_buf = []
            i += 1
            continue
        if in_code:
            code_buf.append(line)
            i += 1
            continue
        if not line.strip():
            flush_list()
            i += 1
            continue
        if line.startswith("# "):
            flush_list()
            out.append(f"<h1>{html.escape(line[2:].strip())}</h1>")
        elif line.startswith("## "):
            flush_list()
            out.append(f"<h2>{html.escape(line[3:].strip())}</h2>")
        elif line.startswith("### "):
            flush_list()
            out.append(f"<h3>{html.escape(line[4:].strip())}</h3>")
        elif line.startswith("- ") or line.startswith("* "):
            list_buf.append(inline_format(line[2:].strip()))
        elif line.startswith("> "):
            flush_list()
            out.append(
                '<ac:structured-macro ac:name="info"><ac:rich-text-body>'
                f"<p>{inline_format(line[2:].strip())}</p></ac:rich-text-body></ac:structured-macro>"
            )
        elif "|" in line and i + 1 < len(lines) and "|" in lines[i + 1] and re.match(r"^[\s|:-]+$", lines[i + 1]):
            flush_list()
            rows = []
            rows.append(line)
            i += 2
            while i < len(lines) and "|" in lines[i]:
                rows.append(lines[i])
                i += 1
            out.append(table_html(rows))
            continue
        else:
            flush_list()
            out.append(f"<p>{inline_format(line.strip())}</p>")
        i += 1
    flush_list()
    if in_code and code_buf:
        out.append(code_macro("\n".join(code_buf), code_lang))
    return "\n".join(out)


def inline_format(text: str) -> str:
    text = html.escape(text)
    text = re.sub(r"\*\*(.+?)\*\*", r"<strong>\1</strong>", text)
    text = re.sub(r"`([^`]+)`", r"<code>\1</code>", text)
    text = re.sub(
        r"\[([^\]]+)\]\(([^)]+)\)",
        lambda m: f'<a href="{html.escape(m.group(2), quote=True)}">{m.group(1)}</a>',
        text,
    )
    return text


def table_html(rows: list[str]) -> str:
    cells = [[c.strip() for c in r.strip().strip("|").split("|")] for r in rows]
    if len(cells) < 2:
        return ""
    head, body = cells[0], cells[1:]
    html_rows = "<tr>" + "".join(f"<th>{inline_format(h)}</th>" for h in head) + "</tr>"
    for row in body:
        html_rows += "<tr>" + "".join(f"<td>{inline_format(c)}</td>" for c in row) + "</tr>"
    return f"<table><tbody>{html_rows}</tbody></table>"


def _chrome_executable() -> str | None:
    import glob

    candidates = glob.glob(
        str(Path.home() / ".cache/puppeteer/chrome-headless-shell/*/chrome-headless-shell-mac-arm64/chrome-headless-shell")
    )
    return candidates[-1] if candidates else None


def render_diagrams(diagram_dir: Path) -> bool:
    diagram_dir.mkdir(parents=True, exist_ok=True)
    chrome = _chrome_executable()
    if not chrome:
        return False
    env = {**os.environ, "PUPPETEER_EXECUTABLE_PATH": chrome}
    ok = True
    for name, src in MERMAID.items():
        mmd = diagram_dir / name.replace(".png", ".mmd")
        mmd.write_text(src, encoding="utf-8")
        png = diagram_dir / name
        try:
            subprocess.run(
                [
                    "npm", "exec", "--yes", "--package=@mermaid-js/mermaid-cli", "--",
                    "mmdc", "-i", str(mmd), "-o", str(png), "-w", "1400", "-q",
                ],
                capture_output=True,
                check=True,
                timeout=120,
                env=env,
            )
        except (subprocess.CalledProcessError, subprocess.TimeoutExpired):
            ok = False
    return ok


def upload_diagrams(wm, page_id: str, diagram_dir: Path) -> None:
    for name in MERMAID:
        png = diagram_dir / name
        if png.exists():
            wm.upload_attachment(name, str(png), page_id=page_id)


def hub_content(diagrams_ok: bool) -> str:
    imgs = ""
    if diagrams_ok:
        imgs = (
            image_macro("pressure-to-pattern.png", "Pressure to Pattern")
            + image_macro("four-buckets.png", "Four Architectural Buckets")
            + image_macro("pattern-selection-tree.png", "Pattern Selection Tree")
            + image_macro("maturity-curve.png", "Pattern Maturity Curve")
        )
    return f"""
<ac:structured-macro ac:name="info"><ac:rich-text-body>
<p><strong>Architect Notes v2.0</strong> — design patterns as consequences of pressure, not interview memorization.</p>
</ac:rich-text-body></ac:structured-macro>
<p><strong>GitHub:</strong> <a href="{GITHUB}">{GITHUB}</a></p>
<p><strong>Tags:</strong> v1.0-foundation · v2.0-architect-notes</p>
<h2>Core Philosophy</h2>
<p>Architects do not start with patterns. Architects start with pressures.</p>
{imgs}
<h2>White Papers (15)</h2>
<p>Child pages below — Volume 1 through Volume 4, plus roadmaps and decision framework.</p>
<h2>Runnable Java Examples</h2>
<p>Strategy, State, Command, Visitor, Lookup, Specification, Chain of Responsibility, Builder, Factory — see GitHub <code>code-samples/</code>.</p>
<h2>Start Here</h2>
<ul>
<li>Paper 01 — Foundation</li>
<li>Paper 13 — Anti-Patterns &amp; Maturity Curve</li>
<li>Paper 14 — Pattern Selection Decision Tree</li>
<li>Paper 15 — Which Patterns Still Matter in 2026</li>
</ul>
"""


def paper_content(num: str, rel_path: str, title: str, diagram_file: str | None, diagrams_ok: bool) -> str:
    readme = REPO / rel_path / "README.md"
    body = md_to_storage(readme.read_text(encoding="utf-8"))
    header = f"""
<p><strong>GitHub:</strong> <a href="{GITHUB}/tree/main/{rel_path}">{GITHUB}/tree/main/{rel_path}</a></p>
<ac:structured-macro ac:name="info"><ac:rich-text-body>
<p>Paper {num} — Architect Notes Series</p>
</ac:rich-text-body></ac:structured-macro>
"""
    diagram = ""
    if diagrams_ok and diagram_file:
        diagram = image_macro(diagram_file, title)
    return header + diagram + body


def main() -> int:
    wm = load_wiki_manager()
    diagram_dir = Path("/tmp/architect-notes-wiki-diagrams")
    diagrams_ok = render_diagrams(diagram_dir)
    print(f"Diagrams rendered: {diagrams_ok}", file=sys.stderr)

    existing = wm.search_cql('space="~pramods" AND title="Architect Notes"')
    if existing.get("results"):
        hub_id = existing["results"][0]["id"]
        print(f"Hub exists: {hub_id}", file=sys.stderr)
    else:
        hub = wm.create_page(SPACE, "Architect Notes", hub_content(diagrams_ok), HOME_PAGE_ID)
        hub_id = hub["id"]
        print(json.dumps(hub))
        if diagrams_ok:
            upload_diagrams(wm, hub_id, diagram_dir)

    for wiki_title, rel in DOCS:
        md_path = REPO / rel
        content = (
            f'<p><strong>GitHub:</strong> <a href="{GITHUB}/tree/main/{rel}">{GITHUB}/tree/main/{rel}</a></p>'
            + md_to_storage(md_path.read_text(encoding="utf-8"))
        )
        hits = wm.search_cql(f'space="{SPACE}" AND title="{wiki_title}"')
        if hits.get("results"):
            print(f"Skip existing: {wiki_title}", file=sys.stderr)
            continue
        result = wm.create_page(SPACE, wiki_title, content, hub_id)
        print(json.dumps(result))

    for num, rel, title in PAPERS:
        wiki_title = f"Paper {num} — {title}"
        hits = wm.search_cql(f'space="{SPACE}" AND title="{wiki_title}"')
        if hits.get("results"):
            print(f"Skip existing: {wiki_title}", file=sys.stderr)
            continue
        diag = PAPER_DIAGRAM.get(num)
        content = paper_content(num, rel, title, diag, diagrams_ok)
        result = wm.create_page(SPACE, wiki_title, content, hub_id)
        page_id = result["id"]
        print(json.dumps(result))
        if diagrams_ok and diag:
            png = diagram_dir / diag
            if png.exists():
                wm.upload_attachment(page_id, diag, str(png))

    print("DONE", file=sys.stderr)
    return 0


if __name__ == "__main__":
    sys.exit(main())
