// Injeta o HTML renderizado no servidor (dist-ssr) em cada página de dist/ e remove o bundle de SSR.
import { readFile, rm, writeFile } from 'node:fs/promises';
import { fileURLToPath, pathToFileURL } from 'node:url';

const root = fileURLToPath(new URL('..', import.meta.url));
const { paginas } = await import(pathToFileURL(`${root}dist-ssr/entry-server.js`).href);

for (const [pagina, render] of Object.entries(paginas)) {
  const file = `${root}dist/${pagina}`;
  const html = await readFile(file, 'utf8');
  if (!html.includes('<!--app-html-->')) throw new Error(`Marcador <!--app-html--> não encontrado em dist/${pagina}`);
  await writeFile(file, html.replace('<!--app-html-->', render()));
  console.log(`Pré-renderizado: dist/${pagina}`);
}
await rm(`${root}dist-ssr`, { recursive: true, force: true });
