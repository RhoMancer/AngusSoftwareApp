#!/bin/sh
JACOCO_XML="composeApp/build/reports/jacoco/androidConnectedTest/report.xml"
if [ ! -f "$JACOCO_XML" ]; then
  echo "JaCoCo XML not found"
  exit 0
fi
python3 -c '
import xml.etree.ElementTree as ET
tree = ET.parse("'"$JACOCO_XML"'")
for pkg in tree.findall(".//package"):
    for sf in pkg.findall("sourcefile"):
        src = sf.get("name","")
        missed_lines = []
        for line in sf.findall("line"):
            mi = int(line.get("mi","0"))
            ci = int(line.get("ci","0"))
            nr = line.get("nr","")
            if mi > 0 and ci == 0:
                missed_lines.append(nr)
        if missed_lines:
            # Group consecutive line numbers
            groups = []
            start = int(missed_lines[0])
            prev = start
            for ln in missed_lines[1:]:
                ln = int(ln)
                if ln == prev + 1:
                    prev = ln
                else:
                    if start == prev:
                        groups.append(str(start))
                    else:
                        groups.append(f"{start}-{prev}")
                    start = ln
                    prev = ln
            if start == prev:
                groups.append(str(start))
            else:
                groups.append(f"{start}-{prev}")
            print(f"{src}: lines {', '.join(groups)}")
'
