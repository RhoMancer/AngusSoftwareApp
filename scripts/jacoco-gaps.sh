#!/bin/sh
JACOCO_XML="composeApp/build/reports/jacoco/androidConnectedTest/report.xml"
if [ ! -f "$JACOCO_XML" ]; then
  echo "JaCoCo XML not found at $JACOCO_XML"
  exit 0
fi
python3 -c '
import xml.etree.ElementTree as ET
tree = ET.parse("'"$JACOCO_XML"'")
results = []
for pkg in tree.findall(".//package"):
    for cls in pkg.findall("class"):
        name = cls.get("name","")
        src = cls.get("sourcefilename","")
        lm = lc = 0
        for c in cls.findall("counter"):
            if c.get("type","") == "LINE":
                lm += int(c.get("missed","0"))
                lc += int(c.get("covered","0"))
        if lm > 0:
            results.append((lm, lc, lm+lc, src, name))
results.sort(key=lambda x: -x[0])
print(f"Total missed lines: {sum(r[0] for r in results)}")
print()
for lm, lc, total, src, name in results[:20]:
    pct = 100.0 * lc / total if total > 0 else 0
    print(f"{src}: {lc}/{total} ({pct:.0f}%) MISSED {lm} [{name}]")
'
