"""REF：參照完整性。"""
from __future__ import annotations

from ..ids import KIND_LABEL, is_valid_entity_id
from ..model import Finding, Loc, Model, Ref
from . import check, finding


def resolve(model: Model, ref: Ref) -> bool:
    """反引號 ID 能否在定義處找到。"""
    k = ref.kind
    if k == "path":
        return True  # 檔案路徑不是 ID，略過
    if k == "entity":
        return ref.id in model.entities
    if k == "attribute":
        return ref.id in model.attributes
    if k == "role":
        return ref.id in model.roles
    if k == "usecase":
        return ref.id in model.usecases
    if k == "event":
        return ref.id in model.events
    if k == "screen":
        return ref.id in model.screens
    return False


@check("REF-01", "spec-check", "error", "spec 反引號 ID 皆已定義")
def ref_01(model: Model) -> list[Finding]:
    out = []
    for spec in model.specs:
        for r in spec.refs:
            if not resolve(model, r):
                out.append(finding(r.loc, "error", "REF-01", f'反引號 ID "{r.id}" 未定義（種類：{KIND_LABEL[r.kind]}）'))
    return out


@check("REF-02", "spec-check", "error", "Aggregate 註解名稱是實體 ID")
def ref_02(model: Model) -> list[Finding]:
    out = []
    ents = model.entities
    for sc in model.scenarios:
        for name in sc.aggregates:
            if name not in ents:
                out.append(finding(sc.aggregate_loc or sc.loc, "error", "REF-02", f'Aggregate 註解 "{name}" 不是實體 ID'))
    return out


@check("REF-03", "spec-check", "error", "Feature 標頭角色名稱在角色表")
def ref_03(model: Model) -> list[Finding]:
    out = []
    names = model.role_names
    for spec in model.specs:
        for f in spec.features:
            if f.role_name is not None and f.role_name not in names:
                out.append(finding(f.gherkin_loc or f.loc, "error", "REF-03", f'Feature 標頭角色 "{f.role_name}" 不在角色定義表'))
    return out


@check("REF-04", "spec-check", "error", "欄位 ID 的實體存在")
def ref_04(model: Model) -> list[Finding]:
    out = []
    ents = model.entities
    for a in model.attributes.values():
        if not a.attr or a.entity not in ents:
            out.append(finding(a.loc, "error", "REF-04", f'欄位 "{a.id}" 的實體 "{a.entity}" 不存在'))
    return out


@check("REF-05", "spec-check", "error", "關係表來源／目標是實體 ID")
def ref_05(model: Model) -> list[Finding]:
    out = []
    ents = model.entities
    for r in model.relations:
        for side, val in (("來源", r.source), ("目標", r.target)):
            if val not in ents:
                out.append(finding(r.loc, "error", "REF-05", f'關係 {r.source}→{r.target} 的 "{side}" 不是實體 ID'))
        if not r.min.isdigit():
            out.append(finding(r.loc, "error", "REF-05", f'關係 {r.source}→{r.target} 的 "min" 必須是整數'))
        if not (r.max.isdigit() or r.max == "n"):
            out.append(finding(r.loc, "error", "REF-05", f'關係 {r.source}→{r.target} 的 "max" 必須是整數或 n'))
    return out


@check("REF-06", "spec-check", "error", "@CR-xxx 已在 CR.md 登記")
def ref_06(model: Model) -> list[Finding]:
    out = []
    ids = model.cr_ids
    for spec in model.specs:
        for sc in spec.scenarios:
            for cr in sc.crs:
                if cr not in ids:
                    out.append(finding(sc.tag_loc or sc.loc, "error", "REF-06", f"@{cr} 未在 .dev/CR.md 登記"))
        for row in spec.changelog:
            if row.cr and row.cr not in ids:
                out.append(finding(row.loc, "error", "REF-06", f"@{row.cr} 未在 .dev/CR.md 登記"))
    return out


@check("REF-07", "ui-check", "error", "ui 反引號 ID 皆已定義")
def ref_07(model: Model) -> list[Finding]:
    out = []
    for d in model.designs:
        for r in d.refs:
            if not resolve(model, r):
                out.append(finding(r.loc, "error", "REF-07", f'ui 檔引用的 ID "{r.id}" 未定義（種類：{KIND_LABEL[r.kind]}）'))
    return out


@check("REF-08", ("spec-check", "ui-check"), "error", "ID 全專案唯一")
def ref_08(model: Model) -> list[Finding]:
    out = []
    defs: dict[str, list[Loc]] = {}
    for spec in model.specs:
        for e in spec.entities:
            defs.setdefault(e.id, []).append(e.loc)
        for a in spec.attributes:
            defs.setdefault(a.id, []).append(a.loc)
        for r in spec.roles:
            defs.setdefault(r.id, []).append(r.loc)
        for u in spec.usecases:
            defs.setdefault(u.id, []).append(u.loc)
    for d in model.designs:
        for s in d.screens:
            defs.setdefault(s.id, []).append(s.loc)
    for id_, locs in defs.items():
        if len(locs) > 1:
            first = locs[0]
            for dup in locs[1:]:
                out.append(finding(dup, "error", "REF-08", f'ID "{id_}" 重複定義於 {first.file}:{first.line} 與 {dup.file}:{dup.line}'))
    return out


@check("REF-09", "spec-check", "error", "實體 ID 格式合法")
def ref_09(model: Model) -> list[Finding]:
    out = []
    for spec in model.specs:
        for e in spec.entities:
            if not is_valid_entity_id(e.id):
                out.append(finding(e.loc, "error", "REF-09", f'實體 ID "{e.id}" 格式不合法'))
    return out
