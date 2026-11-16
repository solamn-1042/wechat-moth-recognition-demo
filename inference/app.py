"""Per-request YOLO inference; Java owns users and persistent history."""
import io,os,threading,uuid
from pathlib import Path
from flask import Flask,request,jsonify,send_from_directory
from PIL import Image,UnidentifiedImageError

ROOT=Path(__file__).resolve().parent
NAMES={'Spodoptera frugiperda':'草地贪夜蛾','Spodoptera litura':'斜纹夜蛾','Spodoptera exigua':'甜菜夜蛾','Leucania loreyi':'劳氏粘虫','Agrotis segetum':'黄地老虎'}

def create_app(predictor=None,output_dir=None):
    app=Flask(__name__);app.config['MAX_CONTENT_LENGTH']=8*1024*1024
    output=Path(output_dir or os.getenv('RESULT_DIR',str(ROOT/'runtime/results')));output.mkdir(parents=True,exist_ok=True)
    lock=threading.Lock();state={'model':predictor}
    def model():
        if state['model'] is None:
            from ultralytics import YOLO
            state['model']=YOLO(os.getenv('MODEL_PATH',str(ROOT/'models/best.pt')))
        return state['model']
    @app.get('/health')
    def health():return {'status':'ready','model_loaded':state['model'] is not None,'device':os.getenv('MODEL_DEVICE','cpu')}
    @app.post('/request')
    def detect():
        if 'img' not in request.files:return jsonify(error='IMAGE_REQUIRED'),400
        try:
            raw=request.files['img'].read()
            with Image.open(io.BytesIO(raw)) as im:
                if im.width*im.height>20_000_000:return jsonify(error='IMAGE_TOO_LARGE'),413
                image=im.convert('RGB')
        except (UnidentifiedImageError,OSError,Image.DecompressionBombError):return jsonify(error='INVALID_IMAGE'),400
        name=uuid.uuid4().hex+'.jpg'
        with lock:
            results=model().predict(image,save=False,device=os.getenv('MODEL_DEVICE','cpu'),verbose=False)
            r=results[0];detections=[]
            if r.boxes is not None:
                for idx,confidence in zip(r.boxes.cls.cpu().tolist(),r.boxes.conf.cpu().tolist()):
                    label=r.names[int(idx)];detections.append({'class':NAMES.get(label,label),'confidence':float(confidence)})
            Image.fromarray(r.plot()[:,:,::-1]).save(output/name,format='JPEG')
        base=os.getenv('RESULT_BASE_URL','http://127.0.0.1:5000').rstrip('/')
        return jsonify(result_url=base+'/result/'+name,image_name=name,detections=detections,
            main_detection=max(detections,key=lambda d:d['confidence']) if detections else None)
    @app.get('/result/<filename>')
    def result(filename):return send_from_directory(output,filename,mimetype='image/jpeg')
    return app

app=create_app()
if __name__=='__main__':app.run(host=os.getenv('FLASK_HOST','127.0.0.1'),port=int(os.getenv('FLASK_PORT','5000')),debug=False)
