import os

import numpy as np
import torch.hub
from ultralytics import YOLO  # 导入Yolov8，需要提前安装ultralytics库
from flask import json, request  # 运用Python的flask类实现与前台信息的交互
from flask import Flask
from flask import send_file
from flask_cors import CORS  # 实现跨域请求
from flask import send_from_directory
from flask import Flask, request, jsonify
import traceback

import base64
import cv2
import time
import uuid  # 用于生成唯一名称

model = YOLO('./best.pt')  # 调用

app = Flask(__name__)
CORS(app)

# 用于记录当前预测输出路径
last_output_path = None

# 上传图片+ 识别
@app.route('/request', methods=['GET', 'POST'])
def uploads():
    global last_output_path

    img = request.files.get('img')  # 拿到变量img对应的图片

    # ===== 新增：为上传图片生成唯一文件名 =====
    unique_img_name = f"{uuid.uuid4()}.jpg"  # 生成唯一图片名
    # =====================================

    os.makedirs('./img', exist_ok=True)

    # ===== 修改：保存为唯一文件名而非固定 img.jpg =====
    img_path = os.path.join('./img', unique_img_name)
    img.save(img_path)
    # =====================================

    # 每次生成唯一输出目录
    unique_name = str(uuid.uuid4())
    
    # ===== 修改：传入单个图像文件路径而非目录，避免冗余预测 =====
    model.predict(img_path, save=True, device=0, name=unique_name)
    # =====================================

    # 记录输出图像路径（注意 img.jpg 是 yolov8 默认输出名）
    last_output_path = f'./runs/detect/{unique_name}/{unique_img_name}'
    
    # === 新增：保存识别记录 ===
    record = {
        'time': time.time(),
        'image_name': unique_img_name,
        'result_path': last_output_path
    }
    os.makedirs('./records', exist_ok=True)
    with open('./records/history.jsonl', 'a', encoding='utf-8') as f:
        f.write(json.dumps(record, ensure_ascii=False) + '\n')
    # ==========================

    return 'success'

# 获取识别结果
@app.route('/get', methods=['GET', 'POST'])
def download():  # 实现前端的功能，具体看前端代码
    print("working")

    # 返回当前识别图路径，而不是固定路径
    if last_output_path and os.path.exists(last_output_path):
        return send_file(last_output_path)
    else:
        return 'No result available', 404

@app.route('/history', methods=['GET'])
def get_history():
    records = []
    try:
        with open('./records/history.jsonl', 'r', encoding='utf-8') as f:
            for line in f:
                if line.strip():
                    records.append(json.loads(line.strip()))
        # 按时间倒序排序，time字段越大越靠前
        records.sort(key=lambda x: x['time'], reverse=True)
    except Exception as e:
        print("读取历史记录异常:", e)
        traceback.print_exc()
        return {'status': 'error', 'msg': str(e)}, 500
    
    return {'status': 'ok', 'data': records}

# 允许访问runs/detect文件夹下的图片
@app.route('/runs/detect/<path:filename>')
def serve_detected_image(filename):
    return send_from_directory('./runs/detect', filename)


# 按间距中的绿色按钮以运行脚本。
if __name__ == '__main__':
    model = YOLO('./best.pt')  # 直接调用训练好的模型
    app.run(host='localhost', port=8080, debug=True)  # 本机上运行
    #  app.run(host='192.168.225.149',debug=True)#局域网实现，
    # 需要确保手机和电脑在同一局域网内
