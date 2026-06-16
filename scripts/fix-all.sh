#!/usr/bin/env bash
# =====================================
# VisePanda-Android-Hermes v0.2.0
# Comprehensive fix script
# =====================================
set -e

BASE="/home/ubuntu/projects/visepanda-android-hermes"
VISE_PANDA_2="/home/ubuntu/projects/vise-panda-2"

echo "=== Fix 0: Change app name to vp-hermes ==="

# AndroidManifest - change label
sed -i 's/android:label="VisePanda"/android:label="vp-hermes"/g' \
  "$BASE/app/src/main/AndroidManifest.xml"

# build.gradle - change appName
sed -i 's/appName = "VisePanda"/appName = "vp-hermes"/g' \
  "$BASE/app/build.gradle.kts"

echo "✅ App name: vp-hermes"

echo "=== Fix 1: Backend import re ==="
ORIG_IMPORT=$(sed -n '1,50p' "$VISE_PANDA_2/api/index.py" | grep -n "^import" | head -1 | cut -d: -f1)
if ! grep -q "^import re" "$VISE_PANDA_2/api/index.py"; then
  # Add import re right after the first import line
  FIRST_IMPORT_LINE=$(grep -n "^import \|^from " "$VISE_PANDA_2/api/index.py" | head -1 | cut -d: -f1)
  sed -i "${FIRST_IMPORT_LINE}a import re" "$VISE_PANDA_2/api/index.py"
  # Remove the local import inside _tokenize
  TOKENIZE_LINE=$(grep -n "^def _tokenize" "$VISE_PANDA_2/api/index.py" | head -1 | cut -d: -f1)
  SED_END=$((TOKENIZE_LINE + 5))
  sed -i "${TOKENIZE_LINE},${SED_END}s/^    import re/#    import re (moved to module level)/" "$VISE_PANDA_2/api/index.py"
  echo "✅ Backend import re fixed"
else
  echo "⏭️  Backend import re already present"
fi

echo "=== Fix 2: Delete empty ToolItem.kt ==="
if [ -f "$BASE/app/src/main/java/space/jtcao/visepanda/data/model/ToolItem.kt" ]; then
  rm "$BASE/app/src/main/java/space/jtcao/visepanda/data/model/ToolItem.kt"
  echo "✅ ToolItem.kt deleted"
else
  echo "⏭️  ToolItem.kt not found"
fi

echo "=== Fix 3: Fix ToolsViewModel conflict ==="
sed -i '/import.*\.data\.model\.ToolEntry/d' \
  "$BASE/app/src/main/java/space/jtcao/visepanda/ui/tools/ToolsViewModel.kt"
echo "✅ ToolsViewModel import fixed"

echo "=== Fix 4-6: Write clean replacements ==="
# These are handled by Python script below

echo "All shell fixes done!"
