#!/bin/sh
if [ ! -f composeApp/build/reports/kover/report.xml ]; then
  exit 0
fi
python3 -c '
import xml.etree.ElementTree as ET
tree = ET.parse("composeApp/build/reports/kover/report.xml")
for cls in tree.findall(".//class"):
    name = cls.get("name","")
    src = cls.get("sourcefilename","")
    if any(k in src.lower() for k in ["theme","platform","navigation","mainactivity"]):
        print(f"{name} (source: {src})")
'
