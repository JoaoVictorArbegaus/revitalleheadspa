import type { PointerEvent } from 'react';

/*
 * Borda que acende perto do cursor (adaptado de BorderGlow, React Bits, MIT).
 * Diferente do original, não pinta o fundo do cartão: só a borda e o halo, para não matar o vidro.
 * Uso: classe `bglow` no cartão, <BorderGlowLayers /> dentro dele e borderGlowMove no onPointerMove.
 */

export function borderGlowMove(e: PointerEvent<HTMLElement>) {
  if (e.pointerType !== 'mouse') return;
  const el = e.currentTarget;
  const r = el.getBoundingClientRect();
  const cx = r.width / 2;
  const cy = r.height / 2;
  const dx = e.clientX - r.left - cx;
  const dy = e.clientY - r.top - cy;
  // proximidade da borda: 0 no centro, 1 na borda
  const kx = dx !== 0 ? cx / Math.abs(dx) : Infinity;
  const ky = dy !== 0 ? cy / Math.abs(dy) : Infinity;
  const edge = Math.min(Math.max(1 / Math.min(kx, ky), 0), 1);
  let deg = (Math.atan2(dy, dx) * 180) / Math.PI + 90;
  if (deg < 0) deg += 360;
  el.style.setProperty('--edge-proximity', (edge * 100).toFixed(2));
  el.style.setProperty('--cursor-angle', `${deg.toFixed(2)}deg`);
}

export function BorderGlowLayers() {
  return (
    <>
      <span className="bglow__cone" aria-hidden="true">
        <span className="bglow__ring" />
      </span>
      <span className="bglow__edge" aria-hidden="true" />
    </>
  );
}
