import { mkdirSync, readFileSync, writeFileSync } from 'node:fs';
import { dirname, resolve } from 'node:path';

const reports = [
  {
    label: 'Backend',
    file: 'backend/build/reports/cyclonedx-direct/bom.json',
  },
  {
    label: 'Frontend',
    file: 'frontend/build/reports/cyclonedx/bom.json',
  },
];

const outputFile = 'build/reports/sbom-report/index.html';

function escapeHtml(value) {
  return String(value ?? '')
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#39;');
}

function readBom(report) {
  const bom = JSON.parse(readFileSync(report.file, 'utf8'));
  const components = (bom.components ?? []).map((component) => ({
    source: report.label,
    type: component.type ?? '',
    group: component.group ?? '',
    name: component.name ?? '',
    version: component.version ?? '',
    scope: component.scope ?? '',
    purl: component.purl ?? component['bom-ref'] ?? '',
    licenses: readLicenses(component),
  }));

  return {
    ...report,
    bomFormat: bom.bomFormat ?? '',
    specVersion: bom.specVersion ?? '',
    serialNumber: bom.serialNumber ?? '',
    componentName: bom.metadata?.component?.name ?? report.label,
    componentVersion: bom.metadata?.component?.version ?? '',
    components,
  };
}

function readLicenses(component) {
  return (component.licenses ?? [])
    .map((entry) => entry.license?.id ?? entry.license?.name ?? entry.expression ?? '')
    .filter(Boolean)
    .join(', ');
}

function countBy(items, selector) {
  const counts = new Map();
  for (const item of items) {
    const key = selector(item) || 'unknown';
    counts.set(key, (counts.get(key) ?? 0) + 1);
  }
  return [...counts.entries()].sort((a, b) => b[1] - a[1] || a[0].localeCompare(b[0]));
}

function packageType(component) {
  const match = component.purl.match(/^pkg:([^/]+)/);
  return match?.[1] ?? component.type ?? 'unknown';
}

function summaryCard(label, value, hint) {
  return `
    <section class="metric">
      <span>${escapeHtml(label)}</span>
      <strong>${escapeHtml(value)}</strong>
      <small>${escapeHtml(hint)}</small>
    </section>
  `;
}

function barList(title, counts, limit = 10) {
  const max = Math.max(...counts.map(([, count]) => count), 1);
  const rows = counts.slice(0, limit).map(([label, count]) => `
    <li>
      <span class="bar-label">${escapeHtml(label)}</span>
      <span class="bar-track"><span class="bar-fill" style="width: ${(count / max) * 100}%"></span></span>
      <span class="bar-count">${count}</span>
    </li>
  `).join('');

  return `
    <section class="panel">
      <h2>${escapeHtml(title)}</h2>
      <ul class="bars">${rows}</ul>
    </section>
  `;
}

function renderTable(components) {
  return components.map((component) => `
    <tr>
      <td>${escapeHtml(component.source)}</td>
      <td>${escapeHtml(packageType(component))}</td>
      <td>
        <strong>${escapeHtml(component.name)}</strong>
        ${component.group ? `<small>${escapeHtml(component.group)}</small>` : ''}
      </td>
      <td>${escapeHtml(component.version)}</td>
      <td>${escapeHtml(component.licenses || 'unknown')}</td>
      <td><code>${escapeHtml(component.purl)}</code></td>
    </tr>
  `).join('');
}

function renderReport() {
  const boms = reports.map(readBom);
  const components = boms.flatMap((bom) => bom.components);
  const bySource = countBy(components, (component) => component.source);
  const byPackageType = countBy(components, packageType);
  const byLicense = countBy(components, (component) => component.licenses);
  const withUnknownLicense = components.filter((component) => !component.licenses).length;

  const generatedAt = new Date().toLocaleString('en-GB', {
    dateStyle: 'medium',
    timeStyle: 'medium',
  });

  return `<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Mail System SBOM Report</title>
  <style>
    :root {
      color-scheme: light;
      --bg: #f6f7f9;
      --ink: #18212f;
      --muted: #647084;
      --line: #d7dde7;
      --panel: #ffffff;
      --accent: #0f766e;
      --accent-soft: #d9f2ef;
      --blue: #2563eb;
      --blue-soft: #e4ecff;
      --warn: #9a3412;
      --warn-soft: #ffedd5;
      --shadow: 0 12px 30px rgba(15, 23, 42, .08);
    }

    * {
      box-sizing: border-box;
    }

    body {
      margin: 0;
      background: var(--bg);
      color: var(--ink);
      font-family: Inter, ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
      line-height: 1.5;
    }

    header {
      background: #111827;
      color: white;
      padding: 40px 32px;
    }

    header div,
    main {
      max-width: 1180px;
      margin: 0 auto;
    }

    h1,
    h2 {
      margin: 0;
      letter-spacing: 0;
    }

    h1 {
      font-size: 34px;
      line-height: 1.15;
    }

    h2 {
      font-size: 18px;
      margin-bottom: 18px;
    }

    p {
      color: #cbd5e1;
      margin: 10px 0 0;
      max-width: 720px;
    }

    main {
      padding: 28px 24px 48px;
    }

    .metrics {
      display: grid;
      grid-template-columns: repeat(4, minmax(0, 1fr));
      gap: 16px;
      margin-bottom: 24px;
    }

    .metric,
    .panel,
    .table-wrap {
      background: var(--panel);
      border: 1px solid var(--line);
      border-radius: 8px;
      box-shadow: var(--shadow);
    }

    .metric {
      padding: 18px;
    }

    .metric span,
    .metric small {
      display: block;
      color: var(--muted);
      font-size: 13px;
    }

    .metric strong {
      display: block;
      font-size: 30px;
      line-height: 1.1;
      margin: 8px 0;
    }

    .grid {
      display: grid;
      grid-template-columns: repeat(2, minmax(0, 1fr));
      gap: 16px;
      margin-bottom: 24px;
    }

    .panel {
      padding: 20px;
    }

    .bars {
      list-style: none;
      padding: 0;
      margin: 0;
      display: grid;
      gap: 12px;
    }

    .bars li {
      display: grid;
      grid-template-columns: minmax(140px, 1fr) 2fr 48px;
      gap: 12px;
      align-items: center;
      font-size: 14px;
    }

    .bar-label {
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      color: var(--ink);
    }

    .bar-track {
      height: 10px;
      background: var(--blue-soft);
      border-radius: 999px;
      overflow: hidden;
    }

    .bar-fill {
      display: block;
      height: 100%;
      background: var(--blue);
      border-radius: inherit;
    }

    .bar-count {
      color: var(--muted);
      text-align: right;
      font-variant-numeric: tabular-nums;
    }

    .search {
      display: flex;
      gap: 12px;
      align-items: center;
      margin-bottom: 14px;
    }

    .search input {
      width: 100%;
      border: 1px solid var(--line);
      border-radius: 8px;
      padding: 12px 14px;
      font: inherit;
      background: white;
      color: var(--ink);
    }

    .search span {
      white-space: nowrap;
      color: var(--muted);
      font-size: 14px;
    }

    .table-wrap {
      padding: 16px;
      overflow: hidden;
    }

    .table-scroll {
      overflow: auto;
      max-height: 680px;
      border: 1px solid var(--line);
      border-radius: 8px;
    }

    table {
      width: 100%;
      border-collapse: collapse;
      min-width: 980px;
      font-size: 14px;
    }

    th,
    td {
      border-bottom: 1px solid var(--line);
      padding: 11px 12px;
      text-align: left;
      vertical-align: top;
    }

    th {
      position: sticky;
      top: 0;
      z-index: 1;
      background: #f8fafc;
      color: var(--muted);
      font-size: 12px;
      text-transform: uppercase;
    }

    td small {
      display: block;
      color: var(--muted);
      margin-top: 2px;
    }

    code {
      color: var(--warn);
      background: var(--warn-soft);
      border-radius: 4px;
      padding: 2px 4px;
      white-space: normal;
      word-break: break-all;
      font-size: 12px;
    }

    footer {
      max-width: 1180px;
      margin: 0 auto;
      padding: 0 24px 32px;
      color: var(--muted);
      font-size: 13px;
    }

    @media (max-width: 860px) {
      header {
        padding: 30px 22px;
      }

      h1 {
        font-size: 28px;
      }

      .metrics,
      .grid {
        grid-template-columns: 1fr;
      }

      .bars li {
        grid-template-columns: 1fr;
        gap: 6px;
      }

      .bar-count {
        text-align: left;
      }
    }
  </style>
</head>
<body>
  <header>
    <div>
      <h1>Mail System SBOM Report</h1>
      <p>Readable overview generated from CycloneDX backend and frontend software bill of materials files.</p>
    </div>
  </header>
  <main>
    <section class="metrics">
      ${summaryCard('Total Components', components.length, 'Backend and frontend dependencies')}
      ${summaryCard('Backend Components', bySource.find(([name]) => name === 'Backend')?.[1] ?? 0, 'Gradle CycloneDX SBOM')}
      ${summaryCard('Frontend Components', bySource.find(([name]) => name === 'Frontend')?.[1] ?? 0, 'npm CycloneDX SBOM')}
      ${summaryCard('Unknown Licenses', withUnknownLicense, 'Components without license metadata')}
    </section>

    <section class="grid">
      ${barList('Components By Source', bySource)}
      ${barList('Package Ecosystems', byPackageType)}
      ${barList('Top Licenses', byLicense)}
      <section class="panel">
        <h2>SBOM Files</h2>
        <ul class="bars">
          ${boms.map((bom) => `
            <li>
              <span class="bar-label">${escapeHtml(bom.label)} ${escapeHtml(bom.bomFormat)} ${escapeHtml(bom.specVersion)}</span>
              <span class="bar-track"><span class="bar-fill" style="width: 100%"></span></span>
              <span class="bar-count">${bom.components.length}</span>
            </li>
          `).join('')}
        </ul>
      </section>
    </section>

    <section class="table-wrap">
      <h2>Components</h2>
      <div class="search">
        <input id="search" type="search" placeholder="Search by package, version, license, purl, backend, frontend">
        <span id="result-count">${components.length} shown</span>
      </div>
      <div class="table-scroll">
        <table>
          <thead>
            <tr>
              <th>Source</th>
              <th>Type</th>
              <th>Component</th>
              <th>Version</th>
              <th>License</th>
              <th>Package URL / Reference</th>
            </tr>
          </thead>
          <tbody id="components">
            ${renderTable(components)}
          </tbody>
        </table>
      </div>
    </section>
  </main>
  <footer>
    Generated at ${escapeHtml(generatedAt)} from ${escapeHtml(reports.map((report) => report.file).join(' and '))}.
  </footer>
  <script>
    const search = document.querySelector('#search');
    const rows = Array.from(document.querySelectorAll('#components tr'));
    const resultCount = document.querySelector('#result-count');

    search.addEventListener('input', () => {
      const query = search.value.trim().toLowerCase();
      let visible = 0;

      for (const row of rows) {
        const matches = !query || row.textContent.toLowerCase().includes(query);
        row.style.display = matches ? '' : 'none';
        if (matches) visible += 1;
      }

      resultCount.textContent = visible + ' shown';
    });
  </script>
</body>
</html>`;
}

const outputPath = resolve(outputFile);
mkdirSync(dirname(outputPath), { recursive: true });
writeFileSync(outputPath, renderReport(), 'utf8');
console.log(`Wrote ${outputFile}`);
