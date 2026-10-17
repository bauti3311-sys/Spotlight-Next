"""Native host parity check of compiled production Kotlin; never an Android benchmark.
Supply compiled debug classes, ORT 1.23.2 desktop/JSON/Kotlin jars in --classpath.
Python dependencies: onnxruntime 1.23.2, safetensors 0.6.2, numpy.
Weights stay outside Git. No network request or signing occurs in this script.
"""
import argparse,json,os,resource,subprocess,tempfile
from pathlib import Path
p=argparse.ArgumentParser()
p.add_argument("--model-dir",type=Path,required=True,help="Android layout: model.onnx, vocab.txt, dense.safetensors")
p.add_argument("--reference",type=Path,required=True)
p.add_argument("--classpath",required=True)
p.add_argument("--java-bin",type=Path,required=True)
p.add_argument("--output",type=Path,required=True)
a=p.parse_args()
JAVA=r'''import dev.bautista.spotlight.semantic.*;
import org.json.*;
import java.nio.file.*;
import java.util.*;
public class ProductionSemanticProbe {
 public static void main(String[] args)throws Exception {
  Path model=Path.of(args[0]);JSONArray refs=new JSONArray(Files.readString(Path.of(args[1])));
  WordPiece tokenizer=new WordPiece(Files.readAllLines(model.resolve("vocab.txt")));
  long started=System.nanoTime();JSONArray vectors=new JSONArray();
  try(OnnxSentenceEncoder encoder=new OnnxSentenceEncoder(model.toFile())) {
   for(int i=0;i<refs.length();i++) {
    JSONObject ref=refs.getJSONObject(i);long[] ids=tokenizer.encode(ref.getString("text"));JSONArray wanted=ref.getJSONArray("ids");
    if(ids.length!=wanted.length())throw new AssertionError("Tokenizer length "+i);
    for(int j=0;j<ids.length;j++)if(ids[j]!=wanted.getLong(j))throw new AssertionError("Tokenizer id "+i+":"+j);
    float[] v=encoder.embed(ids,new InferenceCancellation());
    if(v.length!=512)throw new AssertionError("dimensions");
    vectors.put(new JSONObject().put("text",ref.getString("text")).put("embedding",v));
   }
  }
  JSONObject result=new JSONObject().put("platform","Linux x86_64 host Java17, NOT Android ARM64").put("productionClasses",true).put("samples",refs.length()).put("elapsedMs",(System.nanoTime()-started)/1e6).put("vectors",vectors);
  Files.writeString(Path.of(args[2]),result.toString(2)+"\n");System.out.println("Production Kotlin WordPiece + ONNX + Dense: "+refs.length()+" real embeddings; tokenizer IDs match Python reference.");
 }
}'''
with tempfile.TemporaryDirectory() as tmp:
 folder=Path(tmp);src=folder/"ProductionSemanticProbe.java";src.write_text(JAVA)
 subprocess.run([str(a.java_bin/"javac"),"-cp",a.classpath,str(src)],check=True)
 subprocess.run([str(a.java_bin/"java"),"-Xmx512m","-cp",a.classpath+os.pathsep+tmp,"ProductionSemanticProbe",str(a.model_dir.resolve()),str(a.reference.resolve()),str(folder/"vectors.json")],check=True)
 data=json.loads((folder/"vectors.json").read_text())
import numpy as np,onnxruntime as ort
from safetensors.numpy import load_file
assert ort.__version__=="1.23.2"
refs=json.loads(a.reference.read_text());weights=load_file(str(a.model_dir/"dense.safetensors"))
o=ort.SessionOptions();o.intra_op_num_threads=2;o.inter_op_num_threads=1;o.enable_cpu_mem_arena=False
s=ort.InferenceSession(str(a.model_dir/"model.onnx"),o,providers=["CPUExecutionProvider"])
errors=[];vectors=[]
assert len(refs)==len(data["vectors"])==16
for r,j in zip(refs,data["vectors"]):
 assert r["text"]==j["text"]
 ids=np.array([r["ids"]],dtype=np.int64)
 h=s.run(None,{"input_ids":ids,"attention_mask":np.ones_like(ids)})[0][0]
 v=np.tanh(weights["linear.weight"]@h.mean(0)+weights["linear.bias"]);v/=np.linalg.norm(v)
 actual=np.array(j["embedding"]);vectors.append(actual);errors.append(float(np.max(np.abs(v-actual))))
assert max(errors)<1e-5,errors
scores=np.array(vectors[8:])@np.array(vectors[:8]).T;tops=np.argmax(scores,axis=1).tolist()
assert tops==list(range(8)),tops
out={k:v for k,v in data.items() if k!="vectors"}
out.update({"maxAbsoluteComponentErrorVsPython":max(errors),"tolerance":1e-5,"tokenIdsMatchOfficialReference":True,"syntheticSpanishRecallAt1":1.0,"cases":8,"maxChildRssKiB":resource.getrusage(resource.RUSAGE_CHILDREN).ru_maxrss,"limitation":"Host native ONNX and production Kotlin, NOT Android ARM64 performance or broad retrieval quality."})
a.output.parent.mkdir(parents=True,exist_ok=True);a.output.write_text(json.dumps(out,indent=2)+"\n")
print(json.dumps(out,indent=2))
