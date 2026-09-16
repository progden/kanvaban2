"""解析 `spec-<模組>.md`（scripts.md §2.1）。"""
from __future__ import annotations

import re
from typing import Optional

from . import md
from .ids import kind_of, split_attr
from .model import (
    Attribute,
    ChangeLogRow,
    Entity,
    Feature,
    Loc,
    Ref,
    Relation,
    Role,
    Scenario,
    Sentence,
    SpecFile,
    Step,
    UseCase,
)
from .yamlloc import YamlError, load_with_lines

H2_FIXED = ["名詞定義", "角色定義", "Aggregate 標記說明", "變更紀錄", "待釐清"]
H3_TERMS = ["實體", "欄位", "關係", "其他名詞"]
STATUS_RE = re.compile(r"^狀態[：:]\s*(.+?)\s*$")
STEP_RE = re.compile(r"^(Given|When|Then|And|But|\*)\s+(.*)$")
QUOTED_RE = re.compile(r'"([^"]*)"')
AGG_HEADER = "# Related aggregate:"
AGG_LINE_RE = re.compile(r"^#\s*([^:\s]+)\s*:\s*(.+?)\s*$")
UC_REQUIRED = ["id", "name", "roles", "crud", "pre", "post"]
UC_OPTIONAL = ["fail", "emits", "requires", "calls-sync"]
FAIL_FIXED_SENTENCE = "拒絕，資料不變"


def parse_spec(path: str) -> SpecFile:
    lines = md.read_lines(path)
    fences = md.find_fences(lines)
    fmap = md.in_fence_map(lines, fences)
    headings = md.find_headings(lines, fmap)
    spec = SpecFile(path=path, module=md.module_of(path, "spec"))

    h2s = [h for h in headings if h.level == 2]
    first_h2 = h2s[0].index if h2s else len(lines)

    # 「狀態：草稿／開發中」行（第一個 H2 之前，必填）
    for i in range(first_h2):
        if fmap[i] is None:
            m = STATUS_RE.match(lines[i].strip())
            if m:
                spec.status = m.group(1)
                if spec.status not in ("草稿", "開發中"):
                    spec.structure_errors.append((i + 1, f"「狀態：」的值必須是「草稿」或「開發中」，實際「{spec.status}」"))
                break
    if spec.status is None:
        spec.structure_errors.append((1, "第一個 H2 之前缺少「狀態：草稿」或「狀態：開發中」這一行"))

    counts = {k: 0 for k in H2_FIXED}
    feature_count = 0
    for n, h in enumerate(h2s):
        end = h2s[n + 1].index if n + 1 < len(h2s) else len(lines)
        text = h.text
        if md.heading_matches(text, "Feature:"):
            feature_count += 1
            _parse_feature(spec, lines, fences, fmap, headings, h, end)
            continue
        matched = None
        for fixed in H2_FIXED:
            if md.heading_matches(text, fixed):
                matched = fixed
                break
        if matched is None:
            continue  # 額外的 H2 不擋
        counts[matched] += 1
        if matched == "名詞定義":
            _parse_terms(spec, lines, fmap, headings, h.index, end)
        elif matched == "角色定義":
            _parse_roles(spec, lines, fmap, h.index)
        elif matched == "變更紀錄":
            _parse_changelog(spec, lines, fmap, h.index)

    for fixed in ("名詞定義", "角色定義", "Aggregate 標記說明", "變更紀錄"):
        if counts[fixed] != 1:
            spec.structure_errors.append((1, f"「## {fixed}」應恰好出現一次，實際 {counts[fixed]} 次"))
    if counts["待釐清"] > 1:
        spec.structure_errors.append((1, f"「## 待釐清」至多一次，實際 {counts['待釐清']} 次"))
    if feature_count == 0:
        spec.structure_errors.append((1, "沒有任何「## Feature:」區段"))

    # 全文反引號（程式碼圍欄以外）
    for i, line in md.iter_prose_lines(lines, fmap):
        for raw in md.backticks(line):
            spec.refs.append(Ref(id=raw.strip(), kind=kind_of(raw), loc=Loc(path, i + 1), context="正文"))
    return spec


# ---------------------------------------------------------------- 名詞定義


def _parse_terms(spec: SpecFile, lines, fmap, headings, start: int, end: int) -> None:
    h3s = [h for h in headings if h.level == 3 and start < h.index < end]
    seen: dict[str, int] = {}
    for h in h3s:
        for term in H3_TERMS:
            if md.heading_matches(h.text, term):
                seen[term] = seen.get(term, 0) + 1
                if term == "實體":
                    _parse_entities(spec, lines, fmap, h.index)
                elif term == "欄位":
                    _parse_attributes(spec, lines, fmap, h.index)
                elif term == "關係":
                    _parse_relations(spec, lines, fmap, h.index)
    for term in ("實體", "欄位", "關係"):
        if seen.get(term, 0) != 1:
            spec.structure_errors.append((start + 1, f"「### {term}」應在名詞定義下恰好出現一次，實際 {seen.get(term, 0)} 次"))


def _cell(cells: list[str], i: int) -> str:
    return cells[i] if i < len(cells) else ""


def _parse_entities(spec, lines, fmap, idx):
    t = md.parse_table_at(lines, idx + 1, fmap)
    if not t:
        spec.structure_errors.append((idx + 1, "「### 實體」下沒有表格"))
        return
    agg_idx = t.header.index("所屬 Aggregate") if "所屬 Aggregate" in t.header else None
    desc_idx = t.header.index("說明") if "說明" in t.header else 2
    for cells, li in t.rows:
        eid = _cell(cells, 0)
        if not eid:
            continue
        spec.entities.append(
            Entity(
                id=eid,
                name=_cell(cells, 1),
                desc=_cell(cells, desc_idx),
                loc=Loc(spec.path, li + 1),
                aggregate=_cell(cells, agg_idx) if agg_idx is not None else "",
            )
        )
        spec.entity_loc_range[eid] = (li + 1, li + 1)


def _parse_attributes(spec, lines, fmap, idx):
    t = md.parse_table_at(lines, idx + 1, fmap)
    if not t:
        spec.structure_errors.append((idx + 1, "「### 欄位」下沒有表格"))
        return
    for cells, li in t.rows:
        aid = _cell(cells, 0)
        if not aid:
            continue
        ent, attr = split_attr(aid)
        spec.attributes.append(
            Attribute(
                id=aid,
                entity=ent,
                attr=attr,
                type=_cell(cells, 1),
                constraint=_cell(cells, 2),
                desc=_cell(cells, 3),
                loc=Loc(spec.path, li + 1),
            )
        )


def _parse_relations(spec, lines, fmap, idx):
    t = md.parse_table_at(lines, idx + 1, fmap)
    if not t:
        spec.structure_errors.append((idx + 1, "「### 關係」下沒有表格"))
        return
    for cells, li in t.rows:
        if not _cell(cells, 0):
            continue
        spec.relations.append(
            Relation(
                source=_cell(cells, 0),
                target=_cell(cells, 1),
                min=_cell(cells, 2),
                max=_cell(cells, 3),
                desc=_cell(cells, 4),
                loc=Loc(spec.path, li + 1),
            )
        )


def _parse_roles(spec, lines, fmap, idx):
    t = md.parse_table_at(lines, idx + 1, fmap)
    if not t:
        spec.structure_errors.append((idx + 1, "「## 角色定義」下沒有表格"))
        return
    for cells, li in t.rows:
        rid = _cell(cells, 0)
        if not rid:
            continue
        spec.roles.append(Role(id=rid, name=_cell(cells, 1), desc=_cell(cells, 2), loc=Loc(spec.path, li + 1)))


def _parse_changelog(spec, lines, fmap, idx):
    t = md.parse_table_at(lines, idx + 1, fmap)
    if not t:
        return  # 新模組可先留空
    for cells, li in t.rows:
        if not any(cells):
            continue
        spec.changelog.append(
            ChangeLogRow(date=_cell(cells, 0), cr=_cell(cells, 1), type=_cell(cells, 2), summary=_cell(cells, 3), loc=Loc(spec.path, li + 1))
        )


# ---------------------------------------------------------------- Feature


def _parse_feature(spec: SpecFile, lines, fences, fmap, headings, h, end: int) -> None:
    name = h.text.split(":", 1)[1].strip() if ":" in h.text else ""
    feat = Feature(name=name, module=spec.module, loc=Loc(spec.path, h.index + 1), end_line=end)
    spec.features.append(feat)

    feat.usecase_section_count = sum(
        1 for x in headings if x.level == 3 and h.index < x.index < end and md.heading_matches(x.text, "Use Case 定義")
    )
    uc_fences = [f for f in fences if f.lang == "usecase" and h.index < f.start < end]
    gh_fences = [f for f in fences if f.lang == "gherkin" and h.index < f.start < end]
    feat.usecase_block_count = len(uc_fences)
    if uc_fences and gh_fences:
        feat.usecase_before_gherkin = uc_fences[0].start < gh_fences[0].start
    if uc_fences:
        feat.usecase_block_loc = Loc(spec.path, uc_fences[0].start + 1)
        _parse_usecases(spec, feat, lines, uc_fences[0])
    if gh_fences:
        feat.gherkin_loc = Loc(spec.path, gh_fences[0].start + 1)
        _parse_gherkin(spec, feat, lines, gh_fences[0])


def _parse_usecases(spec: SpecFile, feat: Feature, lines, fence: md.Fence) -> None:
    body = lines[fence.body_start : fence.body_end]
    text = "\n".join(body)
    base = fence.body_start  # YAML 第 0 行對應的檔案 index

    def L(path: tuple, fallback: int = 0) -> int:
        return base + ylines.get(path, fallback) + 1

    try:
        value, ylines = load_with_lines(text)
    except YamlError as e:
        feat.usecase_errors.append((base + e.line + 1, f"YAML 解析失敗：{e.msg}"))
        return
    if value is None:
        value = []
    if not isinstance(value, list):
        feat.usecase_errors.append((base + 1, "頂層必須是 list"))
        return

    for i, item in enumerate(value):
        item_line = L((i,))
        next_line = L((i + 1,)) - 1 if i + 1 < len(value) else fence.end
        if not isinstance(item, dict):
            feat.usecase_errors.append((item_line, f"第 {i + 1} 項不是 mapping"))
            continue
        errs: list[tuple[int, str]] = []
        uid = item.get("id")
        label = uid if isinstance(uid, str) else f"第 {i + 1} 項"
        for k in UC_REQUIRED:
            if k not in item:
                errs.append((item_line, f"{label} 缺少必填欄位 {k}"))
        for k in UC_OPTIONAL:
            if k not in item:
                errs.append((item_line, f"{label} 缺少欄位 {k}（可空但必須存在）"))
        for k in item:
            if k not in UC_REQUIRED and k not in UC_OPTIONAL:
                errs.append((L((i, k), 0), f"{label} 有多餘欄位 {k}"))

        if not isinstance(uid, str) or kind_of(uid) != "usecase":
            errs.append((item_line, f"{label} 的 id 必須是 uc- 前綴的合法 ID"))
        nm = item.get("name")
        if not isinstance(nm, str) or not nm.strip():
            errs.append((item_line, f"{label} 的 name 必須是非空字串"))

        roles = _str_list(item.get("roles"), errs, L((i, "roles"), 0), f"{label} 的 roles")
        emits = _str_list(item.get("emits"), errs, L((i, "emits"), 0), f"{label} 的 emits")
        requires = _str_list(item.get("requires"), errs, L((i, "requires"), 0), f"{label} 的 requires")
        calls = _str_list(item.get("calls-sync"), errs, L((i, "calls-sync"), 0), f"{label} 的 calls-sync")

        crud: dict[str, str] = {}
        raw_crud = item.get("crud")
        if raw_crud is None:
            raw_crud = {}
        if not isinstance(raw_crud, dict):
            errs.append((L((i, "crud"), 0), f"{label} 的 crud 必須是 mapping"))
        else:
            for k, v in raw_crud.items():
                v = "" if v is None else str(v)
                if not v or any(ch not in "CRUD" for ch in v) or len(set(v)) != len(v):
                    errs.append((L((i, "crud", k), 0), f"{label} 的 crud.{k} 值 \"{v}\" 必須是 C/R/U/D 不重複組合"))
                crud[str(k)] = v

        pre: dict[str, Sentence] = {}
        raw_pre = item.get("pre")
        if raw_pre is None:
            raw_pre = {}
        if not isinstance(raw_pre, dict):
            errs.append((L((i, "pre"), 0), f"{label} 的 pre 必須是 mapping"))
        else:
            expected = [f"p{n}" for n in range(1, len(raw_pre) + 1)]
            if list(raw_pre.keys()) != expected:
                errs.append((L((i, "pre"), 0), f"{label} 的 pre key 必須是 p1、p2… 連號，實際 {list(raw_pre.keys())}"))
            for k, v in raw_pre.items():
                pre[str(k)] = _sentence(spec, v, L((i, "pre", k), 0))

        post: list[Sentence] = []
        raw_post = item.get("post")
        if raw_post is None:
            raw_post = []
        if not isinstance(raw_post, list):
            errs.append((L((i, "post"), 0), f"{label} 的 post 必須是 list"))
        else:
            for j, v in enumerate(raw_post):
                post.append(_sentence(spec, v, L((i, "post", j), 0)))

        fail: dict[str, Sentence] = {}
        raw_fail = item.get("fail")
        if raw_fail is None:
            raw_fail = {}
        if not isinstance(raw_fail, dict):
            errs.append((L((i, "fail"), 0), f"{label} 的 fail 必須是 mapping"))
        else:
            for k, v in raw_fail.items():
                fail[str(k)] = _sentence(spec, v, L((i, "fail", k), 0))

        feat.usecase_errors.extend(errs)
        if not isinstance(uid, str) or kind_of(uid) != "usecase":
            continue  # 沒有合法 id 就不建物件
        uc = UseCase(
            id=uid,
            name=str(nm) if nm else "",
            feature=feat.name,
            roles=roles,
            crud=crud,
            pre=pre,
            post=post,
            fail=fail,
            emits=emits,
            requires=requires,
            calls_sync=calls,
            loc=Loc(spec.path, item_line),
            end_line=next_line,
        )
        feat.usecases.append(uc)
        # usecase 句子裡的 ID 也算引用
        for field_name, sents in (("pre", pre.values()), ("post", post), ("fail", fail.values())):
            for s in sents:
                for rid in s.ids:
                    spec.refs.append(Ref(id=rid, kind=kind_of(rid), loc=s.loc, context=f"usecase {field_name}"))
        for field_name, idlist in (("roles", roles), ("emits", emits), ("requires", requires), ("calls-sync", calls)):
            for rid in idlist:
                spec.refs.append(Ref(id=rid, kind=kind_of(rid), loc=Loc(spec.path, L((i, field_name), 0)), context=f"usecase {field_name}"))


def _str_list(v, errs, line, label) -> list[str]:
    if v is None:
        return []
    if not isinstance(v, list):
        errs.append((line, f"{label} 必須是 list"))
        return []
    out = []
    for x in v:
        if not isinstance(x, str):
            errs.append((line, f"{label} 的元素必須是字串"))
        else:
            out.append(x)
    return out


def _sentence(spec: SpecFile, v, line: int) -> Sentence:
    text = "" if v is None else str(v)
    return Sentence(text=text, ids=[x.strip() for x in md.backticks(text)], loc=Loc(spec.path, line))


# ---------------------------------------------------------------- Gherkin


def _parse_gherkin(spec: SpecFile, feat: Feature, lines, fence: md.Fence) -> None:
    body_idx = range(fence.body_start, fence.body_end)
    it = iter(body_idx)
    # Feature 標頭
    header_expect = ["身為", "我想要", "以便"]
    pending_tags: list[str] = []
    tag_loc: Optional[Loc] = None
    pending_comments: list[tuple[int, str]] = []
    comment_after_tag = True
    current: Optional[Scenario] = None
    last_primary = ""
    in_header = False
    header_seen: list[str] = []

    def finish(idx_end: int):
        nonlocal current
        if current is not None:
            current.end_line = idx_end
            current = None

    for i in it:
        raw = lines[i]
        s = raw.strip()
        if not s:
            continue
        if s.startswith("Feature:"):
            feat.gherkin_name = s[len("Feature:"):].strip()
            in_header = True
            header_seen = []
            continue
        if in_header:
            matched = False
            for k in header_expect:
                if s.startswith(k) and k not in header_seen:
                    header_seen.append(k)
                    matched = True
                    if k == "身為":
                        feat.role_name = s[len(k):].strip()
                    break
            if matched and len(header_seen) < 3:
                continue
            in_header = False
            feat.header_missing = [k for k in header_expect if k not in header_seen]
            if matched:
                continue
        if s.startswith("@"):
            if pending_comments:
                comment_after_tag = False
            if tag_loc is None:
                tag_loc = Loc(spec.path, i + 1)
            pending_tags.extend(s.split())
            continue
        if s.startswith("#"):
            pending_comments.append((i, s))
            continue
        if s.startswith("Background:"):
            finish(i)
            pending_tags, tag_loc, pending_comments, comment_after_tag = [], None, [], True
            continue
        if s.startswith("Scenario:") or s.startswith("Scenario Outline:"):
            finish(i)
            name = s.split(":", 1)[1].strip()
            aggs, agg_loc, agg_errs = _parse_aggregates(spec, pending_comments)
            current = Scenario(
                name=name,
                feature=feat.name,
                tags=pending_tags,
                tag_loc=tag_loc,
                aggregates=aggs,
                aggregate_loc=agg_loc,
                aggregate_after_tags=comment_after_tag,
                steps=[],
                loc=Loc(spec.path, i + 1),
                aggregate_errors=agg_errs,
            )
            feat.scenarios.append(current)
            pending_tags, tag_loc, pending_comments, comment_after_tag = [], None, [], True
            last_primary = ""
            continue
        m = STEP_RE.match(s)
        if m and current is not None:
            kw, text = m.group(1), m.group(2)
            if kw in ("Given", "When", "Then"):
                last_primary = kw
            current.steps.append(
                Step(keyword=kw, text=text, quoted=QUOTED_RE.findall(text), loc=Loc(spec.path, i + 1), is_then=(last_primary == "Then"))
            )
            continue
        # 資料表列、Examples 等：略過
    finish(fence.end)


def _parse_aggregates(spec: SpecFile, comments: list[tuple[int, str]]):
    aggs: dict[str, set[str]] = {}
    errs: list[str] = []
    loc: Optional[Loc] = None
    for i, s in comments:
        if s.startswith(AGG_HEADER):
            loc = Loc(spec.path, i + 1)
            continue
        if loc is None:
            continue
        m = AGG_LINE_RE.match(s)
        if not m:
            errs.append(f"無法解析的註解行「{s}」")
            continue
        name, modes = m.group(1), [x.strip() for x in m.group(2).split(",")]
        bad = [x for x in modes if x not in ("read", "write")]
        if bad:
            errs.append(f"{name} 的存取方式 {bad} 不是 read/write")
        aggs[name] = {x for x in modes if x in ("read", "write")}
    return aggs, loc, errs
