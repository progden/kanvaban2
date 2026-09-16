"""GH：Gherkin 格式。"""
from __future__ import annotations

from ..model import Finding, Loc, Model, Scenario
from . import check, finding

STATUS = ("@added", "@changed", "@deprecated")
MSG_KEYWORDS = ("錯誤訊息", "確認訊息", "提示")


def _tag_rank(tag: str) -> int | None:
    if tag in STATUS:
        return 0
    if tag == "@wip":
        return 1
    if tag.startswith("@CR-"):
        return 2
    if tag.startswith("@uc-"):
        return 3
    if tag.startswith("@fail-"):
        return 4
    return None


def _loc(sc: Scenario) -> Loc:
    return sc.tag_loc or sc.loc


@check("GH-01", "spec-check", "error", "Scenario 恰好一個 @uc-，至多一個 @fail-")
def gh_01(model: Model) -> list[Finding]:
    out = []
    for spec in model.specs:
        for f in spec.features:
            local = {u.id for u in f.usecases}
            for sc in f.scenarios:
                if not sc.ucs:
                    out.append(finding(_loc(sc), "error", "GH-01", f'Scenario "{sc.name}" 缺少 @uc- tag'))
                elif len(sc.ucs) > 1:
                    out.append(finding(_loc(sc), "error", "GH-01", f'Scenario "{sc.name}" 有多個 @uc- tag'))
                elif sc.ucs[0] not in local:
                    out.append(finding(_loc(sc), "error", "GH-01", f'Scenario "{sc.name}" @uc-{sc.ucs[0]} 不屬於本 Feature'))
                if len(sc.fails) > 1:
                    out.append(finding(_loc(sc), "error", "GH-01", f'Scenario "{sc.name}" 有多個 @fail- tag'))
    return out


@check("GH-02", "spec-check", "error", "Scenario 有 Aggregate 註解")
def gh_02(model: Model) -> list[Finding]:
    out = []
    for sc in model.scenarios:
        if sc.aggregate_loc is None or not sc.aggregates:
            out.append(finding(sc.loc, "error", "GH-02", f'Scenario "{sc.name}" 缺少 Aggregate 註解'))
            continue
        if not sc.aggregate_after_tags:
            out.append(finding(sc.aggregate_loc, "error", "GH-02", f'Scenario "{sc.name}" 的 Aggregate 註解必須放在 tag 行之後'))
        for e in sc.aggregate_errors:
            out.append(finding(sc.aggregate_loc, "error", "GH-02", f'Scenario "{sc.name}" 的 Aggregate 註解錯誤：{e}'))
    return out


@check("GH-03", "spec-check", "error", "Feature 標頭三行齊全")
def gh_03(model: Model) -> list[Finding]:
    out = []
    for spec in model.specs:
        for f in spec.features:
            if f.gherkin_loc is None:
                out.append(finding(f.loc, "error", "GH-03", f'Feature "{f.name}" 標頭不完整：缺少 "gherkin 區塊"'))
                continue
            if f.gherkin_name is None:
                out.append(finding(f.gherkin_loc, "error", "GH-03", f'Feature "{f.name}" 標頭不完整：缺少 "Feature:"'))
                continue
            for k in f.header_missing:
                out.append(finding(f.gherkin_loc, "error", "GH-03", f'Feature "{f.name}" 標頭不完整：缺少 "{k}"'))
            if f.gherkin_name != f.name:
                out.append(finding(f.gherkin_loc, "error", "GH-03", f'Feature "{f.name}" 標頭不完整：gherkin 的 Feature 名稱 "{f.gherkin_name}" 與標題不同'))
    return out


@check("GH-04", "spec-check", "error", "tag 順序與 @changed/@deprecated 配對")
def gh_04(model: Model) -> list[Finding]:
    out = []
    for spec in model.specs:
        for f in spec.features:
            deprecated = [sc for sc in f.scenarios if "@deprecated" in sc.tags]
            for sc in f.scenarios:
                ranks = []
                for t in sc.tags:
                    r = _tag_rank(t)
                    if r is None:
                        out.append(finding(_loc(sc), "error", "GH-04", f'Scenario "{sc.name}" tag 錯誤：未知 tag {t}'))
                    else:
                        ranks.append(r)
                if ranks != sorted(ranks):
                    out.append(finding(_loc(sc), "error", "GH-04", f'Scenario "{sc.name}" tag 錯誤：順序應為 狀態 → @wip → @CR- → @uc- → @fail-'))
                if len(sc.status_tags) > 1:
                    out.append(finding(_loc(sc), "error", "GH-04", f'Scenario "{sc.name}" tag 錯誤：{sc.status_tags} 互斥'))
                if sc.wip and not sc.crs:
                    out.append(finding(_loc(sc), "error", "GH-04", f'Scenario "{sc.name}" tag 錯誤：@wip 必須伴隨 @CR-'))
                if "@changed" in sc.tags:
                    ok = any(set(d.crs) & set(sc.crs) and d.uc == sc.uc for d in deprecated)
                    if not ok:
                        out.append(finding(_loc(sc), "error", "GH-04", f'Scenario "{sc.name}" tag 錯誤：@changed 沒有同票號、同 @uc- 的 @deprecated Scenario'))
    return out


@check("GH-05", ("spec-check", "cr-check"), "error", "已定稿的新增／變更 Scenario、以及開發中模組被 diff 動到的 Scenario，必有 @CR-")
def gh_05(model: Model) -> list[Finding]:
    out = []
    for spec in model.specs:
        if not spec.finalized:
            continue
        for sc in spec.scenarios:
            if sc.status_tags and not sc.crs:
                out.append(finding(_loc(sc), "error", "GH-05", f'Scenario "{sc.name}" 規格已定稿，新增／變更卻沒有 @CR- tag'))
    # diff 部分（cr-check 有 base 時）由 checks/cr.py 的 gh_05_diff 補上
    if model.diff is not None:
        from .cr import gh_05_diff

        out.extend(gh_05_diff(model))
    return out


@check("GH-06", "spec-check", "error", "Aggregate 註解與 crud 一致")
def gh_06(model: Model) -> list[Finding]:
    out = []
    ucs = model.usecases
    for sc in model.scenarios:
        u = ucs.get(sc.uc or "")
        if u is None or not sc.aggregates:
            continue
        problems = []
        for ent, modes in sc.aggregates.items():
            if ent not in u.crud:
                problems.append(f"註解有 {ent} 但 crud 沒有")
            elif "write" in modes and not any(ch in u.crud[ent] for ch in "CUD"):
                problems.append(f"註解 {ent} 標 write 但 crud.{ent}={u.crud[ent]}")
        if sc.is_success:
            for ent, v in u.crud.items():
                if any(ch in v for ch in "CUD") and "write" not in sc.aggregates.get(ent, set()):
                    problems.append(f"crud.{ent}={v} 但註解未標 {ent}: write")
        for p in problems:
            out.append(finding(sc.aggregate_loc or sc.loc, "error", "GH-06", f'Scenario "{sc.name}" 的 Aggregate 註解與 uc "{u.id}" 的 crud 不一致：{p}'))
    return out


def _edit_distance(a: str, b: str) -> int:
    prev = list(range(len(b) + 1))
    for i, ca in enumerate(a, 1):
        cur = [i]
        for j, cb in enumerate(b, 1):
            cur.append(min(prev[j] + 1, cur[j - 1] + 1, prev[j - 1] + (ca != cb)))
        prev = cur
    return prev[-1]


@check("GH-07", "spec-check", "warn", "同一 Use Case 的訊息文字一致")
def gh_07(model: Model) -> list[Finding]:
    out = []
    by_uc: dict[str, list[tuple[str, Loc]]] = {}
    for sc in model.scenarios:
        if not sc.uc:
            continue
        for st in sc.steps:
            if st.is_then and any(k in st.text for k in MSG_KEYWORDS):
                for q in st.quoted:
                    by_uc.setdefault(sc.uc, []).append((q, st.loc))
    for uc, items in by_uc.items():
        seen: set[tuple[str, str]] = set()
        for i in range(len(items)):
            for j in range(i + 1, len(items)):
                a, b = items[i][0], items[j][0]
                if a == b or (a, b) in seen or (b, a) in seen:
                    continue
                if _edit_distance(a, b) <= 3:
                    seen.add((a, b))
                    out.append(finding(items[j][1], "warn", "GH-07", f'uc "{uc}" 的訊息文字不一致："{a}" vs "{b}"'))
    return out


@check("GH-08", "spec-check", "error", "文件固定結構")
def gh_08(model: Model) -> list[Finding]:
    out = []
    for spec in model.specs:
        for line, detail in spec.structure_errors:
            out.append(finding(Loc(spec.path, line), "error", "GH-08", f"文件結構錯誤：{detail}"))
    return out


@check("GH-09", "spec-check", "error", "步驟不含反引號")
def gh_09(model: Model) -> list[Finding]:
    out = []
    for sc in model.scenarios:
        for st in sc.steps:
            if "`" in st.text:
                out.append(finding(st.loc, "error", "GH-09", f'Scenario "{sc.name}" 的步驟含反引號'))
                break
    return out
