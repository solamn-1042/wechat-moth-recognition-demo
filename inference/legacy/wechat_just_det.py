import os

import numpy as np
import torch.hub
from ultralytics import YOLO  # 导入Yolov11，需要提前安装ultralytics库
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

@app.route('/request', methods=['POST'])
def flask_request():
    img = request.files.get('img')
    unique_img_name = f"{uuid.uuid4()}.jpg"
    os.makedirs('./img', exist_ok=True)
    img_path = os.path.join('./img', unique_img_name)
    img.save(img_path)

    unique_name = str(uuid.uuid4())
    
    # 执行预测并获取结果
    results = model.predict(img_path, save=True, device=0, name=unique_name)
    
    # 获取识别结果（第一个结果）
    result = results[0]
    
    # 提取识别信息
    detected_classes = []
    confidences = []
    
    # 如果有检测到对象
    if len(result.boxes) > 0:
        # 获取识别的类别
        cls_indices = result.boxes.cls.cpu().numpy()
        # 获取置信度
        conf_scores = result.boxes.conf.cpu().numpy()
        
        # 将数字类别索引转换为类别名称
        class_name_mapping = {
            "Spodoptera frugiperda": "草地贪夜蛾",
            "Spodoptera litura": "斜纹夜蛾",
            "Spodoptera exigua": "甜菜夜蛾",
            "Leucania loreyi": "劳氏粘虫",
            "Agrotis segetum": "黄地老虎"
        }

        
        class_names = result.names  # 获取类别名称字典
        
        for idx, conf in zip(cls_indices, conf_scores):
            class_idx = int(idx)
            # 然后在获取类别名称时使用映射
            class_name = class_name_mapping.get(class_names[class_idx], class_names[class_idx])
            detected_classes.append(class_name)
            confidences.append(float(conf))
    
    # 组合检测结果
    detection_results = [
        {"class": cls, "confidence": conf} 
        for cls, conf in zip(detected_classes, confidences)
    ]
    
    output_dir = f'./runs/detect/{unique_name}'
    output_path = os.path.join(output_dir, unique_img_name)

    # 记录识别历史，增加识别类别信息
    record = {
        'time': time.time(),
        'image_name': unique_img_name,
        'result_path': output_path,
        'detections': detection_results  # 添加识别结果
    }
    os.makedirs('./records', exist_ok=True)
    with open('./records/history.jsonl', 'a', encoding='utf-8') as f:
        f.write(json.dumps(record, ensure_ascii=False) + '\n')

    # 返回识别图片的访问URL和识别结果
    result_url = f"http://localhost:5000/result/{unique_name}/{unique_img_name}"
    
    # 判断主要识别结果（置信度最高的类别）
    main_detection = None
    if detected_classes:
        # 找出置信度最高的结果
        max_conf_idx = confidences.index(max(confidences))
        main_detection = {
            "class": detected_classes[max_conf_idx],
            "confidence": confidences[max_conf_idx]
        }

    return jsonify({
        "result_url": result_url,
        "image_name": unique_img_name,
        "detections": detection_results,  # 所有检测结果
        "main_detection": main_detection  # 置信度最高的检测结果
    })

# 静态文件访问，支持访问识别结果图片
@app.route('/result/<folder>/<filename>')
def serve_result_image(folder, filename):
    file_path = os.path.join('./runs/detect', folder, filename)
    if os.path.exists(file_path):
        return send_file(file_path, mimetype='image/jpeg')
    else:
        return "Not Found", 404

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
    app.run(host='localhost', port=5000, debug=True)  # 本机上运行
    #  app.run(host='192.168.225.149',debug=True)#局域网实现，
    # 需要确保手机和电脑在同一局域网内
