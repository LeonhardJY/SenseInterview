"""
DeepFace 表情识别微服务

启动: python app.py (首次会下载 ~500MB 模型权重，需联网)
API:  POST /analyze  →  { "emotion":"happy", "confidence":0.95, "details":{...} }
      GET  /health   →  { "status":"ok", "model":"DeepFace", "loaded":true }

支持情绪: happy(自信) / neutral(平静) / sad(低落) / angry(紧张)
          fear(紧张) / surprise(惊讶) / disgust(不适)
"""

import sys, os, base64, tempfile, time
import logging

# 抑制 TensorFlow/Aten 的冗长日志
os.environ["TF_CPP_MIN_LOG_LEVEL"] = "3"
os.environ["TF_ENABLE_ONEDNN_OPTS"] = "0"
os.environ["KMP_WARNINGS"] = "off"

import numpy as np
from flask import Flask, request, jsonify
from deepface import DeepFace

app = Flask(__name__)
logging.basicConfig(level=logging.INFO, format="[DeepFace] %(message)s")
log = logging.getLogger("deepface")

# 情绪 → 中文标签
EMOTION_LABELS = {
    "happy": "自信", "neutral": "平静", "surprise": "惊讶",
    "sad": "低落", "angry": "紧张", "fear": "紧张", "disgust": "不适"
}


def preload_model():
    """启动时预下载模型权重（避免第一次请求卡住）"""
    log.info("正在下载 VGG-Face 模型权重（约 500MB，只需下载一次）...")
    log.info("如果卡住不动，请开 VPN 后重试")
    try:
        # build_model 会下载并缓存模型到 ~/.deepface/weights/
        DeepFace.build_model("VGG-Face")
        log.info("模型加载完成 ✓")
        return True
    except Exception as e:
        log.error(f"模型加载失败: {e}")
        return False


@app.route("/health", methods=["GET"])
def health():
    return jsonify({
        "status": "ok",
        "model": "DeepFace",
        "loaded": hasattr(DeepFace, "build_model")
    })


@app.route("/analyze", methods=["POST"])
def analyze():
    """分析图片中的面部情绪"""
    data = request.get_json()
    if not data or "image" not in data:
        return jsonify({"error": "缺少 image 字段"}), 400

    # 解码
    try:
        image_data = base64.b64decode(data["image"])
    except Exception:
        return jsonify({"error": "图片不是有效的 Base64"}), 400

    # 图片太小没必要分析
    if len(image_data) < 100:
        return jsonify({"emotion": "none", "confidence": 0, "details": {}})

    tmp_path = None
    try:
        with tempfile.NamedTemporaryFile(suffix=".jpg", delete=False) as f:
            f.write(image_data)
            tmp_path = f.name

        result = DeepFace.analyze(
            img_path=tmp_path,
            actions=["emotion"],
            enforce_detection=False,
            silent=True
        )

        if isinstance(result, list) and len(result) > 0:
            r = result[0]
            emotion = r.get("dominant_emotion", "unknown")
            emotions = r.get("emotion", {})
            confidence = emotions.get(emotion, 0.0) / 100.0
            label = EMOTION_LABELS.get(emotion, emotion)

            # DeepFace 返回 numpy float32，jsonify 不能序列化，需转原生 float
            return jsonify({
                "emotion": emotion,
                "label": label,
                "confidence": float(round(confidence, 4)),
                "details": {k: float(round(v / 100.0, 4)) for k, v in emotions.items()}
            })

        return jsonify({"emotion": "none", "confidence": 0, "details": {}})

    except Exception as e:
        log.error(f"分析异常: {e}")
        return jsonify({"error": f"分析失败: {str(e)}"}), 500

    finally:
        if tmp_path and os.path.exists(tmp_path):
            try: os.unlink(tmp_path)
            except: pass


if __name__ == "__main__":
    print("=" * 50)
    print("  DeepFace 表情识别服务")
    print("=" * 50)
    print()

    ok = preload_model()
    if ok:
        print("\n[OK] 服务就绪 -> http://localhost:5000")
        print("  POST /analyze  情绪分析")
        print("  GET  /health   健康检查\n")
        app.run(host="0.0.0.0", port=5000, debug=False)
    else:
        print("\n[FAIL] 模型下载失败，请手动下载:")
        model_path = str(Path.home() / ".deepface" / "weights" / "vgg_face_weights.h5")
        print(f"  1. 打开: https://github.com/serengil/deepface_models/releases/download/v1.0/vgg_face_weights.h5")
        print(f"  2. 下载后放到: {model_path}")
        sys.exit(1)
