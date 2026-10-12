"""Validate manifest identity/version from the built APK, never source constants."""
import re
import sys
from pathlib import Path


HISTORICAL_CERTIFICATE_SHA256 = "11b0b3b37544bd95b128de52798fa7ffb7489f94819614b1ada383bd4ee7a6cf"


def verify_certificate(report: str) -> str:
    certificates = re.findall(r"^Signer #[0-9]+ certificate SHA-256 digest: ([0-9a-fA-F]{64})$", report, re.M)
    if len(certificates) != 1 or certificates[0].lower() != HISTORICAL_CERTIFICATE_SHA256:
        raise ValueError("Certificate differs from the installed historical APK; artifact withheld")
    return f"Historical certificate SHA-256: {HISTORICAL_CERTIFICATE_SHA256}\nAPK signature: verified by apksigner\n"


def verify(badging: str, installed_code: int) -> str:
    package = re.search(r"^package: name='([^']+)' versionCode='([0-9]+)'", badging, re.M)
    if not package or package[1] != "dev.bautista.spotlight.next":
        raise ValueError("APK applicationId differs from Spotlight Next")
    if "application-debuggable" in badging:
        raise ValueError("Release APK must not be debuggable")
    code = int(package[2])
    if code <= installed_code:
        raise ValueError("APK versionCode must exceed the installed versionCode")
    sdk = re.search(r"^sdkVersion:'([0-9]+)'", badging, re.M)
    if not sdk or int(sdk[1]) > 26:
        raise ValueError("APK no longer supports the existing Android minimum")
    return f"applicationId: {package[1]}\nversionCode: {code} > installed {installed_code}\nminSdk: {sdk[1]}\n"


if __name__ == "__main__":
    try:
        print(verify(Path(sys.argv[1]).read_text(), int(sys.argv[2])), end="")
        if len(sys.argv)>5:
            badging=Path(sys.argv[1]).read_text()
            identity=re.search(r"^package: name='[^']+' versionCode='([0-9]+)' versionName='([^']+)'",badging,re.M)
            if not identity or identity[1]!=sys.argv[4] or identity[2]!=sys.argv[5]:
                raise ValueError("APK version differs from the requested release")
        if len(sys.argv)>3:
            print(verify_certificate(Path(sys.argv[3]).read_text()), end="")
    except (ValueError, IndexError) as error:
        sys.exit(str(error))
