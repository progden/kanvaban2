"""UC：Use Case 一致性。"""
from __future__ import annotations

from ..ids import kind_of, split_attr
from ..model import Finding, Loc, Model, UseCase
from ..parser_spec import FAIL_FIXED_SENTENCE
from . import check, finding


@check("UC-01", "spec-check", "error", "usecase 區塊格式")
def uc_01(model: Model) -> list[Finding]:
    out = []
    for spec in model.specs:
        for f in spec.features:
            if f.usecase_section_count != 1:
                out.append(finding(f.loc, "error", "UC-01", f"usecase 區塊格式錯誤：Feature「{f.name}」的「### Use Case 定義」應恰好一個，實際 {f.usecase_section_count} 個"))
            if f.usecase_block_count != 1:
                out.append(finding(f.loc, "error", "UC-01", f"usecase 區塊格式錯誤：Feature「{f.name}」的 ```usecase 區塊應恰好一個，實際 {f.usecase_block_count} 個"))
            elif not f.usecase_before_gherkin:
                out.append(finding(f.usecase_block_loc or f.loc, "error", "UC-01", f"usecase 區塊格式錯誤：Feature「{f.name}」的 usecase 區塊必須在 gherkin 區塊之前"))
            for line, detail in f.usecase_errors:
                out.append(finding(Loc(spec.path, line), "error", "UC-01", f"usecase 區塊格式錯誤：{detail}"))
    return out


@check("UC-02", "spec-check", "error", "fail 的 key 都在 pre")
def uc_02(model: Model) -> list[Finding]:
    out = []
    for u in model.usecases.values():
        for k, s in u.fail.items():
            if k not in u.pre:
                out.append(finding(s.loc, "error", "UC-02", f'uc "{u.id}" 的 fail.{k} 在 pre 中不存在'))
    return out


@check("UC-03", "spec-check", "error", "pre/post/fail 每句至少一個 ID")
def uc_03(model: Model) -> list[Finding]:
    out = []
    for u in model.usecases.values():
        for field, sents in (("pre", list(u.pre.values())), ("post", u.post), ("fail", list(u.fail.values()))):
            for s in sents:
                if s.ids:
                    continue
                if field == "fail" and s.text.strip() == FAIL_FIXED_SENTENCE:
                    continue
                out.append(finding(s.loc, "error", "UC-03", f'uc "{u.id}" 的 {field} 句 "{s.text}" 沒有引用任何 ID'))
    return out


@check("UC-04", "spec-check", "error", "@fail- 指向的 key 存在")
def uc_04(model: Model) -> list[Finding]:
    out = []
    ucs = model.usecases
    for sc in model.scenarios:
        u = ucs.get(sc.uc or "")
        if u is None:
            continue
        for key in sc.fails:
            if key not in u.fail:
                out.append(finding(sc.tag_loc or sc.loc, "error", "UC-04", f'Scenario "{sc.name}" 的 @fail-{key} 在 uc "{u.id}" 的 fail 中不存在'))
    return out


@check("UC-05", "spec-check", "error", "fail 每個 key 有 @fail- Scenario")
def uc_05(model: Model) -> list[Finding]:
    out = []
    covered: set[tuple[str, str]] = {(sc.uc, k) for sc in model.scenarios if sc.uc for k in sc.fails}
    for u in model.usecases.values():
        for k, s in u.fail.items():
            if (u.id, k) not in covered:
                out.append(finding(s.loc, "error", "UC-05", f'uc "{u.id}" 的 fail.{k} 沒有對應的 @fail-{k} Scenario'))
    return out


@check("UC-06", "spec-check", "error", "成功 Scenario 與寫入 Use Case 必填")
def uc_06(model: Model) -> list[Finding]:
    out = []
    has_success = {sc.uc for sc in model.scenarios if sc.uc and sc.is_success}
    for u in model.usecases.values():
        if u.id not in has_success:
            out.append(finding(u.loc, "error", "UC-06", f'uc "{u.id}" 沒有成功 Scenario'))
        if u.is_write:
            if not u.roles and not u.requires:
                out.append(finding(u.loc, "error", "UC-06", f'uc "{u.id}" 為寫入 Use Case 但 roles 為空'))
            if not u.pre:
                out.append(finding(u.loc, "error", "UC-06", f'uc "{u.id}" 為寫入 Use Case 但 pre 為空'))
            if not u.post:
                out.append(finding(u.loc, "error", "UC-06", f'uc "{u.id}" 為寫入 Use Case 但 post 為空'))
    return out


@check("UC-07", "spec-check", "error", "post 更新的欄位其實體 crud 含 C/U")
def uc_07(model: Model) -> list[Finding]:
    out = []
    for u in model.usecases.values():
        for s in u.post:
            for rid in s.ids:
                if kind_of(rid) != "attribute":
                    continue
                ent, _ = split_attr(rid)
                if not any(ch in u.crud.get(ent, "") for ch in "CU"):
                    out.append(finding(s.loc, "error", "UC-07", f'uc "{u.id}" 的 post 更新 "{rid}"，但 crud.{ent} 不含 C/U'))
    return out


@check("UC-08", "spec-check", "error", "事件恰好一個 emits")
def uc_08(model: Model) -> list[Finding]:
    out = []
    for ev, emitters in model.events.items():
        if len(emitters) > 1:
            names = "、".join(u.id for u in emitters)
            out.append(finding(emitters[1].loc, "error", "UC-08", f'事件 "{ev}" 被 {len(emitters)} 個 Use Case emits：{names}'))
    return out


@check("UC-09", "spec-check", "warn", "事件至少一個 requires")
def uc_09(model: Model) -> list[Finding]:
    out = []
    required = {ev for u in model.usecases.values() for ev in u.requires}
    for ev, emitters in model.events.items():
        if ev not in required:
            out.append(finding(emitters[0].loc, "warn", "UC-09", f'事件 "{ev}" 沒有任何 Use Case requires'))
    return out


@check("UC-10", "spec-check", "warn", "max=1 關係的 post 不得新增第二筆（字面）")
def uc_10(model: Model) -> list[Finding]:
    out = []
    rels = [r for r in model.relations if r.max == "1"]
    for u in model.usecases.values():
        for s in u.post:
            ents = {split_attr(x)[0] if kind_of(x) == "attribute" else x for x in s.ids}
            for r in rels:
                if r.source in ents and r.target in ents and "新增" in s.text:
                    out.append(finding(s.loc, "warn", "UC-10", f'uc "{u.id}" 的 post "{s.text}" 可能違反 {r.source}→{r.target} max=1'))
    return out


def _find_cycle(edges: dict[str, set[str]]) -> list[str] | None:
    """DFS 找一個循環，回傳節點路徑；沒有回 None。"""
    WHITE, GRAY, BLACK = 0, 1, 2
    color: dict[str, int] = {n: WHITE for n in edges}
    stack: list[str] = []

    def dfs(n: str) -> list[str] | None:
        color[n] = GRAY
        stack.append(n)
        for m in sorted(edges.get(n, ())):
            c = color.get(m, WHITE)
            if c == GRAY:
                return stack[stack.index(m):] + [m]
            if c == WHITE:
                color.setdefault(m, WHITE)
                r = dfs(m)
                if r:
                    return r
        stack.pop()
        color[n] = BLACK
        return None

    for n in sorted(edges):
        if color[n] == WHITE:
            r = dfs(n)
            if r:
                return r
    return None


@check("UC-11", "spec-check", "error", "requires 無循環")
def uc_11(model: Model) -> list[Finding]:
    ucs = model.usecases
    events = model.events
    edges: dict[str, set[str]] = {u: set() for u in ucs}
    for u in ucs.values():
        for ev in u.requires:
            for emitter in events.get(ev, []):
                edges.setdefault(emitter.id, set()).add(u.id)
    cyc = _find_cycle(edges)
    if not cyc:
        return []
    return [finding(ucs[cyc[0]].loc, "error", "UC-11", "requires 形成循環：" + " → ".join(cyc))]


@check("UC-12", "spec-check", "error", "calls-sync 無循環")
def uc_12(model: Model) -> list[Finding]:
    ucs = model.usecases
    edges: dict[str, set[str]] = {u: set(x.calls_sync) for u, x in ucs.items()}
    cyc = _find_cycle(edges)
    if not cyc:
        return []
    start = ucs.get(cyc[0])
    loc = start.loc if start else Loc(model.specs[0].path, 1)
    return [finding(loc, "error", "UC-12", "calls-sync 形成循環：" + " → ".join(cyc))]


@check("UC-13", "spec-check", "warn", "每個實體至少被一個 Use Case 建立")
def uc_13(model: Model) -> list[Finding]:
    out = []
    created = {e for u in model.usecases.values() for e, v in u.crud.items() if "C" in v}
    for e in model.entities.values():
        if e.id not in created:
            out.append(finding(e.loc, "warn", "UC-13", f'實體 "{e.id}" 沒有任何 Use Case 建立它'))
    return out


@check("UC-14", "spec-check", "error", "roles 是 r- ID、crud key 是實體 ID")
def uc_14(model: Model) -> list[Finding]:
    out = []
    ents = model.entities
    for u in model.usecases.values():
        for r in u.roles:
            if kind_of(r) != "role":
                out.append(finding(u.loc, "error", "UC-14", f'uc "{u.id}" 的 roles 含非法 ID "{r}"'))
        for e in u.crud:
            if e not in ents:
                out.append(finding(u.loc, "error", "UC-14", f'uc "{u.id}" 的 crud 含非法 ID "{e}"'))
    return out
