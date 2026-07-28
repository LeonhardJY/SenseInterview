"""
DeepFace 模型权重下载助手

用法: python download_model.py
需要联网（国内需开 VPN），下载约 500MB 到 ~/.deepface/weights/
"""

import os
import urllib.request
import hashlib
import sys
from pathlib import Path

MODEL_URL = "https://github.com/serengil/deepface_models/releases/download/v1.0/vgg_face_weights.h5"
MODEL_DIR = Path.home() / ".deepface" / "weights"
MODEL_PATH = MODEL_DIR / "vgg_face_weights.h5"
MODEL_SIZE_MB = 524  # ~500MB

# 可选镜像（如果 GitHub 连不上）
MIRROR_URLS = [
    MODEL_URL,
    "https://huggingface.co/serengil/deepface_weights/resolve/main/vgg_face_weights.h5",
]


def download_file(url, dest, expected_mb=None):
    """下载文件并显示进度"""
    req = urllib.request.Request(url, headers={
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
    })

    try:
        with urllib.request.urlopen(req, timeout=30) as resp:
            total = int(resp.headers.get("Content-Length", 0))
            downloaded = 0
            chunk_size = 8192

            with open(dest, "wb") as f:
                while True:
                    chunk = resp.read(chunk_size)
                    if not chunk:
                        break
                    f.write(chunk)
                    downloaded += len(chunk)
                    if total > 0:
                        pct = downloaded * 100 // total
                        mb = downloaded / 1024 / 1024
                        print(f"\r  下载中... {pct}% ({mb:.0f}MB)", end="", flush=True)
            print()
            return True
    except Exception as e:
        print(f"  失败: {e}")
        return False


def main():
    print("=" * 50)
    print("  DeepFace VGG-Face 模型下载")
    print("=" * 50)
    print()

    MODEL_DIR.mkdir(parents=True, exist_ok=True)

    if MODEL_PATH.exists():
        size_mb = MODEL_PATH.stat().st_size / 1024 / 1024
        if size_mb > 500:
            print(f"✅ 模型已存在 ({size_mb:.0f}MB)")
            return
        else:
            print(f"⚠️  模型文件不完整 ({size_mb:.0f}MB)，重新下载")

    print(f"模型大小: ~{MODEL_SIZE_MB}MB")
    print(f"保存路径: {MODEL_PATH}")
    print()
    print("正在下载，国内用户请开 VPN...")
    print()

    for url in MIRROR_URLS:
        print(f"尝试源: {url}")
        if download_file(url, MODEL_PATH):
            size_mb = MODEL_PATH.stat().st_size / 1024 / 1024
            if size_mb > 500:
                print(f"\n✅ 下载完成！({size_mb:.0f}MB)")
                print(f"   模型已保存到: {MODEL_PATH}")
                return
        print()

    print("\n❌ 所有下载源均失败")
    print(f"   请手动下载后放到: {MODEL_PATH}")
    print(f"   下载地址: {MODEL_URL}")


if __name__ == "__main__":
    main()
