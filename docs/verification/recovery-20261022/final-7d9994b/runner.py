import os,sys,subprocess,urllib.parse
base='/tmp/spotlight-tools';env=os.environ.copy();p=urllib.parse.urlsplit(env.get('HTTPS_PROXY',''))
opts='-Djavax.net.ssl.trustStore=/etc/ssl/certs/java/cacerts'
if p.hostname:
 for scheme in ('https','http'):opts+=f' -D{scheme}.proxyHost={p.hostname} -D{scheme}.proxyPort={p.port}'
env.update(JAVA_HOME=__import__('pathlib').Path(base+'/java-home').read_text().strip(),ANDROID_HOME=base+'/android-sdk',ANDROID_SDK_ROOT=base+'/android-sdk',GRADLE_USER_HOME='/tmp/spotlight-gradle-cache',JAVA_TOOL_OPTIONS=opts,GRADLE_OPTS=opts)
sys.exit(subprocess.call([base+'/gradle-8.10.2/bin/gradle',*sys.argv[1:],'--no-daemon','--max-workers=2','--console=plain','-Pkotlin.incremental=false','--init-script',base+'/tests.gradle'],env=env))
