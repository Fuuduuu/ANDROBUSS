# AGENTS

## Roles

- Claude: architecture and review.
- Codex: implementation.
- Qwen/other low-cost model: cheap review and cross-check.
- Human: final decisions and field testing.

## Sniper Prompt Format

Use this structure for each pass:

1. PASS
2. TYPE
3. Gate
4. Read
5. Touch
6. Never
7. Do
8. Validate
9. Output

## Execution Principle

Follow docs-first, pass-based execution. Keep each pass narrow, verifiable, and auditable.
