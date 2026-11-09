"""Fail-closed native60 APK/source verification. Never executes archive contents."""
import hashlib
import json
import os
from pathlib import Path, PurePosixPath
import re
import struct
import subprocess
import sys
import zipfile
from native60_policy import SOURCE, CERTIFICATE, PACKAGE, VERSION_NAME


def sha(path):
    return hashlib.sha256(Path(path).read_bytes()).hexdigest()


def certificate(report):
    values=re.findall(r'Signer #\d+ certificate SHA-256 digest: ([0-9a-fA-F]{64})',report)
    if len(values)!=1 or values[0].lower()!=CERTIFICATE or 'Verified using v2 scheme (APK Signature Scheme v2): true' not in report:
        raise ValueError('Historical Preview signature not verified.')


def metadata(report):
    expected=f"package: name='{PACKAGE}' versionCode='60' versionName='{VERSION_NAME}'"
    if expected not in report.splitlines()[0] or 'application-debuggable' in report:
        raise ValueError('Wrong Preview identity or debuggable APK.')
    for line in ["sdkVersion:'26'", "targetSdkVersion:'35'", "native-code: 'arm64-v8a'"]:
        if line not in report.splitlines(): raise ValueError('Wrong SDK/ABI metadata.')


def safe_paths(z):
    names=[i.filename for i in z.infolist()]
    if len(set(names))!=len(names): raise ValueError('Duplicate archive entries.')
    for i in z.infolist():
        p=PurePosixPath(i.filename)
        if p.is_absolute() or '..' in p.parts or (i.external_attr>>16)&0o170000==0o120000:
            raise ValueError('Unsafe archive entry.')
        if p.name in {'credentials.properties','local.properties'} or p.suffix.lower() in {'.keystore','.jks'}:
            raise ValueError('Private signing/configuration input in archive.')


def source_archive(path):
    with zipfile.ZipFile(path) as z:
        safe_paths(z)
        if z.comment.decode('ascii')!=SOURCE or z.testzip() is not None:
            raise ValueError('Source ZIP is not the exact approved git archive.')


def payload(path):
    with zipfile.ZipFile(path) as z:
        safe_paths(z)
        if z.testzip() is not None: raise ValueError('Corrupt APK archive.')
        entries={i.filename:hashlib.sha256(z.read(i)).hexdigest() for i in z.infolist()}
        if any('/_nuxt/' in n or 'raycast-web/' in n for n in entries): raise ValueError('Unexpected web visual assets.')
        dex=[n for n in entries if re.fullmatch(r'classes\d*\.dex',n)]
        if not dex or any(b'LocalRaycastWebView' in z.read(n) for n in dex): raise ValueError('Unexpected visual bridge or missing DEX.')
        libs=[i for i in z.infolist() if i.filename.startswith('lib/') and i.filename.endswith('.so')]
        if not libs: raise ValueError('No native libraries.')
        for i in libs:
            if not i.filename.startswith('lib/arm64-v8a/') or i.compress_type!=zipfile.ZIP_STORED: raise ValueError('Wrong ABI/compression.')
            data=z.read(i)
            if len(data)<64 or data[:6]!=b'\x7fELF\x02\x01' or struct.unpack_from('<H',data,18)[0]!=183:
                raise ValueError('Invalid ARM64 ELF.')
            offset=struct.unpack_from('<Q',data,32)[0]
            size,count=struct.unpack_from('<HH',data,54)
            if size<56 or not count or offset+size*count>len(data): raise ValueError('Invalid ELF table.')
            loads=0
            for idx in range(count):
                kind,_,pos,vaddr,_,_,_,align=struct.unpack_from('<IIQQQQQQ',data,offset+idx*size)
                if kind==1:
                    loads+=1
                    if align<16384 or align&(align-1) or (vaddr-pos)%align: raise ValueError('ELF not 16KiB aligned.')
            if not loads: raise ValueError('Missing ELF load segment.')
        return entries


def run(*args):
    result=subprocess.run([str(x) for x in args],capture_output=True,text=True)
    if result.returncode: raise ValueError('Android artifact verification command failed.')
    return result.stdout


def main(mode,root,tools):
    root,tools=Path(root),Path(tools)
    info=json.loads((root/'provenance.json').read_text())
    if info['source_sha']!=SOURCE or info['workflow_sha']!=os.environ['GITHUB_WORKFLOW_SHA'] or str(info['run_id'])!=os.environ['GITHUB_RUN_ID'] or info['profile']!='optimized':
        raise ValueError('Provenance does not match this approved run.')
    if mode=='unsigned':
        apk,src=root/'unsigned.apk',root/'source.zip'
        if sha(apk)!=info['unsigned_sha256'] or sha(src)!=info['source_zip_sha256']: raise ValueError('Artifact digest mismatch.')
        probe=subprocess.run([str(tools/'apksigner'),'verify',str(apk)],capture_output=True)
        if probe.returncode==0: raise ValueError('Build job must produce an unsigned candidate.')
    elif mode=='signed':
        apk,src=root/'Spotlight-Next-Native57-Validation60-e9647c1.apk',root/'Spotlight-Next-source-e9647c1.zip'
        report=run(tools/'apksigner','verify','--verbose','--print-certs',apk)
        certificate(report);(root/'signature.txt').write_text(report)
        if sha(src)!=info['source_zip_sha256']: raise ValueError('Source digest mismatch.')
    else: raise ValueError('Unsupported verification stage.')
    source_archive(src)
    report=run(tools/'aapt','dump','badging',apk);metadata(report)
    contents=payload(apk)
    if mode=='signed' and contents!=info['payload']: raise ValueError('Signed payload differs from the approved build.')
    run(tools/'zipalign','-c','-P','16','4',apk)
    if mode=='signed':
        (root/'package.txt').write_text(report)
        (root/'SOURCE_COMMIT').write_text(SOURCE+'\n')
        (root/'WORKFLOW_COMMIT').write_text(info['workflow_sha']+'\n')
        (root/'SHA256SUMS').write_text(''.join(f'{sha(p)}  {p.name}\n' for p in [apk,src]))


if __name__=='__main__':
    try: main(*sys.argv[1:])
    except (ValueError,KeyError,TypeError,OSError,zipfile.BadZipFile):
        sys.exit('Native60 artifact verification failed; output withheld.')
