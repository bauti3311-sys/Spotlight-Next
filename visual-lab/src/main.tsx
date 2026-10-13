import { useEffect, useRef, useState } from 'react';
import { createRoot } from 'react-dom/client';
import './style.css';

const references = [
  ['Arc UI · Command Palette', 'https://uiarc.dev/components/blocks/command-palette'],
  ['EasyUI · Command Menu / Glass Navbar', 'https://github.com/Surajmaurya1/easyui'],
  ['Motion Primitives · Animated Background', 'https://motion-primitives.com/docs/animated-background'],
  ['MicroKit · Spotlight Indicator', 'https://microkit.co/components/spotlight-indicator'],
  ['Aceternity · Gooey Dropdown', 'https://ui.aceternity.com/labs'],
  ['SmoothUI · Gooey Popover', 'https://smoothui.dev/docs/components/gooey-popover'],
  ['Arc UI · Resizable Panels', 'https://uiarc.dev/components/resizable-panels'],
  ['Motion Primitives · Morphing Dialog', 'https://motion-primitives.com/docs/morphing-dialog'],
  ['Aceternity · Expandable Card / Notch', 'https://ui.aceternity.com/components/expandable-card'],
  ['Hugeicons · Stroke Rounded', 'https://hugeicons.com/'],
  ['Phosphor · Regular / Duotone', 'https://phosphoricons.com/'],
];
const files = ['Guía de aviación.pdf', 'Checklist.txt', 'Notas de navegación.pdf'];
const actions = ['Abrir', 'Copiar nombre', 'Añadir a selección'];

function Lab() {
  const [query, setQuery] = useState('');
  const [selected, setSelected] = useState(0);
  const [preview, setPreview] = useState(false);
  const [expanded, setExpanded] = useState(false);
  const [menu, setMenu] = useState(false);
  const [action, setAction] = useState(0);
  const [feedback, setFeedback] = useState('');
  const [panelWidth, setPanelWidth] = useState(320);
  const search = useRef<HTMLInputElement>(null);
  const actionButtons = useRef<(HTMLButtonElement | null)[]>([]);
  const shown = files.filter(name => name.toLocaleLowerCase().includes(query.toLocaleLowerCase()));
  const chosen = shown[selected];

  useEffect(() => { if (menu) actionButtons.current[action]?.focus(); }, [menu, action]);
  function runAction(index: number) { setFeedback(`${actions[index]} · ${chosen}`); }
  function key(event: React.KeyboardEvent) {
    if (event.key === 'Tab' && chosen) {
      event.preventDefault(); setPreview(true);
      setAction(menu ? (action + (event.shiftKey ? -1 : 1) + actions.length) % actions.length : 0);
      setMenu(true);
    } else if (event.key === 'Enter' && chosen) {
      event.preventDefault(); if (menu) runAction(action); else setFeedback(`Abrir · ${chosen}`);
    } else if (event.key === 'Escape') {
      event.preventDefault();
      if (menu) setMenu(false); else if (expanded) setExpanded(false); else setPreview(false);
      search.current?.focus();
    } else if (!menu && (event.key === 'ArrowDown' || event.key === 'ArrowUp')) {
      event.preventDefault(); setSelected(Math.max(0, Math.min(shown.length - 1, selected + (event.key === 'ArrowDown' ? 1 : -1))));
      setPreview(true);
    }
  }
  return <main>
    <header><span className="eyebrow">Spotlight Next · investigación</span><h1>Laboratorio visual</h1><p>Patrones interactivos con datos sintéticos para estudiar selección, foco y paneles antes de recrearlos en Compose.</p></header>
    <section className="palette" aria-label="Prototipo de referencias" onKeyDown={key}>
      <div className="search"><span aria-hidden="true">⌕</span><input ref={search} autoFocus aria-label="Buscar referencias" placeholder="Buscar documentos de prueba…" value={query} onChange={event => {setQuery(event.target.value);setSelected(0);setMenu(false);setPreview(false);}}/><kbd>Tab</kbd></div>
      <div className={`workspace ${expanded ? 'expanded' : ''}`} style={{ '--panel': `${panelWidth}px` } as React.CSSProperties}>
        <div className="results" role="listbox" aria-label="Resultados">
          {shown.map((name, index) => <button role="option" aria-selected={index === selected} key={name} className={index === selected ? 'selected' : ''} onClick={() => {setSelected(index);setPreview(true);setMenu(false);}}><span aria-hidden="true">▤</span><div><strong>{name}</strong><small>Archivo sintético · selección contextual</small></div><span className="indicator"/></button>)}
          {shown.length === 0 && <p className="empty">Sin resultados de prueba</p>}
        </div>
        {preview && chosen && <aside aria-label="Panel lateral">
          <div className="panel-heading"><strong>{chosen}</strong><button aria-label="Ampliar vista previa" onClick={() => setExpanded(!expanded)}>{expanded ? 'Reducir' : 'Ampliar'}</button></div>
          <div className="document"><span>QUICK LOOK · REFERENCIA</span><h2>Guía de navegación</h2><p>Ejemplo de distribución del panel. El renderizado PDF real se comprueba en Android con PdfRenderer.</p><div className="lines"/><small>Documento simulado · 1 / 2</small></div>
          <button className="context-button" onClick={() => {setMenu(!menu);setAction(0);}}>Acciones contextuales</button>
          {menu && <div className="actions" aria-label="Acciones">{actions.map((name, index) => <button ref={node => {actionButtons.current[index] = node;}} className={index === action ? 'focused' : ''} key={name} onClick={() => runAction(index)}>{name}<kbd>{index === action ? 'Enter' : ''}</kbd></button>)}</div>}
        </aside>}
      </div>
      <footer>Enter ejecuta · Tab / Shift+Tab recorre acciones · Escape vuelve</footer>
    </section>
    <p role="status">{feedback || 'Elegí un resultado para inspeccionar el panel.'}</p>
    <section className="inspector"><label>Ancho del panel <input aria-label="Ancho del panel" type="range" min="240" max="440" value={panelWidth} onChange={event => setPanelWidth(Number(event.target.value))}/></label><p>Implementación propia de laboratorio; las referencias externas requieren revisión de licencia antes de reutilizar código o recursos.</p></section>
    <section className="references"><h2>Referencias aprobadas</h2>{references.map(([name, url]) => <a key={url} href={url} target="_blank" rel="noreferrer">{name}<span>↗</span></a>)}</section>
  </main>;
}
createRoot(document.getElementById('root')!).render(<Lab/>);
