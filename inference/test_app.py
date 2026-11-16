import io,tempfile,unittest
from types import SimpleNamespace
from pathlib import Path
import numpy as np
from PIL import Image
from app import create_app

class Predictor:
    def predict(self,*args,**kwargs):
        return [SimpleNamespace(boxes=None,names={},plot=lambda:np.zeros((20,20,3),dtype=np.uint8))]

class ContractTests(unittest.TestCase):
    def setUp(self):
        self.tmp=tempfile.TemporaryDirectory()
        self.client=create_app(Predictor(),self.tmp.name).test_client()
    def tearDown(self):self.tmp.cleanup()
    def upload(self):
        data=io.BytesIO();Image.new('RGB',(20,20)).save(data,'PNG');data.seek(0)
        return self.client.post('/request',data={'img':(data,'sample.png')})
    def test_health(self):self.assertEqual(self.client.get('/health').status_code,200)
    def test_missing_image(self):self.assertEqual(self.client.post('/request').status_code,400)
    def test_invalid_image(self):
        self.assertEqual(self.client.post('/request',data={'img':(io.BytesIO(b'not an image'),'x.jpg')}).status_code,400)
    def test_empty_detections_are_valid(self):
        r=self.upload();self.assertEqual(r.status_code,200);self.assertIsNone(r.json['main_detection']);self.assertEqual(r.json['detections'],[])
    def test_request_scoped_result(self):
        a=self.upload().json;b=self.upload().json
        self.assertNotEqual(a['image_name'],b['image_name'])
        with self.client.get('/result/'+a['image_name']) as response:
            self.assertEqual(response.status_code,200)
        self.assertEqual(len(list(Path(self.tmp.name).glob('*.jpg'))),2)
    def test_missing_result(self):self.assertEqual(self.client.get('/result/missing.jpg').status_code,404)
    def test_traversal(self):self.assertEqual(self.client.get('/result/..%2Fapp.py').status_code,404)
    def test_request_limit(self):
        self.assertEqual(self.client.post('/request',data=b'x'*(9*1024*1024),content_type='multipart/form-data').status_code,413)

if __name__=='__main__':unittest.main()
