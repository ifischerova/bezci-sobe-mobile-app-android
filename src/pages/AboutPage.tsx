import { Target, Users, Sparkles, Calendar, Car, Search, Handshake, Sprout, PiggyBank, Heart } from 'lucide-react';
import { useTranslation } from '../contexts/LanguageContext';

export const AboutPage = () => {
  const { t } = useTranslation();
  return (
    <div className="section-container animate-fade-in">
      <div className="text-center mb-12">
        <h1 className="text-4xl md:text-5xl/tight font-bold bg-gradient-to-r from-primary-600 to-accent-600 bg-clip-text text-transparent mb-4 leading-tight pb-[5px]">
          {t('about.title')}
        </h1>
        <p className="text-lg text-dark-600 dark:text-dark-300 max-w-2xl mx-auto">
          {t('about.subtitle')}
        </p>
      </div>

      <section className="card-modern p-8 mb-8 max-w-4xl mx-auto">
        <div className="flex items-center mb-4">
          <div className="w-12 h-12 bg-gradient-to-br from-primary-500 to-accent-500 rounded-xl flex items-center justify-center mr-4">
            <Target size={24} strokeWidth={1.5} className="text-white" />
          </div>
          <h2 className="text-3xl font-bold text-dark-800 dark:text-dark-50">{t('about.vision.title')}</h2>
        </div>
        <p className="text-dark-700 dark:text-dark-200 leading-relaxed text-lg">
          {t('about.vision.body')}
        </p>
      </section>

      <section className="card-modern p-8 mb-8 max-w-4xl mx-auto">
        <div className="flex items-center mb-4">
          <div className="w-12 h-12 bg-gradient-to-br from-accent-500 to-primary-500 rounded-xl flex items-center justify-center mr-4">
            <Users size={24} strokeWidth={1.5} className="text-white" />
          </div>
          <h2 className="text-3xl font-bold text-dark-800 dark:text-dark-50">{t('about.who.title')}</h2>
        </div>
        <p className="text-dark-700 dark:text-dark-200 leading-relaxed text-lg">
          {t('about.who.body')}
        </p>
      </section>

      <section className="glass-card p-8 mb-8 max-w-4xl mx-auto">
        <div className="flex items-center mb-6">
          <div className="w-12 h-12 bg-gradient-to-br from-primary-500 to-primary-600 rounded-xl flex items-center justify-center mr-4">
            <Sparkles size={24} strokeWidth={1.5} className="text-white" />
          </div>
          <h2 className="text-3xl font-bold text-dark-800 dark:text-dark-50">{t('about.offer.title')}</h2>
        </div>
        <div className="grid md:grid-cols-2 gap-4">
          <div className="flex items-start space-x-3 bg-white/50 dark:bg-surface-850/60 rounded-xl p-4">
            <Calendar size={24} strokeWidth={1.5} className="text-primary-600 dark:text-primary-300 flex-shrink-0" />
            <div>
              <h3 className="font-semibold text-dark-800 dark:text-dark-50">{t('about.offer.calendar.title')}</h3>
              <p className="text-sm text-dark-600 dark:text-dark-300">{t('about.offer.calendar.desc')}</p>
            </div>
          </div>
          <div className="flex items-start space-x-3 bg-white/50 dark:bg-surface-850/60 rounded-xl p-4">
            <Car size={24} strokeWidth={1.5} className="text-primary-600 dark:text-primary-300 flex-shrink-0" />
            <div>
              <h3 className="font-semibold text-dark-800 dark:text-dark-50">{t('about.offer.seats.title')}</h3>
              <p className="text-sm text-dark-600 dark:text-dark-300">{t('about.offer.seats.desc')}</p>
            </div>
          </div>
          <div className="flex items-start space-x-3 bg-white/50 dark:bg-surface-850/60 rounded-xl p-4">
            <Search size={24} strokeWidth={1.5} className="text-primary-600 dark:text-primary-300 flex-shrink-0" />
            <div>
              <h3 className="font-semibold text-dark-800 dark:text-dark-50">{t('about.offer.search.title')}</h3>
              <p className="text-sm text-dark-600 dark:text-dark-300">{t('about.offer.search.desc')}</p>
            </div>
          </div>
          <div className="flex items-start space-x-3 bg-white/50 dark:bg-surface-850/60 rounded-xl p-4">
            <Handshake size={24} strokeWidth={1.5} className="text-primary-600 dark:text-primary-300 flex-shrink-0" />
            <div>
              <h3 className="font-semibold text-dark-800 dark:text-dark-50">{t('about.offer.connect.title')}</h3>
              <p className="text-sm text-dark-600 dark:text-dark-300">{t('about.offer.connect.desc')}</p>
            </div>
          </div>
          <div className="flex items-start space-x-3 bg-white/50 dark:bg-surface-850/60 rounded-xl p-4">
            <Sprout size={24} strokeWidth={1.5} className="text-accent-600 dark:text-accent-300 flex-shrink-0" />
            <div>
              <h3 className="font-semibold text-dark-800 dark:text-dark-50">{t('about.offer.eco.title')}</h3>
              <p className="text-sm text-dark-600 dark:text-dark-300">{t('about.offer.eco.desc')}</p>
            </div>
          </div>
          <div className="flex items-start space-x-3 bg-white/50 dark:bg-surface-850/60 rounded-xl p-4">
            <PiggyBank size={24} strokeWidth={1.5} className="text-primary-600 dark:text-primary-300 flex-shrink-0" />
            <div>
              <h3 className="font-semibold text-dark-800 dark:text-dark-50">{t('about.offer.savings.title')}</h3>
              <p className="text-sm text-dark-600 dark:text-dark-300">{t('about.offer.savings.desc')}</p>
            </div>
          </div>
        </div>
      </section>

      <section className="card-modern p-8 max-w-4xl mx-auto">
        <div className="flex items-center mb-6">
          <div className="w-12 h-12 bg-gradient-to-br from-accent-500 to-accent-600 rounded-xl flex items-center justify-center mr-4">
            <Heart size={24} strokeWidth={1.5} className="text-white" />
          </div>
          <h2 className="text-3xl font-bold text-dark-800 dark:text-dark-50">{t('about.values.title')}</h2>
        </div>
        <div className="space-y-6">
          <div className="feature-card">
            <h3 className="text-xl font-bold text-dark-800 dark:text-dark-50 mb-2 flex items-center">
              <span className="text-2xl mr-3"></span>
              {t('about.values.sustainability.title')}
            </h3>
            <p className="text-dark-700 dark:text-dark-200 leading-relaxed">
              {t('about.values.sustainability.body')}
            </p>
          </div>
          <div className="feature-card">
            <h3 className="text-xl font-bold text-dark-800 dark:text-dark-50 mb-2 flex items-center">
              <span className="text-2xl mr-3"></span>
              {t('about.values.community.title')}
            </h3>
            <p className="text-dark-700 dark:text-dark-200 leading-relaxed">
              {t('about.values.community.body')}
            </p>
          </div>
          <div className="feature-card">
            <h3 className="text-xl font-bold text-dark-800 dark:text-dark-50 mb-2 flex items-center">
              <span className="text-2xl mr-3"></span>
              {t('about.values.support.title')}
            </h3>
            <p className="text-dark-700 dark:text-dark-200 leading-relaxed">
              {t('about.values.support.body')}
            </p>
          </div>
        </div>
      </section>
    </div>
  );
};
