"""
DeepFace emotion recognition microservice API tests.

Covers /health and /analyze happy paths, parameter validation
(missing / invalid base64 / too-small image), error fallback (model
failure -> 500), and the numpy-float serialization regression.
"""
import base64

import numpy as np

import app


def _img_b64(n=200):
    """Build a >= 100-byte fake image in base64. DeepFace is mocked, content is irrelevant."""
    return base64.b64encode(b"\xff\xd8" + b"\x00" * n).decode()


def test_health_ok(client):
    resp = client.get("/health")

    assert resp.status_code == 200
    assert resp.get_json()["status"] == "ok"


def test_analyze_missing_image_returns_400(client):
    resp = client.post("/analyze", json={})

    assert resp.status_code == 400
    assert "image" in resp.get_json()["error"]


def test_analyze_invalid_base64_returns_400(client):
    resp = client.post("/analyze", json={"image": "!!!not-base64!!!"})

    assert resp.status_code == 400


def test_analyze_tiny_image_returns_none(client):
    tiny = base64.b64encode(b"\x01\x02").decode()
    resp = client.post("/analyze", json={"image": tiny})

    assert resp.status_code == 200
    assert resp.get_json()["emotion"] == "none"


def test_analyze_success_maps_emotion_and_converts_numpy_floats(client, monkeypatch):
    def fake_analyze(img_path, actions, enforce_detection, silent):
        # DeepFace returns numpy.float32, which jsonify cannot serialize directly.
        return [{
            "dominant_emotion": "happy",
            "emotion": {"happy": np.float32(95), "neutral": np.float32(5)},
        }]

    monkeypatch.setattr(app.DeepFace, "analyze", staticmethod(fake_analyze))

    resp = client.post("/analyze", json={"image": _img_b64()})

    assert resp.status_code == 200
    body = resp.get_json()
    assert body["emotion"] == "happy"
    assert body["label"] == "自信"
    assert body["confidence"] == 0.95
    assert isinstance(body["confidence"], float)
    assert body["details"]["happy"] == 0.95


def test_analyze_model_error_returns_500(client, monkeypatch):
    def fake_analyze(*args, **kwargs):
        raise RuntimeError("model crash")

    monkeypatch.setattr(app.DeepFace, "analyze", staticmethod(fake_analyze))

    resp = client.post("/analyze", json={"image": _img_b64()})

    assert resp.status_code == 500
    assert "分析失败" in resp.get_json()["error"]
