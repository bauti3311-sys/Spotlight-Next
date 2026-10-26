from pathlib import Path
import subprocess,json,hashlib,zipfile,re,shutil,struct
import xml.etree.ElementTree as ET

root=Path('Spotlight-Next');out=Path('candidate40-delivery');out.mkdir(exist_ok=True)
request=json.loads(Path('.recovery-library/delivery-request.json').read_text());sha=request['code_sha']
log=Path(request['log']).read_text()
assert 'BUILD SUCCESSFUL' in log and 'BUILD FAILED' not in log,'Final gate has not succeeded'
def git(*args):return subprocess.check_output(['git','-C',str(root),*args]).decode().strip()
assert git('rev-parse','HEAD')==sha
assert not git('diff','--name-only','HEAD','--','app','build.gradle.kts','settings.gradle.kts','gradle.properties','release.properties')
apk=out/f'Spotlight-Next-Raycast-Candidate-40-{sha[:7]}.apk'
shutil.copy2(root/'app/build/outputs/apk/raycastPreview/app-raycastPreview.apk',apk)
tools=Path('/workspace/scratch/cd7b8fe1285c/spotlight-runtime/android-sdk/build-tools/35.0.0')
signature=subprocess.check_output([str(tools/'apksigner'),'verify','--verbose','--print-certs',str(apk)],text=True)
certificate='885838aa970e0d4ba168a2b5f3e8f1a6e7334263c34c9cbc888f5c70381a52ba'
assert certificate in signature
assert 'Verified using v2 scheme (APK Signature Scheme v2): true' in signature
(out/'signature.txt').write_text(signature)
badging=subprocess.check_output([str(tools/'aapt2'),'dump','badging',str(apk)],text=True)
assert "name='dev.bautista.spotlight.next.raycastpreview'" in badging and "versionCode='40'" in badging
assert "minSdkVersion:'26'" in badging and "targetSdkVersion:'35'" in badging
assert 'application-debuggable' not in badging
assert (root/'app/build/outputs/mapping/raycastPreview/mapping.txt').stat().st_size>0
(out/'package.txt').write_text(badging)
alignment=subprocess.check_output([str(tools/'zipalign'),'-c','-P','16','-v','4',str(apk)],text=True)
(out/'zipalign.txt').write_text(alignment)
native={}
with zipfile.ZipFile(apk) as z:
    for n in z.namelist():
        if n.startswith('lib/'):
            b=z.read(n)
            assert n.startswith('lib/arm64-v8a/') and n.endswith('.so') and not Path(n).name.startswith('.')
            assert b[:4]==b'\x7fELF' and struct.unpack_from('<H',b,18)[0]==183
            native[Path(n).name]=hashlib.sha256(b).hexdigest()
    assert native and any(n.startswith('classes') and n.endswith('.dex') for n in z.namelist())
    dex_files=[n for n in z.namelist() if n.startswith('classes') and n.endswith('.dex')]
    assert all(z.read(n).startswith(b'dex\n') for n in dex_files)
origins={}
for p in Path('/workspace/scratch/cd7b8fe1285c/spotlight-gradle-cache/caches/modules-2/files-2.1').rglob('*.aar'):
    with zipfile.ZipFile(p) as z:
        for n in z.namelist():
            if n.startswith('jni/arm64-v8a/') and Path(n).name in native:
                origins.setdefault(Path(n).name,set()).add(hashlib.sha256(z.read(n)).hexdigest())
assert all(v in origins.get(k,set()) for k,v in native.items()),'Native payload differs from original dependencies'
source=out/f'Spotlight-Next-source-{sha[:7]}.zip'
subprocess.run(['git','-C',str(root),'archive','--format=zip','--prefix=Spotlight-Next/','-o',str(source.resolve()),sha],check=True)
expected={}
for row in subprocess.check_output(['git','-C',str(root),'ls-tree','-r','-z',sha]).split(b'\0'):
    if row:
        meta,name=row.split(b'\t',1);expected[name.decode()]=meta.decode().split()[2]
with zipfile.ZipFile(source) as z:
    actual={n.removeprefix('Spotlight-Next/'):z.read(n) for n in z.namelist() if not n.endswith('/')}
assert actual.keys()==expected.keys()
for n,b in actual.items():assert hashlib.sha1(b'blob '+str(len(b)).encode()+b'\0'+b).hexdigest()==expected[n],n
assert not any(n.endswith(('.keystore','.jks','credentials.properties')) for n in actual)
inputs={n:d for n,d in expected.items() if n.startswith('app/src/main/') or n in ['app/build.gradle.kts','app/proguard-rules.pro','build.gradle.kts','settings.gradle.kts','gradle.properties','release.properties'] or n.startswith('gradle/wrapper/')}
(out/'source-inputs.json').write_text(json.dumps({'code_sha':sha,'inputs':inputs,'verified_zip_blobs':len(actual)},indent=2))
lint=ET.parse(root/'app/build/reports/lint-results-raycastPreview.xml').getroot()
severities={s:sum(i.attrib.get('severity')==s for i in lint.findall('issue')) for s in ['Fatal','Error','Warning','Information']}
assert severities['Fatal']==0 and severities['Error']==0
files={p.name:{'size_bytes':p.stat().st_size,'sha256':hashlib.sha256(p.read_bytes()).hexdigest()} for p in [apk,source]}
result={**request,'versionCode':40,'package':'dev.bautista.spotlight.next.raycastpreview','certificate_sha256':certificate,'signature_v2':True,'zipalign_16KB':True,'minSdk':26,'targetSdk':35,'abi':['arm64-v8a'],'native_libraries_match_original_aars':native,'source_zip_verified_blobs':len(actual),'production_inputs':len(inputs),'lint':severities,'release_r8':True,'physical_device':False,'files':files}
(out/'verification.json').write_text(json.dumps(result,indent=2))
print(json.dumps(result,indent=2))
