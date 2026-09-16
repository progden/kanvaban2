"""帶行號的 YAML 載入：用 PyYAML 的 compose 取得每個節點的起始行。

回傳 (value, lines)：value 是一般 Python 物件；lines 是 dict，
key 為路徑 tuple（list 索引或 mapping key），value 為 0-based 行號（相對於 YAML 文字）。
"""
from __future__ import annotations

from typing import Any

import yaml


class YamlError(Exception):
    def __init__(self, line: int, msg: str):
        super().__init__(msg)
        self.line = line
        self.msg = msg


def load_with_lines(text: str) -> tuple[Any, dict[tuple, int]]:
    try:
        node = yaml.compose(text, Loader=yaml.SafeLoader)
    except yaml.MarkedYAMLError as e:  # pragma: no cover - 直接轉成自家例外
        line = e.problem_mark.line if e.problem_mark else 0
        raise YamlError(line, str(e.problem or e)) from e
    except yaml.YAMLError as e:
        raise YamlError(0, str(e)) from e
    lines: dict[tuple, int] = {}
    if node is None:
        return None, lines
    value = _convert(node, (), lines)
    return value, lines


def _convert(node: yaml.Node, path: tuple, lines: dict[tuple, int]) -> Any:
    lines[path] = node.start_mark.line
    if isinstance(node, yaml.MappingNode):
        out: dict = {}
        for k_node, v_node in node.value:
            key = _scalar(k_node)
            lines[path + (key,)] = k_node.start_mark.line
            out[key] = _convert(v_node, path + (key,), lines)
        return out
    if isinstance(node, yaml.SequenceNode):
        return [_convert(child, path + (i,), lines) for i, child in enumerate(node.value)]
    return _scalar(node)


def _scalar(node: yaml.Node) -> Any:
    if not isinstance(node, yaml.ScalarNode):
        return None
    # 交給 SafeLoader 的 constructor 做型別轉換（數字、布林、null）
    loader = yaml.SafeLoader("")
    try:
        return loader.construct_object(node, deep=True)
    finally:
        loader.dispose()
