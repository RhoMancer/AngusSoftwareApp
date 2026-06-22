#!/bin/sh
# Dumps per-file Kover coverage from report.xml
if [ ! -f composeApp/build/reports/kover/report.xml ]; then
  echo "Kover XML not found"
  exit 0
fi

python3 -c '
import xml.etree.ElementTree as ET
tree = ET.parse("composeApp/build/reports/kover/report.xml")
for pkg in tree.findall(".//package"):
    for cls in pkg.findall("class"):
        source = cls.get("sourcefilename","")
        lm=lc=im=ic=bm=bc=0
        for c in cls.findall("counter"):
            t=c.get("type","")
            m=int(c.get("missed","0"))
            cv=int(c.get("covered","0"))
            if t=="LINE": lm+=m; lc+=cv
            elif t=="INSTRUCTION": im+=m; ic+=cv
            elif t=="BRANCH": bm+=m; bc+=cv
        total=lm+lc
        if total>0:
            pct=100.0*lc/total
            print(f"{source}: {lc}/{total} lines ({pct:.0f}%), {im} instr missed, {bm} branches missed")
'
