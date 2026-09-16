"""物件模型：三支腳本共用的記憶體內資料結構。

解析器只負責把 Markdown 轉成這裡的物件，檢查函式只讀這些物件，
兩層互不依賴（見 `.dev/conventions/scripts.md` §1.2）。
"""
from __future__ import annotations

from dataclasses import dataclass, field
from typing import Optional


@dataclass(frozen=True)
class Loc:
    """檔案位置，行號為 1-based。"""

    file: str
    line: int


@dataclass
class Finding:
    """一筆檢查結果。"""

    file: str
    line: int
    level: str  # "error" 或 "warn"
    check: str  # 檢查 ID，例如 "REF-01"
    message: str

    def format(self) -> str:
        return f"{self.file}:{self.line}: {self.level} {self.check}: {self.message}"


@dataclass
class Ref:
    """自由文字裡掃到的一個反引號 ID。"""

    id: str
    kind: str  # entity / attribute / role / usecase / event / screen / invalid
    loc: Loc
    context: str  # 說明來源：例如 "usecase pre"、"ui 資料表.來源"


@dataclass
class Entity:
    id: str
    name: str
    desc: str
    loc: Loc
    aggregate: str = ""  # 「所屬 Aggregate」欄，自由文字，腳本不解析


@dataclass
class Attribute:
    id: str
    entity: str
    attr: str
    type: str
    constraint: str
    desc: str
    loc: Loc


@dataclass
class Relation:
    source: str
    target: str
    min: str
    max: str
    desc: str
    loc: Loc


@dataclass
class Role:
    id: str
    name: str
    desc: str
    loc: Loc


@dataclass
class Sentence:
    """usecase 的 pre／post／fail 一句，附上句中反引號 ID。"""

    text: str
    ids: list[str]
    loc: Loc


@dataclass
class UseCase:
    id: str
    name: str
    feature: str
    roles: list[str]
    crud: dict[str, str]
    pre: dict[str, Sentence]
    post: list[Sentence]
    fail: dict[str, Sentence]
    emits: list[str]
    requires: list[str]
    calls_sync: list[str]
    loc: Loc
    end_line: int = 0  # 項目最後一行，供 cr-check 判斷 diff 落點
    format_errors: list[tuple[int, str]] = field(default_factory=list)

    @property
    def is_write(self) -> bool:
        return any(ch in v for v in self.crud.values() for ch in "CUD")


@dataclass
class Step:
    keyword: str  # Given / When / Then / And / But
    text: str
    quoted: list[str]
    loc: Loc
    is_then: bool  # Then，或延續 Then 的 And／But


@dataclass
class Scenario:
    name: str
    feature: str
    tags: list[str]  # 原始順序
    tag_loc: Optional[Loc]
    aggregates: dict[str, set[str]]  # entity -> {"read","write"}
    aggregate_loc: Optional[Loc]
    aggregate_after_tags: bool  # 註解位置是否在 tag 行之後
    steps: list[Step]
    loc: Loc
    end_line: int = 0
    aggregate_errors: list[str] = field(default_factory=list)

    # 依 tag 前綴分類
    @property
    def status_tags(self) -> list[str]:
        return [t for t in self.tags if t in ("@added", "@changed", "@deprecated")]

    @property
    def wip(self) -> bool:
        return "@wip" in self.tags

    @property
    def crs(self) -> list[str]:
        return [t[1:] for t in self.tags if t.startswith("@CR-")]

    @property
    def ucs(self) -> list[str]:
        return [t[1:] for t in self.tags if t.startswith("@uc-")]

    @property
    def fails(self) -> list[str]:
        return [t[len("@fail-"):] for t in self.tags if t.startswith("@fail-")]

    @property
    def uc(self) -> Optional[str]:
        return self.ucs[0] if len(self.ucs) == 1 else None

    @property
    def is_success(self) -> bool:
        return not self.fails


@dataclass
class Feature:
    name: str  # `## Feature:` 標題文字
    module: str
    loc: Loc
    gherkin_name: Optional[str] = None
    gherkin_loc: Optional[Loc] = None
    role_name: Optional[str] = None
    header_missing: list[str] = field(default_factory=list)
    usecases: list[UseCase] = field(default_factory=list)
    scenarios: list[Scenario] = field(default_factory=list)
    usecase_section_count: int = 0
    usecase_block_count: int = 0
    usecase_before_gherkin: bool = True
    usecase_block_loc: Optional[Loc] = None
    usecase_errors: list[tuple[int, str]] = field(default_factory=list)
    end_line: int = 0


@dataclass
class ChangeLogRow:
    date: str
    cr: str
    type: str
    summary: str
    loc: Loc


@dataclass
class SpecFile:
    path: str
    module: str
    status: Optional[str] = None  # 「狀態：草稿／開發中」那一行的值（缺少或值不合法由 GH-08 報）
    entities: list[Entity] = field(default_factory=list)
    attributes: list[Attribute] = field(default_factory=list)
    relations: list[Relation] = field(default_factory=list)
    roles: list[Role] = field(default_factory=list)
    features: list[Feature] = field(default_factory=list)
    changelog: list[ChangeLogRow] = field(default_factory=list)
    refs: list[Ref] = field(default_factory=list)
    structure_errors: list[tuple[int, str]] = field(default_factory=list)
    entity_loc_range: dict[str, tuple[int, int]] = field(default_factory=dict)

    @property
    def in_development(self) -> bool:
        """「已進入開發」判定：檔頭「狀態：」行為「開發中」。"""
        return (self.status or "").strip() == "開發中"

    @property
    def usecases(self) -> list[UseCase]:
        return [u for f in self.features for u in f.usecases]

    @property
    def scenarios(self) -> list[Scenario]:
        return [s for f in self.features for s in f.scenarios]


@dataclass
class TableRow:
    cells: list[str]
    loc: Loc


@dataclass
class Screen:
    id: str
    name: str
    module: str
    loc: Loc
    feature: Optional[str] = None
    type: Optional[str] = None
    status: Optional[str] = None
    header_errors: list[str] = field(default_factory=list)
    sections: dict[str, Loc] = field(default_factory=dict)
    section_order: list[str] = field(default_factory=list)
    data: list[TableRow] = field(default_factory=list)
    ops: list[TableRow] = field(default_factory=list)
    role_rows: list[TableRow] = field(default_factory=list)
    nav_from: list[str] = field(default_factory=list)
    nav_to: list[str] = field(default_factory=list)
    nav_refs: list[Ref] = field(default_factory=list)
    is_entry: bool = False
    states: dict[str, str] = field(default_factory=dict)
    # 特定欄位的反引號 ID，key 為 "資料表.來源"、"操作表.觸發"、"角色表.角色"
    col_refs: dict[str, list[Ref]] = field(default_factory=dict)
    end_line: int = 0


@dataclass
class DesignFile:
    path: str
    module: str
    screens: list[Screen] = field(default_factory=list)
    refs: list[Ref] = field(default_factory=list)
    structure_errors: list[tuple[int, str]] = field(default_factory=list)
    forbidden_sections: list[tuple[int, str]] = field(default_factory=list)
    spec_missing: bool = False


@dataclass
class Impact:
    id: str
    action: str  # "" / "新增" / "移除"


@dataclass
class CR:
    id: str
    title: str
    type: str
    proposer: str
    date: str
    modules: list[str]
    impact: list[Impact]
    status: str
    done_date: str
    detail: str
    loc: Loc
    format_errors: list[str] = field(default_factory=list)

    @property
    def in_progress(self) -> bool:
        return self.status in ("修改規格", "待處理")


@dataclass
class Model:
    """三支腳本共用的完整模型。"""

    specs: list[SpecFile] = field(default_factory=list)
    designs: list[DesignFile] = field(default_factory=list)
    crs: list[CR] = field(default_factory=list)
    cr_path: Optional[str] = None
    cr_loaded: bool = False
    cr_format_errors: list[tuple[int, str]] = field(default_factory=list)
    # cr-check 才會填：PR diff 的上下文（見 checks/cr.py 的 DiffContext）
    diff: object = None

    # ---- 聚合查詢（跨檔） ----
    @property
    def entities(self) -> dict[str, Entity]:
        return {e.id: e for s in self.specs for e in s.entities}

    @property
    def attributes(self) -> dict[str, Attribute]:
        return {a.id: a for s in self.specs for a in s.attributes}

    @property
    def relations(self) -> list[Relation]:
        return [r for s in self.specs for r in s.relations]

    @property
    def roles(self) -> dict[str, Role]:
        return {r.id: r for s in self.specs for r in s.roles}

    @property
    def role_names(self) -> dict[str, Role]:
        return {r.name: r for s in self.specs for r in s.roles}

    @property
    def usecases(self) -> dict[str, UseCase]:
        return {u.id: u for s in self.specs for u in s.usecases}

    @property
    def scenarios(self) -> list[Scenario]:
        return [sc for s in self.specs for sc in s.scenarios]

    @property
    def events(self) -> dict[str, list[UseCase]]:
        """事件 ID -> emits 它的 UseCase 清單。"""
        out: dict[str, list[UseCase]] = {}
        for u in self.usecases.values():
            for ev in u.emits:
                out.setdefault(ev, []).append(u)
        return out

    @property
    def screens(self) -> dict[str, Screen]:
        return {sc.id: sc for d in self.designs for sc in d.screens}

    @property
    def cr_ids(self) -> set[str]:
        return {c.id for c in self.crs}

    def spec_of_module(self, module: str) -> Optional[SpecFile]:
        for s in self.specs:
            if s.module == module:
                return s
        return None
