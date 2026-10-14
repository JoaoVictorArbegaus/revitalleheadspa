import type { SVGProps } from 'react';

type IconProps = SVGProps<SVGSVGElement>;

/** Símbolos reutilizados por <use> e filtros SVG da refração do liquid glass (aplicados só no Chromium). */
export function SvgDefs() {
  return (
    <svg className="sr-only" aria-hidden="true" focusable="false" width="0" height="0">
      <filter id="lg-refract" x="0" y="0" width="100%" height="100%" colorInterpolationFilters="sRGB">
        <feTurbulence type="fractalNoise" baseFrequency="0.011 0.016" numOctaves={2} seed={4} result="n" />
        <feGaussianBlur in="n" stdDeviation="1.5" result="nb" />
        <feDisplacementMap in="SourceGraphic" in2="nb" scale={34} xChannelSelector="R" yChannelSelector="G" />
      </filter>
      <filter id="lg-refract-hover" x="0" y="0" width="100%" height="100%" colorInterpolationFilters="sRGB">
        <feTurbulence type="fractalNoise" baseFrequency="0.009 0.013" numOctaves={2} seed={9} result="n" />
        <feGaussianBlur in="n" stdDeviation="1.5" result="nb" />
        <feDisplacementMap in="SourceGraphic" in2="nb" scale={62} xChannelSelector="R" yChannelSelector="G" />
      </filter>
      <defs>
        <symbol id="i-wa" viewBox="0 0 24 24">
          <path
            fill="currentColor"
            d="M17.47 14.38c-.3-.15-1.76-.87-2.03-.97-.27-.1-.47-.15-.67.15-.2.3-.77.97-.94 1.16-.17.2-.35.22-.64.08-.3-.15-1.26-.47-2.39-1.48-.88-.79-1.48-1.76-1.65-2.06-.17-.3-.02-.46.13-.6.13-.14.3-.35.45-.52.15-.18.2-.3.3-.5.1-.2.05-.37-.03-.52-.07-.15-.67-1.61-.92-2.2-.24-.58-.49-.5-.67-.51h-.57c-.2 0-.52.07-.79.37-.27.3-1.04 1.02-1.04 2.48s1.07 2.88 1.21 3.07c.15.2 2.1 3.2 5.08 4.49.71.31 1.26.49 1.7.63.71.22 1.36.19 1.87.12.57-.09 1.76-.72 2-1.41.25-.7.25-1.29.18-1.41-.08-.13-.27-.2-.57-.35m-5.42 7.4h-.01a9.87 9.87 0 0 1-5.03-1.38l-.36-.21-3.74.98 1-3.65-.24-.37a9.86 9.86 0 0 1-1.51-5.26c0-5.45 4.44-9.88 9.89-9.88 2.64 0 5.12 1.03 6.99 2.9a9.83 9.83 0 0 1 2.89 6.99c0 5.45-4.44 9.88-9.88 9.88m8.41-18.3A11.82 11.82 0 0 0 12.05 0C5.5 0 .16 5.34.16 11.89c0 2.1.55 4.14 1.59 5.95L.06 24l6.3-1.65a11.88 11.88 0 0 0 5.68 1.45h.01c6.55 0 11.89-5.34 11.89-11.89a11.82 11.82 0 0 0-3.48-8.41z"
          />
        </symbol>
        <symbol id="i-star" viewBox="0 0 22 22">
          <path fill="currentColor" d="M11 1.6l2.78 6.02 6.58.7-4.92 4.43 1.38 6.48L11 15.92l-5.82 3.31 1.38-6.48L1.64 8.32l6.58-.7z" />
        </symbol>
        <symbol id="i-stars" viewBox="0 0 120 22">
          {[0, 24.5, 49, 73.5, 98].map((x) => (
            <use key={x} href="#i-star" x={x} width="22" height="22" />
          ))}
        </symbol>
      </defs>
    </svg>
  );
}

export function WhatsAppIcon(props: IconProps) {
  return (
    <svg className="ico" viewBox="0 0 24 24" aria-hidden="true" focusable="false" {...props}>
      <use href="#i-wa" />
    </svg>
  );
}

export function ArrowIcon() {
  return (
    <svg className="ico ico--arrow" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
      <path d="M5 12h14M13 6l6 6-6 6" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  );
}

export function Stars({ large = false, label }: { large?: boolean; label?: string }) {
  return (
    <span className={large ? 'stars stars--lg' : 'stars'} aria-hidden={label ? undefined : true} aria-label={label}>
      <svg viewBox="0 0 120 22" aria-hidden="true">
        <use href="#i-stars" />
      </svg>
    </span>
  );
}

const pillarPaths = {
  consulta: (
    <>
      <circle cx="16" cy="11" r="5.5" />
      <path d="M6 27c1.4-5.2 5.4-8 10-8s8.6 2.8 10 8" />
      <path d="M22.5 6.5l3-3M24.5 10h3.5" />
    </>
  ),
  ambiente: (
    <>
      <path d="M16 28c-6-3.6-9-8.4-9-13.2C7 9.6 11 5 16 4c5 1 9 5.6 9 10.8C25 19.6 22 24.4 16 28z" />
      <path d="M16 28V12M16 18l-4-4M16 22l5-5" />
    </>
  ),
  tecnologia: (
    <>
      <circle cx="16" cy="16" r="5" />
      <path d="M16 4v4M16 24v4M4 16h4M24 16h4M7.5 7.5l2.8 2.8M21.7 21.7l2.8 2.8M7.5 24.5l2.8-2.8M21.7 10.3l2.8-2.8" />
    </>
  ),
};

export function PillarIcon({ name }: { name: keyof typeof pillarPaths }) {
  return (
    <svg className="pillar__ico" viewBox="0 0 32 32" aria-hidden="true" focusable="false">
      {pillarPaths[name]}
    </svg>
  );
}
