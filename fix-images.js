const fs = require('fs');
const path = require('path');

const dir = path.join(__dirname, 'acg-frontend/src/views');
const traePattern = /https:\/\/trae-api-cn\.mchost\.guru\/api\/ide\/v1\/text_to_image\?prompt=[^'")\s]+/g;

const replacements = [
  [/anime\+cosplay|cosplay.*makeup|makeup.*service/i, 'https://picsum.photos/seed/cosplay/640/400'],
  [/anime\+figure|anime/i, 'https://picsum.photos/seed/anime/400/400'],
  [/.*/, 'https://picsum.photos/seed/acg/400/400'],
];

function walk(dir) {
  const files = [];
  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    const full = path.join(dir, entry.name);
    if (entry.isDirectory()) files.push(...walk(full));
    else if (entry.name.endsWith('.vue')) files.push(full);
  }
  return files;
}

const vueFiles = walk(dir);
let totalFixed = 0;

for (const file of vueFiles) {
  let content = fs.readFileSync(file, 'utf8');
  if (!content.includes('trae-api-cn')) continue;
  
  const newContent = content.replace(traePattern, (url) => {
    for (const [pattern, replacement] of replacements) {
      if (pattern.test(url)) return replacement;
    }
    return 'https://picsum.photos/seed/acg/400/400';
  });
  
  if (newContent !== content) {
    fs.writeFileSync(file, newContent, 'utf8');
    totalFixed++;
    console.log('Fixed:', file);
  }
}

console.log(`\nTotal files fixed: ${totalFixed}`);

// Verify
const remaining = vueFiles.filter(f => fs.readFileSync(f, 'utf8').includes('trae-api-cn'));
if (remaining.length > 0) {
  console.log('WARNING: Still have trae-api URLs in:', remaining);
} else {
  console.log('All slow AI image URLs replaced!');
}
