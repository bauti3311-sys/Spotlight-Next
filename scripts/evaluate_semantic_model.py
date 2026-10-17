import sys,time,json,resource,hashlib
from pathlib import Path
import argparse
p=argparse.ArgumentParser();p.add_argument("--model-dir",type=Path,required=True);p.add_argument("--output-dir",type=Path,required=True);args=p.parse_args();args.output_dir.mkdir(parents=True,exist_ok=True)
import onnxruntime as ort,numpy as np
from tokenizers import Tokenizer
from safetensors.numpy import load_file
root=args.model_dir;tok=Tokenizer.from_file(str(root/'tokenizer.json'));tok.enable_truncation(max_length=128)
weights=load_file(str(root/'2_Dense/model.safetensors'));print('dense',[(k,v.shape) for k,v in weights.items()],flush=True)
opt=ort.SessionOptions();opt.intra_op_num_threads=2;opt.inter_op_num_threads=1;opt.enable_cpu_mem_arena=False
started=time.perf_counter();session=ort.InferenceSession(str(root/'onnx/model_qint8_arm64.onnx'),opt,providers=['CPUExecutionProvider']);cold=(time.perf_counter()-started)*1000
print('inputs',[(x.name,x.shape) for x in session.get_inputs()],'outputs',[(x.name,x.shape) for x in session.get_outputs()],flush=True)
docs=[('vestibular','Los ejercicios de habituación y equilibrio ayudan a reducir el vértigo. Se practica la estabilidad durante la marcha y la coordinación de los movimientos de la cabeza.'),('fractura','El tratamiento de una fractura de muñeca incluye inmovilización inicial y rehabilitación progresiva de la movilidad y fuerza de la mano.'),('factura','El comprobante de pago detalla el importe del alquiler mensual, los impuestos y la fecha de vencimiento.'),('receta','Para cocinar una focaccia se mezcla harina con agua, levadura y aceite de oliva. La masa reposa antes del horneado.'),('musica','La aplicación permite reproducir canciones, pausar el audio y ajustar el volumen del dispositivo.'),('anatomia','El nervio óptico transmite señales visuales desde la retina hasta la corteza cerebral.'),('diabetes','La regulación de la glucosa requiere alimentación equilibrada, actividad física y seguimiento clínico.'),('seguridad','El cifrado protege los archivos privados mediante una clave y limita el acceso a personas autorizadas.')]
queries=[('mareos al caminar y recuperar estabilidad','vestibular'),('cómo mejorar una mano después de romperse un hueso','fractura'),('cuánto debo abonar por la vivienda este mes','factura'),('preparar pan con levadura al horno','receta'),('detener canciones y bajar el sonido','musica'),('cómo llega al cerebro lo que vemos','anatomia'),('control del azúcar en sangre','diabetes'),('evitar que otros lean mis documentos confidenciales','seguridad')]
latencies=[];vectors=[];tokens=[]
for text in [x[1] for x in docs]+[x[0] for x in queries]:
 e=tok.encode(text);tokens.append({'text':text,'ids':e.ids,'tokens':e.tokens})
 inputs={'input_ids':np.array([e.ids],dtype=np.int64),'attention_mask':np.ones((1,len(e.ids)),dtype=np.int64)}
 t=time.perf_counter();output=session.run(None,inputs)[0][0];pooled=output.mean(axis=0);v=np.tanh(weights['linear.weight']@pooled+weights['linear.bias']);v/=np.linalg.norm(v);latencies.append((time.perf_counter()-t)*1000);vectors.append(v)
scores=np.array(vectors[len(docs):])@np.array(vectors[:len(docs)]).T
results=[]
for (query,expected),row in zip(queries,scores):
 ranking=np.argsort(-row);results.append({'query':query,'expected':expected,'top':docs[ranking[0]][0],'score':float(row[ranking[0]]),'ranking':[{'id':docs[i][0],'score':float(row[i])} for i in ranking]})
report={'model':'sentence-transformers/distiluse-base-multilingual-cased-v2','revision':'bfe45d0732ca50787611c0fe107ba278c7f3f889','platform':'Linux x86_64 AMD host, NOT Android ARM64 or Tab A9+','ort':ort.__version__,'threads':2,'coldLoadMs':cold,'latencyMs':{'min':min(latencies),'median':float(np.median(latencies)),'p95':float(np.percentile(latencies,95))},'processMaxRssKiB':resource.getrusage(resource.RUSAGE_SELF).ru_maxrss,'recallAt1':sum(x['top']==x['expected'] for x in results)/len(results),'cases':results}
(args.output_dir/'host-evaluation.json').write_text(json.dumps(report,indent=2,ensure_ascii=False)+'\n');(args.output_dir/'tokenizer-reference.json').write_text(json.dumps(tokens,indent=2,ensure_ascii=False)+'\n');print(json.dumps(report,indent=2,ensure_ascii=False),flush=True)
