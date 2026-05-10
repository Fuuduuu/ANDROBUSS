#!/usr/bin/env python3
"""Validates docs/PROJECT_STATE.yml schema. Run: python tools/validate_project_state.py"""

from pathlib import Path
import re
import subprocess
import sys

ROOT = Path(__file__).resolve().parents[1]
STATE_FILE = ROOT / "docs" / "PROJECT_STATE.yml"
REQUIRED_TOP = {"project", "scope", "architecture", "invariants", "risks", "validation", "debug"}

def fail(msg):
    print(f"ERROR: {msg}")
    return 1

def get_section(lines, name):
    start = None
    for i, line in enumerate(lines):
        if re.match(rf"^{re.escape(name)}:\s*$", line):
            start = i + 1
            break
    if start is None:
        return []
    out = []
    for line in lines[start:]:
        if re.match(r"^[A-Za-z_][A-Za-z0-9_-]*:\s*(?:.*)?$", line):
            break
        out.append(line)
    return out

def get_scalar(section_lines, key):
    pat = rf"^\s{{2}}{re.escape(key)}:\s*(.+?)\s*$"
    for line in section_lines:
        m = re.match(pat, line)
        if m:
            value = m.group(1).strip().strip("'\"")
            return None if value == "" or value.lower() == "null" else value
    return None

def get_required_commands(validation_lines):
    cmds, inside = [], False
    for line in validation_lines:
        if re.match(r"^\s{2}required_commands:\s*$", line):
            inside = True
            continue
        if not inside:
            continue
        if re.match(r"^\s{4}-\s+.+$", line):
            cmds.append(re.sub(r"^\s{4}-\s+", "", line).strip())
            continue
        if re.match(r"^\s{2}[A-Za-z_][A-Za-z0-9_-]*:\s*(?:.*)?$", line):
            break
    return cmds

def get_critical_invariants(invariant_lines):
    blocks, current, in_critical = [], None, False
    for line in invariant_lines:
        if re.match(r"^\s{2}critical:\s*$", line):
            in_critical = True
            continue
        if not in_critical:
            continue
        if re.match(r"^\s{2}[A-Za-z_][A-Za-z0-9_-]*:\s*(?:.*)?$", line):
            break
        m_id = re.match(r"^\s{4}-\s+id:\s*(.+?)\s*$", line)
        if m_id:
            if current:
                blocks.append(current)
            current = {"id": m_id.group(1).strip().strip("'\"")}
            continue
        if not current:
            continue
        m_rule = re.match(r"^\s{6}rule:\s*(.+?)\s*$", line)
        if m_rule:
            current["rule"] = m_rule.group(1).strip().strip("'\"")
        m_test = re.match(r"^\s{6}test:\s*(.+?)\s*$", line)
        if m_test:
            current["test"] = m_test.group(1).strip().strip("'\"")
    if current:
        blocks.append(current)
    return blocks

def main():
    if not STATE_FILE.exists():
        return fail("docs/PROJECT_STATE.yml does not exist.")
    lines = STATE_FILE.read_text(encoding="utf-8").splitlines()
    top = {m.group(1) for m in (re.match(r"^([A-Za-z_][A-Za-z0-9_-]*):\s*$", l) for l in lines) if m}
    missing = sorted(REQUIRED_TOP - top)
    if missing:
        return fail(f"Missing top-level keys: {', '.join(missing)}")
    project = get_section(lines, "project")
    if get_scalar(project, "name") != "ANDROBUSS":
        return fail("project.name must be ANDROBUSS.")
    if get_scalar(project, "branch") != "main":
        return fail("project.branch must be main.")
    if get_scalar(project, "remote") != "https://github.com/Fuuduuu/ANDROBUSS.git":
        return fail("project.remote must be https://github.com/Fuuduuu/ANDROBUSS.git.")
    last_accepted = get_scalar(project, "last_accepted_commit")
    if not last_accepted:
        return fail("project.last_accepted_commit is missing or empty.")
    if not get_scalar(project, "last_known_good_commit"):
        return fail("project.last_known_good_commit is missing or empty.")
    if not get_required_commands(get_section(lines, "validation")):
        return fail("validation.required_commands is missing or empty.")
    critical = get_critical_invariants(get_section(lines, "invariants"))
    if not critical:
        return fail("invariants.critical is missing or empty.")
    for i, inv in enumerate(critical, 1):
        if not inv.get("id") or not inv.get("rule") or not inv.get("test"):
            return fail(f"critical invariant #{i} is missing id, rule, or test.")
    head = subprocess.run(
        ["git", "rev-parse", "HEAD"], cwd=ROOT, check=False, capture_output=True, text=True
    ).stdout.strip()
    if head and not head.startswith(last_accepted):
        print("WARNING: PROJECT_STATE last_accepted_commit may be stale.")
    print("PROJECT_STATE.yml validation PASSED")
    return 0

if __name__ == "__main__":
    sys.exit(main())
