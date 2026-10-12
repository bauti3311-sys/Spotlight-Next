"""Public release identity shared by Gradle and CI. Never reads signing inputs."""
import re
import sys
from pathlib import Path

def read(path=Path(__file__).resolve().parents[1] / "release.properties"):
    data=dict(line.split("=",1) for line in Path(path).read_text().splitlines() if "=" in line and not line.lstrip().startswith("#"))
    name=data["version.name"].strip()
    code=int(data["version.code"])
    previous=int(data["installed.code"])
    if not re.fullmatch(r"[0-9]+\.[0-9]+\.[0-9]+",name) or code<=previous or previous<1:
        raise ValueError("Invalid release identity or update baseline")
    return {"name":name,"code":str(code),"installed":str(previous)}

if __name__=="__main__":
    try: print(read()[sys.argv[1]])
    except (ValueError,KeyError,IndexError) as error: sys.exit(str(error))
