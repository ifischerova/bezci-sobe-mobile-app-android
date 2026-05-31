import { createContext, useContext, useEffect, useMemo, useState } from 'react';
import type { ReactNode } from 'react';
import { translations } from '../i18n/translations';
import type { Locale, TranslationKey } from '../i18n/translations';

const STORAGE_KEY = 'bezci_locale';

interface LanguageContextType {
  locale: Locale;
  setLocale: (locale: Locale) => void;
  t: (key: TranslationKey, vars?: Record<string, string | number>) => string;
  formatDate: (iso: string) => string;
  formatDateTime: (iso: string) => string;
}

const LanguageContext = createContext<LanguageContextType | undefined>(undefined);

function resolveInitialLocale(): Locale {
  if (typeof window === 'undefined') return 'cs';
  const stored = window.localStorage.getItem(STORAGE_KEY);
  if (stored === 'cs' || stored === 'en') return stored;
  const browser = window.navigator?.language ?? '';
  return browser.toLowerCase().startsWith('cs') ? 'cs' : 'en';
}

export const LanguageProvider = ({ children }: { children: ReactNode }) => {
  const [locale, setLocaleState] = useState<Locale>(resolveInitialLocale);

  useEffect(() => {
    window.localStorage.setItem(STORAGE_KEY, locale);
    document.documentElement.lang = locale;
  }, [locale]);

  const value = useMemo<LanguageContextType>(() => {
    const t = (key: TranslationKey, vars?: Record<string, string | number>) => {
      let value = translations[locale][key] ?? key;
      if (vars) {
        for (const [k, v] of Object.entries(vars)) {
          value = value.replace(new RegExp(`\\{${k}\\}`, 'g'), String(v));
        }
      }
      return value;
    };

    const langTag = locale === 'cs' ? 'cs-CZ' : 'en-GB';

    const formatDate = (iso: string) => {
      try {
        return new Date(iso).toLocaleDateString(langTag, {
          weekday: 'long',
          day: 'numeric',
          month: 'long',
          year: 'numeric',
        });
      } catch {
        return iso;
      }
    };

    const formatDateTime = (iso: string) => {
      try {
        return new Date(iso).toLocaleString(langTag);
      } catch {
        return iso;
      }
    };

    return { locale, setLocale: setLocaleState, t, formatDate, formatDateTime };
  }, [locale]);

  return <LanguageContext.Provider value={value}>{children}</LanguageContext.Provider>;
};

export const useTranslation = (): LanguageContextType => {
  const ctx = useContext(LanguageContext);
  if (!ctx) {
    throw new Error('useTranslation must be used within a LanguageProvider');
  }
  return ctx;
};
