import { Moon, Sun } from 'lucide-react';
import { useTheme } from '../../contexts/ThemeContext';
import { useTranslation } from '../../contexts/LanguageContext';

// Floating circular toggle, fixed bottom-right of the viewport. Always reachable.
export function ThemeSwitcher() {
  const { theme, toggleTheme } = useTheme();
  const { t } = useTranslation();
  const isDark = theme === 'dark';
  const label = isDark
    ? t('common.themeSwitcher.toLight')
    : t('common.themeSwitcher.toDark');

  return (
    <button
      type="button"
      onClick={toggleTheme}
      aria-label={label}
      aria-pressed={isDark}
      title={label}
      className="fixed bottom-6 right-6 z-40 flex items-center justify-center w-12 h-12 rounded-full shadow-lg backdrop-blur-md bg-white/80 dark:bg-surface-900/80 border border-white/30 dark:border-white/10 hover:scale-110 active:scale-95 transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-primary-500 dark:focus:ring-primary-400 focus:ring-offset-2 focus:ring-offset-transparent"
    >
      {isDark ? (
        <Sun size={20} className="text-primary-300" strokeWidth={2} />
      ) : (
        <Moon size={20} className="text-primary-600" strokeWidth={2} />
      )}
    </button>
  );
}
