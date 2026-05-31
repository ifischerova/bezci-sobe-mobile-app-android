import { Trophy, Megaphone, Sprout, TrendingUp, Handshake, BarChart3 } from 'lucide-react';
import { useTranslation } from '../contexts/LanguageContext';

export const OrganizersPage = () => {
  const { t } = useTranslation();
  return (
    <div className="section-container animate-fade-in">
      <div className="text-center mb-12">
        <div className="inline-block w-16 h-16 bg-gradient-to-br from-accent-500 to-primary-500 rounded-2xl flex items-center justify-center mb-4 mx-auto animate-bounce-slow">
          <Trophy size={32} strokeWidth={1.5} className="text-white" />
        </div>
        <h1 className="text-4xl md:text-5xl/tight font-bold bg-gradient-to-r from-accent-600 to-primary-600 bg-clip-text text-transparent mb-4 leading-tight pb-[5px]">
          {t('organizers.title')}
        </h1>
        <p className="text-lg text-dark-600 dark:text-dark-300 max-w-2xl mx-auto">
          {t('organizers.subtitle')}
        </p>
      </div>

      <section className="glass-card p-8 mb-8 max-w-4xl mx-auto">
        <h2 className="text-3xl font-bold text-dark-800 dark:text-dark-50 mb-4">{t('organizers.intro.title')}</h2>
        <p className="text-dark-700 dark:text-dark-200 leading-relaxed text-lg">
          {t('organizers.intro.body')}
        </p>
      </section>

      <section className="card-modern p-8 mb-8 max-w-4xl mx-auto">
        <h2 className="text-3xl font-bold text-dark-800 dark:text-dark-50 mb-6">{t('organizers.offer.title')}</h2>
        <div className="space-y-4">
          <div className="feature-card">
            <div className="flex items-start space-x-4">
              <Megaphone size={28} strokeWidth={1.5} className="text-primary-600 dark:text-primary-300 flex-shrink-0 mt-1" />
              <div>
                <h3 className="font-bold text-lg text-dark-800 dark:text-dark-50 mb-1">{t('organizers.offer.promotion.title')}</h3>
                <p className="text-dark-600 dark:text-dark-300">
                  {t('organizers.offer.promotion.desc')}
                </p>
              </div>
            </div>
          </div>
          <div className="feature-card">
            <div className="flex items-start space-x-4">
              <Sprout size={28} strokeWidth={1.5} className="text-accent-600 dark:text-accent-300 flex-shrink-0 mt-1" />
              <div>
                <h3 className="font-bold text-lg text-dark-800 dark:text-dark-50 mb-1">{t('organizers.offer.eco.title')}</h3>
                <p className="text-dark-600 dark:text-dark-300">
                  {t('organizers.offer.eco.desc')}
                </p>
              </div>
            </div>
          </div>
          <div className="feature-card">
            <div className="flex items-start space-x-4">
              <TrendingUp size={28} strokeWidth={1.5} className="text-primary-600 dark:text-primary-300 flex-shrink-0 mt-1" />
              <div>
                <h3 className="font-bold text-lg text-dark-800 dark:text-dark-50 mb-1">{t('organizers.offer.attendance.title')}</h3>
                <p className="text-dark-600 dark:text-dark-300">
                  {t('organizers.offer.attendance.desc')}
                </p>
              </div>
            </div>
          </div>
          <div className="feature-card">
            <div className="flex items-start space-x-4">
              <Handshake size={28} strokeWidth={1.5} className="text-primary-600 dark:text-primary-300 flex-shrink-0 mt-1" />
              <div>
                <h3 className="font-bold text-lg text-dark-800 dark:text-dark-50 mb-1">{t('organizers.offer.community.title')}</h3>
                <p className="text-dark-600 dark:text-dark-300">
                  {t('organizers.offer.community.desc')}
                </p>
              </div>
            </div>
          </div>
          <div className="feature-card">
            <div className="flex items-start space-x-4">
              <BarChart3 size={28} strokeWidth={1.5} className="text-primary-600 dark:text-primary-300 flex-shrink-0 mt-1" />
              <div>
                <h3 className="font-bold text-lg text-dark-800 dark:text-dark-50 mb-1">{t('organizers.offer.stats.title')}</h3>
                <p className="text-dark-600 dark:text-dark-300">
                  {t('organizers.offer.stats.desc')}
                </p>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section className="glass-card p-8 mb-8 max-w-4xl mx-auto">
        <h2 className="text-3xl font-bold text-dark-800 dark:text-dark-50 mb-6">{t('organizers.how.title')}</h2>
        <div className="space-y-4">
          <div className="flex items-start space-x-4 p-4 bg-gradient-to-r from-primary-50 to-primary-100 dark:from-primary-900/30 dark:to-primary-800/30 rounded-xl">
            <div className="flex-shrink-0 w-10 h-10 bg-gradient-to-br from-primary-500 to-primary-600 rounded-xl flex items-center justify-center text-white font-bold">
              1
            </div>
            <div>
              <h3 className="font-bold text-lg text-dark-800 dark:text-dark-50">{t('organizers.how.register.title')}</h3>
              <p className="text-dark-600 dark:text-dark-300">{t('organizers.how.register.desc')}</p>
            </div>
          </div>
          <div className="flex items-start space-x-4 p-4 bg-gradient-to-r from-accent-50 to-accent-100 dark:from-accent-900/30 dark:to-accent-800/30 rounded-xl">
            <div className="flex-shrink-0 w-10 h-10 bg-gradient-to-br from-accent-500 to-accent-600 rounded-xl flex items-center justify-center text-white font-bold">
              2
            </div>
            <div>
              <h3 className="font-bold text-lg text-dark-800 dark:text-dark-50">{t('organizers.how.promote.title')}</h3>
              <p className="text-dark-600 dark:text-dark-300">{t('organizers.how.promote.desc')}</p>
            </div>
          </div>
          <div className="flex items-start space-x-4 p-4 bg-gradient-to-r from-primary-50 to-primary-100 dark:from-primary-900/30 dark:to-primary-800/30 rounded-xl">
            <div className="flex-shrink-0 w-10 h-10 bg-gradient-to-br from-primary-500 to-primary-600 rounded-xl flex items-center justify-center text-white font-bold">
              3
            </div>
            <div>
              <h3 className="font-bold text-lg text-dark-800 dark:text-dark-50">{t('organizers.how.monitor.title')}</h3>
              <p className="text-dark-600 dark:text-dark-300">{t('organizers.how.monitor.desc')}</p>
            </div>
          </div>
          <div className="flex items-start space-x-4 p-4 bg-gradient-to-r from-accent-50 to-accent-100 dark:from-accent-900/30 dark:to-accent-800/30 rounded-xl">
            <div className="flex-shrink-0 w-10 h-10 bg-gradient-to-br from-accent-500 to-accent-600 rounded-xl flex items-center justify-center text-white font-bold">
              4
            </div>
            <div>
              <h3 className="font-bold text-lg text-dark-800 dark:text-dark-50">{t('organizers.how.sustain.title')}</h3>
              <p className="text-dark-600 dark:text-dark-300">{t('organizers.how.sustain.desc')}</p>
            </div>
          </div>
        </div>
      </section>

      <section className="card-modern p-8 max-w-3xl mx-auto text-center">
        <div className="mb-6">
          <h2 className="text-3xl font-bold text-dark-800 dark:text-dark-50 mb-3">{t('organizers.contact.title')}</h2>
          <p className="text-lg text-dark-600 dark:text-dark-300 mb-6">{t('organizers.contact.desc')}</p>
        </div>

        <a
          href="mailto:team@bezcisobe.cz"
          className="btn-accent-custom inline-block text-lg mb-6"
        >
          {t('organizers.contact.button')}
        </a>

        <p className="text-dark-600 dark:text-dark-300 leading-relaxed">
          {t('organizers.contact.footer')}
        </p>
      </section>
    </div>
  );
};
