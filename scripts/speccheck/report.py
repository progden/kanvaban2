"""`--report`：從模型即時印出矩陣，不寫檔、不進版控（scripts.md §2.2、§3.2）。"""
from __future__ import annotations

from typing import Optional

from .model import Model


def _table(header: list[str], rows: list[list[str]]) -> str:
    lines = ["| " + " | ".join(header) + " |", "|" + "|".join("---" for _ in header) + "|"]
    for r in rows:
        lines.append("| " + " | ".join(r) + " |")
    return "\n".join(lines)


def _screens_by_uc(model: Model) -> dict[str, list[str]]:
    out: dict[str, list[str]] = {}
    for d in model.designs:
        for sc in d.screens:
            for r in sc.col_refs.get("操作表.觸發", []):
                out.setdefault(r.id, []).append(sc.id)
    return out


def _filter_ucs(model: Model, only_cr: Optional[str]) -> set[str] | None:
    if not only_cr:
        return None
    for c in model.crs:
        if c.id == only_cr:
            ids = {i.id for i in c.impact}
            ucs = {u.id for u in model.usecases.values() if u.id in ids or any(e in ids for e in u.crud)}
            return ucs
    return set()


def spec_report(model: Model, only_cr: Optional[str] = None) -> str:
    ucs = list(model.usecases.values())
    keep = _filter_ucs(model, only_cr)
    if keep is not None:
        ucs = [u for u in ucs if u.id in keep]
    ents = sorted(model.entities)
    roles = sorted(model.roles)
    parts = []

    parts.append("## CRUD 矩陣\n" + _table(["Feature", "UseCase"] + ents, [[u.feature, u.id] + [u.crud.get(e, "") for e in ents] for u in ucs]))
    parts.append("## 角色 × UseCase\n" + _table(["UseCase"] + roles, [[u.id] + ["✓" if r in u.roles else "" for r in roles] for u in ucs]))

    ev_rows = []
    for ev, emitters in sorted(model.events.items()):
        req = [u.id for u in model.usecases.values() if ev in u.requires]
        if keep is not None and not ({e.id for e in emitters} & keep or set(req) & keep):
            continue
        ev_rows.append([ev, "、".join(e.id for e in emitters), "、".join(req) or "—"])
    parts.append("## 事件表\n" + _table(["Event", "emits", "requires"], ev_rows))

    has_design = bool(model.designs)
    by_uc = _screens_by_uc(model)
    header = ["Feature", "UseCase", "Entity"] + (["Screen"] if has_design else [])
    rows = []
    for u in ucs:
        row = [u.feature, u.id, "、".join(f"{e}({v})" for e, v in u.crud.items())]
        if has_design:
            row.append("、".join(by_uc.get(u.id, [])) or "—")
        rows.append(row)
    parts.append("## 追溯矩陣\n" + _table(header, rows))

    # 外部引用：本模組引用、但定義在其他模組的 ID
    def_module: dict[str, str] = {}
    for s in model.specs:
        for x in list(s.entities) + list(s.attributes) + list(s.roles) + list(s.usecases):
            def_module[x.id] = s.module
        for u in s.usecases:
            for ev in u.emits:
                def_module[ev] = s.module
    ext_rows = []
    for s in model.specs:
        seen: set[str] = set()
        for r in s.refs:
            owner = def_module.get(r.id)
            if owner and owner != s.module and r.id not in seen:
                seen.add(r.id)
                ext_rows.append([s.module, r.id, f"spec-{owner}.md"])
    parts.append("## 外部引用\n" + (_table(["模組", "引用的 ID", "定義於"], ext_rows) if ext_rows else "（無）"))
    return "\n\n".join(parts) + "\n"


def design_report(model: Model) -> str:
    parts = []
    rows = []
    for d in model.designs:
        for sc in d.screens:
            trig = [r.id for r in sc.col_refs.get("操作表.觸發", [])]
            rows.append([sc.id, sc.name, sc.feature or "", sc.type or "", "、".join(trig) or "—", sc.status or ""])
    parts.append("## 畫面總表\n" + _table(["Screen", "名稱", "所屬 Feature", "類型", "觸發 UseCase", "狀態"], rows))

    by_uc = _screens_by_uc(model)
    rows = []
    for u in model.usecases.values():
        rows.append([u.feature, u.id, "、".join(f"{e}({v})" for e, v in u.crud.items()), "、".join(by_uc.get(u.id, [])) or "—"])
    parts.append("## 追溯矩陣\n" + _table(["Feature", "UseCase", "Entity", "Screen"], rows))

    untriggered = [u.id for u in model.usecases.values() if u.is_write and not u.requires and u.id not in by_uc]
    parts.append("## 未被畫面觸發的寫入 UseCase\n" + ("\n".join(f"- {u}" for u in untriggered) if untriggered else "（無）"))
    return "\n\n".join(parts) + "\n"
