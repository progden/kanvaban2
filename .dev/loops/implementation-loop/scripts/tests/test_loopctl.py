"""loopctl 的測試：每個案例在暫存 git repo 的 impl/<task> 分支上跑真的指令。"""

import os
import subprocess
import tempfile
import unittest
from pathlib import Path

LOOPCTL = Path(__file__).resolve().parents[1] / "loopctl"
STATE = Path(".dev/loops/implementation-loop/.state")
TASK = "T-03-be-card"

OQ_BODY = """情況：【推論＋所本原文】
spec原文：`spec-x.md` 第 1 行逐字：『看板至少保留一個 Swimlane』
推論：剛建立的看板也要有一個。
問題：預設名稱是什麼？
選項：A. 維持現狀；B. 走 CR。
"""


class LoopctlTest(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.root = Path(self.tmp.name)
        self.bodies = tempfile.TemporaryDirectory()
        self.addCleanup(self.bodies.cleanup)
        self.git("init", "-q", "-b", "loop/implementation")
        self.git("config", "user.name", "t")
        self.git("config", "user.email", "t@t")
        (self.root / STATE).mkdir(parents=True)
        (self.root / STATE / "tasks.md").write_text(
            "| ID | 產出範圍 | 依賴 | 備註 |\n|---|---|---|---|\n| T-03-be-card | x | 無 | |\n| T-04-be-board-membership | y | 無 | |\n",
            encoding="utf-8",
        )
        self.git("add", "-A")
        self.git("commit", "-qm", "init")
        self.git("switch", "-qc", f"impl/{TASK}")

    def tearDown(self):
        self.tmp.cleanup()

    def git(self, *args):
        return subprocess.run(["git", *args], cwd=self.root, check=True, capture_output=True, text=True).stdout

    def body(self, text):
        # 內文檔放在 repo 外面（agent 實際上也是放 /tmp），才不會弄髒工作區
        fd, name = tempfile.mkstemp(suffix=".md", dir=self.bodies.name)
        with os.fdopen(fd, "w", encoding="utf-8") as fh:
            fh.write(text)
        return name

    def run_ctl(self, *args, role="Dev", round_=1, max_rounds=6, task=TASK):
        env = {**os.environ, "LOOP_TASK_ID": task, "LOOP_ROLE": role, "LOOP_ROUND": str(round_), "LOOP_MAX_ROUNDS": str(max_rounds)}
        return subprocess.run([str(LOOPCTL), *args], cwd=self.root, env=env, capture_output=True, text=True)

    def ok(self, *args, **kw):
        r = self.run_ctl(*args, **kw)
        self.assertEqual(r.returncode, 0, r.stderr)
        return r.stdout.strip()

    def refused(self, needle, *args, **kw):
        r = self.run_ctl(*args, **kw)
        self.assertEqual(r.returncode, 2, r.stdout)
        self.assertIn(needle, r.stderr)

    def task_file(self, name):
        return (self.root / STATE / "tasks" / TASK / name).read_text(encoding="utf-8")

    def oq(self, blocking="no", level="高", **kw):
        return self.ok("oq", "add", "--level", level, "--blocking", blocking, "--owner", "人工",
                       "--reason-code", "spec-ambiguous", "--scope", "F01/uc-x", "--file", self.body(OQ_BODY), **kw)

    # ---- 基本流程 ----
    def test_dev_happy_path_commits_with_trailer(self):
        self.ok("log", "--file", self.body("做了 A。"))
        out = self.ok("finish", "--verdict", "review-pending", "--state", self.body("現況：交出。"))
        self.assertIn("已收尾", out)
        self.assertEqual(self.task_file("status"), "review-pending\n")
        self.assertRegex(self.task_file("rounds.log"), r"\tDev\t1\treview-pending\n$")
        self.assertIn("Dev 第 1 輪", self.task_file("decision-log.md"))
        self.assertEqual(self.git("status", "--porcelain"), "")
        self.assertIn("Loopctl: Dev r1", self.git("log", "-1", "--format=%B"))

    def test_review_bounce_then_dev_fix_then_approve(self):
        self.ok("log", "--file", self.body("x")); self.ok("finish", "--verdict", "review-pending", "--state", self.body("s"))
        fid = self.ok("fix", "add", "--file", self.body("測試 X\n沒斷言 | 訊息"), role="Review")
        self.assertEqual(fid, "D-01")
        self.assertIn("| D-01 | 1 | todo | — | 測試 X 沒斷言 ｜ 訊息 |", self.task_file("fixes.md"))
        self.ok("log", "--file", self.body("退回"), "--title", "退回", role="Review")
        self.ok("finish", "--verdict", "doing", "--state", self.body("s"), role="Review")
        # Dev 第 2 輪：沒標 done 不能交
        self.ok("log", "--file", self.body("修了"), round_=2)
        self.refused("還有 D-xx 沒處理", "finish", "--verdict", "review-pending", "--state", self.body("s"), round_=2)
        self.ok("fix", "done", "D-01", round_=2)
        self.ok("finish", "--verdict", "review-pending", "--state", self.body("s"), round_=2)
        self.ok("log", "--file", self.body("核准"), role="Review", round_=2)
        self.ok("finish", "--verdict", "done", "--state", self.body("s"), role="Review", round_=2)
        self.assertEqual(self.task_file("status"), "done\n")

    # ---- 拒絕規則 ----
    def test_wrong_branch_refused(self):
        self.git("switch", "-q", "loop/implementation")
        self.refused("不是 impl/", "show")

    def test_missing_env_refused(self):
        env = {k: v for k, v in os.environ.items() if not k.startswith("LOOP_")}
        r = subprocess.run([str(LOOPCTL), "show"], cwd=self.root, env=env, capture_output=True, text=True)
        self.assertEqual(r.returncode, 2)

    def test_dev_cannot_mark_done_or_add_fix(self):
        self.ok("log", "--file", self.body("x"))
        self.refused("只能是", "finish", "--verdict", "done", "--state", self.body("s"))
        self.refused("只有 Review", "fix", "add", "--file", self.body("x"))

    def test_finish_requires_log_and_clean_tree(self):
        self.refused("還沒有紀錄", "finish", "--verdict", "review-pending", "--state", self.body("s"))
        self.ok("log", "--file", self.body("x"))
        (self.root / "stray.txt").write_text("x")
        self.refused("沒 commit", "finish", "--verdict", "review-pending", "--state", self.body("s"))

    def test_doing_requires_new_fix_and_not_last_round(self):
        self.ok("log", "--file", self.body("x"), role="Review")
        self.refused("fix add", "finish", "--verdict", "doing", "--state", self.body("s"), role="Review")
        self.ok("fix", "add", "--file", self.body("y"), role="Review", round_=6)
        self.ok("log", "--file", self.body("x"), role="Review", round_=6)
        self.refused("最後一輪", "finish", "--verdict", "doing", "--state", self.body("s"), role="Review", round_=6)

    def test_blocked_requires_blocking_oq_and_blocks_approval(self):
        self.ok("log", "--file", self.body("x"), role="Review")
        self.refused("--blocking yes", "finish", "--verdict", "blocked", "--state", self.body("s"), role="Review")
        self.assertEqual(self.oq(blocking="yes", level="覆蓋", role="Review"), f"OQ-{TASK}-01")
        self.refused("只能 --verdict blocked", "finish", "--verdict", "done", "--state", self.body("s"), role="Review")
        self.ok("finish", "--verdict", "blocked", "--state", self.body("s"), role="Review")

    def test_same_problem_third_bounce_must_block(self):
        self.ok("fix", "add", "--file", self.body("a"), role="Review", round_=1)
        self.ok("fix", "done", "D-01", round_=2)
        self.ok("fix", "add", "--file", self.body("a 還是沒好"), "--continues", "D-01", role="Review", round_=2)
        self.ok("fix", "done", "D-02", round_=3)
        self.ok("fix", "add", "--file", self.body("a 第三次"), "--continues", "D-02", role="Review", round_=3)
        self.ok("log", "--file", self.body("x"), role="Review", round_=3)
        self.refused("第 3 次被退回", "finish", "--verdict", "doing", "--state", self.body("s"), role="Review", round_=3)

    # ---- OQ 格式 ----
    def test_oq_format_and_numbering(self):
        self.assertEqual(self.oq(), f"OQ-{TASK}-01")
        self.assertEqual(self.oq(), f"OQ-{TASK}-02")
        text = self.task_file("open-questions.md")
        for needle in ("[Level: F01/uc-x]", "- 等級：高", "- 阻塞：否", "- 接手：人工", "- 原因代碼：spec-ambiguous", "- 狀態：待處理"):
            self.assertIn(needle, text)

    def test_oq_validation(self):
        base = ["oq", "add", "--reason-code", "r", "--scope", "s"]
        self.refused("逐字引用", *base, "--level", "高", "--blocking", "no", "--owner", "無", "--file", self.body("情況：【引用原文】\n問題：x\n選項：y"))
        self.refused("四選一", *base, "--level", "高", "--blocking", "no", "--owner", "無", "--file", self.body("情況：我覺得\n『a』\n問題：x\n選項：y"))
        self.refused("一律阻塞", *base, "--level", "覆蓋", "--blocking", "no", "--owner", "無", "--file", self.body(OQ_BODY))
        self.refused("不是任務清單上", *base, "--level", "高", "--blocking", "no", "--owner", "T-99-x", "--file", self.body(OQ_BODY))
        self.refused("分開寫", *base, "--level", "高", "--blocking", "no", "--owner", "無",
                     "--file", self.body("情況：【推論＋所本原文】\n『a』\n問題：x\n選項：y"))
        self.ok(*base, "--level", "高", "--blocking", "no", "--owner", "T-04-be-board-membership", "--file", self.body(OQ_BODY))

    def test_state_too_long_refused(self):
        self.ok("log", "--file", self.body("x"))
        self.refused("超過", "finish", "--verdict", "review-pending", "--state", self.body("\n".join(["l"] * 21)))


if __name__ == "__main__":
    unittest.main()
