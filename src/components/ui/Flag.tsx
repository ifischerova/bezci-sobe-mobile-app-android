import type { Locale } from '../../i18n/translations';

interface FlagProps {
  code: Locale;
  size?: number;
  className?: string;
}

// Inline-SVG flag icons. Czech is straightforward (two stripes + triangle).
// The British flag (used here as a stand-in for the English locale) is the
// Union Jack — a few overlaid rectangles + crosses keeps it recognisable
// without being a 2 KB blob.

export function Flag({ code, size = 20, className = '' }: FlagProps) {
  if (code === 'cs') {
    return (
      <svg
        viewBox="0 0 60 40"
        width={size * 1.5}
        height={size}
        role="img"
        aria-hidden="true"
        className={`rounded-sm border border-gray-200/60 dark:border-white/20 ${className}`}
      >
        <rect width="60" height="20" fill="#ffffff" />
        <rect y="20" width="60" height="20" fill="#d7141a" />
        <polygon points="0,0 30,20 0,40" fill="#11457e" />
      </svg>
    );
  }

  // Union Jack (used for the English locale).
  return (
    <svg
      viewBox="0 0 60 40"
      width={size * 1.5}
      height={size}
      role="img"
      aria-hidden="true"
      className={`rounded-sm border border-gray-200/60 dark:border-white/20 ${className}`}
    >
      <rect width="60" height="40" fill="#012169" />
      {/* white diagonals */}
      <path d="M0,0 L60,40 M60,0 L0,40" stroke="#ffffff" strokeWidth="8" />
      {/* red diagonals (St Patrick) — split so they look offset */}
      <path d="M0,0 L60,40" stroke="#c8102e" strokeWidth="4" />
      <path d="M60,0 L0,40" stroke="#c8102e" strokeWidth="4" />
      {/* white cross (St George background) */}
      <path d="M30,0 V40 M0,20 H60" stroke="#ffffff" strokeWidth="12" />
      {/* red cross (St George) */}
      <path d="M30,0 V40 M0,20 H60" stroke="#c8102e" strokeWidth="6" />
    </svg>
  );
}
