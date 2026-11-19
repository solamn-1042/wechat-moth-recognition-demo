"""Generate one real inference fixture for the documentation preview."""
import io,json,sys,tempfile,os
from pathlib import Path
root=Path(__file__).resolve().parents[2]
sys.path.insert(0,str(root/'inference'))
os.environ.setdefault('YOLO_CONFIG_DIR',str(root/'inference/runtime/yolo-config'))
from app import create_app

dest=root/'docs/images'
dest.mkdir(parents=True,exist_ok=True)
source=root/'miniapp/pages/data/spodoptera_frugiperda.jpg'
with tempfile.TemporaryDirectory() as tmp:
    client=create_app(output_dir=tmp).test_client()
    response=client.post('/request',data={'img':(io.BytesIO(source.read_bytes()),source.name)})
    assert response.status_code==200,response.status_code
    data=response.json
    image=client.get('/result/'+data['image_name'])
    assert image.status_code==200
    (dest/'inference-sample.jpg').write_bytes(image.data)
    image.close()
    fixture={'source_image':'miniapp/pages/data/'+source.name,'method':'Flask test client with repository checkpoint on CPU; not a WeChat end-to-end test','detections':data['detections'],'main_detection':data['main_detection'],'result_image':'docs/images/inference-sample.jpg'}
    (dest/'inference-sample.json').write_text(json.dumps(fixture,ensure_ascii=False,indent=2),encoding='utf-8')
    print(json.dumps(fixture,ensure_ascii=False))
