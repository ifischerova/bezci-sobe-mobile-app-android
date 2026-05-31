import { Heart } from 'lucide-react';
import { useTranslation } from '../../contexts/LanguageContext';

export const Footer = () => {
  const { t } = useTranslation();
  const currentYear = new Date().getFullYear();

  return (
    <footer className="glass-card border-t border-white/30 dark:border-white/5 mt-auto">
      <div className="container mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Bottom Bar */}
        <div className="text-center">
          <p className="text-sm text-dark-600 dark:text-dark-300 inline-flex items-center gap-1">
            {currentYear} © {t('common.footer.madeWith')}
            <Heart size={14} strokeWidth={1.5} className="text-red-500 animate-pulse" />
            by <span className="font-semibold text-primary-600 dark:text-primary-300">Iva Fischerová</span>
          </p>
          <p className="text-xs text-dark-500 dark:text-dark-400 mt-2">
            {t('common.footer.tagline')}
          </p>
        </div>
      </div>
    </footer>
  );
};
