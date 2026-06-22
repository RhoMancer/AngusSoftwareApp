#!/bin/sh
if [ ! -f composeApp/build/reports/kover/report.xml ]; then
  echo "Kover XML not found"
  exit 0
fi

python3 -c '
import xml.etree.ElementTree as ET
tree = ET.parse("composeApp/build/reports/kover/report.xml")
for pkg in tree.findall(".//package"):
    pn = pkg.get("name","")
    for cls in pkg.findall("class"):
        cn = cls.get("name","")
        source = cls.get("sourcefilename","")
        lm=lc=0
        for c in cls.findall("counter"):
            if c.get("type","")=="LINE":
                lm+=int(c.get("missed","0")); lc+=int(c.get("covered","0"))
        total=lm+lc
        if total>0 and lm>0:
            print(f"{pn}/{cn} ({source}): {lc}/{total} lines, {lm} missed")
'
