import { useTranslation } from '../../contexts/LanguageContext';
import { Flag } from './Flag';
import type { Locale } from '../../i18n/translations';

interface LanguageSwitcherProps {
  className?: string;
}

// Shows a single flag — the *other* locale. Clicking it flips the language.
export function LanguageSwitcher({ className = '' }: LanguageSwitcherProps) {
  const { locale, setLocale, t } = useTranslation();
  const nextLocale: Locale = locale === 'cs' ? 'en' : 'cs';
  const labelKey =
    nextLocale === 'cs'
      ? ('common.languageSwitcher.cs' as const)
      : ('common.languageSwitcher.en' as const);
  const label = t(labelKey);

  return (
    <button
      type="button"
      onClick={() => setLocale(nextLocale)}
      aria-label={label}
      title={label}
      className={`flex items-center justify-center w-9 h-7 rounded-md border border-transparent transition-all duration-200 opacity-80 hover:opacity-100 hover:border-gray-300 dark:hover:border-surface-700 focus:outline-none focus:ring-2 focus:ring-primary-500 dark:focus:ring-primary-400 focus:ring-offset-1 focus:ring-offset-white/50 dark:focus:ring-offset-surface-900 ${className}`}
    >
      <Flag code={nextLocale} size={20} />
    </button>
  );
}
