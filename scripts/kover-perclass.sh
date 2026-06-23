#!/bin/sh
if [ ! -f composeApp/build/reports/kover/report.xml ]; then
  echo "Kover XML not found"
  exit 0
fi
python3 -c '
import xml.etree.ElementTree as ET
tree = ET.parse("composeApp/build/reports/kover/report.xml")
for pkg in tree.findall(".//package"):
    for cls in pkg.findall("class"):
        name = cls.get("name","")
        src = cls.get("sourcefilename","")
        lm = lc = 0
        for c in cls.findall("counter"):
            if c.get("type","") == "LINE":
                lm += int(c.get("missed","0"))
                lc += int(c.get("covered","0"))
        total = lm + lc
        if lm > 0:
            pct = 100.0 * lc / total if total > 0 else 0
            print(f"{src}: {lc}/{total} ({pct:.0f}%) MISSED {lm} [{name}]")
'
