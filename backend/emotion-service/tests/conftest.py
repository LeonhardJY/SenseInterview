"""
pytest 公共配置：

1. 让 tests/ 能 import 到上一层的 app.py
2. 在不安装 TensorFlow / DeepFace 的前提下 stub 掉 deepface 模块，
   使测试可在离线环境运行，模型调用统一在用例中 mock
"""
import sys
import types
from pathlib import Path

import pytest

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

_deepface = types.ModuleType("deepface")


class _FakeDeepFace:
    @staticmethod
    def build_model(*args, **kwargs):
        return object()

    @staticmethod
    def analyze(*args, **kwargs):
        raise NotImplementedError("请在测试用例中 monkeypatch app.DeepFace.analyze")


_deepface.DeepFace = _FakeDeepFace
sys.modules.setdefault("deepface", _deepface)


@pytest.fixture()
def client():
    import app

    app.app.config["TESTING"] = True
    return app.app.test_client()
