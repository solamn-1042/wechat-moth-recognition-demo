"""Integration smoke on five app illustrations; these are NOT a labeled validation set."""
import io,json,platform,tempfile,time
from pathlib import Path
from ultralytics import YOLO
from app import create_app

def main():
    root=Path(__file__).resolve().parent
    started=time.perf_counter();model=YOLO(root/'models/best.pt');load_ms=(time.perf_counter()-started)*1000
    parameters_before_fusion=sum(p.numel() for p in model.model.parameters())
    rows=[]
    with tempfile.TemporaryDirectory() as tmp:
        client=create_app(model,tmp).test_client()
        for name in ['agrotis_segetum','leucania_loreyi','spodoptera_exigua','spodoptera_frugiperda','spodoptera_litura']:
            source=root.parent/'miniapp/pages/data'/f'{name}.jpg'
            t=time.perf_counter();r=client.post('/request',data={'img':(io.BytesIO(source.read_bytes()),source.name)})
            assert r.status_code==200,(name,r.status_code)
            with client.get('/result/'+r.json['image_name']) as download:
                assert download.status_code==200
            rows.append({'image':source.name,'elapsed_ms':round((time.perf_counter()-t)*1000,2),'detections':r.json['detections'],'result_image_download':True})
    report={'date':'2026-11-16','python':platform.python_version(),'platform':platform.platform(),'device':'cpu','parameters':sum(p.numel() for p in model.model.parameters()),'classes':model.names,'model_load_ms':round(load_ms,2),'samples':rows,'scope':'Five app illustrations; HTTP test client plus real checkpoint, not accuracy benchmark or full three-service end-to-end test.'}
    report['parameters_before_fusion']=parameters_before_fusion
    report['parameters_after_fusion']=report.pop('parameters')
    out=root.parent/'docs/model_smoke.json';out.write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8')
    print(json.dumps({'parameters_before_fusion':parameters_before_fusion,'requests':len(rows),'latencies_ms':[r['elapsed_ms'] for r in rows]}))
if __name__=='__main__':main()
