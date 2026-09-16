"""腳本的端對端測試：good fixture 應無 error；bad fixture 應報出預期的檢查 ID。

執行：`python3 -m unittest discover -s scripts/tests`（在 repo 根目錄）
"""
from __future__ import annotations

import os
import shutil
import subprocess
import sys
import tempfile
import unittest

HERE = os.path.dirname(os.path.abspath(__file__))
SCRIPTS = os.path.dirname(HERE)
sys.path.insert(0, SCRIPTS)

from speccheck import checks  # noqa: E402
from speccheck.runner import build_model, main_cr, main_ui, main_spec  # noqa: E402

GOOD = os.path.join(HERE, "fixtures", "good")
BAD = os.path.join(HERE, "fixtures", "bad")


def run_checks(root: str, script: str):
    specs = sorted(_find(root, "spec-"))
    designs = sorted(_find(root, "ui-"))
    model = build_model(specs, designs, os.path.join(root, ".dev", "CR.md"))
    return checks.run(model, script)


def _find(root: str, prefix: str):
    for dp, _, fns in os.walk(root):
        for fn in fns:
            if fn.startswith(prefix) and fn.endswith(".md"):
                yield os.path.join(dp, fn)


class GoodFixture(unittest.TestCase):
    def test_spec_check_no_errors(self):
        fs = run_checks(GOOD, "spec-check")
        errors = [f.format() for f in fs if f.level == "error"]
        self.assertEqual(errors, [])
        # 預期的 warn：board／card 沒有 C（UC-13）
        self.assertEqual({f.check for f in fs}, {"UC-13"})

    def test_design_check_no_errors(self):
        fs = run_checks(GOOD, "ui-check")
        self.assertEqual([f.format() for f in fs if f.level == "error"], [])
        self.assertEqual([f.format() for f in fs], [])

    def test_report_runs(self):
        from speccheck.report import design_report, spec_report

        specs = sorted(_find(GOOD, "spec-"))
        designs = sorted(_find(GOOD, "ui-"))
        model = build_model(specs, designs, os.path.join(GOOD, ".dev", "CR.md"))
        text = spec_report(model)
        self.assertIn("uc-delete-swimlane", text)
        self.assertIn("s-swimlane-delete-dialog", text)
        self.assertIn("s-swimlane-list", design_report(model))


class BadFixture(unittest.TestCase):
    def test_expected_error_ids(self):
        fs = run_checks(BAD, "spec-check")
        got = {f.check for f in fs}
        expected = {
            "REF-01",  # `BoardClock.now()`、`swimlane` 在 gherkin 之外…
            "REF-03",  # 角色「路人」
            "REF-04",  # stage.name
            "REF-05",  # 關係 card
            "REF-06",  # CR-099、CR-024 未登記（CR.md 不存在）
            "REF-09",  # Card
            "UC-01",  # pre key 不連號
            "UC-02",  # fail.p2
            "UC-03",  # post 無 ID
            "UC-04",  # @fail-p9
            "UC-05",  # fail.p2 沒有 Scenario
            "UC-06",  # uc-rename-swimlane 沒有成功 Scenario、roles 空
            "UC-08",  # ev-swimlane-deleted 兩個 emits
            "UC-12",  # calls-sync 循環
            "GH-02",  # 第二個 Scenario 沒有註解
            "GH-03",  # 缺「以便」
            "GH-04",  # tag 順序、@changed 無 @deprecated
            "GH-06",  # crud swimlane: D 但註解只 read（成功 Scenario）
            "GH-09",  # 步驟含反引號
        }
        missing = expected - got
        self.assertEqual(missing, set(), f"未報出：{missing}\n" + "\n".join(f.format() for f in fs))


class CrCheck(unittest.TestCase):
    """在暫存 git repo 裡：base = good fixture，head = 改了 usecase 區塊但 CR 影響 ID 沒列。"""

    def setUp(self):
        self.tmp = tempfile.mkdtemp(prefix="cr-check-test-")
        shutil.copytree(GOOD, self.tmp, dirs_exist_ok=True)
        self._git("init", "-q", "-b", "main")
        self._git("config", "user.email", "t@example.com")
        self._git("config", "user.name", "t")
        self._git("add", ".")
        self._git("commit", "-q", "-m", "base")

    def tearDown(self):
        shutil.rmtree(self.tmp, ignore_errors=True)

    def _git(self, *a):
        subprocess.run(["git", "-C", self.tmp, *a], check=True, capture_output=True)

    def _run(self, *extra):
        cwd = os.getcwd()
        os.chdir(self.tmp)
        try:
            from io import StringIO
            from contextlib import redirect_stdout

            buf = StringIO()
            with redirect_stdout(buf):
                code = main_cr(["--base", "HEAD", "--cr-file", ".dev/CR.md", *extra])
            return code, buf.getvalue()
        finally:
            os.chdir(cwd)

    def test_clean_diff_passes(self):
        code, out = self._run("--cr", "CR-024")
        # CR-024 列了 swimlane、card、uc-delete-swimlane、s-swimlane-delete-dialog 但 diff 沒動 → CR-01 少改
        self.assertEqual(code, 1)
        self.assertIn("CR-01", out)
        self.assertIn("CR 列了但未改", out)

    def test_scenario_change_without_cr(self):
        path = os.path.join(self.tmp, ".dev/F01-basic-kanban/spec-kanban-basic.md")
        with open(path, encoding="utf-8") as f:
            s = f.read()
        s = s.replace("Then 該操作應該被記錄為一筆活動紀錄，包含操作人與操作時間", "Then 該操作應該被記錄為一筆活動紀錄")
        with open(path, "w", encoding="utf-8") as f:
            f.write(s)
        code, out = self._run()
        self.assertEqual(code, 1)
        self.assertIn("GH-05", out)  # 已進入開發、被 diff 動到、沒有 @CR-


class Registry(unittest.TestCase):
    def test_all_ids_in_checks_md(self):
        """checks.md 的每個 ID 都有登記，登記的每個 ID 都在 checks.md。"""
        import re

        doc = os.path.join(SCRIPTS, "..", ".dev", "conventions", "checks.md")
        with open(doc, encoding="utf-8") as f:
            ids_in_doc = set(re.findall(r"^\| ((?:REF|UC|GH|DS|CR)-\d\d) \|", f.read(), re.M))
        self.assertEqual(ids_in_doc, set(checks.REGISTRY))


if __name__ == "__main__":
    unittest.main()
