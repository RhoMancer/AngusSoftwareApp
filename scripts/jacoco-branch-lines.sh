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
    for sf in pkg.findall("sourcefile"):
        src = sf.get("name","")
        for line in sf.findall("line"):
            mb = int(line.get("mb","0"))
            if mb > 0:
                nr = line.get("nr","")
                cb = int(line.get("cb","0"))
                ci = int(line.get("ci","0"))
                mi = int(line.get("mi","0"))
                results.append((src, int(nr), mb, cb))

# Group by file
from collections import defaultdict
by_file = defaultdict(list)
for src, nr, mb, cb in results:
    by_file[src].append((nr, mb, cb))

# Sort files by total missed branches
file_totals = [(sum(x[1] for x in lines), src, lines) for src, lines in by_file.items()]
file_totals.sort(key=lambda x: -x[0])

total_missed = sum(ft[0] for ft in file_totals)
print("Total lines with missed branches: " + str(len(results)))
print("Total missed branches: " + str(total_missed))
print()

for total, src, lines in file_totals[:15]:
    print("=== " + src + " (" + str(total) + " missed branches) ===")
    for nr, mb, cb in sorted(lines):
        print("  line " + str(nr) + ": " + str(mb) + " missed / " + str(cb) + " covered")
    print()
PYEOF
