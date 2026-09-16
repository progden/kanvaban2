"""DS：ui 內部與跨檔。"""
from __future__ import annotations

import re

from ..model import Finding, Loc, Model
from ..parser_design import SECTIONS, STATE_ITEMS
from . import check, finding

DASH = ("—", "-", "－", "–")


@check("DS-01", "ui-check", "error", "畫面標題與標頭格式")
def ds_01(model: Model) -> list[Finding]:
    out = []
    for d in model.designs:
        for line, detail in d.structure_errors:
            out.append(finding(Loc(d.path, line), "error", "DS-01", f"畫面標題或標頭格式錯誤：{detail}"))
        spec = model.spec_of_module(d.module)
        if spec is None:
            out.append(finding(Loc(d.path, 1), "error", "DS-01", f"畫面標題或標頭格式錯誤：找不到對應的 spec-{d.module}.md"))
        feat_names = {f.name for f in spec.features} if spec else set()
        for sc in d.screens:
            for e in sc.header_errors:
                out.append(finding(sc.loc, "error", "DS-01", f"畫面標題或標頭格式錯誤：{sc.id} {e}"))
            if spec and sc.feature is not None and sc.feature not in feat_names:
                out.append(finding(sc.loc, "error", "DS-01", f"畫面標題或標頭格式錯誤：{sc.id} 的所屬 Feature「{sc.feature}」不在 spec 中"))
    return out


@check("DS-02", "ui-check", "error", "八個段落齊全且順序固定")
def ds_02(model: Model) -> list[Finding]:
    out = []
    for d in model.designs:
        for sc in d.screens:
            if sc.section_order == SECTIONS:
                continue
            missing = [s for s in SECTIONS if s not in sc.section_order]
            if missing:
                for s in missing:
                    out.append(finding(sc.loc, "error", "DS-02", f'畫面 "{sc.id}" 缺少段落 "{s}" 或順序錯誤'))
            else:
                out.append(finding(sc.loc, "error", "DS-02", f'畫面 "{sc.id}" 缺少段落 "" 或順序錯誤：實際順序 {sc.section_order}'))
    return out


@check("DS-03", "ui-check", "error", "表格欄位的 ID 種類正確")
def ds_03(model: Model) -> list[Finding]:
    out = []
    expect = {"資料表.來源": ({"attribute", "entity"}, "Attribute 或 Entity"), "操作表.觸發": ({"usecase"}, "UseCase"), "角色表.角色": ({"role"}, "Role")}
    for d in model.designs:
        for sc in d.screens:
            for key, refs in sc.col_refs.items():
                kinds, label = expect[key]
                table, col = key.split(".")
                for r in refs:
                    if r.kind not in kinds:
                        out.append(finding(r.loc, "error", "DS-03", f'畫面 "{sc.id}" 的 {table}.{col} "{r.id}" 種類錯誤，應為 {label}'))
            # 操作表：觸發欄必須是反引號 uc 或 —
            if sc.ops:
                header = sc.ops[0].cells
                idx = header.index("觸發") if "觸發" in header else 1
                for row in sc.ops[1:]:
                    cell = row.cells[idx] if idx < len(row.cells) else ""
                    if cell.strip() in DASH or "`" in cell:
                        continue
                    out.append(finding(row.loc, "error", "DS-03", f'畫面 "{sc.id}" 的 操作表.觸發 "{cell}" 種類錯誤，應為 UseCase'))
            for r in sc.nav_refs:
                if r.kind != "screen":
                    out.append(finding(r.loc, "error", "DS-03", f'畫面 "{sc.id}" 的 進入與離開.畫面 "{r.id}" 種類錯誤，應為 Screen'))
    return out


@check("DS-04", "ui-check", "error", "狀態段五項齊全")
def ds_04(model: Model) -> list[Finding]:
    out = []
    for d in model.designs:
        for sc in d.screens:
            loc = sc.sections.get("狀態", sc.loc)
            for item in STATE_ITEMS:
                if not sc.states.get(item):
                    out.append(finding(loc, "error", "DS-04", f'畫面 "{sc.id}" 的狀態段缺少 "{item}"'))
    return out


def _split_ops(cell: str) -> set[str]:
    return {x.strip() for x in re.split(r"[、,，]", cell) if x.strip()}


@check("DS-05", "ui-check", "error", "操作可用角色與 uc roles 一致")
def ds_05(model: Model) -> list[Finding]:
    out = []
    ucs = model.usecases
    for d in model.designs:
        for sc in d.screens:
            if not sc.ops or not sc.role_rows:
                continue
            oh = sc.ops[0].cells
            op_idx = oh.index("操作") if "操作" in oh else 0
            tr_idx = oh.index("觸發") if "觸發" in oh else 1
            rh = sc.role_rows[0].cells
            role_idx = rh.index("角色") if "角色" in rh else 0
            can_idx = rh.index("做得到") if "做得到" in rh else 2
            role_can: dict[str, set[str]] = {}
            for row in sc.role_rows[1:]:
                rid = "".join(re.findall(r"`([^`]+)`", row.cells[role_idx] if role_idx < len(row.cells) else ""))
                can = row.cells[can_idx] if can_idx < len(row.cells) else ""
                role_can[rid] = _split_ops(can)
            for row in sc.ops[1:]:
                op = row.cells[op_idx] if op_idx < len(row.cells) else ""
                trig = row.cells[tr_idx] if tr_idx < len(row.cells) else ""
                for uid in re.findall(r"`([^`]+)`", trig):
                    u = ucs.get(uid)
                    if u is None:
                        continue
                    allowed = {r for r, ops in role_can.items() if op in ops}
                    if allowed != set(u.roles):
                        out.append(finding(row.loc, "error", "DS-05", f'畫面 "{sc.id}" 操作 "{op}" 的可用角色 {sorted(allowed)} 與 uc "{uid}" 的 roles {sorted(u.roles)} 不一致'))
    return out


def _triggered_ucs(model: Model) -> set[str]:
    out: set[str] = set()
    for d in model.designs:
        for sc in d.screens:
            for r in sc.col_refs.get("操作表.觸發", []):
                out.add(r.id)
    return out


@check("DS-06", "ui-check", "warn", "寫入 Use Case 至少被一個畫面觸發")
def ds_06(model: Model) -> list[Finding]:
    out = []
    triggered = _triggered_ucs(model)
    for u in model.usecases.values():
        if u.is_write and not u.requires and u.id not in triggered:
            out.append(finding(u.loc, "warn", "DS-06", f'寫入 uc "{u.id}" 沒有任何畫面觸發它'))
    return out


@check("DS-07", "ui-check", "warn", "畫面被導向或是模組入口")
def ds_07(model: Model) -> list[Finding]:
    out = []
    referenced: set[str] = set()
    for d in model.designs:
        for sc in d.screens:
            for sid in sc.nav_from + sc.nav_to:
                if sid != sc.id:
                    referenced.add(sid)
    for d in model.designs:
        for sc in d.screens:
            if sc.is_entry or sc.id in referenced:
                continue
            out.append(finding(sc.loc, "warn", "DS-07", f'畫面 "{sc.id}" 沒有任何畫面導向它，也不是模組入口'))
    return out


@check("DS-08", "ui-check", "error", "ui 檔不定義新概念")
def ds_08(model: Model) -> list[Finding]:
    out = []
    for d in model.designs:
        for line, what in d.forbidden_sections:
            out.append(finding(Loc(d.path, line), "error", "DS-08", f'ui 檔不得含 "{what}"'))
    return out
