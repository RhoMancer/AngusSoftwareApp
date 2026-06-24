#!/bin/sh
JACOCO_XML="composeApp/build/reports/jacoco/androidConnectedTest/report.xml"
if [ ! -f "$JACOCO_XML" ]; then
  echo "JaCoCo XML not found"
  exit 0
fi
python3 << 'PYEOF'
import xml.etree.ElementTree as ET
tree = ET.parse("composeApp/build/reports/jacoco/androidConnectedTest/report.xml")
results = []
for pkg in tree.findall(".//package"):
    for cls in pkg.findall("class"):
        name = cls.get("name","")
        src = cls.get("sourcefilename","")
        bm = bc = 0
        for c in cls.findall("counter"):
            if c.get("type","") == "BRANCH":
                bm += int(c.get("missed","0"))
                bc += int(c.get("covered","0"))
        if bm > 0:
            results.append((bm, bc, bm+bc, src, name))
results.sort(key=lambda x: -x[0])
print("Total missed branches: " + str(sum(r[0] for r in results)))
print()
for bm, bc, total, src, name in results[:20]:
    pct = 100.0 * bc / total if total > 0 else 0
    print(src + ": " + str(bc) + "/" + str(total) + " (" + str(round(pct)) + "%) MISSED " + str(bm))
PYEOF
